package com.cisco.wx2.helloworld.client;

import com.cisco.wx2.client.Client;
import com.cisco.wx2.dto.BuildInfo;
import com.cisco.wx2.dto.health.ServiceHealth;
import com.cisco.wx2.helloworld.common.HelloResponse;
import com.cisco.wx2.util.EmailAddress;

import java.net.URI;

public class HelloWorldClient extends Client {

    protected HelloWorldClient(HelloWorldClientFactory factory, URI baseUrl) {
        super(factory, baseUrl);
    }

    public ServiceHealth ping() {
       return get("ping").execute(ServiceHealth.class);
    }

    public ServiceHealth getServiceHealth() {
        return get("service_health").execute(ServiceHealth.class);
    }

    public BuildInfo getBuildInfo() {
        return get("build_info").execute(BuildInfo.class);
    }

    public HelloResponse hello(EmailAddress email) {
        try {
            return get(email.toString()).execute(HelloResponse.class);
        } catch (Exception e) {
            return new HelloResponse("403 Forbidden response");
        }
    }
}
