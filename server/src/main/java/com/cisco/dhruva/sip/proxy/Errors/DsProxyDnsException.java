package com.cisco.dhruva.sip.proxy.Errors;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import javax.naming.CommunicationException;
import javax.naming.NameNotFoundException;

public class DsProxyDnsException extends DsProxyError {

  public static final int QUERY_TYPE_SRV = 1;
  public static final int QUERY_TYPE_A = 2;

  private int queryType;
  private String query;
  private Exception exception;
  private DsBindingInfo bindingInfo;

  public DsProxyDnsException(int queryType, String query, Exception e, DsBindingInfo bindingInfo) {
    this.queryType = queryType;
    this.query = query;
    this.exception = e;
    this.bindingInfo = bindingInfo;

    if (e instanceof NameNotFoundException) {
      errorCode =
          (queryType == QUERY_TYPE_SRV)
              ? DsProxyErrorCode.ERROR_DNS_SRV_NO_RECORDS
              : DsProxyErrorCode.ERROR_DNS_A_NO_RECORDS;
    } else if (e instanceof CommunicationException) {
      errorCode =
          (queryType == QUERY_TYPE_SRV)
              ? DsProxyErrorCode.ERROR_DNS_SRV_QUERY_TIMEDOUT
              : DsProxyErrorCode.ERROR_DNS_A_QUERY_TIMEDOUT;
    } else {
      errorCode =
          (queryType == QUERY_TYPE_SRV)
              ? DsProxyErrorCode.ERROR_DNS_SRV_OTHER
              : DsProxyErrorCode.ERROR_DNS_A_OTHER;
    }
  }

  @Override
  public String getDescription() {
    StringBuilder builder = new StringBuilder();
    builder
        .append("{errorCode:")
        .append(errorCode)
        .append(",exceptionClass:")
        .append(exception.getClass())
        .append(",exceptionMessage:")
        .append(exception.getLocalizedMessage())
        .append(",query:")
        .append(query)
        .append(",queryType:")
        .append(queryType);
    if (bindingInfo != null) {
      if (bindingInfo.getRemoteAddress() != null) {
        builder
            .append(",remoteIP:")
            .append(bindingInfo.getRemoteAddress().getHostAddress())
            .append(",remotePort:")
            .append(bindingInfo.getRemotePort());
      }
      if (bindingInfo.getLocalAddress() != null) {
        builder
            .append(",localIP:")
            .append(bindingInfo.getLocalAddress().getHostAddress())
            .append(",localPort:")
            .append(bindingInfo.getLocalPort());
      }
    }
    builder.append(",errorType:").append(errorCode.getDescription()).append("}").toString();
    return builder.toString();
  }

  @Override
  public DsSipResponse getResponse() {
    return null;
  }

  @Override
  public Throwable getException() {
    return exception;
  }

  public String getQuery() {
    return query;
  }

  @Override
  public DsBindingInfo getBindingInfo() {
    return bindingInfo;
  }
}
