// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.DsLibs.DsUtil.IPValidator;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * A set of utility methods shared between the simple resolver functionality in the transactions and
 * the DsSipServerLocator.
 */
public class DsSipResolverUtils {
  /** Local port unspecified. */
  public static final int LPU = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;
  /** Remote port unspecified. */
  public static final int RPU = DsBindingInfo.REMOTE_PORT_UNSPECIFIED;
  /** Binding transport unspecified. */
  public static final int BTU = DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED;

  /** The mask of transports that this stack is listening on. */
  private static byte m_staticSupportedTransports;

  /** No transports. */
  public static final byte TRANSPORT_NONE = (byte) 0;
  /** All transports. */
  public static final byte TRANSPORT_MASK = (byte) 0xff;

  /**
   * Initialize this server locator after construction.
   *
   * @param network the associated network object
   * @param resolver the resolver to use
   * @param host the host part of the URI
   * @param port the port part of the URI
   * @param proto the protocol part of the URIo
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be located
   */
  static void initialize(
      DsNetwork network, DsSipResolver resolver, String host, int port, int proto)
      throws UnknownHostException, DsSipServerNotFoundException {
    // will not change local binding info.
    resolver.initialize(network, null, LPU, host, port, proto, IPValidator.hostIsIPAddr(host));
  }

  /**
   * Initialize this server locator after construction.
   *
   * @param network the associated network object
   * @param resolver the resolver to use
   * @param localAddr local address
   * @param localPort local port
   * @param host the host part of the URI
   * @param port the port part of the URI
   * @param proto the protocol part of the URIo
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be located
   */
  static void initialize(
      DsNetwork network,
      DsSipResolver resolver,
      InetAddress localAddr,
      int localPort,
      String host,
      int port,
      int proto)
      throws UnknownHostException, DsSipServerNotFoundException {
    resolver.initialize(
        network, localAddr, localPort, host, port, proto, IPValidator.hostIsIPAddr(host));
  }

  /**
   * Initialize this class as per the specified SIP URL. Does the lookup depending upon the "maddr"
   * parameter of the specified SIP URL. If "maddr" parameter is present, then uses this host name
   * for lookup query, otherwise uses the host name present in the SIP URL itself.
   *
   * @param network the associated network object
   * @param resolver the resolver to use
   * @param sipURL The SIP URL containing the hostname which requires the lookup.
   * @throws DsSipParserException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be found
   */
  static void initialize(DsNetwork network, DsSipResolver resolver, DsSipURL sipURL)
      throws DsSipParserException, UnknownHostException, DsSipServerNotFoundException {

    // m_resolver.m_isSipsURI = sipURL.isSecure();

    String host = DsByteString.toString(sipURL.getMAddrParam());

    if (null == host) {
      host = DsByteString.toString(sipURL.getHost());
    }
    // GOGONG - 07.13.05 - Return default outgoing transport if the transport is not specified
    int transportParam = sipURL.getTransportParam();
    int transport =
        sipURL.isSecure()
            ? DsSipTransportType.TLS
            : ((transportParam == DsSipTransportType.NONE) ? BTU : transportParam);
    resolver.initialize(
        network,
        null,
        LPU,
        host,
        sipURL.hasPort() ? sipURL.getPort() : RPU,
        transport,
        IPValidator.hostIsIPAddr(host));
  }

  /**
   * Initialize this class as per the specified SIP URL. Does the lookup depending upon the "maddr"
   * parameter of the specified SIP URL. If "maddr" parameter is present, then uses this host name
   * for lookup query, otherwise uses the host name present in the SIP URL itself.
   *
   * @param network the associated network object
   * @param resolver the resolver to use
   * @param localAddr local address
   * @param localPort local port
   * @param sipURL The SIP URL containing the hostname which requires the lookup.
   * @throws DsSipParserException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   * @throws UnknownHostException if host is not known
   * @throws DsSipServerNotFoundException if no server could be found
   */
  static void initialize(
      DsNetwork network,
      DsSipResolver resolver,
      InetAddress localAddr,
      int localPort,
      DsSipURL sipURL)
      throws DsSipParserException, UnknownHostException, DsSipServerNotFoundException {
    String host = DsByteString.toString(sipURL.getMAddrParam());

    if (null == host) {
      host = DsByteString.toString(sipURL.getHost());
    }
    // GOGONG - 07.13.05 - Return default outgoing transport if the transport is not specified
    int transportParam = sipURL.getTransportParam();
    int transport =
        sipURL.isSecure()
            ? DsSipTransportType.TLS
            : ((transportParam == DsSipTransportType.NONE) ? BTU : transportParam);
    resolver.initialize(
        network,
        localAddr,
        localPort,
        host,
        sipURL.hasPort() ? sipURL.getPort() : RPU,
        transport,
        IPValidator.hostIsIPAddr(host));
  }

  /**
   * Check to see if the query for 'sipURL' would match the current query. Used to prevent duplicate
   * lookups, if the client is already holding a DsSipServerLocator.
   *
   * @param sipURL to check
   * @param matchHost the host to match against
   * @param matchPort the port to match against
   * @param matchTransport the transport type to match against
   * @return true if query for this url would match the current query
   */
  static boolean queryMatches(
      DsSipURL sipURL, String matchHost, int matchPort, int matchTransport) {
    boolean ret_value = false;

    String host = DsByteString.toString(sipURL.getMAddrParam());
    int port = sipURL.hasPort() ? sipURL.getPort() : RPU;
    // GOGONG - 07.13.05 - Return default outgoing transport if the transport is not specified
    int transportParam = sipURL.getTransportParam();
    int transport =
        sipURL.isSecure()
            ? DsSipTransportType.TLS
            : ((transportParam == DsSipTransportType.NONE) ? BTU : transportParam);
    if (null == host) {
      host = DsByteString.toString(sipURL.getHost());
    }
    if (host.equals(matchHost) && (matchPort == port) && (matchTransport == transport)) {
      ret_value = true;
    }
    return ret_value;
  }

  /**
   * Return <code>true</code> if this transport type is represented in the mask.
   *
   * @param mask the bit mask.
   * @param supported the transport to look for.
   * @param sizeExceedsMTU if <code>true</code>, return <code>false</code> if <code>supported</code>
   *     is {@link DsSipTransportType#UDP}
   * @return <code>true</code> if this transport type is represented in the mask.
   */
  static boolean isSupported(int transport, int supported, boolean sizeExceedsMTU) {
    byte mask = TRANSPORT_NONE;
    switch (transport) {
      case DsSipTransportType.UDP:
        // --                mask = sizeExceedsMTU ? TRANSPORT_NONE : DsSipTransportType.UDP_MASK;
        // Ignore sizeExceedsMTU flag
        mask = DsSipTransportType.UDP_MASK;
        break;
      case DsSipTransportType.MULTICAST:
        // --                mask =  sizeExceedsMTU ? TRANSPORT_NONE :
        // DsSipTransportType.MULTICAST_MASK;
        // Ignore sizeExceedsMTU flag
        mask = DsSipTransportType.MULTICAST_MASK;
        break;
      case DsSipTransportType.TCP:
        mask = DsSipTransportType.TCP_MASK;
        break;
      case DsSipTransportType.TLS:
        mask = DsSipTransportType.TLS_MASK;
        break;
      case DsSipTransportType.SCTP:
        mask = DsSipTransportType.SCTP_MASK;
        break;
    }
    return isSupported(mask, supported);
  }

  /**
   * Return <code>true</code> if this transport type is represented in the mask.
   *
   * @param mask the bit mask.
   * @param supported the transport to look for.
   * @return <code>true</code> if this transport type is represented in the mask.
   */
  private static boolean isSupported(byte mask, int supported) {
    return (supported != TRANSPORT_NONE)
        ? (supported & mask) > 0
        : (m_staticSupportedTransports & mask) > 0;
  }

  /**
   * Convert the Set of Integer(s) to a bitmask.
   *
   * @return the bitmask.
   */
  static byte getMaskedTransports(Set supported_transports) {
    byte transports = TRANSPORT_NONE;
    if (supported_transports != null) {
      Iterator iter = supported_transports.iterator();
      Integer t = null;
      while (iter.hasNext()) {
        t = (Integer) iter.next();
        switch (t.intValue()) {
          case DsSipTransportType.UDP:
            transports |= DsSipTransportType.UDP_MASK;
            break;
          case DsSipTransportType.MULTICAST:
            transports |= DsSipTransportType.MULTICAST_MASK;
            break;
          case DsSipTransportType.TCP:
            transports |= DsSipTransportType.TCP_MASK;
            break;
          case DsSipTransportType.TLS:
            transports |= DsSipTransportType.TLS_MASK;
            break;
          case DsSipTransportType.SCTP:
            transports |= DsSipTransportType.SCTP_MASK;
            break;
        }
      }
    }
    return transports;
  }

  static synchronized void setGlobalSupportedTransports(Set supported_transports) {
    if (supported_transports != null) {
      m_staticSupportedTransports = DsSipResolverUtils.getMaskedTransports(supported_transports);
    }
  }

  static synchronized void setGlobalSupportedTransports(byte supported_transports) {
    m_staticSupportedTransports = supported_transports;
  }
}
