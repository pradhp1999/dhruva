/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.metric.Metric;
import com.cisco.dhruva.common.metric.Metrics;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.channel.Channel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class DhruvaTransportLayer implements TransportLayer {

  private final ExecutorService executorService;
  private final MetricService metricService;
  private MessageForwarder messageForwarder;
  private ChannelEventsListener connectionCacheEventHandler = new ConnectionCacheEventHandler();

  private Logger logger = DhruvaLoggerFactory.getLogger(DhruvaTransportLayer.class);

  private int connectionSweepInterval = 60;
  private ConnectionCache connectionCache =
      ConnectionCache.getInstance(connectionSweepInterval, TimeUnit.MINUTES);
  private ConcurrentHashMap<ConnectionKey, Channel> listenServers = new ConcurrentHashMap<>();
  private int maxConnections;

  public DhruvaTransportLayer(
      MessageForwarder messageForwarder,
      ExecutorService executorService,
      MetricService metricService) {
    this.messageForwarder = messageForwarder;
    this.executorService = executorService;
    this.metricService = metricService;
    registerMetric();
  }

  private void registerMetric() {
    metricService.registerPeriodicMetric(
        "activeConnections", connectionCountMetricSupplier(), 1, TimeUnit.MINUTES);
  }

  @NotNull
  private Supplier<Set<Metric>> connectionCountMetricSupplier() {
    return () -> {
      AtomicInteger udpConnectionCount = new AtomicInteger();
      AtomicInteger tcpConnectionCount = new AtomicInteger();
      AtomicInteger tlsConnectionCount = new AtomicInteger();
      connectionCache
          .getConnectionMap()
          .forEach(
              (connectionKey, connectionCompletableFuture) -> {
                if (connectionCompletableFuture.isDone()) {
                  try {
                    Connection connection = connectionCompletableFuture.get();
                    switch (connection.getConnectionType()) {
                      case UDP:
                        udpConnectionCount.getAndIncrement();
                        break;
                      case TCP:
                        tcpConnectionCount.getAndIncrement();
                        break;
                      case TLS:
                        tlsConnectionCount.getAndIncrement();
                        break;
                    }
                  } catch (ExecutionException | InterruptedException e) {
                    logger.info(
                        "Exception "
                            + e.getMessage()
                            + " in getting connection metric from connection cache "
                            + "ignore the exception , this connection Future should be cleaned up by the Future handler");
                  }
                }
              });

      Set<Metric> metrics = new HashSet<>();

      metrics.add(
          Metrics.newMetric()
              .tag(Transport.TRANSPORT, Transport.UDP.name())
              .field("count", udpConnectionCount.get()));
      metrics.add(
          Metrics.newMetric()
              .tag(Transport.TRANSPORT, Transport.TCP.name())
              .field("count", tcpConnectionCount.get()));
      metrics.add(
          Metrics.newMetric()
              .tag(Transport.TRANSPORT, Transport.TLS.name())
              .field("count", tlsConnectionCount.get()));

      return metrics;
    };
  }

  @Override
  public CompletableFuture startListening(
      Transport transportType,
      DsNetwork transportConfig,
      InetAddress address,
      int port,
      MessageForwarder messageForwarder) {

    CompletableFuture<Channel> serverStartFuture = new CompletableFuture();
    if (transportType == null || address == null || messageForwarder == null) {
      serverStartFuture.completeExceptionally(
          new NullPointerException(
              "TransportType or address or messageForwarder passed to NettyTransportLayer.startListening is null"));
      return serverStartFuture;
    }

    try {
      Server server =
          ServerFactory.getInstance()
              .getServer(
                  transportType,
                  messageForwarder,
                  transportConfig,
                  executorService,
                  metricService,
                  connectionCacheEventHandler);
      server.startListening(address, port, serverStartFuture);
      serverStartFuture.whenComplete(
          (channel, throwable) -> {
            if (throwable == null) {
              listenServers.put(new CTableListenKey(address, port, transportType), channel);
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
              transportType,
              o -> client.getConnection(localSocketAddress, remoteSocketAddress));

      client.addConnectionEventHandler(connectionCacheEventHandler);

      // If establishing the connection fails remove the connection from connection cache
      CompletableFuture<Connection> finalConnectionCompletableFuture = connectionCompletableFuture;
      connectionCompletableFuture.whenComplete(
          (connection, throwable) -> {
            if (throwable != null) {
              logger.info(
                  "Removing the Connection which completed Exceptionally from Connection cache, localAddress"
                      + localSocketAddress
                      + " remoteAddress= "
                      + remoteSocketAddress
                      + " transport "
                      + transportType);
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

  public void addConnectionToCache(
      InetSocketAddress localAddress,
      InetSocketAddress remoteAddress,
      Transport transport,
      Connection connection) {
    CompletableFuture<Connection> connectionFuture = new CompletableFuture<>();
    connectionFuture.complete(connection);
    connectionCache.add(localAddress, remoteAddress, transport, connectionFuture);
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

  @Override
  public void shutdown() {
    listenServers.forEach((connectionKey, channel) -> channel.close());
  }

  private class ConnectionCacheEventHandler implements ChannelEventsListener {

    @Override
    public void connectionActive(
        InetSocketAddress localAddress,
        InetSocketAddress remoteAddress,
        Transport transport,
        Connection connection) {

      if (connection != null) {
        CompletableFuture<Connection> existingConnectionFuture =
            connectionCache.get(localAddress, remoteAddress, transport);

        if (existingConnectionFuture != null) {

          if (existingConnectionFuture.isDone()) {
            if (!existingConnectionFuture.isCompletedExceptionally()) {

              try {
                Connection existingConnection = existingConnectionFuture.get();

                if (connection.equals(existingConnection)) {
                  logger.info("Connection already in the cache , skipping connection Add to cache");
                } else {
                  logger.error(
                      "Different Connection exists while trying to add new Connection , "
                          + "existing connection = "
                          + existingConnection
                          + ", new Connection = "
                          + connection
                          + " , New connection will replace existing connection");
                  addConnectionToConnectionCache(
                      localAddress, remoteAddress, transport, connection);
                }
              } catch (InterruptedException | ExecutionException e) {
                logger.error(
                    "This should never happen: Exception while getting the connection from"
                        + " existing Connection Cache, we are trying to add a new connection to"
                        + " Cache but connection already exists in Cache, replacing the Connection"
                        + " in the Cache with new Connection = "
                        + connection);

                addConnectionToConnectionCache(localAddress, remoteAddress, transport, connection);
              }
            } else {
              logger.error(
                  "Trying to add Connection "
                      + connection
                      + " to connection cache,"
                      + " but connection cache already has a connection which is completed exceptionally"
                      + " going to replace the connection with new connection ");
              addConnectionToConnectionCache(localAddress, remoteAddress, transport, connection);
            }
          } else {
            logger.warn(
                "Trying to add Connection "
                    + connection
                    + " to connection cache,"
                    + " but connection cache already has a connection which has not connected yet"
                    + " going to replace the connection with new connection ");
            addConnectionToConnectionCache(localAddress, remoteAddress, transport, connection);
          }
        } else {
          logger.logWithContext(
              "Connection is not in the Cache , Adding connection " + connection + " to Cache",
              connection.connectionInfoMap());
          addConnectionToConnectionCache(localAddress, remoteAddress, transport, connection);
        }
      }
    }

    @Override
    public void connectionInActive(
        InetSocketAddress localAddress, InetSocketAddress remoteAddress, Transport transport) {
      CompletableFuture<Connection> removedConnectionFuture =
          connectionCache.remove(localAddress, remoteAddress, transport);

      Object removedObject;
      try {
        removedObject =
            removedConnectionFuture.isDone()
                ? removedConnectionFuture.isCompletedExceptionally()
                    ? removedConnectionFuture
                    : removedConnectionFuture.get()
                : removedConnectionFuture;
      } catch (InterruptedException | ExecutionException e) {
        removedObject = removedConnectionFuture;
      }

      logger.info(
          "Removed Connection localAddress="
              + localAddress
              + ","
              + " remoteAddress="
              + remoteAddress
              + " from cache, removedConnection = "
              + removedObject);
    }

    @Override
    public void onException(Throwable throwable) {}

    private void addConnectionToConnectionCache(
        InetSocketAddress localAddress,
        InetSocketAddress remoteAddress,
        Transport transport,
        Connection connection) {
      CompletableFuture<Connection> connectionFuture = new CompletableFuture<>();
      connectionFuture.complete(connection);
      connectionCache.add(localAddress, remoteAddress, transport, connectionFuture);
    }
  }
}
