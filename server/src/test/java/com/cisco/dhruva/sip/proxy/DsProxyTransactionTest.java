package com.cisco.dhruva.sip.proxy;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.adaptor.AppAdaptorInterface;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.common.messaging.models.IDhruvaMessage;
import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.cisco.dhruva.sip.controller.DsAppController;
import com.cisco.dhruva.sip.controller.DsProxyController;
import com.cisco.dhruva.sip.controller.DsProxyResponseGenerator;
import com.cisco.dhruva.sip.controller.DsREControllerFactory;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.*;

@SpringBootTest
public class DsProxyTransactionTest {
  DsSipTransactionFactory transactionFactory;
  DsSipTransactionManager transactionManager;
  private AppAdaptorInterface adaptorInterface;
  private ProxyAdaptorFactoryInterface proxyAdaptorFactoryInterface;
  DsNetwork dsNetwork;
  DsNetwork externalIpEnabledNetwork;
  DsControllerConfig ourConfig;
  private DsBindingInfo incomingMessageBindingInfo1;
  private DsBindingInfo incomingMessageBindingInfo2;

  private InetAddress localAddress;
  private InetAddress remoteAddress;
  private int localPort1, localPort2, remotePort;

  @Autowired SipServerLocatorService locatorService;

  @BeforeClass
  void init() throws Exception {
    dsNetwork = DsNetwork.getNetwork("Default");
    externalIpEnabledNetwork = DsNetwork.getNetwork("External_IP_enabled");
    ourConfig = DsControllerConfig.getCurrent();

    // This is required to set the via handler, route fix interface, global states are maintained
    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipProxyManager dsSipProxyManager = DsSipProxyManager.getInstance();
    // Mock the stack interfaces
    transactionFactory = mock(DsSipDefaultTransactionFactory.class);
    if (dsSipProxyManager == null) {
      DsSipTransportLayer transportLayer = mock(DsSipTransportLayer.class);
      dsSipProxyManager =
              new DsSipProxyManager(
                      transportLayer, controllerFactory, transactionFactory, locatorService);
    }

    DsSipProxyManager.setM_Singleton(dsSipProxyManager);
    dsSipProxyManager.setRouteFixInterface(controllerFactory);

    adaptorInterface = mock(AppAdaptorInterface.class);
    proxyAdaptorFactoryInterface = mock(ProxyAdaptorFactoryInterface.class);

    localAddress = InetAddress.getByName("127.0.0.1");
    remoteAddress = InetAddress.getByName("127.0.0.1");
    localPort1 = 5060;
    localPort2 = 5061;
    remotePort = 5070;
    incomingMessageBindingInfo1 =
            new DsBindingInfo(localAddress, localPort1, localAddress, remotePort, Transport.UDP);
    incomingMessageBindingInfo1.setNetwork(dsNetwork);

    incomingMessageBindingInfo2 =
            new DsBindingInfo(localAddress, localPort2, localAddress, remotePort, Transport.UDP);
    incomingMessageBindingInfo2.setNetwork(externalIpEnabledNetwork);

    DsSipServerTransactionImpl.setThreadPoolExecutor(
            (ThreadPoolExecutor) Executors.newFixedThreadPool(1));

    DsSipClientTransactionImpl.setThreadPoolExecutor(
            (ThreadPoolExecutor) Executors.newFixedThreadPool(1));
    DsProxyController.setIsCreateDnsServerGroup(false);

    // Add listen interfaces in DsControllerConfig, causes issues in getVia while sending out the
    // packet
    try {
      DsControllerConfig.addListenInterface(
              dsNetwork,
              InetAddress.getByName("127.0.0.1"),
              localPort1,
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

  private void setDhruvaProp() {
    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);
  }

  @AfterMethod
  private void resetDhruvaProp() {
    DsNetwork.setDhruvaConfigProperties(null);
  }

  @Test
  public void testProxyClientTransaction() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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

  @DataProvider
  public Object[] getNetworkAndBindingInfo() {

    return new RRViaHeaderValidationDataProvider[][] {
            {
                    new RRViaHeaderValidationDataProvider(
                            dsNetwork, incomingMessageBindingInfo1, null, "1.1.1.1", true)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            externalIpEnabledNetwork, incomingMessageBindingInfo2, null, "1.1.1.1", true)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            dsNetwork, incomingMessageBindingInfo1, null, "1.1.1.1", false)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            externalIpEnabledNetwork, incomingMessageBindingInfo2, null, "1.1.1.1", false)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            dsNetwork, incomingMessageBindingInfo1, null, "dhruva.sjc.webex.com", true)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            externalIpEnabledNetwork,
                            incomingMessageBindingInfo2,
                            null,
                            "dhruva.sjc.webex.com",
                            true)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            dsNetwork, incomingMessageBindingInfo1, null, "dhruva.sjc.webex.com", false)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            externalIpEnabledNetwork,
                            incomingMessageBindingInfo2,
                            null,
                            "dhruva.sjc.webex.com",
                            false)
            }
    };
  }

  @Test(dataProvider = "getNetworkAndBindingInfo")
  public void testProxyToAddViaClientTransaction(RRViaHeaderValidationDataProvider input)
          throws Exception {

    try {
      DsControllerConfig.addListenInterface(
              externalIpEnabledNetwork,
              InetAddress.getByName("127.0.0.1"),
              localPort2,
              Transport.UDP,
              InetAddress.getByName("127.0.0.1"),
              true);

    } catch (DsInconsistentConfigurationException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    }

    reset(transactionFactory);

    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);

    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(input.network);
    sipRequest.setBindingInfo(input.bindingInfo);

    System.out.println("Initial request : " + sipRequest.toString());

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
            .thenReturn(adaptorInterface);

    doReturn(input.isHostPortEnabled).when(dhruvaSIPConfigProperties).isHostPortEnabled();
    doReturn(input.hostIpOrFqdn).when(dhruvaSIPConfigProperties).getHostInfo();
    // when(dhruvaSIPConfigProperties.isHostPortEnabled()).thenReturn(input.isHostPortEnabled);
    // when(dhruvaSI PConfigProperties.getHostInfo()).thenReturn(input.hostIpOrFqdn);

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

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(input.network);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
            .when(appInterfaceMock)
            .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    System.out.println("Updated Request (Dhruva Via header added) : " + sipRequest.toString());

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

    // check Via header IP:port added by CP
    if (!input.isHostPortEnabled || input.network.getName().equals("Default")) {
      Assert.assertEquals(
              sipViaHeader.getHost().toString(), input.bindingInfo.getLocalAddress().getHostAddress());
    } else {
      Assert.assertEquals(sipViaHeader.getHost().toString(), input.hostIpOrFqdn);
    }

    Assert.assertEquals(sipViaHeader.getPort(), input.bindingInfo.getLocalPort());

    Assert.assertNotNull(proxy.getClientTransaction());

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
            proxy.getTransactionInterfaces();

    DsSipResponse resp =
            DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    System.out.println("Response expected to be received by Dhruva: " + resp.toString());

    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);

    // Check Via in received 200OK. CP Via has to be removed before sending out
    DsSipViaHeader topMostViaIn200OkSentOut = (DsSipViaHeader) resp.getViaHeader();
    Assert.assertEquals(
            topMostViaIn200OkSentOut.getHost().toString(),
            input.bindingInfo.getLocalAddress().getHostAddress());
    Assert.assertNotEquals(topMostViaIn200OkSentOut.getPort(), input.bindingInfo.getLocalPort());

    System.out.println("Response after Via header removal: " + resp.toString());

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));

    // Retransmission of 200 OK should not be sent upwards
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);
    // No increment in verify, same as previous

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));
  }

  @DataProvider
  public Object[] getNetworkBindingInfoAndRR() {

    return new RRViaHeaderValidationDataProvider[][] {
            {
                    new RRViaHeaderValidationDataProvider(
                            dsNetwork,
                            incomingMessageBindingInfo1,
                            "<sip:rr$n=Default@127.0.0.1:5060;transport=udp;lr>",
                            "1.1.1.1",
                            true)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            externalIpEnabledNetwork,
                            incomingMessageBindingInfo2,
                            "<sip:rr$n=External_IP_enabled@1.1.1.1:5061;transport=udp;lr>",
                            "1.1.1.1",
                            true)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            dsNetwork,
                            incomingMessageBindingInfo1,
                            "<sip:rr$n=Default@127.0.0.1:5060;transport=udp;lr>",
                            "1.1.1.1",
                            false)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            externalIpEnabledNetwork,
                            incomingMessageBindingInfo2,
                            "<sip:rr$n=External_IP_enabled@127.0.0.1:5061;transport=udp;lr>",
                            "1.1.1.1",
                            false)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            dsNetwork,
                            incomingMessageBindingInfo1,
                            "<sip:rr$n=Default@127.0.0.1:5060;transport=udp;lr>",
                            "dhruva.sjc.webex.com",
                            true)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            externalIpEnabledNetwork,
                            incomingMessageBindingInfo2,
                            "<sip:rr$n=External_IP_enabled@dhruva.sjc.webex.com:5061;transport=udp;lr>",
                            "dhruva.sjc.webex.com",
                            true)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            dsNetwork,
                            incomingMessageBindingInfo1,
                            "<sip:rr$n=Default@127.0.0.1:5060;transport=udp;lr>",
                            "dhruva.sjc.webex.com",
                            false)
            },
            {
                    new RRViaHeaderValidationDataProvider(
                            externalIpEnabledNetwork,
                            incomingMessageBindingInfo2,
                            "<sip:rr$n=External_IP_enabled@127.0.0.1:5061;transport=udp;lr>",
                            "dhruva.sjc.webex.com",
                            false)
            }
    };
  }

  @Test(dataProvider = "getNetworkBindingInfoAndRR")
  public void testProxyToAddRRClientTransaction(RRViaHeaderValidationDataProvider input)
          throws Exception {

    try {
      DsControllerConfig.addListenInterface(
              externalIpEnabledNetwork,
              InetAddress.getByName("127.0.0.1"),
              localPort2,
              Transport.UDP,
              InetAddress.getByName("127.0.0.1"),
              true);

    } catch (DsInconsistentConfigurationException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    }

    DhruvaSIPConfigProperties dhruvaSIPConfigProperties = mock(DhruvaSIPConfigProperties.class);
    DsNetwork.setDhruvaConfigProperties(dhruvaSIPConfigProperties);

    reset(transactionFactory);

    DsControllerConfig.addRecordRouteInterface(
            InetAddress.getByName("127.0.0.1"), 5060, Transport.UDP, dsNetwork);
    DsControllerConfig.addRecordRouteInterface(
            InetAddress.getByName("127.0.0.1"), 5061, Transport.UDP, externalIpEnabledNetwork);

    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(input.network);
    sipRequest.setBindingInfo(input.bindingInfo);

    System.out.println("Initial request : " + sipRequest.toString());

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
            .thenReturn(adaptorInterface);

    doReturn(input.isHostPortEnabled).when(dhruvaSIPConfigProperties).isHostPortEnabled();
    doReturn(input.hostIpOrFqdn).when(dhruvaSIPConfigProperties).getHostInfo();

    // when(dhruvaSIPConfigProperties.isHostPortEnabled()).thenReturn(input.isHostPortEnabled);
    // when(dhruvaSIPConfigProperties.getHostInfo()).thenReturn(input.hostIpOrFqdn);

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

    DsProxyController ctrlr = spy((DsProxyController) controller);

    Location loc = new Location(sipRequest.getURI());
    loc.setProcessRoute(true);
    loc.setNetwork(input.network);

    AppAdaptorInterface appInterfaceMock = mock(AppAdaptorInterface.class);
    doNothing()
            .when(appInterfaceMock)
            .handleResponse(any(Location.class), any(Optional.class), any(int.class));

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    System.out.println("Updated Request (Dhruva RR header added) : " + sipRequest.toString());

    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);

    verify(transactionFactory)
            .createClientTransaction(
                    argumentCaptor.capture(),
                    any(DsSipClientTransportInfo.class),
                    any(DsSipClientTransactionInterface.class));

    DsSipRequest request = argumentCaptor.getValue();
    Assert.assertNotNull(request);
    Assert.assertEquals(request.getMethod(), DsByteString.newInstance("INVITE"));

    DsSipHeaderList addedRRHeaders = request.getHeaders(DsSipConstants.RECORD_ROUTE);
    DsSipRecordRouteHeader expectedRRHeader =
            new DsSipRecordRouteHeader(input.expectedRR.getBytes());

    // check RR header IP:port added by CP
    Assert.assertEquals(addedRRHeaders.getFirst().toString(), expectedRRHeader.toString());

    Assert.assertNotNull(proxy.getClientTransaction());

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
            proxy.getTransactionInterfaces();

    DsSipResponse resp =
            DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    System.out.println("Response expected to be received by Dhruva : " + resp.toString());

    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);

    System.out.println("Response after changes (RR will not be removed) : " + resp.toString());

    // Check RR in received 200OK is same as one sent in INVITE
    DsSipHeaderList respRRHeader = resp.getHeaders(DsSipConstants.RECORD_ROUTE);
    Assert.assertEquals(respRRHeader.getFirst().toString(), expectedRRHeader.toString());

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));

    // Retransmission of 200 OK should not be sent upwards
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);
    // No increment in verify, same as previous
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));
  }

  // TODO, FIXME respond is called on server side
  @Test
  public void testProxyClientResponseWithRRHeader() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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

    DsSipResponse resp =
            DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_OK, sipRequest);

    DsSipRecordRouteHeader rrHeader =
            new DsSipRecordRouteHeader(
                    "Record-Route: <sip:rr$n=Default@1.2.3.4:5060;transport=udp;lr>".getBytes());

    resp.addHeader(rrHeader);

    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));

    // Retransmission of 200 OK should not be sent upwards
    transactionInterfaces.finalResponse(mockSipClientTransaction, resp);
    // No increment in verify, same as previous
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(1));
  }

  @Test
  public void testTransactionInterfacesClientTimeout() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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

    transactionInterfaces.timeOut(mockSipClientTransaction);

    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(408));
  }

  @Test
  public void testTransactionInterfacesClientProvisionalResponse() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
            proxy.getTransactionInterfaces();

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    DsSipResponse resp =
            DsProxyResponseGenerator.createResponse(DsSipResponseCode.DS_RESPONSE_RINGING, sipRequest);

    transactionInterfaces.provisionalResponse(mockSipClientTransaction, resp);
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(180));
  }

  @Test
  public void testProxyClientTransactionFinalResponses() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
            .thenReturn(adaptorInterface);

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
  public void testTransactionInterfacesClientICMPError() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
            .thenReturn(adaptorInterface);

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
  public void testTransactionInterfacesClientCloseEvent() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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

    DsProxyTransaction.TransactionInterfaces transactionInterfaces =
            proxy.getTransactionInterfaces();

    ctrlr.proxyTo(loc, sipRequest, appInterfaceMock);

    transactionInterfaces.close(mockSipClientTransaction);
    verify(appInterfaceMock).handleResponse(any(Location.class), any(Optional.class), eq(500));
  }

  @Test
  public void testTransactionInterfacesServerICMPError() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
            .thenReturn(adaptorInterface);

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
  public void testTransactionInterfacesServerCloseEvent() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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
    verify(appInterfaceMock).handleRequest(isNull());
  }

  @Test
  public void testTransactionInterfacesServerAckEvent() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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
    verify(appInterfaceMock).handleRequest(any(DsSipAckMessage.class));
  }

  @Test
  public void testTransactionInterfacesServerTimeout() throws Exception {

    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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
  public void testProxyToStrayACKClientTransaction() throws Exception {

    reset(transactionFactory);
    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.ACK));

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

    when(proxyAdaptorFactoryInterface.getProxyAdaptor(((DsAppController) controller), app))
            .thenReturn(adaptorInterface);

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
  public void testClientInvalidParamException() throws Exception {
    reset(transactionFactory);
    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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
  public void testClientDestinationUnreachableException() throws Exception {
    reset(transactionFactory);
    setDhruvaProp();
    DsSipRequest sipRequest =
            SIPRequestBuilder.createRequest(
                    new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo1);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
            m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerFactoryInterface cf = new DsREControllerFactory();

    AppInterface app = mock(AppSession.class);
    DsProxyFactoryInterface proxyFactory = new DsProxyFactory();
    DsControllerInterface controller =
            cf.getController(
                    serverTransaction, sipRequest, proxyAdaptorFactoryInterface, app, proxyFactory, null);

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

  public class RRViaHeaderValidationDataProvider {
    public DsNetwork network;
    public DsBindingInfo bindingInfo;
    public String expectedRR;
    public String hostIpOrFqdn;
    public boolean isHostPortEnabled;

    public RRViaHeaderValidationDataProvider(
            DsNetwork network,
            DsBindingInfo bindingInfo,
            String expectedRR,
            String hostIpOrFqdn,
            boolean isHostPortEnabled) {
      this.network = network;
      this.bindingInfo = bindingInfo;
      this.expectedRR = expectedRR;
      this.hostIpOrFqdn = hostIpOrFqdn;
      this.isHostPortEnabled = isHostPortEnabled;
    }

    public String toString() {
      return "Network: {"
              + network.toString()
              + "}; "
              + "BindingInfo: {"
              + bindingInfo.toString()
              + "}; "
              + "CP RR expected in sip msg: {"
              + expectedRR
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
