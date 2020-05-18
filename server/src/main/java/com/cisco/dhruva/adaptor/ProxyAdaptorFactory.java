package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.sip.controller.DsProxyController;

public class ProxyAdaptorFactory implements ProxyAdaptorFactoryInterface {

  public ProxyAdaptorFactory() {}

  @Override
  public AppAdaptorInterface getProxyAdaptor(DsProxyController controller, AppInterface app) {
    return new ProxyAdaptor(controller, app);
  }
}
