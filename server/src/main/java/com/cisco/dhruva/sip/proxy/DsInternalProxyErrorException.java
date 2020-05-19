package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

public class DsInternalProxyErrorException extends DsException {

  public DsInternalProxyErrorException(String msg) {
    super(msg);
  }
}
