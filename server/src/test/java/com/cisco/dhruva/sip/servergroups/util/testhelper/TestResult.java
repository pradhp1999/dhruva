package com.cisco.dhruva.sip.servergroups.util.testhelper;

import org.testng.Reporter;

public class TestResult {
  public static void printSuccess(String getInfo) {
    Reporter.log("TEST SUCCESSFULL FOR  " + getInfo, true);
    Reporter.log(
        "-------------------------------------------------------------------------------", true);
  }

  public static void printFailure(Throwable e, String getInfo) {
    Reporter.log("TEST FAILED FOR " + getInfo + "\nStackTrace: " + e.getMessage(), true);
    Reporter.log(e.getStackTrace().toString(), true);
    Reporter.log(
        "-------------------------------------------------------------------------------", true);
  }

  public static void printFailure(Exception e, String getInfo) {
    Reporter.log("TEST FAILED FOR " + getInfo + "\nStackTrace: " + e.getMessage(), true);
    Reporter.log(e.getStackTrace().toString(), true);
    Reporter.log(
        "-------------------------------------------------------------------------------", true);
  }

  public static void printFailure(String getInfo) {
    Reporter.log("TEST FAILED FOR " + getInfo, true);
    Reporter.log(
        "-------------------------------------------------------------------------------", true);
  }

  public static void printFailure(AssertionError e, String getInfo) {
    Reporter.log("TEST FAILED FOR " + getInfo + "\nStackTrace: " + e.getMessage(), true);
    Reporter.log(e.getStackTrace().toString(), true);
    Reporter.log(
        "-------------------------------------------------------------------------------", true);
  }

  public static String printVerifyFailure(String verifyError, String getInfo) {
    String message =
        "TEST FAILED FOR "
            + getInfo
            + "\nError: "
            + verifyError
            + "\n------------------------------------------------------------------------------";
    return message;
  }
}
