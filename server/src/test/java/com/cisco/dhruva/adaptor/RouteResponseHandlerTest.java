package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.common.messaging.models.RouteAppMessage;
import com.cisco.dhruva.sip.proxy.controller.DsControllerInterface;
import com.cisco.dhruva.sip.proxy.controller.DsProxyController;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class RouteResponseHandlerTest {

  @Test
  public void testRouteResponseHandler() {
    DsControllerInterface controller = Mockito.mock(DsProxyController.class);
    RouteResponseHandler handler = new RouteResponseHandler(controller);

    ExecutionContext context = new ExecutionContext();
    DsSipResponse response = new DsSipResponse();
    response.setStatusCode(200);
    response.setReasonPhrase(new DsByteString("success"));
    MessageBody payload = MessageBody.fromPayloadData(response, MessageBodyType.SIPRESPONSE);
    IDhruvaMessage message =
        RouteAppMessage.newBuilder().withContext(context).withPayload(payload).build();

    handler.onMessage(message);
  }
}
