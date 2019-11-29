package com.ciscospark.dhruva.client;

import com.cisco.wx2.client.Client;
import com.cisco.wx2.client.health.ServiceHealthPinger;
import com.cisco.wx2.dto.health.ServiceHealth;
import com.ciscospark.dhruva.api.Greeting;
import java.net.URI;
import org.apache.http.HttpResponse;

public class DhruvaClient extends Client implements ServiceHealthPinger {
  protected DhruvaClient(DhruvaClientFactory factory, URI baseUrl) {
    super(factory, baseUrl);
  }

  public ServiceHealth ping() {
    return get("ping").execute(ServiceHealth.class);
  }

  public Greeting getGreeting(String name) {
    return get("greetings", name).execute(Greeting.class);
  }

  public Greeting setGreeting(String name, Greeting greeting) {
    return post("greetings", name).jsonEntity(greeting).execute(Greeting.class);
  }

  public HttpResponse deleteGreeting(String name) {
    return delete("greetings", name).execute();
  }
}
