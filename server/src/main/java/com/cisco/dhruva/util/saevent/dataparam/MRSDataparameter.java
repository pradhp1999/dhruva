package com.cisco.dhruva.util.saevent.dataparam;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MRSDataparameter extends DataParam {
  private String callId;
  private String sessionId;
  private String reasonText;
  private String meetingURI;
  private String mrsHostname;
  private String exception;
  private String exceptionInfo;
  private String stackTrace;

  @JsonProperty("Call-ID")
  public String getCallId() {
    return callId;
  }

  public void setCallId(String callId) {
    this.callId = callId;
  }

  @JsonProperty("Session-ID")
  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getReasonText() {
    return reasonText;
  }

  public void setReasonText(String reasonText) {
    this.reasonText = reasonText;
  }

  public String getmeetingURI() {
    return meetingURI;
  }

  public void setmeetingURI(String hostname) {
    this.meetingURI = hostname;
  }

  public String getmrsHostname() {
    return mrsHostname;
  }

  public void setmrsHostname(String hostname) {
    this.mrsHostname = hostname;
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

  public void setExceptionInfo(String exceptioninfo) {
    this.exceptionInfo = exceptioninfo;
  }

  @JsonProperty("stackTrace")
  public String getExceptionstackTrace() {
    return stackTrace;
  }

  public void setExceptionstackTrace(String exceptionstackTrace) {
    this.stackTrace = exceptionstackTrace;
  }
}
