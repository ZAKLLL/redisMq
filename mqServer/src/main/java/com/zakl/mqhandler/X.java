package com.zakl.mqhandler;


import cn.hutool.core.lang.Pair;
import com.zakl.statusManage.SubClientInfo;
import com.zakl.dto.MqMessage;
import com.zakl.protocol.MqSubMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.zakl.statusManage.StatusManager.statusMap;
import static com.zakl.statusManage.SubClientManager.*;
import static com.zakl.statusManage.MqKeysManager.activePushKeys;
import static com.zakl.statusManage.MqKeysManager.passiveCallKeys;
import static com.zakl.mqhandler.MqHandleUtil.checkIfSortedSet;

@Slf4j
public class X {

    private final static Set<String> queueSet = new CopyOnWriteArraySet<>();


    private final static Double MIN_SCORE = Double.MIN_VALUE;
    private final static String KEY_HOLDER = "holder";


    private final static ExecutorService executors = Executors.newCachedThreadPool();


    /**
     * 优先级队列
     */
    private final static Set<String> priorityKeys = new HashSet<>();

    /**
     * 先进先出队列
     */
    private final static Set<String> fifoKeys = new HashSet<>();


    public static void registerSubClient(ChannelHandlerContext ctx, MqSubMessage msg) {

        activePushKeys.addAll(msg.getActivePushKeys());
        passiveCallKeys.addAll(msg.getPassiveCallKeys());

        SubClientInfo subClientInfo = new SubClientInfo(msg.clientId, msg.getClientWeight(), ctx);

        clientMap.put(msg.getClientId(), subClientInfo);


    }

    public static void doMqClientRegister(Iterable<String> keys, SubClientInfo clientInfo) {


        for (final String keyName : keys) {
            log.info("new Client registered {}", clientInfo);

            if (!queueSet.contains(keyName)) {


                ReentrantLock lock = new ReentrantLock();

                Condition consumeCondition = lock.newCondition();


                PriorityBlockingQueue<SubClientInfo> clientPq = new PriorityBlockingQueue<>(16, Comparator.comparing(SubClientInfo::getWeight).reversed());
                if (checkIfSortedSet(keyName) && !priorityKeys.contains(keyName)) {
                    log.info("start register new sorted Set in redis");
                    registerNewSortedSet(keyName);
                    log.info("start register new sorted Set in redis succeed ");

                    priorityKeys.add(keyName);

                    sortedSetHandleConditionMap.put(keyName, consumeCondition);

                    sortedSetHandleLockMap.putIfAbsent(keyName, lock);

                    sortedSetClientMap.computeIfAbsent(keyName, v -> clientPq).add(clientInfo);

                } else {
                    fifoKeys.add(keyName);

                    listHandleConditionMap.put(keyName, consumeCondition);

                    listHandleLockMap.put(keyName, lock);

                    listKeyClientMap.computeIfAbsent(keyName, v -> clientPq).add(clientInfo);
                }

                queueSet.add(keyName);


                AtomicBoolean clientAlive = new AtomicBoolean(true);

                clientAliveMap.put(clientInfo.getClientId(), clientAlive);


                //只监听
                executors.submit(() -> {

                    while (true) {
                        if (clientAlive.get()) {
                            log.info("{} offline", clientInfo);
                            cleanSubClientInfo(keyName, clientInfo);
                            break;
                        }
                        lock.lock();
                        while (statusMap.get(keyName)) {
                            handleRcvAndDistribute(keyName);
                        }
                        try {
                            consumeCondition.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        lock.unlock();
                    }
                });
            }
        }
    }

    /**
     * 如果client已经离线,需要清楚当前客户端在服务端的状态信息
     *
     * @param keyName
     * @param clientInfo
     */
    private static void cleanSubClientInfo(String keyName, SubClientInfo clientInfo) {


    }


    public static void handleRcvAndDistribute(String keyName) {
        PubMsgBufHandle msgBufHandler;
        MqMsgDistributeHandle distributeHandler;
        if (checkIfSortedSet(keyName)) {
            msgBufHandler = PriorityPubMsgBufBufHandler.getInstance();
            distributeHandler = PriorityMsgDistributeHandler.getInstance();
        } else {
            msgBufHandler = FifoPubMsgBufBufHandler.getInstance();
            distributeHandler = FifoMsgDistributeHandler.getInstance();
        }
        MqMessage mqMessage = msgBufHandler.listen(keyName);
        distributeHandler.distribute(mqMessage, keyName);
    }

    /**
     * 注册一个新的SortedSet到Redis
     *
     * @param channelName
     */
    public static void registerNewSortedSet(String channelName) {

        PriorityPubMsgBufBufHandler.registerNewSortedSetBuf(channelName);
        RedisUtil.syncSortedSetAdd(channelName, new Pair<>(MIN_SCORE, KEY_HOLDER + channelName));
    }



}

