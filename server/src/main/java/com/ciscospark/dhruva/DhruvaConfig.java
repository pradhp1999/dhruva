package com.ciscospark.dhruva;

import com.cisco.wx2.dto.health.ServiceType;
import com.cisco.wx2.feature.client.FeatureClientFactory;
import com.cisco.wx2.redis.RedisDataSource;
import com.cisco.wx2.redis.RedisDataSourceManager;
import com.cisco.wx2.server.config.ConfigProperties;
import com.cisco.wx2.server.health.MonitorableClientServiceMonitor;
import com.cisco.wx2.server.health.ServiceMonitor;
import com.cisco.wx2.util.ObjectMappers;
import com.ciscospark.server.CiscoSparkServerProperties;
import com.ciscospark.server.Wx2ConfigAdapter;
import com.codahale.metrics.MetricRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnWebApplication
public class DhruvaConfig extends Wx2ConfigAdapter {
    private static final long DEFAULT_CACHE_TIMEOUT = 10;

    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private FeatureClientFactory featureClientFactory;

    @Override
    public String getServiceName() {
        return "Dhruva";
    }

    @Override
    public String getMetricsNamespace() {
        return "dhruva";
    }

    @Override
    public String getUserAgent() {
        return "Dhruva/1.0";
    }


    @Bean
    @Qualifier("featureClientFactory")
    public FeatureClientFactory featureClientFactory() {
        return FeatureClientFactory.builder(configProperties)
                .baseUrl(configProperties.getFeatureServicePublicUrl())
                .objectMapper(ObjectMappers.getObjectMapper())
                .build();
    }


    @Bean
    @ConditionalOnBean(name = {"featureClientFactory"})
    public ServiceMonitor featureServiceMonitor() {
        return MonitorableClientServiceMonitor.newMonitor(featureClientFactory, ServiceType.OPTIONAL);
    }

    @Bean
    @Qualifier("store")
    public Map<String, String> greetingStore(RedisDataSourceManager redisDataSourceManager, MetricRegistry metricRegistry, CiscoSparkServerProperties props) {
        RedisDataSource redisDataSource = redisDataSourceManager.getRedisDataSource("dhruvaapp");
        return new RedisHashMap(redisDataSource, props.getName() + "-store", defaultCacheTimeout(), String.class, ObjectMappers.getObjectMapper(), metricRegistry);
    }

    @Bean
    public Integer defaultCacheTimeout() {
        return (int) TimeUnit.MINUTES.toSeconds(DEFAULT_CACHE_TIMEOUT);
    }

}
