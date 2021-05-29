package com.zakl.core;

import com.zakl.dto.MqMessage;

public class PriorityMsgDistributeHandler implements MqMsgDistributeHandle {

    private final static PriorityMsgDistributeHandler instance = new PriorityMsgDistributeHandler();

    private PriorityMsgDistributeHandler() {
        
    }


    @Override
    public MqMsgDistributeHandle getInstance() {
        return instance;
    }

    public void distribute(MqMessage listen) {
        //todo 将此信息与RedisServer 中的 max value进行比对,如果当前信息优先级更高,则分发该信息

    }
}
