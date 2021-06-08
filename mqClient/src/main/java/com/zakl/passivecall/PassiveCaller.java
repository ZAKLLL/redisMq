package com.zakl.passivecall;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.dto.MqMessage;
import com.zakl.msgdistribute.MessageCallBack;
import com.zakl.nettyhandle.MqSubMessageClientHandler;
import com.zakl.protocol.MqSubMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
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
    public static List<MqMessage> doConsume(Pair<String, Integer>... keyAndExceptCounts) {
        String passiveCallId = UUID.randomUUID().toString();
        MqSubMessage passiveCallRequest = new MqSubMessage();
        passiveCallRequest.setPassiveCallId(passiveCallId);
        passiveCallRequest.setPassiveCallKeys(Arrays.asList(keyAndExceptCounts));
        MessageCallBack<List<MqMessage>> messageCallBack = new MessageCallBack<>();
        passiveCallMsgMap.put(passiveCallId, messageCallBack);
        MqSubMessageClientHandler.getCtx().writeAndFlush(passiveCallRequest);
        return messageCallBack.start();
    }

}
