package com.cisco.dhruva.util.saevent.dataparam;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

// POJO class builder for SIP message events

public class SIPMessageDataParam extends DataParam {
  public static class Builder {
    private String callId;
    private String callType;
    private int code;
    private int contentLength;
    private String cseq;
    private String cseqMethod;
    private String direction;
    private Map headerParams;
    private boolean hostAndLocalIPSame;
    private boolean isInternallyGenerated;
    private String isMidCall;
    private boolean isMsgRetransmitted;
    private String localAlias;
    private String localIPAddress;
    private int localPort;
    private String localSessionID;
    private String method;
    private String reason;
    private int reasonCause;
    private String reasonProtocol;
    private String reasonText;
    private String certCommonName;
    private Map certSubAltName;
    private String remoteAlias;
    private String remoteIPAddress;
    private int remotePort;
    private String remoteSessionID;
    private String requestURI;
    private String ruriHost;
    private String ruriUser;
    private String sdp;
    private String serviceType;
    private String siteUUID;
    private String sipMsgType;
    private String stackTrace;
    private String transport;
    private String sessionId;
    private String callMediaType;
    private String callingNumber;
    private String calledNumber;

    public SIPMessageDataParam build() {
      return new SIPMessageDataParam(this);
    }

    public Builder callId(String callId) {
      this.callId = callId;
      return this;
    }

    public Builder callType(String callType) {
      this.callType = callType;
      return this;
    }

    public Builder siteUUID(String siteUUID) {
      this.siteUUID = siteUUID;
      return this;
    }

    public Builder code(int code) {
      this.code = code;
      return this;
    }

    public Builder contentLength(int contentLength) {
      this.contentLength = contentLength;
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

    public Builder direction(String direction) {
      this.direction = direction;
      return this;
    }

    public Builder headerParams(Map headerParams) {
      this.headerParams = headerParams;
      return this;
    }

    public Builder hostAndLocalIPSame(boolean hostAndLocalIPSame) {
      this.hostAndLocalIPSame = hostAndLocalIPSame;
      return this;
    }

    public Builder isInternallyGenerated(boolean isInternallyGenerated) {
      this.isInternallyGenerated = isInternallyGenerated;
      return this;
    }

    public Builder isMsgRetransmitted(boolean isMsgRetransmitted) {
      this.isMsgRetransmitted = isMsgRetransmitted;
      return this;
    }

    public Builder isMidCall(String isMidCall) {
      this.isMidCall = isMidCall;
      return this;
    }

    public Builder localAlias(String localAlias) {
      this.localAlias = localAlias;
      return this;
    }

    public Builder localIPAddress(String localIPAddress) {
      this.localIPAddress = localIPAddress;
      return this;
    }

    public Builder localPort(int localPort) {
      this.localPort = localPort;
      return this;
    }

    public Builder localSessionID(String localSessionID) {
      this.localSessionID = localSessionID;
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

    public Builder reasonCause(int reasonCause) {
      this.reasonCause = reasonCause;
      return this;
    }

    public Builder reasonProtocol(String reasonProtocol) {
      this.reasonProtocol = reasonProtocol;
      return this;
    }

    public Builder reasonText(String reasonText) {
      this.reasonText = reasonText;
      return this;
    }

    public Builder certCommonName(String certCommonName) {
      this.certCommonName = certCommonName;
      return this;
    }

    public Builder certSubAltName(Map certSubAltName) {
      this.certSubAltName = certSubAltName;
      return this;
    }

    public Builder remoteAlias(String remoteAlias) {
      this.remoteAlias = remoteAlias;
      return this;
    }

    public Builder remoteIPAddress(String remoteIPAddress) {
      this.remoteIPAddress = remoteIPAddress;
      return this;
    }

    public Builder remotePort(int remotePort) {
      this.remotePort = remotePort;
      return this;
    }

    public Builder remoteSessionID(String remoteSessionID) {
      this.remoteSessionID = remoteSessionID;
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

    public Builder callingNumber(String callingNumber) {
      this.callingNumber = callingNumber;
      return this;
    }

    public Builder calledNumber(String calledNumber) {
      this.calledNumber = calledNumber;
      return this;
    }

    public Builder ruriUser(String ruriUser) {
      this.ruriUser = ruriUser;
      return this;
    }

    public Builder sdp(String sdp) {
      this.sdp = sdp;
      return this;
    }

    public Builder serviceType(String serviceType) {
      this.serviceType = serviceType;
      return this;
    }

    public Builder sipMsgType(String sipMsgType) {
      this.sipMsgType = sipMsgType;
      return this;
    }

    public Builder stackTrace(String stackTrace) {
      this.stackTrace = stackTrace;
      return this;
    }

    public Builder transport(String transport) {
      this.transport = transport;
      return this;
    }

    public Builder callMediaType(String callMediaType) {
      this.callMediaType = callMediaType;
      return this;
    }

    public Builder sessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
    }
  }

  private String callId;
  private String callType;
  private int code;
  private int contentLength;
  private String cseq;
  private String cseqMethod;
  private String direction;
  private Map headerParams;
  private boolean hostAndLocalIPSame;
  private boolean isInternallyGenerated;
  private String isMidCall;
  private boolean isMsgRetransmitted;
  private String localAlias;
  private String localIPAddress;
  private int localPort;
  private String localSessionID;
  private String method;
  private String reason;
  private int reasonCause;
  private String reasonProtocol;
  private String reasonText;
  private String certCommonName;
  private Map certSubAltName;
  private String remoteAlias;
  private String remoteIPAddress;
  private int remotePort;
  private String remoteSessionID;
  private String requestURI;
  private String ruriHost;
  private String ruriUser;
  private String sdp;
  private String serviceType;
  private String siteUUID;
  private String sipMsgType;
  private String stackTrace;
  private String transport;
  private String sessionId;
  private String callMediaType;
  private String callingNumber;
  private String calledNumber;

  private SIPMessageDataParam(Builder builder) {
    this.method = builder.method;
    this.requestURI = builder.requestURI;
    this.sipMsgType = builder.sipMsgType;
    this.direction = builder.direction;
    this.callId = builder.callId;
    this.cseq = builder.cseq;
    this.cseqMethod = builder.cseqMethod;
    this.contentLength = builder.contentLength;
    this.localAlias = builder.localAlias;
    this.localSessionID = builder.localSessionID;
    this.remoteSessionID = builder.remoteSessionID;
    this.localIPAddress = builder.localIPAddress;
    this.localPort = builder.localPort;
    this.remoteIPAddress = builder.remoteIPAddress;
    this.remotePort = builder.remotePort;
    this.code = builder.code;
    this.reason = builder.reason;
    this.isInternallyGenerated = builder.isInternallyGenerated;
    this.isMsgRetransmitted = builder.isMsgRetransmitted;
    this.remoteAlias = builder.remoteAlias;
    this.isMidCall = builder.isMidCall;
    this.callType = builder.callType;
    this.serviceType = builder.serviceType;
    this.siteUUID = builder.siteUUID;
    this.ruriUser = builder.ruriUser;
    this.ruriHost = builder.ruriHost;
    this.transport = builder.transport;
    this.headerParams = builder.headerParams;
    this.stackTrace = builder.stackTrace;
    this.sdp = builder.sdp;
    this.hostAndLocalIPSame = builder.hostAndLocalIPSame;
    this.reasonProtocol = builder.reasonProtocol;
    this.reasonCause = builder.reasonCause;
    this.reasonText = builder.reasonText;
    this.certCommonName = builder.certCommonName;
    this.certSubAltName = builder.certSubAltName;
    this.sessionId = builder.sessionId;
    this.callMediaType = builder.callMediaType;
    this.callingNumber = builder.callingNumber;
    this.calledNumber = builder.calledNumber;
  }

  @JsonProperty("Call-ID")
  public String getCallId() {
    return callId;
  }

  public String getCallType() {
    return callType;
  }

  public int getCode() {
    return code;
  }

  public int getContentLength() {
    return contentLength;
  }

  @JsonProperty("CSEQ")
  public String getCseq() {
    return cseq;
  }

  public String getCseqMethod() {
    return cseqMethod;
  }

  public String getDirection() {
    return direction;
  }

  public Map getHeaderParams() {
    return headerParams;
  }

  public String getIsMidCall() {
    return isMidCall;
  }

  public String getLocalAlias() {
    return localAlias;
  }

  public String getLocalIPAddress() {
    return localIPAddress;
  }

  public int getLocalPort() {
    return localPort;
  }

  public String getLocalSessionID() {
    return localSessionID;
  }

  public String getMethod() {
    return method;
  }

  public String getReason() {
    return reason;
  }

  public int getReasonCause() {
    return reasonCause;
  }

  public String getReasonProtocol() {
    return reasonProtocol;
  }

  public String getReasonText() {
    return reasonText;
  }

  public String getCertCommonName() {
    return certCommonName;
  }

  public Map getCertSubAltName() {
    return certSubAltName;
  }

  public String getRemoteAlias() {
    return remoteAlias;
  }

  public String getRemoteIPAddress() {
    return remoteIPAddress;
  }

  public int getRemotePort() {
    return remotePort;
  }

  public String getRemoteSessionID() {
    return remoteSessionID;
  }

  public String getRequestURI() {
    return requestURI;
  }

  public String getRuriHost() {
    return ruriHost;
  }

  public String getCallingNumber() {
    return callingNumber;
  }

  public String getCalledNumber() {
    return calledNumber;
  }

  public String getRuriUser() {
    return ruriUser;
  }

  public String getSdp() {
    return sdp;
  }

  public String getServiceType() {
    return serviceType;
  }

  public String getSiteUUID() {
    return siteUUID;
  }

  public String getSipMsgType() {
    return sipMsgType;
  }

  public String getStackTrace() {
    return stackTrace;
  }

  public String getTransport() {
    return transport;
  }

  public String getCallMediaType() {
    return callMediaType;
  }

  public void setCallMediaType(String callMediaType) {
    this.callMediaType = callMediaType;
  }

  public boolean isHostAndLocalIPSame() {
    return hostAndLocalIPSame;
  }

  public boolean isIsInternallyGenerated() {
    return isInternallyGenerated;
  }

  public boolean isIsMsgRetransmitted() {
    return isMsgRetransmitted;
  }

  public void setCallId(String callId) {
    this.callId = callId;
  }

  public void setCallType(String callType) {
    this.callType = callType;
  }

  @JsonProperty("Session-ID")
  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  public void setCseq(String cseq) {
    this.cseq = cseq;
  }

  public void setCseqMethod(String cseqMethod) {
    this.cseqMethod = cseqMethod;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public void setHeaderParams(Map headerParams) {
    this.headerParams = headerParams;
  }

  public void setHostAndLocalIPSame(boolean hostAndLocalIPSame) {
    this.hostAndLocalIPSame = hostAndLocalIPSame;
  }

  public void setIsInternallyGenerated(boolean isInternallyGenerated) {
    this.isInternallyGenerated = isInternallyGenerated;
  }

  public void setIsMsgRetransmitted(boolean isMsgRetransmitted) {
    this.isMsgRetransmitted = isMsgRetransmitted;
  }

  public void setIsMidCall(String isMidCall) {
    this.isMidCall = isMidCall;
  }

  public void setLocalAlias(String localAlias) {
    this.localAlias = localAlias;
  }

  public void setLocalIPAddress(String localIPAddress) {
    this.localIPAddress = localIPAddress;
  }

  public void setLocalPort(int localPort) {
    this.localPort = localPort;
  }

  public void setLocalSessionID(String localSessionID) {
    this.localSessionID = localSessionID;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public void setReasonCause(int reasonCause) {
    this.reasonCause = reasonCause;
  }

  public void setReasonProtocol(String reasonProtocol) {
    this.reasonProtocol = reasonProtocol;
  }

  public void setReasonText(String reasonText) {
    this.reasonText = reasonText;
  }

  public void setCertCommonName(String certCommonName) {
    this.certCommonName = certCommonName;
  }

  public void setCertSubAltName(Map certSubAltName) {
    this.certSubAltName = certSubAltName;
  }

  public void setRemoteAlias(String remoteAlias) {
    this.remoteAlias = remoteAlias;
  }

  public void setRemoteIPAddress(String remoteIPAddress) {
    this.remoteIPAddress = remoteIPAddress;
  }

  public void setRemotePort(int remotePort) {
    this.remotePort = remotePort;
  }

  public void setRemoteSessionID(String remoteSessionID) {
    this.remoteSessionID = remoteSessionID;
  }

  public void setRequestURI(String requestURI) {
    this.requestURI = requestURI;
  }

  public void setRuriHost(String ruriHost) {
    this.ruriHost = ruriHost;
  }

  public void setCallingNumber(String callingNumber) {
    this.callingNumber = callingNumber;
  }

  public void setCalledNumber(String calledNumber) {
    this.calledNumber = calledNumber;
  }

  public void setRuriUser(String ruriUser) {
    this.ruriUser = ruriUser;
  }

  public void setSdp(String sdp) {
    this.sdp = sdp;
  }

  public void setServiceType(String serviceType) {
    this.serviceType = serviceType;
  }

  public void setSiteUUID(String siteUUID) {
    this.siteUUID = siteUUID;
  }

  public void setSipMsgType(String sipMsgType) {
    this.sipMsgType = sipMsgType;
  }

  public void setStackTrace(String stackTrace) {
    this.stackTrace = stackTrace;
  }

  public void setTransport(String transport) {
    this.transport = transport;
  }
}
