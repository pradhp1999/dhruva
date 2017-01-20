package com.ciscospark.helloworld;

import com.cisco.wx2.server.spring.ExceptionResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import java.util.List;

@Configuration
@Import({HelloWorldController.class})
public class HelloWordlTestConfig extends WebMvcConfigurationSupport
{

    public HelloWordlTestConfig() {
//        MockitoAnnotations.initMocks(this);
    }

//    @Bean
//    public GreetingStore greetingStore() {
//        return new GreetingStore(new HelloWorldProperties());
//    }

    @Override
    protected void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        super.configureHandlerExceptionResolvers(exceptionResolvers);
        exceptionResolvers.add(0, new ExceptionResolver());
    }
}
