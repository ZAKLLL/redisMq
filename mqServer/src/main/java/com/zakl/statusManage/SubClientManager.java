package com.zakl.statusManage;

import com.zakl.mqhandler.MqHandleUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * SubClientManager
 */
public class SubClientManager {


    /**
     * sortedSet and it's subscriber
     */
    public final static Map<String, PriorityBlockingQueue<SubClientInfo>> sortedSetClientMap = new ConcurrentHashMap<>(16);

    /**
     * list and it's subscriber
     */
    public final static Map<String, PriorityBlockingQueue<SubClientInfo>> listKeyClientMap = new ConcurrentHashMap<>(16);

    /**
     * if sortedSet can continue be handleRcvAndDistribute() condition
     */
    public final static Map<String, Condition> sortedSetHandleConditionMap = new ConcurrentHashMap<>();

    /**
     * sortedSet handleRcvAndDistribute() Lock
     */
    public final static Map<String, Lock> sortedSetHandleLockMap = new ConcurrentHashMap<>();

    /**
     * if list can continue be handleRcvAndDistribute() condition
     */
    public final static Map<String, Condition> listHandleConditionMap = new ConcurrentHashMap<>();

    /**
     * list handleRcvAndDistribute() Lock
     */
    public final static Map<String, Lock> listHandleLockMap = new ConcurrentHashMap<>();

    /**
     * clientId-alive
     */
    public final static Map<String, AtomicBoolean> clientAliveMap = new ConcurrentHashMap<>();

    /**
     * clientId->clientInfo including(clientId,weight,channelCtx)
     */
    public final static Map<String, SubClientInfo> clientMap = new ConcurrentHashMap<>(16);


    /**
     * remind key's consume thread can continue consume
     *
     * @param keyName
     */
    public static void remindConsume(String keyName) {
        Map<String, Lock> lockMap;
        Map<String, Condition> conditionMap;
        if (MqHandleUtil.checkIfSortedSet(keyName)) {
            lockMap = sortedSetHandleLockMap;
            conditionMap = sortedSetHandleConditionMap;
        } else {
            lockMap = listHandleLockMap;
            conditionMap = listHandleConditionMap;
        }
        Condition condition = conditionMap.get(keyName);
        Lock lock = lockMap.get(keyName);
        try {
            lock.lock();
            condition.signal();
        } finally {
            lock.unlock();
        }
    }


}
