package com.cisco.dhruva.adaptor;

import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.router.MessageListener;
import com.cisco.dhruva.sip.proxy.ProxyInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

// MEETPASS, delete AppResponseInterface, not used
// Use the same MessageListener interface for any communication,let the context object store the
// routes
public class RouteResponseHandler implements MessageListener {

  private Logger logger = DhruvaLoggerFactory.getLogger(RouteResponseHandler.class);
  ProxyAdaptor proxyAdaptor;

  public RouteResponseHandler(ProxyAdaptor proxyAdaptor) {
    requireNonNull(proxyAdaptor);
    this.proxyAdaptor = proxyAdaptor;
  }

  @Override
  public void onMessage(IDhruvaMessage message) throws DhruvaException {
    logger.info("onMessage {}" + message.getSessionId());

    // MEETPASS Check for message type
    // If request, formulate the Location object from context.Form DsSipRequest object
    // If response , invoke the controller respond with DsSipResponse
    DsSipResponse response =
        (DsSipResponse) MessageConvertor.convertDhruvaMessageToSipMessage(message);
    ProxyInterface controller = proxyAdaptor.getProxyController();
    if (controller == null) {
      throw new DhruvaException(
          "controller object for this message is null" + message.getSessionId());
    }
    try {
      controller.respond(response);
    } catch (DsException e) {
      throw new DhruvaException("exception sending response" + e.getMessage());
    }
  }
}
