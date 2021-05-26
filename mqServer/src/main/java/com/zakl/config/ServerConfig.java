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
public class ServerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ServerConfig instance = new ServerConfig();


    /**
     * 代理服务器绑定主机host
     */
    private String serverBind;

    /**
     * 代理服务器与代理客户端通信端口
     */
    private Integer serverPort;

    /**
     * 配置服务绑定主机host
     */
    private String configServerBind;

    /**
     * 配置服务端口
     */
    private Integer configServerPort;


    /**
     * HTTP代理服务 绑定主机host
     */
    private String httpServerProxyBind;

    /**
     * HTTP代理服务 端口
     */
    private Integer httpServerProxyPort;

    /**
     * udp ServerBind
     */
    private String udpServerBind;

    /**
     * udp ServerPort
     */
    private Integer udpServerPort;

    /**
     * 配置服务管理员用户名
     */
    private String configAdminUsername;

    /**
     * 配置服务管理员密码
     */
    private String configAdminPassword;


    private ServerConfig() {


        // 代理服务器主机和端口配置初始化
        this.serverPort = Config.getInstance().getIntValue("server.port");
        this.serverBind = Config.getInstance().getStringValue("server.bind", "0.0.0.0");

        /**
         * HTTP代理服务 绑定主机host 端口
         */
        this.httpServerProxyPort = Config.getInstance().getIntValue("http.proxy.server.port");
        this.httpServerProxyBind = Config.getInstance().getStringValue("http.proxy.server.bind", "0.0.0.0");


        /**
         * UDP Server 绑定主机host 端口
         */
        this.udpServerPort = Config.getInstance().getIntValue("http.udp.server.port");
        this.udpServerBind = Config.getInstance().getStringValue("http.udp.server.bind", "0.0.0.0");


        // 配置服务器主机和端口配置初始化
        this.configServerPort = Config.getInstance().getIntValue("config.server.port");
        this.configServerBind = Config.getInstance().getStringValue("config.server.bind", "0.0.0.0");

        // 配置服务器管理员登录认证信息
        this.configAdminUsername = Config.getInstance().getStringValue("config.admin.username");
        this.configAdminPassword = Config.getInstance().getStringValue("config.admin.password");

        log.info(
                "config init serverBind {}, serverPort {}, configServerBind {}, configServerPort {}, configAdminUsername {}, configAdminPassword {}",
                serverBind, serverPort, configServerBind, configServerPort, configAdminUsername, configAdminPassword);

    }

    public static ServerConfig getInstance() {
        return instance;
    }
}
