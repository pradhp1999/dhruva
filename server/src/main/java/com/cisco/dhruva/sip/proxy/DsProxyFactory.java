package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

public class DsProxyFactory implements DsProxyFactoryInterface {

  @Override
  public DsProxyInterface createProxyTransaction(
      DsControllerInterface controller,
      DsProxyParamsInterface config,
      DsSipServerTransaction server,
      DsSipRequest request)
      throws DsInternalProxyErrorException {

    return new DsProxyTransaction(controller, config, server, request);
  }
}
