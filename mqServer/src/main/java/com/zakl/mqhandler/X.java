package com.zakl.mqhandler;


import com.zakl.protocol.MqSubMessage;
import com.zakl.statusManage.StatusManager;
import com.zakl.statusManage.SubClientInfo;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.zakl.statusManage.MqKeyDistributeTypeManager.activePushKeys;
import static com.zakl.statusManage.MqKeyDistributeTypeManager.passiveCallKeys;
import static com.zakl.statusManage.MqKeyHandleStatusManager.clientIdMap;

@Slf4j
public class X {

    private final static Set<String> keySet = new CopyOnWriteArraySet<>();


    public static void registerSubClient(ChannelHandlerContext ctx, MqSubMessage msg) {

        activePushKeys.addAll(msg.getActivePushKeys());
        passiveCallKeys.addAll(msg.getPassiveCallKeys());

        SubClientInfo subClientInfo = new SubClientInfo(msg.clientId, msg.getClientWeight(), ctx);

        clientIdMap.put(msg.getClientId(), subClientInfo);


    }

    public static void doMqClientRegister(Iterable<String> keys, SubClientInfo clientInfo) {


        for (final String keyName : keys) {
            if (!keySet.contains(keyName)) {
                StatusManager.initNewKey(keyName, clientInfo);
                keySet.add(keyName);
            }
        }
    }

    /**
     * 如果client已经离线,需要清楚当前客户端在服务端的状态信息
     *
     * @param keyName
     * @param clientInfo
     */
    private static void cleanSubClientInfo(String keyName, SubClientInfo clientInfo) {


    }


}

