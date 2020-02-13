/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.transport.netty.BaseChannelInitializer;
import com.cisco.dhruva.transport.netty.BootStrapFactory;
import com.cisco.dhruva.transport.netty.ChannelInitializerFactory;
import com.cisco.dhruva.transport.netty.hanlder.UDPChannelHandler;
import com.cisco.dhruva.util.SIPMessageGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DhruvaTransportLayerTest {

  TransportLayer transportLayer;
  @InjectMocks MessageForwarder handler;

  @Mock Environment env = new MockEnvironment();

  @Test(
      enabled = true,
      description =
          "Testing the Server Socket creating success scenario for UDP and receiving a Message in handler")
  public void testStartListeningSuccessUDP() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    try {

      InetAddress localAddress = InetAddress.getByName("0.0.0.0");
      int port = 5060;
      handler = mock(MessageForwarder.class);
      EmbeddedChannel embeddedChannel = new EmbeddedChannel();
      ChannelFuture bindFuture = mock(ChannelFuture.class);

      BaseChannelInitializer channelInitializer =
          ChannelInitializerFactory.getInstance().getChannelInitializer(Transport.UDP, handler);
      Bootstrap bootstrap =
          spy(
              BootStrapFactory.getInstance()
                  .getServerBootStrap(Transport.UDP, networkConfig, channelInitializer));

      BootStrapFactory.getInstance().setUdpBootstrap(bootstrap);

      transportLayer = TransportLayerFactory.getInstance().getTransportLayer(handler);

      // Return success future when bind is called
      doAnswer(
              invocation -> {
                ChannelFuture channelFuture = embeddedChannel.newSucceededFuture();
                return channelFuture;
              })
          .when(bootstrap)
          .bind(localAddress, port);

      // Argument capture to validate if the ChannelInitializer is called with proper values
      ArgumentCaptor<BaseChannelInitializer> argumentCaptor =
          ArgumentCaptor.forClass(BaseChannelInitializer.class);

      CompletableFuture startListenFuture =
          transportLayer.startListening(Transport.UDP, networkConfig, localAddress, port, handler);

      Boolean isBindComplete = startListenFuture.isDone();
      Object returnedChannel = startListenFuture.get();

      //verify(bootstrap).handler(argumentCaptor.capture());
      BaseChannelInitializer capturedBaseChannelInitializer = argumentCaptor.getValue();
      assertEquals(
          capturedBaseChannelInitializer.getMessageHandler().getClass(),
          UDPChannelHandler.class,
          "Message handler initialized is incorrect ");
      assertEquals(isBindComplete, Boolean.TRUE, "TransportLayer.startListening returned false");
      assertEquals(returnedChannel, embeddedChannel, "Returned channel is not matching");
    } catch (Exception e) {
      if (e instanceof ExecutionException) {
        Assert.fail(
            "Exception thrown in TransportLayer.startListening" + e.getCause().getMessage(),
            e.getCause());
      }
      Assert.fail("Exception thrown in TransportLayer.startListening" + e.getMessage(), e);
    }
  }

  @Test(
      enabled = false,
      description = "Testing the Server Socket creating failure When Bootstrap binding fails")
  public void testStartListenExceptionInBindUDP() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    try {

      InetAddress localAddress = InetAddress.getByName("0.0.0.0");
      String exceptionError = "Bind failed";
      int port = 5060;
      transportLayer = TransportLayerFactory.getInstance().getTransportLayer(null);
      handler = mock(MessageForwarder.class);
      BaseChannelInitializer channelInitializer =
          ChannelInitializerFactory.getInstance().getChannelInitializer(Transport.UDP, handler);
      Bootstrap bootstrap =
          spy(
              BootStrapFactory.getInstance()
                  .getServerBootStrap(Transport.UDP, networkConfig, channelInitializer));

      // Return Future which failed when bind is called
      doAnswer(
              invocation -> {
                EmbeddedChannel embeddedChannel = new EmbeddedChannel();
                ChannelFuture channelFuture =
                    embeddedChannel.newFailedFuture(new Exception(exceptionError));
                return channelFuture;
              })
          .when(bootstrap)
          .bind(localAddress, port);

      // Argument capture to validate if the ChannelInitializer is called with proper values
      ArgumentCaptor<BaseChannelInitializer> argumentCaptor =
          ArgumentCaptor.forClass(BaseChannelInitializer.class);

      CompletableFuture startListenFuture =
          transportLayer.startListening(Transport.UDP, networkConfig, localAddress, port, handler);

      Boolean isCompletedExceptionally = startListenFuture.isCompletedExceptionally();

      startListenFuture.whenComplete(
          (arg1, arg2) -> {
            assertEquals(arg2.getClass(), Exception.class, "Expected Exception as bind is failing");
            assertEquals(
                arg2.toString(),
                new Exception(exceptionError),
                "Exception error string is not matching for bind failure");
          });

      verify(bootstrap).handler(argumentCaptor.capture());
      BaseChannelInitializer capturedBaseChannelInitializer = argumentCaptor.getValue();
      assertEquals(
          capturedBaseChannelInitializer.getMessageHandler().getClass(),
          UDPChannelHandler.class,
          "Message handler initialized is incorrect ");
      assertEquals(
          isCompletedExceptionally,
          Boolean.TRUE,
          "TransportLayer.startListening should have completed Exceptionally");
    } catch (Exception e) {
      if (e instanceof ExecutionException) {
        Assert.fail(
            "Exception thrown in TransportLayer.startListening" + e.getCause().getMessage(),
            e.getCause());
      }
      Assert.fail("Exception thrown in TransportLayer.startListening" + e.getMessage(), e);
    }
  }

  @Test(enabled = false, description = "Testing the TransportLayer startListening with null values")
  public void testStartListeningFailureUDPWithNullValues() {
    NetworkConfig networkConfig = new NetworkConfig(env);

    try {
      InetAddress localAddress = InetAddress.getByName("0.0.0.0");
      int port = 5060;
      transportLayer = TransportLayerFactory.getInstance().getTransportLayer(null);
      CompletableFuture startListenFuture =
          transportLayer.startListening(Transport.UDP, networkConfig, null, port, handler);

      startListenFuture.whenComplete(
          (o, o2) -> {
            Assert.assertEquals(
                o2.getClass(), NullPointerException.class, "Expected Null Pointer Exception");
          });

      startListenFuture.join();
    } catch (CompletionException ne) {
      Assert.assertEquals(
          ne.getCause().getClass(), NullPointerException.class, "Expected Null Pointer Exception");
      // Expected
    } catch (Exception e) {
      Assert.fail(
          "Exception thrown in TransportLayer.startListening expecting a "
              + "CompletableFuture which is completed Exceptionally"
              + e.getMessage(),
          e);
    }
  }

  @Test(
      enabled = false,
      description =
          "Testing the Server Socket creating success scenario with multiple "
              + "Server sockets for UDP and receiving a Message in handler")
  public void testStartListeningSuccessUDPMultipleServerSockets() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    try {

      InetAddress localAddress = InetAddress.getByName("0.0.0.0");
      int port1 = 5060;
      int port2 = 5070;
      transportLayer = TransportLayerFactory.getInstance().getTransportLayer(null);
      handler = mock(MessageForwarder.class);
      BaseChannelInitializer channelInitializer =
          ChannelInitializerFactory.getInstance().getChannelInitializer(Transport.UDP, handler);
      Bootstrap bootstrap =
          spy(
              BootStrapFactory.getInstance()
                  .getServerBootStrap(Transport.UDP, networkConfig, channelInitializer));

      EmbeddedChannel embeddedChannel1 = new EmbeddedChannel();
      EmbeddedChannel embeddedChannel2 = new EmbeddedChannel();

      ChannelFuture bindFuture = mock(ChannelFuture.class);

      // Return success future when bind is called
      doAnswer(
              invocation -> {
                ChannelFuture channelFuture = embeddedChannel1.newSucceededFuture();
                return channelFuture;
              })
          .when(bootstrap)
          .bind(localAddress, port1);

      // Return success future when bind is called
      doAnswer(
              invocation -> {
                ChannelFuture channelFuture = embeddedChannel2.newSucceededFuture();
                return channelFuture;
              })
          .when(bootstrap)
          .bind(localAddress, port2);

      // Argument capture to validate if the ChannelInitializer is called with proper values
      ArgumentCaptor<BaseChannelInitializer> argumentCaptor =
          ArgumentCaptor.forClass(BaseChannelInitializer.class);

      CompletableFuture startListenFuture1 =
          transportLayer.startListening(Transport.UDP, networkConfig, localAddress, port1, handler);

      CompletableFuture startListenFuture2 =
          transportLayer.startListening(Transport.UDP, networkConfig, localAddress, port2, handler);

      Boolean isBindComplete1 = startListenFuture1.isDone();
      Object returnedChannel1 = startListenFuture1.get();

      Boolean isBindComplete2 = startListenFuture2.isDone();
      Object returnedChannel2 = startListenFuture2.get();

      verify(bootstrap).handler(argumentCaptor.capture());
      BaseChannelInitializer capturedBaseChannelInitializer = argumentCaptor.getValue();
      assertEquals(
          capturedBaseChannelInitializer.getMessageHandler().getClass(),
          UDPChannelHandler.class,
          "Message handler initialized is incorrect ");
      assertEquals(isBindComplete1, Boolean.TRUE, "TransportLayer.startListening returned false");
      assertEquals(returnedChannel1, embeddedChannel1, "Returned channel is not matching");
      assertEquals(isBindComplete2, Boolean.TRUE, "TransportLayer.startListening returned false");
      assertEquals(returnedChannel2, embeddedChannel2, "Returned channel is not matching");
    } catch (Exception e) {
      if (e instanceof ExecutionException) {
        Assert.fail(
            "Exception thrown in TransportLayer.startListening" + e.getCause().getMessage(),
            e.getCause());
      }
      Assert.fail("Exception thrown in TransportLayer.startListening" + e.getMessage(), e);
    }
  }

  @Test(enabled = false, description = "Tests TransportLayer.getConnection() for UDP transport")
  public void testGetConnectionSuccess() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    try {

      transportLayer =
          TransportLayerFactory.getInstance()
              .getTransportLayer(
                  (a, b) -> {
                    System.out.println("MessageForwarder " + new String(a) + " ");
                  });
      CompletableFuture<Connection> connectionFuture =
          transportLayer.getConnection(
              networkConfig,
              Transport.UDP,
              InetAddress.getByName("0.0.0.0"),
              5060,
              InetAddress.getByName("10.78.98.21"),
              5060);
      Connection udpConnection = connectionFuture.get();

      CompletableFuture writeFuture =
          udpConnection.send(SIPMessageGenerator.getInviteMessage("graivitt").getBytes());

      System.out.println(writeFuture);
      System.out.println(writeFuture.isDone());
      System.out.println(writeFuture.get());
      System.out.println(writeFuture.isDone());

      Assert.assertNotNull(udpConnection, "udpConnection object is null");

    } catch (Exception e) {
      Assert.fail("UnExpected Exception in TransportLayer.getConnection ", e);
    }
  }

  @Test(
      enabled = false,
      description =
          "Tests the get connection summary for UDP connections when all connections are active")
  public void testGetConnectionSummary() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    try {
      CompletableFuture<Connection> connectionFuture1 =
          transportLayer.getConnection(
              networkConfig,
              Transport.UDP,
              InetAddress.getByName("0.0.0.0"),
              5060,
              InetAddress.getByName("1.1.1.1"),
              5060);
      CompletableFuture<Connection> connectionFuture2 =
          transportLayer.getConnection(
              networkConfig,
              Transport.UDP,
              InetAddress.getByName("0.0.0.0"),
              5060,
              InetAddress.getByName("1.1.1.1"),
              5061);

      HashMap<Transport, Integer> summaryMap = transportLayer.getConnectionSummary();
      Assert.assertEquals(
          summaryMap.get(Transport.UDP),
          (Integer) 2,
          "Expected 2 connections from transportLayer.getConnectionSummary() , but received "
              + summaryMap.get(Transport.UDP));

    } catch (Exception e) {
      Assert.fail("UnExpected Exception in testGetConnectionSummary ", e);
    }
  }

  @Test(
      enabled = false,
      description =
          "Tests the get connection summary for UDP connections , after connection disconnect")
  public void testGetConnectionSummaryDisconnect() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    try {
      CompletableFuture<Connection> connectionFuture1 =
          transportLayer.getConnection(
              networkConfig,
              Transport.UDP,
              InetAddress.getByName("0.0.0.0"),
              5060,
              InetAddress.getByName("1.1.1.1"),
              5060);
      CompletableFuture<Connection> connectionFuture2 =
          transportLayer.getConnection(
              networkConfig,
              Transport.UDP,
              InetAddress.getByName("0.0.0.0"),
              5060,
              InetAddress.getByName("1.1.1.1"),
              5061);
      Connection secondConnection = connectionFuture2.get();
      secondConnection.closeConnection();

      HashMap<Transport, Integer> summaryMap = transportLayer.getConnectionSummary();
      Assert.assertEquals(
          summaryMap.get(Transport.UDP),
          (Integer) 1,
          "Expected 1 connections from transportLayer.getConnectionSummary() , but received "
              + summaryMap.get(Transport.UDP));

    } catch (Exception e) {
      Assert.fail("UnExpected Exception in testGetConnectionSummary ", e);
    }
  }
}
