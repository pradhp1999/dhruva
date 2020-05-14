package com.cisco.dhruva.sip.proxy.Errors;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.transport.Transport;
import java.net.ConnectException;
import java.net.InetAddress;

public class DsProxyTlsConnectException extends DsProxyError {

  private DsSSLContext context;
  private Exception exception;
  private DsBindingInfo bindingInfo;

  public DsProxyTlsConnectException(
      Exception e,
      DsSSLContext context,
      InetAddress localAddr,
      int localPort,
      InetAddress remortAddr,
      int remortPort) {
    this.context = context;
    this.exception = e;
    this.bindingInfo =
        new DsBindingInfo(localAddr, localPort, remortAddr, remortPort, Transport.TLS);
    interpretPlatformException();
  }

  private void interpretPlatformException() {

    if (this.exception instanceof ConnectException) {
      errorCode = DsProxyErrorCode.ERROR_TCP_CONNECTION_REFUSED;
    } else if (this.exception.getLocalizedMessage().contains("connect timed out")) {
      errorCode = DsProxyErrorCode.ERROR_TCP_CONNECTION_TIMEDOUT;
    } else if (this.exception.getLocalizedMessage().contains("Read timed out")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_TIMEDOUT;
    } else if (this.exception.getLocalizedMessage().contains("No trusted certificate found")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_NO_TRUSTED_CERT;
    } else if (this.exception
        .getLocalizedMessage()
        .contains("End entity certificate extension check failed")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_CERT_EXT_EE;
    } else if (this.exception
        .getLocalizedMessage()
        .contains("CA certificate extension check failed")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_CERT_EXT_CA;
    } else if (this.exception.getLocalizedMessage().contains("CertificateExpiredException")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_CERT_EXPIRED;
    } else if (this.exception
        .getLocalizedMessage()
        .contains("Certificate signature validation failed")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_CERT_SIGN;
    } else if (this.exception.getLocalizedMessage().contains("Certificate chaining error")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_CERT_CHAIN;
    } else if (this.exception
        .getLocalizedMessage()
        .contains("Certificate signature algorithm disabled")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_CERT_SIGN_ALG;
    } else if (this.exception.getLocalizedMessage().contains("Untrusted certificate")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_CERT_HASH;
    } else if (this.exception.getLocalizedMessage().contains("Received fatal alert")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_ALERT;
    } else if (this.exception
        .getLocalizedMessage()
        .contains("Remote host closed connection during handshake")) {
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_MISC1;
    } else { // infer more based on the error string here
      errorCode = DsProxyErrorCode.ERROR_TLS_HANDSHAKE_OTHER;
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
