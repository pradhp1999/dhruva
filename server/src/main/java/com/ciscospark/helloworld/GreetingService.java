package com.ciscospark.helloworld;

import com.cisco.wx2.dto.wdm.FeatureToggle;
import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.server.auth.AuthInfo;
import com.cisco.wx2.feature.client.FeatureClient;
import com.cisco.wx2.feature.client.FeatureClientFactory;
import com.ciscospark.helloworld.api.Greeting;
import com.ciscospark.server.CiscoSparkServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
@RefreshScope
public class GreetingService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String defaultGreetingPrefix;
    private final String message;
    private final String trailer;
    private final Map<String, String> store;
    private final FeatureClientFactory featureClientFactory;
    private final CiscoSparkServerProperties serverProperties;

    @Autowired
    public GreetingService(HelloWorldProperties properties, FeatureClientFactory featureClientFactory, @Qualifier("store") Map<String, String> store, CiscoSparkServerProperties serverProperties) {
        this.defaultGreetingPrefix = properties.getDefaultGreetingPrefix();
        this.message = properties.getMessage();
        this.trailer = properties.getTrailer();
        this.featureClientFactory = featureClientFactory;
        this.store = store;
        this.serverProperties = serverProperties;
    }

    Greeting getGreeting(String name, Optional<AuthInfo> authInfo) {
        requireNonNull(name);
        requireNonNull(authInfo);

        if(authInfo.isPresent()) {
            log.info("Getting greeting for name : {}, userId : {}, orgId : {}", name, authInfo.get().getEffectiveUserId(), authInfo.get().getOrgId());
        }
        
        try {
            return getEnhancedGreeting(name, authInfo);
        } catch (Exception e) {
            log.error("Unable to retrieve enhanced greeting", e);
            return getDefaultGreeting(name, authInfo);
        }
    }

    private Greeting getEnhancedGreeting(String name, Optional<AuthInfo> authInfo) {
        requireNonNull(name);
        requireNonNull(authInfo);

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
                authInfo.flatMap(ai -> {
                    String msg = message;
                    FeatureClient client = featureClientFactory.newClient(ai.getAuthorization());
                    String key = serverProperties.getName() + "-adduserresponse";
                    FeatureToggle feature = client.getDeveloperFeatureOrNull(ai.getEffectiveUser().getId(), key);
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
        requireNonNull(name);
        requireNonNull(authInfo);

        log.debug("Circuit failure. Executing default greeting");
        String greeting = defaultGreetingPrefix + " " + name;
        return Greeting.builder().greeting(greeting).message(message + " (fallback)").build();
    }

    Greeting setGreeting(String name, String greeting, AuthInfo authInfo) {
        requireNonNull(name);
        requireNonNull(greeting);
        requireNonNull(authInfo);


        log.info("Setting greeting for name '{}' to '{}'", name, greeting);
        store.put(name, greeting);
        return Greeting.builder().greeting(greeting).message(message).build();
    }

    void deleteGreeting(String name, AuthInfo authInfo) {
        requireNonNull(name);
        requireNonNull(authInfo);

        if (store.get(name) == null) {
            throw ServerException.notFound("Greeting not found!");
        }

        log.info("Removing name '{}' from greeting store", name);
        store.remove(name);

    }
}
