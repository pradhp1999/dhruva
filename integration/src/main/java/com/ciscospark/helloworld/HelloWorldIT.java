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

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestConfig.class)
public class HelloWorldIT extends AbstractJUnit4SpringContextTests {
    @Autowired
    HelloWorldClientFactory helloWorldClientFactory;

    @Test
    public void testPing() {
        helloWorldClientFactory.newHelloWorldClient().ping();
    }

    @Test
    public void testGetGreeting() {
        helloWorldClientFactory.newHelloWorldClient().getGreeting("homer");
    }

    @Test(expected = ClientException.class)
    public void testPostGreeting() {
        helloWorldClientFactory.newHelloWorldClient().setGreeting("homer", Greeting.builder().greeting("hi").build());
    }
}
