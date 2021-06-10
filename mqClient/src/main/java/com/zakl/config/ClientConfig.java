package com.zakl.config;

import com.zakl.util.ConfigUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.UUID;

/**
 * server config
 */
@Slf4j
@Data
public class ClientConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private ClientConfig() {

    }

    /**
     * mqServerIp
     */
    private static final String serverIp;


    /**
     * 接收publish 端口
     */
    private static final Integer mqPubPort;

    /**
     * 接收subscribe端口
     */
    private static final Integer mqSubPort;

    /**
     * consume method package
     */
    private static final String consumePackage;

    /**
     * clientId for pubClient (uuid)
     */
    private static final String pubClientId;

    /**
     * clientId for subClient (uuid)
     */
    private static final String subClientId;

    static {
        mqPubPort = ConfigUtil.getInstance().getIntValue("server.mqPubPort");

        mqSubPort = ConfigUtil.getInstance().getIntValue("server.mqSubPort");

        serverIp = ConfigUtil.getInstance().getStringValue("server.ip");

        consumePackage = ConfigUtil.getInstance().getStringValue("client.consumerPackage");

        pubClientId = UUID.randomUUID().toString();

        subClientId = UUID.randomUUID().toString();
        log.info(
                "\nconfig init serverIp: {} \n mqPubPort: {} \n mqSubPort: {} \n consumePackage: {} \n pubClientId(uuid): {} \n subClientId(uuid): {}",
                serverIp, mqPubPort, mqSubPort, consumePackage, pubClientId, subClientId);
    }

    public static String getServerIp() {
        return serverIp;
    }

    public static Integer getMqPubPort() {
        return mqPubPort;
    }

    public static Integer getMqSubPort() {
        return mqSubPort;
    }

    public static String getConsumePackage() {
        return consumePackage;
    }

    public static String getPubClientId() {
        return pubClientId;
    }

    public static String getSubClientId() {
        return subClientId;
    }
}
