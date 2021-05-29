package com.zakl.dto;

import lombok.Data;

/**
 * @author ZhangJiaKui
 * @classname MqMessageDTO
 * @description TODO
 * @date 5/28/2021 10:00 AM
 */
@Data
public class MqMessage {
    /**
     * 消息id uuid唯一
     */
    private String messageId;

    /**
     * 权重 如果为-1将基于redisQueue 进行Fifo的消息传递
     */
    private Double weight;


    /**
     * channel name
     */
    private String key;

    /**
     * 消息内容
     */
    private String message;
}
