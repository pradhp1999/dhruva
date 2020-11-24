package com.cisco.dhruva.sip.servergroups;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.cisco.dhruva.sip.dto.Hop;
import com.cisco.dhruva.sip.enums.DNSRecordSource;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.LocateSIPServersResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DnsServerGroupUtilTest {
  DsNetwork dsNetwork;
  DsControllerConfig ourConfig;
  private float maxDnsPriority = 65535f;
  private int maxDnsQValue = 65536;

  @BeforeClass
  void init() throws Exception {
    dsNetwork = DsNetwork.getNetwork("Default");
    ourConfig = DsControllerConfig.getCurrent();
  }

  @Test
  public void testCreateDnsServerGroup() throws ExecutionException, InterruptedException {
    SipServerLocatorService sipServerLocatorService = mock(SipServerLocatorService.class);
    when(sipServerLocatorService.locateDestination(any(), any(), any()))
        .thenReturn(
            new LocateSIPServersResponse(
                Collections.singletonList(
                    new Hop(
                        "webex.example.com",
                        "2.2.2.2",
                        Transport.TLS,
                        5061,
                        1,
                        DNSRecordSource.DNS)),
                null,
                null,
                null,
                LocateSIPServersResponse.Type.HOSTNAME));

    DnsServerGroupUtil dnsServerGroupUtil = new DnsServerGroupUtil(sipServerLocatorService);
    ServerGroupInterface dnsServerGroup =
        dnsServerGroupUtil.createDNSServerGroup(
            new DsByteString("webex.example.com"), 5061, dsNetwork, Transport.TLS, null);

    TreeSet<ServerGroupElementInterface> actualElementList = dnsServerGroup.getElements();

    TreeSet<ServerGroupElementInterface> expectedElementList = new TreeSet<>();
    DnsNextHop hop =
        new DnsNextHop(
            dsNetwork.toByteString(),
            new DsByteString("2.2.2.2"),
            5061,
            Transport.TLS,
            (maxDnsQValue - 1) / maxDnsPriority,
            new DsByteString("webex.example.com"));
    expectedElementList.add(hop);
    Assert.assertEquals(actualElementList, expectedElementList);
  }
}
