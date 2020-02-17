package com.cisco.dhruva;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import com.cisco.wx2.dto.User;
import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.server.auth.AuthInfo;
import com.ciscospark.dhruva.api.Greeting;
import com.ciscospark.server.CiscoSparkServerProperties;
import java.util.Optional;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Note the use of @MockBean here. What it does in a testing context is that it allows you to run
 * without a web context, but still use the beans that are defined in your SpringApplication
 * configuration, *without* requiring a web environment. In other words, it keeps your tests small,
 * efficient and fast. You need to have a mock bean for every @Autowire that exists in your
 * SpringApplication configuration, and a @MockBean for every @Autowire or @Autowire constructor
 * parameter for a @Component (or @Resource, or @Service or spring stereotype).
 */
@RunWith(SpringRunner.class)
@Ignore
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = TestConfig.class,
    properties = {
      "cisco-spark.server.importLegacyServerConfig=false",
      "dhruva.defaultGreetingPrefix=Doh!",
      "dhruva.message=" + GreetingServiceTest.message,
      "dhruva.trailer=" + GreetingServiceTest.trailer
    })
public class GreetingServiceTest {

  static final String message =
      "To alcohol! The cause of, and solution to, all of life's problems.";
  static final String trailer = " Proudly created by: ";
  private static final String JOE_RANDOM_TEST_USER = "Joe Random TestUser";

  /* Since we do not have a real application context, server properties are a dummy, so pull this in separately */
  @Value("${spring.application.name:application}")
  private String name;

  @MockBean private CiscoSparkServerProperties serverProperties;

  @MockBean private HttpServletRequest servletRequest;

  @Mock private AuthInfo authInfo;

  @Autowired private GreetingService greetingService;

  @Before
  public void init() {
    when(serverProperties.getName()).thenReturn(name);

    String n = serverProperties.getName() + "-adduserresponse";
    User user = Mockito.mock(User.class);
    when(user.getName()).thenReturn(JOE_RANDOM_TEST_USER);
    when(user.getId()).thenReturn(UUID.randomUUID());

    when(authInfo.getEffectiveUser()).thenReturn(user);
    when(authInfo.getAuthorization()).thenReturn("Basic dummy authorization string");
  }

  /* Default GET that is done without a login */
  @Test
  public void testGetDefault() throws Exception {
    Greeting expected = Greeting.builder().greeting("Doh! Homer Simpson").message(message).build();
    assertThat(greetingService.getGreeting("Homer Simpson", Optional.empty())).isEqualTo(expected);
  }

  @Test
  public void testSetAndGet() throws Exception {

    Greeting expected = Greeting.builder().greeting("hi").message(message).build();
    assertThat(greetingService.setGreeting("me", "hi", authInfo)).isEqualTo(expected);

    assertThat(greetingService.getGreeting("me", Optional.of(authInfo))).isEqualTo(expected);
  }

  @Test
  public void testDelete() throws Exception {
    Greeting expected = Greeting.builder().greeting("hi").message(message).build();
    assertThat(greetingService.setGreeting("me", "hi", authInfo)).isEqualTo(expected);

    greetingService.deleteGreeting("me", authInfo);

    // Verify deleting again throws exception
    assertThatThrownBy(() -> greetingService.deleteGreeting("me", authInfo))
        .isInstanceOf(ServerException.class)
        .hasMessageContaining("not found");

    // Verify deleting a non-existent greeting throws not found exception
    assertThatThrownBy(() -> greetingService.deleteGreeting("non-existent", authInfo))
        .isInstanceOf(ServerException.class)
        .hasMessageContaining("not found");
  }
}
