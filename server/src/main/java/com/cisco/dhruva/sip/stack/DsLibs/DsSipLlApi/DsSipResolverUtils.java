// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.transport.Transport;
import java.util.Iterator;
import java.util.Set;

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
  public static final Transport BTU = DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED;

  /** The mask of transports that this stack is listening on. */
  private static byte m_staticSupportedTransports;

  /** No transports. */
  public static final byte TRANSPORT_NONE = (byte) 0;
  /** All transports. */
  public static final byte TRANSPORT_MASK = (byte) 0xff;

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
      DsSipURL sipURL, String matchHost, int matchPort, Transport matchTransport) {
    boolean ret_value = false;

    String host = DsByteString.toString(sipURL.getMAddrParam());
    int port = sipURL.hasPort() ? sipURL.getPort() : RPU;
    // GOGONG - 07.13.05 - Return default outgoing transport if the transport is not specified
    Transport transportParam = sipURL.getTransportParam();
    Transport transport =
        sipURL.isSecure()
            ? Transport.TLS
            : ((transportParam == Transport.NONE) ? BTU : transportParam);
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
   * @param supported the transport to look for.
   * @param sizeExceedsMTU if <code>true</code>, return <code>false</code> if <code>supported</code>
   *     is {@link DsSipTransportType#UDP}
   * @return <code>true</code> if this transport type is represented in the mask.
   */
  static boolean isSupported(Transport transport, int supported, boolean sizeExceedsMTU) {
    byte mask = TRANSPORT_NONE;
    switch (transport) {
      case UDP:
        // --                mask = sizeExceedsMTU ? TRANSPORT_NONE : DsSipTransportType.UDP_MASK;
        // Ignore sizeExceedsMTU flag
        mask = Transport.UDP_MASK;
        break;
      case MULTICAST:
        // --                mask =  sizeExceedsMTU ? TRANSPORT_NONE :
        // DsSipTransportType.MULTICAST_MASK;
        // Ignore sizeExceedsMTU flag
        mask = Transport.MULTICAST_MASK;
        break;
      case TCP:
        mask = Transport.TCP_MASK;
        break;
      case TLS:
        mask = Transport.TLS_MASK;
        break;
      case SCTP:
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
