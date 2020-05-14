package com.cisco.dhruva.sip.proxy.Errors;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;

public class DsXclRoutingFailureException extends DsProxyError {

  private DsSipResponse sipResponse;

  public DsXclRoutingFailureException(DsSipResponse sipResponse) {
    this.sipResponse = sipResponse;
    this.errorCode = DsProxyErrorCode.ERROR_XCL_ROUTING_FAIlURE;
  }

  public DsSipResponse getSipResponse() {
    return sipResponse;
  }

  @Override
  public String getDescription() {
    return new StringBuilder()
        .append("{errorCode:")
        .append(errorCode)
        .append(",statusCode:")
        .append(sipResponse.getStatusCode())
        .append(",reasonPharse:")
        .append(sipResponse.getReasonPhrase())
        .append(",errorType:")
        .append(errorCode.getDescription())
        .append("}")
        .toString();
  }

  @Override
  public DsSipResponse getResponse() {
    return sipResponse;
  }

  @Override
  public Throwable getException() {
    return null;
  }

  @Override
  public DsBindingInfo getBindingInfo() {
    return sipResponse.getBindingInfo();
  }
}
