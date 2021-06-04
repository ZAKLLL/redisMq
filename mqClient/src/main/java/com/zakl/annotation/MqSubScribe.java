package com.zakl.annotation;

import com.zakl.constant.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = ElementType.METHOD)
@Retention(RUNTIME)
public @interface MqSubScribe {


    enum KeyTypeEnum {
        LIST(Constants.MQ_LIST_PREFIX),
        SORTEDSET(Constants.MQ_SORTED_SET_PREFIX);

        public String MQ_PREFIX;

        KeyTypeEnum(String MQ_PREFIX) {
            this.MQ_PREFIX = MQ_PREFIX;
        }
    }

    /**
     * 订阅的keys
     */
    String[] keys();


    KeyTypeEnum keyType() default KeyTypeEnum.SORTEDSET;


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


}
