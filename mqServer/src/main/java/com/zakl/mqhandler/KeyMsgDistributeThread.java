package com.zakl.mqhandler;


import com.zakl.dto.MqMessage;
import com.zakl.statusManage.MqKeyHandleStatusManager;
import com.zakl.statusManage.StatusManager;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static com.zakl.statusManage.MqKeyHandleStatusManager.*;
import static com.zakl.util.MqHandleUtil.checkIfSortedSet;

/**
 * @author Zakl
 * @version 1.0
 * @className MqMsgDistributeThread
 * @date 6/1/2021
 * @desc todo
 **/
@Slf4j
public class KeyMsgDistributeThread extends Thread {


    private final static ExecutorService executors = Executors.newCachedThreadPool();

    private final static Map<String, KeyMsgDistributeThread> keyMsgDistributeThreadMap = new HashMap<>();

    public final static String THREAD_NAME_PREFIX = "thread:keyDistribute:";

    private final Lock lock;
    private final String keyName;
    private final Condition canConsumeCondition;
    private final AtomicBoolean canConsumeFlag;

    private KeyMsgDistributeThread(String keyName) {
        if (!keyHandleLockMap.containsKey(keyName) ||
                !keyHandleConditionMap.containsKey(keyName) ||
                !canConsumeStatusMap.containsKey(keyName)) {
            String errorMsg = "MqKeyHandleStatusManager haven't init key: {} 's status yet";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        this.lock = keyHandleLockMap.get(keyName);
        this.canConsumeCondition = keyHandleConditionMap.get(keyName);
        this.canConsumeFlag = canConsumeStatusMap.get(keyName);
        this.keyName = keyName;
        super.setName(THREAD_NAME_PREFIX + keyName);
        keyMsgDistributeThreadMap.put(keyName, this);
    }


    @Override
    public void run() {
        while (true) {
            try {
                lock.lock();
                while (canConsumeFlag.get()) {
                    handleRcvAndDistribute(keyName);
                }
                try {
                    log.info("key {} or its client queue is empty,pause distributing msg", keyName);
                    canConsumeCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error("key {} mqMsg distribute thread error,restart distributeThread", keyName);
                    StatusManager.resetKeyStatus(keyName, canConsumeFlag);
                }
            } catch (Exception e) {
                log.error("",e);
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    private static void handleRcvAndDistribute(String keyName) {
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
     * startANewMsgDistributeThread
     *
     * @param keyName
     */
    public static void registerMsgDistributor(String keyName) {
        KeyMsgDistributeThread thread = keyMsgDistributeThreadMap.getOrDefault(keyName, null);
        if (thread != null) {
            log.error("current key: {} exist msgDistributeThread :{} ,thread state:{}", thread.keyName, thread.getName(), thread.getState());
            return;
        }
        log.info("start a mqMsg distribute thread for key: {}", keyName);
        executors.submit(new KeyMsgDistributeThread(keyName));
    }
}
