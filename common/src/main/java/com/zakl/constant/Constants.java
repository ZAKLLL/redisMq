package com.zakl.constant;

/**
 * @author ZhangJiaKui
 * @classname Contains
 * @description 常量
 * @date 5/28/2021 10:52 AM
 */
public interface Constants {
    /**
     * 推送到服务端的消息缓冲区
     */
    Integer PUB_BUFFER_MAX_LIMIT = 100000;

    /**
     * 以List数据结构实现的Mq队列 key名称前缀
     */
    String MQ_LIST_PREFIX = "MQ_LIST:";

    /**
     * 以Sorted_Set 数据结构实现的Mq队列 Key 名称前缀
     */
    String MQ_SORTED_SET_PREFIX = "MQ_SORTED_SET:";


}
