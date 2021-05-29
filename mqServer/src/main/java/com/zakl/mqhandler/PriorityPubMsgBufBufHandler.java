package com.zakl.mqhandler;

import com.zakl.dto.MqMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static com.zakl.constant.Constants.PUB_BUFFER_MAX_LIMIT;

public class PriorityPubMsgBufBufHandler implements PubMsgBufHandle {


    private final static Map<String, LinkedBlockingDeque<MqMessage>> sortedSetBufMap = new ConcurrentHashMap<>();


    private PriorityPubMsgBufBufHandler() {

    }

    private final static PriorityPubMsgBufBufHandler instance = new PriorityPubMsgBufBufHandler();

    public static PubMsgBufHandle getInstance() {
        return instance;
    }


    public static void registerNewSortedSetBuf(String sortedSetName) {
        sortedSetBufMap.putIfAbsent(sortedSetName, new LinkedBlockingDeque<>());
    }


    public MqMessage listen(String keyName) {
        if (!sortedSetBufMap.containsKey(keyName)) {
            throw new RuntimeException("当前Channel不存在");
        }
        LinkedBlockingDeque<MqMessage> mqMessages = sortedSetBufMap.get(keyName);
        return mqMessages.isEmpty() ? null : mqMessages.pollFirst();
    }

    public void add(String keyName, MqMessage mqMessage, boolean tail) {
        if (!sortedSetBufMap.containsKey(keyName)) {
            throw new RuntimeException("当前Channel不存在");
        }
        LinkedBlockingDeque<MqMessage> msgBuffers = sortedSetBufMap.get(keyName);
        if (msgBuffers.size() > PUB_BUFFER_MAX_LIMIT) {
            RedisUtil.syncSortedSetAdd(mqMessage);
        }
        if (tail) {
            msgBuffers.addLast(mqMessage);
        } else {
            msgBuffers.addFirst(mqMessage);
        }
    }



}
