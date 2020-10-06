package com.cisco.dhruva.hostPort;

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

public class HostPortUtil {

  private static final Logger Log = DhruvaLoggerFactory.getLogger(HostPortUtil.class);

  private HostPortUtil() {}

  private static String getHostIp(DsListenInterface listenIf) {
    if (DsNetwork.getDhruvaSIPConfigProperties().isHostPortEnabled()
        && listenIf != null
        && listenIf.shouldAttachExternalIp()) {
      return DsNetwork.getDhruvaSIPConfigProperties().getHostIp();
    }
    return null;
  }

  /**
   * replace local IP with External IP for public network in 'user' portion of RR.
   *
   * <p>Used during RR addition and modification
   *
   * @param uri
   * @return DsByteString of the resulting IP
   */
  public static DsByteString convertLocalIpToExternalIp(DsSipURL uri) {
    try {
      ListenIf listenIf =
          (ListenIf)
              DsControllerConfig.getCurrent()
                  .getInterface(
                      InetAddress.getByName(uri.getHost().toString()),
                      uri.getTransportParam(),
                      uri.getPort());
      String hostIp = getHostIp(listenIf);
      if (hostIp != null) {
        Log.debug("Host IP {} obtained for {}", hostIp, uri);
        return new DsByteString(hostIp);
      }
    } catch (UnknownHostException e) {
      Log.warn("No IP address for the host[{}] found ", uri.getHost());
    }
    Log.debug("No host IP found. Use local IP from {}", uri);
    return uri.getHost();
  }

  /**
   * This method is also used to fetch local IP or External IP as previous method.
   *
   * <p>Used for Via header addition
   *
   * @param listenIf
   * @return DsByteString of the resulting IP
   */
  public static DsByteString convertLocalIpToExternalIp(DsListenInterface listenIf) {
    String hostIp = getHostIp(listenIf);
    if (hostIp != null) {
      Log.debug("Host IP {} obtained for {}", hostIp, listenIf);
      return new DsByteString(hostIp);
    }
    Log.debug("No host IP found. Use local IP for {}", listenIf);
    return listenIf.getAddress();
  }

  /**
   * when 'hostPort' feature is enabled & if hostIp is attached to the URL -> get the corresponding
   * network's local IP when disabled -> host itself contains local IP, hence use that
   *
   * <p>Used for RR modification and Route header removal
   *
   * @param uri
   * @return DsByteString of the resulting IP
   */
  public static DsByteString reverseExternalIpToLocalIp(DsSipURL uri) {

    ListenIf listenIf =
        (ListenIf)
            DsControllerConfig.getCurrent().getInterface(uri.getPort(), uri.getTransportParam());

    if (DsNetwork.getDhruvaSIPConfigProperties().isHostPortEnabled()
        && listenIf != null
        && listenIf.shouldAttachExternalIp()) {
      Log.debug("Local IP {} found for {}", listenIf.getAddress(), uri);
      return listenIf.getAddress();
    }
    Log.debug("No host IP found. Use local IP for {}", uri);
    return uri.getHost();
  }
}
