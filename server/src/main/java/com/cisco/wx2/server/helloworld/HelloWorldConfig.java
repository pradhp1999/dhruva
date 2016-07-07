package com.cisco.wx2.server.helloworld;

import com.cisco.wx2.helloworld.client.HelloWorldClient;
import com.cisco.wx2.helloworld.client.HelloWorldClientFactory;
import com.cisco.wx2.server.SparkDispatcherServlet;
import com.cisco.wx2.server.config.ServerConfig;
import com.cisco.wx2.server.config.Wx2Properties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;
import java.net.URI;

/**
 * Spring configuration for the app.
 */
@Configuration
public class HelloWorldConfig extends ServerConfig {

    @Override
    public String getUserAgent() {
        return "sparks/" + getServiceName();
    }

    @Override
    public String getServiceName() {
        return "hello-world";
    }

    @Override
    public String getMetricsNamespace() {
        return getServiceName();
    }

    @Override
    public Wx2Properties wx2Properties() {
        return new Wx2Properties(env);
    }

    @Override
    public URI getServiceBasePublicUrl() {
        return helloWorldProperties().getApiServiceUrl();
    }

    @Bean
    public HelloWorldProperties helloWorldProperties() {
        return new HelloWorldProperties(env);
    }

    @Bean
    @RefreshScope
    public HelloWorldClient helloWorldClient() {
        return HelloWorldClientFactory.builder().apiServiceUrl(helloWorldProperties().getApiServiceUrl()).build().newHelloWorldClient();
    }

    @Bean
    Servlet dispatcherServlet() {
        return new SparkDispatcherServlet();
    }
}
