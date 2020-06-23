package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerTransaction;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

@FunctionalInterface
public interface DsProxyFactoryInterface {
  DsProxyInterface createProxyTransaction(
      DsControllerInterface controller,
      DsProxyParamsInterface config,
      DsSipServerTransaction server,
      DsSipRequest request)
      throws DsInternalProxyErrorException;
}
