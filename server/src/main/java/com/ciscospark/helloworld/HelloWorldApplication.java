package com.ciscospark.helloworld;

import com.cisco.wx2.client.health.ServiceHealthPinger;
import com.cisco.wx2.dto.health.ServiceHealth;
import com.cisco.wx2.dto.health.ServiceType;
import com.cisco.wx2.server.config.ConfigProperties;
import com.cisco.wx2.server.health.MonitorableClientServiceMonitor;
import com.cisco.wx2.server.health.ServiceMonitor;
import com.cisco.wx2.server.util.JedisPoolRedisCacheClient;
import com.cisco.wx2.server.util.RedisCache;
import com.cisco.wx2.server.util.RedisCacheClient;
import com.cisco.wx2.util.ObjectMappers;
import com.cisco.wx2.wdm.client.FeatureClientFactory;
import com.ciscospark.server.CiscoSparkServerProperties;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import redis.clients.jedis.JedisPool;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/*
 * As of this writing, you cannot run the micro-service in IntelliJ via simply clicking on the "Play" button. You
 * need to create a run/debug configuration that uses a Tomcat container (or Jetty, but that is a royal PITA to set up),
 * and add the exploded hello-world-server jar to the configuration.
 *
 * Note that since this is a Spring Boot Application, this class is also a configuration class - there is generally
 * not a need to create a separate configuration class unless you specifically need to override some behavior that is
 * in AbstractConfig. Since we use autoconfiguration - see CiscoSparkServerAutoConfiguration in cisco-spark-autoconfigure
 * - a configuration instance of AbstractConfig is automatically available to the application, so you can simply
 * autowire in all of the beans that you are accustomed to using, such as wx2Properties, env, etc.
 *
 * In the case that you have to derive from Wx2ConfigAdapter/AbstractConfig to create a configuration class, the
 * pattern to use is as follows:
 *
 * @Configuration
 * @ServletComponentScan
 * @ConditionalOnWebApplication      // To prevent the context being loaded during unit tests
 * public class MyConfiguration extends Wx2ConfigAdapter {
 *
 *   @Override
 *   public void myOverride() { ... }
 *  ...
 * }
 *
 * There should not be many use cases that require this.
 *
 * Values such as service name, metrics namespace and user agent are automatically configured and picked up from
 * application.properties/application.yml. Specifically, the algorithm for this is:
 * 1. service-name <-- spring.application.name. If not present, then "application"
 * 2. metrics namespace <-- cisco-spark.server.name. If not present, then service-name.
 * 3. user agent <-- cisco-spark.server.name. If not present, then service-name.
 * 4. service base public URL <-- http://localhost:8080/cisco-spark.server.api-path, or http://localhost:8080/api if not
 *    present.
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = WebMvcAutoConfiguration.class)
public class HelloWorldApplication extends SpringBootServletInitializer {
    private static final String DEFAULT_FEATURE_PUBLIC_URL = "https://feature-a.wbx2.com/feature/api/v1";
    private static final String FEATURE_URL_PROP = "featureServicePublicUrl";
    private static final long DEFAULT_CACHE_TIMEOUT = 10;


    @Autowired
    private ConfigProperties configProperties;

    @Autowired
    private Environment env;

    @Autowired
    private CiscoSparkServerProperties props;

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private MetricRegistry metricRegistry;


    @Bean
    public FeatureClientFactory featureClientFactory() {
        return FeatureClientFactory.builder(configProperties)
                .baseUrl(getFeatureServicePublicUrl())
                .objectMapper(ObjectMappers.getObjectMapper())
                .build();
    }

    /* Feature service currently does not implement ServiceHealthPinger, so we need to create one ourselves, hence
     * the single-line closure for pinger ...
     */
    @Bean
    @ConditionalOnMissingBean(name = "featureServiceServiceMonitor")
    @ConditionalOnBean(name = {"featureClientFactory"})
    @Autowired
    public ServiceMonitor featureServiceServiceMonitor(@Qualifier("featureClientFactory") FeatureClientFactory clientFactory) {
        ServiceHealthPinger pinger = () -> {
            return (ServiceHealth) clientFactory.newClient().get(new Object[]{"ping"}).execute(ServiceHealth.class);
        };
        return MonitorableClientServiceMonitor.newMonitor("FeatureService", ServiceType.REQUIRED, pinger);
    }

    @Bean
    @Qualifier("store")
    public Map<String, String> greetingStore() {
        return new RedisHashMap<>(jedisPool, props.getName() + "-store", defaultCacheTimeout(), ObjectMappers.getObjectMapper(), metricRegistry);
    }

    @Bean
    public Integer defaultCacheTimeout() {
        return (int)TimeUnit.MINUTES.toSeconds(DEFAULT_CACHE_TIMEOUT);
    }

    public URI getFeatureServicePublicUrl() {
        return URI.create(env.getProperty(FEATURE_URL_PROP, DEFAULT_FEATURE_PUBLIC_URL));
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        setRegisterErrorPageFilter(false);
        return application.sources(HelloWorldApplication.class);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(HelloWorldApplication.class, args);
    }
}
