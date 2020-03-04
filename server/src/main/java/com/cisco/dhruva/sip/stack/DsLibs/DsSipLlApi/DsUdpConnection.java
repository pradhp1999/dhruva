// Copyright (c) 2020 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.transport.Connection;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * UDP datagram socket.
 */
public class DsUdpConnection extends DsAbstractConnection {
  public DsUdpConnection(Connection connection) {
    super(connection);
  }

  /**
   * Sends the specified data buffer across the network through the underlying datagram socket to
   * the desired destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public void send(byte buffer[]) throws IOException {
    super.send(buffer);
  }

  /**
   * Sends the specified data buffer across the network through the underlying datagram socket to
   * the desired destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @param addr destination address
   * @param port destination port
   * @throws IOException if there is an I/O error while sending the message
   */
  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException {
    send(buffer);
  }
}
