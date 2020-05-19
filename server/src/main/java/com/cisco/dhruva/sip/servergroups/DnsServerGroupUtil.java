package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSRVWrapper;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerLocator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipTransactionManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.io.IOException;
import java.net.InetAddress;
import java.util.TreeSet;
import javax.naming.NamingException;

public class DnsServerGroupUtil {
  private float maxDnsPriority = 65535f;
  private int maxDnsQValue = 65536;
  private Exception failureException;

  protected static Trace log = Trace.getTrace(DnsServerGroupUtil.class.getName());

  public ServerGroupInterface createDNSServerGroup(
      DsByteString host, DsNetwork network, Transport protocol, DsSipRequest request) {
    // create DNS ServerGroup from from SRV, if SRV lookup fails then do A records lookup
    if (Trace.on && log.isDebugEnabled()) {
      log.debug("new Server Group will be created by doing DNS SRV/A lookup for host : " + host);
    }
    DsSipServerLocator dnsResolver = new DsSipServerLocator();
    ServerGroupInterface dnsServerGroup =
        createServerGroupFromSrv(dnsResolver, host, network, protocol, request);
    if (dnsServerGroup == null) {
      int port = getPortForARecord(protocol, request);
      dnsServerGroup = createServerGroupFromARecords(dnsResolver, host, network, protocol, port);
    }
    setFailureException(dnsResolver.getFailureException());
    if (dnsResolver.getJndiDNSContext() != null) {
      try {
        dnsResolver.getJndiDNSContext().close();
      } catch (NamingException e) {
        log.debug("Exception while cleaning up dns context: " + e);
      }
    }
    FailoverResponseCode failOverRespCodes = FailoverResponseCode.getInstance();
    failOverRespCodes.setFailoverCodes(
        new DsByteString(host).toString(), failOverRespCodes.getGlobalDnsServerFailoverCodes());
    return dnsServerGroup;
  }

  public ServerGroupInterface createServerGroupFromSrv(
      DsSipServerLocator dnsResolver,
      DsByteString host,
      DsNetwork network,
      Transport protocol,
      DsSipRequest request) {
    DsSRVWrapper[] srvResults = null;
    ServerGroupInterface dnsServerGroup = null;
    TreeSet<ServerGroupElementInterface> elementList = new TreeSet<>();
    try {
      request.setNetwork(network);
      request.getBindingInfo().setTransport(protocol);
      DsSipTransactionManager.getRequestConnection(request, dnsResolver);
      srvResults = dnsResolver.getSrvResults();
      if (srvResults == null) {
        if (Trace.on && log.isWarnEnabled()) {
          log.warn(
              "SRV lookup results null for host : "
                  + host
                  + ", protocol : "
                  + protocol
                  + ", network : "
                  + network);
        }
        return null;
      }
      for (int i = 0; i < srvResults.length; ++i) {
        float qValue = (maxDnsQValue - srvResults[i].getLevel()) / maxDnsPriority;
        AbstractNextHop anh =
            new DnsNextHop(
                network.toByteString(),
                new DsByteString(srvResults[i].getIPAddress().getHostAddress()),
                srvResults[i].getPort(),
                protocol,
                qValue,
                host);
        elementList.add(anh);
      }
      log.info("DNS Server Group Elements : " + elementList.toString());
    } catch (DsException | IOException e) {
      if (Trace.on && log.isWarnEnabled()) {
        log.warn("Exception in SRV looup ", e);
      }
    }

    dnsServerGroup =
        new ServerGroup(
            new DsByteString(host),
            network.toByteString(),
            elementList,
            SG.index_sgSgLbType_call_id,
            false);
    return dnsServerGroup;
  }

  public ServerGroupInterface createServerGroupFromARecords(
      DsSipServerLocator dnsResolver,
      DsByteString host,
      DsNetwork network,
      Transport protocol,
      int port) {
    InetAddress[] aResults = null;
    ServerGroupInterface dnsServerGroup = null;
    TreeSet<ServerGroupElementInterface> elementList = new TreeSet<>();
    aResults = dnsResolver.getARecordResults();
    if (aResults == null) {
      if (Trace.on && log.isWarnEnabled()) {
        log.warn(
            "A Record lookup results null for host : "
                + host
                + ", protocol : "
                + protocol
                + ", network : "
                + network);
      }
      return null;
    }

    for (int i = 0; i < aResults.length; ++i) {
      AbstractNextHop anh =
          new DnsNextHop(
              network.toByteString(),
              new DsByteString(aResults[i].toString().replace("/", "")),
              port,
              protocol,
              1,
              host);
      elementList.add(anh);
    }
    log.info("DNS Server Group Elements : " + elementList.toString());
    dnsServerGroup =
        new ServerGroup(
            new DsByteString(host),
            network.toByteString(),
            elementList,
            SG.index_sgSgLbType_call_id,
            false);
    return dnsServerGroup;
  }

  public Exception getFailureException() {
    return failureException;
  }

  public void setFailureException(Exception failureException) {
    this.failureException = failureException;
  }

  public int getPortForARecord(Transport protocol, DsSipRequest request) {
    int port = 5060;
    try {
      port = request.getRequestURIPort();
      DsSipURL reqUrl = new DsSipURL(request.getURI().toString());
      if (!reqUrl.hasPort() && protocol == Transport.TLS) {
        port = 5061;
      }
      DsSipRouteHeader topRoute = (DsSipRouteHeader) request.getHeaderValidate(DsSipRequest.ROUTE);
      if (topRoute != null) {
        if (topRoute.lr()) {
          DsSipURL url = new DsSipURL(topRoute.getURI().toString());
          if (!url.hasPort() && protocol == Transport.TLS) {
            port = 5061;
          } else {
            port = url.getPort();
          }
        }
      }
    } catch (DsSipParserException | DsSipParserListenerException e) {
      log.warn(
          "Exception in getting port from request with call-id : "
              + request.getCallId()
              + ", method : "
              + request.getMethod(),
          e);
    }

    return port;
  }
}
