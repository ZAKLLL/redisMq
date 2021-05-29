package com.zakl.core;

import com.zakl.dto.MqMessage;

public class FifoPubMsgBufBufHandler implements PubMsgBufHandle {

    public static PubMsgBufHandle getInstance() {
        return null;
    }

    @Override
    public MqMessage listen(String keyName) {
        return null;
    }

    @Override
    public void add(String keyName, MqMessage mqMessage, boolean tail) {

    }
}
