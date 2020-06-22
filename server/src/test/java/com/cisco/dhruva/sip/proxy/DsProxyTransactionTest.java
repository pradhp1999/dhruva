package com.cisco.dhruva.sip.proxy;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.adaptor.AppAdaptorInterface;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.sip.controller.*;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SIPRequestBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DsProxyTransactionTest {
  DsSipTransactionFactory transactionFactory;
  DsSipTransactionManager transactionManager;
  private AppAdaptorInterface adaptorInterface;
  private ProxyAdaptorFactoryInterface proxyAdaptorFactoryInterface;
  DsNetwork dsNetwork;
  DsControllerConfig ourConfig;
  private DsBindingInfo incomingMessageBindingInfo;

  private InetAddress localAddress;
  private InetAddress remoteAddress;
  private int localPort, remotePort;

  @BeforeClass
  void init() throws Exception {
    dsNetwork = DsNetwork.getNetwork("Default");
    ourConfig = DsControllerConfig.getCurrent();

    // This is required to set the via handler, route fix interface, global states are maintained
    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipProxyManager dsSipProxyManager = DsSipProxyManager.getInstance();
    if (dsSipProxyManager == null) {
      DsSipTransportLayer transportLayer = mock(DsSipTransportLayer.class);
      // Mock the stack interfaces
      transactionFactory = mock(DsSipDefaultTransactionFactory.class);
      dsSipProxyManager =
          new DsSipProxyManager(transportLayer, controllerFactory, transactionFactory);
    }
    // DsSipProxyManager sipProxyManager = spy(dsSipProxyManager);
    DsSipProxyManager.setM_Singleton(dsSipProxyManager);
    dsSipProxyManager.setRouteFixInterface(controllerFactory);

    adaptorInterface = mock(AppAdaptorInterface.class);
    proxyAdaptorFactoryInterface = mock(ProxyAdaptorFactoryInterface.class);

    localAddress = InetAddress.getByName("0.0.0.0");
    remoteAddress = InetAddress.getByName("0.0.0.0");
    localPort = 5060;
    remotePort = 5070;
    incomingMessageBindingInfo =
        new DsBindingInfo(localAddress, localPort, localAddress, remotePort, Transport.UDP);
    dsNetwork = DsNetwork.getNetwork("Default");
    incomingMessageBindingInfo.setNetwork(dsNetwork);

    DsSipServerTransactionImpl.setThreadPoolExecutor(
        (ThreadPoolExecutor) Executors.newFixedThreadPool(1));

    DsSipClientTransactionImpl.setThreadPoolExecutor(
        (ThreadPoolExecutor) Executors.newFixedThreadPool(1));

    // Add listen interfaces in DsControllerConfig, causes issues in getVia while sending out the
    // packet
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

  @BeforeMethod
  void initController() {}

  @Test
  public void testProxyClientTransaction() throws DsException, IOException {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);
    // when(transactionManager.startClientTransaction(mockSipClientTransaction)).thenReturn(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));
    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    Assert.assertNotNull(proxy.getClientTransaction());

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));

    // Retransmission of 200 OK should not be sent upwards
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);
    // No increment in verify, same as previous
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));
  }

  @Test
  public void testProxyToAddViaClientTransaction() throws DsException, IOException {

    reset(transactionFactory);

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);
    // when(transactionManager.startClientTransaction(mockSipClientTransaction)).thenReturn(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    verify(transactionFactory)
        .createClientTransaction(
            argumentCaptor.capture(),
            any(DsSipClientTransportInfo.class),
            any(DsSipClientTransactionInterface.class));

    DsSipRequest request = argumentCaptor.getValue();
    Assert.assertNotNull(request);

    DsSipViaHeader sipViaHeader = (DsSipViaHeader) request.getViaHeader();

    Assert.assertEquals(request.getMethod(), DsByteString.newInstance("INVITE"));

    Assert.assertEquals(
        sipViaHeader.getHost().toString(),
        incomingMessageBindingInfo.getLocalAddress().getHostAddress());

    Assert.assertNotNull(proxy.getClientTransaction());

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));

    // Retransmission of 200 OK should not be sent upwards
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);
    // No increment in verify, same as previous
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));
  }

  @Test
  public void testProxyToAddRRClientTransaction() throws DsException, IOException {

    reset(transactionFactory);

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);
    // when(transactionManager.startClientTransaction(mockSipClientTransaction)).thenReturn(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    verify(transactionFactory)
        .createClientTransaction(
            argumentCaptor.capture(),
            any(DsSipClientTransportInfo.class),
            any(DsSipClientTransactionInterface.class));

    DsSipRequest request = argumentCaptor.getValue();
    Assert.assertNotNull(request);

    DsSipHeaderList rrHeader = request.getHeaders(DsSipConstants.RECORD_ROUTE);

    Assert.assertEquals(request.getMethod(), DsByteString.newInstance("INVITE"));

    DsSipRecordRouteHeader addedRRHeader =
        new DsSipRecordRouteHeader("<sip:rr,n=Default@0.0.0.0:5060;transport=udp;lr>".getBytes());

    Assert.assertEquals(rrHeader.getFirst(), addedRRHeader);

    Assert.assertNotNull(proxy.getClientTransaction());

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));

    // Retransmission of 200 OK should not be sent upwards
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);
    // No increment in verify, same as previous
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));
  }

  // TODO, FIXME respond is called on server side
  @Test
  public void testProxyClientResponseWithRRHeader() throws DsException, IOException {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);
    // when(transactionManager.startClientTransaction(mockSipClientTransaction)).thenReturn(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));
    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    Assert.assertNotNull(proxy.getClientTransaction());

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    DsSipRecordRouteHeader rrHeader =
        new DsSipRecordRouteHeader(
            "Record-Route: <sip:rr,n=Default@1.2.3.4:5060;transport=udp;lr>".getBytes());

    resp.addHeader(rrHeader);

    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));

    // Retransmission of 200 OK should not be sent upwards
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);
    // No increment in verify, same as previous
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));
  }

  @Test
  public void testTransactionInterfacesClientTimeout() throws DsException, IOException {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);
    // when(transactionManager.startClientTransaction(mockSipClientTransaction)).thenReturn(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));
    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    Assert.assertNotNull(proxy.getClientTransaction());

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    transactionInterfaces.timeOut(mockSipClientTransaction);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(408));
  }

  @Test
  public void testTransactionInterfacesClientProvisionalResponse() throws DsException, IOException {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_RINGING, sipRequest);

    transactionInterfaces.provisionalResponse(mockSipClientTransaction, resp);
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(180));
  }

  @Test
  public void testProxyClientTransactionFinalResponses() throws DsException, IOException {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));
    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    Assert.assertNotNull(proxy.getClientTransaction());

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    DsSipResponse resp4xx =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_BAD_REQUEST, sipRequest);
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp4xx);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(400));

    DsSipResponse resp5xx =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_BAD_GATEWAY, sipRequest);
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp5xx);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(502));

    DsSipResponse resp6xx =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_DECLINE, sipRequest);
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp6xx);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(603));

    DsSipResponse resp3xx =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_MOVED_PERMANENTLY, sipRequest);
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp3xx);

    // Check, why Controller sends response code as 1 for 3xx responses?
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));
  }

  @Test
  public void testTransactionInterfacesClientICMPError() throws DsException, IOException {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);
    // when(transactionManager.startClientTransaction(mockSipClientTransaction)).thenReturn(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    transactionInterfaces.icmpError(mockSipClientTransaction);
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(4));
  }

  @Test
  public void testTransactionInterfacesClientCloseEvent() throws DsException, IOException {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    transactionInterfaces.close(mockSipClientTransaction);
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(500));
  }

  @Test
  public void testTransactionInterfacesServerICMPError() throws DsException, IOException {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    transactionInterfaces.icmpError(serverTransaction);
    // Just used for logging purpose incase of server flow
    verify(appInterfaceMock, times(0))
        .handleResponse(any(Location.class), any(Optional.class), eq(4));
  }

  @Test
  public void testTransactionInterfacesServerCloseEvent() throws DsException, IOException {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(appInterfaceMock);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsAppController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    doNothing().when(appInterfaceMock).handleRequest(any(DsSipRequest.class));

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    transactionInterfaces.close(serverTransaction);
    // In this case CANCEL is triggered from proxy/controller for server close, first call
    // onNewRequest
    verify(appInterfaceMock, times(2)).handleRequest(any(DsSipCancelMessage.class));
  }

  @Test
  public void testTransactionInterfacesServerAckEvent() throws DsException, IOException {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(appInterfaceMock);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsAppController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    doNothing().when(appInterfaceMock).handleRequest(any(DsSipRequest.class));

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    ArgumentCaptor<DsSipCancelMessage> argumentCaptor =
        ArgumentCaptor.forClass(DsSipCancelMessage.class);

    DsSipAckMessage ackRequest =
        (DsSipAckMessage)
            SIPRequestBuilder.createRequest(
                new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.ACK));

    transactionInterfaces.ack(serverTransaction, ackRequest);
    // In this case CANCEL is triggered from proxy/controller for server close, first call
    // onNewRequest
    verify(appInterfaceMock, times(2)).handleRequest(any(DsSipAckMessage.class));
  }

  @Test
  public void testTransactionInterfacesServerTimeout() throws DsException, IOException {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(appInterfaceMock);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsAppController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    doNothing().when(appInterfaceMock).handleRequest(any(DsSipRequest.class));

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
        proxy.getTransactionInterfaces();
    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    transactionInterfaces.timeOut(serverTransaction);
    // Logging purpose, no message is passed to upper layer, 1 is for onNewRequest
    verify(appInterfaceMock, times(1)).handleRequest(any(DsSipRequest.class));
  }

  @Test
  public void testProxyToStrayACKClientTransaction() throws DsException, IOException {

    reset(transactionFactory);

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.ACK));

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    // when(transactionFactory.createClientTransaction(sipRequest, null,
    // proxy.getTransactionInterfaces())).thenReturn(mockSipClientTransaction);
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));
    doNothing().when(mockSipClientTransaction).start();
    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);
    // when(transactionManager.startClientTransaction(mockSipClientTransaction)).thenReturn(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    // Client transaction is not created for Stray ACK
    verify(transactionFactory, times(0))
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsSipClientTransactionInterface.class));
  }

  // Test all exceptions

  @Test()
  public void testClientInvalidParamException() throws DsException, IOException {
    reset(transactionFactory);

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(appInterfaceMock);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    doThrow(new DsException("test")).when(mockSipClientTransaction).start();

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsAppController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    doNothing().when(appInterfaceMock).handleRequest(any(DsSipRequest.class));

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    // errorCode 10 for PROXY_ERROR
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(10));
  }

  @Test()
  public void testClientDestinationUnreachableException() throws DsException, IOException {
    reset(transactionFactory);

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(appInterfaceMock);

    DsProxyTransaction proxy =
        (DsProxyTransaction) controller.onNewRequest(serverTransaction, sipRequest);

    Assert.assertNotNull(proxy);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);

    DsSipClientTransaction mockSipClientTransaction = mock(DsSipClientTransaction.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    doReturn(mockSipClientTransaction)
        .when(transactionFactory)
        .createClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    doThrow(new IOException("test unreachable")).when(mockSipClientTransaction).start();

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(mockSipClientTransaction);

    doReturn(mockSipClientTransaction)
        .when(transactionManager)
        .startClientTransaction(
            any(DsSipRequest.class),
            any(DsSipClientTransportInfo.class),
            any(DsProxyTransaction.TransactionInterfaces.class));

    DsProxyController ctrlr = spy((DsAppController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);

    doNothing().when(appInterfaceMock).handleRequest(any(DsSipRequest.class));

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    // ErroCode UNREACHABLE = 6
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(6));
  }
}
