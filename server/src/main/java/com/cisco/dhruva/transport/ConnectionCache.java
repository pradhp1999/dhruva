/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ConnectionCache {

  private static final ConnectionCache connectionCache = new ConnectionCache();
  private final Logger logger = DhruvaLoggerFactory.getLogger(ConnectionCache.class);
  private int connectionSweeperInterval = 60;
  private TimeUnit connectionSweeperIntervalTimeUnit = TimeUnit.MINUTES;

  private final ConcurrentHashMap<ConnectionKey, CompletableFuture<Connection>> connectionMap =
      new ConcurrentHashMap<>();

  private final ScheduledExecutorService connectionSweeperSchedulerService =
      Executors.newSingleThreadScheduledExecutor();

  public static ConnectionCache getInstance(
      int connectionSweeperInterval, TimeUnit connectionSweeperIntervalTimeUnit) {
    connectionCache.connectionSweeperInterval = connectionSweeperInterval;
    connectionCache.connectionSweeperIntervalTimeUnit = connectionSweeperIntervalTimeUnit;
    ScheduledFuture scheduledFuture =
        connectionCache.connectionSweeperSchedulerService.scheduleAtFixedRate(
            connectionCache.connectionSweeperTask,
            connectionCache.connectionSweeperInterval,
            connectionCache.connectionSweeperInterval,
            connectionCache.connectionSweeperIntervalTimeUnit);
    return connectionCache;
  }

  private Runnable connectionSweeperTask =
      () -> {
        try {
          Collection collection = connectionMap.values();
          Iterator iterator = collection.iterator();
          while (iterator.hasNext()) {
            CompletableFuture<Connection> connectionFuture = ((CompletableFuture) iterator.next());
            if (connectionFuture.isDone() && !connectionFuture.isCompletedExceptionally()) {
              try {
                Connection connection = connectionFuture.get();
                if (connection.shouldClose()) {
                  logger.warn(
                      "Removing connection {} from cache in the ConnectionSweeper", connection);
                  iterator.remove();
                  connection.closeConnection();
                  connection = null;
                }
              } catch (Exception e) {
                logger.error(
                    "Error getting connection from ConnectionFuture "
                        + connectionFuture
                        + " in Connection Sweeper task exception is ",
                    e);
              }
            } else if (connectionFuture.isCompletedExceptionally()) {
              logger.error(
                  "Exceptionally completed Future is in Connection Cache ,"
                      + " This should never happen, bug in code ");
            }
          }
        } catch (Exception e) {
          logger.error("Unhandled Exception Connection Sweeper task exception is ", e);
        }
      };

  private ConnectionCache() {}

  public void add(
      InetSocketAddress localAddress,
      InetSocketAddress remoteAddress,
      Transport transport,
      CompletableFuture<Connection> connectionFuture) {
    ConnectionKey connectionKey =
        new ConnectionKey(
            localAddress.getAddress(),
            localAddress.getPort(),
            remoteAddress.getAddress(),
            remoteAddress.getPort(),
            transport);
    connectionMap.put(connectionKey, connectionFuture);
  }

  public CompletableFuture<Connection> get(
      InetSocketAddress localAddress, InetSocketAddress remoteAddress, Transport transport) {
    ConnectionKey connectionKey =
        new ConnectionKey(
            localAddress.getAddress(),
            localAddress.getPort(),
            remoteAddress.getAddress(),
            remoteAddress.getPort(),
            transport);
    return connectionMap.get(connectionKey);
  }

  public CompletableFuture<Connection> computeIfAbsent(
      InetSocketAddress localAddress,
      InetSocketAddress remoteAddress,
      Transport transport,
      Function computeFunction) {
    ConnectionKey connectionKey =
        new ConnectionKey(
            localAddress.getAddress(),
            localAddress.getPort(),
            remoteAddress.getAddress(),
            remoteAddress.getPort(),
            transport);
    return connectionMap.computeIfAbsent(connectionKey, computeFunction);
  }

  public void remove(
      InetSocketAddress localAddress,
      InetSocketAddress remoteAddress,
      Transport transport,
      CompletableFuture<Connection> connectionCompletableFuture) {
    ConnectionKey connectionKey =
        new ConnectionKey(
            localAddress.getAddress(),
            localAddress.getPort(),
            remoteAddress.getAddress(),
            remoteAddress.getPort(),
            transport);
    connectionMap.remove(connectionKey, connectionCompletableFuture);
  }

  public CompletableFuture<Connection> remove(
      InetSocketAddress localAddress, InetSocketAddress remoteAddress, Transport transport) {
    ConnectionKey connectionKey =
        new ConnectionKey(
            localAddress.getAddress(),
            localAddress.getPort(),
            remoteAddress.getAddress(),
            remoteAddress.getPort(),
            transport);
    return connectionMap.remove(connectionKey);
  }

  protected ConcurrentHashMap<ConnectionKey, CompletableFuture<Connection>> getConnectionMap() {
    return connectionMap;
  }

  protected void clear() {
    connectionMap.clear();
  }
}
