package com.cisco.dhruva.adaptor;

import static java.util.Objects.requireNonNull;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.CommonContext;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.router.MessageListener;
import com.cisco.dhruva.sip.controller.DsProxyController;
import com.cisco.dhruva.sip.controller.DsProxyResponseGenerator;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.Optional;

public class ProxyAdaptor extends AbstractProxyAdaptor<AppSession> implements AppAdaptorInterface {

  private Logger logger = DhruvaLoggerFactory.getLogger(ProxyAdaptor.class);
  /**
   * Creates new instance of an proxyAdaptor
   *
   * @param controller
   */
  protected DsProxyController controller;

  protected MessageListener handler;

  protected AppInterface appSession;

  public ProxyAdaptor(DsProxyController controller, AppInterface appSession) {
    super();
    requireNonNull(controller, "controller cannot be null");
    requireNonNull(appSession, "app session interface cannot be null");
    this.controller = controller;
    this.appSession = appSession;
  }

  /*
   * Handles the incoming request from proxy/controller
   * Builds the execution context, dhruva message from sip request
   * Forwards the request to the App
   */
  @Override
  public void handleRequest(DsSipRequest request) throws DhruvaException {
    logger.info("ProxyAdaptor.handleRequest()");

    // MEETPASS
    final ExecutionContext context;
    handler = new RouteResponseHandler(this);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    IDhruvaMessage dhruvaRequest = buildDhruvaMessageFromSIPRequest(request, context);

    if (appSession == null) {
      throw new DhruvaException("AppSession cannot be null");
    }
    appSession.handleRequest(dhruvaRequest);
  }

  /*
   * Handles the response from proxy/controller
   */
  @Override
  public void handleResponse(Location loc, Optional<DsSipResponse> response, int responseCode)
      throws DhruvaException {
    DsSipResponse resp;
    if (!response.isPresent()) {
      logger.info("response object is empty");
      try {
        resp = DsProxyResponseGenerator.createResponse(responseCode, this.controller.getRequest());
      } catch (DsException e) {
        logger.error("exception while handling response ", e.getMessage());
        throw new DhruvaException("exception in handling empty response", e.getMessage());
      }
    } else {
      resp = response.get();
    }

    final ExecutionContext context;
    handler = new RouteResponseHandler(this);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    IDhruvaMessage dhruvaResponse = buildDhruvaMessageFromSIPResponse(resp, context);
    appSession.handleResponse(dhruvaResponse);
  }

  private IDhruvaMessage buildDhruvaMessageFromSIPRequest(
      DsSipRequest request, ExecutionContext context) throws DhruvaException {

    requireNonNull(request, "incoming request object cannot be null");
    requireNonNull(context, "incoming context object cannot be null");
    return MessageConvertor.convertSipMessageToDhruvaMessage(
        request, MessageBodyType.SIPREQUEST, context);
  }

  private IDhruvaMessage buildDhruvaMessageFromSIPResponse(
      DsSipResponse response, ExecutionContext context) throws DhruvaException {

    return MessageConvertor.convertSipMessageToDhruvaMessage(
        response, MessageBodyType.SIPRESPONSE, context);
  }

  public DsProxyController getProxyController() {
    return this.controller;
  }
}
