package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.sip.proxy.controller.DsControllerInterface;

public interface ProxyAdaptorFactoryInterface {
  public AppAdaptorInterface getProxyAdaptor(DsControllerInterface controller);
}
