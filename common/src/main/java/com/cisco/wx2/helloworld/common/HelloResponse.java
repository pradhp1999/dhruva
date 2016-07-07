package com.cisco.wx2.helloworld.common;

import static com.google.common.base.Preconditions.checkNotNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HelloResponse {
    private final String greeting;

    @JsonCreator
    public HelloResponse(@JsonProperty("greeting") String greeting) {
        this.greeting = checkNotNull(greeting, "greeting");
    }

    public String getGreeting() {
        return greeting;
    }
    
    @Override
    public String toString() {
        return "HelloResponse{" + "greeting=" + greeting + '}';
    }
}