package com.cisco.dhruva.router;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import org.springframework.stereotype.Service;

@Service
public class AppSession extends AbstractAppSession {
  public AppSession() {
    super();
  }

  @Override
  public void handleRequest(IDhruvaMessage request) {
    super.handleRequest(request);
  }

  @Override
  public void handleResponse(IDhruvaMessage response) {
    super.handleResponse(response);
  }
}
