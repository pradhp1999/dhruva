/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public interface Client {

  CompletableFuture<Connection> getConnection(
      SocketAddress localSocketAddress, SocketAddress remoteSocketAddress);

  Transport getTransport();

  public void addConnectionEventHandler(ChannelEventsListener connectionEventHandler);
}
