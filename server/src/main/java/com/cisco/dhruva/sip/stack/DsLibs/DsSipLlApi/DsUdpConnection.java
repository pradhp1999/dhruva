// Copyright (c) 2020 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.transport.Connection;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * UDP datagram socket.
 */
public class DsUdpConnection extends DsAbstractConnection {

  public DsUdpConnection(Connection connection) {
    super(connection);
  }
}
