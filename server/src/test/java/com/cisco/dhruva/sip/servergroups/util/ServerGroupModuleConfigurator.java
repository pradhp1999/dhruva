package com.cisco.dhruva.sip.servergroups.util;

import static org.mockito.Mockito.mock;

import com.cisco.dhruva.adaptor.ProxyAdaptorFactory;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.loadbalancer.LBFactory;
import com.cisco.dhruva.loadbalancer.LBRepositoryHolder;
import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.loadbalancer.ServerGroupInterface;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.sip.DsPings.DsErrorResponseCodeSet;
import com.cisco.dhruva.sip.controller.DsAppController;
import com.cisco.dhruva.sip.proxy.DsProxyFactoryInterface;
import com.cisco.dhruva.sip.servergroups.*;
import com.cisco.dhruva.sip.servergroups.util.ServerGroupInput.InputServerGroup.Elements;
import com.cisco.dhruva.sip.servergroups.util.interfaces.ModuleConfigurator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class ServerGroupModuleConfigurator implements ModuleConfigurator {
  ServerGroupInput testCase;
  HashMap<DsByteString, AbstractServerGroup> serverGroupMap =
      new HashMap<DsByteString, AbstractServerGroup>();
  ServerGroupInterface serverGroup;
  int loadBalancer;
  Transport incomingTransport;
  Transport outgoingTransport;
  DsNetwork testNetwork;
  //  DsScriptController dsScriptController;
  DsAppController dsAppController;
  LBRepositoryHolder lbRepositoryHolder;
  static final int udpTries = 10;
  static final int tcpTries = 1;
  static final int tlsTries = 1;

  public ServerGroupModuleConfigurator(ServerGroupInput testCase) {
    this.testCase = testCase;
  }

  public void configure() throws Exception {
    testNetwork = DsNetwork.getNetwork(new DsByteString("testNetwork"));
    ServerGroupInput.InputServerGroup inputServerGroup = testCase.getInputServerGroup();
    ServerGroupInput.InputServerGroup.TestConfig.TestCombination testCombination =
        inputServerGroup.getTestConfig().testCombination;
    String serverGroupName = inputServerGroup.getName();
    DsByteString testNetwork = new DsByteString("testNetwork");

    configureLoadBalancer(testCombination.loadBalancerType[0]);
    configureIncomingTransport(testCombination.incomingTransport[0]);
    configureOutgoingTransport(testCombination.outgoingTransport[0]);

    TreeSet<ServerGroupElementInterface> setOfElements =
        createServerGroupElements(inputServerGroup, testNetwork);

    serverGroup =
        new ServerGroup(
            new DsByteString(serverGroupName), testNetwork, setOfElements, loadBalancer, false);
    DsErrorResponseCodeSet failoverCodeSet = new DsErrorResponseCodeSet();
    for (int failoverCode : inputServerGroup.getFailover()) {
      failoverCodeSet.addErrorCode(failoverCode);
    }
    FailoverResponseCode.getInstance().setFailoverCodes(serverGroupName, failoverCodeSet);
    serverGroupMap.put(new DsByteString(serverGroupName), (AbstractServerGroup) serverGroup);
    LBFactory.setUDPTries(udpTries);
    LBFactory.setTCPTries(tcpTries);
    LBFactory.setTLSTries(tlsTries);

    //    CallProcessingConfig cpConfig = CallProcessingConfig.getInstance();
    //    cpConfig.setServerGroupBackupToPrimary();
    //    dsScriptController = new DsScriptController(cpConfig.getXCLRepository());
    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();
    AppInterface app = new AppSession();
    //    AppInterface app = ServerGroupTestController.app;
    dsAppController = new DsAppController(pf, app);
  }

  public void initController() {
    DsControllerConfig ourConfig = DsControllerConfig.getCurrent();
    int requestTimeout =
        DsConfigManager.getTimerValue(DsNetwork.getDefault(), DsSipConstants.serverTn);
    //    final int SEQ_REQUEST_TIMEOUT_DIVISIBLE =
    //        Integer.parseInt(System.getProperty("SEQ_REQUEST_TIMEOUT_DIVISIBLE", "2"));
    //    int sequentialSearchTimeout = requestTimeout / SEQ_REQUEST_TIMEOUT_DIVISIBLE;
    DsProxyFactoryInterface proxyFactory = mock(DsProxyFactoryInterface.class);
    dsAppController.init(
        ourConfig.getSearchType(),
        requestTimeout,
        ourConfig.getStateMode(),
        ourConfig.isRecursing(),
        ourConfig,
        ourConfig.getDefaultRetryAfterMilliSeconds(),
        lbRepositoryHolder,
        ourConfig.getNextHopFailureAction(),
        proxyFactory,
        null);
  }

  private void configureLoadBalancer(String inputLoadBalancerType) throws Exception {
    loadBalancer = 0;
    switch (inputLoadBalancerType) {
      case "highest-q":
        loadBalancer = SG.index_sgSgLbType_highest_q;
        break;
      case "call-id":
        loadBalancer = SG.index_sgSgLbType_call_id;
        break;
      case "request-uri":
        loadBalancer = SG.index_sgSgLbType_request_uri;
        break;
      case "to-uri":
        loadBalancer = SG.index_sgSgLbType_to_uri;
        break;
      case "weight":
        loadBalancer = SG.index_sgSgLbType_weight;
        break;
      case "Ms-Conversation-ID":
        loadBalancer = SG.index_sgSgLbType_ms_id;
        break;
      default:
        throw new Exception("Unknown loadBalancer Exception");
    }
  }

  private void configureIncomingTransport(String incomingTransportType) throws Exception {
    incomingTransport = Transport.NONE;
    switch (incomingTransportType) {
      case "udp":
        incomingTransport = Transport.UDP;
        break;
      case "tcp":
        incomingTransport = Transport.TCP;
        break;
      case "tls":
        incomingTransport = Transport.TLS;
        break;
      default:
        throw new Exception("Incoming transport unknown");
    }
  }

  private void configureOutgoingTransport(String incomingTransportType) throws Exception {
    outgoingTransport = Transport.NONE;
    switch (incomingTransportType) {
      case "udp":
        outgoingTransport = Transport.UDP;
        break;
      case "tcp":
        outgoingTransport = Transport.TCP;
        break;
      case "tls":
        outgoingTransport = Transport.TLS;
        break;
      default:
        throw new Exception("Outgoing transport unknown");
    }
  }

  private TreeSet<ServerGroupElementInterface> createServerGroupElements(
      ServerGroupInput.InputServerGroup serverGroup, DsByteString testNetwork) {
    TreeSet<ServerGroupElementInterface> setOfElements = new TreeSet<ServerGroupElementInterface>();
    for (Elements element : serverGroup.getElements()) {
      AbstractNextHop anh =
          new DefaultNextHop(
              testNetwork,
              new DsByteString(element.getIp()),
              element.getPort(),
              outgoingTransport,
              element.getQvalue(),
              new DsByteString(serverGroup.getName()));
      anh.setWeight(element.getWeight());
      setOfElements.add(anh);
    }
    return setOfElements;
  }

  public void markElementsDown() {
    List<Elements> inputSgElements = new ArrayList<Elements>();
    inputSgElements = Arrays.asList(testCase.getInputServerGroup().getElements());
    List<String> downElementIps = new ArrayList<String>();
    inputSgElements.forEach(
        element -> {
          if (((Elements) element).getTestConfig().getStatus().equals("down")) {
            downElementIps.add(((Elements) element).getIp());
          }
        });
    serverGroup
        .getElements()
        .forEach(
            anh -> {
              downElementIps.forEach(
                  downIp -> {
                    if (downIp.equals(((AbstractNextHop) anh).getDomainName().toString())) {
                      ((AbstractNextHop) anh)
                          .getGlobalWrapper()
                          .zeroTries("Making this element down");
                    }
                  });
            });
  }

  public HashMap<DsByteString, AbstractServerGroup> getServerGroupMap() {
    return serverGroupMap;
  }

  public void setServerGroupMap(HashMap<DsByteString, AbstractServerGroup> serverGroupMap) {
    this.serverGroupMap = serverGroupMap;
  }

  public ServerGroupInterface getServerGroup() {
    return serverGroup;
  }

  public void setServerGroup(ServerGroupInterface serverGroup) {
    this.serverGroup = serverGroup;
  }

  public int getLoadBalancer() {
    return loadBalancer;
  }

  public void setLoadBalancer(int loadBalancer) {
    this.loadBalancer = loadBalancer;
  }

  public Transport getIncomingTransport() {
    return incomingTransport;
  }

  public void setIncomingTransport(Transport incomingTransport) {
    this.incomingTransport = incomingTransport;
  }

  public Transport getOutgoingTransport() {
    return outgoingTransport;
  }

  public void setOutgoingTransport(Transport outgoingTransport) {
    this.outgoingTransport = outgoingTransport;
  }

  public DsNetwork getTestNetwork() {
    return testNetwork;
  }

  public void setTestNetwork(DsNetwork testNetwork) {
    this.testNetwork = testNetwork;
  }

  //  public DsScriptController getDsScriptController() {
  //    return dsScriptController;
  //  }
  //
  //  public void setDsScriptController(DsScriptController dsScriptController) {
  //    this.dsScriptController = dsScriptController;
  //  }
  public DsAppController getDsAppController() {
    return dsAppController;
  }

  public void setDsAppController(DsAppController dsAppController) {
    this.dsAppController = dsAppController;
  }

  public LBRepositoryHolder getLbRepositoryHolder() {
    return lbRepositoryHolder;
  }

  public void setLbRepositoryHolder(LBRepositoryHolder lbRepositoryHolder) {
    this.lbRepositoryHolder = lbRepositoryHolder;
  }
}
