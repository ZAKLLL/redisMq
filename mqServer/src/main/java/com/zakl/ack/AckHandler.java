package com.zakl.ack;


import com.zakl.statusManage.SubClientInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
public class AckHandler implements Runnable {


    private final Queue<AckCallBack> ackCallBackQueue = new ConcurrentLinkedQueue<>();

    private final SubClientInfo subClientInfo;

    private final ReentrantLock lock;

    private final Condition condition;

    private Thread ackHandleRealThread;

    private final static ExecutorService executors = Executors.newCachedThreadPool();


    public AckHandler(SubClientInfo subClientInfo) {
        if (subClientInfo == null) {
            String errorMsg = "need subClientInfo to create a new AckHandleThread!";
            log.error(errorMsg);
            throw new RuntimeException("need subClientInfo to create a new AckHandleThread!");
        }
        lock = new ReentrantLock();
        condition = lock.newCondition();
        this.subClientInfo = subClientInfo;
    }

    public void submitNewAckHandleRequest(AckCallBack ackCallBack) {
        if (ackCallBack == null) {
            return;
        }
        log.info("submit New AckHandleRequest,MqMsg:{}", ackCallBack.getMqMessage());
        String messageId = ackCallBack.getMqMessage().getMessageId();
        AckResponseHandler.ackCallBackMap.put(messageId, ackCallBack);

        lock.lock();
        try {
            ackCallBackQueue.offer(ackCallBack);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }


    public void forceShutDown() {
        log.info("forceShutDown current client: {} 's achHandleThread", subClientInfo.getClientId());
        subClientInfo.setAlive(false);
        if (ackHandleRealThread == null) {
            String errorMsg = String.format("ackHandleRealThread is null ,maybe current thread: %s didn't submit to ThreadPool successFully", subClientInfo.getClientId());
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        //使用执行线程进行中断处理
        ackHandleRealThread.interrupt();
    }

    @Override
    public void run() {
        ackHandleRealThread = Thread.currentThread();
        while (true) {
            lock.lock();
            try {
                while (!ackCallBackQueue.isEmpty() && subClientInfo.isAlive) {
                    AckCallBack ackCallBack = ackCallBackQueue.poll();
                    //todo 是否为每个ack 开启一个线程 过于耗费性能
                    //异步
                    executors.submit(() -> ackCallBack.start(subClientInfo));
                }
                condition.await();
            } catch (InterruptedException e) {
                log.error("AckHandleThread be Interrupted", e);
                break;
            } finally {
                lock.unlock();
            }
        }
    }


}
