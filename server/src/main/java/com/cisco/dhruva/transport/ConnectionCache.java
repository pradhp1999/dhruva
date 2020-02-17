/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.net.InetAddress;
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

  private static ConnectionCache connectionCache = new ConnectionCache();
  private Logger logger = DhruvaLoggerFactory.getLogger(ConnectionCache.class);
  private int connectionSweeperInterval = 60;
  private TimeUnit connectionSweeperIntervalTimeUnit = TimeUnit.MINUTES;

  private ConcurrentHashMap<ConnectionKey, CompletableFuture<Connection>> connectionMap =
      new ConcurrentHashMap();

  private ScheduledExecutorService connectionSweeperSchedulerService =
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
                    "Error getting connection from future in Connection Sweeper task exception is ",
                    e);
              }
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

  protected void clear() {
    connectionMap.clear();
  }

  class ConnectionKey {

    public InetAddress localAddress;
    public int localPort;
    public InetAddress remoteAddress;
    public int remotePort;
    public Transport transport;

    public ConnectionKey(
        InetAddress localAddress,
        int localPort,
        InetAddress remoteAddress,
        int remotePort,
        Transport transportType) {
      this.localAddress = localAddress;
      this.localPort = localPort;
      this.remoteAddress = remoteAddress;
      this.remotePort = remotePort;
      this.transport = transportType;
    }

    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof ConnectionKey)) {
        return false;
      }

      ConnectionKey aKey = (ConnectionKey) obj;
      if (aKey.remotePort != remotePort) {
        return false;
      }
      if (aKey.transport != transport) {
        return false;
      }
      // m_InetAddress should not be null. But we check it anyway
      if (aKey.remoteAddress == null || !(aKey.remoteAddress.equals(remoteAddress))) {
        return false;
      }
      // if one local port is not specified, we treat it as equal
      if ((aKey.localPort != localPort)
          && (aKey.localPort != DsBindingInfo.LOCAL_PORT_UNSPECIFIED)
          && (localPort != DsBindingInfo.LOCAL_PORT_UNSPECIFIED)) {
        return false;
      }
      // if one local address is not specified, we treat it as equal
      if ((aKey.localAddress != null)
          && (localAddress != null)
          && !(aKey.localAddress.equals(localAddress))) {
        return false;
      }

      return true;
    }

    /**
     * Determine the hash code.
     *
     * @return the hash code
     */
    public int hashCode() {
      // return m_Port * 2 + m_TransportType + m_InetAddress.hashCode();
      return ((remotePort + transport.ordinal()) * 3) * remoteAddress.hashCode();
    }

    public String toString() {
      StringBuffer buffer =
          new StringBuffer(64)
              .append(transport)
              .append(':')
              .append(localAddress == null ? null : localAddress.getHostAddress())
              .append(':')
              .append(localPort)
              .append(':')
              .append(remoteAddress == null ? null : remoteAddress.getHostAddress())
              .append(':')
              .append(remotePort);
      return buffer.toString();
    }
  }
}
