package com.zakl.protocol;

import lombok.Data;

/**
 * @author ZhangJiaKui
 * @classname MqMessageType
 * @description TODO
 * @date 6/10/2021 9:29 AM
 */
@Data
public class SupMqMessage {
    /**
     * 消息类型
     */
    private byte type;

    /**
     * clientId
     */
    private String clientId;
}
