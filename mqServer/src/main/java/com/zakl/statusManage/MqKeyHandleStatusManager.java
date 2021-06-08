package com.zakl.statusManage;

import com.zakl.dto.MqMessage;

import java.util.List;
import java.util.Map;
import java.util.Queue;
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
     * key and it's subscriber (ActivePush)
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
     * clientId->clientInfo including(clientId,weight,channelCtx)
     */
    public final static Map<String, SubClientInfo> clientIdMap = new ConcurrentHashMap<>(16);


    /**
     * if key has msg && has client to let thread consume
     */
    public final static Map<String, AtomicBoolean> canConsumeStatusMap = new ConcurrentHashMap<>();


    /**
     * mq key and it's buf data
     */
    public final static Map<String, Queue<MqMessage>> keyMessagesBufMap = new ConcurrentHashMap<>();





}
