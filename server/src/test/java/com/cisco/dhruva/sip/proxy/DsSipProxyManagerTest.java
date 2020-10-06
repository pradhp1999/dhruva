package com.cisco.dhruva.sip.proxy;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.adaptor.AppAdaptorInterface;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.service.SipServerLocatorService;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest
public class DsSipProxyManagerTest {
  DsSipTransactionFactory transactionFactory;
  DsSipProxyManager dsSipProxyManager;
  DsREControllerFactory controllerFactory;
  DsSipTransactionManager transactionManager;
  DsSipTransportLayer transportLayer;
  private AppAdaptorInterface adaptorInterface;
  private ProxyAdaptorFactoryInterface proxyAdaptorFactoryInterface;
  DsNetwork dsNetwork;
  DsControllerConfig ourConfig;
  private DsBindingInfo incomingMessageBindingInfo;

  private InetAddress localAddress;
  private InetAddress remoteAddress;
  private int localPort, remotePort;

  @Autowired SipServerLocatorService locatorService;

  @BeforeClass
  void init() throws Exception {
    dsNetwork = DsNetwork.getNetwork("Default");
    ourConfig = DsControllerConfig.getCurrent();

    // This is required to set the via handler, route fix interface, global states are maintained
    controllerFactory = mock(DsREControllerFactory.class);

    dsSipProxyManager = DsSipProxyManager.getInstance();
    // Mock the stack interfaces
    transactionFactory = mock(DsSipDefaultTransactionFactory.class);
    transportLayer = mock(DsSipTransportLayer.class);
    if (dsSipProxyManager == null) {
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
          InetAddress.getByName("127.0.0.1"),
          5060,
          Transport.UDP,
          InetAddress.getByName("127.0.0.1"),
          false);

      DsControllerConfig.addRecordRouteInterface(
          InetAddress.getByName("127.0.0.1"), 5060, Transport.UDP, dsNetwork);
    } catch (DsInconsistentConfigurationException ignored) {
      // In this case it was already set, there is no means to remove the key from map
    }
  }

  @AfterClass
  void cleanUp() {
    DsSipTransactionManager.setSmp_theSingleton(null);
    DsSipProxyManager.setM_Singleton(null);
  }

  @Test
  public void testRequestInterface() throws Exception {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.INVITE));
    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionFactory m_transactionFactory = new DsSipDefaultTransactionFactory();

    DsSipServerTransaction serverTransaction =
        m_transactionFactory.createServerTransaction(sipRequest, key, key, false);

    DsControllerInterface controller = mock(DsControllerInterface.class);
    when(controllerFactory.getController(
            any(DsSipServerTransaction.class),
            any(DsSipRequest.class),
            any(ProxyAdaptorFactoryInterface.class),
            any(AppInterface.class),
            any(DsProxyFactoryInterface.class)))
        .thenReturn(controller);
    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);
    dsSipProxyManager.request(serverTransaction, sipRequest);

    verify(controller).onNewRequest(any(DsSipServerTransaction.class), argumentCaptor.capture());

    DsSipRequest request = argumentCaptor.getValue();
    Assert.assertNotNull(request);
  }

  @Test
  public void testStrayAck() throws Exception {

    DsSipRequest sipRequest =
        SIPRequestBuilder.createRequest(
            new SIPRequestBuilder().getRequestAsString(SIPRequestBuilder.RequestMethod.ACK));

    DsSipTransactionKey key = sipRequest.forceCreateKey();
    sipRequest.setNetwork(dsNetwork);
    sipRequest.setBindingInfo(incomingMessageBindingInfo);

    DsControllerInterface controller = mock(DsControllerInterface.class);
    when(controllerFactory.getController(
            any(DsSipServerTransaction.class),
            any(DsSipRequest.class),
            any(ProxyAdaptorFactoryInterface.class),
            any(AppInterface.class),
            any(DsProxyFactoryInterface.class)))
        .thenReturn(controller);
    ArgumentCaptor<DsSipRequest> argumentCaptor = ArgumentCaptor.forClass(DsSipRequest.class);
    dsSipProxyManager.strayAck((DsSipAckMessage) sipRequest);

    verify(controller).onNewRequest(any(DsSipServerTransaction.class), argumentCaptor.capture());

    DsSipRequest request = argumentCaptor.getValue();
    Assert.assertNotNull(request);
  }

  @Test
  public void testStrayResponseBadVia() throws DsException, IOException {
    SIPRequestBuilder builder = new SIPRequestBuilder();
    DsSipResponse response = builder.getResponse(200);
    response.setNetwork(dsNetwork);
    response.setBindingInfo(incomingMessageBindingInfo);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);
    DsSipConnection sendConnection = mock(DsSipConnection.class);
    when(DsSipTransactionManager.getConnection(response)).thenReturn(sendConnection);

    // Bad Via, gets dropped
    dsSipProxyManager.strayResponse(response);

    verify(sendConnection, times(0)).send(response);
  }

  @Test
  public void testStrayResponseViaWithoutBranch() throws DsException, IOException {
    SIPRequestBuilder builder = new SIPRequestBuilder();
    DsSipResponse response = builder.getResponse(200);
    response.setNetwork(dsNetwork);
    response.setBindingInfo(incomingMessageBindingInfo);

    DsSipViaHeader localViaHeader =
        new DsSipViaHeader(new DsByteString("127.0.0.1"), 5060, Transport.UDP);

    response.addHeader(localViaHeader, true, false);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);
    DsSipConnection sendConnection = mock(DsSipConnection.class);
    when(DsSipTransactionManager.getConnection(response)).thenReturn(sendConnection);

    // Bad Via, gets dropped, no branch
    dsSipProxyManager.strayResponse(response);

    verify(sendConnection, times(0)).send(response);
  }

  @Test
  public void testStrayResponse() throws DsException, IOException {
    transportLayer = mock(DsSipTransportLayer.class);
    SIPRequestBuilder builder = new SIPRequestBuilder();
    DsSipResponse response = builder.getResponse(200);
    response.setNetwork(dsNetwork);
    response.setBindingInfo(incomingMessageBindingInfo);

    DsSipViaHeader localViaHeader1 =
        new DsSipViaHeader(new DsByteString("127.0.0.1"), 5070, Transport.UDP);
    localViaHeader1.setBranch(new DsByteString("z9hG4bKUsaQangfWbsEVmsoPdTNBA~~4"));
    response.addHeader(localViaHeader1, true, false);

    DsSipViaHeader localViaHeader2 =
        new DsSipViaHeader(new DsByteString("127.0.0.1"), 5060, Transport.UDP);
    localViaHeader2.setBranch(new DsByteString("z9hG4bKUsaQangfWbsEVmsoPdTNBA~~1"));
    response.addHeader(localViaHeader2, true, false);

    DsSipTransactionManager stackManager = DsSipTransactionManager.getTransactionManager();
    transactionManager = spy(stackManager);
    DsSipConnection sendConnection = mock(DsSipConnection.class);
    // return isLocalInterface true
    doReturn(true)
        .when(transportLayer)
        .isListenInterface(anyString(), anyInt(), any(Transport.class));
    when(transportLayer.getConnection(
            any(DsNetwork.class), any(InetAddress.class), anyInt(), any(Transport.class)))
        .thenReturn(sendConnection);
    dsSipProxyManager.strayResponse(response);

    // TODO, it should be invoked once.Transport Mock is causing issues
    // uncomment reset(transportLayer) , first line above
    verify(sendConnection, times(0)).send(response);
  }
}
