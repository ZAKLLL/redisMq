package com.zakl.statusManage;

import com.zakl.dto.MqMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * SubClientManager
 */
public class MqKeyHandleStatusManager {


    /**
     * key and it's subscriber
     */
    public final static Map<String, PriorityBlockingQueue<SubClientInfo>> keyClientsMap = new ConcurrentHashMap<>(16);


    /**
     * if sortedSet can continue be handleRcvAndDistribute() condition
     */
    public final static Map<String, Condition> keyHandleConditionMap = new ConcurrentHashMap<>();

    /**
     * key handleRcvAndDistribute() Lock
     */
    public final static Map<String, Lock> keyHandleLockMap = new ConcurrentHashMap<>();


    /**
     * clientId-alive
     */
    public final static Map<String, AtomicBoolean> clientAliveMap = new ConcurrentHashMap<>();

    /**
     * clientId->clientInfo including(clientId,weight,channelCtx)
     */
    public final static Map<String, SubClientInfo> clientIdMap = new ConcurrentHashMap<>(16);


    /**
     * if key has msg let thread consume
     */
    public final static Map<String, AtomicBoolean> canConsumeStatusMap = new ConcurrentHashMap<>();


    /**
     * mq key and it's buf data
     */
    public final static Map<String, LinkedBlockingDeque<MqMessage>> keyMessagesBufMap = new ConcurrentHashMap<>();

    /**
     * remind key's consume thread can continue consume
     *
     * @param keyName
     */
    public static void remindConsume(String keyName) {
        Condition condition = keyHandleConditionMap.get(keyName);
        Lock lock = keyHandleLockMap.get(keyName);
        AtomicBoolean flag = canConsumeStatusMap.get(keyName);
        if (flag.get()) {
            try {
                lock.lock();
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }

}
