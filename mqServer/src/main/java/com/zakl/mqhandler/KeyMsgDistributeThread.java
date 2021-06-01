package com.zakl.mqhandler;


import com.zakl.dto.MqMessage;
import lombok.extern.slf4j.Slf4j;

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

    public final Lock lock;
    public final String keyName;
    public final Condition canConsumeCondition;
    public final AtomicBoolean canConsumeFlag;

    public KeyMsgDistributeThread(Lock lock, String keyName, Condition canConsumeCondition, AtomicBoolean canConsumeFlag) {
        this.lock = lock;
        this.keyName = keyName;
        this.canConsumeCondition = canConsumeCondition;
        this.canConsumeFlag = canConsumeFlag;
    }


    @Override
    public void run() {
        while (true) {
            lock.lock();
            while (canConsumeFlag.get()) {
                handleRcvAndDistribute(keyName);
            }
            try {
                canConsumeCondition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock();
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

}
