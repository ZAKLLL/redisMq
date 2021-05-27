package com.zakl.config;

import com.zakl.common.Config;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.*;
import lombok.SneakyThrows;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author ZhangJiaKui
 * @classname RedisConfig
 * @description TODO
 * @date 5/27/2021 4:58 PM
 */

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

    private final static String host;

    private final static Integer port;

    private final static String pwd;

    private final static Integer db;


    static {
        host = Config.getInstance().getStringValue("redis.host");
        port = Config.getInstance().getIntValue("redis.port");
        pwd = Config.getInstance().getStringValue("redis.pwd");
        db = Config.getInstance().getIntValue("redis.db");
    }

    private static GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    //todo 池化lettuce
    public static void init() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(30);

        RedisClient client = RedisClient.create(String.format("redis://%s@%s:%d/%d", pwd, host, port, db));

        pool = ConnectionPoolSupport.createGenericObjectPool(
                client::connect, poolConfig);

    }

    @SneakyThrows
    public static StatefulRedisConnection<String, String> getConnection() {
        return pool.borrowObject(10000);
    }

    public static void returnConnection(StatefulRedisConnection<String, String> connection) {
        pool.returnObject(connection);
    }

}
