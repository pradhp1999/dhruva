// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.
package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.transport.Connection;
import java.io.IOException;
import java.net.InetAddress;

/*
TODO : Skeleton for now , has to be built properly
 */
public class DsTcpConnection extends DsAbstractConnection {

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public void send(byte buffer[]) throws IOException {
    super.send(buffer);
    updateTimeStamp();
  }

  protected DsTcpConnection(Connection connection) {
    super(connection);
  }

  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException {
    // TODO: exception here if addr and port don't match
    send(buffer);
  }
}
