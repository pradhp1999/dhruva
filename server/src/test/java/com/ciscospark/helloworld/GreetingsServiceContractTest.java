package com.ciscospark.helloworld;


import com.jayway.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@AutoConfigureStubRunner(workOffline = true, ids = "com.cisco.wx2:feature-server:+:stubs:8090")
public class GreetingsServiceContractTest extends AbstractJUnit4SpringContextTests
{
    @BeforeClass
    public static void setup() {
        RestAssured.port = Integer.valueOf(8090);
//        com.jayway.restassured.RestAssured.basePath = "/api/v1/";
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void testPingFeatureService() {
//        given().
//                when().
//                get("/features/ping").
//                then().
//                statusCode(200).
//                body("serviceName", equalTo("feature"));
    }

}
