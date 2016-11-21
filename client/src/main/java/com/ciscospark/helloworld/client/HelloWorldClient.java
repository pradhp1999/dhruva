package com.ciscospark.helloworld.client;

import com.cisco.wx2.client.Client;
import com.cisco.wx2.client.health.ServiceHealthPinger;
import com.cisco.wx2.dto.health.ServiceHealth;

import java.net.URI;

public class HelloWorldClient extends Client implements ServiceHealthPinger {
    protected HelloWorldClient(HelloWorldClientFactory factory, URI baseUrl) {
        super(factory, baseUrl);
    }

    // TODO: Add client methods.

    public ServiceHealth ping() {
       return get("ping").execute(ServiceHealth.class);
    }
}
