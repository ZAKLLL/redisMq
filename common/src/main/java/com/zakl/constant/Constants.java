package com.zakl.constant;

/**
 * @author ZhangJiaKui
 * @classname Contains
 * @description 常量
 * @date 5/28/2021 10:52 AM
 */
public interface Constants {


    /**
     * REDIS Request TimeOut limit
     */
    Integer TIME_OUT_LIMIT = 10000;


    /**
     * 以List数据结构实现的Mq队列 key名称前缀
     */
    String MQ_LIST_PREFIX = "MQ:LIST:";

    /**
     * 以Sorted_Set 数据结构实现的Mq队列 Key 名称前缀
     */
    String MQ_SORTED_SET_PREFIX = "MQ:SORTED_SET:";


    /**
     * MQ 服务所有存放在redisServer中的前缀
     */
    String REDIS_PREFIX = "MQ:";


    /**
     * ack info in redis
     */
    String ACK_SET_KEY = "MQ:ACK:SET:";

    /**
     * sortedSet placeholder score
     */
    Double MIN_SCORE = Double.MIN_VALUE;


    /**
     * score for List msg to construct MqMessage(every element has a score,-1 means list data)
     */
    Double LIST_MSG_SCORE=-1d;
}
