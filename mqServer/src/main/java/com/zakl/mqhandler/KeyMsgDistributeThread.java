package com.zakl.mqhandler;


import com.zakl.dto.MqMessage;
import com.zakl.statusManage.StatusManager;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static com.zakl.mqhandler.MqHandleUtil.checkIfSortedSet;

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

    public final static String THREAD_NAME_PREFIX = "thread:keyDistribute:";

    private final Lock lock;
    private final String keyName;
    private final Condition canConsumeCondition;
    private final AtomicBoolean canConsumeFlag;

    public KeyMsgDistributeThread(Lock lock, String keyName, Condition canConsumeCondition, AtomicBoolean canConsumeFlag) {
        this.lock = lock;
        this.keyName = keyName;
        this.canConsumeCondition = canConsumeCondition;
        this.canConsumeFlag = canConsumeFlag;
        super.setName(THREAD_NAME_PREFIX+keyName);
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
                    log.info("key {} is empty,wait for publish", keyName);
                    canConsumeCondition.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    log.error("key {} mqMsg distribute thread error,restart distributeThread", keyName);
                    StatusManager.resetKeyStatus(keyName, canConsumeFlag);
                }
            } finally {
                lock.unlock();
            }
        }
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


    public static void startANewMsgDistributeThread(KeyMsgDistributeThread thread) {
        log.info("key {} start a mqMsg distribute thread ", thread.keyName);
        executors.submit(thread);
    }
}
