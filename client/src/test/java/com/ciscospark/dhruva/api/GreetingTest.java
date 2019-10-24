package com.ciscospark.dhruva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

public class GreetingTest {
    private JacksonTester<Greeting> json;

    @Before
    public void setup() {
        // TODO: Extract ObjectMappers from cloud-apps-common into cisco-spark-client and consume from there.
        JacksonTester.initFields(this, new ObjectMapper());
    }

    Greeting greeting = Greeting.builder().greeting("Hello Spark").message("Beer is proof God loves us and wants us to be happy").build();

    @Test
    public void testSerialize() throws Exception {
        assertThat(json.write(greeting)).extractingJsonPathStringValue("$.greeting").isEqualTo(greeting.getGreeting());
        assertThat(json.write(greeting)).extractingJsonPathStringValue("$.message").isEqualTo(greeting.getMessage());
    }

    @Test
    public void testDeserialize() throws Exception {
        String content = "{\"greeting\": \"Hello Spark\", \"message\": \"Beer is proof God loves us and wants us to be happy\"}";
        assertThat(json.parse(content)).isEqualTo(greeting);
        assertThat(json.parseObject(content).getGreeting()).isEqualTo(greeting.getGreeting());
    }
}