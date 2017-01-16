package com.ciscospark.helloworld;

import com.ciscospark.helloworld.api.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RefreshScope
public class GreetingStore {
    private final String defaultGreetingPrefix;
    private final String message;

    private final Map<String, String> store = new HashMap<>();

    @Autowired
    public GreetingStore(HelloWorldProperties properties) {
        this.defaultGreetingPrefix = properties.getDefaultGreetingPrefix();
        this.message = properties.message;
    }

    public Greeting getGreeting(String name) {
        String greeting = store.get(name);
        if (greeting == null) {
            greeting = defaultGreetingPrefix + " " + name;
        }

        return Greeting.builder().greeting(greeting).message(message).build();
    }

    public Greeting setGreeting(String name, String greeting) {
        store.put(name, greeting);

        return Greeting.builder().greeting(greeting).message(message).build();
    }

    public Greeting deleteGreeting(String name) {
        String greeting = store.remove(name);

        Greeting result = null;

        if(greeting != null)
        {
            result =  Greeting.builder().greeting(greeting).message(message).build();
        }

        return result;
    }
}
