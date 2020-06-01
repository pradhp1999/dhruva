package com.cisco.dhruva.adaptor;

import static com.cisco.dhruva.common.CommonContext.PROXY_ROUTE_RESULT;
import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.router.MessageListener;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.proxy.ProxyInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsURI;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
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

  public Location constructProxyLocation(String routeResult) throws DhruvaException {
    try {
      DsURI uri = DsURI.constructFrom(routeResult);
      return new Location(uri);
    } catch (DsSipParserException ex) {
      throw new DhruvaException(
          "failed to construct location from route result", ex.getCause(), ex.getMessage());
    }
  }

  @Override
  public void onMessage(IDhruvaMessage message) throws DhruvaException {
    logger.info("onMessage {}" + message.getSessionId());
    ProxyInterface controller = proxyAdaptor.getProxyController();
    if (controller == null) {
      throw new DhruvaException(
          "controller object for this message is null" + message.getSessionId());
    }
    MessageBodyType messageType = message.getMessageBody().getBodyType();
    switch (messageType) {
      case SIPREQUEST:
        ExecutionContext ctx = message.getContext();
        String routeResult = (String) ctx.get(PROXY_ROUTE_RESULT);
        Location loc = constructProxyLocation(routeResult);
        DsSipRequest request =
            (DsSipRequest) MessageConvertor.convertDhruvaMessageToSipMessage(message);
        controller.proxyTo(loc, request, proxyAdaptor);
        break;

      case SIPRESPONSE:
        // MEETPASS Check for message type
        // If request, formulate the Location object from context.Form DsSipRequest object
        // If response , invoke the controller respond with DsSipResponse
        DsSipResponse response =
            (DsSipResponse) MessageConvertor.convertDhruvaMessageToSipMessage(message);
        try {
          controller.respond(response);
        } catch (DsException e) {
          throw new DhruvaException("exception sending response" + e.getMessage());
        }
        break;

      default:
        logger.warn(
            "unhandled message type {} , session Id {}", messageType, message.getSessionId());
        throw new DhruvaException("Unknown message type" + messageType);
    }
  }
}
