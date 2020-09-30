package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.service.SipServerLocatorService;
import com.cisco.dhruva.sip.dto.Hop;
import com.cisco.dhruva.sip.enums.LocateSIPServerTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DnsServerGroupUtil {
  private float maxDnsPriority = 65535f;
  private int maxDnsQValue = 65536;

  private SipServerLocatorService locatorService;

  private static final Logger log = DhruvaLoggerFactory.getLogger(DnsServerGroupUtil.class);

  public DnsServerGroupUtil(SipServerLocatorService locatorService) {
    this.locatorService = locatorService;
  }

  public ServerGroupInterface createDNSServerGroup(
      DsByteString host, int port, DsNetwork network, Transport protocol, DsSipRequest request)
      throws ExecutionException, InterruptedException {
    // create DNS ServerGroup from from SRV, if SRV lookup fails then do A records lookup

    log.debug("new Server Group will be created by doing DNS SRV/A lookup for host : " + host);

    DnsDestination dnsDestination =
        new DnsDestination(host.toString(), port, LocateSIPServerTransportType.TLS_AND_TCP);

    LocateSIPServersResponse locateSIPServersResponse =
        locatorService.locateDestination(null, dnsDestination, null);

    return getServerGroupFromHops(locateSIPServersResponse, network, host, protocol);

    // TODO DNS Handle failures
  }

  public ServerGroupInterface getServerGroupFromHops(
      LocateSIPServersResponse response, DsNetwork network, DsByteString host, Transport protocol) {

    List<Hop> networkHops = response.getHops();

    TreeSet<ServerGroupElementInterface> elementList =
        networkHops.stream()
            .map(
                r ->
                    new DnsNextHop(
                        network.toByteString(),
                        new DsByteString(r.getHost()),
                        r.getPort(),
                        protocol,
                        (maxDnsQValue - r.getPriority()) / maxDnsPriority,
                        host))
            .collect(Collectors.toCollection(TreeSet::new));

    return new ServerGroup(
        new DsByteString(host),
        network.toByteString(),
        elementList,
        SG.index_sgSgLbType_call_id,
        false);
  }
}
