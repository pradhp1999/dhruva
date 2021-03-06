package com.cisco.dhruva.adaptor;

import static com.cisco.dhruva.common.CommonContext.PROXY_ROUTE_RESULT;
import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.app.Destination;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.router.MessageListener;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.proxy.ProxyInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.Objects;
import java.util.Optional;

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

  public void addRequestDestinationSipRoutes(DsSipRequest request, Destination destination)
      throws DsSipParserListenerException, DsSipParserException {
    Objects.requireNonNull(destination);
    Objects.requireNonNull(request);
    Transport outgoingTransport = Transport.TLS;
    if (destination.network != null) {
      Optional<Transport> transportOptional = DsNetwork.getTransport(destination.network);
      if (transportOptional.isPresent()) {
        outgoingTransport = transportOptional.get();
      }
    }
    String transportStr = Transport.getTypeAsByteString(outgoingTransport).toString().toLowerCase();

    DsSipRouteHeader routeHeader =
        new DsSipRouteHeader(
            ("<sip:" + destination.address + ";lr" + ";transport=" + transportStr + ">")
                .getBytes());

    DsSipHeaderList routeHeaders = new DsSipHeaderList();
    routeHeaders.addLast(routeHeader);
    request.addHeaders(routeHeaders, true);
  }

  public Location constructProxyLocation(DsSipRequest request, Destination routeResult)
      throws DhruvaException {
    try {
      DsNetwork network = DsNetwork.findNetwork(routeResult.network);
      Location loc = new Location(request.getURI());
      loc.setNetwork(network);
      switch (routeResult.destinationType) {
        case DEFAULT_SIP: // Mid Dialog cases
          loc.setProcessRoute(true);
          break;
        case SRV: // L2SIP cluster address
          loc.setURI(request.getURI());
          loc.setProcessRoute(true);
          // If we want to change outgoing requri
          // loc.setURI(DsURI.constructFrom(routeResult.address));
          break;
        default:
          logger.warn("routeResult not set properly for request, {}", routeResult);
      }

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

        try {
          addRequestDestinationSipRoutes(request, routeResult);
          // If App has modified reqURI , modify in outgoing request
          request.setURI(DsURI.constructFrom(message.getReqURI()));
        } catch (DsSipParserException | DsSipParserListenerException e) {
          throw new DhruvaException("exception while constructing route header", e.getMessage());
        }

        Location loc = constructProxyLocation(request, routeResult);

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
