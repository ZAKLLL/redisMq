package com.zakl.ack;

import com.zakl.dto.MqMessage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AckCallBack {

    private final Lock lock = new ReentrantLock();
    private final Condition finish = lock.newCondition();

    private final MqMessage mqMessage;

    public AckCallBack(MqMessage mqMessage) {
        this.mqMessage = mqMessage;
    }

    public void start() {
        try {
            lock.lock();
            await();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


    public void over(byte ackType) {
        try {
            lock.lock();
            finish.signal();
            AckHandler.handleAckFailed(mqMessage,ackType);
        } finally {
            lock.unlock();
        }
    }

    private void await() throws TimeoutException {
        boolean timeout = false;
        try {
            timeout = finish.await(10000, TimeUnit.MILLISECONDS);
            AckHandler.handleAckFailed(mqMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!timeout) {
            throw new TimeoutException("Ack time out,push back to redis...");
        }
    }
}
