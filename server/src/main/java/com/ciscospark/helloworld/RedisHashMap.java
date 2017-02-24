package com.ciscospark.helloworld;

import com.cisco.wx2.server.util.JedisPoolRedisCacheClient;
import com.cisco.wx2.server.util.RedisCacheClient;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.NotImplementedException;
import redis.clients.jedis.JedisPool;


import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A simple wrapper around RedisCacheClient to make it function as a standard java collections Map, enabling us to
 * abstract redis out of our tests.
 */
public class RedisHashMap<K, V> implements Map<K, V> {


    private final TypeReference<V> valueType = new TypeReference<V>(){};
    private final int expiration;
    private RedisCacheClient<V> cacheClient;



    public RedisHashMap(JedisPool jedisPool, String redisPrefix, int expiration, ObjectMapper objectMapper, MetricRegistry metricRegistry) {
        cacheClient = new JedisPoolRedisCacheClient<V>(jedisPool, redisPrefix, valueType, (e,t) -> { throw new RuntimeException(t); }, objectMapper, metricRegistry);
        this.expiration = expiration;
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
        return cacheClient.get(String.valueOf(key.hashCode()));
    }

    @Override
    public V put(K key, V value) {
        Preconditions.checkNotNull(key);
        cacheClient.set(String.valueOf(key.hashCode()), value, expiration);
        return value;
    }

    @Override
    public V remove(Object key) {
        Preconditions.checkNotNull(key);
        V v = get(key);
        cacheClient.delete(String.valueOf(key.hashCode()));
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
