package com.zakl.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MqPubMessage implements Serializable {

    /**
     * 心跳消息
     */
    public transient static final byte TYPE_HEARTBEAT = 0x00;

    /**
     * mq发布消息
     */
    public transient static final byte TYPE_PUBLISH = 0x01;


    /**
     * 消息类型
     */
    private byte type;

    /**
     * 消息id
     */
    private String messageId;


    /**
     * mq推送数据
     */
    private Map<String, List<String>> pubMsgs;


}
