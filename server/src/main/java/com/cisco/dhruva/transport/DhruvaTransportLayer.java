/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DhruvaTransportLayer implements TransportLayer {

  private MessageForwarder messageForwarder;

  private Logger logger = DhruvaLoggerFactory.getLogger(DhruvaTransportLayer.class);

  private ConnectionCache connectionCache = ConnectionCache.getInstance();

  public DhruvaTransportLayer(MessageForwarder messageForwarder) {
    this.messageForwarder = messageForwarder;
  }

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

    if (messageForwarder == null) {
      messageForwarder = this.messageForwarder;
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

    if (transportType == null || remoteAddress == null) {
      return exceptionallyCompletedFuture(
          new NullPointerException(
              "transportType or remoteAddress passed to DhruvaTransportLayer.getConnection is null transport = "
                  + transportType + " , remoteAddress = " + remoteAddress));
    }

    if (remotePort <= 0) {
      return exceptionallyCompletedFuture(
          new Exception("Invalid remoteport  value in DhruvaTransportLayer.getConnection , remotePort = "+remotePort));
    }

    try {

      InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);

      CompletableFuture<Connection> connectionCompletableFuture =
          connectionCache.get(localSocketAddress, remoteSocketAddress, transportType);

      if (connectionCompletableFuture != null) {
        if (connectionCompletableFuture.isDone()
            && connectionCompletableFuture.isCompletedExceptionally()) {
          connectionCache.remove(localSocketAddress, remoteSocketAddress, transportType,
              connectionCompletableFuture);
        } else {
          logger.info("Returning Connection {} from cache ", connectionCompletableFuture.get());
          return connectionCompletableFuture;
        }
      }

      Client client =
          ClientFactory.getInstance().getClient(transportType, networkConfig, messageForwarder);
      connectionCompletableFuture =
          connectionCache.computeIfAbsent(
              localSocketAddress,
              remoteSocketAddress,
              Transport.UDP,
              o -> {
                CompletableFuture<Connection> connectionFuture =
                    client.getConnection(localSocketAddress, remoteSocketAddress);
                return connectionFuture;
              });

      // If establishing the connection fails remove the connection from connection cache
      CompletableFuture<Connection> finalConnectionCompletableFuture = connectionCompletableFuture;
      connectionCompletableFuture.whenComplete(
          (connection, throwable) -> {
            if (throwable != null) {
              connectionCache.remove(
                  localSocketAddress, remoteSocketAddress, transportType,
                  finalConnectionCompletableFuture);
            }
          });

      logger.info(
          "Returning a new connection for localAddress {} remoteAddress {} , connectionfuture is {} ",
          localSocketAddress,
          remoteSocketAddress,
          connectionCompletableFuture);
      return connectionCompletableFuture;
    } catch (Exception e) {
      return exceptionallyCompletedFuture(e);
    }
  }

  private CompletableFuture exceptionallyCompletedFuture(Throwable e) {
    CompletableFuture exceptionFuture = new CompletableFuture();
    exceptionFuture.completeExceptionally(e);
    return exceptionFuture;
  }

  @Override
  public HashMap<Transport, Integer> getConnectionSummary() {
    return null;
  }
}
