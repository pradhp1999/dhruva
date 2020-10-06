package com.cisco.dhruva.router;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.CommonContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.Objects;

public abstract class AbstractAppSession implements AppInterface {

  protected AppEngine appEngine;

  private Logger logger = DhruvaLoggerFactory.getLogger(AbstractAppSession.class);

  public AbstractAppSession() {}

  public void handleRequest(IDhruvaMessage request) {

    logger.info("handleIncomingRequest session Id's : {}", request.getSessionId());

    if (appEngine == null) {
      appEngine = new AppEngine(this);
    }

    try {
      appEngine.handleMessage(request);
    } catch (Exception e) {
      logger.error("exception while creating response {}", e.getMessage());
      throw new DhruvaException(
          "exception {} while handling request {}" + e.getMessage() + request.getSessionId());
    }
  }

  public void handleResponse(IDhruvaMessage response) {
    Objects.requireNonNull(response);
    logger.info("onResponse: invoking message handler {}", response.getSessionId());
    MessageListener handler;
    handler = (MessageListener) response.getContext().get(CommonContext.PROXY_RESPONSE_HANDLER);
    // MEETPASS for end to end test
    handler.onMessage(response);
  }
}
