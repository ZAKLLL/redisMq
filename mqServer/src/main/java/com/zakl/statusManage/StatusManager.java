package com.zakl.statusManage;

import com.zakl.mqhandler.MqHandleUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author ZhangJiaKui
 * @classname StatusManager
 * @description
 * @date 6/1/2021 5:22 PM
 */
public class StatusManager {

    public static Map<String, Boolean> canConsumeStatusMap = new ConcurrentHashMap<>();


    public static void initNewKey(String keyName) {

        Map<String, Condition> conditionMap;
        Map<String, Lock> lockMap;
        Map<String, PriorityBlockingQueue<SubClientInfo>> keyClientMap;
        if (MqHandleUtil.checkIfSortedSet(keyName)) {
            conditionMap = SubClientManager.sortedSetHandleConditionMap;
            lockMap = SubClientManager.sortedSetHandleLockMap;
            keyClientMap = SubClientManager.sortedSetClientMap;
        } else {
            conditionMap = SubClientManager.listHandleConditionMap;
            lockMap = SubClientManager.listHandleLockMap;
            keyClientMap = SubClientManager.listKeyClientMap;
        }



    }

    public static void initFromRedis() {

    }

}
