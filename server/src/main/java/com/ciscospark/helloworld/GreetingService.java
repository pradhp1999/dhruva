package com.ciscospark.helloworld;

import com.cisco.wx2.dto.wdm.FeatureToggle;
import com.cisco.wx2.server.ServerException;
import com.cisco.wx2.server.auth.AuthInfo;
import com.cisco.wx2.wdm.client.FeatureClient;
import com.cisco.wx2.wdm.client.FeatureClientFactory;
import com.ciscospark.helloworld.api.Greeting;
import com.ciscospark.server.CiscoSparkServerProperties;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RefreshScope
public class GreetingService {
    private final String defaultGreetingPrefix;
    private final String message;
    private final String trailer;

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private final Map<String, String> store = new HashMap<>();
    private final FeatureClientFactory featureClientFactory;

    @Autowired
    private CiscoSparkServerProperties serverProperties;

    @Autowired
    public GreetingService(HelloWorldProperties properties, FeatureClientFactory featureClientFactory)
    {
        this.defaultGreetingPrefix = properties.getDefaultGreetingPrefix();
        this.message = properties.message;
        this.trailer = properties.trailer;
        this.featureClientFactory = featureClientFactory;
    }

    Greeting getGreeting(String name, AuthInfo... authInfo)
    {
        Preconditions.checkNotNull(name);
        String sendingMessage = message;

        String greeting = store.get(name);
        if (greeting == null) {
            log.debug("Greeting is not present in the greeting store, so using default greeting prefix");
            greeting = defaultGreetingPrefix + " " + name;
        }

        /* Use the feature client to determine what the exact message is to return to the caller.
         * This is simply an example of how to call another service. Note that since getGreeting can
         * be called without authorization, we're using a varargs list
         */
        if (authInfo.length > 0 && authInfo[0] != null) {
            FeatureClient client = featureClientFactory.newClient(authInfo[0].getAuthorization());
            String key = serverProperties.getName() + ".adduserresponse";
            FeatureToggle feature = client.getFeature(authInfo[0].getEffectiveUser().getId(), key);
            if (feature != null && feature.getBooleanValue()) {
                log.debug("Feature {} is set, adding trailer and username to response", key);
                sendingMessage += " " + trailer + authInfo[0].getEffectiveUser().getName();
            } else
                log.debug("Feature {} is not set, omitting trailer and username", key);
        } else
            log.debug("AuthInfo is not present, omitting trailer and username");

        return Greeting.builder().greeting(greeting).message(sendingMessage).build();
    }

    Greeting setGreeting(String name, String greeting, AuthInfo authInfo)
    {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(greeting);


        log.debug("Setting greeting for name '{}' to '{}'", name, greeting);
        store.put(name, greeting);
        return Greeting.builder().greeting(greeting).message(message).build();
    }

    void deleteGreeting(String name)
    {
        Preconditions.checkNotNull(name);

        log.info("Removing name '{}' from greeting store", name);
        String greeting = store.remove(name);

        if (greeting == null) {
            throw ServerException.notFound("Greeting not found!");
        }
    }
}
