// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.
package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.transport.Connection;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * TCP socket. This concrete connection can be constructed through the {@link
 * DsDefaultConnectionFactory DsDefaultConnectionFactory} by passing appropriate parameter like
 * transport type and address.
 */

/*
TODO : Skeleton for now , has to be built properly

 */
public class DsTcpConnection extends DsAbstractConnection {

  protected DsTcpConnection(Connection connection) {
    super(connection);
  }

  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException {
    // TODO: exception here if addr and port don't match
    send(buffer);
  }
}
