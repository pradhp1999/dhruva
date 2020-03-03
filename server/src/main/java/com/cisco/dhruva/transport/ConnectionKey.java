/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import java.net.InetAddress;

public class ConnectionKey {

  public InetAddress localAddress;
  public int localPort;
  public InetAddress remoteAddress;
  public int remotePort;
  public Transport transport;

  public ConnectionKey(
      InetAddress localAddress,
      int localPort,
      InetAddress remoteAddress,
      int remotePort,
      Transport transportType) {
    this.localAddress = localAddress;
    this.localPort = localPort;
    this.remoteAddress = remoteAddress;
    this.remotePort = remotePort;
    this.transport = transportType;
  }

  public ConnectionKey(InetAddress remoteAddress, int remotePort, Transport transportType) {
    this.localAddress = null;
    this.localPort = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;
    this.remoteAddress = remoteAddress;
    this.remotePort = remotePort;
    this.transport = transportType;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConnectionKey)) {
      return false;
    }

    ConnectionKey aKey = (ConnectionKey) obj;
    if (aKey.remotePort != remotePort) {
      return false;
    }
    if (aKey.transport != transport) {
      return false;
    }
    // m_InetAddress should not be null. But we check it anyway
    if (aKey.remoteAddress == null || !(aKey.remoteAddress.equals(remoteAddress))) {
      return false;
    }
    // if one local port is not specified, we treat it as equal
    if ((aKey.localPort != localPort)
        && (aKey.localPort != DsBindingInfo.LOCAL_PORT_UNSPECIFIED)
        && (localPort != DsBindingInfo.LOCAL_PORT_UNSPECIFIED)) {
      return false;
    }
    // if one local address is not specified, we treat it as equal
    if ((aKey.localAddress != null)
        && (localAddress != null)
        && !(aKey.localAddress.equals(localAddress))) {
      return false;
    }

    return true;
  }

  public InetAddress getRemoteAddress() {
    return remoteAddress;
  }

  public int getRemotePort() {
    return remotePort;
  }

  /**
   * Determine the hash code.
   *
   * @return the hash code
   */
  public int hashCode() {
    // return m_Port * 2 + m_TransportType + m_InetAddress.hashCode();
    return ((remotePort + transport.ordinal()) * 3) * remoteAddress.hashCode();
  }

  public String toString() {
    StringBuffer buffer =
        new StringBuffer(64)
            .append(transport)
            .append(':')
            .append(localAddress == null ? null : localAddress.getHostAddress())
            .append(':')
            .append(localPort)
            .append(':')
            .append(remoteAddress == null ? null : remoteAddress.getHostAddress())
            .append(':')
            .append(remotePort);
    return buffer.toString();
  }
}
