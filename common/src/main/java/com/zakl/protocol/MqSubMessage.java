package com.zakl.protocol;

import cn.hutool.core.lang.Pair;
import com.zakl.dto.MqMessage;
import io.protostuff.Morph;
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
     * mq message for activePush
     */
    public transient static final byte TYPE_MQ_MESSAGE_ACTIVE_PUSH = 0x02;

    /**
     * mq message for passiveCall
     */
    public transient static final byte TYPE_MQ_MESSAGE_PASSIVE_CALL = 0x03;

    /**
     * ACK确认信息 自动确认
     */
    public transient static final byte TYPE_ACK_AUTO = 0x04;

    /**
     * ACK确认消息 手动确认
     */
    public transient static final byte TYPE_ACK_MANUAL = 0x05;

    /**
     * PASSIVE_CALL
     */
    public transient static final byte PASSIVE_CALL = 0x06;


    /**
     * 消息类型
     */
    private byte type;


    /**
     * clientId
     */
    public String clientId;

    /**
     * clientWeight
     */
    public Integer clientWeight = -1;


    /**
     * ackMsgIdSet
     */
    @Morph
    public Set<String> ackMsgIdSet;

    /**
     * key 将会为 自动转换为 MQ_LIST:key or MQ_SORTED_SET:key
     * 取决于@Subscribe 中的注解 配置keyType
     */

    /**
     * 订阅的通道(服务端主动推送)
     * only user for first register , null value when other time;
     */
    @Morph
    private Set<String> activePushKeys;

    /**
     * 客户端主动调用的key,以及 对应的msg数量 只有当type==PASSIVE_CALL 时候有效
     * only user for first register , null value when other time;
     */
    @Morph
    private List<Pair<String, Integer>> passiveCallKeys;

    /**
     * 被动调用的id
     */
    private String passiveCallId;
    /**
     * messages From Server
     */
    @Morph
    private List<MqMessage> mqMessages;

}
