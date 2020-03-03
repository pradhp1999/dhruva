// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.transport.Connection;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * TLS socket.
 *
 * <p>This concrete connection can be constructed through the {@link DsDefaultConnectionFactory
 * DsDefaultConnectionFactory} by passing appropriate parameter like transport type and address.
 */
/*
TODO : Skeleton for now , has to be built properly
*/
public class DsTlsConnection extends DsTcpConnection {

  private DsSSLContext sslContext;

  protected DsTlsConnection(Connection connection, DsSSLContext sslContext) {
    super(connection);
    this.sslContext = sslContext;
  }
}
