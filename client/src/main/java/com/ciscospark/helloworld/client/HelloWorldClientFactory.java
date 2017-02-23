package com.ciscospark.helloworld.client;

import com.cisco.wx2.client.ClientFactory;

import java.net.URI;

public class HelloWorldClientFactory extends ClientFactory {

    private HelloWorldClientFactory(Builder builder) {
        super(builder);
    }

    public HelloWorldClient newHelloWorldClient() {
        return new HelloWorldClient(this, baseUrl);
    }

    public static Builder builder(ClientFactory.Properties props, URI baseUrl) {
        return new Builder(props, baseUrl);
    }

    public static class Builder extends ClientFactory.Builder<Builder> {


        public Builder(ClientFactory.Properties props, URI baseUrl) {
            super(props, baseUrl);
        }

        public HelloWorldClientFactory build() {
            return new HelloWorldClientFactory(this);
        }

    }
}
