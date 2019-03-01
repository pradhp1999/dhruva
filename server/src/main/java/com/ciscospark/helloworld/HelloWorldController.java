package com.ciscospark.helloworld;

import com.cisco.wx2.server.auth.AuthInfo;
import com.cisco.wx2.server.auth.AuthUtil;
import com.cisco.wx2.server.auth.ng.Auth.Org;
import com.cisco.wx2.server.auth.ng.Scope;
import com.cisco.wx2.server.auth.ng.annotation.AuthorizeAnonymous;
import com.cisco.wx2.server.auth.ng.annotation.AuthorizeWhen;
import com.ciscospark.helloworld.api.Greeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * The Hello World microservice.
 */
@RestController
@RequestMapping("${cisco-spark.server.api-path:/api}/v1")
public class HelloWorldController {
    private final GreetingService greetingService;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Environment env;

    @Autowired
    public HelloWorldController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    /*
     * It's pretty simple to get authorization and user information. Just include the servlet request parameter in the
     * method signature, and it will be automatically injected into the method call. Then use that to get the AuthInfo
     * attribute. Note that as of this writing, injection of AuthInfo parameter directly into the method signature
     * will cause a null pointer exception due to lack of default constructor for AuthInfo ...
     */
    @AuthorizeAnonymous
    @RequestMapping(value = "/greetings/{name}", method = GET)
    public Greeting getGreeting(@PathVariable("name") String name, HttpServletRequest request) {
        AuthInfo authInfo = AuthUtil.getAuthInfo(request);
        String buildNumber = env.getProperty("buildNumber");
        log.info("QQQ Properties found buildNumber={}", buildNumber);

        return greetingService.getGreeting(name, Optional.ofNullable(authInfo));
    }

    @AuthorizeWhen(scopes = Scope.ANY, targetOrgId = Org.NONE)
    @RequestMapping(value = "/greetings/{name}", method = POST)
    public Greeting postGreeting(@PathVariable("name") String name, @RequestBody Greeting greeting, HttpServletRequest request) {
        AuthInfo authInfo = AuthUtil.getAuthInfo(request);
        log.debug("Greeting is going to be set by '{}'", authInfo != null ? authInfo.getEffectiveUser().getName() : "nobody");
        return greetingService.setGreeting(name, greeting.getGreeting(), authInfo);
    }

    @AuthorizeWhen(scopes = Scope.ANY, targetOrgId = Org.NONE)
    @RequestMapping(value = "/greetings/{name}", method = DELETE)
    public void deleteGreeting(@PathVariable("name") String name, HttpServletRequest request) {
        AuthInfo authInfo = AuthUtil.getAuthInfo(request);

        greetingService.deleteGreeting(name, authInfo);
    }
}
