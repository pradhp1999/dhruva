package com.ciscospark.helloworld;

import com.cisco.wx2.dto.User;
import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.server.auth.AuthInfo;
import com.cisco.wx2.server.config.ConfigProperties;
import com.cisco.wx2.wdm.client.FeatureClient;
import com.cisco.wx2.wdm.client.FeatureClientFactory;
import com.ciscospark.helloworld.api.Greeting;
import com.ciscospark.server.CiscoSparkServerProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Note the use of @MockBean here. What it does in a testing context is that it allows you to run without a web
 * context, but still use the beans that are defined in your SpringApplication configuration, *without* requiring
 * a web environment. In other words, it keeps your tests small, efficient and fast. You need to have a mock bean
 * for every @Autowire that exists in your SpringApplication configuration, and a @MockBean for every @Autowire
 * or @Autowire constructor parameter for a @Component (or @Resource, or @Service or spring stereotype).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "cisco-spark.server.importLegacyServerConfig=false",
                "hello-world.defaultGreetingPrefix=Doh!",
                "hello-world.message=" + GreetingServiceTest.message,
                "hello-world.trailer=" + GreetingServiceTest.trailer,
                "featureServicePublicUrl=" + "http://localhost:8090/"
        })
@AutoConfigureStubRunner(workOffline = true, ids = "com.cisco.wx2:feature-server:+:stubs:8090")
public class GreetingServiceTest {

    static final String message = "To alcohol! The cause of, and solution to, all of life's problems.";
    static final String trailer = " Proudly created by: ";
    private static final String JOE_RANDOM_TEST_USER = "Joe Random TestUser";
    private static final String JOE_RANDOM_TEST_UUID = "b3474dff-8de5-4b12-8e2b-dfeee1cd11b2";

    private static final String BOB_RANDOM_UNAUTHORIZED_USER = "BOB Random TestUser";
    private static final String BOB_RANDOM_UNAUTHORIZED_TEST_UUID = "2fdf0844-d1aa-4d4b-8699-8043b97ddac5";


    /* Since we do not have a real application context, server properties are a dummy, so pull this in separately */
    @Value("${spring.application.name:application}")
    private String name;

    @MockBean
    private ConfigProperties configProperties;

    @MockBean
    private CiscoSparkServerProperties serverProperties;

    @MockBean
    private FeatureClient featureClient;

    @Autowired
    private FeatureClientFactory featureClientFactory;

    @MockBean
    private HttpServletRequest servletRequest;

    @Autowired
    private HelloWorldProperties properties;

    @Mock
    private AuthInfo authInfo;

    @MockBean
    private JedisPool jedisPool;

    @MockBean
    private JedisPoolConfig jedisPoolConfig;

    private GreetingService greetingService;

    @Before
    public void init() {

        greetingService = new GreetingService(properties, featureClientFactory, new HashMap<>(), serverProperties);
        when(serverProperties.getName()).thenReturn(name);

        User user = Mockito.mock(User.class);
        when(user.getName()).thenReturn(JOE_RANDOM_TEST_USER);
        when(user.getId()).thenReturn(UUID.fromString(JOE_RANDOM_TEST_UUID));

        when(authInfo.getEffectiveUser()).thenReturn(user);
        when(authInfo.getAuthorization()).thenReturn("Basic dummy authorization string");
    }

    /* Default GET that is done without a login */
    @Test
    public void testGetDefault() throws Exception {
        Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message).build();
        assertThat(greetingService.getGreeting("Homer Simpson", Optional.empty()))
                .isEqualTo(expected);
    }

    /* GET that is done with a login, and with the adduserresponse feature toggle set */
    @Test
    public void testGetDefaultWithTrailerTrueToggleFromAuthorizedUser() throws Exception {
        when(servletRequest.getAttribute("AuthInfo")).thenReturn(authInfo);

        Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message + trailer + JOE_RANDOM_TEST_USER).build();
        assertThat(greetingService.getGreeting("Homer Simpson", Optional.of(authInfo)))
                .isEqualTo(expected);
    }

    /* GET that is done with a login and with the  */
    @Test
    public void testGetDefaultWithTrailerFalseToggleFromUnAuthorizedUser() throws Exception {

        User user = Mockito.mock(User.class);
        when(user.getName()).thenReturn(BOB_RANDOM_UNAUTHORIZED_USER);
        when(user.getId()).thenReturn(UUID.fromString(BOB_RANDOM_UNAUTHORIZED_TEST_UUID));

        when(authInfo.getEffectiveUser()).thenReturn(user);

        Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message).build();
        assertThat(greetingService.getGreeting("Homer Simpson", Optional.of(authInfo)))
                .isEqualTo(expected);
    }

    /* GET that is done with a login, and with the adduserresponse feature not present */
    @Test
    public void testGetDefaultWithTrailerNoToggle() throws Exception {
        Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message).build();
        assertThat(greetingService.getGreeting("Homer Simpson", Optional.empty()))
                .isEqualTo(expected);
    }

    @Test
    public void testSetAndGet() throws Exception {

        Greeting expected = Greeting.builder().greeting("hi").message(message).build();
        assertThat(greetingService.setGreeting("me", "hi"))
                .isEqualTo(expected);

        assertThat(greetingService.getGreeting("me", Optional.empty()))
                .isEqualTo(expected);
    }

    @Test
    public void testDelete() throws Exception {
        Greeting expected = Greeting.builder().greeting("hi").message(message).build();
        assertThat(greetingService.setGreeting("me", "hi"))
                .isEqualTo(expected);

        greetingService.deleteGreeting("me");

        // Verify deleting again throws exception
        assertThatThrownBy(() -> greetingService.deleteGreeting("me"))
                .isInstanceOf(ServerException.class)
                .hasMessageContaining("not found");

        // Verify deleting a non-existent greeting throws not found exception
        assertThatThrownBy(() -> greetingService.deleteGreeting("non-existent"))
                .isInstanceOf(ServerException.class)
                .hasMessageContaining("not found");

    }
}
