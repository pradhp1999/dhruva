package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import com.cisco.dhruva.adaptor.AppAdaptorInterface;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.cisco.dhruva.sip.cac.SIPSessions;
import com.cisco.dhruva.sip.controller.DsAppController;
import com.cisco.dhruva.sip.controller.DsProxyController;
import com.cisco.dhruva.sip.controller.DsREControllerFactory;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SIPRequestBuilder;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DsSipClientTransactionTest {

  private DsSipTransactionManager sipTransactionManager;
  private DsSipProxyManager sipProxyManager;
  private DsSipTransportLayer transportLayer;
  private DsSipStrayMessageInterface strayMessageInterface;
  private DsSipRequestInterface requestInterface;
  private DsSipTransactionEventInterface transactionEventInterface;
  private AppAdaptorInterface adaptorInterface;
  private ProxyAdaptorFactoryInterface proxyAdaptorFactoryInterface;
  DsSipTransactionFactory transactionFactory;

  private DsBindingInfo incomingMessageBindingInfo;
  private InetAddress localAddress;
  private InetAddress remoteAddress;
  private int localPort, remotePort;
  private DsNetwork dsNetwork;
  private SipServerLocatorService locatorService;
  private DsSipServerLocator sipServerLocator;

  @BeforeClass
  public void init() throws UnknownHostException, DsException {
    transportLayer = mock(DsSipTransportLayer.class);
    strayMessageInterface = mock(DsSipStrayMessageInterface.class);
    requestInterface = mock(DsSipRequestInterface.class);
    transactionEventInterface = mock(DsSipTransactionEventInterface.class);

    adaptorInterface = mock(AppAdaptorInterface.class);
    proxyAdaptorFactoryInterface = mock(ProxyAdaptorFactoryInterface.class);

    localAddress = InetAddress.getByName("127.0.0.1");
    remoteAddress = InetAddress.getByName("127.0.0.1");
    localPort = 5060;
    remotePort = 5070;
    incomingMessageBindingInfo =
        new DsBindingInfo(localAddress, localPort, localAddress, remotePort, Transport.UDP);
    dsNetwork = DsNetwork.getNetwork("Default");
    incomingMessageBindingInfo.setNetwork(dsNetwork);
    locatorService = mock(SipServerLocatorService.class);
    sipServerLocator = mock(DsSipServerLocator.class);

    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipProxyManager dsSipProxyManager = DsSipProxyManager.getInstance();
    if (dsSipProxyManager == null) {
      transactionFactory = new DsSipDefaultTransactionFactory();
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

    sipTransactionManager = DsSipTransactionManager.getTransactionManager();

    DsSipTransactionManager.setProxyServerMode(true);

    try {
      DsControllerConfig.addListenInterface(
          dsNetwork,
          InetAddress.getByName("127.0.0.1"),
          5060,
          Transport.UDP,
          InetAddress.getByName("127.0.0.1"));

      DsControllerConfig.addRecordRouteInterface(
          InetAddress.getByName("127.0.0.1"), 5060, Transport.UDP, dsNetwork);

    } catch (DsInconsistentConfigurationException | IOException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @AfterClass
  void cleanUp() {
    DsSipTransactionManager.setSmp_theSingleton(null);
    DsSipProxyManager.setM_Singleton(null);
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
  }

  @Test
  public void testSipClientTransaction() throws Exception {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipServerTransaction serverTransaction =
        transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    ArgumentCaptor<DsSipMessage> argumentCaptor = ArgumentCaptor.forClass(DsSipMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));

    // Configure Transport Layer for sending INVITE request from Transaction manager
    DsSipConnection sendConnection = mock(DsSipConnection.class);
    when(sendConnection.getBindingInfo()).thenReturn(incomingMessageBindingInfo);
    when(sendConnection.getTransportType()).thenReturn(Transport.UDP);
    when(locatorService.isSupported(any(Transport.class))).thenReturn(true);
    when(locatorService.shouldSearch(any(DsSipURL.class))).thenReturn(false);
    when(locatorService.getLocator()).thenReturn(sipServerLocator);
    doNothing().when(sipServerLocator).setSupportedTransports(any(byte.class));

    doReturn(sendConnection)
        .when(transportLayer)
        .getConnection(
            any(DsNetwork.class),
            any(InetAddress.class),
            any(Integer.class),
            any(InetAddress.class),
            any(Integer.class),
            any(Transport.class));
    doReturn(sendConnection).when(transportLayer).getConnection(any(DsBindingInfo.class));

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    // If set true, it will pick the route header for routing.Here we want based on req uri
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    ctrlr.setRequest(sipRequest);
    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    // Check the INVITE request
    verify(sendConnection).send(argumentCaptor.capture());

    DsSipRequest requestReceivedAtConnection = (DsSipRequest) argumentCaptor.getValue();
    Assert.assertNotNull(requestReceivedAtConnection);
    Assert.assertEquals(requestReceivedAtConnection.getMethodID(), 1);
  }

  @Test()
  public void testSipClientGetConnectionException() throws Exception {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipServerTransaction serverTransaction =
        transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
        cf.getController(
            serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
        .thenReturn(adaptorInterface);

    ArgumentCaptor<DsSipMessage> argumentCaptor = ArgumentCaptor.forClass(DsSipMessage.class);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));

    // Configure Transport Layer for sending INVITE request from Transaction manager
    DsSipConnection sendConnection = mock(DsSipConnection.class);
    when(sendConnection.getBindingInfo()).thenReturn(incomingMessageBindingInfo);
    when(sendConnection.getTransportType()).thenReturn(Transport.UDP);

    doThrow(new DsException("Unable to connect"))
        .when(transportLayer)
        .getConnection(
            any(DsNetwork.class),
            any(InetAddress.class),
            any(Integer.class),
            any(InetAddress.class),
            any(Integer.class),
            any(Transport.class));

    DsProxyController ctrlr = (DsProxyController) controller;

    Location loc = new Location(sipRequest.getURI());
    // If set true, it will pick the route header for routing.Here we want based on req uri
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
        .when(appInterfaceMock)
        .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    ctrlr.setRequest(sipRequest);
    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    // verify that sendTo is not sent
    verify(sendConnection, times(0)).send(argumentCaptor.capture());

    // errorCode 6 for unReachable
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(6));
  }
}
