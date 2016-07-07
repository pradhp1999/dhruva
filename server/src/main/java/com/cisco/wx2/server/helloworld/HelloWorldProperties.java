package com.cisco.wx2.server.helloworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@ConfigurationProperties(prefix="hello-world")
public class HelloWorldProperties {

    private static final Logger log = LoggerFactory.getLogger(HelloWorldProperties.class);
    private final Environment env;

    public HelloWorldProperties(Environment env) {
        this.env = env;
    }


    public URI getApiServiceUrl() {
        URI uri = URI.create(env.getProperty("helloWorldApiServiceUrl",
                "http://localhost:8080/hello-world-server/hello-world/api/v1"));
        log.info("troubleshooting HelloWorldApiServiceUrl : " + uri.toString());
        return uri;
    }
}
