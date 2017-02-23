package com.ciscospark.helloworld;

import com.cisco.wx2.test.BaseTestConfig;
import com.cisco.wx2.test.TestProperties;
import com.ciscospark.helloworld.client.HelloWorldClientFactory;
import com.google.common.base.Preconditions;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;

@Configuration
@EnableConfigurationProperties
public class TestConfig extends BaseTestConfig {
    @Data
    @Component
    @ConfigurationProperties
    static class HelloWorldTestProperties extends TestProperties {
        @Autowired
        public HelloWorldTestProperties(Environment env) {

            super(env);
        }

        private URI helloWorldApiUrl = URI.create("http://localhost:8080/api/v1");
    }

    @Autowired
    HelloWorldTestProperties testProperties;

    @Bean
    public HelloWorldClientFactory helloWorldClientFactory() {
        Preconditions.checkNotNull(testProperties);
        return HelloWorldClientFactory.builder()
                .configure(testProperties)
                .apiServiceUrl(testProperties.getHelloWorldApiUrl())
                .build();
    }
}
