package com.zakl.config;

import com.zakl.common.Config;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

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


    static {
        host = Config.getInstance().getStringValue("redis.host");
        port = Config.getInstance().getIntValue("redis.port");
        pwd = Config.getInstance().getStringValue("redis.pwd");
        db = Config.getInstance().getIntValue("redis.db");

        log.info(
                "config init redisServer info host {}, port {}, pwd {} ,db{}",
                host, port, pwd, db);

    }


}
