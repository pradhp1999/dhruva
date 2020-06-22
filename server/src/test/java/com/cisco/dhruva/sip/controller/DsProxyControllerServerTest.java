package com.cisco.dhruva.sip.controller;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.adaptor.*;
import com.cisco.dhruva.common.CommonContext;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.router.MessageListener;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SIPRequestBuilder;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DsProxyControllerServerTest {

  private DsSipProxyManager sipProxyManager;
  DsNetwork dsNetwork;
  private AppAdaptorInterface adaptorInterface;
  private ProxyAdaptorFactoryInterface proxyAdaptorFactoryInterface;

  @BeforeClass
  void init() throws Exception {

    dsNetwork = DsNetwork.getNetwork("Default");
    adaptorInterface = mock(AppAdaptorInterface.class);
    proxyAdaptorFactoryInterface = mock(ProxyAdaptorFactoryInterface.class);
    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipProxyManager dsSipProxyManager = DsSipProxyManager.getInstance();
    if (dsSipProxyManager == null) {
      DsSipTransportLayer transportLayer = mock(DsSipTransportLayer.class);
      DsSipTransactionFactory transactionFactory = new DsSipDefaultTransactionFactory();
      dsSipProxyManager =
          new DsSipProxyManager(transportLayer, controllerFactory, transactionFactory);
    }
    sipProxyManager = spy(dsSipProxyManager);
    DsSipProxyManager.setM_Singleton(sipProxyManager);
    sipProxyManager.setRouteFixInterface(controllerFactory);

    DsSipServerTransactionImpl.setThreadPoolExecutor(
        (ThreadPoolExecutor) Executors.newFixedThreadPool(1));

    DsSipClientTransactionImpl.setThreadPoolExecutor(
        (ThreadPoolExecutor) Executors.newFixedThreadPool(1));

    try {
      DsControllerConfig.addListenInterface(
          dsNetwork,
          InetAddress.getByName("0.0.0.0"),
          5060,
          Transport.UDP,
          InetAddress.getByName("0.0.0.0"));

      DsControllerConfig.addRecordRouteInterface(
          InetAddress.getByName("0.0.0.0"), 5060, Transport.UDP, dsNetwork);
    } catch (DsInconsistentConfigurationException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    }
  }

  @AfterClass
  void cleanUp() {
    DsSipTransactionManager.setSmp_theSingleton(null);
    DsSipProxyManager.setM_Singleton(null);
  }

  @Test(description = "DsAppController handling brand new invite request")
  public void testOnNewRequestInvite() throws DsException {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = new AppSession();
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);
  }

  @Test(
      description =
          "Controller throws exception when invite is received without sip server transactipn",
      expectedExceptions = {NullPointerException.class})
  public void testOnNewRequestInviteThrowsException() throws DsException {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = new AppSession();

    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    sipProxyManager.request(serverTransaction, sipRequest);
  }

  @Test(description = "Test controller handling ACK request")
  public void testOnNewRequestACK() throws DsException {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = new AppSession();
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipRequest ackRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.ACK));

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    controller.onAck(proxy, proxy.getServerTransaction(), (DsSipAckMessage) ackRequest);

    verify(adaptorInterface, times(2)).handleRequest(argumentCaptor.capture());

    DsSipAckMessage receivedAck = (DsSipAckMessage) argumentCaptor.getValue();

    Assert.assertNotNull(receivedAck);
    Assert.assertEquals(receivedAck.getMethodID(), 2);
  }

  @Test(description = "Controller handling cancel request")
  public void testOnNewRequestCancel() throws DsException {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();
    AppInterface app = new AppSession();
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);
    DsAppController c = (DsAppController) controller;
    AppAdaptorInterface adaptor = mock(AppAdaptorInterface.class);
    c.setProxyAdaptor(adaptor);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    sipProxyManager.request(serverTransaction, sipRequest);

    DsSipRequest cancelRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.CANCEL));

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    controller.onCancel(proxy, proxy.getServerTransaction(), (DsSipCancelMessage) cancelRequest);

    verify(adaptor, times(2)).handleRequest(argumentCaptor.capture());

    DsSipCancelMessage receivedCancel = (DsSipCancelMessage) argumentCaptor.getValue();

    Assert.assertNotNull(receivedCancel);
    Assert.assertEquals(receivedCancel.getMethodID(), 3);
  }

  @Test(description = "Controller handling invite with Route header, should by-pass App layer")
  public void testOnNewRequestWithRouteHeader() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);
    DsSipRouteHeader dsSipCurrentNodeRouteHeader =
        new DsSipRouteHeader(
            new String(
                    "<sip:bob@"
                        + "1.2.3.4"
                        + ":"
                        + "5060"
                        + ";transport=udp;lr;x-cisco-call-type=sip>")
                .getBytes());

    sipRequest.removeHeaders(DsSipConstants.ROUTE);
    sipRequest.addHeader(dsSipCurrentNodeRouteHeader, true);
    Assert.assertEquals(sipRequest.getHeader(DsSipConstants.ROUTE), dsSipCurrentNodeRouteHeader);
    dsNetwork.setRemoveOwnRouteHeader(true);

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();
    AppInterface app = new AppSession();
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);
    DsAppController c = (DsAppController) controller;
    AppAdaptorInterface adaptor = mock(AppAdaptorInterface.class);
    c.setProxyAdaptor(adaptor);
    c = spy(c);
    doNothing()
        .when((DsProxyController) c)
        .proxyTo(new Location(sipRequest.getURI()), sipRequest, null);

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    DsProxyStatelessTransaction proxy = c.onNewRequest(serverTransaction, sipRequest);

    verify(adaptor, times(0)).handleRequest(argumentCaptor.capture());

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);
  }

  @Test(description = "Controller receiving mid-dialog requests, should by-pass application layer")
  public void testOnNewRequestMidDialog() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    DsSipTransactionKey key = sipRequest.forceCreateKey();

    DsSipRouteHeader dsSipCurrentNodeRouteHeader =
        new DsSipRouteHeader(
            new String(
                    "<sip:bob@"
                        + "1.2.3.4"
                        + ":"
                        + "5060"
                        + ";transport=udp;lr;x-cisco-call-type=sip>")
                .getBytes());

    sipRequest.removeHeaders(DsSipConstants.ROUTE);
    sipRequest.addHeader(dsSipCurrentNodeRouteHeader, true);

    sipRequest.setNetwork(dsNetwork);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();
    AppInterface app = new AppSession();
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);
    DsAppController c = (DsAppController) controller;
    AppAdaptorInterface adaptor = mock(AppAdaptorInterface.class);
    c.setProxyAdaptor(adaptor);
    c = spy(c);
    doNothing()
        .when((DsProxyController) c)
        .proxyTo(new Location(sipRequest.getURI()), sipRequest, null);

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    DsProxyStatelessTransaction proxy = c.onNewRequest(serverTransaction, sipRequest);

    verify(adaptor, times(0)).handleRequest(argumentCaptor.capture());

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);
  }

  @Test(
      description = "controller sending 200 Ok , but no server context set",
      expectedExceptions = {DhruvaException.class})
  public void testResponse200OkForInviteNoIntialRequestSent() throws Exception {

    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    sipRequest.removeHeaders(DsSipConstants.ROUTE);

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsControllerFactoryInterface cf = new DsREControllerFactory();

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();
    AppInterface app = new AppSession();
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    ProxyAdaptor adaptor =
        (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, new AppSession());

    final ExecutionContext context;
    MessageListener handler = new RouteResponseHandler(adaptor);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    IDhruvaMessage responseMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            resp, MessageBodyType.SIPRESPONSE, context);

    handler.onMessage(responseMsg);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);
    verify(serverTransaction, times(1)).sendResponse(argumentCaptor.capture());

    DsSipResponse receivedResp = (DsSipResponse) argumentCaptor.getValue();

    Assert.assertNotNull(receivedResp);
    Assert.assertEquals(receivedResp.getMethodID(), 1);
    Assert.assertEquals(resp.toString(), responseMsg.toString());
  }

  @Test(description = "success response path for invite transaction")
  public void testResponse200OkForInvite() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    sipRequest.removeHeaders(DsSipConstants.ROUTE);

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsControllerFactoryInterface cf = new DsREControllerFactory();

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction sTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsSipServerTransaction serverTransaction = spy(sTransaction);

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = mock(AppSession.class);
    doNothing().when(app).handleRequest(null);

    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    final ExecutionContext context;
    MessageListener handler = new RouteResponseHandler(adaptor);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    IDhruvaMessage responseMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            resp, MessageBodyType.SIPRESPONSE, context);

    handler.onMessage(responseMsg);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);
    verify(serverTransaction, times(1)).sendResponse(argumentCaptor.capture());

    DsSipResponse receivedResp = (DsSipResponse) argumentCaptor.getValue();

    Assert.assertNotNull(receivedResp);
    Assert.assertEquals(receivedResp.getMethodID(), 1);
    Assert.assertEquals(resp.toString(), receivedResp.toString());
  }

  @Test(
      description =
          "success response path for invite transaction.Set Record Route headers in response msg."
              + "Dhruva should flip the RR (stateful)")
  public void testResponse200OkWithRRForInvite() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    sipRequest.removeHeaders(DsSipConstants.ROUTE);

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsControllerFactoryInterface cf = new DsREControllerFactory();

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction sTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsSipServerTransaction serverTransaction = spy(sTransaction);

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = mock(AppSession.class);
    doNothing().when(app).handleRequest(null);

    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    final ExecutionContext context;
    MessageListener handler = new RouteResponseHandler(adaptor);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    DsSipRecordRouteHeader rr1 =
        new DsSipRecordRouteHeader("<sip:rr,n=service@1.2.3.4:5080;transport=udp;lr>".getBytes());

    DsSipRecordRouteHeader rr2 =
        new DsSipRecordRouteHeader("<sip:rr,n=Default@0.0.0.0:5060;transport=udp;lr>".getBytes());
    resp.addHeader(rr1);
    resp.addHeader(rr2);

    IDhruvaMessage responseMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            resp, MessageBodyType.SIPRESPONSE, context);

    handler.onMessage(responseMsg);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);
    verify(serverTransaction, times(1)).sendResponse(argumentCaptor.capture());

    DsSipResponse receivedResp = (DsSipResponse) argumentCaptor.getValue();

    DsSipHeaderList rrHeader = receivedResp.getHeaders(DsSipConstants.RECORD_ROUTE);

    Assert.assertNotNull(receivedResp);
    Assert.assertEquals(receivedResp.getMethodID(), 1);
    Assert.assertEquals(resp.toString(), receivedResp.toString());

    // TODO , since there is only one listen point, there is no flip happening
    Assert.assertEquals(rrHeader.getLast(), rr2);
  }

  @Test(description = "controller forwarding 4xx response to invite request")
  public void testResponse4XXForInvite() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    sipRequest.removeHeaders(DsSipConstants.ROUTE);

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsControllerFactoryInterface cf = new DsREControllerFactory();

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction sTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsSipServerTransaction serverTransaction = spy(sTransaction);

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = mock(AppSession.class);
    doNothing().when(app).handleRequest(null);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    final ExecutionContext context;
    MessageListener handler = new RouteResponseHandler(adaptor);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_NOT_FOUND, sipRequest);

    IDhruvaMessage responseMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            resp, MessageBodyType.SIPRESPONSE, context);

    doNothing().when(serverTransaction).sendResponse(resp);

    handler.onMessage(responseMsg);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);
    verify(serverTransaction, times(1)).sendResponse(argumentCaptor.capture());

    DsSipResponse receivedResp = argumentCaptor.getValue();

    Assert.assertNotNull(receivedResp);
    Assert.assertEquals(receivedResp.getMethodID(), 1);
    Assert.assertEquals(resp.toString(), receivedResp.toString());
  }

  @Test(description = "controller forwarding 5xx response to invite request")
  public void testResponse5XXForInvite() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    sipRequest.removeHeaders(DsSipConstants.ROUTE);

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsControllerFactoryInterface cf = new DsREControllerFactory();

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction sTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsSipServerTransaction serverTransaction = spy(sTransaction);

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = mock(AppSession.class);
    doNothing().when(app).handleRequest(null);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    final ExecutionContext context;
    MessageListener handler = new RouteResponseHandler(adaptor);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE, sipRequest);

    IDhruvaMessage responseMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            resp, MessageBodyType.SIPRESPONSE, context);

    doNothing().when(serverTransaction).sendResponse(resp);

    handler.onMessage(responseMsg);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);
    verify(serverTransaction, times(1)).sendResponse(argumentCaptor.capture());

    DsSipResponse receivedResp = argumentCaptor.getValue();

    Assert.assertNotNull(receivedResp);
    Assert.assertEquals(receivedResp.getMethodID(), 1);
    Assert.assertEquals(resp.toString(), receivedResp.toString());
  }

  @Test(description = "controller forwarding 3xx response to invite request")
  public void testResponse3XXForInvite() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    sipRequest.removeHeaders(DsSipConstants.ROUTE);

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsControllerFactoryInterface cf = new DsREControllerFactory();

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction sTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsSipServerTransaction serverTransaction = spy(sTransaction);

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = mock(AppSession.class);
    doNothing().when(app).handleRequest(null);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    final ExecutionContext context;
    MessageListener handler = new RouteResponseHandler(adaptor);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_MOVED_TEMPORARILY, sipRequest);

    IDhruvaMessage responseMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            resp, MessageBodyType.SIPRESPONSE, context);

    doNothing().when(serverTransaction).sendResponse(resp);

    handler.onMessage(responseMsg);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);
    verify(serverTransaction, times(1)).sendResponse(argumentCaptor.capture());

    DsSipResponse receivedResp = argumentCaptor.getValue();

    Assert.assertNotNull(receivedResp);
    Assert.assertEquals(receivedResp.getMethodID(), 1);
    Assert.assertEquals(resp.toString(), receivedResp.toString());
  }

  @Test(description = "controller forwarding 5xx response to invite request")
  public void testResponse5XXForInviteDestinationUnreachable() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    sipRequest.removeHeaders(DsSipConstants.ROUTE);

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    DsControllerFactoryInterface cf = new DsREControllerFactory();

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction sTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsSipServerTransaction serverTransaction = spy(sTransaction);

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();
    AppInterface app = new AppSession();
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface ctrlr =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    DsControllerInterface controller = spy(ctrlr);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);
    DsAppController c = (DsAppController) controller;
    AppAdaptorInterface adaptor = c.getProxyAdaptor();

    final ExecutionContext context;
    MessageListener handler = new RouteResponseHandler((ProxyAdaptor) adaptor);
    context = new ExecutionContext();
    context.set(CommonContext.PROXY_RESPONSE_HANDLER, handler);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE, sipRequest);

    IDhruvaMessage responseMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            resp, MessageBodyType.SIPRESPONSE, context);

    handler.onMessage(responseMsg);
  }
}
