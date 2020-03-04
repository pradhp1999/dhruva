/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import java.net.InetAddress;

/** This class implements the key used to store listening sockets. */
public class CTableListenKey extends ConnectionKey {

  /**
   * Constructs table listen key with the specified listening address, port and transport type.
   *
   * @param inetAddress the interface which the socket is listening on
   * @param port the listening port
   * @param transportType the listening transport type
   */
  public CTableListenKey(InetAddress inetAddress, int port, Transport transportType) {
    super(inetAddress, port, transportType);
  }

  /**
   * Constructs table listen key with the specified local and remote address.
   *
   * @param laddr the local interface
   * @param lport the local port
   * @param addr the remote interface
   * @param port the remote port
   * @param transport the listening transport type
   */
  public CTableListenKey(
      InetAddress laddr, int lport, InetAddress addr, int port, Transport transport) {
    super(laddr, lport, addr, port, transport);
  }
}
