package com.cisco.dhruva.sip.servergroups.util;

import com.cisco.dhruva.sip.servergroups.util.interfaces.TestValidator;

public class ServerGroupValidatorFactory {

  public static TestValidator createValidator(ServerGroupInput testCase) {
    String lbType =
        testCase
            .getInputServerGroup()
            .getTestConfig()
            .getTestCombination()
            .getLoadBalancerType()[0];
    switch (lbType) {
      case "highest-q":
        return new ServerGroupValidatorHighestQ(testCase);
      case "call-id":
      case "request-uri":
      case "to-uri":
        return new ServerGroupValidatorHashBased(testCase);
      case "weight":
        return new ServerGroupValidatorWeight(testCase);
    }
    return null;
  }
}
