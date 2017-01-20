package com.ciscospark.helloworld;

import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.server.auth.AuthorizationNone;
import com.ciscospark.helloworld.api.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.versly.rest.wsdoc.AuthorizationScope;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

/**
 * The Hello World microservice.
 */
@RestController
@RequestMapping("${cisco-spark.server.api-path:/api}/v1")
public class HelloWorldController {
    private final GreetingStore greetingStore;

    @Autowired
    public HelloWorldController(GreetingStore greetingStore) {
        this.greetingStore = greetingStore;
    }

    @AuthorizationNone
    @RequestMapping(value = "/greetings/{name}", method = GET)
    public Greeting getGreeting(@PathVariable("name") String name) {
        return greetingStore.getGreeting(name);
    }

    @AuthorizationScope("*any*")
    @RequestMapping(value = "/greetings/{name}", method = POST)
    public Greeting postGreeting(@PathVariable("name") String name, @RequestBody Greeting greeting) {
        return greetingStore.setGreeting(name, greeting.getGreeting());
    }

    @AuthorizationScope("*any*")
    @RequestMapping(value = "/greetings/{name}", method = DELETE)
    public void deleteGreeting(@PathVariable("name") String name) {

        greetingStore.deleteGreeting(name);
    }
}
