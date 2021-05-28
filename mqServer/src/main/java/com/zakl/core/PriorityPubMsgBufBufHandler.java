package com.zakl.core;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.protocol.MqMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static com.zakl.constant.Contains.PUB_BUFFER_MAX_LIMIT;

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
        return sortedSetBufMap.get(keyName).pollFirst();
    }

    public void add(String keyName, MqMessage mqMessage, boolean tail) {
        if (!sortedSetBufMap.containsKey(keyName)) {
            throw new RuntimeException("当前Channel不存在");
        }
        LinkedBlockingDeque<MqMessage> msgBuffers = sortedSetBufMap.get(keyName);
        if (msgBuffers.size() > PUB_BUFFER_MAX_LIMIT) {
            //直接推送数据到Redis
            String uuid = UUID.fastUUID().toString();
            String redisData = String.format("uuid:%s\n%s", uuid, mqMessage.getMessage());
            RedisUtil.syncSortedSetAdd(keyName, new Pair<>(mqMessage.getWeight(), redisData));
        }
        if (tail) {
            msgBuffers.addLast(mqMessage);
        } else {
            msgBuffers.addFirst(mqMessage);
        }

    }


}
