package com.cisco.dhruva.sip.proxy.Errors;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;

public abstract class DsProxyError extends Exception {

  protected DsProxyErrorCode errorCode = DsProxyErrorCode.ERROR_UNIDENTIFIED;

  public abstract String getDescription();

  public DsProxyErrorCode getErrorCode() {
    return errorCode;
  }

  public abstract DsSipResponse getResponse();

  public abstract Throwable getException();

  public abstract DsBindingInfo getBindingInfo();
}
