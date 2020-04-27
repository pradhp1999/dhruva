package com.cisco.dhruva.router;

import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

public class AppSession extends AbstractAppSession {
  public AppSession() {
    super();
  }

  @Override
  public void handleRequest(IDhruvaMessage request) {
    super.handleIncomingRequest(request);
  }

  @Override
  public void handleResponse(IDhruvaMessage response) {}
}
