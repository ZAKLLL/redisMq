package com.zakl.ack;

import com.zakl.dto.MqMessage;
import com.zakl.statusManage.MqKeyHandleStatusManager;
import com.zakl.statusManage.StatusManager;
import com.zakl.statusManage.SubClientInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.zakl.statusManage.StatusManager.remindDistributeThreadConsume;

@Slf4j
public class AckCallBack {

    private final Lock lock = new ReentrantLock();
    private final Condition finish = lock.newCondition();

    private volatile boolean finishFlag = false;

    private final MqMessage mqMessage;

    public AckCallBack(MqMessage mqMessage) {
        this.mqMessage = mqMessage;
    }

    public void start(SubClientInfo subClientInfo) {
        try {
            lock.lock();

            if (finishFlag) {
                //it means ack response before ackCallback start
                // (because ackCall back start control by AckHandler,which  calls ackCallBack's start one by one blocking)
                MqKeyHandleStatusManager.keyClientsMap.get(mqMessage.getKey()).offer(subClientInfo);
                remindDistributeThreadConsume(mqMessage.getKey());
                return;
            }
            log.info("start new Ack callBack,msg:{}", mqMessage);
            try {
                await();
                // current msg get target client ACkResponse
                // push client back to clientPq
                MqKeyHandleStatusManager.keyClientsMap.get(mqMessage.getKey()).offer(subClientInfo);
                //remind  Distributor continue work
                remindDistributeThreadConsume(mqMessage.getKey());
            } catch (TimeoutException e) {
                AckResponseHandler.ackBackUp(mqMessage);
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }

    public void over(byte ackType) {
        try {
            lock.lock();
            AckResponseHandler.handleAckSuccessFully(mqMessage, ackType);
            AckResponseHandler.cleanAckBackUp(mqMessage);
            finishFlag = true;
            finish.signal();
        } finally {
            lock.unlock();
        }
    }

    private void await() throws TimeoutException {
        boolean timeout = false;
        try {
            timeout = finish.await(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("msg:{} handleAckSuccessFailed,cause by interrupted,", mqMessage);
            AckResponseHandler.handleAckSuccessFailed(mqMessage);
            e.printStackTrace();
        }
        if (!timeout) {
            AckResponseHandler.handleAckSuccessFailed(mqMessage);
            log.error("Ack time out,mqMsg:{}", mqMessage);
            throw new TimeoutException("Ack time out,push back to redis...");
        }
    }

    public MqMessage getMqMessage() {
        return mqMessage;
    }
}
