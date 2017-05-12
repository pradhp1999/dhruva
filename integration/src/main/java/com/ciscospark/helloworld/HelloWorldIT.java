package com.ciscospark.helloworld;

import com.cisco.wx2.client.ClientException;
import com.ciscospark.helloworld.api.Greeting;
import com.ciscospark.helloworld.client.HelloWorldClientFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfigIT.class)
public class HelloWorldIT extends AbstractJUnit4SpringContextTests {
    @Autowired
    private HelloWorldClientFactory helloWorldClientFactory;

    @Test
    public void testPing() {
        helloWorldClientFactory.newHelloWorldClient().ping();
    }

    @Test
    public void testGetGreeting() {
        // Testing that this works without authentication
        helloWorldClientFactory.newHelloWorldClient().getGreeting("homer");
    }

    @Test
    public void testPostGreeting() {
        // Testing that this returns a 401 as the helloWorldClientFactory does not have a token provider configured.
        try {
            helloWorldClientFactory.newHelloWorldClient().setGreeting("homer", Greeting.builder().greeting("hi").build());
            fail("POST /greetings should have returned 401.");
        } catch (ClientException e) {
            assertEquals("Expected 401 status code.",401, e.getStatusCode());
        }
    }
}
