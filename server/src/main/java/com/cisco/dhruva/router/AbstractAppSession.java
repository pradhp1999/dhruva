package com.cisco.dhruva.router;

import com.cisco.dhruva.common.CommonContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;

public abstract class AbstractAppSession implements AppInterface {

  protected AppEngine appEngine;

  public AbstractAppSession() {}

  public void handleIncomingRequest(IDhruvaMessage request) {
    if (appEngine == null) {
      appEngine = new AppEngine(this);
    }
    appEngine.handleMessage(request);
  }

  void onResponse(IDhruvaMessage response) {
    MessageListener handler =
        (MessageListener) response.getContext().get(CommonContext.PROXY_RESPONSE_HANDLER);
    handler.onMessage(response);
  }
}
