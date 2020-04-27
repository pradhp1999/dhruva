package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.router.MessageListener;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.proxy.controller.DsControllerInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

public class RouteResponseHandler implements MessageListener, ProxyResponseInterface {

  private Logger logger = DhruvaLoggerFactory.getLogger(RouteResponseHandler.class);
  private DsControllerInterface controller;

  public RouteResponseHandler(DsControllerInterface controller) {
    this.controller = controller;
  }

  @Override
  public void onMessage(IDhruvaMessage message) {
    DsSipResponse response =
        (DsSipResponse) MessageConvertor.convertDhruvaMessageToSipMessage(message);

    int code = response.getStatusCode();

    switch (code) {
      case 200:
        this.onSuccessResponse(new Location(), message, code);
        break;
      default:
        logger.warn("Unknown response code", code);
    }
  }

  @Override
  public void onSuccessResponse(Location location, IDhruvaMessage response, int responseCode) {}
}
