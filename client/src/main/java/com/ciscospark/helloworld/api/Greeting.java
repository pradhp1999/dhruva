package com.ciscospark.helloworld.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = Greeting.GreetingBuilder.class)
@Builder
@Value
public class Greeting {
    private final String greeting;
    private final String message;

    @JsonPOJOBuilder
    public static final class GreetingBuilder {
    }
}
