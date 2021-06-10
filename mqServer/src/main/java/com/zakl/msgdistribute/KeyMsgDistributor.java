package com.zakl.msgdistribute;


import com.zakl.dto.MqMessage;
import com.zakl.statusManage.StatusManager;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static com.zakl.statusManage.MqKeyHandleStatusManager.*;

/**
 * @author Zakl
 * @version 1.0
 * @className MqMsgDistributeThread
 * @date 6/1/2021
 * @desc todo
 **/
@Slf4j
public class KeyMsgDistributor implements Runnable {


    private final static ExecutorService executors = Executors.newCachedThreadPool();

    private final static Map<String, KeyMsgDistributor> keyMsgDistributeThreadMap = new HashMap<>();


    private final Lock keyHandleLock;
    private final String keyName;
    private final Condition canConsumeCondition;
    private final AtomicBoolean canConsumeFlag;
    private Thread keyMsgDistributorThread;

    private KeyMsgDistributor(String keyName) {
        if (!keyHandleLockMap.containsKey(keyName) ||
                !keyHandleConditionMap.containsKey(keyName) ||
                !canConsumeStatusMap.containsKey(keyName)) {
            String errorMsg = "MqKeyHandleStatusManager haven't init key: {} 's status yet";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        this.keyHandleLock = keyHandleLockMap.get(keyName);
        this.canConsumeCondition = keyHandleConditionMap.get(keyName);
        this.canConsumeFlag = canConsumeStatusMap.get(keyName);
        this.keyName = keyName;
        keyMsgDistributeThreadMap.put(keyName, this);
    }


    @Override
    public void run() {
        keyMsgDistributorThread = Thread.currentThread();
        while (true) {
            try {
                keyHandleLock.lock();
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
                log.error("", e);
                e.printStackTrace();
            } finally {
                keyHandleLock.unlock();
            }
        }
    }

    private static void handleRcvAndDistribute(String keyName) {
        PubMsgBufBufHandler msgBufHandler = PubMsgBufBufHandler.getInstance();
        MsgDistributeHandler distributeHandler = MsgDistributeHandler.getInstance();
        MqMessage mqMessage = msgBufHandler.listen(keyName);
        distributeHandler.distribute(mqMessage, keyName);
    }


    /**
     * startANewMsgDistributeThread
     *
     * @param keyName
     */
    public static void registerMsgDistributor(String keyName) {
        KeyMsgDistributor thread = keyMsgDistributeThreadMap.getOrDefault(keyName, null);
        if (thread != null) {
            log.error("current key: {} exist msgDistributor :{} ,thread state:{}", thread.keyName, thread.keyMsgDistributorThread, thread.keyMsgDistributorThread.getState());
            return;
        }
        log.info("start a mqMsg distribute thread for key: {}", keyName);
        executors.submit(new KeyMsgDistributor(keyName));
    }
}
