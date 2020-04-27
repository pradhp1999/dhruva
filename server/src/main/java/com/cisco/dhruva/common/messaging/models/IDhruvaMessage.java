package com.cisco.dhruva.common.messaging.models;

import com.cisco.dhruva.common.context.ExecutionContext;
import java.util.Map;

public interface IDhruvaMessage extends Cloneable {

  /** Returns the call context of this message. */
  ExecutionContext getContext();

  MessageHeaders getHeaders();

  void setHeaders(MessageHeaders newHeaders);

  boolean hasBody();

  void setHasBody(boolean hasBody);

  public MessageBody getMessageBody();

  public void setMessageBody(MessageBody body);

  public Map<String, String> getProperties();

  void setProperties(Map<String, String> properties);

  public String getCorrelationId();

  public void setCorrelationId(String correlationId);

  public String getSessionId();

  public void setSessionId(String sessionId);

  public String getReqURI();

  public void setCallType(String callType);

  public String getCallType();

  public void setReqURI(String reqURI);

  /** Returns a copy of this message. */
  IDhruvaMessage clone();
}
