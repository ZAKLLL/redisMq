package com.zakl.mqhandler;

import cn.hutool.core.lang.Pair;
import com.zakl.config.RedisConfig;
import com.zakl.dto.MqMessage;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.zakl.mqhandler.MqHandleUtil.convertMqMessageToJsonDate;

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
    private static StatefulRedisConnection<String, String> getConnection() {
        return pool.borrowObject(10000);
    }

    private static void returnConnection(StatefulRedisConnection<String, String> connection) {
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


    public static void syncSortedSetAdd(MqMessage... mqMessages) {

        Map<String, List<MqMessage>> keyMsgMap = Arrays.stream(mqMessages).collect(Collectors.groupingBy(MqMessage::getKey));

        for (String key : keyMsgMap.keySet()) {
            List<MqMessage> keyMsgs = keyMsgMap.get(key);
            Pair<Double, String>[] members = new Pair[keyMsgs.size()];
            for (int i = 0; i < mqMessages.length; i++) {
                MqMessage msg = mqMessages[i];
                double score = msg.getWeight();
                String data = String.format("uuid:%s\n%s", msg.getMessageId(), msg.getMessage());
                members[i] = new Pair<>(score, data);
            }
            syncSortedSetAdd(key, members);
        }
    }

    public static void syncSetAdd(String key, MqMessage... mqMessages) {
        StatefulRedisConnection<String, String> connection = getConnection();
        try {
            RedisCommands<String, String> sync = connection.sync();
            String[] datas = new String[mqMessages.length];
            for (int i = 0; i < mqMessages.length; i++) {
                datas[i] = convertMqMessageToJsonDate(mqMessages[i]);
            }
            sync.sadd(key, datas);
        } finally {
            returnConnection(connection);
        }
    }

    public static void syncSetRemove(String key, MqMessage... mqMessages) {
        StatefulRedisConnection<String, String> connection = getConnection();
        try {
            RedisCommands<String, String> sync = connection.sync();
            String[] dataes = new String[mqMessages.length];
            for (int i = 0; i < mqMessages.length; i++) {
                dataes[i] = convertMqMessageToJsonDate(mqMessages[i]);
            }
            sync.srem(key, dataes);
        } finally {
            returnConnection(connection);
        }
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

    public static String syncQueueRPop(String key) {
        StatefulRedisConnection<String, String> connection = getConnection();

        String rpop = connection.sync().rpop(key);

        returnConnection(connection);
        return rpop;
    }



}
