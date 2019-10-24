package com.ciscospark.dhruva.client;

import com.cisco.wx2.client.ClientFactory;

import java.net.URI;

public class DhruvaClientFactory extends ClientFactory {

    private DhruvaClientFactory(Builder builder) {
        super(builder);
    }

    public DhruvaClient newDhruvaClient() {
        return new DhruvaClient(this, baseUrl);
    }

    public static Builder builder(ClientFactory.Properties props, URI baseUrl) {
        return new Builder(props, baseUrl);
    }

    public static class Builder extends ClientFactory.Builder<Builder> {


        public Builder(ClientFactory.Properties props, URI baseUrl) {
            super(props, baseUrl);
        }

        public DhruvaClientFactory build() {
            return new DhruvaClientFactory(this);
        }

    }
}
