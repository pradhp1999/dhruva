package com.cisco.dhruva.sip.controller;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.adaptor.ProxyAdaptor;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactory;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.common.messaging.MessageConvertor;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SIPRequestBuilder;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SpringBootTest
public class DsProxyControllerClientTest {
  DsNetwork dsNetwork;
  DsControllerConfig ourConfig;
  private DsBindingInfo incomingMessageBindingInfo;

  private InetAddress localAddress;
  private InetAddress remoteAddress;
  private int localPort, remotePort;
  @Autowired SipServerLocatorService locatorService;

  @BeforeClass
  void init() throws DsException, UnknownHostException {
    dsNetwork = DsNetwork.getNetwork("Default");
    ourConfig = DsControllerConfig.getCurrent();

    // This is required to set the via handler, route fix interface, global states are maintained
    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipProxyManager dsSipProxyManager = DsSipProxyManager.getInstance();
    if (dsSipProxyManager == null) {
      DsSipTransportLayer transportLayer = mock(DsSipTransportLayer.class);
      DsSipTransactionFactory transactionFactory = new DsSipDefaultTransactionFactory();
      dsSipProxyManager =
          new DsSipProxyManager(
              transportLayer, controllerFactory, transactionFactory, locatorService);
    }
    DsSipProxyManager sipProxyManager = spy(dsSipProxyManager);
    DsSipProxyManager.setM_Singleton(sipProxyManager);
    sipProxyManager.setRouteFixInterface(controllerFactory);

    localAddress = InetAddress.getByName("127.0.0.1");
    remoteAddress = InetAddress.getByName("127.0.0.1");
    localPort = 5060;
    remotePort = 5070;
    incomingMessageBindingInfo =
        new DsBindingInfo(localAddress, localPort, localAddress, remotePort, Transport.UDP);
    DsNetwork dsNetwork = DsNetwork.getNetwork("Default");
    incomingMessageBindingInfo.setNetwork(dsNetwork);
  }

  @AfterClass
  void cleanUp() {
    DsSipTransactionManager.setSmp_theSingleton(null);
    DsSipProxyManager.setM_Singleton(null);
  }

  @BeforeMethod
  void initController() {}

  @Test(description = "Basic client flow starting from App")
  public void testProxyTo() throws Exception {
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
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyInterface proxyInterface = mock(DsProxyInterface.class);
    DsProxyCookieInterface cookie = mock(DsProxyCookieInterface.class);
    DsProxyParamsInterface ppIface = ourConfig;

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    when(proxyFactory.createProxyTransaction(controller, ppIface, serverTransaction, sipRequest))
        .thenReturn(proxyInterface);

    doNothing().when(proxyInterface).addProxyRecordRoute(sipRequest, ppIface);
    doNothing().when(proxyInterface).proxyTo(sipRequest, cookie, ppIface);

    DsProxyController ctrlr = (DsAppController) controller;

    Location loc = new Location(sipRequest.getURI());
    // loc.setProcessRoute(true);
    loc.setNetwork(dsNetwork);
    ctrlr.usingRouteHeader = false;

    // This is to set ourRequest global variable

    ctrlr.setRequest(sipRequest);
    ctrlr.setProxyTransaction(proxyInterface);

    ctrlr.proxyTo(loc, sipRequest, null);

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    verify(proxyInterface)
        .proxyTo(
            argumentCaptor.capture(),
            any(DsProxyCookieInterface.class),
            any(DsProxyBranchParamsInterface.class));

    DsSipRequest requestReceived = argumentCaptor.getValue();
    Assert.assertNotNull(requestReceived);
  }

  @Test(description = "test flow from onNewRequest -> App layer -> ProxyTo ")
  public void testAppHandleRequest() throws Exception {
    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    // Let the App handle
    sipRequest.removeHeaders(DsSipConstants.ROUTE);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = new AppSession();
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyInterface proxyInterface;
    DsProxyStatelessTransaction statelessTransaction = mock(DsProxyStatelessTransaction.class);
    DsProxyCookieInterface cookie = mock(DsProxyCookieInterface.class);
    DsProxyParamsInterface ppIface = ourConfig;

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    when(proxyFactory.createProxyTransaction(controller, ppIface, serverTransaction, sipRequest))
        .thenReturn(statelessTransaction);

    doNothing().when(statelessTransaction).addProxyRecordRoute(sipRequest, ppIface);
    doNothing().when(statelessTransaction).proxyTo(sipRequest, cookie, ppIface);

    DsProxyController ctrlr = (DsAppController) controller;
    proxyInterface = ctrlr.onNewRequest(serverTransaction, sipRequest);
    //    Assert.assertNotNull(proxy);

    //    Location loc = new Location(sipRequest.getURI());
    //    // loc.setProcessRoute(true);
    //    loc.setNetwork(dsNetwork);
    //    ctrlr.usingRouteHeader = false;
    //
    //    // This is to set ourRequest global variable
    //
    //    ctrlr.setRequest(sipRequest);
    //    ctrlr.setProxyTransaction(proxyInterface);
    //
    //    ctrlr.proxyTo(loc, sipRequest, null);

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    verify(proxyInterface)
        .proxyTo(
            argumentCaptor.capture(),
            any(DsProxyCookieInterface.class),
            any(DsProxyBranchParamsInterface.class));

    DsSipRequest requestReceived = argumentCaptor.getValue();
    Assert.assertNotNull(requestReceived);
  }

  @Test
  public void testProxyToWithoutProcessRoute() throws Exception {
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
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyInterface proxyInterface = mock(DsProxyInterface.class);
    DsProxyCookieInterface cookie = mock(DsProxyCookieInterface.class);
    DsProxyParamsInterface ppIface = ourConfig;

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    when(proxyFactory.createProxyTransaction(controller, ppIface, serverTransaction, sipRequest))
        .thenReturn(proxyInterface);

    doNothing().when(proxyInterface).addProxyRecordRoute(sipRequest, ppIface);
    doNothing().when(proxyInterface).proxyTo(sipRequest, cookie, ppIface);

    DsProxyController ctrlr = (DsAppController) controller;

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);
    ctrlr.usingRouteHeader = false;

    ctrlr.setRequest(sipRequest);
    ctrlr.setProxyTransaction(proxyInterface);

    ctrlr.proxyTo(loc, sipRequest, null);

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    verify(proxyInterface)
        .proxyTo(
            argumentCaptor.capture(),
            any(DsProxyCookieInterface.class),
            any(DsProxyBranchParamsInterface.class));

    DsSipRequest requestReceived = argumentCaptor.getValue();
    Assert.assertNotNull(requestReceived);
  }

  @Test
  public void testonSuccessResponse() throws Exception {
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

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyTransaction proxyTransaction = mock(DsProxyTransaction.class);
    DsProxyParamsInterface ppIface = ourConfig;

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    doNothing().when(app).handleResponse(any(IDhruvaMessage.class));
    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);
    DsProxyCookieThing cookie = new DsProxyCookieThing(loc, adaptor, sipRequest);
    DsProxyClientTransaction clientTrans = mock(DsProxyClientTransaction.class);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    controller.onSuccessResponse(proxyTransaction, cookie, clientTrans, resp);

    verify(app).handleResponse(argumentCaptor.capture());
    IDhruvaMessage respMessage = argumentCaptor.getValue();
    Assert.assertNotNull(respMessage);

    // There can be some more asserts

  }

  @Test
  public void testOnFailureResponse() throws Exception {
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

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyTransaction proxyTransaction = mock(DsProxyTransaction.class);

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    doNothing().when(app).handleResponse(null);
    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);
    DsProxyCookieThing cookie = new DsProxyCookieThing(loc, adaptor, sipRequest);
    DsProxyClientTransaction clientTrans = mock(DsProxyClientTransaction.class);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE, sipRequest);

    controller.onFailureResponse(proxyTransaction, cookie, clientTrans, resp);

    verify(app).handleResponse(argumentCaptor.capture());
    IDhruvaMessage respMessage = argumentCaptor.getValue();
    Assert.assertNotNull(respMessage);

    // Global failure response
    DsSipResponse resp6xx =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_SERVICE_UNAVAILABLE, sipRequest);

    controller.onGlobalFailureResponse(proxyTransaction, cookie, clientTrans, resp6xx);

    verify(app, times(2)).handleResponse(argumentCaptor.capture());
    IDhruvaMessage respMessage6xx = argumentCaptor.getValue();
    Assert.assertNotNull(respMessage6xx);
  }

  @Test
  public void testonProvisionalResponses() throws Exception {
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

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyTransaction proxyTransaction = mock(DsProxyTransaction.class);

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    doNothing().when(app).handleResponse(null);
    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);
    DsProxyCookieThing cookie = new DsProxyCookieThing(loc, adaptor, sipRequest);
    DsProxyClientTransaction clientTrans = mock(DsProxyClientTransaction.class);

    DsSipResponse resp100Trying =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_TRYING, sipRequest);

    controller.onProvisionalResponse(proxyTransaction, cookie, clientTrans, resp100Trying);

    verify(app).handleResponse(argumentCaptor.capture());
    IDhruvaMessage respMessage1 = argumentCaptor.getValue();
    Assert.assertNotNull(respMessage1);

    DsSipResponse resp180Ringing =
        DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_RINGING, sipRequest);

    controller.onProvisionalResponse(proxyTransaction, cookie, clientTrans, resp180Ringing);

    verify(app, times(2)).handleResponse(argumentCaptor.capture());
    IDhruvaMessage respMessage2 = argumentCaptor.getValue();
    Assert.assertNotNull(respMessage2);
  }

  @Test
  public void testOnRedirectResponses() throws Exception {
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

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyTransaction proxyTransaction = mock(DsProxyTransaction.class);

    DsProxyParamsInterface ppIface = ourConfig;

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    doNothing().when(app).handleResponse(null);
    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);
    DsProxyCookieThing cookie = new DsProxyCookieThing(loc, adaptor, sipRequest);
    DsProxyClientTransaction clientTrans = mock(DsProxyClientTransaction.class);

    DsSipResponse resp =
        DsProxyResponseGenerator.createResponse(
            DsSipResponseCode.DS_RESPONSE_MOVED_TEMPORARILY, sipRequest);

    controller.onRedirectResponse(proxyTransaction, cookie, clientTrans, resp);

    verify(app).handleResponse(argumentCaptor.capture());
    IDhruvaMessage respMessage = argumentCaptor.getValue();
    Assert.assertNotNull(respMessage);
  }

  @Test
  public void testonRequestTimeOut() throws Exception {
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

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyTransaction proxyTransaction = mock(DsProxyTransaction.class);

    DsProxyParamsInterface ppIface = ourConfig;

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    doNothing().when(app).handleResponse(null);
    ArgumentCaptor<IDhruvaMessage> argumentCaptor = ArgumentCaptor.forClass(IDhruvaMessage.class);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    ProxyAdaptor adaptor = (ProxyAdaptor) f.getProxyAdaptor((DsProxyController) controller, app);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);
    DsProxyCookieThing cookie = new DsProxyCookieThing(loc, adaptor, sipRequest);
    DsProxyClientTransaction clientTrans = mock(DsProxyClientTransaction.class);

    ((DsProxyController) controller).setRequest(sipRequest);

    controller.onRequestTimeOut(proxyTransaction, cookie, clientTrans);

    verify(app).handleResponse(argumentCaptor.capture());
    IDhruvaMessage respMessage = argumentCaptor.getValue();
    Assert.assertNotNull(respMessage);
    DsSipResponse sipResponse =
        (DsSipResponse) MessageConvertor.convertDhruvaMessageToSipMessage(respMessage);
    Assert.assertNotNull(sipResponse);
    Assert.assertEquals(sipResponse.getStatusCode(), 408);
  }

  @Test
  public void testOnICMPError() throws Exception {
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

    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    DsProxyTransaction proxyTransaction = mock(DsProxyTransaction.class);

    DsControllerInterface controller =
        cf.getController(serverTransaction, sipRequest, pf, app, proxyFactory);

    doNothing().when(app).handleResponse(null);

    ProxyAdaptorFactory f = new ProxyAdaptorFactory();
    ProxyAdaptor adaptor = mock(ProxyAdaptor.class);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(false);
    loc.setNetwork(dsNetwork);
    DsProxyCookieThing cookie = new DsProxyCookieThing(loc, adaptor, sipRequest);
    DsProxyClientTransaction clientTrans = mock(DsProxyClientTransaction.class);

    ((DsProxyController) controller).setRequest(sipRequest);
    ArgumentCaptor<Optional> argumentCaptor = ArgumentCaptor.forClass(Optional.class);

    controller.onICMPError(proxyTransaction, cookie, clientTrans);

    // ICMP code is 4
    // There is no response body sent to App
    verify(adaptor).handleResponse(any(Location.class), argumentCaptor.capture(), eq(4));
    Optional<DsSipResponse> respMessage = argumentCaptor.getValue();
    Assert.assertNotNull(respMessage);
    Assert.assertFalse(respMessage.isPresent());
  }
}
