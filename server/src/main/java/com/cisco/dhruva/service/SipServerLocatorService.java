package com.cisco.dhruva.service;

import com.cisco.dhruva.config.sip.DhruvaSIPConfigProperties;
import com.cisco.dhruva.sip.dto.Hop;
import com.cisco.dhruva.sip.enums.LocateSIPServerTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsSipServerLocator;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.LocateSIPServersResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.SipDestination;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.wx2.dto.User;
import com.cisco.wx2.util.JsonUtil;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SipServerLocatorService {

  private static final Logger logger = DhruvaLoggerFactory.getLogger(SipServerLocatorService.class);

  @Autowired DhruvaSIPConfigProperties props;

  @Autowired protected DsSipServerLocator locator;

  @Autowired
  public SipServerLocatorService(DhruvaSIPConfigProperties props) {
    this.props = props;
  }

  public LocateSIPServersResponse locateDestination(
      User user, SipDestination sipDestination, String callId)
      throws ExecutionException, InterruptedException {
    final String name = sipDestination.getAddress();
    final LocateSIPServerTransportType transportLookupType =
        sipDestination.getTransportLookupType();
    Integer iPort = (sipDestination.getPort() < 0) ? null : sipDestination.getPort();
    final String userIdInject = (user == null) ? null : user.getId().toString();

    // TODO enable when required
    boolean useDnsInjection = false;

    LocateSIPServersResponse response =
        useDnsInjection
            ? locator.resolve(name, transportLookupType, iPort, userIdInject)
            : locator.resolve(name, transportLookupType, iPort);

    logger.info(
        "DNS lookup name={} port={} transportLookupType={} -> \n{}\n",
        name,
        iPort,
        transportLookupType,
        JsonUtil.toJsonPretty(response));

    return response;
  }

  public boolean shouldSearch(DsSipURL sipURL) throws DsSipParserException {
    return locator.shouldSearch(sipURL);
  }

  public boolean shouldSearch(String hostName, int port, Transport transport) {
    return locator.shouldSearch(hostName, port, transport);
  }

  public boolean shouldSearch(SipDestination outbound) {
    Transport transport;
    Optional<Transport> optTrans = outbound.getTransportLookupType().toSipTransport();
    transport = optTrans.orElse(Transport.TLS); // Default
    return locator.shouldSearch(outbound.getAddress(), outbound.getPort(), transport);
  }

  public boolean isSupported(Transport transport) {
    return locator.isSupported(transport);
  }

  public DsSipServerLocator getLocator() {
    return locator;
  }

  public List<DsBindingInfo> getBindingInfoMapFromHops(
      DsNetwork network,
      @Nullable InetAddress lAddr,
      int lPort,
      String host,
      int port,
      Transport transport,
      LocateSIPServersResponse sipServersResponse) {
    try {
      List<Hop> networkHops = sipServersResponse.getHops();
      return networkHops.stream()
          .map(h -> new DsBindingInfo(lAddr, lPort, host, port, transport))
          .collect(Collectors.toList());
    } catch (Exception e) {
      logger.error("response ", e);
      return Collections.emptyList();
    }
  }
}
