package com.zakl.ack;

import com.zakl.dto.MqMessage;
import com.zakl.mqhandler.RedisUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AckHandler {


    public static final Map<String, AckCallBack> ackCallBackMap = new ConcurrentHashMap<>();

    /**
     * redis用来存放ack确认信息
     */
    public static final String ACK_SET_KEY = "ack:key:set";

    /**
     * 成功推送Ack的type
     *
     * @param message
     * @param ackType
     */
    public static void handleAckFailed(MqMessage message, byte ackType) {
        log.info("{}消息发送成功AckType:{}", message, ackType);
    }


    /**
     * @param mqMessage
     */
    public static void handleAckFailed(MqMessage mqMessage) {
        ackCallBackMap.remove(mqMessage.getKey());
        //重新发送到ACK中
        RedisUtil.syncSortedSetAdd(mqMessage);
    }

    public static void ackBackUp(MqMessage mqMessage) {
        RedisUtil.syncSetAdd(ACK_SET_KEY, mqMessage);
        log.info("备份到待确认ACK信息的消息: {} 到redis", mqMessage);
    }

    public static void cleanAckBackUp(MqMessage mqMessage) {
        RedisUtil.syncSetRemove(ACK_SET_KEY, mqMessage);
        log.info("从redis 清除确认ACK信息的消息: {} ", mqMessage);
    }


}
