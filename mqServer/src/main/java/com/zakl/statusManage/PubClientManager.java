package com.zakl.statusManage;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class PubClientManager {

    private PubClientManager() {

    }

    private static final PubClientManager instance = new PubClientManager();


    public Map<String, ChannelHandlerContext> pubClientMap = new HashMap<>();


    public static PubClientManager getInstance() {
        return instance;
    }
}
