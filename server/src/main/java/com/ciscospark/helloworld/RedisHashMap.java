package com.ciscospark.helloworld;

import com.cisco.wx2.redis.RedisDataSource;
import com.cisco.wx2.redis.operations.commands.RedisCommonCommands;
import com.cisco.wx2.server.util.RedisCache;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A simple wrapper around RedisCache to make it function as a standard java collections Map, enabling us to
 * abstract redis out of our tests.
 * It also use a RedisCommonCommands to validate the caching data for demonstration purpose.
 */
public class RedisHashMap<K, V> implements Map<K, V> {


    private final RedisCache<V> redisCache;
    private final RedisCommonCommands redisCommonCommands;


    public RedisHashMap(RedisDataSource redisDataSource, String redisPrefix, int expiration, Class<V> valueClass, ObjectMapper objectMapper, MetricRegistry metricRegistry) {
        this.redisCache = new RedisCache<>(redisDataSource, redisPrefix, expiration, valueClass , null, objectMapper, metricRegistry, null);
        this.redisCommonCommands = redisDataSource.createRedisCommonCommands();
    }

    @Override
    public int size() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean isEmpty() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public V get(Object key) {
        Preconditions.checkNotNull(key);
        V v = redisCache.getIfPresent(String.valueOf(key.hashCode()));
        if ( v !=null) {
            String stringV = redisCommonCommands.getValue(String.valueOf(key.hashCode()));
            if (stringV == null) {
                throw new RuntimeException("two redis operation not in sync");
            }
        }
        return v;
    }

    @Override
    public V put(K key, V value) {
        Preconditions.checkNotNull(key);
        redisCache.put(String.valueOf(key.hashCode()), value);
        return value;
    }

    @Override
    public V remove(Object key) {
        Preconditions.checkNotNull(key);
        V v = get(key);
        redisCache.invalidate(String.valueOf(key.hashCode()));
        if (redisCommonCommands.exists(String.valueOf(key.hashCode()))) {
            throw new RuntimeException("redis value still exists after invalidate!");
        }
        return v;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.entrySet().forEach((e) -> { put(e.getKey(), e.getValue()); });
    }

    @Override
    public void clear() {
        throw new NotImplementedException("Not implemented");

    }

    @Override
    public Set<K> keySet() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Collection<V> values() {
        throw new NotImplementedException("Not implemented");
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new NotImplementedException("Not implemented");
    }
}
