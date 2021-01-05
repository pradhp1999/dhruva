package com.ciscospark.dhruva;

import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

public class DhruvaTestProperties {
  private static final String DEFAULT_TEST_LISTEN_ADDRESS = "localhost";
  private static final Integer DEFAULT_TEST_SERVER_PORT_UDP = 5061;
  public static final Integer DEFAULT_TEST_SERVER_PORT_TLS = 5062;

  private static final String DEFAULT_DHRUVA_HOST = "localhost";
  private static final Integer DEFAULT_DHRUVA_SIP_PORT_UDP = 5070;
  private static final Integer DEFAULT_DHRUVA_SIP_PORT_TLS = 5071;

  private static Environment env = new StandardEnvironment();

  public String getTestAddress() {
    return env.getProperty("testHost", DEFAULT_TEST_LISTEN_ADDRESS);
  }

  public int getTestUdpPort() {
    return env.getProperty("testUdpPort", Integer.class, DEFAULT_TEST_SERVER_PORT_UDP);
  }

  public int getTestTlsPort() {
    return env.getProperty("testTlsPort", Integer.class, DEFAULT_TEST_SERVER_PORT_TLS);
  }

  public String getDhruvaHost() {
    return env.getProperty("dhruvaHost", DEFAULT_DHRUVA_HOST);
  }

  public int getDhruvaUdpPort() {
    return env.getProperty("dhruvaSipUdpPort", Integer.class, DEFAULT_DHRUVA_SIP_PORT_UDP);
  }

  public int getDhruvaTlsPort() {
    return env.getProperty("dhruvaSipTlsPort", Integer.class, DEFAULT_DHRUVA_SIP_PORT_TLS);
  }
}
