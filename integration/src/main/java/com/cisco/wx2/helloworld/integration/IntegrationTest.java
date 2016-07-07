package com.cisco.wx2.helloworld.integration;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.cisco.wx2.helloworld.client.HelloWorldClient;
import com.cisco.wx2.helloworld.client.HelloWorldClientFactory;
import com.cisco.wx2.test.TestUser;
import com.cisco.wx2.test.TestUserManager;

@Test
@ContextConfiguration(classes = { TestConfig.class })
public abstract class IntegrationTest extends com.cisco.wx2.test.integration.IntegrationTest {
    @Autowired
    protected TestUser testUser;

    protected HelloWorldClient helloWorldClient;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected IntegrationTest() {
        super();
    }

    @PostConstruct
    private void init() {
        log.info("init: App context = " + applicationContext);
        helloWorldClient = newHelloWorldClient(testUser);
    }

    @Autowired
    private HelloWorldClientFactory helloWorldClientFactory;

    @Autowired
    private TestUserManager testUserManager;

    protected HelloWorldClient newHelloWorldClient() {
        return helloWorldClientFactory.newHelloWorldClient();
    }

    protected HelloWorldClient newHelloWorldClient(TestUser testUser) {
        HelloWorldClient client = newHelloWorldClient();
        client.setAuthorization(testUser.getAuthorization());
        return client;
    }

    protected TestUser createTestUser() {
        return testUserManager.createTestUser();
    }

    protected TestUser createTestUser(String name) {
        return testUserManager.createTestUser(name);
    }
}
