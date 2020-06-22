package com.cisco.dhruva.sip.controller;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.adaptor.AppAdaptorInterface;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DsProxyErrorAggregatorTest {

  private DsSipTransportLayer transportLayer;
  private DsSipProxyManager sipProxyManager;
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
      transportLayer = mock(DsSipTransportLayer.class);
      // Mock the stack interfaces
      transactionFactory = mock(DsSipDefaultTransactionFactory.class);
      dsSipProxyManager =
          new DsSipProxyManager(transportLayer, controllerFactory, transactionFactory);
    }
    sipProxyManager = spy(dsSipProxyManager);
    DsSipProxyManager.setM_Singleton(sipProxyManager);
    sipProxyManager.setRouteFixInterface(controllerFactory);

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

  @Test()
  public void testEnableProxyErrorAggregator() throws DsException, IOException {}
}
