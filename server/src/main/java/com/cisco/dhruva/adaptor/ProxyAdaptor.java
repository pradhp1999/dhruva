package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.CommonContext;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.sip.proxy.controller.DsControllerInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

public class ProxyAdaptor extends AbstractProxyAdaptor<AppSession> implements AppAdaptorInterface {

  private Logger logger = DhruvaLoggerFactory.getLogger(ProxyAdaptor.class);
  /**
   * Creates new instance of an proxyAdaptor
   *
   * @param controller
   */
  protected DsControllerInterface controller;

  protected RouteResponseHandler handler;

  protected AppSession appSession;

  /*
  public ProxyAdaptor(DsProxyController controller) {
      super(controller);
  }
  */

  public ProxyAdaptor(DsControllerInterface controller) {
    super();
    this.controller = controller;
  }

  @Override
  public void handleRequest(DsSipRequest request) throws DhruvaException {
    logger.debug("ProxyAdaptor.handleRequest: ");
    final ExecutionContext context;
    handler = new RouteResponseHandler(this.controller);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    IDhruvaMessage dhruvaRequest = buildDhruvaMessageFromSIPRequest(request, context);

    if (appSession == null) {
      this.appSession = new AppSession();
    }
    appSession.handleRequest(dhruvaRequest);

    return;
  }

  private IDhruvaMessage buildDhruvaMessageFromSIPRequest(
      DsSipRequest request, ExecutionContext context) {
    IDhruvaMessage message =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);
    return message;
  }
}
