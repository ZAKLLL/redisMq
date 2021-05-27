package com.zakl.config;

import com.zakl.common.Config;
import com.zakl.container.MqServerContainer;
import com.zakl.protocol.MqPubMessage;
import com.zakl.protocol.MqSubMessage;
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
public class ServerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ServerConfig instance = new ServerConfig();


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
        MqServerContainer.regisMsgPort(MqPubMessage.class,mqPubPort);

        mqSubPort = Config.getInstance().getIntValue("server.mqSubPort");
        MqServerContainer.regisMsgPort(MqSubMessage.class,mqSubPort);

        log.info(
                "config init mqPubPort {}, mqSubPort {}",
                mqPubPort, mqSubPort);
    }

    public static void init(){
        log.info("init ServerConfig");
    }

}
