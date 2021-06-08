package com.zakl.msgdistribute;

import cn.hutool.core.lang.UUID;
import com.zakl.annotation.AnnotationMethodInfo;
import com.zakl.annotation.AnnotationUtil;
import com.zakl.annotation.MqSubScribe;
import com.zakl.config.ClientConfig;
import com.zakl.dto.MqMessage;
import com.zakl.protocol.MqSubMessage;
import com.zakl.util.MqHandleUtil;
import com.zakl.util.ReflectionUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static com.zakl.protocol.MqSubMessage.TYPE_SUBSCRIBE;

/**
 * @author ZhangJiaKui
 * @classname ClientRegister
 * @description TODO
 * @date 6/4/2021 3:57 PM
 */
@Slf4j
public class keyMethodManager {
    public static final Map<String, AnnotationMethodInfo<MqSubScribe>> keyConsumeMethodsMap = new HashMap<>();


    private static final Set<String> activePushKeys = new HashSet<>();

    private static final Set<String> passiveCallKeys = new HashSet<>();

    public static void main(String[] args) {
        MqSubMessage mqSubMessage = genMqSubscribeMsg();
        System.out.println(mqSubMessage);
    }

    @SneakyThrows
    public static MqSubMessage genMqSubscribeMsg() {
        log.info("scan consume Method...");
        List<AnnotationMethodInfo<MqSubScribe>> annotationMethodInfos = AnnotationUtil.scanAnnotationMethods(MqSubScribe.class, ClientConfig.getConsumePackage(), true);
        log.info("scan consume Method successfully");
        fillTargetObjet(annotationMethodInfos);
        for (AnnotationMethodInfo<MqSubScribe> annotationMethodInfo : annotationMethodInfos) {
            MqSubScribe annotation = annotationMethodInfo.getAnnotation();
            Method method = annotationMethodInfo.getMethod();

            checkConsumeMethodParam(annotation, method);

            String[] keys = annotation.keys();

            for (String key : keys) {
                if (!MqHandleUtil.checkIfKeyValid(key)) {
                    key = annotation.keyType().MQ_PREFIX + key;
                }
                if (keyConsumeMethodsMap.containsKey(key)) {
                    String errMsg = "can not reSubScribe the same Key ";
                    log.error(errMsg);
                    throw new RuntimeException(errMsg);
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

    /**
     * checkConsumeMethodParam if meet the requirements
     *
     * @param mqSubScribe
     * @param method
     */
    private static void checkConsumeMethodParam(MqSubScribe mqSubScribe, Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        boolean flag = false;
        if (mqSubScribe.autoAck() && parameterTypes.length == 1) {
            if (parameterTypes[0].isAssignableFrom(List.class)) {
                flag = true;
            }
        } else if (!mqSubScribe.autoAck() && parameterTypes.length == 2) {
            if (parameterTypes[0].isAssignableFrom(List.class) && parameterTypes[1].isAssignableFrom(AckClientHandler.class)) {
                flag = true;
            }
        }
        if (!flag) throw new RuntimeException("illegal paramException");
    }


    public static void disTributeMsgToConsumeMethod(MqSubMessage msg) {
        Map<String, List<MqMessage>> keyMsgsMap = msg.getMqMessages().stream().collect(Collectors.groupingBy(MqMessage::getKey));
        for (String key : keyMsgsMap.keySet()) {
            List<MqMessage> mqMessages = keyMsgsMap.get(key);
            AnnotationMethodInfo<MqSubScribe> annotationMethodInfo = keyConsumeMethodsMap.get(key);
            MqSubScribe annotation = annotationMethodInfo.getAnnotation();
            Method method = annotationMethodInfo.getMethod();
            Object object = annotationMethodInfo.getTargetObject();
            if (annotation.autoAck()) {
                new AckClientHandler(MqSubMessage.TYPE_ACK_AUTO).confirm(mqMessages.toArray(new MqMessage[0]));
                ReflectionUtils.reflectInvoke(object, method, new Object[]{mqMessages});
            } else {
                ReflectionUtils.reflectInvoke(object, method, new Object[]{mqMessages, new AckClientHandler(MqSubMessage.TYPE_ACK_MANUAL)});
            }
        }
    }
}
