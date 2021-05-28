package com.zakl.core;

import com.zakl.protocol.MqMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZhangJiaKui
 * @classname PubMsgHandler
 * @description TODO
 * @date 5/28/2021 10:20 AM
 */
public interface PubMsgBufHandle {




    MqMessage listen(String keyName);


    void add(String keyName, MqMessage mqMessage, boolean tail);


}
