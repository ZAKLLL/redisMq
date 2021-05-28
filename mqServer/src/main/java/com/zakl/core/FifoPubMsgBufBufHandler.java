package com.zakl.core;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import com.zakl.protocol.MqMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

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
