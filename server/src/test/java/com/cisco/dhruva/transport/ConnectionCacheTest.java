/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.transport.netty.UDPConnection;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConnectionCacheTest {

  @Test(
      description =
          "Testing connection cache cleanup logic with three Connections, with last connection unreferenced ,"
              + " Timer values are tuned to run withgout delay")
  public void testConnectionCacheCleanupWithThreeConnections() {

    // get a connection cache which sweeps in a second
    ConnectionCache connectionCache = ConnectionCache.getInstance(1, TimeUnit.SECONDS);

    // Create connections

    NetworkConfig networkConfig = mock(NetworkConfig.class);

    EmbeddedChannel channel = spy(EmbeddedChannel.class);

    try {

      when(networkConfig.connectionCacheConnectionIdleTimeout()).thenReturn(1);

      InetAddress localAddress = InetAddress.getByName("10.78.98.21");
      InetAddress remoteAddress = InetAddress.getByName("10.78.98.22");
      int localPort = 5070, remotePort = 5060;

      // Add connection to the cache
      InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);

      doAnswer(
              invocation -> {
                return (SocketAddress) localSocketAddress;
              })
          .when(channel)
          .localAddress();

      doAnswer(
              invocation -> {
                return remoteSocketAddress;
              })
          .when(channel)
          .remoteAddress();

      Connection connection = new UDPConnection(channel, networkConfig);
      connection.addReference();
      CompletableFuture<Connection> connection1Future = new CompletableFuture<>();
      connection1Future.complete(connection);

      InetSocketAddress remoteSocketAddress2 = new InetSocketAddress(remoteAddress, 5070);
      doAnswer(
              invocation -> {
                return (SocketAddress) remoteSocketAddress2;
              })
          .when(channel)
          .remoteAddress();
      Connection connection2 = new UDPConnection(channel, networkConfig);
      connection.addReference();
      CompletableFuture<Connection> connection2Future = new CompletableFuture<>();
      connection2Future.complete(connection2);
      connection2.addReference();

      connectionCache.add(
          localSocketAddress, remoteSocketAddress, Transport.UDP, connection1Future);

      connectionCache.add(
          localSocketAddress, remoteSocketAddress2, Transport.UDP, connection2Future);

      // Add a third connection without any reference
      InetSocketAddress remoteSocketAddress3 = new InetSocketAddress(remoteAddress, 5080);
      doAnswer(
              invocation -> {
                return remoteSocketAddress3;
              })
          .when(channel)
          .remoteAddress();
      Connection connection3 = new UDPConnection(channel, networkConfig);

      CompletableFuture<Connection> connection3Future = new CompletableFuture<>();
      connection3Future.complete(connection3);
      connectionCache.add(
          localSocketAddress, remoteSocketAddress3, Transport.UDP, connection3Future);

      Assert.assertEquals(
          connectionCache.get(localSocketAddress, remoteSocketAddress3, Transport.UDP),
          connection3Future);
      // sleep for 1 sec and check if connection  is present , connection should be in cache as its
      // referenced
      Thread.sleep(1000);

      // Connection should be removed as it is not referenced
      Assert.assertEquals(
          connectionCache.get(localSocketAddress, remoteSocketAddress3, Transport.UDP), null);

      // This should be present as its still referenced
      Assert.assertEquals(
          connectionCache.get(localSocketAddress, remoteSocketAddress, Transport.UDP),
          connection1Future);
      Assert.assertEquals(
          connectionCache.get(localSocketAddress, remoteSocketAddress2, Transport.UDP),
          connection2Future);

    } catch (Exception e) {
      Assert.fail("UnExpected Exception in ConnectionCacheTest testConnectionCacheCleanup", e);
    }
  }

  @Test
  public void testConnectionCacheCleanupWithExceptionInOneConnectionGet() {

    // get a connection cache which sweeps in a second
    ConnectionCache connectionCache = ConnectionCache.getInstance(1, TimeUnit.SECONDS);

    // Create connections

    NetworkConfig networkConfig = mock(NetworkConfig.class);

    EmbeddedChannel channel = spy(EmbeddedChannel.class);

    try {

      when(networkConfig.connectionCacheConnectionIdleTimeout()).thenReturn(1);

      InetAddress localAddress = InetAddress.getByName("10.78.98.21");
      InetAddress remoteAddress = InetAddress.getByName("10.78.98.22");
      int localPort = 5070, remotePort = 5060;

      // Add connection to the cache
      InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);

      Connection connection = mock(Connection.class);

      // Connection connection throws exception , sweeper should recover
      CompletableFuture<Connection> connection1Future = new CompletableFuture<>();
      connection1Future.completeExceptionally(new Exception("Bind Exception"));
      connectionCache.add(
          localSocketAddress, remoteSocketAddress, Transport.UDP, connection1Future);

      CompletableFuture<Connection> connection2Future = new CompletableFuture<>();
      Connection connection2 = mock(Connection.class);
      when(connection2.shouldClose()).thenReturn(true);
      connection2Future.complete(connection2);
      InetSocketAddress remoteSocketAddress2 = new InetSocketAddress(remoteAddress, 5070);
      connectionCache.add(
          localSocketAddress, remoteSocketAddress2, Transport.UDP, connection2Future);

      // sleep for 1 sec and check if connection  is present , connection should be in cache as its
      // referenced
      Thread.sleep(2000);

      // Connection should be removed as it is not referenced
      Assert.assertEquals(
          connectionCache.get(localSocketAddress, remoteSocketAddress2, Transport.UDP), null);

      verify(connection2, Mockito.times(1)).closeConnection();

    } catch (Exception e) {
      Assert.fail("UnExpected Exception in ConnectionCacheTest testConnectionCacheCleanup", e);
    }
  }
}
