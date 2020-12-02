package com.cisco.dhruva.common.messaging.models;

import com.cisco.dhruva.common.CallType;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.DhruvaMessageImpl;
import com.cisco.dhruva.util.log.LogContext;
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
    private boolean isMidCall;
    private boolean isRequest;
    private String network;
    private LogContext loggingContext;

    private RouteAppMessageBuilder() {
      routeAppMessageBuilder(null, null, null, null, null, null, null, null, false);
    }

    private void routeAppMessageBuilder(
        ExecutionContext context,
        MessageHeaders headers,
        MessageBody payload,
        CallType callType,
        String sessionID,
        String correlationID,
        String reqURI,
        String network,
        boolean isMidCall) {
      this.context = context;
      this.headers = headers;
      this.payload = payload;
      this.callType = callType;
      this.sessionID = sessionID;
      this.correlationID = correlationID;
      this.reqURI = reqURI;
      this.isMidCall = isMidCall;
      this.network = network;
    }

    public IDhruvaMessage build() {
      IDhruvaMessage message = new DhruvaMessageImpl(this.context, this.headers, this.payload);
      if (callType != null) {
        message.setCallType(callType);
      }
      if (correlationID != null) {
        message.setCorrelationId(correlationID);
      }
      if (reqURI != null) {
        message.setReqURI(reqURI);
      }
      if (sessionID != null) {
        message.setSessionId(sessionID);
      }
      if (network != null) {
        message.setNetwork(network);
      }

      message.setLoggingContext(loggingContext);
      message.setMidCall(isMidCall);
      message.setRequest(isRequest);
      return message;
    }

    public RouteAppMessageBuilder withPayload(MessageBody payload) {
      Assert.notNull(payload, "Payload must not be null");
      this.payload = payload;
      return this;
    }

    public RouteAppMessageBuilder withHeaders(MessageHeaders headers) {
      Assert.notNull(headers, "Payload must not be null");
      this.headers = headers;
      return this;
    }

    public RouteAppMessageBuilder withContext(ExecutionContext context) {
      Assert.notNull(context, "Payload must not be null");
      this.context = context;
      return this;
    }

    public RouteAppMessageBuilder sessionId(String sessionID) {
      this.sessionID = sessionID;
      return this;
    }

    public RouteAppMessageBuilder correlationId(String correlationID) {
      this.correlationID = correlationID;
      return this;
    }

    public RouteAppMessageBuilder reqURI(String reqURI) {
      this.reqURI = reqURI;
      return this;
    }

    public RouteAppMessageBuilder callType(CallType callType) {
      this.callType = callType;
      return this;
    }

    public RouteAppMessageBuilder midCall(boolean isMidCall) {
      this.isMidCall = isMidCall;
      return this;
    }

    public RouteAppMessageBuilder request(boolean isRequest) {
      this.isRequest = isRequest;
      return this;
    }

    public RouteAppMessageBuilder loggingContext(LogContext loggingContext) {
      this.loggingContext = loggingContext;
      return this;
    }

    public RouteAppMessageBuilder network(String network) {
      this.network = network;
      return this;
    }
  }

  private RouteAppMessage() {}
}
