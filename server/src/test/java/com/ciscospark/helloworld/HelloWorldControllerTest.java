package com.ciscospark.helloworld;

import com.cisco.wx2.util.ObjectMappers;
import com.ciscospark.helloworld.api.Greeting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Read up on testing with Spring and Spring Boot.
 *   http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
 */
@RunWith(SpringRunner.class)
@WebMvcTest(HelloWorldController.class)
public class HelloWorldControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    private GreetingStore greetingStore;

    private final Greeting greeting = Greeting.builder().greeting("Hello spark").message("A special message for you.").build();

    @Test
    public void testGetGreeting() throws Exception {
        given(greetingStore.getGreeting("spark")).willReturn(greeting);

        mvc.perform(get("/hello-world/api/v1/greeting/spark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greeting").value("Hello spark"))
                .andExpect(jsonPath("$.message").value("A special message for you."));
    }

    @Test
    public void testPostGreeting() throws Exception {
        given(greetingStore.setGreeting("spark", "Hello spark")).willReturn(greeting);

        mvc.perform(post("/hello-world/api/v1/greeting/spark").contentType(MediaType.APPLICATION_JSON).content(ObjectMappers.toJson(greeting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greeting").value("Hello spark"))
                .andExpect(jsonPath("$.message").value("A special message for you."));
    }
}