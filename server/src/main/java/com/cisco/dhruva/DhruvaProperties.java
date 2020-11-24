package com.cisco.dhruva;

import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.wx2.dto.BuildInfo;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Properties that are specific to the dhruva service.
 *
 * <p>See also:
 * http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
 */
@ConfigurationProperties(prefix = "dhruva")
@Component
public class DhruvaProperties {

  private static final String DEFAULT_DHRUVA_USER_AGENT = "WX2_DHRUVA";

  private static BuildInfo buildInfo;
  private final Environment env;

  @Autowired private DhruvaSIPConfigProperties sipConfigProperties;

  @Autowired
  public DhruvaProperties(Environment env) {
    // super(env, createUserAgentString(DEFAULT_DHRUVA_USER_AGENT, env));
    this.env = env;
  }

  public String getL2SIPClusterAddress() {
    return env.getProperty("l2sipClusterAddress", String.class, "l2sip.l2sip");
  }

  public static String createUserAgentString(String uaType, Environment env) {
    String userAgent = uaType;

    // Also, set the static buildInfo instance
    buildInfo = BuildInfo.fromEnv(env);

    if (!Strings.isNullOrEmpty(buildInfo.getBuildId())) {
      userAgent += "/" + buildInfo.getBuildId();
    }
    userAgent += " (" + env.getProperty("environment", "local") + ")";

    return userAgent;
  }
}
