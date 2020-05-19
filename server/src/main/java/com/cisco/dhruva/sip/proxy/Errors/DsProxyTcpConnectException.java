package com.cisco.dhruva.sip.proxy.Errors;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class DsProxyTcpConnectException extends DsProxyError {

  private Exception exception;
  private DsBindingInfo bindingInfo;

  public DsProxyTcpConnectException(Exception e, DsBindingInfo bindingInfo) {
    this.exception = e;
    this.bindingInfo = bindingInfo;

    if (e instanceof SocketTimeoutException) {
      this.errorCode = DsProxyErrorCode.ERROR_TCP_CONNECTION_TIMEDOUT;
    } else if (e instanceof ConnectException) {
      this.errorCode = DsProxyErrorCode.ERROR_TCP_CONNECTION_REFUSED;
    } else {
      errorCode = DsProxyErrorCode.ERROR_TCP_CONNECTION_OTHER;
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
        .append(exception.getLocalizedMessage());
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

    builder.append(",errorType:").append(errorCode.getDescription()).append("}");
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

  @Override
  public DsBindingInfo getBindingInfo() {
    return bindingInfo;
  }
}
