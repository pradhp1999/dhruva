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
  /** The default greeting prefix if no specific greeting is found. */
  private String defaultGreetingPrefix = "Hello";

  /** Message to be delivered with the greeting. */
  private String message = "Beer is proof God loves us and wants us to be happy";

  /** Something to optionally append to the greeting. */
  private String trailer = "Message lovingly crafted by";

  @Autowired private DhruvaSIPConfigProperties sipConfigProperties;

  public DhruvaProperties(Environment env) {
    // super(env, createUserAgentString(DEFAULT_DHRUVA_USER_AGENT, env));
  }

  public String getDefaultGreetingPrefix() {
    return this.defaultGreetingPrefix;
  }

  public String getMessage() {
    return this.message;
  }

  public String getTrailer() {
    return this.trailer;
  }

  public void setDefaultGreetingPrefix(String defaultGreetingPrefix) {
    this.defaultGreetingPrefix = defaultGreetingPrefix;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setTrailer(String trailer) {
    this.trailer = trailer;
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
