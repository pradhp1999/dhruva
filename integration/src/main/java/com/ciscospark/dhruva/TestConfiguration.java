package com.ciscospark.dhruva;

import com.cisco.wx2.test.BaseTestConfig;
import com.cisco.wx2.test.TestProperties;
import com.ciscospark.dhruva.client.DhruvaClientFactory;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.URI;

@Configuration
@EnableConfigurationProperties
public class TestConfiguration extends BaseTestConfig {
    @Component
    @ConfigurationProperties
    static class DhruvaTestProperties {
        @Value("${helloWorldPublicUrl:http://localhost:8080/api/v1}")
        private String helloWorldUrl;

        URI helloWorldUrl() {
            return URI.create(helloWorldUrl);
        }
    }

    @Bean
    public DhruvaClientFactory helloWorldClientFactory(TestProperties testProperties, DhruvaTestProperties helloWorldTestProperties) {
        Preconditions.checkNotNull(testProperties);
        return DhruvaClientFactory.builder(testProperties, helloWorldTestProperties.helloWorldUrl()).build();
    }
}
