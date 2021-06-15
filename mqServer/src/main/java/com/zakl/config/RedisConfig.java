package com.zakl.config;

import com.zakl.util.ConfigUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ZhangJiaKui
 * @classname RedisConfig
 * @description TODO
 * @date 5/27/2021 4:58 PM
 */
@Slf4j
public class RedisConfig {


//    private static Integer maxIdle;
//
//    private static Integer minIdle;
//
//    private static Integer maxTotal;
//
//    private static Integer maxWaitMillis;
//
//    private static Integer minEvictableIdleTimeMillis;
//
//    private static Integer numTestsPerEvictionRun;
//
//    private static Integer connectTimeout;

    public final static String host;

    public final static Integer port;

    public final static String pwd;

    public final static Integer db;

    public final static int poolSize;


    static {
        host = ConfigUtil.getInstance().getStringValue("redis.host");
        port = ConfigUtil.getInstance().getIntValue("redis.port");
        pwd = ConfigUtil.getInstance().getStringValue("redis.pwd");
        db = ConfigUtil.getInstance().getIntValue("redis.db");
        poolSize = ConfigUtil.getInstance().getIntValue("redis.poolSize",100);

        log.info(
                "config init redisServer info host {}, port {}, pwd {} ,db{}",
                host, port, pwd, db);

    }


}
