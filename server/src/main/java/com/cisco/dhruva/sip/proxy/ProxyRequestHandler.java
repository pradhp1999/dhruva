package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.controller.DsProxyController;

public class ProxyRequestHandler {
        //implements ProxyInterface {
  private DsProxyController controller;

  public ProxyRequestHandler(DsProxyController controller) {
    this.controller = controller;
  }
}
