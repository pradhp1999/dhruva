package com.cisco.dhruva;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.metric.InfluxClient;
import com.cisco.dhruva.common.metric.MetricClient;
import com.ciscospark.server.Wx2ConfigAdapter;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnWebApplication
public class DhruvaConfig extends Wx2ConfigAdapter {

  private static final long DEFAULT_CACHE_TIMEOUT = 10;

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
  public ExecutorService getExecutorService() {
    return new ExecutorService("DhruvaSipServer");
  }

  @Bean
  public MetricClient getMetricClient() {
    return new InfluxClient();
  }

  @Bean
  public Integer defaultCacheTimeout() {
    return (int) TimeUnit.MINUTES.toSeconds(DEFAULT_CACHE_TIMEOUT);
  }
}
