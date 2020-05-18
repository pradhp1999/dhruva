package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.common.messaging.models.RouteAppMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;

public class RouteResponseHandlerTest {

  @Test(
      description = "sending response via proxy adaptor when controller object is not set",
      expectedExceptions = DhruvaException.class)
  public void testRouteResponseHandler() {
    ProxyAdaptor adaptor = Mockito.mock(ProxyAdaptor.class);
    RouteResponseHandler handler = new RouteResponseHandler(adaptor);

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
