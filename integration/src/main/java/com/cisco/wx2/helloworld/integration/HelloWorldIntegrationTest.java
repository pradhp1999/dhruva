package com.cisco.wx2.helloworld.integration;

import static org.testng.Assert.assertNotNull;

import org.testng.annotations.Test;

import com.cisco.wx2.helloworld.common.HelloResponse;
import com.cisco.wx2.util.EmailAddress;
import com.cisco.wx2.dto.BuildInfo;
import com.cisco.wx2.dto.health.ServiceHealth;

@Test
public class HelloWorldIntegrationTest extends IntegrationTest {

    public void testPing() {
        newHelloWorldClient().ping();
    }

    public void testServiceHealth() {
        ServiceHealth health = newHelloWorldClient().getServiceHealth();
        assertNotNull(health);
    }

    public void testBuildInfo() {
        BuildInfo buildInfo = newHelloWorldClient(testUser).getBuildInfo();
        assertNotNull(buildInfo);
    }

    public void testHello() {
        HelloResponse res = newHelloWorldClient(testUser).hello(testUser.getEmail());
        System.out.println(res.toString());
        System.out.println("response is : " + res.getGreeting());
    }

    public void testUnAuthHello() {
        HelloResponse res = newHelloWorldClient(testUser).hello(EmailAddress.fromString("unauthorized@mycorp.com"));
        System.out.println(res.toString());
        System.out.println("response is : " + res.getGreeting());
    }

    public void testAuthorization(){
        assertNotNull(testUser.getAuthorization(),"testUser.getAuthoriation");
        System.out.println("testUser.getAuthoriation: "+testUser.getAuthorization());
    }

}
