/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import java.net.InetAddress;
import java.util.Objects;

public class ConnectionInfo {

  private Transport transport;
  private InetAddress localAddress;
  private int localPort;
  private InetAddress remoteAddress;
  private InetAddress remotePort;

  public Transport getTransport() {
    return transport;
  }

  public void setTransport(Transport transport) {
    this.transport = transport;
  }

  public InetAddress getLocalAddress() {
    return localAddress;
  }

  public void setLocalAddress(InetAddress localAddress) {
    this.localAddress = localAddress;
  }

  public int getLocalPort() {
    return localPort;
  }

  public void setLocalPort(int localPort) {
    this.localPort = localPort;
  }

  public InetAddress getRemoteAddress() {
    return remoteAddress;
  }

  public void setRemoteAddress(InetAddress remoteAddress) {
    this.remoteAddress = remoteAddress;
  }

  public InetAddress getRemotePort() {
    return remotePort;
  }

  public void setRemotePort(InetAddress remotePort) {
    this.remotePort = remotePort;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ConnectionInfo)) {
      return false;
    }
    ConnectionInfo that = (ConnectionInfo) o;
    return getLocalPort() == that.getLocalPort()
        && getTransport() == that.getTransport()
        && Objects.equals(getLocalAddress(), that.getLocalAddress())
        && Objects.equals(getRemoteAddress(), that.getRemoteAddress())
        && Objects.equals(getRemotePort(), that.getRemotePort());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getTransport(), getLocalAddress(), getLocalPort(), getRemoteAddress(), getRemotePort());
  }
}
