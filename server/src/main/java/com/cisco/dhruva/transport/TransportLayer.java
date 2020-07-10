/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface TransportLayer {

  public CompletableFuture startListening(
      Transport transportType,
      DsNetwork transportConfig,
      InetAddress address,
      int port,
      MessageForwarder handler);

  public CompletableFuture<Connection> getConnection(
      DsNetwork networkConfig,
      Transport transportType,
      InetAddress localAddress,
      int localPort,
      InetAddress remoteAddress,
      int remotePort);

  public CompletableFuture<Connection> getCachedConnection(
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

  void clearConnectionCache();

  void stop();

  ConnectionKey findListenKeyForTransport(Transport transport);

  void setMaxConnections(int max_connections);

  int getMaxConnections();

  Enumeration getListenKeys();

  void shutdown();
}
