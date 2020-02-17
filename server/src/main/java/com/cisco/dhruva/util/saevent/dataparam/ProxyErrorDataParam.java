package com.cisco.dhruva.util.saevent.dataparam;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ProxyErrorDataParam extends DataParam {

  private static final long serialVersionUID = 1L;

  public enum CallStatus {
    SUCCESS("Success"),
    FAILURE("Failure");
    private String status;

    private CallStatus(String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }
  }

  public static class Builder {
    private String proxyErrorType;
    private Integer proxyErrorCode;
    private String remoteIPAddress;
    private String localIPAddress;
    private Integer remotePort;
    private Integer localPort;
    private String method;
    private String reason;
    private String requestURI;
    private String ruriHost;
    private String ruriUser;
    private String reasonHeader;
    private String cseq;
    private String cseqMethod;
    private String from;
    private String to;
    private Integer code;
    private String callId;
    private String exception;
    private String exceptionInfo;
    private String stackTrace;
    private String query;
    private String description;
    private String serverGroupName;
    private Map<String, String> headerParams;
    private String callStatus; // SUCCESS | FAILURE
    // REFACTOR
    private String bindingInfoType = "response";
    // private String bindingInfoType = SIPMessageLoggerMBeanImpl.RESPONSE;
    private String sessionId;
    private String callType;

    public Builder proxyErrorType(String proxyErrorType) {
      this.proxyErrorType = proxyErrorType;
      return this;
    }

    public Builder proxyErrorCode(Integer proxyErrorCode) {
      this.proxyErrorCode = proxyErrorCode;
      return this;
    }

    public Builder remoteIPAddress(String remoteIPAddress) {
      this.remoteIPAddress = remoteIPAddress;
      return this;
    }

    public Builder localIPAddress(String localIPAddress) {
      this.localIPAddress = localIPAddress;
      return this;
    }

    public Builder remotePort(Integer remotePort) {
      this.remotePort = remotePort;
      return this;
    }

    public Builder localPort(Integer localPort) {
      this.localPort = localPort;
      return this;
    }

    public Builder method(String method) {
      this.method = method;
      return this;
    }

    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    public Builder requestURI(String requestURI) {
      this.requestURI = requestURI;
      return this;
    }

    public Builder ruriHost(String ruriHost) {
      this.ruriHost = ruriHost;
      return this;
    }

    public Builder ruriUser(String ruriUser) {
      this.ruriUser = ruriUser;
      return this;
    }

    public Builder reasonHeader(String reasonHeader) {
      this.reasonHeader = reasonHeader;
      return this;
    }

    public Builder cseq(String cseq) {
      this.cseq = cseq;
      return this;
    }

    public Builder cseqMethod(String cseqMethod) {
      this.cseqMethod = cseqMethod;
      return this;
    }

    public Builder from(String from) {
      this.from = from;
      return this;
    }

    public Builder to(String to) {
      this.to = to;
      return this;
    }

    public Builder code(Integer code) {
      this.code = code;
      return this;
    }

    public Builder callId(String callId) {
      this.callId = callId;
      return this;
    }

    public Builder exception(String exception) {
      this.exception = exception;
      return this;
    }

    public Builder exceptionInfo(String exceptionInfo) {
      this.exceptionInfo = exceptionInfo;
      return this;
    }

    public Builder stackTrace(String stackTrace) {
      this.stackTrace = stackTrace;
      return this;
    }

    public Builder query(String query) {
      this.query = query;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder serverGroupName(String serverGroupName) {
      this.serverGroupName = serverGroupName;
      return this;
    }

    public Builder headerParams(Map<String, String> headerParams) {
      this.headerParams = headerParams;
      return this;
    }

    public Builder callStatus(CallStatus callStatus) {
      this.callStatus = callStatus.getStatus();
      return this;
    }

    public Builder bindingInfoType(String sipMsgType) {
      this.bindingInfoType = sipMsgType;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }

    public Builder callType(String callType) {
      this.callType = callType;
      return this;
    }

    public ProxyErrorDataParam build() {
      return new ProxyErrorDataParam(this);
    }
  }

  private String proxyErrorType;
  private Integer proxyErrorCode;
  private String remoteIPAddress;
  private String localIPAddress;
  private Integer remotePort;
  private Integer localPort;
  private String method;
  private String reason;
  private String requestURI;
  private String ruriHost;
  private String ruriUser;
  private String reasonHeader;
  private String cseq;
  private String cseqMethod;
  private String from;
  private String to;
  private Integer code;
  private String callId;
  private String exception;
  private String exceptionInfo;
  private String stackTrace;
  private String query;
  private String description;
  private String serverGroupName;
  private Map<String, String> headerParams;
  private final Boolean proxyErrorEvent = true;
  private String callStatus; // SUCCESS | FAILURE
  // REFACTOR
  private String bindingInfoType = "response";
  // private String bindingInfoType = SIPMessageLoggerMBeanImpl.RESPONSE;
  private String sessionId;
  private String callType;

  private ProxyErrorDataParam(Builder builder) {
    this.proxyErrorType = builder.proxyErrorType;
    this.proxyErrorCode = builder.proxyErrorCode;
    this.remoteIPAddress = builder.remoteIPAddress;
    this.localIPAddress = builder.localIPAddress;
    this.remotePort = builder.remotePort;
    this.localPort = builder.localPort;
    this.method = builder.method;
    this.reason = builder.reason;
    this.requestURI = builder.requestURI;
    this.ruriHost = builder.ruriHost;
    this.ruriUser = builder.ruriUser;
    this.reasonHeader = builder.reasonHeader;
    this.cseq = builder.cseq;
    this.cseqMethod = builder.cseqMethod;
    this.from = builder.from;
    this.to = builder.to;
    this.code = builder.code;
    this.callId = builder.callId;
    this.exception = builder.exception;
    this.exceptionInfo = builder.exceptionInfo;
    this.stackTrace = builder.stackTrace;
    this.query = builder.query;
    this.serverGroupName = builder.serverGroupName;
    this.headerParams = builder.headerParams;
    this.callStatus = builder.callStatus;
    this.bindingInfoType = builder.bindingInfoType;
    this.sessionId = builder.sessionId;
    this.callType = builder.callType;
    this.description = builder.description;
  }

  public String getProxyErrorType() {
    return proxyErrorType;
  }

  public void setProxyErrorType(String proxyErrorType) {
    this.proxyErrorType = proxyErrorType;
  }

  public Integer getProxyErrorCode() {
    return proxyErrorCode;
  }

  public void setProxyErrorCode(Integer proxyErrorCode) {
    this.proxyErrorCode = proxyErrorCode;
  }

  public String getRemoteIPAddress() {
    return remoteIPAddress;
  }

  public void setRemoteIPAddress(String remoteIPAddress) {
    this.remoteIPAddress = remoteIPAddress;
  }

  public String getLocalIPAddress() {
    return localIPAddress;
  }

  public void setLocalIPAddress(String localIPAddress) {
    this.localIPAddress = localIPAddress;
  }

  public Integer getRemotePort() {
    return remotePort;
  }

  public void setRemotePort(Integer remotePort) {
    this.remotePort = remotePort;
  }

  public Integer getLocalPort() {
    return localPort;
  }

  public void setLocalPort(Integer localPort) {
    this.localPort = localPort;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getRequestURI() {
    return requestURI;
  }

  public void setRequestURI(String requestURI) {
    this.requestURI = requestURI;
  }

  public String getRuriHost() {
    return ruriHost;
  }

  public void setRuriHost(String ruriHost) {
    this.ruriHost = ruriHost;
  }

  public String getRuriUser() {
    return ruriUser;
  }

  public void setRuriUser(String ruriUser) {
    this.ruriUser = ruriUser;
  }

  public String getReasonHeader() {
    return reasonHeader;
  }

  public void setReasonHeader(String reasonHeader) {
    this.reasonHeader = reasonHeader;
  }

  @JsonProperty("CSEQ")
  public String getCseq() {
    return cseq;
  }

  public void setCseq(String cseq) {
    this.cseq = cseq;
  }

  public String getCseqMethod() {
    return cseqMethod;
  }

  public void setCseqMethod(String cseqMethod) {
    this.cseqMethod = cseqMethod;
  }

  public String getFrom() {
    return from;
  }

  public void setFrom(String from) {
    this.from = from;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  @JsonProperty("Call-ID")
  public String getCallId() {
    return callId;
  }

  public void setCallId(String callId) {
    this.callId = callId;
  }

  public String getException() {
    return exception;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public String getExceptionInfo() {
    return exceptionInfo;
  }

  public void setExceptionInfo(String exceptionInfo) {
    this.exceptionInfo = exceptionInfo;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public Map<String, String> getHeaderParams() {
    return headerParams;
  }

  public void setHeaderParams(Map<String, String> headerParams) {
    this.headerParams = headerParams;
  }

  public String getCallStatus() {
    return callStatus;
  }

  public void setCallStatus(CallStatus callStatus) {
    this.callStatus = callStatus.getStatus();
  }

  public Boolean getProxyErrorEvent() {
    return proxyErrorEvent;
  }

  public String getBindingInfoType() {
    return bindingInfoType;
  }

  public void setBindingInfoType(String bindingInfoType) {
    this.bindingInfoType = bindingInfoType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getServerGroupName() {
    return serverGroupName;
  }

  public void setServerGroupName(String serverGroupName) {
    this.serverGroupName = serverGroupName;
  }

  @JsonProperty("Session-ID")
  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getCallType() {
    return callType;
  }

  public void setCallType(String callType) {
    this.callType = callType;
  }
}
