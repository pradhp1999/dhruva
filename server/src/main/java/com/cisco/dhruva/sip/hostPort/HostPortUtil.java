package com.cisco.dhruva.sip.hostPort;

import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.sip.DsUtil.ListenIf;
import com.cisco.dhruva.sip.proxy.DsListenInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.function.Predicate;

public class HostPortUtil {

  private static final Logger Log = DhruvaLoggerFactory.getLogger(HostPortUtil.class);

  private HostPortUtil() {}

  private static Predicate<DsListenInterface> hostPortCheck =
      (DsListenInterface listenIf) ->
          DsNetwork.getDhruvaSIPConfigProperties().isHostPortEnabled()
              && listenIf != null
              && listenIf.shouldAttachExternalIp();

  private static Optional<String> getHostInfo(DsListenInterface listenIf) {
    if (hostPortCheck.test(listenIf)) {
      return Optional.ofNullable(DsNetwork.getDhruvaSIPConfigProperties().getHostInfo());
    }
    return Optional.empty();
  }

  /**
   * replace local IP with Host IP/FQDN for public network in 'user' portion of RR.
   *
   * <p>Used during RR addition and modification
   *
   * @param uri
   * @return DsByteString of the resulting IP
   */
  public static DsByteString convertLocalIpToHostInfo(DsSipURL uri) {
    try {
      ListenIf listenIf =
          (ListenIf)
              DsControllerConfig.getCurrent()
                  .getInterface(
                      InetAddress.getByName(uri.getHost().toString()),
                      uri.getTransportParam(),
                      uri.getPort());

      Optional<String> hostInfo = getHostInfo(listenIf);

      return hostInfo
          .map(
              h -> {
                Log.debug("Host IP/FQDN {} obtained for {}", h, uri);
                return new DsByteString(h);
              })
          .orElseGet(
              () -> {
                Log.debug("No host IP/FQDN found. Use local IP from {}", uri);
                return uri.getHost();
              });

    } catch (UnknownHostException e) {
      Log.warn("No IP address for the host[{}] found ", uri.getHost());
      return uri.getHost();
    }
  }

  /**
   * This method is also used to fetch local IP or host IP/FQDN as previous method.
   *
   * <p>Used for Via header addition
   *
   * @param listenIf
   * @return DsByteString of the resulting IP
   */
  public static DsByteString convertLocalIpToHostInfo(DsListenInterface listenIf) {
    Optional<String> hostInfo = getHostInfo(listenIf);

    return hostInfo
        .map(
            h -> {
              Log.debug("Host IP/FQDN {} obtained for {}", h, listenIf);
              return new DsByteString(h);
            })
        .orElseGet(
            () -> {
              Log.debug("No host IP/FQDN found. Use local IP from {}", listenIf);
              return listenIf.getAddress();
            });
  }

  /**
   * when 'hostPort' feature is enabled & if host IP/FQDN is attached to the URL -> get the
   * corresponding network's local IP when disabled -> host itself contains local IP, hence use that
   *
   * <p>Used for RR modification and Route header removal
   *
   * @param uri
   * @return DsByteString of the resulting IP
   */
  public static DsByteString reverseHostInfoToLocalIp(DsSipURL uri) {

    ListenIf listenIf =
        (ListenIf)
            DsControllerConfig.getCurrent().getInterface(uri.getPort(), uri.getTransportParam());

    if (hostPortCheck.test(listenIf)) {
      Log.debug("Local IP {} found for {}", listenIf.getAddress(), uri);
      return listenIf.getAddress();
    }
    Log.debug("No host IP/FQDN found. Use local IP for {}", uri);
    return uri.getHost();
  }
}
