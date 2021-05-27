package com.zakl.config;

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


    private static Integer maxIdle;

    private static Integer minIdle;

    private static Integer maxTotal;

    private static Integer maxWaitMillis;

    private static Integer minEvictableIdleTimeMillis;

    private static Integer numTestsPerEvictionRun;

    private static Integer connectTimeout;

    private static String host;

    private static String port;

    private static String pwd;

    private static Integer dataBase;


    static {

    }

    private static GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    //todo 池化lettuce
    public static void init() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(30);

        RedisClient client = RedisClient.create("redis://password@localhost:6379/0");


        StatefulRedisConnection<String, String> connect = client.connect();


        pool = ConnectionPoolSupport.createGenericObjectPool(
                client::connect, poolConfig);

    }

    @SneakyThrows
    public StatefulRedisConnection<String, String> getConnection() {
        return pool.borrowObject(10000);
    }
}
