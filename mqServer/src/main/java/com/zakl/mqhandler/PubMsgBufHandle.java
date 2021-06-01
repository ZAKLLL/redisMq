package com.zakl.mqhandler;

import com.zakl.dto.MqMessage;

import java.util.List;
import java.util.Map;

/**
 * @author ZhangJiaKui
 * @classname PubMsgHandler
 * @description TODO
 * @date 5/28/2021 10:20 AM
 */
public interface PubMsgBufHandle {


    MqMessage listen(String keyName);


    void add(boolean tail, String keyName, MqMessage... mqMessage);

    void add(boolean tail, Map<String, List<MqMessage>> keyMsgs);

}
