package com.cisco.dhruva.sip.servergroups;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.cisco.dhruva.adaptor.ProxyAdaptorFactory;
import com.cisco.dhruva.adaptor.ProxyAdaptorFactoryInterface;
import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.loadbalancer.LBException;
import com.cisco.dhruva.loadbalancer.LBRepositoryHolder;
import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.loadbalancer.ServerGroupInterface;
import com.cisco.dhruva.router.AppEngine;
import com.cisco.dhruva.router.AppInterface;
import com.cisco.dhruva.router.AppSession;
import com.cisco.dhruva.sip.controller.DsAppController;
import com.cisco.dhruva.sip.loadbalancer.resources.RequestBuilder;
import com.cisco.dhruva.sip.proxy.DsProxyBranchParamsInterface;
import com.cisco.dhruva.sip.proxy.DsProxyCookieInterface;
import com.cisco.dhruva.sip.proxy.DsProxyTransaction;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SpringApplicationContext;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GenericServerGroupTest {
  public static List calls;
  private ApplicationContext applicationContext;
  private ExecutorService executorService;

  private @BeforeClass void init() {

    applicationContext = mock(ApplicationContext.class);
    executorService = mock(ExecutorService.class);
    //doRecordRoute needs to be false for this test case to be false
    //Default and External_IP_enabled are two directions which are used in some other tests to addRecordRouteInterface
    DsControllerConfig.removeRecordRouteInterface("Default");
    DsControllerConfig.removeRecordRouteInterface("External_IP_enabled");
    SpringApplicationContext springApplicationContext = new SpringApplicationContext();
    springApplicationContext.setApplicationContext(applicationContext);

    ScheduledThreadPoolExecutor scheduledThreadPoolExecutor =
        mock(ScheduledThreadPoolExecutor.class);
    when(executorService.getScheduledExecutorThreadPool(ExecutorType.AKKA_CONTROLLER_TIMER))
        .thenReturn(scheduledThreadPoolExecutor);
    //
    // doReturn(scheduledThreadPoolExecutor).when(executorService).getExecutorThreadPool(ExecutorType.AKKA_CONTROLLER_TIMER);
    AppEngine.startShutdownTimers(executorService);
  }

  @BeforeMethod
  public void setUpMethod() throws Exception {
    calls = new ArrayList<>();
  }

  // create dataproviders with load balance type and corresponding result
  @DataProvider(name = "dataProviders")
  public Object[][] dataProvider() {
    return new Object[][] {
      {SG.index_sgSgLbType_global, "testHost4", 20},
      {SG.index_sgSgLbType_highest_q, "testHost1", 20},
      {SG.index_sgSgLbType_request_uri, "testHost4", 20},
      {SG.index_sgSgLbType_call_id, "testHost2", 20},
      {SG.index_sgSgLbType_to_uri, "testHost2", 20},
      {SG.index_sgSgLbType_weight, "testHost1,testHost2", 20},
      {SG.index_sgSgLbType_ms_id, "testHost3", 20}
    };
  }

  @Test(
      dataProvider = "dataProviders",
      description =
          "creates a large no of SG elements and verifies load balanced output for a given LB Types")
  public void testServerGroupLoadBalancing(int lbType, String result, int times)
      throws LBException, DsException {
    for (int i = 0; i < times; i++) {
      genericSGTest(lbType);
    }

    // after making specified no of calls, verify all the calls returned same element for the same
    // lb type
    // assert the returned element after load balance

    if (lbType == SG.index_sgSgLbType_weight) {
      String result1 = result.substring(0, result.indexOf(","));
      String result2 = result.substring(result.indexOf(",") + 1);

      int callDistribution1 = Collections.frequency(calls, result1);
      int callDistribution2 = Collections.frequency(calls, result2);

      assertTrue(callDistribution1 + callDistribution2 == calls.size());
      assertTrue(calls.contains(result1) && calls.contains(result2));
    } else {
      boolean allEql = Collections.frequency(calls, calls.get(0)) == calls.size();
      assertTrue(allEql);
      assertEquals(calls.get(0), result);
    }
    calls.clear();
  }

  public void genericSGTest(int lbType) throws DsException {
    DsSipRequest request = RequestBuilder.getRequestWithMParam("1234", "applicationsharing");
    //    CallProcessingConfig cpConfig = CallProcessingConfig.getInstance();
    ProxyAdaptorFactoryInterface pf = new ProxyAdaptorFactory();
    AppInterface app = new AppSession();
    DsAppController controller = new DsAppController(pf, app);

    Location location = new Location(request.getURI(), null, new DsByteString("SG1"), .1f);

    AbstractNextHop anh1 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost1"),
            0001,
            Transport.UDP,
            0.8f,
            new DsByteString("testSG1"));
    AbstractNextHop anh2 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost2"),
            0002,
            Transport.UDP,
            0.8f,
            new DsByteString("testSG2"));
    AbstractNextHop anh3 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost3"),
            0003,
            Transport.UDP,
            0.8f,
            new DsByteString("testSG3"));
    AbstractNextHop anh4 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost4"),
            0004,
            Transport.UDP,
            0.8f,
            new DsByteString("testSG4"));
    AbstractNextHop anh5 =
        new DefaultNextHop(
            new DsByteString("testNw"),
            new DsByteString("testHost5"),
            0005,
            Transport.UDP,
            0.8f,
            new DsByteString("testSG5"));
    anh5.setWeight(50);
    anh4.setWeight(50);

    TreeSet<ServerGroupElementInterface> set = new TreeSet<ServerGroupElementInterface>();
    set.add(anh1);
    set.add(anh2);
    set.add(anh3);
    set.add(anh4);
    set.add(anh5);
    ServerGroupInterface SG1 =
        new ServerGroup(new DsByteString("SG1"), new DsByteString("net1"), set, lbType, false);
    HashMap serverGroups = new HashMap();
    serverGroups.put(new DsByteString("SG1"), SG1);

    controller.setRequest(request);
    controller.setRepositoryHolder(mock(LBRepositoryHolder.class));
    //    controller.repositoryHolder = mock(LBRepositoryHolder.class);
    //    when(controller.repositoryHolder.getServerGroups()).thenReturn(serverGroups);
    when(controller.getRepositoryHolder().getServerGroups()).thenReturn(serverGroups);
    //    controller.ppIface = ourConfig;
    DsControllerConfig ourConfig = DsControllerConfig.getCurrent();
    controller.setPpIface(ourConfig);
    ProxyTest proxyTest = new ProxyTest();
    //    controller.ourProxy = proxyTest;
    controller.setOurProxy(proxyTest);

    ServerGroupInterface serverGroup = mock(ServerGroupInterface.class);
    when(serverGroup.getElements()).thenReturn(set);

    HashMap map1 = mock(HashMap.class);
    when(map1.get(Mockito.any())).thenReturn(serverGroup);
    controller.proxyToServerGroup(
        location, null, request, null, DsNetwork.getNetwork("testNw"), (AbstractServerGroup) SG1);
  }
}

class ProxyTest extends DsProxyTransaction {
  public void proxyTo(
      DsSipRequest request, DsProxyCookieInterface cookie, DsProxyBranchParamsInterface params) {
    GenericServerGroupTest.calls.add(params.getProxyToAddress().toString());
  }
}
