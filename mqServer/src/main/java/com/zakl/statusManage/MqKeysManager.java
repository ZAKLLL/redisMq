package com.zakl.statusManage;

import java.util.HashSet;
import java.util.Set;

public class MqKeysManager {

    /**
     * 服务端主动像客户端推送的keys
     */
    public final static Set<String> activePushKeys = new HashSet<>();

    /**
     * 客户端主动调用的keys
     */
    public final static Set<String> passiveCallKeys = new HashSet<>();
}
