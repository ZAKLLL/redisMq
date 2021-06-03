package com.zakl.config;

import com.zakl.protocol.MqPubMessage;
import com.zakl.util.ConfigUtil;
import com.zakl.protocol.MqSubMessage;
import com.zakl.container.MqServerContainer;
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


    /**
     * 推送到服务端的消息缓冲区
     */
    public final static Integer PUB_BUFFER_MAX_LIMIT;

    static {
        PUB_BUFFER_MAX_LIMIT = ConfigUtil.getInstance().getIntValue("server.MqSubMsgBufSize", 10000);

        mqPubPort = ConfigUtil.getInstance().getIntValue("server.mqPubPort");
        MqServerContainer.regisMsgPort(MqPubMessage.class, mqPubPort);

        mqSubPort = ConfigUtil.getInstance().getIntValue("server.mqSubPort");
        MqServerContainer.regisMsgPort(MqSubMessage.class, mqSubPort);


        log.info(
                "config init mqPubPort {}, mqSubPort {} ,mqServerBufSize {}",
                mqPubPort, mqSubPort, PUB_BUFFER_MAX_LIMIT);
    }

    public static void init() {
        log.info("init ServerConfig");
    }

}
