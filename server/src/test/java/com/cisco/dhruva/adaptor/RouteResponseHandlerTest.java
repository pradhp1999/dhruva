package com.cisco.dhruva.adaptor;

import static com.cisco.dhruva.common.CommonContext.PROXY_ROUTE_RESULT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.app.Destination;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBody;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.common.messaging.models.RouteAppMessage;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.sip.bean.SIPListenPoint;
import com.cisco.dhruva.sip.controller.DsProxyController;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SIPRequestBuilder;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RouteResponseHandlerTest {
  private DsNetwork dsNetwork;
  private SIPListenPoint defaultListenPoint;

  @BeforeClass
  void init() throws Exception {
    try {
      dsNetwork = DsNetwork.getNetwork("Default");
      DsControllerConfig.addListenInterface(
          dsNetwork,
          InetAddress.getByName("127.0.0.1"),
          5060,
          Transport.UDP,
          InetAddress.getByName("127.0.0.1"),
          false);

    } catch (DsInconsistentConfigurationException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    }

    defaultListenPoint =
        new SIPListenPoint.SIPListenPointBuilder()
            .setName(dsNetwork.getName())
            .setHostIPAddress("127.0.0.1")
            .setTransport(Transport.UDP)
            .setPort(5060)
            .setRecordRoute(false)
            .build();
  }

  @Test(
      description = "sending response via proxy adaptor when controller object is not set",
      expectedExceptions = DhruvaException.class)
  public void testRouteResponseHandler() {
    ProxyAdaptor adaptor = mock(ProxyAdaptor.class);
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

  @Test
  public void testOnMessageSipRequest() throws Exception {
    ProxyAdaptor proxyAdaptor = mock(ProxyAdaptor.class);
    RouteResponseHandler responseHandler = new RouteResponseHandler(proxyAdaptor);
    DsProxyController proxyInterface = mock(DsProxyController.class);
    ExecutionContext context = new ExecutionContext();

    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);

    List<SIPListenPoint> listenPointList = new ArrayList<>();
    listenPointList.add(defaultListenPoint);
    when(dhruvaSIPConfigProperties.getListeningPoints()).thenReturn(listenPointList);
    DsSipRequest request =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder()
                .getRequestAsString(
                    SIPRequestBuilder.RequestMethod.INVITE, "dhruva@cisco.call.ciscospark.com"));

    Destination destination =
        new Destination(Destination.DestinationType.SRV, "1.1.1.1", dsNetwork.getName());

    context.set(PROXY_ROUTE_RESULT, destination);

    IDhruvaMessage reqMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            request, MessageBodyType.SIPREQUEST, context);

    when(proxyAdaptor.getProxyController()).thenReturn(proxyInterface);
    doNothing()
        .when(proxyInterface)
        .proxyTo(any(Location.class), any(DsSipRequest.class), any(ProxyAdaptor.class));
    responseHandler.onMessage(reqMsg);
    // Assertions
    verify(proxyInterface)
        .proxyTo(any(Location.class), any(DsSipRequest.class), any(ProxyAdaptor.class));
    DsSipRouteHeader expectedRouteHeader =
        new DsSipRouteHeader(
            ("<sip:"
                    + "1.1.1.1"
                    + ";lr"
                    + ";transport="
                    + Transport.UDP.toString().toLowerCase()
                    + ">")
                .getBytes());
    DsSipHeaderList routeHeaderList = request.getHeaders(DsSipConstants.ROUTE);
    Assert.assertEquals(routeHeaderList.getFirst().toString(), expectedRouteHeader.toString());
  }
}
