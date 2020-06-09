package com.cisco.dhruva.common;

import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.common.messaging.models.RouteAppMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.util.SIPRequestBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RouteAppMessageTest {

  private IDhruvaMessage message;

  @Test
  public void testRouteAppMessageBuilder()
      throws DsSipParserListenerException, DsSipParserException {
    ExecutionContext context = new ExecutionContext();
    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    MessageBody payload = MessageBody.fromPayloadData(request, MessageBodyType.SIPREQUEST);
    message = RouteAppMessage.newBuilder().withContext(context).withPayload(payload).build();

    Assert.assertEquals(message.getMessageBody().getPayloadData(), request);
    Assert.assertEquals(message.getContext(), context);
    Assert.assertNotNull(message.getHeaders());
    Assert.assertNull(message.getCallType());
    Assert.assertNull(message.getCorrelationId());
    Assert.assertNull(message.getReqURI());
    Assert.assertNull(message.getSessionId());
  }

  @Test
  public void testRouteAppMessageBuilderWithAdditionalFields()
      throws DsSipParserListenerException, DsSipParserException {
    ExecutionContext context = new ExecutionContext();
    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    MessageBody payload = MessageBody.fromPayloadData(request, MessageBodyType.SIPREQUEST);
    message =
        RouteAppMessage.newBuilder()
            .withContext(context)
            .withPayload(payload)
            .sessionId("testSession")
            .reqURI("sip:test@webex.com")
            .correlationId("ABCD")
            .callType(CallType.SIP)
            .build();

    Assert.assertEquals(message.getMessageBody().getPayloadData(), request);
    Assert.assertEquals(message.getContext(), context);
    Assert.assertNotNull(message.getHeaders());
    Assert.assertEquals(message.getCallType(), CallType.SIP);
    Assert.assertEquals(message.getCorrelationId(), "ABCD");
    Assert.assertEquals(message.getReqURI(), "sip:test@webex.com");
    Assert.assertEquals(message.getSessionId(), "testSession");
  }
}
