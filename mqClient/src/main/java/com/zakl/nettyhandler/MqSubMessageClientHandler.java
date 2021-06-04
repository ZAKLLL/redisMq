package com.zakl.nettyhandler;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.annotation.AnnotationMethodInfo;
import com.zakl.annotation.AnnotationUtil;
import com.zakl.annotation.MqSubScribe;
import com.zakl.config.ClientConfig;
import com.zakl.protocol.MqSubMessage;
import com.zakl.util.MqHandleUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.zakl.protocol.MqSubMessage.TYPE_SUBSCRIBE;

/**
 * @author ZhangJiaKui
 * @classname MqSubMessageHandler
 * @description TODO
 * @date 5/27/2021 4:26 PM
 */
@Slf4j
public class MqSubMessageClientHandler extends SimpleChannelInboundHandler<MqSubMessage> {



    private static final Map<String, AnnotationMethodInfo<MqSubScribe>> keyConsumeMethodsMap = new HashMap<>();


    private static Set<String> activePushKeys = new HashSet<>();

    private static Set<String> passiveCallKeys = new HashSet<>();


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqSubMessage msg) throws Exception {
        log.info(msg.toString());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("【" + ctx.channel().id() + "】" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelActive");

        MqSubMessage mqSubMessage = genMqSubscribeMsg();
        ctx.writeAndFlush(mqSubMessage);
        super.channelActive(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("【" + ctx.channel().id() + "】" + new SimpleDateFormat("yyyy/MM/dd HH/mm/ss").format(new Date()) + "==>>>"
                + "channelInactive");
        super.channelInactive(ctx);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    @SneakyThrows
    public static MqSubMessage genMqSubscribeMsg() {
        log.info("scan consume Method...");
        List<AnnotationMethodInfo<MqSubScribe>> annotationMethodInfos = AnnotationUtil.scanAnnotationMethods(MqSubScribe.class, ClientConfig.getConsumePackage(), true);
        log.info("scan consume Method successfully");
        fillTargetObjet(annotationMethodInfos);
        for (AnnotationMethodInfo<MqSubScribe> annotationMethodInfo : annotationMethodInfos) {
            MqSubScribe annotation = annotationMethodInfo.getAnnotation();
            String[] keys = annotation.keys();
            for (String key : keys) {
                if (activePushKeys.contains(key) || passiveCallKeys.contains(key)) {
                    String errMsg = "can not reSubScribe the same Key ";
                    log.error(errMsg);
                    throw new RuntimeException(errMsg);
                }
                if (!MqHandleUtil.checkIfKeyValid(key)) {
                    key = annotation.keyType().MQ_PREFIX + key;
                }
                if (annotation.activePush()) {
                    activePushKeys.add(key);
                } else {
                    passiveCallKeys.add(key);
                }
                keyConsumeMethodsMap.put(key, annotationMethodInfo);
            }
        }
        MqSubMessage mqSubMessage = new MqSubMessage();
        mqSubMessage.setClientId(UUID.randomUUID().toString());
        mqSubMessage.setType(TYPE_SUBSCRIBE);
        mqSubMessage.setActivePushKeys(activePushKeys);
        mqSubMessage.setPassiveCallKeys(passiveCallKeys);
        return mqSubMessage;
    }

    private static void fillTargetObjet(List<AnnotationMethodInfo<MqSubScribe>> annotationMethodInfos) {
        Map<Class<?>, Object> targetClassMap = new HashMap<>();
        for (AnnotationMethodInfo<MqSubScribe> annotationMethodInfo : annotationMethodInfos) {
            Method method = annotationMethodInfo.getMethod();
            Class<?> targetObjectClass = method.getDeclaringClass();
            if (Modifier.isStatic(method.getModifiers())) {
                annotationMethodInfo.setTargetObject(targetObjectClass);
            } else {
                //实例方法的情况下将会被代理生成一个新的 对象,并使用此对象执行消息消费逻辑
                Object target = targetClassMap.getOrDefault(targetObjectClass, null);
                if (target == null) {
                    try {
                        Constructor<?> constructor = targetObjectClass.getConstructor();
                        //if private
                        constructor.setAccessible(true);
                        target = constructor.newInstance();
                    } catch (Exception e) {
                        log.info("instancing consume method 's object failed,for targetObjectClass:{} without nonArgsConstructor", targetObjectClass.getName());
                        log.error("", e);
                        continue;
                    }
                    targetClassMap.put(targetObjectClass, target);
                }
                annotationMethodInfo.setTargetObject(target);
            }
            //todo 兼容Springboot
        }
    }
}
