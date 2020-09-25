package com.ciscospark.dhruva;

import com.ciscospark.dhruva.client.DhruvaClientFactory;
import com.ciscospark.dhruva.util.DhruvaConfig;
import com.ciscospark.integration.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ComponentScan(basePackages = "com.ciscospark.dhruva")
@ContextConfiguration(classes = {TestConfiguration.class, DhruvaConfig.class})
public class DhruvaIT extends AbstractTestNGSpringContextTests {
  @Autowired private DhruvaClientFactory helloWorldClientFactory;

  public SipStackService getSipStackService() {
    return sipStackService;
  }

  @Autowired protected SipStackService sipStackService;

  @Test
  @Tag("TAP")
  public void testPing() {
    helloWorldClientFactory.newDhruvaClient().ping();
  }
}
