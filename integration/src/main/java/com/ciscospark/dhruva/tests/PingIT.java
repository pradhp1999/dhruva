package com.ciscospark.dhruva.tests;

import com.ciscospark.dhruva.DhruvaIT;
import com.ciscospark.integration.Tag;
import org.testng.annotations.Test;

public class PingIT extends DhruvaIT {

  @Test
  @Tag("TAP")
  public void testPing() {
    super.helloWorldClientFactory.newDhruvaClient().ping();
  }
}
