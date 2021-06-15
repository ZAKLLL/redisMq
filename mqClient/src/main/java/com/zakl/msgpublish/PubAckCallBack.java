package com.zakl.msgpublish;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ZhangJiaKui
 * @classname AckCallBack
 * @description TODO
 * @date 6/11/2021 3:38 PM
 */
@Slf4j
public class PubAckCallBack {
    private final Lock ackCallBackLock = new ReentrantLock();
    private final Condition finish = ackCallBackLock.newCondition();

    private volatile boolean finishFlag = false;

    private final String pubId;

    public PubAckCallBack(String pubId) {
        this.pubId = pubId;
    }

    public boolean start() {
        if (finishFlag) {
            return true;
        }
        try {
            ackCallBackLock.lock();
            await();
            return true;
        } catch (TimeoutException e) {
            e.printStackTrace();
            return false;
        } finally {
            ackCallBackLock.unlock();
        }
    }

    public void over() {
        try {
            ackCallBackLock.lock();
            finishFlag = true;
            finish.signal();
        } finally {
            ackCallBackLock.unlock();
        }
    }

    private void await() throws TimeoutException {
        boolean timeout = false;
        try {
            timeout = finish.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!timeout) {
            throw new TimeoutException("Ack time out,push back to redis...");
        }
    }

    public String getPubId() {
        return pubId;
    }
}
