package com.cisco.dhruva.adaptor;

import static com.cisco.dhruva.common.CommonContext.PROXY_ROUTE_RESULT;
import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.app.Destination;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.common.messaging.models.MessageHeaders;
import com.cisco.dhruva.router.MessageListener;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.proxy.ProxyInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

// MEETPASS
// Use the same MessageListener interface for any communication,let the context object store the
// routes
public class RouteResponseHandler implements MessageListener {

  private final Logger logger = DhruvaLoggerFactory.getLogger(RouteResponseHandler.class);
  ProxyAdaptor proxyAdaptor;

  public RouteResponseHandler(ProxyAdaptor proxyAdaptor) {
    requireNonNull(proxyAdaptor);
    this.proxyAdaptor = proxyAdaptor;
  }

  public Location constructProxyLocation(DsSipRequest request, Destination routeResult)
      throws DhruvaException {
    try {
      DsNetwork network = DsNetwork.findNetwork(routeResult.network);
      Location loc = new Location(request.getURI());
      switch (routeResult.destinationType) {
        case DEFAULT_SIP: // Mid Dialog cases
          loc.setProcessRoute(true);
          network = request.getNetwork();
          break;
        case SRV: // L2SIP cluster address
          loc.setURI(request.getURI());
          loc.setProcessRoute(true);
          // loc.setURI(DsURI.constructFrom("sip:" + routeResult.address));
          // loc.setURI(request.getURI());
          break;
        default:
          logger.warn("routeResult not set properly for request {}", request.getCallId());
      }
      loc.setNetwork(network);
      return loc;
    } catch (Exception ex) {
      throw new DhruvaException(
          "failed to construct location from route result", ex.getCause(), ex.getMessage());
    }
  }

  @Override
  public void onMessage(IDhruvaMessage message) throws DhruvaException {
    logger.info("message : " + message.toString());
    logger.info("onMessage {}", message.getSessionId());
    ProxyInterface controller = proxyAdaptor.getProxyController();
    if (controller == null) {
      throw new DhruvaException(
          "controller object for this message is null" + message.getSessionId());
    }
    MessageBodyType messageType = message.getMessageBody().getBodyType();

    // MEETPASS Check for message type
    // If request, formulate the Location object from context.Form DsSipRequest object
    // If response , invoke the controller respond with DsSipResponse
    switch (messageType) {
      case SIPREQUEST:
        ExecutionContext ctx = message.getContext();
        Destination routeResult = (Destination) ctx.get(PROXY_ROUTE_RESULT);

        DsSipRequest request =
            (DsSipRequest) MessageConvertor.convertDhruvaMessageToSipMessage(message);
        Location loc = constructProxyLocation(request, routeResult);

        MessageHeaders newHeaders = message.getHeaders();
        if (!newHeaders.isEmpty()) {
          String routeHeader = (String) newHeaders.get("Route");
          if (routeHeader != null) {
            try {
              DsSipRouteHeader r = new DsSipRouteHeader(DsURI.constructFrom(routeHeader));
              DsSipHeaderList routeHeaders = new DsSipHeaderList();
              routeHeaders.addLast(r);
              loc.setRouteHeaders(routeHeaders);
              logger.info("adding route header {} in to location {}", r.toString(), loc);
            } catch (DsSipParserException e) {
              throw new DhruvaException(
                  "exception while constructing route header", e.getMessage());
            }
          }
        }

        // Location loc = new Location(request.getURI());
        logger.info("route result {}", routeResult);
        controller.proxyTo(loc, request, proxyAdaptor);
        break;

      case SIPRESPONSE:
        DsSipResponse response =
            (DsSipResponse) MessageConvertor.convertDhruvaMessageToSipMessage(message);
        try {
          controller.respond(response);
        } catch (DsException e) {
          throw new DhruvaException("exception sending response " + e.getMessage());
        }
        break;

      default:
        logger.warn(
            "unhandled message type {} , session Id {}", messageType, message.getSessionId());
        throw new DhruvaException("Unknown message type" + messageType);
    }
  }
}
