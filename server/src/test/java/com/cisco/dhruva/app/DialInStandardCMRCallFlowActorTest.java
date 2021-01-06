package com.cisco.dhruva.app;

import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.util.SIPRequestBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DialInStandardCMRCallFlowActorTest {

  @Test
  public void testDMZValidCase() throws Exception {

    ExecutionContext context = new ExecutionContext();

    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(
                    SIPRequestBuilder.RequestMethod.INVITE, "cptest_wbxuser@cptest.dmz.webex.com"));
    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);
    Assert.assertTrue(DialInStandardCMRCallFlowActor.getFilter().test(reqMsg));
  }

  @Test
  public void testValidCase() throws Exception {

    ExecutionContext context = new ExecutionContext();

    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(
                    SIPRequestBuilder.RequestMethod.INVITE, "supnaras@cisco.webex.com"));
    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);
    Assert.assertTrue(DialInStandardCMRCallFlowActor.getFilter().test(reqMsg));
  }

  @Test
  public void testInvalidHost() throws Exception {

    ExecutionContext context = new ExecutionContext();

    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE, "supnaras@webex.com"));
    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);
    Assert.assertFalse(DialInStandardCMRCallFlowActor.getFilter().test(reqMsg));
  }

  @Test
  public void testInvalidUser() throws Exception {

    ExecutionContext context = new ExecutionContext();

    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(
                    SIPRequestBuilder.RequestMethod.INVITE, "supn&&aras@alpha.webex.com"));
    MessageBody payload = MessageBody.fromPayloadData(request, MessageBodyType.SIPREQUEST);
    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);
    Assert.assertFalse(DialInStandardCMRCallFlowActor.getFilter().test(reqMsg));
  }
}
