package com.cisco.dhruva.app;

import com.cisco.dhruva.app.util.DhruvaMessageHelper;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.util.SIPRequestBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DhruvaMessageHelperTest {

  @Test
  public void testHostPortion() throws Exception {

    ExecutionContext context = new ExecutionContext();

    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(
                    SIPRequestBuilder.RequestMethod.INVITE, "supnaras@testdmz.dmz.webex.com"));
    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);

    Assert.assertEquals(
        DhruvaMessageHelper.getHostPortion(reqMsg.getReqURI()), "testdmz.dmz.webex.com");
  }

  @Test
  public void testUserPortion() throws Exception {

    ExecutionContext context = new ExecutionContext();

    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(
                    SIPRequestBuilder.RequestMethod.INVITE,
                    "supnaras.naras@testdmz.dmz.webex.com"));
    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);

    Assert.assertEquals(DhruvaMessageHelper.getUserPortion(reqMsg.getReqURI()), "supnaras.naras");
  }
}
