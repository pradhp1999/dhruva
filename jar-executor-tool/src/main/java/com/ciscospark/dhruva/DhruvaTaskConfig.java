package com.ciscospark.dhruva;

import com.ciscospark.jarexecutor.config.ApplicationTaskConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class DhruvaTaskConfig {

  @Autowired @Lazy ApplicationTaskConfig applicationTaskConfig;

  public DhruvaTaskConfig() {
    System.out.println("***** hey i got created******");
  }

  @Bean
  @Lazy
  public EchoTask echoTask() {
    return new EchoTask();
  }
}
