package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.sip.controller.DsProxyController;

@FunctionalInterface
public interface ProxyAdaptorFactoryInterface {
  AppAdaptorInterface getProxyAdaptor(DsProxyController controller, AppInterface app);
}
