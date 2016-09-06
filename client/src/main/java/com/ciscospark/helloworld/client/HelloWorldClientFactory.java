package com.ciscospark.helloworld.client;

import com.cisco.wx2.client.ClientFactory;
import java.net.URI;

import static com.google.common.base.Preconditions.*;

public class HelloWorldClientFactory extends ClientFactory {
    private final URI apiServiceUrl;


    private HelloWorldClientFactory(Builder builder) {
        super(builder);
        this.apiServiceUrl = builder.apiServiceUrl;
    }

    public HelloWorldClient newHelloWorldClient() {
        return new HelloWorldClient(this, apiServiceUrl);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ClientFactory.Builder<Builder> {

        private URI apiServiceUrl;

        public HelloWorldClientFactory build() {
            checkState(apiServiceUrl != null, "API service URL required");
            return new HelloWorldClientFactory(this);
        }

        public Builder apiServiceUrl(URI apiServiceUrl) {
            this.apiServiceUrl = apiServiceUrl;
            return this;
        }
    }

}
