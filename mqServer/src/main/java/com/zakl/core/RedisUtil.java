package com.zakl.core;

import cn.hutool.core.lang.Pair;
import com.zakl.config.RedisConfig;
import io.lettuce.core.RedisClient;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.support.ConnectionPoolSupport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.InputStream;

@Slf4j
public class RedisUtil {
    private static final GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    private static final ZAddArgs nx = new ZAddArgs().nx();

    private final static Integer TIME_OUT_LIMIT = 10000;

    static {

        log.info("initializing the Lettuce pool ");

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxIdle(30);

        RedisClient client = RedisClient.create(String.format("redis://%s@%s:%d/%d", RedisConfig.pwd, RedisConfig.host, RedisConfig.port, RedisConfig.db));

        pool = ConnectionPoolSupport.createGenericObjectPool(
                client::connect, poolConfig);
        log.info("initialize the Lettuce pool succeed");

    }

    @SneakyThrows
    public static StatefulRedisConnection<String, String> getConnection() {
        return pool.borrowObject(10000);
    }

    public static void returnConnection(StatefulRedisConnection<String, String> connection) {
        pool.returnObject(connection);
    }


    @SafeVarargs
    public static void syncSortedSetAdd(String key, Pair<Double, String>... members) {

        Object[] scoreMembers = new Object[members.length * 2];
        int index = 0;
        for (Pair<Double, String> member : members) {
            scoreMembers[index++] = member.getKey();
            scoreMembers[index++] = member.getValue();
        }
        StatefulRedisConnection<String, String> connection = getConnection();
        connection.sync().zadd(key, nx, scoreMembers);
        returnConnection(connection);
    }

    public static ScoredValue<String> syncSortedSetPopMax(String key) {
        StatefulRedisConnection<String, String> connection = getConnection();

        ScoredValue<String> zMaxValue = connection.sync().zpopmax(key);

        returnConnection(connection);

        return zMaxValue;
    }

    public static void syncQueueAdd(String key, String... members) {
        StatefulRedisConnection<String, String> connection = getConnection();

        connection.sync().lpush(key, members);

        returnConnection(connection);
    }

    public static String syncQueueRpop(String key) {
        StatefulRedisConnection<String, String> connection = getConnection();

        String rpop = connection.sync().rpop(key);

        returnConnection(connection);
        return rpop;
    }


}
