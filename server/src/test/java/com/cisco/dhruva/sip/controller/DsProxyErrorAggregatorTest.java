package com.cisco.dhruva.sip.controller;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.adaptor.AppAdaptorInterface;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.cisco.dhruva.sip.controller.exceptions.DsInconsistentConfigurationException;
import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@SpringBootTest
public class DsProxyErrorAggregatorTest {

  DsSipTransactionFactory transactionFactory;
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
    DsREControllerFactory controllerFactory = new DsREControllerFactory();
    DsSipProxyManager dsSipProxyManager = DsSipProxyManager.getInstance();
    if (dsSipProxyManager == null) {
      DsSipTransportLayer transportLayer = mock(DsSipTransportLayer.class);
      // Mock the stack interfaces
      transactionFactory = mock(DsSipDefaultTransactionFactory.class);
      dsSipProxyManager =
          new DsSipProxyManager(
              transportLayer, controllerFactory, transactionFactory, locatorService);
    }
    DsSipProxyManager sipProxyManager = spy(dsSipProxyManager);
    DsSipProxyManager.setM_Singleton(sipProxyManager);
    sipProxyManager.setRouteFixInterface(controllerFactory);

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

    // Add listen interfaces in DsControllerConfig, causes issues in getVia while sending out the
    // packet
    try {
      DsControllerConfig.addListenInterface(
          dsNetwork,
          InetAddress.getByName("127.0.0.1"),
          5060,
          Transport.UDP,
          InetAddress.getByName("127.0.0.1"));

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

  @Test()
  public void testEnableProxyErrorAggregator() throws DsException, IOException {}
}
