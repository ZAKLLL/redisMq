package com.zakl.msgpublish;

import com.zakl.protocol.MqPubMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhangJiaKui
 * @classname AckHandler
 * @description TODO
 * @date 6/11/2021 3:48 PM
 */
public class PubAckHandler {
    private static Map<String, PubAckCallBack> ackCallBackMap = new ConcurrentHashMap<>();

    public static void confirm(MqPubMessage msg) {
        PubAckCallBack pubAckCallBack = ackCallBackMap.get(msg.getPubId());
        pubAckCallBack.over();
    }

    public static boolean submitAckCallBack(PubAckCallBack pubAckCallBack) {
        ackCallBackMap.put(pubAckCallBack.getPubId(), pubAckCallBack);
        return pubAckCallBack.start();
    }
}
