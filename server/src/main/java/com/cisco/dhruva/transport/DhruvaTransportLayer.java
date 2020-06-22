/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DhruvaTransportLayer implements TransportLayer {

  private final ExecutorService executorService;
  private final MetricService metricService;
  private MessageForwarder messageForwarder;

  private Logger logger = DhruvaLoggerFactory.getLogger(DhruvaTransportLayer.class);

  private int connectionSweepInterval = 60;
  private ConnectionCache connectionCache =
      ConnectionCache.getInstance(connectionSweepInterval, TimeUnit.MINUTES);
  private ConcurrentHashMap<ConnectionKey, Server> listenServers = new ConcurrentHashMap<>();
  private int maxConnections;

  public DhruvaTransportLayer(
      MessageForwarder messageForwarder,
      ExecutorService executorService,
      MetricService metricService) {
    this.messageForwarder = messageForwarder;
    this.executorService = executorService;
    this.metricService = metricService;
  }

  @Override
  public CompletableFuture startListening(
      Transport transportType,
      DsNetwork transportConfig,
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
      Server server =
          ServerFactory.getInstance()
              .getServer(
                  transportType, messageForwarder, transportConfig, executorService, metricService);
      server.startListening(address, port, serverStartFuture);
      serverStartFuture.whenComplete(
          (channel, throwable) -> {
            if (throwable == null) {
              listenServers.put(new CTableListenKey(address, port, transportType), server);
            }
          });

    } catch (Exception e) {
      serverStartFuture.completeExceptionally(e);
    }
    return serverStartFuture;
  }

  @Override
  public CompletableFuture<Connection> getConnection(
      DsNetwork networkConfig,
      Transport transportType,
      InetAddress localAddress,
      int localPort,
      InetAddress remoteAddress,
      int remotePort) {

    if (transportType == null || remoteAddress == null) {
      return exceptionallyCompletedFuture(
          new NullPointerException(
              "transportType or remoteAddress passed to DhruvaTransportLayer.getConnection is null transport = "
                  + transportType
                  + " , remoteAddress = "
                  + remoteAddress));
    }

    if (remotePort <= 0) {
      return exceptionallyCompletedFuture(
          new Exception(
              "Invalid remoteport  value in DhruvaTransportLayer.getConnection , remotePort = "
                  + remotePort));
    }

    try {

      InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);

      CompletableFuture<Connection> connectionCompletableFuture =
          connectionCache.get(localSocketAddress, remoteSocketAddress, transportType);

      if (connectionCompletableFuture != null) {
        logger.info(
            "Returning a cached connection for localAddress {} remoteAddress {} , connection future is {} ",
            localSocketAddress,
            remoteSocketAddress,
            connectionCompletableFuture);
        return connectionCompletableFuture;
      }

      Client client =
          ClientFactory.getInstance()
              .getClient(
                  transportType, networkConfig, messageForwarder, executorService, metricService);
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
                  localSocketAddress,
                  remoteSocketAddress,
                  transportType,
                  finalConnectionCompletableFuture);
            }
          });

      logger.info(
          "Returning a new connection for localAddress {} remoteAddress {} , connection future is {} ",
          localSocketAddress,
          remoteSocketAddress,
          connectionCompletableFuture);
      return connectionCompletableFuture;
    } catch (Exception e) {
      return exceptionallyCompletedFuture(e);
    }
  }

  @Override
  public CompletableFuture<Connection> getCachedConnection(
      Transport transportType,
      InetAddress localAddress,
      int localPort,
      InetAddress remoteAddress,
      int remotePort) {
    InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
    InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);
    return connectionCache.get(localSocketAddress, remoteSocketAddress, transportType);
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

  @Override
  public void clearConnectionCache() {
    connectionCache.clear();
  }

  @Override
  public void stop() {}

  @Override
  public ConnectionKey findListenKeyForTransport(Transport transport) {
    AtomicReference<CTableListenKey> retKey = new AtomicReference<>();
    listenServers.forEachKey(
        Long.MAX_VALUE,
        connectionKey -> {
          if (connectionKey.transport == transport) {
            retKey.set((CTableListenKey) connectionKey);
          }
        });
    return retKey.get();
  }

  /*
  TODO implement maxconnection logic
   */
  @Override
  public void setMaxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
  }

  @Override
  public int getMaxConnections() {
    return this.maxConnections;
  }

  @Override
  public Enumeration getListenKeys() {
    return listenServers.keys();
  }
}
