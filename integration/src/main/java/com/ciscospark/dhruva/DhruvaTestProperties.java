package com.ciscospark.dhruva;

import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

public class DhruvaTestProperties {
  private static final String DEFAULT_TEST_LISTEN_ADDRESS = "localhost";
  public static final Integer DEFAULT_TEST_SERVER_PORT_UDP = 5070;

  public static final String DEFAULT_DHRUVA_HOST = "localhost";
  public static final Integer DEFAULT_DHRUVA_SIP_PORT_UDP = 5060;
  private static Environment env = new StandardEnvironment();

  public String getTestAddress() {
    return env.getProperty("testHost", DEFAULT_TEST_LISTEN_ADDRESS);
  }

  public int getTestUdpPort() {
    return env.getProperty("testUdpPort", Integer.class, DEFAULT_TEST_SERVER_PORT_UDP);
  }

  public String getDhruvaHost() {
    return env.getProperty("dhruvaHost", DEFAULT_DHRUVA_HOST);
  }

  public int getDhruvaUdpPort() {
    return env.getProperty("dhruvaSipPort", Integer.class, DEFAULT_DHRUVA_SIP_PORT_UDP);
  }
}
