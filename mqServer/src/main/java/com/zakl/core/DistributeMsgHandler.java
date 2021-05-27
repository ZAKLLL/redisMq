package com.zakl.core;

import com.zakl.protocol.MqPubMessage;

public class DistributeMsgHandler {


    public static void distributePubMsg(MqPubMessage listen) {
        //todo 将此信息与RedisServer 中的 max value进行比对,如果当前信息优先级更高,则分发该信息

    }
}
