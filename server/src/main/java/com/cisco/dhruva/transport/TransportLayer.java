/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.transport.config.NetworkConfig;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface TransportLayer {

  public CompletableFuture<Boolean> startListening(
      Transport transportType,
      NetworkConfig transportConfig,
      InetAddress address,
      int port,
      MessageHandler handler);

  public CompletableFuture<Connection> getConnection(
      NetworkConfig networkConfig,
      Transport transportType,
      InetAddress localAddress,
      int localPort,
      InetAddress remoteAddress,
      int remotePort);

  /**
   * Returns Connection Summary
   *
   * @return Map of Transport type and Number of connections for the transport
   */
  public HashMap<Transport, Integer> getConnectionSummary();
}
