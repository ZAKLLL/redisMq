package com.zakl.msgdistribute;

import com.zakl.annotation.AnnotationMethodInfo;
import com.zakl.annotation.MqSubScribe;
import com.zakl.dto.MqMessage;
import com.zakl.nettyhandle.MqSubMessageClientHandler;
import com.zakl.protocol.MqSubMessage;
import com.zakl.util.ReflectionUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.zakl.msgdistribute.keyMethodManager.keyConsumeMethodsMap;
import static com.zakl.protocol.MqSubMessage.TYPE_ACK_AUTO;

/**
 * @author ZhangJiaKui
 * @classname MqmsgDistributor
 * @description TODO
 * @date 6/8/2021 3:58 PM
 */
@Slf4j
public class MqMsgDistributor {

    public final static Map<String, MessageCallBack> passiveCallMsgMap = new ConcurrentHashMap<>();

    public static void distributeMsgToConsumeMethod(MqSubMessage msg) {
        Map<String, List<MqMessage>> keyMsgsMap = msg.getMqMessages().stream().collect(Collectors.groupingBy(MqMessage::getKey));
        for (String key : keyMsgsMap.keySet()) {
            List<MqMessage> mqMessages = keyMsgsMap.get(key);
            AnnotationMethodInfo<MqSubScribe> annotationMethodInfo = keyConsumeMethodsMap.get(key);
            MqSubScribe annotation = annotationMethodInfo.getAnnotation();
            Method method = annotationMethodInfo.getMethod();
            Object object = annotationMethodInfo.getTargetObject();
            if (annotation.autoAck()) {
                new AckClientHandler(TYPE_ACK_AUTO).confirm(mqMessages.toArray(new MqMessage[0]));
                ReflectionUtils.reflectInvoke(object, method, new Object[]{mqMessages});
            } else {
                ReflectionUtils.reflectInvoke(object, method, new Object[]{mqMessages, new AckClientHandler(MqSubMessage.TYPE_ACK_MANUAL)});
            }
        }
    }


    public static void distributeMsgToCaller(MqSubMessage mqSubMessage) {
        MessageCallBack messageCallBack = passiveCallMsgMap.get(mqSubMessage.getPassiveCallId());
        if (messageCallBack == null) {
            log.error("no corresponding messageCallBack for passiveCallId: {}", mqSubMessage.getPassiveCallId());
            return;
        }
        messageCallBack.over(mqSubMessage.getMqMessages());
        //return ack
        Set<String> ackIdSet = new HashSet<>();
        for (MqMessage mqMessage : mqSubMessage.getMqMessages()) {
            ackIdSet.add(mqMessage.getMessageId());
        }
        MqSubMessage ackMsg = new MqSubMessage();
        ackMsg.setType(TYPE_ACK_AUTO);
        ackMsg.setAckMsgIdSet(ackIdSet);
        MqSubMessageClientHandler.getCtx().writeAndFlush(ackMsg);
    }

}
