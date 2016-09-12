package com.ciscospark.helloworld;

import com.ciscospark.helloworld.api.Greeting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "cisco-spark.server.importLegacyServerConfig=false",
                "hello-world.defaultGreetingPrefix=Doh!",
                "hello-world.message=" + GreetingStoreTest.message
        })
public class GreetingStoreTest {
    static final String message = "To alcohol! The cause of, and solution to, all of life's problems.";

    @Autowired
    GreetingStore greetingStore;

    @Test
    public void testGetDefault() throws Exception {
        Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message).build();
        assertThat(greetingStore.getGreeting("Homer Simpson"))
                .isEqualTo(expected);
    }

    @Test
    public void testSetAndGet() throws Exception {
        Greeting expected = Greeting.builder().greeting("hi").message(message).build();
        assertThat(greetingStore.setGreeting("me", "hi"))
                .isEqualTo(expected);

        assertThat(greetingStore.getGreeting("me"))
                .isEqualTo(expected);
    }
}
