// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.transport.Connection;

public class DsSipTcpConnection extends DsSipAbstractSipConnection implements DsSipConnection {
  protected DsSipTcpConnection(Connection connection) {
    super(connection);
  }
} // Ends class DsSipTcpConnection
