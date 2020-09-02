package com.ciscospark.dhruva;

import com.ciscospark.dhruva.client.DhruvaClientFactory;
import com.ciscospark.integration.Tag;
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
}
