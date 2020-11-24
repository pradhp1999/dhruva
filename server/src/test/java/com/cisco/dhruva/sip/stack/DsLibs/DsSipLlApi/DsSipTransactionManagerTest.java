/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.cac.SIPSessions;
import com.cisco.dhruva.sip.controller.DsREControllerFactory;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.LMAUtil;
import com.cisco.dhruva.util.SIPMessageGenerator;
import com.cisco.dhruva.util.SIPRequestBuilder;
import com.cisco.dhruva.util.SIPRequestBuilder.RequestMethod;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.*;

public class DsSipTransactionManagerTest {

  private DsSipTransactionManager sipTransactionManager;
  private DsSipTransportLayer transportLayer;
  private DsSipStrayMessageInterface strayMessageInterface;
  private DsSipRequestInterface requestInterface;
  private DsSipTransactionEventInterface transactionEventInterface;
  private DsBindingInfo incomingMessageBindingInfo;
  private InetAddress localAddress;
  private InetAddress remoteAddress;
  private int localPort, remotePort;
  DsNetwork dsNetwork, externalIpEnabledNetwork;
  private DsSipRouteFixInterface rfi;
  @InjectMocks private MetricService metricService;
  @InjectMocks private LMAUtil lmaUtil;

  @BeforeClass
  public void init() throws Exception {
    metricService = mock(MetricService.class);
    transportLayer = mock(DsSipTransportLayer.class);
    strayMessageInterface = mock(DsSipStrayMessageInterface.class);
    requestInterface = mock(DsSipRequestInterface.class);
    transactionEventInterface = mock(DsSipTransactionEventInterface.class);
    localAddress = InetAddress.getByName("127.0.0.1");
    remoteAddress = InetAddress.getByName("127.0.0.1");
    localPort = 5060;
    remotePort = 5070;
    incomingMessageBindingInfo =
        new DsBindingInfo(localAddress, localPort, localAddress, remotePort, Transport.UDP);
    dsNetwork = DsNetwork.getNetwork("Default");
    externalIpEnabledNetwork = DsNetwork.getNetwork("External_IP_enabled");
    incomingMessageBindingInfo.setNetwork(dsNetwork);
    rfi = new DsREControllerFactory();

    DsSipServerTransactionImpl.setThreadPoolExecutor(
        (ThreadPoolExecutor) Executors.newFixedThreadPool(1));

    try {
      DsSipTransactionFactory transactionFactory = new DsSipDefaultTransactionFactory();
      sipTransactionManager =
          new DsSipTransactionManager(transportLayer, requestInterface, transactionFactory);
    } catch (DsException e) {
      Assert.fail(
          "DsSipTransactionManagerTest DsSipTransactionManager " + "object creation failed " + e);
    }

    DsSipTransactionManager.setProxyServerMode(true);
    sipTransactionManager.setStrayMessageInterface(strayMessageInterface);
    sipTransactionManager.setTransactionEventInterface(transactionEventInterface);

    try {
      DsControllerConfig.addListenInterface(
          dsNetwork,
          InetAddress.getByName("127.0.0.1"),
          5060,
          Transport.UDP,
          InetAddress.getByName("127.0.0.1"),
          false);

      DsControllerConfig.addRecordRouteInterface(
          InetAddress.getByName("127.0.0.1"), 5060, Transport.UDP, dsNetwork);
      DsControllerConfig.addRecordRouteInterface(
          InetAddress.getByName("127.0.0.1"), 5061, Transport.UDP, externalIpEnabledNetwork);
    } catch (DsInconsistentConfigurationException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    }

    MockitoAnnotations.initMocks(this);
  }

  @AfterClass
  void cleanUp() {
    DsSipTransactionManager.setSmp_theSingleton(null);
  }

  @AfterMethod
  void cleanup() throws DsException {
    SIPSessions.resetActiveSessions();
    transportLayer = mock(DsSipTransportLayer.class);
    requestInterface = mock(DsSipRequestInterface.class);
    strayMessageInterface = mock(DsSipStrayMessageInterface.class);
    transactionEventInterface = mock(DsSipTransactionEventInterface.class);
    sipTransactionManager.setStrayMessageInterface(strayMessageInterface);
    sipTransactionManager.setTransactionEventInterface(transactionEventInterface);
    sipTransactionManager.setRequestInterface(requestInterface, null);
    sipTransactionManager.setTransportLayer(transportLayer);
    sipTransactionManager.setM_transactionTable(new DsSipTransactionTable());
    DsNetwork.setDhruvaConfigProperties(null);
  }

  private void setDhruvaProp() {
    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);
  }

  @Test(
      description =
          "Testing the Invite Processing by the Transaction Manager, "
              + "checks if Message is parsed ,Session is created ,SessionId header"
              + " is added,Received and Rport are added and Message is "
              + "forwarded to requestInterface ")
  public void testInviteProcessingInTransactionManager() throws DsException, IOException {

    setDhruvaProp();
    String callId = "05e3b66d495da4d4f172a99384ce24";
    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt", callId).getBytes();

    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    ArgumentCaptor<DsSipServerTransactionIImpl> argumentCaptor =
        ArgumentCaptor.forClass(DsSipServerTransactionIImpl.class);

    Assert.assertNull(SIPSessions.getActiveSession(callId));

    // Configure Transport Layer for sending Response from Transaction manager , here its 100 Trying
    DsSipConnection responseConnection = mock(DsSipConnection.class);
    when(responseConnection.getBindingInfo()).thenReturn(incomingMessageBindingInfo);
    when(responseConnection.getTransportType()).thenReturn(Transport.UDP);
    ArgumentCaptor<DsSipMessage> argumentCaptorTryingResponse =
        ArgumentCaptor.forClass(DsSipMessage.class);
    when(transportLayer.getConnection(
            incomingMessageBindingInfo.getNetwork(),
            localAddress,
            localPort,
            remoteAddress,
            remotePort,
            Transport.UDP,
            true))
        .thenReturn(responseConnection);

    // unit under test
    sipTransactionManager.processMessageBytes(sipMessageBytes);

    // Check the 100 Trying
    verify(responseConnection).send(argumentCaptorTryingResponse.capture());

    DsSipResponse responseReceivedAtConnection =
        (DsSipResponse) argumentCaptorTryingResponse.getValue();
    Assert.assertNotNull(responseReceivedAtConnection);
    Assert.assertEquals(responseReceivedAtConnection.getMethodID(), 1);
    Assert.assertEquals(responseReceivedAtConnection.getStatusCode(), 100);
    Assert.assertEquals(responseReceivedAtConnection.getReasonPhrase().toString(), "Trying");
    Assert.assertNotNull(
        responseReceivedAtConnection.getHeader(DsByteString.newInstance("Session-Id")));

    // Check If Invite is forwarded to request interface , and Check Transaction manager functions
    verify(requestInterface).request(argumentCaptor.capture());
    DsSipRequest requestReceivedAtInterface = argumentCaptor.getValue().getRequest();
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) requestReceivedAtInterface.getViaHeader();

    Assert.assertNotNull(requestReceivedAtInterface);
    Assert.assertEquals(requestReceivedAtInterface.getMethod(), DsByteString.newInstance("INVITE"));
    Assert.assertEquals(
        requestReceivedAtInterface.getHeader(DsByteString.newInstance("Max-Forwards")).toString(),
        "Max-Forwards: 13\r\n");
    Assert.assertNotNull(
        requestReceivedAtInterface.getHeader(DsByteString.newInstance("Session-Id")));
    Assert.assertNotNull(SIPSessions.getActiveSession(callId));
    Assert.assertEquals(requestReceivedAtInterface.getBindingInfo(), incomingMessageBindingInfo);
    Assert.assertEquals(
        sipViaHeader.getReceived().toString(),
        incomingMessageBindingInfo.getLocalAddress().getHostAddress());
    Assert.assertEquals(sipViaHeader.getRPort(), incomingMessageBindingInfo.getRemotePort());

  }

  @Test(
      description =
          "Testing the Options Processing by the Transaction Manager, "
              + "checks if Message is parsed ,Session is created ,SessionId header"
              + " is added,And 200 OK is sent to the Options message, Also"
              + "Checks Options is not forwarded")
  public void testOptionProcessingInTransactionManager() throws Exception {

    setDhruvaProp();
    String callId;
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    byte[] messagebytes =
        SIPRequestBuilder.createRequest(sipRequestBuilder.getRequestAsString(RequestMethod.OPTIONS))
            .toByteArray();
    callId = sipRequestBuilder.getCallId();
    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    ArgumentCaptor<DsSipServerTransactionIImpl> argumentCaptor =
        ArgumentCaptor.forClass(DsSipServerTransactionIImpl.class);

    Assert.assertNull(SIPSessions.getActiveSession(callId));

    // Configure Transport Layer for sending Response from Transaction manager , here its 100 Trying
    DsSipConnection responseConnection = mock(DsSipConnection.class);
    when(responseConnection.getBindingInfo()).thenReturn(incomingMessageBindingInfo);
    when(responseConnection.getTransportType()).thenReturn(Transport.UDP);
    ArgumentCaptor<DsSipResponse> argumentCaptor200Response =
        ArgumentCaptor.forClass(DsSipResponse.class);

    when(transportLayer.getConnection(
            incomingMessageBindingInfo.getNetwork(),
            localAddress,
            localPort,
            remoteAddress,
            remotePort,
            Transport.UDP,
            true))
        .thenReturn(responseConnection);

    // unit under test
    sipTransactionManager.processMessageBytes(sipMessageBytes);

    // Check the 100 Trying
    verify(responseConnection).send(argumentCaptor200Response.capture());

    DsSipResponse responseReceivedAtConnection = argumentCaptor200Response.getValue();
    Assert.assertNotNull(responseReceivedAtConnection);
    Assert.assertEquals(responseReceivedAtConnection.getMethodID(), 5);
    Assert.assertEquals(responseReceivedAtConnection.getStatusCode(), 200);
    Assert.assertEquals(responseReceivedAtConnection.getReasonPhrase().toString(), "Ok");
    Assert.assertNotNull(
        responseReceivedAtConnection.getHeader(DsByteString.newInstance("Session-Id")));

    // Check If Invite is forwarded to request interface , and Check Transaction manager functions
    verify(requestInterface, times(0)).request(argumentCaptor.capture());

  }

  @Test(
      description =
          "Testing the Response Processing by the Transaction Manager "
              + "Here the response is processed as stray response")
  public void testResponseProcessingInTransactionManager() throws DsException, IOException {

    setDhruvaProp();
    String callId;
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipResponse sipResponse = sipRequestBuilder.getResponse(200);
    sipResponse.removeHeader(DsByteString.newInstance("Session-Id"));
    byte[] messagebytes = sipResponse.toByteArray();
    callId = sipRequestBuilder.getCallId();
    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);
    // unit under test
    sipTransactionManager.processMessageBytes(sipMessageBytes);

    // Check If 200 is forwarded to stray interface , and Check Transaction manager functions
    verify(strayMessageInterface, times(1)).strayResponse(argumentCaptor.capture());

    DsSipResponse responseReceivedAtConnection = argumentCaptor.getValue();
    Assert.assertNotNull(responseReceivedAtConnection);
    Assert.assertEquals(responseReceivedAtConnection.getMethodID(), 1);
    Assert.assertEquals(responseReceivedAtConnection.getStatusCode(), 200);
    Assert.assertEquals(responseReceivedAtConnection.getReasonPhrase().toString(), "OK");

  }

  @Test(
      description =
          "Testing the ACK Processing by the Transaction Manager, "
              + "ACK should be sent to the strayInterface")
  public void testACKProcessingInTransactionManager() throws Exception {

    setDhruvaProp();
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    byte[] messagebytes = sipRequestBuilder.getRequestAsString(RequestMethod.ACK, true).getBytes();
    String callId = sipRequestBuilder.getCallId();

    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    ArgumentCaptor<DsSipAckMessage> argumentCaptor = ArgumentCaptor.forClass(DsSipAckMessage.class);

    Assert.assertNull(SIPSessions.getActiveSession(callId));

    sipTransactionManager.processMessageBytes(sipMessageBytes);

    verify(strayMessageInterface).strayAck((argumentCaptor.capture()));
    DsSipAckMessage requestReceivedAtInterface = argumentCaptor.getValue();
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) requestReceivedAtInterface.getViaHeader();

    Assert.assertNotNull(requestReceivedAtInterface);
    Assert.assertEquals(requestReceivedAtInterface.getMethod(), DsByteString.newInstance("ACK"));
    Assert.assertEquals(
        requestReceivedAtInterface.getHeader(DsByteString.newInstance("Max-Forwards")).toString(),
        "Max-Forwards: 69\r\n");
    Assert.assertEquals(requestReceivedAtInterface.getBindingInfo(), incomingMessageBindingInfo);
    Assert.assertEquals(
        sipViaHeader.getReceived().toString(),
        incomingMessageBindingInfo.getLocalAddress().getHostAddress());

  }

  @DataProvider
  public Object[] getRouteHeader() {

    RouteDataProvider rProvider1 =
        new RouteDataProvider("<sip:rr,n=Default@127.0.0.1:5060;transport=udp;lr>", false, true);
    RouteDataProvider rProvider2 =
        new RouteDataProvider(
            "<sip:rr,n=External_IP_enabled@1.1.1.1:5061;transport=udp;lr>", false, true);
    RouteDataProvider rProvider3 =
        new RouteDataProvider(
            "<sip:rr,n=External_IP_enabled@dhruva.sjc.webex.com:5061;transport=udp;lr>",
            false,
            true);
    RouteDataProvider rProvider4 =
        new RouteDataProvider(
            "<sip:rr,n=unrecognizedNetwork@1.2.3.4:5065;transport=udp;lr>", true, true);
    RouteDataProvider rProvider5 =
        new RouteDataProvider("<sip:rr,n=Default@127.0.0.1:5060;transport=udp;lr>", false, false);
    RouteDataProvider rProvider6 =
        new RouteDataProvider(
            "<sip:rr,n=External_IP_enabled@127.0.0.1:5061;transport=udp;lr>", false, false);
    RouteDataProvider rProvider7 =
        new RouteDataProvider(
            "<sip:rr,n=unrecognizedNetwork@1.2.3.4:5065;transport=udp;lr>", true, false);

    RouteDataProvider[][] inputs =
        new RouteDataProvider[][] {
          {rProvider1},
          {rProvider2},
          {rProvider3},
          {rProvider4},
          {rProvider5},
          {rProvider6},
          {rProvider7}
        };
    return inputs;
  }

  @Test(
      description =
          "Testing the ACK Processing by the Transaction Manager, "
              + "ACK should be processed with Route fix interface. ",
      dataProvider = "getRouteHeader")
  public void testACKProcessingInTransactionManagerWithRouteFixInterface(
      RouteDataProvider rProvider) throws Exception {

    try {
      DsControllerConfig.addListenInterface(
          externalIpEnabledNetwork,
          InetAddress.getByName("127.0.0.1"),
          5061,
          Transport.UDP,
          InetAddress.getByName("127.0.0.1"),
          true);

    } catch (DsInconsistentConfigurationException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    }

    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);

    sipTransactionManager.setRouteFixInterface(rfi);
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    String callId = sipRequestBuilder.getCallId();

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.ACK));
    DsSipRouteHeader cpRoute = new DsSipRouteHeader(rProvider.cpRouteToAdd.getBytes());
    sipRequest.addHeader(cpRoute, true);
    System.out.println("Route in incoming ACK: " + cpRoute.toString());
    byte[] messagebytes = sipRequest.toByteArray();

    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    ArgumentCaptor<DsSipAckMessage> argumentCaptor = ArgumentCaptor.forClass(DsSipAckMessage.class);

    Assert.assertNull(SIPSessions.getActiveSession(callId));

    doReturn(rProvider.isHostPortEnabled).when(dhruvaSIPConfigProperties).isHostPortEnabled();
    // when(dhruvaSIPConfigProperties.isHostPortEnabled()).thenReturn(rProvider.isHostPortEnabled);

    sipTransactionManager.processMessageBytes(sipMessageBytes);

    verify(strayMessageInterface).strayAck((argumentCaptor.capture()));
    DsSipAckMessage requestReceivedAtInterface = argumentCaptor.getValue();

    System.out.println("Processed ACK message : " + requestReceivedAtInterface.toString());

    Assert.assertNotNull(requestReceivedAtInterface);
    Assert.assertEquals(requestReceivedAtInterface.getMethod(), DsByteString.newInstance("ACK"));

    DsSipRouteHeader topRouteHeader =
        (DsSipRouteHeader) requestReceivedAtInterface.getHeader(new DsByteString("Route"));

    if (rProvider.topRouteHeaderMatch)
      // when route header contains a network that CP does not recognize. Then, that route is not
      // removed
      Assert.assertEquals(topRouteHeader.toString(), cpRoute.toString());
    else {
      // Top-most route is recognised by CP and removed
      Assert.assertNotEquals(topRouteHeader.toString(), cpRoute.toString());
    }

    sipTransactionManager.setRouteFixInterface(null);
  }

  @Test(
      description =
          "Testing the Cancel Processing by the Transaction Manager, "
              + "Cancel is sent for an non existing transaction , so Transaction "
              + "Manager should process it as stray cancel")
  public void testCancelProcessingInTransactionManagerWithInviteTransactionWhichIsNotStarted()
      throws Exception {
    // Send Invite first
    setDhruvaProp();
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    byte[] messagebytes = sipRequestBuilder.getRequestAsString(RequestMethod.INVITE).getBytes();
    String callId = sipRequestBuilder.getCallId();
    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    Assert.assertNull(SIPSessions.getActiveSession(callId));

    DsSipConnection responseConnection = mock(DsSipConnection.class);

    when(responseConnection.getBindingInfo()).thenReturn(incomingMessageBindingInfo);
    when(responseConnection.getTransportType()).thenReturn(Transport.UDP);
    when(transportLayer.getConnection(
            incomingMessageBindingInfo.getNetwork(),
            localAddress,
            localPort,
            remoteAddress,
            remotePort,
            Transport.UDP,
            true))
        .thenReturn(responseConnection);

    // Send Invite first
    sipTransactionManager.processMessageBytes(sipMessageBytes);

    // Send Cancel for Invite Transaction
    sipRequestBuilder = new SIPRequestBuilder();
    messagebytes = sipRequestBuilder.getRequestAsString(RequestMethod.CANCEL).getBytes();

    callId = sipRequestBuilder.getCallId();

    sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    ArgumentCaptor<DsSipCancelMessage> argumentCaptor =
        ArgumentCaptor.forClass(DsSipCancelMessage.class);

    Assert.assertNotNull(SIPSessions.getActiveSession(callId));

    sipTransactionManager.processMessageBytes(sipMessageBytes);

    verify(strayMessageInterface, times(0)).strayCancel(any());

  }

  @Test(
      description =
          "Testing the Cancel Processing by the Transaction Manager, "
              + "Cancel is sent for an non existing transaction , so Transaction "
              + "Manager should process it as stray cancel")
  public void testCancelProcessingInTransactionManagerWithNoInviteTransaction() throws Exception {

    setDhruvaProp();
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    byte[] messagebytes = sipRequestBuilder.getRequestAsString(RequestMethod.CANCEL).getBytes();
    String callId = sipRequestBuilder.getCallId();

    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    ArgumentCaptor<DsSipCancelMessage> argumentCaptor =
        ArgumentCaptor.forClass(DsSipCancelMessage.class);

    Assert.assertNull(SIPSessions.getActiveSession(callId));

    sipTransactionManager.processMessageBytes(sipMessageBytes);

    verify(strayMessageInterface).strayCancel((argumentCaptor.capture()));
    DsSipCancelMessage requestReceivedAtInterface = argumentCaptor.getValue();
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) requestReceivedAtInterface.getViaHeader();

    Assert.assertNotNull(requestReceivedAtInterface);
    Assert.assertEquals(requestReceivedAtInterface.getMethod(), DsByteString.newInstance("CANCEL"));

    // stray Cancel, message is dropped so Max-Forwards is not decremented
    Assert.assertEquals(
        requestReceivedAtInterface.getHeader(DsByteString.newInstance("Max-Forwards")).toString(),
        "Max-Forwards: 70\r\n");
    Assert.assertEquals(requestReceivedAtInterface.getBindingInfo(), incomingMessageBindingInfo);
    Assert.assertEquals(sipViaHeader.getRPort(), incomingMessageBindingInfo.getRemotePort());

  }

  @Test(
      description =
          "Testing the Invite Processing by the Transaction Manager, "
              + "Invite doesnot have From header , so Transaction Manager should respond with 4xx")
  public void testInviteProcessingInTransactionManagerWithInviteMessageHavingNoFromHeader()
      throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(RequestMethod.INVITE));

    // Removing the from header
    sipRequest.removeHeader(new DsByteString("From"));

    String callId = sipRequest.getCallId().toString();

    DsSipConnection responseConnection = mock(DsSipConnection.class);

    when(responseConnection.getBindingInfo()).thenReturn(incomingMessageBindingInfo);
    when(responseConnection.getTransportType()).thenReturn(Transport.UDP);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);

    when(transportLayer.getConnection(
            incomingMessageBindingInfo.getNetwork(),
            localAddress,
            0,
            remoteAddress,
            remotePort,
            Transport.UDP))
        .thenReturn(responseConnection);

    SipMessageBytes sipMessageBytes =
        new SipMessageBytes(sipRequest.toByteArray(), incomingMessageBindingInfo);
    Assert.assertNull(SIPSessions.getActiveSession(callId));

    sipTransactionManager.processMessageBytes(sipMessageBytes);

    verify(responseConnection).send(argumentCaptor.capture());
    DsSipResponse responseReceivedAtConnection = argumentCaptor.getValue();
    Assert.assertNotNull(responseReceivedAtConnection);
    Assert.assertEquals(responseReceivedAtConnection.getMethodID(), 1);
    Assert.assertEquals(responseReceivedAtConnection.getStatusCode(), 400);
    Assert.assertEquals(responseReceivedAtConnection.getReasonPhrase().toString(), "Bad Request");
    Assert.assertEquals(
        responseReceivedAtConnection.getBody().toString(),
        "Can't build transaction key: no From data (user/host/port/uridata)");

    Assert.assertNull(SIPSessions.getActiveSession(callId));
    Assert.assertEquals(responseReceivedAtConnection.getBindingInfo(), incomingMessageBindingInfo);

  }

  @Test(
      description =
          "Testing the Invite Retransmission Processing by the Transaction Manager, "
              + "Transaction Mabager should silently drop the second Message")
  public void testInviteProcessingInTransactionManagerWithReTransmittedInvite()
      throws DsException, IOException {

    setDhruvaProp();
    String callId = "05e3b66d495da4d4f172a99384ce24";
    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt", callId).getBytes();

    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    // Configure Transport Layer for sending Response from Transaction manager , here its 100 Trying
    DsSipConnection responseConnection = mock(DsSipConnection.class);
    when(responseConnection.getBindingInfo()).thenReturn(incomingMessageBindingInfo);
    when(responseConnection.getTransportType()).thenReturn(Transport.UDP);
    ArgumentCaptor<byte[]> argumentCaptorTryingResponse = ArgumentCaptor.forClass(byte[].class);
    when(transportLayer.getConnection(
            incomingMessageBindingInfo.getNetwork(),
            localAddress,
            localPort,
            remoteAddress,
            remotePort,
            Transport.UDP,
            true))
        .thenReturn(responseConnection);

    ArgumentCaptor<DsSipServerTransactionIImpl> argumentCaptor =
        ArgumentCaptor.forClass(DsSipServerTransactionIImpl.class);

    Assert.assertNull(SIPSessions.getActiveSession(callId));

    sipTransactionManager.processMessageBytes(sipMessageBytes);

    verify(requestInterface).request(argumentCaptor.capture());
    DsSipRequest requestReceivedAtInterface = argumentCaptor.getValue().getRequest();
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) requestReceivedAtInterface.getViaHeader();

    Assert.assertNotNull(requestReceivedAtInterface);
    Assert.assertEquals(requestReceivedAtInterface.getMethod(), DsByteString.newInstance("INVITE"));
    Assert.assertEquals(
        requestReceivedAtInterface.getHeader(DsByteString.newInstance("Max-Forwards")).toString(),
        "Max-Forwards: 13\r\n");
    Assert.assertNotNull(
        requestReceivedAtInterface.getHeader(DsByteString.newInstance("Session-Id")));
    Assert.assertNotNull(SIPSessions.getActiveSession(callId));
    Assert.assertEquals(requestReceivedAtInterface.getBindingInfo(), incomingMessageBindingInfo);
    Assert.assertEquals(
        sipViaHeader.getReceived().toString(),
        incomingMessageBindingInfo.getLocalAddress().getHostAddress());
    Assert.assertEquals(sipViaHeader.getRPort(), incomingMessageBindingInfo.getRemotePort());

    // Sending the same message Again , this message should be silently dropped
    sipTransactionManager.processMessageBytes(sipMessageBytes);

    // verify that the Invite was dropped by checking if the request reached the requestInterface
    verify(requestInterface, atMost(1)).request(argumentCaptor.capture());

  }

  @Test(
      description =
          "Testing the Invite Processing by the Transaction Manager when the Invite is corrupted"
              + "Here Transaction Manager should Respond with 4xx on the Rport and remoteIp")
  public void testInviteProcessingInTransactionManagerWithInvalidBytes()
      throws DsException, IOException {

    setDhruvaProp();
    String callId = "05e3b66d495da4d4f172a99384ce24";
    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt", callId).getBytes();

    DsSipConnection responseConnection = mock(DsSipConnection.class);

    when(responseConnection.getBindingInfo()).thenReturn(incomingMessageBindingInfo);
    when(responseConnection.getTransportType()).thenReturn(Transport.UDP);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);

    when(transportLayer.getConnection(
            incomingMessageBindingInfo.getNetwork(),
            localAddress,
            localPort,
            remoteAddress,
            remotePort,
            Transport.UDP,
            true))
        .thenReturn(responseConnection);

    // corrupting message
    messagebytes[0] = 'R';

    SipMessageBytes sipMessageBytes = new SipMessageBytes(messagebytes, incomingMessageBindingInfo);

    Assert.assertNull(SIPSessions.getActiveSession(callId));

    sipTransactionManager.processMessageBytes(sipMessageBytes);

    verify(responseConnection).send(argumentCaptor.capture());
    DsSipResponse responseReceivedAtConnection = argumentCaptor.getValue();
    Assert.assertNotNull(responseReceivedAtConnection);
    Assert.assertEquals(responseReceivedAtConnection.getMethodID(), 1);
    Assert.assertEquals(responseReceivedAtConnection.getStatusCode(), 400);
    Assert.assertEquals(responseReceivedAtConnection.getReasonPhrase().toString(), "Bad Request");
    Assert.assertEquals(
        responseReceivedAtConnection.getBody().toString(),
        "Method Type and CSeq Type Do Not Match\r\n");
    Assert.assertNull(SIPSessions.getActiveSession(callId));
    Assert.assertEquals(responseReceivedAtConnection.getBindingInfo(), incomingMessageBindingInfo);

  }

  @Test(
      description =
          "Testing the Received Parameter , received is added if via host and remoteIp differs"
              + "Here its deifferent so received should be added")
  public void testAddReceiveParameterWithRemoteIpDifferentThanReceivedIp()
      throws DsSipParserListenerException, DsSipParserException, UnknownHostException {
    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt").getBytes();
    InetAddress receivedAddress = InetAddress.getByName("127.0.0.1");

    DsSipRequest sipRequest = (DsSipRequest) DsSipMessage.createMessage(messagebytes, true, true);
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();
    Assert.assertNull(sipViaHeader.getReceived());

    sipTransactionManager.addReceiveParameter(sipRequest, receivedAddress);

    sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();
    Assert.assertEquals(sipViaHeader.getReceived().toString(), receivedAddress.getHostAddress());
  }

  @Test(
      description =
          "Testing the Received Parameter , received is added if via host and remoteIp differs"
              + "Here its deifferent so received should be added")
  public void testAddReceiveParameterWithRemoteIPSameAsReceivedIP()
      throws DsSipParserListenerException, DsSipParserException, UnknownHostException {
    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt").getBytes();
    InetAddress receivedAddress = InetAddress.getByName("127.0.0.1");

    DsSipRequest sipRequest = (DsSipRequest) DsSipMessage.createMessage(messagebytes, true, true);
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();
    Assert.assertNull(sipViaHeader.getReceived());
    sipViaHeader.setHost(DsByteString.newInstance(receivedAddress.getHostAddress()));

    sipTransactionManager.addReceiveParameter(sipRequest, receivedAddress);
    sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();

    Assert.assertNull(sipViaHeader.getReceived());
  }

  @Test(
      description =
          "Testing the Received Parameter , received is added if via host and remoteIp differs"
              + "Here receivedIp is null so received should be added")
  public void testAddReceiveParameterWithNullReceivedIP()
      throws DsSipParserListenerException, DsSipParserException, UnknownHostException {
    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt").getBytes();

    DsSipRequest sipRequest = (DsSipRequest) DsSipMessage.createMessage(messagebytes, true, true);
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();
    Assert.assertNull(sipViaHeader.getReceived());

    sipTransactionManager.addReceiveParameter(sipRequest, null);
    sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();

    Assert.assertNull(sipViaHeader.getReceived());
  }

  @Test(
      description =
          "If the via has rport parameter set , then the port value is added to via,"
              + "In this test rport is present , so Received port should be set")
  public void testAddRPortParameter() throws DsSipParserListenerException, DsSipParserException {

    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt").getBytes();

    DsSipRequest sipRequest = (DsSipRequest) DsSipMessage.createMessage(messagebytes, true, true);
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();
    sipViaHeader.removeRPort();
    int receivedPort = 5061;
    Assert.assertFalse(sipViaHeader.isRPortPresent());
    sipViaHeader.setRPort();

    sipTransactionManager.addRPortParameter(sipRequest, receivedPort);

    sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();

    Assert.assertTrue(sipViaHeader.isRPortPresent());
    Assert.assertEquals(sipViaHeader.getRPort(), receivedPort);
  }

  @Test(
      description =
          "If the via has rport parameter set , then the port value is added to via,"
              + "In this test rport is not present in Via , so Received port should not be set")
  public void testAddRPortParameterWithoutRportInVia()
      throws DsSipParserListenerException, DsSipParserException {

    byte[] messagebytes = SIPMessageGenerator.getInviteMessage("graivitt").getBytes();

    DsSipRequest sipRequest = (DsSipRequest) DsSipMessage.createMessage(messagebytes, true, true);
    DsSipViaHeader sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();
    sipViaHeader.removeRPort();
    int receivedPort = 5061;
    Assert.assertFalse(sipViaHeader.isRPortPresent());

    sipTransactionManager.addRPortParameter(sipRequest, receivedPort);

    sipViaHeader = (DsSipViaHeader) sipRequest.getViaHeader();

    Assert.assertFalse(sipViaHeader.isRPortPresent());
    Assert.assertEquals(sipViaHeader.getRPort(), -1);
  }

  public class RouteDataProvider {

    String cpRouteToAdd;
    boolean topRouteHeaderMatch;
    boolean isHostPortEnabled;

    public RouteDataProvider(
        String cpRouteToAdd, boolean topRouteHeaderMatch, boolean isHostPortEnabled) {
      this.cpRouteToAdd = cpRouteToAdd;
      this.topRouteHeaderMatch = topRouteHeaderMatch;
      this.isHostPortEnabled = isHostPortEnabled;
    }

    public String toString() {
      return "CP Route to be added in ACK: {"
          + cpRouteToAdd
          + "}; "
          + "Top-route in ACK match with CP route added(i.e.CP route not removed): {"
          + topRouteHeaderMatch
          + "}; "
          + "HostPort feature: {"
          + isHostPortEnabled
          + "}";
    }
  }
}
