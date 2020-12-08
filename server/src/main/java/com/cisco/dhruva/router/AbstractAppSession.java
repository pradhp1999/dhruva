package com.cisco.dhruva.router;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.CommonContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractAppSession implements AppInterface {

  protected AppEngine appEngine;

  private final Logger logger = DhruvaLoggerFactory.getLogger(AbstractAppSession.class);

  public AbstractAppSession() {
    //      ApplicationContext applicationContext = SpringApplicationContext.getAppContext();
    //      if (applicationContext == null) throw new DhruvaException("spring app context null");
    //      dhruvaProperties = applicationContext.getBean(DhruvaProperties.class);
    this.appEngine = new AppEngine(this.handleResponseFromApp);
  }

  public void handleRequest(IDhruvaMessage request) throws DhruvaException {
    logger.info("handleIncomingRequest session Id's : {}", request.getSessionId());
    appEngine.handleRequest(request);
  }

  public void handleResponse(IDhruvaMessage response) throws DhruvaException {
    Objects.requireNonNull(response);
    appEngine.handleResponse(response);
  }

  Consumer<IDhruvaMessage> handleResponseFromApp =
      (response) -> {
        Objects.requireNonNull(response);
        logger.info("onResponse: invoking message handler for message {}", response.getSessionId());
        MessageListener handler;
        handler = (MessageListener) response.getContext().get(CommonContext.PROXY_RESPONSE_HANDLER);
        handler.onMessage(response);
      };
}
