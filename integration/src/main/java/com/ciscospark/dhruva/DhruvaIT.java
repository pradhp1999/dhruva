package com.ciscospark.dhruva;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.cisco.wx2.client.ClientException;
import com.ciscospark.dhruva.api.Greeting;
import com.ciscospark.dhruva.client.DhruvaClientFactory;
import com.ciscospark.integration.Tag;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class DhruvaIT extends AbstractJUnit4SpringContextTests {
  @Autowired private DhruvaClientFactory helloWorldClientFactory;

  @Test
  @Tag("TAP")
  public void testPing() {
    helloWorldClientFactory.newDhruvaClient().ping();
  }

  @Test
  public void testGetGreeting() {
    // Testing that this works without authentication
    Greeting g = helloWorldClientFactory.newDhruvaClient().getGreeting("homer");
    // if message contains "fallback", it means there is some exception processing the request by
    // dhruva service
    // in the context of this particular test, we fail the test
    Assert.assertTrue(!g.getMessage().contains("(fallback)"));
  }

  @Test
  public void testPostGreeting() {
    // Testing that this returns a 401 as the helloWorldClientFactory does not have a token provider
    // configured.
    try {
      helloWorldClientFactory
          .newDhruvaClient()
          .setGreeting("homer", Greeting.builder().greeting("hi").build());
      fail("POST /greetings should have returned 401.");
    } catch (ClientException e) {
      assertEquals("Expected 401 status code.", 401, e.getStatusCode());
    }
  }
}
