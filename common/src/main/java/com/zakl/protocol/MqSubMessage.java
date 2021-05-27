package com.zakl.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MqSubMessage implements Serializable {

    /**
     * 心跳消息
     */
    public transient static final byte TYPE_HEARTBEAT = 0x00;

    /**
     * mq注册订阅
     */
    public transient static final byte TYPE_SUBSCRIBE = 0x01;

    /**
     * mq消息传递
     */
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
     * clientId
     */
    public String clientId;

    /**
     * 客户端权重
     */
    public Integer clientWeight = -1;

    /**
     * 是否需要服务器主动推送
     */
    public boolean initiativePush = true;



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
