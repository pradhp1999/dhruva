package com.cisco.dhruva.sip.servergroups.util;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cisco.dhruva.loadbalancer.LBRepositoryHolder;
import com.cisco.dhruva.sip.controller.DsProxyCookieThing;
import com.cisco.dhruva.sip.controller.DsProxyParams;
import com.cisco.dhruva.sip.proxy.*;
import com.cisco.dhruva.sip.servergroups.util.ServerGroupInput.InputServerGroup.Elements;
import com.cisco.dhruva.sip.servergroups.util.interfaces.TestConfigurator;
import com.cisco.dhruva.sip.servergroups.util.testhelper.RequestBuilder;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRetryAfterHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDate;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ServerGroupTestConfigurator implements TestConfigurator {
  ServerGroupInput testCase;
  ServerGroupModuleConfigurator moduleConfigurator;
  int totalCalls;

  DsProxyTransaction proxyTransaction;
  Location location;
  DsSipRequest inviteRequest;
  DsProxyCookieThing cookieThing = null;

  public ServerGroupTestConfigurator(
      ServerGroupInput testCase, ServerGroupModuleConfigurator moduleConfigurator) {
    this.testCase = testCase;
    this.moduleConfigurator = moduleConfigurator;
  }

  public void configure() throws Exception {
    ServerGroupInput.InputServerGroup inputServerGroup = testCase.getInputServerGroup();
    totalCalls = inputServerGroup.getTestConfig().getTotalCalls();
    moduleConfigurator.markElementsDown();
    LBRepositoryHolder lbRepositoryHolder = mock(LBRepositoryHolder.class);
    when(lbRepositoryHolder.getServerGroups()).thenReturn(moduleConfigurator.getServerGroupMap());
    moduleConfigurator.setLbRepositoryHolder(lbRepositoryHolder);
    proxyTransaction = mock(DsProxyTransaction.class);
    moduleConfigurator.initController();
    addFailoverBehaviour();
  }

  public void addFailoverBehaviour() {
    List<Elements> inputServerGroupElements =
        Arrays.asList(testCase.getInputServerGroup().getElements());
    List<String> proxyFailoverIps = new ArrayList<String>();
    List<String> requestTimeoutIps = new ArrayList<String>();
    List<String> icmpErrorIps = new ArrayList<String>();
    HashMap<String, Integer> failureResponse = new HashMap<String, Integer>();
    for (ServerGroupInput.InputServerGroup.Elements element : inputServerGroupElements) {
      if (element.getTestConfig().status.equals("up")
          && element.getTestConfig().getFailoverType()
              == ServerGroupInput.failoverType.PROXY_FAILURE) {
        proxyFailoverIps.add(element.getIp());
      } else if (element.getTestConfig().status.equals("up")
          && element.getTestConfig().getFailoverType()
              == ServerGroupInput.failoverType.FAILURE_RESPONSE) {
        failureResponse.put(element.getIp(), element.getTestConfig().getFailoverCode());
      } else if (element.getTestConfig().status.equals("up")
          && element.getTestConfig().getFailoverType()
              == ServerGroupInput.failoverType.REQUEST_TIMEOUT) {
        requestTimeoutIps.add(element.getIp());
      } else if (testCase
              .getInputServerGroup()
              .getTestConfig()
              .getTestCombination()
              .getOutgoingTransport()[0]
              .equals("udp")
          && (element.getTestConfig().status.equals("up")
              && element.getTestConfig().getFailoverType()
                  == ServerGroupInput.failoverType.ICMP_ERROR)) {
        icmpErrorIps.add(element.getIp());
      }
    }
    doAnswer(
            new Answer<Void>() {
              @Override
              public Void answer(InvocationOnMock args) throws Throwable {
                DsProxyParams params = (DsProxyParams) args.getArguments()[2];
                if (proxyFailoverIps.contains(params.getProxyToAddress().toString())) {
                  moduleConfigurator
                      .getDsAppController()
                      .onProxyFailure(
                          (DsProxyStatelessTransaction) proxyTransaction,
                          cookieThing,
                          DsControllerInterface.INVALID_PARAM,
                          "Mocking failover",
                          new Exception());
                } else if (failureResponse.containsKey(params.getProxyToAddress().toString())) {
                  DsSipResponse response = mock(DsSipResponse.class);
                  Calendar c = Calendar.getInstance();
                  c.add(Calendar.DAY_OF_MONTH, 1);
                  Date d = c.getTime();
                  DsDate date = new DsDate();
                  date.setDate(d);
                  DsSipRetryAfterHeader retryAfterHeader = new DsSipRetryAfterHeader(date);
                  when(response.getHeaderValidate(DsSipRetryAfterHeader.sID))
                      .thenReturn(retryAfterHeader);
                  when(response.getStatusCode())
                      .thenReturn(failureResponse.get(params.getProxyToAddress().toString()));
                  moduleConfigurator
                      .getDsAppController()
                      .onFailureResponse(proxyTransaction, cookieThing, null, response);
                } else if (requestTimeoutIps.contains(params.getProxyToAddress().toString())) {

                  DsProxyClientTransaction transaction = mock(DsProxyClientTransaction.class);
                  when(transaction.getResponse()).thenReturn(null);
                  moduleConfigurator
                      .getDsAppController()
                      .onRequestTimeOut(proxyTransaction, cookieThing, transaction);
                } else if (!icmpErrorIps.isEmpty()
                    && icmpErrorIps.contains(params.getProxyToAddress().toString())) {

                  DsProxyClientTransaction transaction = mock(DsProxyClientTransaction.class);
                  when(transaction.getResponse()).thenReturn(null);
                  moduleConfigurator
                      .getDsAppController()
                      .onICMPError(proxyTransaction, cookieThing, transaction);
                }
                return null;
              }
            })
        .when(proxyTransaction)
        .proxyTo(
            Mockito.any(DsSipRequest.class),
            Mockito.any(DsProxyCookieThing.class),
            Mockito.any(DsProxyParams.class));
  }

  public int getTotalCalls() {
    return totalCalls;
  }

  public void setTotalCalls(int totalCalls) {
    this.totalCalls = totalCalls;
  }

  public DsProxyTransaction getProxyTransaction() {
    return proxyTransaction;
  }

  public void setProxyTransaction(DsProxyTransaction proxyTransaction) {
    this.proxyTransaction = proxyTransaction;
  }

  public Location getLocation() {
    location =
        new Location(
            inviteRequest.getURI(),
            null,
            new DsByteString(testCase.getInputServerGroup().getName()),
            .1f);
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public DsSipRequest getInviteRequest() throws UnknownHostException, DsException {
    inviteRequest = createInviteRequest();
    return inviteRequest;
  }

  public void setInviteRequest(DsSipRequest inviteRequest) {
    this.inviteRequest = inviteRequest;
  }

  public DsProxyCookieThing getCookieThing() {
    cookieThing = mock(DsProxyCookieThing.class);
    when(cookieThing.getLocation()).thenReturn(location);
    when(cookieThing.getOutboundRequest()).thenReturn(inviteRequest);
    return cookieThing;
  }

  public void setCookieThing(DsProxyCookieThing cookieThing) {
    this.cookieThing = cookieThing;
  }

  private DsSipRequest createInviteRequest() throws DsException, UnknownHostException {
    RequestBuilder requestBuilder = new RequestBuilder();

    DsSipRequest inviteRequest = null;
    try {
      String inviteString =
          requestBuilder.getRequestAsString(RequestBuilder.RequestMethod.INVITE, true);
      inviteRequest = RequestBuilder.createRequest(inviteString);
      requestBuilder.setBindingInfo(inviteRequest, moduleConfigurator.getIncomingTransport());

    } catch (DsSipParserListenerException | DsSipParserException e) {

      throw e;
    }
    return inviteRequest;
  }
}
