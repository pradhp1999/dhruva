package com.ciscospark.helloworld;

import com.cisco.wx2.server.config.ConfigProperties;
import com.cisco.wx2.test.BaseTestConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
class TestConfig extends BaseTestConfig {

    @Bean
    public ConfigProperties configProperties(Environment env) {
        return new ConfigProperties(env, "hello-world-tests");
    }

    @Bean
    @Qualifier("store")
    public Map<String, String> greetingStore() {
        return new HashMap<>();
    }
}
