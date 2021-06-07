package com.zakl.ack;

import com.zakl.statusManage.SubClientInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ZhangJiaKui
 * @classname AckHandleThreadManager
 * @description TODO
 * @date 6/3/2021 2:45 PM
 */
@Slf4j
public class AckHandlerManager {

    private final static ExecutorService executors = Executors.newCachedThreadPool();

    private final static Map<String, AckHandler> AckHandleThreadMap = new ConcurrentHashMap<>();


    private static void registerNewAckHandleThread(SubClientInfo subClientInfo) {
        String clientId = subClientInfo.getClientId();
        if (AckHandleThreadMap.containsKey(clientId)) {
            log.info("current client: {} exists ackHandleThread", clientId);
            return;
        }
        log.info("register new ackHandleThread for client:{}", clientId);
        AckHandler ackHandler = new AckHandler(subClientInfo);
        AckHandleThreadMap.put(clientId, ackHandler);
        executors.submit(ackHandler);
    }

    public static AckHandler getClientAckHandler(SubClientInfo subClientInfo) {
        String clientId = subClientInfo.getClientId();
        if (!AckHandleThreadMap.containsKey(clientId)) {
            registerNewAckHandleThread(subClientInfo);
        }
        return AckHandleThreadMap.get(clientId);
    }

    public static void removeAckHandleThread(SubClientInfo subClientInfo) {
        AckHandler ackHandler = AckHandleThreadMap.remove(subClientInfo.getClientId());
        if (ackHandler == null) {
            log.error("current client: {} doesn't exist ackHandleThread", subClientInfo);
            return;
        }
        ackHandler.forceShutDown();
    }
}
