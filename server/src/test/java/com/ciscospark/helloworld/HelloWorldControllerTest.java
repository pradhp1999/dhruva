package com.ciscospark.helloworld;

import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.server.config.ConfigProperties;
import com.cisco.wx2.server.spring.ExceptionResolver;
import com.cisco.wx2.util.ObjectMappers;
import com.cisco.wx2.wdm.client.FeatureClient;
import com.cisco.wx2.wdm.client.FeatureClientFactory;
import com.ciscospark.helloworld.api.Greeting;
import com.ciscospark.server.CiscoSparkServerProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Read up on testing with Spring and Spring Boot.
 * http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
 */
@RunWith(SpringRunner.class)
@WebMvcTest(HelloWorldController.class)
public class HelloWorldControllerTest {

    @TestConfiguration
    static class TestConfig extends WebMvcConfigurerAdapter {
        @Override
        public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
            exceptionResolvers.add(new ExceptionResolver());
        }

        /* Even though they are not referenced or used in this test, the MockBean instances are needed in order to satisfy
         * HelloWorldApplication's @Autowired beans, because the use of @TestConfiguration augments, as opposed to replaces
         * the HelloWorldApplication context.
         */
        @MockBean
        private ConfigProperties configProperties;

        @MockBean
        private CiscoSparkServerProperties serverProperties;

        @MockBean
        private FeatureClient featureClient;

        @MockBean
        private FeatureClientFactory featureClientFactory;
    }


    @Autowired
    MockMvc mvc;

    @MockBean
    private GreetingService greetingService;

    private final Greeting greeting = Greeting.builder().greeting("Hello spark").message("A special message for you.").build();

    @Test
    public void testGetGreeting() throws Exception {
        given(greetingService.getGreeting(eq("spark"))).willReturn(greeting);

        mvc.perform(get("/api/v1/greetings/spark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greeting").value("Hello spark"))
                .andExpect(jsonPath("$.message").value("A special message for you."));
    }

    @Test
    public void testPostGreeting() throws Exception {
        given(greetingService.setGreeting(eq("spark"), eq("Hello spark"), any())).willReturn(greeting);

        mvc.perform(post("/api/v1/greetings/spark").contentType(MediaType.APPLICATION_JSON).content(ObjectMappers.toJson(greeting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greeting").value("Hello spark"))
                .andExpect(jsonPath("$.message").value("A special message for you."));
    }

    @Test
    public void testDeleteGreeting() throws Exception {
        mvc.perform(delete("/api/v1/greetings/spark"))
                .andExpect(status().isOk());

        verify(greetingService).deleteGreeting(eq("spark"));
    }

    @Test
    public void testDeleteNonExistentGreetingReturnsNotFound() throws Exception {
        Mockito.doThrow(ServerException.notFound("Greeting not found!")).when(greetingService).deleteGreeting(any());

        mvc.perform(delete("/api/v1/greetings/spark"))
                .andExpect(status().isNotFound());

        verify(greetingService).deleteGreeting(eq("spark"));
    }
}
