// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.transport.Connection;

/**
 * Defines a concrete SIP connection that is used to SIP messages across the network through the
 * underlying TLS socket.
 */

/*
TODO : Skeleton for now , has to be built properly

 */
public class DsSipTlsConnection extends DsSipAbstractSipConnection implements DsSipConnection {

  protected DsSipTlsConnection(Connection connection) {
    super(connection);
  }
}
