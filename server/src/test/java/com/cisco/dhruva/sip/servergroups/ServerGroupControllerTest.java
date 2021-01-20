package com.cisco.dhruva.sip.servergroups;

import static org.mockito.Mockito.*;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.router.AppEngine;
import com.cisco.dhruva.sip.servergroups.util.*;
import com.cisco.dhruva.sip.servergroups.util.interfaces.TestValidator;
import com.cisco.dhruva.sip.servergroups.util.testhelper.TestResult;
import com.cisco.dhruva.util.SpringApplicationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/* This is the controller class for testing ServerGroup Module. It configures the module, the tests, runs
 * test cases and also validates them
 */
public class ServerGroupControllerTest {
  private ApplicationContext applicationContext;
  private com.cisco.dhruva.common.executor.ExecutorService executorService;

  @BeforeClass
  void init() {

    applicationContext = mock(ApplicationContext.class);
    executorService = mock(ExecutorService.class);

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

  public ServerGroupInput[] readTestInput()
      throws FileNotFoundException, IOException, ParseException {
    ServerGroupInput[] testCaseCombination = null;

    ObjectMapper mapper = new ObjectMapper();
    JSONParser parser = new JSONParser();
    Object object =
        parser.parse(
            new FileReader(
                getClass()
                    .getClassLoader()
                    .getResource("serverGroupModuleTestInput.json")
                    .getFile()));
    JSONArray jsonObject = (JSONArray) object;
    testCaseCombination =
        (ServerGroupInput[]) mapper.readValue(jsonObject.toJSONString(), ServerGroupInput[].class);
    System.out.println("Input JSON: \n" + jsonObject.toJSONString());
    return testCaseCombination;
  }

  /* This method reads the test inout json and breaks it down to individual test cases grouped together in an object array.
   * This is then used by the @test method to run individual test cases.
   */
  @DataProvider
  public Object[] getTestCase()
      throws FileNotFoundException, IOException, ParseException, CloneNotSupportedException {
    ServerGroupInput[] testCaseCombination = readTestInput();
    List<ServerGroupInput> testCaseCombinationList = Arrays.asList(testCaseCombination);
    List<ServerGroupInput> testCaseList = new ArrayList<ServerGroupInput>();

    testCaseCombinationList.forEach(
        singleTestCaseCombination -> {
          String[] incomingTransportType =
              singleTestCaseCombination
                  .getInputServerGroup()
                  .getTestConfig()
                  .getTestCombination()
                  .getIncomingTransport();
          String[] outgoingTransportType =
              singleTestCaseCombination
                  .getInputServerGroup()
                  .getTestConfig()
                  .getTestCombination()
                  .getOutgoingTransport();
          String[] loadBalancerType =
              singleTestCaseCombination
                  .getInputServerGroup()
                  .getTestConfig()
                  .getTestCombination()
                  .getLoadBalancerType();
          for (String singleIncomingTransportType : incomingTransportType) {
            for (String singleOutgoingTransportType : outgoingTransportType) {
              for (String singleLoadBalancerType : loadBalancerType) {
                try {
                  ServerGroupInput singleTestCase =
                      (ServerGroupInput) singleTestCaseCombination.clone();

                  singleTestCase
                      .getInputServerGroup()
                      .getTestConfig()
                      .getTestCombination()
                      .setIncomingTransport(new String[] {singleIncomingTransportType});
                  singleTestCase
                      .getInputServerGroup()
                      .getTestConfig()
                      .getTestCombination()
                      .setOutgoingTransport(new String[] {singleOutgoingTransportType});
                  singleTestCase
                      .getInputServerGroup()
                      .getTestConfig()
                      .getTestCombination()
                      .setLoadBalancerType(new String[] {singleLoadBalancerType});
                  testCaseList.add(singleTestCase);
                } catch (CloneNotSupportedException e) {
                  e.printStackTrace();
                  throw new RuntimeException(
                      "Error creating test case from input json. Could not clone it.");
                }
              }
            }
          }
        });

    ServerGroupInput[] testCase =
        new ServerGroupInput[testCaseList.size()]; // reduces the combination elements to
    // just one for a testCase;
    testCase = testCaseList.toArray(testCase);
    Object[] testCaseArray = testCase;
    return testCaseArray;
  }

  @Test(dataProvider = "getTestCase", retryAnalyzer = FailureRetryAnalyzer.class)
  public void run(ServerGroupInput testCase) throws Exception {
    try {
      ServerGroupModuleConfigurator moduleConfigurator =
          new ServerGroupModuleConfigurator(testCase);
      ServerGroupTestConfigurator testConfigurator =
          new ServerGroupTestConfigurator(testCase, moduleConfigurator);
      ServerGroupTestRunner testRunner =
          new ServerGroupTestRunner(testCase, moduleConfigurator, testConfigurator);
      TestValidator validator = ServerGroupValidatorFactory.createValidator(testCase);
      moduleConfigurator.configure();
      testConfigurator.configure();
      testRunner.run(validator);
      validator.validate();
      TestResult.printSuccess(testCase.getInfo());
    } catch (Exception e) {
      TestResult.printFailure(e, testCase.getInfo());
      throw e;
    }
  }
}
