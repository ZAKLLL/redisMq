package com.zakl.consume;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.config.ClientConfig;
import com.zakl.constant.Constants;
import com.zakl.dto.MqMessage;
import com.zakl.msgdistribute.MessageCallBack;
import com.zakl.nettyhandle.MqSubMessageClientHandler;
import com.zakl.protocol.MqSubMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.zakl.msgdistribute.MqMsgDistributor.passiveCallMsgMap;

/**
 * @author ZhangJiaKui
 * @classname PassiveCaller
 * @description TODO
 * @date 6/8/2021 4:27 PM
 */
@Slf4j
public class PassiveCaller {

    /**
     * @param keyAndExceptCounts
     * @return
     */
    @SafeVarargs
    public static List<MqMessage> doConsume(boolean sortedSet, Pair<String, Integer>... keyAndExceptCounts) {
        String passiveCallId = UUID.randomUUID().toString();
        MqSubMessage passiveCallRequest = new MqSubMessage();
        passiveCallRequest.setType(MqSubMessage.PASSIVE_CALL);
        passiveCallRequest.setPassiveCallId(passiveCallId);
        passiveCallRequest.setClientId(ClientConfig.getClientId());
        List<Pair<String, Integer>> passiveCallKeys = new ArrayList<>();
        for (Pair<String, Integer> kv : keyAndExceptCounts) {
            String key = kv.getKey();
            if (sortedSet) {
                key = Constants.MQ_SORTED_SET_PREFIX + key;
            } else {
                key = Constants.MQ_LIST_PREFIX + key;
            }
            passiveCallKeys.add(new Pair<>(key, kv.getValue()));
        }
        passiveCallRequest.setPassiveCallKeys(passiveCallKeys);
        MessageCallBack<List<MqMessage>> messageCallBack = new MessageCallBack<>();
        passiveCallMsgMap.put(passiveCallId, messageCallBack);
        MqSubMessageClientHandler.getCtx().writeAndFlush(passiveCallRequest);
        return messageCallBack.start();
    }

}
