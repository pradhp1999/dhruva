package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.sip.proxy.controller.DsControllerInterface;

public class ProxyAdaptorFactory implements ProxyAdaptorFactoryInterface {

  public ProxyAdaptorFactory() {}

  @Override
  public AppAdaptorInterface getProxyAdaptor(DsControllerInterface controller) {
    AppAdaptorInterface adaptor = new ProxyAdaptor(controller);
    return adaptor;
  }
}
