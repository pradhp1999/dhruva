/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.config.network.NetworkConfig;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DhruvaTransportLayer implements TransportLayer {

  @Override
  public CompletableFuture startListening(
      Transport transportType,
      NetworkConfig transportConfig,
      InetAddress address,
      int port,
      MessageForwarder messageForwarder) {

    CompletableFuture serverStartFuture = new CompletableFuture();
    if (transportType == null || address == null || messageForwarder == null) {
      serverStartFuture.completeExceptionally(
          new NullPointerException(
              "TransportType or address or messageForwarder passed to NettyTransportLayer.startListening is null"));
      return serverStartFuture;
    }
    try {
      ServerFactory.getInstance()
          .getServer(transportType, messageForwarder, transportConfig)
          .startListening(address, port, serverStartFuture);
    } catch (Exception e) {
      serverStartFuture.completeExceptionally(e);
    }
    return serverStartFuture;
  }

  @Override
  public CompletableFuture<Connection> getConnection(
      NetworkConfig networkConfig,
      Transport transportType,
      InetAddress localAddress,
      int localPort,
      InetAddress remoteAddress,
      int remotePort) {
    return null;
  }

  @Override
  public HashMap<Transport, Integer> getConnectionSummary() {
    return null;
  }
}
