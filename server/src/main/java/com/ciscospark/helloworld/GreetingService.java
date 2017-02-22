package com.ciscospark.helloworld;

import com.cisco.wx2.dto.wdm.FeatureToggle;
import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.server.auth.AuthInfo;
import com.cisco.wx2.wdm.client.FeatureClient;
import com.cisco.wx2.wdm.client.FeatureClientFactory;
import com.ciscospark.helloworld.api.Greeting;
import com.ciscospark.server.CiscoSparkServerProperties;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RefreshScope
@RequestScope
public class GreetingService {
    private final String defaultGreetingPrefix;
    private final String message;
    private final String trailer;

    private final Map<String, String> store;

    private final FeatureClientFactory featureClientFactory;

    private final CiscoSparkServerProperties serverProperties;

    private final HttpServletRequest request;

    @Autowired
    public GreetingService(HelloWorldProperties properties, FeatureClientFactory featureClientFactory, @Qualifier("store") Map<String, String> store, CiscoSparkServerProperties serverProperties, HttpServletRequest request) {
        this.defaultGreetingPrefix = properties.getDefaultGreetingPrefix();
        this.message = properties.message;
        this.trailer = properties.trailer;
        this.featureClientFactory = featureClientFactory;
        this.store = store;
        this.serverProperties = serverProperties;
        this.request = request;
    }

    Greeting getGreeting(String name, Optional<AuthInfo> authInfo)
    {
        try {
            return getEnhancedGreeting(name, authInfo);
        } catch(Exception e) {
            log.debug("Unable to retrieve enhanced greeting - {}", e);
            return getDefaultGreeting(name, authInfo);
        }
    }

    private Greeting getEnhancedGreeting(String name, Optional<AuthInfo> authInfo) {
        Preconditions.checkNotNull(name);

        String greeting = store.get(name);
        if (greeting == null) {
            log.debug("Greeting is not present in the greeting store, so using default greeting prefix");
            greeting = defaultGreetingPrefix + " " + name;
        }

        /* Use the feature client to determine what the exact message is to return to the caller.
         * This is simply an example of how to call another service. Note that we could have simply
         * used an "orElse(message)" instead of "orElseGet(...)", but I wanted to include a debug
         * statement here as an example.
         */
        String sendingMessage =
            authInfo.flatMap( ai -> {
                String msg = message;
                FeatureClient client = featureClientFactory.newClient(ai.getAuthorization());
                String key = serverProperties.getName() + ".adduserresponse";
                FeatureToggle feature = client.getFeature(ai.getEffectiveUser().getId(), key);
                if (feature != null && feature.getBooleanValue()) {
                    log.debug("Feature {} is set, adding trailer and username to response", key);
                    msg += " " + trailer + ai.getEffectiveUser().getName();
                } else
                    log.debug("Feature {} is not set, omitting trailer and username", key);

                return Optional.of(msg);
            }).orElseGet(() -> {
                log.debug("AuthInfo is not present, omitting trailer and username");
                return message;
            });

        return Greeting.builder().greeting(greeting).message(sendingMessage).build();
    }

    /* The fallback method will not invoke any network operations, so will return an uncustomized greeting */
    private Greeting getDefaultGreeting(String name, Optional<AuthInfo> authInfo) {
        log.debug("Circuit failure. Executing default greeting");
        String greeting = defaultGreetingPrefix + " " + name;
        return Greeting.builder().greeting(greeting).message(message + " (fallback)").build();
    }

    Greeting setGreeting(String name, String greeting) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(greeting);


        log.debug("Setting greeting for name '{}' to '{}'", name, greeting);
        store.put(name, greeting);
        return Greeting.builder().greeting(greeting).message(message).build();
    }

    void deleteGreeting(String name) {
        Preconditions.checkNotNull(name);

        log.info("Removing name '{}' from greeting store", name);
        String greeting = store.remove(name);

        if (greeting == null) {
            throw ServerException.notFound("Greeting not found!");
        }
    }
}
