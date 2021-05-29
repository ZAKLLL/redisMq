package com.zakl.mqhandler;

import com.zakl.dto.MqMessage;

public class FifoMsgDistributeHandler implements MqMsgDistributeHandle {

    private final static FifoMsgDistributeHandler instance = new FifoMsgDistributeHandler();

    private FifoMsgDistributeHandler() {
        
    }

    public static MqMsgDistributeHandle getInstance() {
        return instance;
    }



    @Override
    public void distribute(MqMessage listen, String keyName) {
        //todo 将此信息与RedisServer 中的 max value进行比对,如果当前信息优先级更高,则分发该信息

    }
}
