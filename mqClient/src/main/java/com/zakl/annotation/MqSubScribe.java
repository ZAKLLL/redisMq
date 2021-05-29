package com.zakl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = ElementType.METHOD)
@Retention(RUNTIME)
public @interface MqSubScribe {

    /**
     * 订阅的keys
     */
     String[] keys() ;

    /**
     * 期望单次推送收到的消息数(当消息不足expectCnt的时候,返回最大消息数)
     */
    int expectCnt() default 1;

    /**
     * 是否自动开启ACK
     */
    boolean autoAck() default true;

    /**
     * 是否由服务端主动推送信息(false 代表客户端主动调用server端获取信息)
     */
    boolean activePush() default true;

    /**
     *
     */

}
