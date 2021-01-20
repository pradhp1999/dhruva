package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.sip.controller.DsAppController;
import com.cisco.dhruva.sip.controller.DsProxyCookieThing;
import com.cisco.dhruva.sip.proxy.DsProxyTransaction;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.servergroups.util.ServerGroupInput;
import com.cisco.dhruva.sip.servergroups.util.ServerGroupModuleConfigurator;
import com.cisco.dhruva.sip.servergroups.util.ServerGroupTestConfigurator;
import com.cisco.dhruva.sip.servergroups.util.interfaces.TestRunner;
import com.cisco.dhruva.sip.servergroups.util.interfaces.TestValidator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import java.util.HashMap;

public class ServerGroupTestRunner implements TestRunner {

  ServerGroupInput testCase;
  HashMap<String, Integer> testRunInfo = new HashMap<String, Integer>();
  ServerGroupModuleConfigurator moduleConfigurator;
  ServerGroupTestConfigurator testConfigurator;

  public HashMap<String, Integer> getTestRunInfo() {
    return this.testRunInfo;
  }

  public ServerGroupTestRunner(
      ServerGroupInput testCase,
      ServerGroupModuleConfigurator moduleConfigurator,
      ServerGroupTestConfigurator testConfigurator) {
    this.testCase = testCase;
    this.moduleConfigurator = moduleConfigurator;
    this.testConfigurator = testConfigurator;
  }

  public void run(TestValidator validator) throws Exception {
    DsNetwork testNetwork = moduleConfigurator.getTestNetwork();

    for (int i = 0; i < testConfigurator.getTotalCalls(); i++) {

      DsSipRequest inviteRequest = testConfigurator.getInviteRequest();
      DsAppController dsAppController = moduleConfigurator.getDsAppController();
      Location location = testConfigurator.getLocation();
      DsProxyCookieThing cookieThing = testConfigurator.getCookieThing();
      DsProxyTransaction proxyTransaction = testConfigurator.getProxyTransaction();
      dsAppController.setOurProxy(proxyTransaction);
      dsAppController.setRequest(inviteRequest);
      dsAppController.setPreprocessedRequest((DsSipRequest) inviteRequest.clone());
      dsAppController.proxyToServerGroup(
          location,
          null,
          inviteRequest,
          cookieThing,
          testNetwork,
          (AbstractServerGroup) moduleConfigurator.getServerGroup());

      validator.capture(dsAppController.getOurProxy());
    }
  }
}
