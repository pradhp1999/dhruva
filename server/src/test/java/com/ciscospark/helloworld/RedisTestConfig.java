package com.ciscospark.helloworld;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * For testing, we don't want an external redis cache, so we substitute a simple hashmap instead
 */
@TestConfiguration
class RedisTestConfig {
    @MockBean
    private JedisPool jedisPool;

    @MockBean
    private JedisPoolConfig jedisPoolConfig;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @Bean
    @Qualifier("store")
    public Map<String, String> greetingStore() {
        return new HashMap<>();
    }
}
