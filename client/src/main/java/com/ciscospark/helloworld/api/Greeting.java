package com.ciscospark.helloworld.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Greeting.GreetingBuilder.class)
public class Greeting {
    private final String greeting;
    private final String message;

    @java.beans.ConstructorProperties({"greeting", "message"})
    Greeting(String greeting, String message) {
        this.greeting = greeting;
        this.message = message;
    }

    public static GreetingBuilder builder() {
        return new GreetingBuilder();
    }

    public String getGreeting() {
        return this.greeting;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Greeting)) return false;
        final Greeting other = (Greeting) o;
        final Object this$greeting = this.getGreeting();
        final Object other$greeting = other.getGreeting();
        if (this$greeting == null ? other$greeting != null : !this$greeting.equals(other$greeting)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $greeting = this.getGreeting();
        result = result * PRIME + ($greeting == null ? 43 : $greeting.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }

    public String toString() {
        return "Greeting(greeting=" + this.getGreeting() + ", message=" + this.getMessage() + ")";
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class GreetingBuilder {
        private String greeting;
        private String message;

        GreetingBuilder() {
        }

        public GreetingBuilder greeting(String greeting) {
            this.greeting = greeting;
            return this;
        }

        public GreetingBuilder message(String message) {
            this.message = message;
            return this;
        }

        public Greeting build() {
            return new Greeting(greeting, message);
        }

        public String toString() {
            return "Greeting.GreetingBuilder(greeting=" + this.greeting + ", message=" + this.message + ")";
        }
    }
}
