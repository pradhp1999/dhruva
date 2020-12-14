package com.cisco.dhruva.sip.controller;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.adaptor.*;
import com.cisco.dhruva.common.CommonContext;
import com.cisco.dhruva.common.context.ExecutionContext;
import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.common.messaging.models.MessageBodyType;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.router.AppEngine;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.router.MessageListener;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SIPRequestBuilder;
import com.cisco.dhruva.util.SpringApplicationContext;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@SpringBootTest
public class DsProxyControllerServerTest {

  private DsSipProxyManager sipProxyManager;
  DsNetwork dsNetwork;
  DsNetwork externalIpEnabledNetwork;
  private AppAdaptorInterface adaptorInterface;
  private ProxyAdaptorFactoryInterface proxyAdaptorFactoryInterface;
  @Autowired SipServerLocatorService locatorService;

  private ApplicationContext applicationContext;
  private com.cisco.dhruva.common.executor.ExecutorService executorService;

  @BeforeClass
  void init() throws Exception {

    applicationContext = mock(ApplicationContext.class);
    executorService = mock(ExecutorService.class);

    SpringApplicationContext springApplicationContext = new SpringApplicationContext();
    springApplicationContext.setApplicationContext(applicationContext);

    dsNetwork = DsNetwork.getNetwork("Default");
    externalIpEnabledNetwork = DsNetwork.getNetwork("External_IP_enabled");

    adaptorInterface = mock(AppAdaptorInterface.class);
    proxyAdaptorFactoryInterface = mock(ProxyAdaptorFactoryInterface.class);
    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipProxyManager dsSipProxyManager = DsSipProxyManager.getInstance();
    if (dsSipProxyManager == null) {
      DsSipTransportLayer transportLayer = mock(DsSipTransportLayer.class);
      DsSipTransactionFactory transactionFactory = new DsSipDefaultTransactionFactory();
      dsSipProxyManager =
          new DsSipProxyManager(
              transportLayer, controllerFactory, transactionFactory, locatorService);
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
          InetAddress.getByName("127.0.0.1"),
          5060,
          Transport.UDP,
          InetAddress.getByName("127.0.0.1"),
          false);

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
  public void testOnNewRequestInvite() throws Exception {

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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);
  }

  @Test(
      description =
          "Controller throws exception when invite is received without sip server transactipn",
      expectedExceptions = {NullPointerException.class})
  public void testOnNewRequestInviteThrowsException() throws Exception {

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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

    DsProxyStatelessTransaction proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    proxy = controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);
    Assert.assertTrue(proxy instanceof DsProxyTransaction);

    sipProxyManager.request(serverTransaction, sipRequest);
  }

  @Test(description = "Test controller handling ACK request")
  public void testOnNewRequestACK() throws Exception {

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
        mock(ScheduledThreadPoolExecutor.class);

    when(executorService.getScheduledExecutorThreadPool(ExecutorType.AKKA_CONTROLLER_TIMER))
        .thenReturn(scheduledThreadPoolExecutor);

    AppEngine.startShutdownTimers(executorService);

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
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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
  public void testOnNewRequestCancel() throws Exception {

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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);
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

  // TODO, temp fix, since mid-dialog requests are getting routed to App
  @Test(
      description = "Controller handling invite with Route header, should by-pass App layer",
      enabled = false)
  public void testOnNewRequestWithRouteHeader() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);
    DsSipRouteHeader dsSipCurrentNodeRouteHeader =
        new DsSipRouteHeader(
            ("<sip:bob@" + "1.2.3.4" + ":" + "5060" + ";transport=udp;lr;x-cisco-call-type=sip>")
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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);
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

  // TODO, temp fix, since mid-dialog requests are getting routed to App
  @Test(
      description = "Controller receiving mid-dialog requests, should by-pass application layer",
      enabled = false)
  public void testOnNewRequestMidDialog() throws Exception {
    SIPRequestBuilder sipRequestBuilder = new SIPRequestBuilder();
    DsSipRequest sipRequest = sipRequestBuilder.getReInviteRequest(null);

    dsNetwork.setRemoveOwnRouteHeader(true);
    DsSipTransactionKey key = sipRequest.forceCreateKey();

    DsSipRouteHeader dsSipCurrentNodeRouteHeader =
        new DsSipRouteHeader(
            ("<sip:bob@" + "1.2.3.4" + ":" + "5060" + ";transport=udp;lr;x-cisco-call-type=sip>")
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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);
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
  public void testResponse200OkForInviteNoInitialRequestSent() throws Exception {

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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

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

    DsSipResponse receivedResp = argumentCaptor.getValue();

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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

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

    DsSipResponse receivedResp = argumentCaptor.getValue();

    Assert.assertNotNull(receivedResp);
    Assert.assertEquals(receivedResp.getMethodID(), 1);
    Assert.assertEquals(resp.toString(), receivedResp.toString());
  }

  @DataProvider
  public Object[] getRecordRouteHeader() throws DsSipParserListenerException, DsSipParserException {
    // single network (hostPort true/false & host IP/FQDN will result in same output after flip)
    DsSipRecordRouteHeader rr1 =
        new DsSipRecordRouteHeader("<sip:rr$n=Default@127.0.0.1:5060;transport=udp;lr>".getBytes());
    DsSipRecordRouteHeader rr1AfterFlip =
        new DsSipRecordRouteHeader("<sip:rr$n=Default@127.0.0.1:5060;transport=udp;lr>".getBytes());

    // host portion is a 'external IP' attached network
    DsSipRecordRouteHeader rr2 =
        new DsSipRecordRouteHeader("<sip:rr$n=Default@1.1.1.1:5061;transport=udp;lr>".getBytes());
    DsSipRecordRouteHeader rr2AfterFlip =
        new DsSipRecordRouteHeader(
            "<sip:rr$n=External_IP_enabled@127.0.0.1:5060;transport=udp;lr>".getBytes());

    // user portion is a 'external IP' attached network
    DsSipRecordRouteHeader rr3 =
        new DsSipRecordRouteHeader(
            "<sip:rr$n=External_IP_enabled@127.0.0.1:5060;transport=udp;lr>".getBytes());
    DsSipRecordRouteHeader rr3AfterFlip =
        new DsSipRecordRouteHeader("<sip:rr$n=Default@1.1.1.1:5061;transport=udp;lr>".getBytes());
    DsSipRecordRouteHeader rr3AfterFlipWithHostPortDisabled =
        new DsSipRecordRouteHeader("<sip:rr$n=Default@127.0.0.1:5061;transport=udp;lr>".getBytes());
    DsSipRecordRouteHeader rr3AfterFlipWithFqdn =
        new DsSipRecordRouteHeader(
            "<sip:rr$n=Default@dhruva.sjc.webex.com:5061;transport=udp;lr>".getBytes());

    // host portion is a 'external IP' attached network but 'hostPort' toggle is disabled
    DsSipRecordRouteHeader rr4 =
        new DsSipRecordRouteHeader("<sip:rr$n=Default@127.0.0.1:5061;transport=udp;lr>".getBytes());
    DsSipRecordRouteHeader rr4AfterFlip =
        new DsSipRecordRouteHeader(
            "<sip:rr$n=External_IP_enabled@127.0.0.1:5060;transport=udp;lr>".getBytes());

    // host portion is a 'external IP' attached network, FQDN provided
    DsSipRecordRouteHeader rr5 =
        new DsSipRecordRouteHeader(
            "<sip:rr$n=Default@dhruva.sjc.webex.com:5061;transport=udp;lr>".getBytes());

    String hostIP = "1.1.1.1";
    String hostFqdn = "dhruva.sjc.webex.com";
    RecordRouteDataProvider rrProvider1 =
        new RecordRouteDataProvider(rr1, rr1AfterFlip, hostIP, true);
    RecordRouteDataProvider rrProvider2 =
        new RecordRouteDataProvider(rr2, rr2AfterFlip, hostIP, true);
    RecordRouteDataProvider rrProvider3 =
        new RecordRouteDataProvider(rr3, rr3AfterFlip, hostIP, true);
    RecordRouteDataProvider rrProvider4 =
        new RecordRouteDataProvider(rr1, rr1AfterFlip, hostIP, false);
    RecordRouteDataProvider rrProvider5 =
        new RecordRouteDataProvider(rr4, rr4AfterFlip, hostIP, false);
    RecordRouteDataProvider rrProvider6 =
        new RecordRouteDataProvider(rr3, rr3AfterFlipWithHostPortDisabled, hostIP, false);

    RecordRouteDataProvider rrProvider7 =
        new RecordRouteDataProvider(rr5, rr2AfterFlip, hostFqdn, true);
    RecordRouteDataProvider rrProvider8 =
        new RecordRouteDataProvider(rr3, rr3AfterFlipWithFqdn, hostFqdn, true);

    return new RecordRouteDataProvider[][] {
      {rrProvider1}, {rrProvider2}, {rrProvider3},
      {rrProvider4}, {rrProvider5}, {rrProvider6},
      {rrProvider7}, {rrProvider8}
    };
  }

  @Test(
      description =
          "success response path for invite transaction.Set Record Route headers in response msg."
              + "Dhruva should flip the RR (stateful)",
      dataProvider = "getRecordRouteHeader")
  public void testResponse200OkWithRRForInvite(RecordRouteDataProvider rrProvider)
      throws Exception {

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

    DsControllerConfig.addRecordRouteInterface(
        InetAddress.getByName("127.0.0.1"), 5060, Transport.UDP, dsNetwork);
    DsControllerConfig.addRecordRouteInterface(
        InetAddress.getByName("127.0.0.1"), 5061, Transport.UDP, externalIpEnabledNetwork);

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

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
        new DsSipRecordRouteHeader("<sip:rr$n=service@1.2.3.4:5080;transport=udp;lr>".getBytes());
    resp.addHeader(rr1);
    resp.addHeader(rrProvider.rrToAdd);

    System.out.println(
        "RR in the response which will be undergoing change : " + rrProvider.rrToAdd.toString());

    IDhruvaMessage responseMsg =
        MessageConvertor.convertSipMessageToDhruvaMessage(
            resp, MessageBodyType.SIPRESPONSE, context);

    doReturn(rrProvider.isHostPortEnabled).when(dhruvaSIPConfigProperties).isHostPortEnabled();
    doReturn(rrProvider.hostIpOrFqdn).when(dhruvaSIPConfigProperties).getHostInfo();

    // when(dhruvaSIPConfigProperties.isHostPortEnabled()).thenReturn(rrProvider.isHostPortEnabled);
    // when(dhruvaSIPConfigProperties.getHostInfo()).thenReturn(rrProvider.hostIpOrFqdn);

    handler.onMessage(responseMsg);
    ArgumentCaptor<DsSipResponse> argumentCaptor = ArgumentCaptor.forClass(DsSipResponse.class);
    verify(serverTransaction, times(1)).sendResponse(argumentCaptor.capture());

    DsSipResponse receivedResp = argumentCaptor.getValue();

    System.out.println("Final response after modifications (RR flip) : " + receivedResp.toString());

    DsSipHeaderList rrHeader = receivedResp.getHeaders(DsSipConstants.RECORD_ROUTE);

    Assert.assertNotNull(receivedResp);
    Assert.assertEquals(receivedResp.getMethodID(), 1);
    Assert.assertEquals(resp.toString(), receivedResp.toString());

    // TODO , since there is only one listen point, there is no flip happening for first testcase.
    // For other two, flip happens
    Assert.assertEquals(rrHeader.getLast(), rrProvider.rrExpected);

    DsNetwork.setDhruvaConfigProperties(null);
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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

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
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory, null);

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

  public class RecordRouteDataProvider {

    DsSipRecordRouteHeader rrToAdd;
    DsSipRecordRouteHeader rrExpected;
    String hostIpOrFqdn;
    boolean isHostPortEnabled;

    public RecordRouteDataProvider(
        DsSipRecordRouteHeader rrToAdd,
        DsSipRecordRouteHeader rrExpected,
        String hostIpOrFqdn,
        boolean isHostPortEnabled) {
      this.rrToAdd = rrToAdd;
      this.rrExpected = rrExpected;
      this.hostIpOrFqdn = hostIpOrFqdn;
      this.isHostPortEnabled = isHostPortEnabled;
    }

    public String toString() {
      return "Original RR: {"
          + rrToAdd.toString()
          + "}; "
          + "RR after flip: {"
          + rrExpected.toString()
          + "}; "
          + "Host IP/FQDN: {"
          + hostIpOrFqdn
          + "}; "
          + "HostPort feature: {"
          + isHostPortEnabled
          + "}";
    }
  }
}
