package com.cisco.dhruva.sip.servergroups.util;

import static com.cisco.dhruva.sip.servergroups.util.ServerGroupTestValidatorHelper.*;
import static org.testng.Assert.assertEquals;

import com.cisco.dhruva.sip.controller.DsProxyParams;
import com.cisco.dhruva.sip.proxy.DsProxyInterface;
import com.cisco.dhruva.sip.servergroups.util.ServerGroupInput.InputServerGroup.Elements;
import com.cisco.dhruva.sip.servergroups.util.interfaces.TestValidator;
import com.cisco.dhruva.sip.servergroups.util.testhelper.TestResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ServerGroupValidatorHighestQ implements TestValidator {
  ServerGroupInput testCase;
  HashMap<String, Integer> testRunInfo;
  HashMap<String, Integer> validationInfo;
  List<DsProxyParams> paramsList;

  public ServerGroupValidatorHighestQ(ServerGroupInput testCase) {
    this.testCase = testCase;
    validationInfo = new HashMap<String, Integer>();
  }

  public void validate() {
    testRunInfo = getTestRunInfo(paramsList);
    getValidationInfo();
    System.out.println("Test run info --> {Element IP : No. of Calls} : " + testRunInfo.toString());
    System.out.println(
        "Validation info --> {Element IP : No. of Calls} : " + validationInfo.toString());
    try {
      testRunInfo
          .entrySet()
          .forEach(
              element -> {
                int callsTaken = element.getValue();
                int callsToTake = (int) validationInfo.get(element.getKey());
                assertEquals(
                    callsTaken, callsToTake, "calls not received by ip" + element.getKey());
              });
      assertZeroCallsToDownElements(testRunInfo, validationInfo);
    } catch (AssertionError e) {
      TestResult.printFailure(e, testCase.getInfo());
      throw e;
    }
  }

  private void getValidationInfo() {
    List<Elements> elements = Arrays.asList(testCase.getInputServerGroup().getElements());
    Collections.sort(elements, new ServerGroupTestValidatorHelper.ElementSort());

    int numTries = getNumTries(testCase);
    int totalCalls = testCase.getInputServerGroup().getTestConfig().totalCalls;
    for (Elements element : elements) {
      ServerGroupInput.failoverType failoverType = element.getTestConfig().getFailoverType();
      String status = element.getTestConfig().status;
      String ip = element.getIp();
      if (isUpElement(testCase, element)) {
        validationInfo.put(ip, totalCalls);

        break;
      } else if (status.equals("up")
          && (failoverType == ServerGroupInput.failoverType.FAILURE_RESPONSE
              || (failoverType == ServerGroupInput.failoverType.ICMP_ERROR
                  && testCase
                      .getInputServerGroup()
                      .getTestConfig()
                      .getTestCombination()
                      .getOutgoingTransport()[0]
                      .equals("udp")))) {
        validationInfo.put(ip, 1);
      } else if (status.equals("up")
          && (failoverType == ServerGroupInput.failoverType.PROXY_FAILURE
              || failoverType == ServerGroupInput.failoverType.REQUEST_TIMEOUT)) {
        validationInfo.put(ip, numTries);
      }
    }
    for (Elements element : elements) {
      if (element.getTestConfig().status.equals("down")) {
        validationInfo.put(element.getIp(), 0);
      }
    }
  }

  public void capture(DsProxyInterface proxyTransaction) {
    paramsList = captureProxyTransactionArgs(testCase, proxyTransaction);
  }
}
