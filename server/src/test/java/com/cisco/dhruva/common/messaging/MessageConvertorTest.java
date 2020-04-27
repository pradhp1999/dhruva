package com.cisco.dhruva.common.messaging;

import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.common.messaging.models.RouteAppMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.util.SIPRequestBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageConvertorTest {

  @Test
  public void validateSIPToDhruvaMessageConversion()
      throws DsSipParserListenerException, DsSipParserException {
    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    IDhruvaMessage msg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, new ExecutionContext());
    Assert.assertNotNull(msg);
    Assert.assertTrue(msg instanceof DhruvaMessageImpl);
  }

  @Test(expectedExceptions = {NullPointerException.class})
  public void shouldFailSIPToDhruvaMessageWithNullContext()
      throws DsSipParserListenerException, DsSipParserException {
    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    IDhruvaMessage msg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, null);
  }

  @Test(expectedExceptions = {NullPointerException.class})
  public void shouldFailSIPToDhruvaMessageWithInvalidInput() {
    DsSipRequest request = null;
    IDhruvaMessage msg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, new ExecutionContext());
  }

  @Test
  public void validateDhruvaToSIPRequestConversion()
      throws DsSipParserListenerException, DsSipParserException {
    ExecutionContext context = new ExecutionContext();
    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    MessageBody payload = MessageBody.fromPayloadData(request, MessageBodyType.SIPREQUEST);
    IDhruvaMessage message =
        RouteAppMessage.newBuilder().withContext(context).withPayload(payload).build();

    DsSipMessage msg = MessageConvertor.convertDhruvaMessageToSipMessage(message);
    Assert.assertTrue(msg instanceof DsSipRequest);
  }
}
