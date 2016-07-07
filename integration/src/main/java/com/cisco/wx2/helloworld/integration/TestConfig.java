package com.cisco.wx2.helloworld.integration;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import com.cisco.wx2.helloworld.client.HelloWorldClientFactory;
import com.cisco.wx2.test.BaseTestConfig;
import com.cisco.wx2.test.TestUser;


@Configuration
public class TestConfig extends BaseTestConfig {
    @Autowired
    private Environment env;

    @Bean
    public TestUser testUser() {
        return testUserManager().createTestUser(null, "Hello World Test User 1");
    }

    @Bean
    public HelloWorldClientFactory avatarClientFactory() {
        return HelloWorldClientFactory.builder().maxConnections(32)
                .maxConnectionsPerRoute(32).connectTimeout(30 * 1000)
                .readTimeout(30 * 1000).userAgent("Hello World test agent")
                .apiServiceUrl(apiServiceUrl()).build();
    }

    @Bean
    public URI apiServiceUrl() {
        return URI.create(env.getProperty("helloWorldApiServiceUrl",
                "http://localhost:8080/hello-world-server/hello-world/api/v1"));
    }

}
