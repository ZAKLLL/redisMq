package com.zakl.ack;


import com.zakl.statusManage.SubClientInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Zakl
 * @version 1.0
 * @className MqMsgDistributeThread
 * @date 6/1/2021
 * @desc todo
 **/
@Slf4j
public class AckHandleThread extends Thread {


    public final static String THREAD_NAME_PREFIX = "thread:SubClientMsgDistribute:";

    private final AtomicReference<AckCallBack> ackCallBackRef;

    private final SubClientInfo subClientInfo;

    private final ReentrantLock lock;

    private final Condition condition;


    public AckHandleThread(SubClientInfo subClientInfo) {
        if (subClientInfo == null) {
            String errorMsg = "need subClientInfo to create a new AckHandleThread!";
            log.error(errorMsg);
            throw new RuntimeException("need subClientInfo to create a new AckHandleThread!");
        }
        super.setName(THREAD_NAME_PREFIX + subClientInfo.getClientId());
        lock = new ReentrantLock();
        condition = lock.newCondition();
        ackCallBackRef = new AtomicReference<>(null);
        this.subClientInfo = subClientInfo;
    }

    public void submitNewAckHandleRequest(AckCallBack ackCallBack) {
        if (ackCallBack == null) {
            return;
        }
        log.info("submit New AckHandleRequest,MqMsg:{}", ackCallBack.getMqMessage());
        AckResponseHandler.ackCallBackMap.put(ackCallBack.getMqMessage().getMessageId(), ackCallBack);
        lock.lock();
        try {
            ackCallBackRef.set(ackCallBack);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public void forceShutDown() {
        subClientInfo.setAlive(false);
        this.interrupt();
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                while (ackCallBackRef.get() != null && subClientInfo.isAlive) {
                    AckCallBack ackCallBack = this.ackCallBackRef.getAndSet(null);
                    ackCallBack.start(subClientInfo);
                }
                condition.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.error("AckHandleThread be Interrupted", e);
                break;
            } finally {
                lock.unlock();
            }
        }
    }


}
