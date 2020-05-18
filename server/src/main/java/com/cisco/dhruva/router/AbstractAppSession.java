package com.cisco.dhruva.router;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.CommonContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.sip.controller.DsProxyResponseGenerator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponseCode;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

public abstract class AbstractAppSession implements AppInterface {

  protected AppEngine appEngine;

  private Logger logger = DhruvaLoggerFactory.getLogger(AbstractAppSession.class);

  public AbstractAppSession() {}

  public void handleRequest(IDhruvaMessage request) {
    logger.info("handleIncomingRequest {}" + request.getSessionId());
    if (appEngine == null) {
      appEngine = new AppEngine(this);
    }
    appEngine.handleMessage(request);
    try {
      // IDhruvaMessage response = createDummyResponse(request);
      // this.handleResponse(response);
    } catch (Exception e) {
      logger.error("exception while creating response {}" + e.getMessage());
      throw new DhruvaException(
          "exception {} while handling request {}" + e.getMessage() + request.getSessionId());
    }
  }

  public void handleResponse(IDhruvaMessage response) {
    logger.info("onResponse: invoking message handler {}" + response.getSessionId());
    MessageListener handler;
    handler = (MessageListener) response.getContext().get(CommonContext.PROXY_RESPONSE_HANDLER);
    // MEETPASS for end to end test
    handler.onMessage(response);
  }

  IDhruvaMessage createDummyResponse(IDhruvaMessage request) throws Exception {
    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_OK,
            (DsSipRequest) request.getMessageBody().getPayloadData());

    return MessageConvertor.convertSipMessageToDhruvaMessage(
        resp, MessageBodyType.SIPRESPONSE, request.getContext());
  }
}
