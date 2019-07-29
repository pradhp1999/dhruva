package com.ciscospark.helloworld;

import com.ciscospark.helloworld.EchoTask;
import com.ciscospark.jarexecutor.config.ApplicationTaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class HelloWorldTaskConfig {

    @Autowired
    @Lazy
    ApplicationTaskConfig applicationTaskConfig;

    public HelloWorldTaskConfig() {
        System.out.println("***** hey i got created******");
    }

    @Bean
    @Lazy
    public EchoTask echoTask() {
        return new EchoTask();
    }

}