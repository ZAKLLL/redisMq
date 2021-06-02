package com.zakl.nettyhandler;

import com.zakl.protocol.MqPubMessage;
import com.zakl.protocol.MqSubMessage;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangJiaKui
 * @classname SimpleHandlerHelper
 * @description TODO
 * @date 5/27/2021 3:19 PM
 */
public class SimpleHandlerHelper {
    private final static Map<Class<?>, SimpleChannelInboundHandler<?>> handlerMap;


    static {
        handlerMap = new HashMap<>();
        //注册一次

        handlerMap.put(MqPubMessage.class, new MqPubMessageHandler());
        handlerMap.put(MqSubMessage.class, new MqSubMessageHandler());
    }

    public static SimpleChannelInboundHandler<?> getSingletonHandler(Class<?> clazz) {
        if (!handlerMap.containsKey(clazz))
            throw new RuntimeException(clazz.getCanonicalName() + "不存在对应的MessageHandler");
        return handlerMap.get(clazz);
    }

    public static SimpleChannelInboundHandler<?> getHandler(Class<?> clazz) {
        if (!handlerMap.containsKey(clazz))
            throw new RuntimeException(clazz.getCanonicalName() + "不存在对应的MessageHandler");
        SimpleChannelInboundHandler<?> simpleChannelInboundHandler = handlerMap.get(clazz);
        try {
            return simpleChannelInboundHandler.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
