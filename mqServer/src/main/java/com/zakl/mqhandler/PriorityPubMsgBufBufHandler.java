package com.zakl.mqhandler;

import com.zakl.statusManage.SubClientManager;
import com.zakl.dto.MqMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static com.zakl.constant.Constants.PUB_BUFFER_MAX_LIMIT;

@Slf4j
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


    @Override
    public void add(boolean tail, String keyName, MqMessage... mqMessages) {
        if (!sortedSetBufMap.containsKey(keyName)) {
            log.info("当前key{} 不存在,创建新的bufKey...", keyName);
            sortedSetBufMap.put(keyName, new LinkedBlockingDeque<>());
        }

        LinkedBlockingDeque<MqMessage> msgBuffers = sortedSetBufMap.get(keyName);
        if (msgBuffers.size() > PUB_BUFFER_MAX_LIMIT) {
            RedisUtil.syncSortedSetAdd(mqMessages);
        }
        for (MqMessage mqMessage : mqMessages) {
            if (tail) {
                msgBuffers.addLast(mqMessage);
            } else {
                msgBuffers.addFirst(mqMessage);
            }
        }
    }


    @Override
    public void add(boolean tail, Map<String, List<MqMessage>> keyMsgs) {
        List<MqMessage> directToRedis = new ArrayList<>();

        for (Map.Entry<String, List<MqMessage>> kv : keyMsgs.entrySet()) {
            String keyName = kv.getKey();
            if (!sortedSetBufMap.containsKey(keyName)) {
                log.info("current key{} doesn't exist,do init new key info in server ", keyName);

                //todo 为该新的key
                sortedSetBufMap.put(keyName, new LinkedBlockingDeque<>());
            }
            List<MqMessage> msgs = kv.getValue();
            LinkedBlockingDeque<MqMessage> msgBuffers = sortedSetBufMap.get(keyName);
            if (msgBuffers.size() > PUB_BUFFER_MAX_LIMIT) {
                directToRedis.addAll(msgs);
            }
            for (MqMessage msg : msgs) {
                if (tail) {
                    msgBuffers.addLast(msg);
                } else {
                    msgBuffers.addFirst(msg);
                }
            }
            //当前key处于可消费状态
            SubClientManager.remindConsume(keyName);
        }
        RedisUtil.syncSortedSetAdd(directToRedis.toArray(new MqMessage[0]));
    }
}
