package com.zakl.mqhandler;

import com.zakl.dto.MqMessage;

import java.util.List;
import java.util.Map;

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
    public void add(boolean tail, String keyName, MqMessage... mqMessage) {

    }

    @Override
    public void add(boolean tail, Map<String, List<MqMessage>> keyMsgs) {



    }
}
