package com.zakl.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqSubMessage implements Serializable {

    /**
     * 心跳消息
     */
    public transient static final byte TYPE_HEARTBEAT = 0x00;

    /**
     * mq订阅信息
     */
    public transient static final byte TYPE_SUBSCRIBE = 0x01;

    public transient static final byte TYPE_MQ_MESSAGE = 0x02;

    /**
     * ACK确认信息
     */
    public transient static final byte TYPE_ACK = 0x03;


    /**
     * 消息类型
     */
    private byte type;

    /**
     * 消息id
     */
    private String messageId;

    /**
     * 订阅的通道
     */
    private Set<String> channels;


    /**
     * 收到消息的通道
     */
    private String msgChannel;

    /**
     * 收到的具体信息
     */
    private String msg;


}
