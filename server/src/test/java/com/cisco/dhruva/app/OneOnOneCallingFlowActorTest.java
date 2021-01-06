package com.cisco.dhruva.app;

import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.util.SIPRequestBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OneOnOneCallingFlowActorTest {
  @Test
  public void testValidCase() throws Exception {

    ExecutionContext context = new ExecutionContext();

    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(
                    SIPRequestBuilder.RequestMethod.INVITE, "dhruva@cisco.call.ciscospark.com"));
    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);
    Assert.assertTrue(OneOnOneCallingFlowActor.getFilter().test(reqMsg));
  }

  @Test
  public void testDMZValidCase() throws Exception {

    ExecutionContext context = new ExecutionContext();

    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(
                    SIPRequestBuilder.RequestMethod.INVITE, "dhruva@callingdmztest.call.wbx2.com"));
    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);
    Assert.assertTrue(OneOnOneCallingFlowActor.getFilter().test(reqMsg));
  }
}
