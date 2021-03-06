package com.zakl.protostuff;


import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class ProtostuffSerializePool {

    public static final int SERIALIZE_POOL_MAX_TOTAL = 500;
    public static final int SERIALIZE_POOL_MIN_IDLE = 10;
    public static final int SERIALIZE_POOL_MAX_WAIT_MILLIS = 5000;
    public static final int SERIALIZE_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS = 600000;

    private final GenericObjectPool<ProtostuffSerialize> protostuffPool;
    private static volatile ProtostuffSerializePool poolFactory = null;

    private ProtostuffSerializePool() {
        protostuffPool = new GenericObjectPool<>(new ProtostuffSerializeFactory());
    }

    public static ProtostuffSerializePool getProtostuffPoolInstance() {
        if (poolFactory == null) {
            synchronized (ProtostuffSerializePool.class) {
                if (poolFactory == null) {
                    poolFactory = new ProtostuffSerializePool(SERIALIZE_POOL_MAX_TOTAL, SERIALIZE_POOL_MIN_IDLE, SERIALIZE_POOL_MAX_WAIT_MILLIS, SERIALIZE_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS);
                }
            }
        }
        return poolFactory;
    }

    public ProtostuffSerializePool(final int maxTotal, final int minIdle, final long maxWaitMillis, final long minEvictableIdleTimeMillis) {
        protostuffPool = new GenericObjectPool<>(new ProtostuffSerializeFactory());

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();

        config.setMaxTotal(maxTotal);
        config.setMinIdle(minIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        config.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);

        protostuffPool.setConfig(config);
    }

    public ProtostuffSerialize borrow() {
        try {
            return getProtostuffPool().borrowObject();
        } catch (final Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void restore(final ProtostuffSerialize object) {
        getProtostuffPool().returnObject(object);
    }

    public GenericObjectPool<ProtostuffSerialize> getProtostuffPool() {
        return protostuffPool;
    }
}

