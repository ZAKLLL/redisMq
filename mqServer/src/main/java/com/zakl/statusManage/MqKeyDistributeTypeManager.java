package com.zakl.statusManage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MqKeyDistributeTypeManager {

    /**
     * 服务端主动像客户端推送的keys
     */
    public final static Set<String> activePushKeys = new CopyOnWriteArraySet<>();

    /**
     * 客户端主动调用的keys
     */
    public final static Set<String> passiveCallKeys = new CopyOnWriteArraySet<>();
}
