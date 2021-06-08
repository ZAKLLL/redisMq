package com.zakl.msgdistribute;

import com.zakl.dto.MqMessage;
import com.zakl.redisinteractive.RedisUtil;
import com.zakl.statusManage.MqKeyHandleStatusManager;
import com.zakl.statusManage.StatusManager;
import com.zakl.util.MqHandleUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

import static com.zakl.config.ServerConfig.PUB_BUFFER_MAX_LIMIT;
import static com.zakl.statusManage.StatusManager.remindDistributeThreadConsume;


@Slf4j
public class PubMsgBufBufHandler {


    private final static Map<String, Queue<MqMessage>> keyBufferMap = MqKeyHandleStatusManager.keyMessagesBufMap;


    private PubMsgBufBufHandler() {

    }

    private final static PubMsgBufBufHandler instance = new PubMsgBufBufHandler();

    public static PubMsgBufBufHandler getInstance() {
        return instance;
    }


    public static void registerNewSortedSetBuf(String sortedSetName) {
        keyBufferMap.putIfAbsent(sortedSetName, new LinkedBlockingDeque<>());
    }


    public void add(String keyName, MqMessage... mqMessages) {
        if (!keyBufferMap.containsKey(keyName)) {
            log.info("当前key{} 不存在,创建新的bufKey...", keyName);
            keyBufferMap.put(keyName, new LinkedBlockingDeque<>());
        }

        Queue<MqMessage> msgBuffer = keyBufferMap.get(keyName);
        if (msgBuffer.size() > PUB_BUFFER_MAX_LIMIT) {
            if (MqHandleUtil.checkIfSortedSet(keyName)) {
                RedisUtil.syncSortedSetAdd(mqMessages);
            } else {
                RedisUtil.syncListLPush(keyName, mqMessages);
            }
        } else {
            for (MqMessage mqMessage : mqMessages) {
                msgBuffer.offer(mqMessage);
            }
        }
    }


    public void add(Map<String, List<MqMessage>> keyMsgs) {
        if (keyMsgs == null || keyMsgs.isEmpty()) return;

        for (Map.Entry<String, List<MqMessage>> kv : keyMsgs.entrySet()) {
            String keyName = kv.getKey();
            List<MqMessage> msgs = kv.getValue();
            if (msgs.isEmpty()) continue;
            if (!keyBufferMap.containsKey(keyName)) {
                log.info("current key{} doesn't exist,do init new key info in server ", keyName);
                StatusManager.initNewKey(keyName);
            }
            Queue<MqMessage> msgBuffers = keyBufferMap.get(keyName);
            if (msgBuffers.size() + msgs.size() <= PUB_BUFFER_MAX_LIMIT) {
                keyMsgs.remove(keyName);
                add(keyName, msgs.toArray(new MqMessage[0]));
            }
            //当前key处于可消费状态
            remindDistributeThreadConsume(keyName);
        }
        //满了的buffer,多余的数据需要push到redisServer
        keyMsgs.forEach((k, v) -> {
            MqMessage[] mqMessages = v.toArray(new MqMessage[0]);
            if (MqHandleUtil.checkIfSortedSet(k)) {
                RedisUtil.syncSortedSetAdd(k, mqMessages);
            } else {
                RedisUtil.syncListLPush(k, mqMessages);
            }
        });
    }

    public MqMessage listen(String keyName) {
        if (!keyBufferMap.containsKey(keyName)) {
            throw new RuntimeException("当前Channel不存在");
        }
        Queue<MqMessage> mqMessages = keyBufferMap.get(keyName);
        return mqMessages.isEmpty() ? null : mqMessages.poll();
    }
}
