package com.cisco.dhruva.sip.servergroups.util;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;

import com.cisco.dhruva.loadbalancer.LBFactory;
import com.cisco.dhruva.sip.controller.DsProxyCookieThing;
import com.cisco.dhruva.sip.controller.DsProxyParams;
import com.cisco.dhruva.sip.proxy.DsProxyInterface;
import com.cisco.dhruva.sip.servergroups.util.ServerGroupInput.InputServerGroup.Elements;
import com.cisco.dhruva.sip.servergroups.util.testhelper.TestResult;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ServerGroupTestValidatorHelper {
  public static List<DsProxyParams> captureProxyTransactionArgs(
      ServerGroupInput testCase, DsProxyInterface proxyTransaction) {
    List<DsProxyParams> paramsList = new ArrayList<DsProxyParams>();
    ArgumentCaptor<DsProxyParams> paramsCaptor = ArgumentCaptor.forClass(DsProxyParams.class);

    verify(
            proxyTransaction,
            Mockito.atLeast(1)
                .description(
                    TestResult.printVerifyFailure(
                        "proxyTransaction mock not invoked even once", testCase.getInfo())))
        .proxyTo(
            Mockito.any(DsSipRequest.class),
            Mockito.any(DsProxyCookieThing.class),
            paramsCaptor.capture());
    paramsList = paramsCaptor.getAllValues();
    return paramsList;
  }

  public static HashMap<String, Integer> getTestRunInfo(List<DsProxyParams> paramsList) {
    HashMap<String, Integer> testRunInfo = new HashMap<String, Integer>();
    paramsList.forEach(
        param -> {
          String ipAddress = param.getProxyToAddress().toString();
          if (testRunInfo.isEmpty()) {
            testRunInfo.put(ipAddress, 1);
          } else {
            if (testRunInfo.containsKey(ipAddress)) {
              int callsRecordedForElement = testRunInfo.get(ipAddress);
              testRunInfo.put(ipAddress, callsRecordedForElement + 1);
            } else {
              testRunInfo.put(ipAddress, 1);
            }
          }
        });
    System.out.println(
        "Test run info --> {Element IP : Total No. of Calls} : " + testRunInfo.toString());
    return testRunInfo;
  }

  public static int getTotalSuccessfulCallsTaken(
      ServerGroupInput testCase, HashMap<String, Integer> testRunInfo) {
    int totalSuccessfulCalls = 0;
    for (Elements element : testCase.getInputServerGroup().getElements()) {
      if (isUpElement(testCase, element)) {
        if (testRunInfo.containsKey(element.getIp())) {
          totalSuccessfulCalls += testRunInfo.get(element.getIp());
        }
      }
    }
    System.out.println("Total SuccessfulCalls taken: " + totalSuccessfulCalls);
    return totalSuccessfulCalls;
  }

  public static void assertZeroCallsToDownElements(
      HashMap<String, Integer> testRunInfo, HashMap<String, Integer> validationInfo)
      throws AssertionError {
    validationInfo
        .entrySet()
        .forEach(
            element -> {
              if (element.getValue() == 0) {
                assertTrue(
                    !testRunInfo.containsKey(element.getKey()),
                    "down element with ip " + element.getKey() + " received calls!");
              }
            });
  }

  public static int getNumTries(ServerGroupInput testCase) {
    int numTries = 0;
    switch (testCase
        .getInputServerGroup()
        .getTestConfig()
        .getTestCombination()
        .getOutgoingTransport()[0]) {
      case "udp":
        numTries = LBFactory.getUDPTries();
        break;
      case "tcp":
        numTries = LBFactory.getTCPTries();
        break;
      case "tls":
        numTries = LBFactory.getTLSTries();
    }
    return numTries;
  }

  public static boolean isUpElement(ServerGroupInput testCase, Elements element) {
    ServerGroupInput.failoverType failoverType = element.getTestConfig().getFailoverType();
    return (element.getTestConfig().getStatus().equals("up")
        && (failoverType == ServerGroupInput.failoverType.NONE
            || (failoverType == ServerGroupInput.failoverType.ICMP_ERROR
                && !testCase
                    .getInputServerGroup()
                    .getTestConfig()
                    .getTestCombination()
                    .getOutgoingTransport()[0]
                    .equals("udp"))));
  }

  public static class ElementSort implements Comparator<Elements> {
    @Override
    public int compare(Elements a, Elements b) {
      int compare = Float.compare(b.getQvalue(), a.getQvalue());
      if (compare == 0) {
        try {
          compare = compareIP(b.getIp(), a.getIp());
        } catch (Exception e) {
          e.printStackTrace();
        }
        if (compare == 0) {
          if (b.getPort() < a.getPort()) return 1;
          else if (b.getPort() > a.getPort()) return -1;
        }
      }
      return compare;
    }

    private static int compareIP(String ip2, String ip1) throws Exception {
      int compare = 0;
      if (!ip1.equals(ip2)) {

        StringTokenizer st1 = new StringTokenizer(ip1.toString(), ".");
        StringTokenizer st2 = new StringTokenizer(ip2.toString(), ".");

        String[] list1 = new String[st1.countTokens()];
        String[] list2 = new String[st2.countTokens()];
        int i = 0;
        while (st1.hasMoreTokens()) {
          list1[i] = st1.nextToken();
          i++;
        }
        i = 0;
        while (st2.hasMoreTokens()) {
          list2[i] = st2.nextToken();
          i++;
        }
        if (list1.length == list2.length) {
          try {
            for (i = 0; i < list1.length; i++) {
              int a = Integer.parseInt(list1[i]);
              int b = Integer.parseInt(list2[i]);
              if (a > b) {
                compare = 1;
                break;
              } else if (b > a) {
                compare = -1;
                break;
              }
            }
          } catch (NumberFormatException nfe) {
            throw nfe;
          }
        } else {
          throw new Exception("not a valid IP.");
        }
      }
      return compare;
    }
  }
}
