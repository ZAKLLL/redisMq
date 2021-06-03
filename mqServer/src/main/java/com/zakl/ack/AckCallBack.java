package com.zakl.ack;

import com.zakl.dto.MqMessage;
import com.zakl.statusManage.MqKeyHandleStatusManager;
import com.zakl.statusManage.SubClientInfo;

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

    public void start(SubClientInfo subClientInfo) {
        try {
            lock.lock();
            await();
            AckResponseHandler.cleanAckBackUp(mqMessage);

            // current msg get target client ACkResponse
            // push client back to clientPq
            MqKeyHandleStatusManager.keyClientsMap.get(mqMessage.getKey()).offer(subClientInfo);
        } catch (TimeoutException e) {
            AckResponseHandler.ackBackUp(mqMessage);
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    //todo handle ack response
    public void over(byte ackType) {
        try {
            lock.lock();
            finish.signal();
            AckResponseHandler.handleAckSuccessFully(mqMessage, ackType);
        } finally {
            lock.unlock();
        }
    }

    private void await() throws TimeoutException {
        boolean timeout = false;
        try {
            timeout = finish.await(10000, TimeUnit.MILLISECONDS);
            AckResponseHandler.handleAckSuccessFully(mqMessage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!timeout) {
            throw new TimeoutException("Ack time out,push back to redis...");
        }
    }

    public MqMessage getMqMessage() {
        return mqMessage;
    }
}
