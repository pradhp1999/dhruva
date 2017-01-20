package com.ciscospark.helloworld;

import com.cisco.wx2.server.ServerException;
import com.ciscospark.helloworld.api.Greeting;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.util.ApplicationContextTestUtils;
import org.springframework.test.context.ContextConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {HelloWordlTestConfig.class})
public class GreetingStoreTest extends ApplicationContextTestUtils {

    HelloWorldProperties properties;
    GreetingStore greetingStore;

    @Before
    public void buildGreetingStore(){

        properties = new HelloWorldProperties("Doh!",
                "To alcohol! The cause of, and solution to, all of life's problems.");
        greetingStore = new GreetingStore(properties);
    }

    @Test
    public void testGetDefault() throws Exception {
        Greeting expected = Greeting.builder().greeting(properties.getDefaultGreetingPrefix()+" Homer Simpson").message(properties.getMessage()).build();
        assertThat(greetingStore.getGreeting("Homer Simpson")).isEqualTo(expected);
    }

    @Test
    public void testSetAndGet() throws Exception {
        Greeting expected = Greeting.builder().greeting("hi").message(properties.getMessage()).build();
        assertThat(greetingStore.setGreeting("me", "hi"))
                .isEqualTo(expected);

        assertThat(greetingStore.getGreeting("me"))
                .isEqualTo(expected);
    }

    @Test
    public void testSuccessfullySetAndDeleteTheSameObject() throws Exception {
        Greeting expected = Greeting.builder().greeting("hi").message(properties.getMessage()).build();
        assertThat(greetingStore.setGreeting("me", "hi"))
                .isEqualTo(expected);

        greetingStore.deleteGreeting("me");
    }

    @Test (expected = ServerException.class)
    public void testDeletingTwiceTheSameGreetingThrowsException() throws Exception {
        Greeting expected = Greeting.builder().greeting("hi").message(properties.getMessage()).build();
        assertThat(greetingStore.setGreeting("me", "hi"))
                .isEqualTo(expected);

        greetingStore.deleteGreeting("me");
        greetingStore.deleteGreeting("me");
    }
}

