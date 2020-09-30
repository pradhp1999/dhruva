package com.ciscospark.dhruva.util;

import com.ciscospark.dhruva.SipStackService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DhruvaConfig {

  @Bean
  public SipStackService sipStackService() {
    return new SipStackService();
  }
}
