package com.cisco.dhruva.sip.servergroups.util.interfaces;

import com.cisco.dhruva.sip.proxy.DsProxyInterface;

public interface TestValidator {
  public void validate() throws Exception;

  public void capture(DsProxyInterface proxyTransactio) throws Exception;
}
