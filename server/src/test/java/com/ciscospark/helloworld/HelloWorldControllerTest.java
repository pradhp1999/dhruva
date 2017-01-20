package com.ciscospark.helloworld;

import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.util.ObjectMappers;
import com.ciscospark.helloworld.api.Greeting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Read up on testing with Spring and Spring Boot.
 *   http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
 */
@RunWith(SpringRunner.class)
@WebMvcTest(value = HelloWorldController.class)
@ContextConfiguration(classes = {HelloWordlTestConfig.class} )
public class HelloWorldControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    private GreetingStore greetingStore;

    private final Greeting greeting = Greeting.builder().greeting("Hello spark").message("A special message for you.").build();

    @Test
    public void testGetGreeting() throws Exception {
        given(greetingStore.getGreeting("spark")).willReturn(greeting);

        mvc.perform(get("/api/v1/greetings/spark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greeting").value("Hello spark"))
                .andExpect(jsonPath("$.message").value("A special message for you."));
    }

    @Test
    public void testPostGreeting() throws Exception {
        given(greetingStore.setGreeting("spark", "Hello spark")).willReturn(greeting);

        mvc.perform(post("/api/v1/greetings/spark").contentType(MediaType.APPLICATION_JSON).content(ObjectMappers.toJson(greeting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greeting").value("Hello spark"))
                .andExpect(jsonPath("$.message").value("A special message for you."));
    }

    @Test
    public void testDeleteGreeting() throws Exception {
        mvc.perform(delete("/api/v1/greetings/spark"))
                .andExpect(status().is2xxSuccessful());

        verify(greetingStore).deleteGreeting(Matchers.eq("spark"));
    }

    @Test
    public void testDeleteNonExistentGreetingReturnsNotFound() throws Exception {
        Mockito.doThrow(ServerException.notFound("No such greeting")).when(greetingStore).deleteGreeting(Matchers.any());

        mvc.perform(delete("/api/v1/greetings/spark"))
                .andExpect(status().isNotFound());

       // verify(greetingStore).deleteGreeting(Matchers.eq("spark"));
    }



}
