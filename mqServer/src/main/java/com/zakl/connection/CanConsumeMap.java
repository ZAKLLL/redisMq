package com.zakl.connection;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CanConsumeMap {

    public static Map<String, Boolean> statusMap = new ConcurrentHashMap<>();


    public static void initCanConsumeMap() {
        //todo 从redis中获取信息判断是否存在可消费
    }
}

