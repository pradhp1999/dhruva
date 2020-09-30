package com.ciscospark.dhruva;

import com.ciscospark.dhruva.client.DhruvaClientFactory;
import com.ciscospark.dhruva.util.DhruvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Listeners;

@Listeners({IntegrationTestListener.class})
@ComponentScan(basePackages = "com.ciscospark.dhruva")
@ContextConfiguration(classes = {TestConfiguration.class, DhruvaConfig.class})
public class DhruvaIT extends AbstractTestNGSpringContextTests {

  @Autowired protected DhruvaClientFactory helloWorldClientFactory;

  @Autowired protected SipStackService sipStackService;

  public DhruvaClientFactory getHelloWorldClientFactory() {
    return helloWorldClientFactory;
  }

  public SipStackService getSipStackService() {
    return sipStackService;
  }
}
