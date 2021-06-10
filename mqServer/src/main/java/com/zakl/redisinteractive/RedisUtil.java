package com.zakl.redisinteractive;

import cn.hutool.core.lang.Pair;
import com.zakl.config.RedisConfig;
import com.zakl.dto.MqMessage;
import com.zakl.util.MqHandleUtil;
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

import java.time.chrono.MinguoChronology;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zakl.constant.Constants.MIN_SCORE;
import static com.zakl.util.MqHandleUtil.convertMqMessageToJsonDate;

@Slf4j
@SuppressWarnings("all")
public class RedisUtil {
    private static final GenericObjectPool<StatefulRedisConnection<String, String>> pool;

    private static final ZAddArgs nx = new ZAddArgs().nx();


    static {

        log.info("initializing the Lettuce pool ");

        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(600);

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
        new RedisCommandRunner<Object>() {
            @Override
            Object exec() {
                connection.sync().zadd(key, nx, scoreMembers);
                return super.exec();
            }
        }.doExec();
    }


    public static void syncSortedSetAdd(MqMessage... mqMessages) {

        Map<String, List<MqMessage>> keyMsgMap = Arrays.stream(mqMessages).collect(Collectors.groupingBy(MqMessage::getKey));

        for (String key : keyMsgMap.keySet()) {
            List<MqMessage> keyMsgs = keyMsgMap.get(key);
            Pair<Double, String>[] members = new Pair[keyMsgs.size()];
            for (int i = 0; i < members.length; i++) {
                MqMessage msg = keyMsgs.get(i);
                double score = msg.getWeight();
                String data = String.format("uuid:%s\n%s", msg.getMessageId(), msg.getMessage());
                members[i] = new Pair<>(score, data);
            }
            syncSortedSetAdd(key, members);
        }
    }


    public static void syncSortedSetAdd(String key, MqMessage... mqMessages) {

        Pair<Double, String>[] members = new Pair[mqMessages.length];
        for (int i = 0; i < mqMessages.length; i++) {
            MqMessage msg = mqMessages[i];
            double score = msg.getWeight();
            String data = String.format("uuid:%s\n%s", msg.getMessageId(), msg.getMessage());
            members[i] = new Pair<>(score, data);
        }
        syncSortedSetAdd(key, members);
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


    public static void syncListRPush(MqMessage... msgs) {

        new RedisCommandRunner<Object>() {
            @Override
            Object exec() {
                Map<String, List<MqMessage>> keysMsgs = Arrays.stream(msgs).collect(Collectors.groupingBy(MqMessage::getKey));
                for (Map.Entry<String, List<MqMessage>> keyMsgs : keysMsgs.entrySet()) {
                    syncListRPush(keyMsgs.getKey(), keyMsgs.getValue().toArray(new MqMessage[0]));
                }
                return super.exec();
            }
        }.doExec();
    }

    public static void syncListRPush(String key, MqMessage... msgs) {
        new RedisCommandRunner<Object>() {
            @Override
            Object exec() {
                List<String> collect = Arrays.stream(msgs).flatMap(i -> Stream.of(MqHandleUtil.convertMqMessageToRedisString(i))).collect(Collectors.toList());
                connection.sync().rpush(key, collect.toArray(new String[0]));
                return super.exec();
            }
        }.doExec();
    }


    public static void syncListLPush(String key, MqMessage... msgs) {
        new RedisCommandRunner<Object>() {
            @Override
            Object exec() {
                List<String> collect = Arrays.stream(msgs).flatMap(i -> Stream.of(MqHandleUtil.convertMqMessageToRedisString(i))).collect(Collectors.toList());
                connection.sync().lpush(key, collect.toArray(new String[0]));
                return super.exec();
            }
        }.doExec();
    }

    public static String syncListRPop(String key) {
        StatefulRedisConnection<String, String> connection = getConnection();
        try {
            return connection.sync().rpop(key);
        } finally {
            returnConnection(connection);
        }
    }

    public static List<String> syncListRPop(String key, int cnt) {

        return new RedisCommandRunner<List<String>>() {
            @Override
            List<String> exec() {
                return connection.sync().rpop(key, cnt);
            }
        }.doExec();

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

    public static Set<String> syncSetPopAll(String key) {
        return new RedisCommandRunner<Set<String>>() {
            @Override
            public Set<String> exec() {
                RedisCommands<String, String> sync = connection.sync();
                return sync.spop(key, sync.scard(key));
            }
        }.doExec();
    }


    public static ScoredValue<String> syncSortedSetPopMax(String key) {
        StatefulRedisConnection<String, String> connection = getConnection();
        try {
            RedisCommands<String, String> sync = connection.sync();
            ScoredValue<String> zpopmax = sync.zpopmax(key);
            if (zpopmax.getScore() != MIN_SCORE) {
                return zpopmax;
            } else {
                sync.zadd(key, zpopmax);
                return ScoredValue.empty();
            }
        } finally {
            returnConnection(connection);
        }
    }

    public static List<ScoredValue<String>> syncSortedSetPopMax(String key, Integer cnt) {
        return new RedisCommandRunner<List<ScoredValue<String>>>() {
            @Override
            List<ScoredValue<String>> exec() {
                List<ScoredValue<String>> values = connection.sync().zpopmax(key, cnt);
                ScoredValue<String> minScoredValue = null;
                for (ScoredValue<String> i : values) {
                    if (i.getScore() == MIN_SCORE) {
                        minScoredValue = i;
                        break;
                    }
                }
                if (minScoredValue != null) {
                    values.remove(minScoredValue);
                }
                return values;
            }
        }.doExec();

    }


    public static List<Pair<String, String>> syncKeysInfo(String keyPattern) {
        List<Pair<String, String>> ret = new ArrayList<>();
        StatefulRedisConnection<String, String> connection = getConnection();
        try {
            RedisCommands<String, String> sync = connection.sync();
            List<String> keys = sync.keys(keyPattern);
            for (String key : keys) {
                String type = sync.type(key);
                ret.add(new Pair<>(key, type));
            }
            return ret;
        } finally {
            returnConnection(connection);
        }
    }

    public static List<String> syncKeys(String keyPattern) {
        StatefulRedisConnection<String, String> connection = getConnection();
        try {
            RedisCommands<String, String> sync = connection.sync();
            return sync.keys(keyPattern);
        } finally {
            returnConnection(connection);
        }
    }


    /**
     * exec RedisCommand with auto connect recycle
     *
     * @param runner
     * @param <T>
     * @return
     */
    public static <T> T doExec(RedisCommandRunner<T> runner) {
        StatefulRedisConnection<String, String> connection = getConnection();
        try {
            runner.setConnection(connection);
            return runner.exec();
        } finally {
            returnConnection(connection);
        }
    }

}
