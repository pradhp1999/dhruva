package com.ciscospark.dhruva;

import java.util.Properties;
import javax.annotation.PostConstruct;
import org.cafesip.sipunit.SipStack;
import org.springframework.stereotype.Component;

@Component
public class SipStackService {

  private static SipStack sipStack;

  public SipStackService() {
  }

  @PostConstruct
  public void init() {
    Properties properties = new Properties();
    properties.setProperty("javax.sip.STACK_NAME", "TestDhruva");
    properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
    try {
      sipStack = createSipStack("UDP", 5070, properties);
    } catch (Exception e) {
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
