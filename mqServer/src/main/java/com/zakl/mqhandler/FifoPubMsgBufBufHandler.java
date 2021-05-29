package com.zakl.mqhandler;

import com.zakl.dto.MqMessage;

public class FifoPubMsgBufBufHandler implements PubMsgBufHandle {

    private final static FifoPubMsgBufBufHandler instance = new FifoPubMsgBufBufHandler();

    private FifoPubMsgBufBufHandler() {

    }

    public static PubMsgBufHandle getInstance() {
        return instance;
    }

    @Override
    public MqMessage listen(String keyName) {
        return null;
    }

    @Override
    public void add(String keyName, MqMessage mqMessage, boolean tail) {

    }
}
