package com.zakl.protocol;

import cn.hutool.core.lang.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
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
     * key 将会为 自动转换为 MQ_LIST:key or MQ_SORTED_SET:key
     * 取决于@Subscribe 中的注解 配置keyType
     */

    /**
     * 订阅的通道(服务端主动推送)
     * only user for first register , null value when other time;
     */
    private Set<String> activePushKeys;

    /**
     * 订阅的通道(客户端主动调用)
     * only user for first register , null value when other time;
     */
    private Set<String> passiveCallKeys;


    /**
     * messages From Server
     */
    private List<Pair<String, String>> keyValues;



}
