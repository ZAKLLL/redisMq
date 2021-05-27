package com.zakl.config;

import com.zakl.common.Config;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * server config
 *
 * @author fengfei
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


    static {
        mqPubPort = Config.getInstance().getIntValue("server.mqPubPort");

        mqSubPort = Config.getInstance().getIntValue("server.mqSubPort");

        serverIp = Config.getInstance().getStringValue("server.ip");

        log.info(
                "config init serverIp{}, mqPubPort {}, mqSubPort {}",
                serverIp, mqPubPort, mqSubPort);
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
}
