package com.ciscospark.helloworld;

import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * For testing, we don't want an external redis cache, so we substitute a simple hashmap instead
 */
@TestConfiguration
class RedisTestConfig {

    @Bean
    @Qualifier("store")
    public Map<String, String> greetingStore() {
        return new HashMap<>();
    }
}
