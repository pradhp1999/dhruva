package com.ciscospark.helloworld;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
class TestConfig {

    @Bean
    @Qualifier("store")
    public Map<String, String> greetingStore() {
        return new HashMap<>();
    }
}
