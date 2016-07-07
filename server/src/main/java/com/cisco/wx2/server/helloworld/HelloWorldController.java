package com.cisco.wx2.server.helloworld;

import com.cisco.wx2.dto.BuildInfo;
import com.cisco.wx2.dto.health.ServiceHealth;
import com.cisco.wx2.helloworld.client.HelloWorldClient;
import com.cisco.wx2.helloworld.common.HelloResponse;
import com.cisco.wx2.metrics.StatsdClient;
import com.cisco.wx2.server.AbstractController;
import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.server.auth.AuthInfo;
import com.cisco.wx2.server.auth.AuthorizationNone;
import com.cisco.wx2.server.health.ServiceHealthManager;
import com.cisco.wx2.util.EmailAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.versly.rest.wsdoc.AuthorizationScope;
import org.versly.rest.wsdoc.DocumentationRestApi;
import org.versly.rest.wsdoc.DocumentationScope;
import org.versly.rest.wsdoc.DocumentationTraits;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * The Hello World microservice.
 * <p/>
 * <i>(TODO: provide API-level documentation for this microservice)</i>
 */
@DocumentationRestApi(id = "hello-world", title = "Hello World", version = "v1", mount = "")
@DocumentationScope(DocumentationScope.PRIVATE)
@DocumentationTraits(DocumentationTraits.STABLE)
@RestController
@RequestMapping("/hello-world/api/v1")
public class HelloWorldController extends AbstractController {
    private final ServiceHealthManager serviceHealthManager;
    private final HelloWorldProperties properties;
    private final BuildInfo buildInfo;
    private final StatsdClient statsdClient;
    private final String metricsPrefix;

    @Autowired
    public HelloWorldController(HelloWorldConfig config) {
        serviceHealthManager = config.serviceHealthManager();
        properties = config.helloWorldProperties();
        buildInfo = config.buildInfo();
        statsdClient = config.statsdClient();
        metricsPrefix = config.wx2Properties().getStatsdPrefix(config.getMetricsNamespace());
    }

    @RequestMapping(value = "service_health", method = GET)
    public ServiceHealth getServiceHealth() {
        return serviceHealthManager.getServiceHealth();
    }

    @RequestMapping(value = "build_info", method = GET)
    public BuildInfo getBuildInfo() {
        return buildInfo;
    }

    @AuthorizationNone
    @RequestMapping(value = "ping", method = GET)
    public ServiceHealth ping() {
        String stat = metricsPrefix+".ping";
        statsdClient.increment(stat);
        return getServiceHealth();
    }

    @Autowired HelloWorldClient client;

    @AuthorizationNone
    @RequestMapping(value = "clientping", method = GET)
    public ServiceHealth clientping() {
        return client.ping();
    }

    @AuthorizationScope("webex-squared")
    @RequestMapping(value = "{email}", method = GET)
    public HelloResponse hello(@PathVariable("email") EmailAddress email) {
        // make sure the current user is authorized to access this resource
        String stat = metricsPrefix+".hello";
        statsdClient.increment(stat);
        AuthInfo authInfo = authInfo();
        if (authInfo != null && authInfo.isUser() && email.equals(authInfo.getUser().getEmail())) {
            stat = metricsPrefix + ".hello.authSuccess";
            statsdClient.increment(stat);
            return new HelloResponse("Hello, " + authInfo.getEffectiveUser().getName() + "!");
        } else {
            stat = metricsPrefix+".hello.authFail";
            statsdClient.increment(stat);
            throw ServerException.forbidden("access to this resource is not allowed");
        }
    }
}
