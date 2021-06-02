package com.zakl.mqhandler;

import io.lettuce.core.api.StatefulRedisConnection;

/**
 * @author ZhangJiaKui
 * @classname redisCommandRunner
 * @description TODO
 * @date 6/2/2021 2:22 PM
 */
public abstract class RedisCommandRunner<T> {
    StatefulRedisConnection<String, String> connection;

    T exec() {
        return null;
    }

    public final void setConnection(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
    }

    public final T doExec() {
        return RedisUtil.doExec(this);
    }
}
