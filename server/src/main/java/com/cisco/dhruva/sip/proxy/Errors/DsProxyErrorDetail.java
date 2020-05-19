package com.cisco.dhruva.sip.proxy.Errors;

import com.cisco.dhruva.sip.cac.SIPSession;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;

public class DsProxyErrorDetail {

  private DsSipRequest request;
  private SIPSession sipSession;
  private DsProxyErrorAggregator.ErrorType errorType;
  private DsProxyError proxyError;

  public DsProxyErrorDetail(
      DsSipRequest request,
      SIPSession sipSession,
      DsProxyErrorAggregator.ErrorType errorType,
      DsProxyError proxyError) {
    this.request = request;
    this.sipSession = sipSession;
    this.errorType = errorType;
    this.proxyError = proxyError;
  }

  public DsSipRequest getRequest() {
    return request;
  }

  public DsSipResponse getResponse() {
    return proxyError.getResponse();
  }

  public SIPSession getSipSession() {
    return sipSession;
  }

  public DsProxyErrorAggregator.ErrorType getErrorType() {
    return errorType;
  }

  public DsProxyError getProxyError() {
    return proxyError;
  }
}
