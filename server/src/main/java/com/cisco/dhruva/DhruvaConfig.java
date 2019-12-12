package com.cisco.dhruva;

import com.cisco.wx2.redis.RedisDataSource;
import com.cisco.wx2.redis.RedisDataSourceManager;
import com.cisco.wx2.server.config.ConfigProperties;
import com.cisco.wx2.util.ObjectMappers;
import com.ciscospark.server.CiscoSparkServerProperties;
import com.ciscospark.server.Wx2ConfigAdapter;
import com.codahale.metrics.MetricRegistry;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
public class DhruvaConfig extends Wx2ConfigAdapter {
  private static final long DEFAULT_CACHE_TIMEOUT = 10;

  @Autowired private ConfigProperties configProperties;

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
  @Qualifier("store")
  public Map<String, String> greetingStore(
      RedisDataSourceManager redisDataSourceManager,
      MetricRegistry metricRegistry,
      CiscoSparkServerProperties props) {
    RedisDataSource redisDataSource = redisDataSourceManager.getRedisDataSource("dhruvaapp");
    return new RedisHashMap(
        redisDataSource,
        props.getName() + "-store",
        defaultCacheTimeout(),
        String.class,
        ObjectMappers.getObjectMapper(),
        metricRegistry);
  }

  @Bean
  public Integer defaultCacheTimeout() {
    return (int) TimeUnit.MINUTES.toSeconds(DEFAULT_CACHE_TIMEOUT);
  }
}
