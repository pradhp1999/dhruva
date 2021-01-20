package com.cisco.dhruva.sip.servergroups.util;

import static com.cisco.dhruva.sip.servergroups.util.ServerGroupTestValidatorHelper.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.cisco.dhruva.sip.controller.DsProxyParams;
import com.cisco.dhruva.sip.proxy.DsProxyInterface;
import com.cisco.dhruva.sip.servergroups.util.ServerGroupInput.InputServerGroup.Elements;
import com.cisco.dhruva.sip.servergroups.util.interfaces.TestValidator;
import com.cisco.dhruva.sip.servergroups.util.testhelper.TestResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ServerGroupValidatorHashBased implements TestValidator {

  ServerGroupInput testCase;
  HashMap<String, Integer> testRunInfo;
  HashMap<String, Integer> validationInfo;
  List<DsProxyParams> paramsList;
  static final int callVariationPercentage = 80;

  public ServerGroupValidatorHashBased(ServerGroupInput testCase) {
    this.testCase = testCase;
    validationInfo = new HashMap<String, Integer>();
  }

  public void validate() {
    try {
      testRunInfo = getTestRunInfo(paramsList);
      getValidationInfo();
      testRunInfo
          .entrySet()
          .forEach(
              element -> {
                int callsTaken = element.getValue();
                int minimumCalls = (int) validationInfo.get(element.getKey());
                assertTrue(
                    callsTaken >= minimumCalls,
                    "minimum calls not received by ip" + element.getKey());
              });
      int totalSuccessfulCalls = getTotalSuccessfulCallsTaken(testCase, testRunInfo);
      assertEquals(
          totalSuccessfulCalls,
          testCase.getInputServerGroup().getTestConfig().getTotalCalls(),
          "total successful calls taken is not equal to total number of calls in the test case.");
      assertZeroCallsToDownElements(testRunInfo, validationInfo);
    } catch (AssertionError e) {
      TestResult.printFailure(e, testCase.getInfo());
      throw e;
    }
  }

  private void getValidationInfo() {
    List<Elements> elements = Arrays.asList(testCase.getInputServerGroup().getElements());
    Collections.sort(elements, new ElementSort());
    float highestQ = 0;
    List<Elements> upElements = new ArrayList<Elements>();
    for (Elements element : elements) {
      if (isUpElement(testCase, element)) {
        highestQ = element.getQvalue();
        break;
      }
    }
    for (Elements element : elements) {
      if (isUpElement(testCase, element) && element.getQvalue() == highestQ) {
        upElements.add(element);
      }
    }
    int totalCalls = testCase.getInputServerGroup().getTestConfig().totalCalls;
    int callDistribution = (totalCalls / upElements.size());
    int minimumCalls = (callVariationPercentage * callDistribution) / 100;
    int numTries = getNumTries(testCase);
    if (minimumCalls < numTries) {
      numTries = minimumCalls;
    }
    for (Elements element : elements) {
      ServerGroupInput.failoverType failoverType = element.getTestConfig().getFailoverType();
      String status = element.getTestConfig().status;
      float qValue = element.getQvalue();
      String ip = element.getIp();
      if (isUpElement(testCase, element) && qValue == highestQ) {
        validationInfo.put(ip, minimumCalls);
      } else if (status.equals("up")
          && (failoverType == ServerGroupInput.failoverType.PROXY_FAILURE
              || failoverType == ServerGroupInput.failoverType.REQUEST_TIMEOUT)
          && qValue >= highestQ) {
        validationInfo.put(ip, numTries);
      } else if (status.equals("up")
          && (failoverType == ServerGroupInput.failoverType.FAILURE_RESPONSE
              || (failoverType == ServerGroupInput.failoverType.ICMP_ERROR
                  && testCase
                      .getInputServerGroup()
                      .getTestConfig()
                      .getTestCombination()
                      .getOutgoingTransport()[0]
                      .equals("udp")))
          && qValue >= highestQ) {
        validationInfo.put(ip, 1);
      } else {
        validationInfo.put(ip, 0);
      }
    }
    System.out.println(
        "Validation info --> {Element IP : Minimum No. of Calls} : " + validationInfo.toString());
  }

  public void capture(DsProxyInterface proxyTransaction) {
    paramsList = captureProxyTransactionArgs(testCase, proxyTransaction);
  }
}
