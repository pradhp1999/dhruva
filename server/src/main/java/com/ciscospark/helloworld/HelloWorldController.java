package com.ciscospark.helloworld;

import com.ciscospark.helloworld.api.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.versly.rest.wsdoc.AuthorizationScope;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * The Hello World microservice.
 */
@RestController
@RequestMapping("/hello-world/api/v1")
public class HelloWorldController {
    private final GreetingStore greetingStore;

    @Autowired
    public HelloWorldController(GreetingStore greetingStore) {
        this.greetingStore = greetingStore;
    }

    @AuthorizationScope("*any*")
    @RequestMapping(value = "/greetings/{name}", method = GET)
    public Greeting getGreeting(@PathVariable("name") String name) {
        return greetingStore.getGreeting(name);
    }

    @AuthorizationScope("*any*")
    @RequestMapping(value = "/greetings/{name}", method = POST)
    public Greeting postGreeting(@PathVariable("name") String name, @RequestBody Greeting greeting) {
        return greetingStore.setGreeting(name, greeting.getGreeting());
    }
}
