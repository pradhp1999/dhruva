package com.ciscospark.helloworld;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties that are specific to the hello-world service.
 *
 * See also: http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
 */
@Data
@ConfigurationProperties(prefix = "hello-world")
@Component
public class HelloWorldProperties {
    /**
     * The default greeting prefix if no specific greeting is found.
     */
    String defaultGreetingPrefix = "Hello";

    /**
     * Message to be delivered with the greeting.
     */
    String message = "Beer is proof God loves us and wants us to be happy";

    public HelloWorldProperties() {
    }

    public HelloWorldProperties(String defaultGreetingPrefix, String message) {
        this.defaultGreetingPrefix = defaultGreetingPrefix;
        this.message = message;
    }

    public String getDefaultGreetingPrefix() {
        return defaultGreetingPrefix;
    }

    public String getMessage() {
        return message;
    }
}
