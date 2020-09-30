package com.ciscospark.dhruva;

import java.util.Properties;
import javax.annotation.PostConstruct;
import org.cafesip.sipunit.SipStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SipStackService {
  private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTestListener.class);
  private static SipStack sipStack;

  public SipStackService() {}

  @PostConstruct
  public void init() {
    Properties properties = new Properties();
    properties.setProperty("javax.sip.STACK_NAME", "TestDhruva");
    properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
    try {
      sipStack = createSipStack("UDP", 5070, properties);
    } catch (Exception e) {
      LOGGER.error("Exception while creating  sip stack ");
      e.printStackTrace();
    }
  }

  SipStack createSipStack(String protocol, int port, Properties properties) throws Exception {
    SipStack sipStack = new SipStack(protocol, port, properties);
    return sipStack;
  }

  public SipStack getSipStack() {
    return sipStack;
  }
}
