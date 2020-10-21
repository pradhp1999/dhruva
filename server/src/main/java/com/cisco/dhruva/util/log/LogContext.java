/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.util.log;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LogContext {

  public static final String SIP_METHOD = "sipMethod";
  public static final String SIP_CALL_ID = "sipCallId";
  public static final String LOCAL_SIP_SESSION_ID_FIELD = "localSessionID";
  public static final String REMOTE_SIP_SESSION_ID_FIELD = "remoteSessionID";

  private String callId;
  private String localSessionId;
  private String remoteSessionId;
  private String trackingId;
  private String method;
  private String sipMethod;

  public LogContext setCallId(String callId) {
    this.callId = callId;
    this.trackingId = callId;
    return this;
  }

  public LogContext setLocalSessionId(String localSessionId) {
    this.localSessionId = localSessionId;
    return this;
  }

  public LogContext setRemoteSessionId(String remoteSessionId) {
    this.remoteSessionId = remoteSessionId;
    return this;
  }

  public LogContext setSipMethod(String sipMethod) {
    this.sipMethod = sipMethod;
    return this;
  }

  public void setLogContext(Map<String, String> logContextMap) {
    callId = logContextMap.get(SIP_CALL_ID);
    localSessionId = logContextMap.get(LOCAL_SIP_SESSION_ID_FIELD);
    remoteSessionId = logContextMap.get(REMOTE_SIP_SESSION_ID_FIELD);
  }

  public Map<String, String> getLogContextAsMap() {
    Map<String, String> logContextMap = new HashMap<>();
    logContextMap.put(SIP_CALL_ID, callId);
    logContextMap.put(LOCAL_SIP_SESSION_ID_FIELD, localSessionId);
    logContextMap.put(REMOTE_SIP_SESSION_ID_FIELD, remoteSessionId);
    logContextMap.put(SIP_METHOD, sipMethod);
    return logContextMap;
  }

  public Optional<LogContext> getLogContext(DsSipMessage message) {
    setCallId(message.getCallId().toString());
    if (message.isRequest()) {
      sipMethod = ((DsSipRequest) message).getMethod().toString();
    } else {
      sipMethod = String.valueOf(((DsSipResponse) message).getStatusCode());
    }
    message.getLocalSessionId().ifPresent(localSessionId -> setLocalSessionId(localSessionId));
    message.getRemoteSessionId().ifPresent(remoteSessionId -> setRemoteSessionId(remoteSessionId));
    return Optional.of(this);
  }

  public void resetLogContext() {
    callId = null;
    localSessionId = null;
    remoteSessionId = null;
  }
}