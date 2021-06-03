package com.zakl.protocol;

import cn.hutool.core.lang.Pair;
import com.zakl.dto.MqMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
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
     * ACK确认信息 自动确认
     */
    public transient static final byte TYPE_ACK_AUTO = 0x03;

    /**
     * ACK确认消息 手动确认
     */
    public transient static final byte TYPE_ACK_MANUAL = 0x04;


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
    private List<MqMessage> mqMessages;


    public List<String> getAllKeys(){
        List<String> keys=new ArrayList<>();
        keys.addAll(activePushKeys);
        keys.addAll(passiveCallKeys);
        return keys;
    }

}
