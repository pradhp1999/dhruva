package com.cisco.dhruva;

import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

  /** The default greeting prefix if no specific greeting is found. */
  private String defaultGreetingPrefix = "Hello";

  /** Message to be delivered with the greeting. */
  private String message = "Beer is proof God loves us and wants us to be happy";

  /** Something to optionally append to the greeting. */
  private String trailer = "Message lovingly crafted by";

  @Autowired private DhruvaSIPConfigProperties sipConfigProperties;

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
}
