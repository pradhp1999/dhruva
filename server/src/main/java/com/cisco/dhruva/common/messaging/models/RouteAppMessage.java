package com.cisco.dhruva.common.messaging.models;

import com.cisco.dhruva.common.CallType;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.DhruvaMessageImpl;
import org.springframework.util.Assert;

public final class RouteAppMessage {

  public static RouteAppMessageBuilder newBuilder() {
    return new RouteAppMessageBuilder();
  }

  public static final class RouteAppMessageBuilder {
    private MessageHeaders headers;

    private MessageBody payload;

    private ExecutionContext context;

    private CallType callType;
    private String sessionID;
    private String correlationID;
    private String reqURI;

    private RouteAppMessageBuilder() {
      this(null, null, null, null, null, null, null);
    }

    private RouteAppMessageBuilder(
        ExecutionContext context,
        MessageHeaders headers,
        MessageBody payload,
        CallType callType,
        String sessionID,
        String correlationID,
        String reqURI) {
      this.context = context;
      this.headers = headers;
      this.payload = payload;
      this.callType = callType;
      this.sessionID = sessionID;
      this.correlationID = correlationID;
      this.reqURI = reqURI;
    }

    public IDhruvaMessage build() {
      IDhruvaMessage message = new DhruvaMessageImpl(this.context, this.headers, this.payload);
      if (callType != null) message.setCallType(callType);
      if (correlationID != null) message.setCorrelationId(correlationID);
      if (reqURI != null) message.setReqURI(reqURI);
      if (sessionID != null) message.setSessionId(sessionID);
      return message;
    }

    public RouteAppMessageBuilder withPayload(MessageBody payload) {
      Assert.notNull(payload, "Payload must not be null");
      return new RouteAppMessageBuilder(
          context, headers, payload, callType, sessionID, correlationID, reqURI);
    }

    public RouteAppMessageBuilder withHeaders(MessageHeaders headers) {
      Assert.notNull(headers, "Payload must not be null");
      return new RouteAppMessageBuilder(
          context, headers, payload, callType, sessionID, correlationID, reqURI);
    }

    public RouteAppMessageBuilder withContext(ExecutionContext context) {
      Assert.notNull(context, "Payload must not be null");
      return new RouteAppMessageBuilder(
          context, headers, payload, callType, sessionID, correlationID, reqURI);
    }

    public RouteAppMessageBuilder sessionId(String sessionID) {
      return new RouteAppMessageBuilder(
          context, headers, payload, callType, sessionID, correlationID, reqURI);
    }

    public RouteAppMessageBuilder correlationId(String correlationID) {
      return new RouteAppMessageBuilder(
          context, headers, payload, callType, sessionID, correlationID, reqURI);
    }

    public RouteAppMessageBuilder reqURI(String reqURI) {
      return new RouteAppMessageBuilder(
          context, headers, payload, callType, sessionID, correlationID, reqURI);
    }

    public RouteAppMessageBuilder callType(CallType callType) {
      return new RouteAppMessageBuilder(
          context, headers, payload, callType, sessionID, correlationID, reqURI);
    }
  }

  private RouteAppMessage() {}
}
