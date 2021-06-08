package com.zakl.statusManage;

import cn.hutool.core.lang.Pair;
import com.zakl.ack.AckHandlerManager;
import com.zakl.config.ServerConfig;
import com.zakl.dto.MqMessage;
import com.zakl.util.MqHandleUtil;
import com.zakl.msgdistribute.PubMsgBufBufHandler;
import com.zakl.redisinteractive.RedisUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.zakl.constant.Constants.*;
import static com.zakl.msgdistribute.KeyMsgDistributor.registerMsgDistributor;
import static com.zakl.util.MqHandleUtil.checkIfSortedSet;
import static com.zakl.redisinteractive.RedisUtil.syncKeys;
import static com.zakl.statusManage.MqKeyHandleStatusManager.*;

/**
 * @author ZhangJiaKui
 * @classname StatusManager
 * @description
 * @date 6/1/2021 5:22 PM
 */
@Slf4j
public class StatusManager {


    private final static String KEY_HOLDER = "holder";

    private static volatile boolean INIT_FROM_REDIS = false;


    private static final Comparator<SubClientInfo> subClientComparator = (o1, o2) -> o2.getWeight() - o1.getWeight();

    /**
     * initNewKey Status
     *
     * @param keyName
     * @param clients
     */
    public static void initNewKey(String keyName, SubClientInfo... clients) {
        log.info("init new key  {} 's status", keyName);
        resetKeyStatus(keyName, new AtomicBoolean(true));
        if (checkIfSortedSet(keyName)) {
            log.info("start register new sorted Set in redis");
            registerNewSortedSetInRedis(keyName);
            log.info("start register new sorted Set in redis succeed ");
        }
        initKeyBuffer(keyName);
        if (clients.length > 0) {
            PriorityBlockingQueue<SubClientInfo> clientPq = new PriorityBlockingQueue<>(16, subClientComparator);
            keyClientsMap.put(keyName, clientPq);
            for (SubClientInfo client : clients) {
                log.info("register new client {} to server ", client);
                clientIdMap.put(keyName, client);
                clientPq.add(client);
            }
        }
        registerMsgDistributor(keyName);
    }

    /**
     * resetKey consumestatus
     *
     * @param keyName
     * @param canConsumeFlag
     */
    public static void resetKeyStatus(String keyName, AtomicBoolean canConsumeFlag) {
        ReentrantLock lock = new ReentrantLock();
        keyHandleConditionMap.put(keyName, lock.newCondition());
        keyHandleLockMap.put(keyName, lock);
        canConsumeStatusMap.put(keyName, canConsumeFlag);
    }


    /**
     * when application start ,call this method to let
     * MqKeyHandleStatusManager
     * init,so that can keep same keys info which in redis;
     */
    public static void initFromRedis() {
        if (INIT_FROM_REDIS) {
            throw new RuntimeException("method initFromRedis only for main ,and once limit!");
        }

        log.info("start init keys info from redis");
        List<String> keys = syncKeys(REDIS_PREFIX + "*");
        for (String key : keys) {
            //data key
            if (key.startsWith(MQ_LIST_PREFIX) || key.startsWith(MQ_SORTED_SET_PREFIX)) {
                keyClientsMap.put(key, new PriorityBlockingQueue<>(16, subClientComparator));

                resetKeyStatus(key, new AtomicBoolean(true));

                initKeyBuffer(key);

                registerMsgDistributor(key);
            }
            //ack key
            else if (key.equals(ACK_SET_KEY)) {
                log.info("begin handle ack backup  from redis:{}", ACK_SET_KEY);
                Set<String> ackMsgBackup = RedisUtil.syncSetPopAll(ACK_SET_KEY);
                List<MqMessage> sortedSetMsg = new ArrayList<>();
                List<MqMessage> listMsg = new ArrayList<>();
                for (String s : ackMsgBackup) {
                    MqMessage mqMessage = MqHandleUtil.convertJsonToMqMessage(s);
                    if (MqHandleUtil.checkIfSortedSet(mqMessage.getKey())) {
                        sortedSetMsg.add(mqMessage);
                    } else {
                        listMsg.add(mqMessage);
                    }
                }
                RedisUtil.syncSortedSetAdd(sortedSetMsg.toArray(new MqMessage[0]));
                RedisUtil.syncListRPush(listMsg.toArray(new MqMessage[0]));
                log.info("handle ack backup from redis to target key succeed");
            }

        }
        log.info("init keys info from redis succeed");

        INIT_FROM_REDIS = true;

    }


    /**
     * register a new SortedSet to Redis Server
     *
     * @param channelName
     */
    public static void registerNewSortedSetInRedis(String channelName) {

        PubMsgBufBufHandler.registerNewSortedSetBuf(channelName);
        RedisUtil.syncSortedSetAdd(channelName, new Pair<>(MIN_SCORE, KEY_HOLDER + channelName));
    }


    /**
     * remind key's consume thread can continue consume
     *
     * @param keyName
     */
    public static void remindDistributeThreadConsume(String keyName) {
        Condition condition = keyHandleConditionMap.get(keyName);
        Lock lock = keyHandleLockMap.get(keyName);
        AtomicBoolean flag = canConsumeStatusMap.get(keyName);
        //if current state is can't consume(key is empty or client queue is empty)
        if (!flag.get()) {
            try {
                lock.lock();
                condition.signal();
                flag.set(true);
            } finally {
                lock.unlock();
            }
        }
    }

    public static void suspendDistributeThread(String keyName) {
        AtomicBoolean canConsumeFlag = MqKeyHandleStatusManager.canConsumeStatusMap.get(keyName);
        canConsumeFlag.set(false);
    }

    public static void cleanUpOffLiveSubClient(SubClientInfo clientInfo, String... keys) {
        log.info("subClient: {} is offline,clean its status", clientInfo);
        for (String key : keys) {
            keyClientsMap.get(key).remove(clientInfo);
        }
        AckHandlerManager.removeAckHandleThread(clientInfo);
    }

    private static void initKeyBuffer(String keyName) {
        if (MqHandleUtil.checkIfSortedSet(keyName)) {
            //buff 缓冲区也需要支持 优先级
            keyMessagesBufMap.put(keyName, new PriorityBlockingQueue<>(ServerConfig.PUB_BUFFER_MAX_LIMIT, (o1, o2) -> Double.compare(o2.getWeight(),o1.getWeight())));
        } else if (MqHandleUtil.checkIfList(keyName)) {
            keyMessagesBufMap.put(keyName, new LinkedBlockingDeque<>());
        } else {
            throw new RuntimeException("invalid keyName:" + keyName);
        }
    }
}

