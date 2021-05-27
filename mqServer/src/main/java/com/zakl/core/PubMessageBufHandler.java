package com.zakl.core;

import com.zakl.protocol.MqPubMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class PubMessageBufHandler {

    private final static Map<String, LinkedBlockingQueue<MqPubMessage>> sortedSetBufMap = new ConcurrentHashMap<>();

    public static void registerNewSortedSetBuf(String sortedSetName) {
        sortedSetBufMap.putIfAbsent(sortedSetName, new LinkedBlockingQueue<>());
    }



    public static MqPubMessage listen(String sortedSetName) {
        if (!sortedSetBufMap.containsKey(sortedSetName)) {
            throw new RuntimeException("当前Channel不存在");
        }
        return sortedSetBufMap.get(sortedSetName).poll();
    }





}
