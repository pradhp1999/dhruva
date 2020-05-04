/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.netty.BaseChannelInitializer;
import com.cisco.dhruva.transport.netty.BootStrapFactory;
import com.cisco.dhruva.transport.netty.hanlder.UDPChannelHandler;
import com.cisco.dhruva.util.SIPMessageGenerator;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DhruvaTransportLayerTest {

  TransportLayer transportLayer;
  @InjectMocks MessageForwarder handler;

  @Mock Environment env = new MockEnvironment();

  DsNetwork networkConfig;

  Bootstrap bootstrap;

  BaseChannelInitializer channelInitializer;

  ExecutorService executorService = new ExecutorService("DhruvaSipServer");

  @BeforeClass
  public void init() throws DsException {

    networkConfig = DsNetwork.getNetwork("Test");
    DsNetwork.setenv(env);
    handler = mock(MessageForwarder.class);
    ChannelHandler channelHandler = new UDPChannelHandler(handler,networkConfig, executorService);
    channelInitializer = new BaseChannelInitializer();
    channelInitializer.channelHanlder(channelHandler);
    bootstrap = (Bootstrap) Mockito.spy(Bootstrap.class);
    BootStrapFactory.getInstance().setUdpBootstrap(bootstrap);
  }

  @AfterMethod
  public void cleanup() {
    if (transportLayer != null) {
      transportLayer.clearConnectionCache();
    }

    reset(bootstrap);
  }

  @Test(
      enabled = true,
      description =
          "Testing the Server Socket creating success scenario for UDP and receiving a Message in handler")
  public void testStartListeningSuccessUDP() {

    try {

      InetAddress localAddress = InetAddress.getByName("0.0.0.0");
      int port = 5060;
      EmbeddedChannel embeddedChannel = new EmbeddedChannel();
      ChannelFuture bindFuture = mock(ChannelFuture.class);
      transportLayer =
          TransportLayerFactory.getInstance().getTransportLayer(handler, executorService);

      // Return success future when bind is called
      doAnswer(
              invocation -> {
                ChannelFuture channelFuture = embeddedChannel.newSucceededFuture();
                return channelFuture;
              })
          .when(bootstrap)
          .bind(localAddress, port);

      CompletableFuture startListenFuture =
          transportLayer.startListening(Transport.UDP, networkConfig, localAddress, port, handler);

      Boolean isBindComplete = startListenFuture.isDone();
      Object returnedChannel = startListenFuture.get();

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
      enabled = true,
      description = "Testing the Server Socket creating failure When Bootstrap binding fails")
  public void testStartListenExceptionInBindUDP() {

    try {
      DsNetwork networkConfig = DsNetwork.getNetwork("Test");
      DsNetwork.setenv(env);
      InetAddress localAddress = InetAddress.getByName("0.0.0.0");
      String exceptionError = "Bind failed";
      int port = 5060;
      transportLayer = TransportLayerFactory.getInstance().getTransportLayer(null, executorService);
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

  @Test(enabled = true, description = "Testing the TransportLayer startListening with null values")
  public void testStartListeningFailureUDPWithNullValues() {

    try {

      DsNetwork networkConfig = DsNetwork.getNetwork("Test");
      DsNetwork.setenv(env);
      InetAddress localAddress = InetAddress.getByName("0.0.0.0");
      int port = 5060;
      transportLayer = TransportLayerFactory.getInstance().getTransportLayer(null, executorService);
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
      enabled = true,
      description =
          "Testing the Server Socket creating success scenario with multiple "
              + "Server sockets for UDP and receiving a Message in handler")
  public void testStartListeningSuccessUDPMultipleServerSockets() {
    try {

      DsNetwork networkConfig = DsNetwork.getNetwork("Test");
      DsNetwork.setenv(env);
      InetAddress localAddress = InetAddress.getByName("0.0.0.0");
      int port1 = 5060;
      int port2 = 5070;
      transportLayer = TransportLayerFactory.getInstance().getTransportLayer(null, executorService);

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

      CompletableFuture startListenFuture1 =
          transportLayer.startListening(Transport.UDP, networkConfig, localAddress, port1, handler);

      CompletableFuture startListenFuture2 =
          transportLayer.startListening(Transport.UDP, networkConfig, localAddress, port2, handler);

      Boolean isBindComplete1 = startListenFuture1.isDone();
      Object returnedChannel1 = startListenFuture1.get();

      Boolean isBindComplete2 = startListenFuture2.isDone();
      Object returnedChannel2 = startListenFuture2.get();

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

  @Test(
      enabled = true,
      description =
          "Tests TransportLayer.getConnection() and Sending message using the connection for UDP transport")
  public void testGetConnectionZndSendMessageSuccess() {
    try {

      InetAddress localAddress = InetAddress.getByName("127.0.0.1");
      InetAddress remoteAddress = InetAddress.getByName("127.0.0.1");
      int localPort = 5070, remotePort = 5060;
      InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);
      byte[] inviteMessageToSend = SIPMessageGenerator.getInviteMessage("graivitt").getBytes();
      byte[] receivedMessage;
      transportLayer =
          TransportLayerFactory.getInstance().getTransportLayer((a, b) -> {}, executorService);

      EmbeddedChannel channel = spy(EmbeddedChannel.class);

      // mocking the netty channel
      doAnswer(
              invocation -> {
                return remoteSocketAddress;
              })
          .when(channel)
          .remoteAddress();
      doAnswer(
              invocation -> {
                return localSocketAddress;
              })
          .when(channel)
          .localAddress();

      // Argument capture to validate if the Datagram packet sent in the channel has proper content
      // and address set
      ArgumentCaptor<DatagramPacket> argumentCaptor = ArgumentCaptor.forClass(DatagramPacket.class);

      // Return success future when connect is called
      doAnswer(
              invocation -> {
                ChannelFuture channelFuture = channel.newSucceededFuture();
                return channelFuture;
              })
          .when(bootstrap)
          .connect(remoteSocketAddress, localSocketAddress);

      CompletableFuture<Connection> connectionFuture =
          transportLayer.getConnection(
              networkConfig, Transport.UDP, localAddress, localPort, remoteAddress, remotePort);

      Connection udpConnection = connectionFuture.get();

      CompletableFuture writeFuture = udpConnection.send(inviteMessageToSend);

      // wait for the send to complete
      writeFuture.get();

      verify(channel).writeAndFlush(argumentCaptor.capture());

      DatagramPacket packetAtChannel = argumentCaptor.getValue();
      ByteBuf receivedByteBuf = packetAtChannel.content();
      receivedMessage = new byte[receivedByteBuf.readableBytes()];
      receivedByteBuf.readBytes(receivedMessage);

      // assert the message sent and message at channel are same
      assertEquals(receivedMessage, inviteMessageToSend);
      assertEquals(packetAtChannel.recipient(), remoteSocketAddress);
      assertEquals(packetAtChannel.sender(), localSocketAddress);

    } catch (Exception e) {
      Assert.fail("UnExpected Exception in TransportLayer.getConnection ", e);
    }
  }

  @Test(
      enabled = true,
      description = "Tests TransportLayer.getConnection() and connection creation fails",
      expectedExceptions = Exception.class)
  public void testGetConnectionConnectionFail()
      throws InterruptedException, ExecutionException, UnknownHostException {
    try {

      InetAddress localAddress = InetAddress.getByName("10.78.98.21");
      InetAddress remoteAddress = InetAddress.getByName("10.78.98.22");
      int localPort = 5070, remotePort = 5060;
      InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);
      transportLayer =
          TransportLayerFactory.getInstance().getTransportLayer((a, b) -> {}, executorService);

      // Return failure future when connect is called
      EmbeddedChannel channel = new EmbeddedChannel();
      doAnswer(
              invocation -> {
                ChannelFuture channelFuture =
                    channel.newFailedFuture(new IOException("Bind Failure"));
                return channelFuture;
              })
          .when(bootstrap)
          .connect(remoteSocketAddress, localSocketAddress);

      CompletableFuture<Connection> connectionFuture =
          transportLayer.getConnection(
              networkConfig, Transport.UDP, localAddress, localPort, remoteAddress, remotePort);

      Connection udpConnection = connectionFuture.get();

    } catch (Exception e) {
      assertEquals(e.getCause().getClass(), IOException.class);
      assertEquals(e.getCause().getMessage(), "Bind Failure");
      System.out.println("guru");
      System.out.println(e);
      throw e;
    }
  }

  @Test(
      enabled = true,
      description =
          "Tests TransportLayer.getConnection() and Sending message using the connection for UDP transport")
  public void testGetMultipleConnectionToSameDestination() {
    try {

      InetAddress localAddress = InetAddress.getByName("10.78.98.21");
      InetAddress remoteAddress = InetAddress.getByName("10.78.98.22");
      int localPort = 5070, remotePort = 5060;
      InetSocketAddress remoteSocketAddress = new InetSocketAddress(remoteAddress, remotePort);
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);
      byte[] inviteMessageToSend = SIPMessageGenerator.getInviteMessage("graivitt").getBytes();
      byte[] receivedMessage;
      transportLayer =
          TransportLayerFactory.getInstance().getTransportLayer((a, b) -> {}, executorService);

      EmbeddedChannel channel = spy(EmbeddedChannel.class);

      // mocking the netty channel
      doAnswer(
              invocation -> {
                return remoteSocketAddress;
              })
          .when(channel)
          .remoteAddress();
      doAnswer(
              invocation -> {
                return localSocketAddress;
              })
          .when(channel)
          .localAddress();

      // Argument capture to validate if the Datagram packet sent in the channel has proper content
      // and address set
      ArgumentCaptor<DatagramPacket> argumentCaptor = ArgumentCaptor.forClass(DatagramPacket.class);

      // Return success future when connect is called
      doAnswer(
              invocation -> {
                ChannelFuture channelFuture = channel.newSucceededFuture();
                return channelFuture;
              })
          .when(bootstrap)
          .connect(remoteSocketAddress, localSocketAddress);

      CompletableFuture<Connection> connectionFuture1 =
          transportLayer.getConnection(
              networkConfig, Transport.UDP, localAddress, localPort, remoteAddress, remotePort);

      CompletableFuture<Connection> connectionFuture2 =
          transportLayer.getConnection(
              networkConfig, Transport.UDP, localAddress, localPort, remoteAddress, remotePort);

      Connection connection1 = connectionFuture1.get();
      Connection connection2 = connectionFuture2.get();

      assertEquals(connection1, connection2);
      assertEquals(connection1.getConnectionInfo().getLocalAddress(), localAddress);
      assertEquals(connection1.getConnectionInfo().getLocalPort(), localPort);
      assertEquals(connection1.getConnectionInfo().getRemoteAddress(), remoteAddress);
      assertEquals(connection1.getConnectionInfo().getRemotePort(), remotePort);

      // Check if its only invoked once , second connection should come from cache
      verify(bootstrap, Mockito.times(1)).connect(remoteSocketAddress, localSocketAddress);
    } catch (Exception e) {
      Assert.fail("UnExpected Exception in TransportLayer.getConnection ", e);
    }
  }

  @Test(
      enabled = true,
      description =
          "Tests TransportLayer.getConnection() with remote adress null, "
              + "returned Future should have nullPointer exception")
  public void testGetConnectionWithRemoteAddressNull() {

    Exception receivedException = null;
    try {

      InetAddress localAddress = InetAddress.getByName("10.78.98.21");
      InetAddress remoteAddress = null; // remote address is null for this test
      int localPort = 5070, remotePort = 5060;
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);
      transportLayer =
          TransportLayerFactory.getInstance().getTransportLayer((a, b) -> {}, executorService);

      CompletableFuture<Connection> connectionFuture =
          transportLayer.getConnection(
              networkConfig, Transport.UDP, localAddress, localPort, remoteAddress, remotePort);

      Connection udpConnection = connectionFuture.get();

    } catch (Exception e) {
      receivedException = e;
    }

    String expectedExceptionMessage =
        "transportType or remoteAddress passed to DhruvaTransportLayer.getConnection is null transport = "
            + Transport.UDP
            + " , remoteAddress = null";
    assertEquals(receivedException.getCause().getClass(), NullPointerException.class);
    assertEquals(receivedException.getCause().getMessage(), expectedExceptionMessage);
  }

  @Test(
      enabled = true,
      description =
          "Tests TransportLayer.getConnection() with remote adress null, "
              + "returned Future should have nullPointer exception")
  public void testGetConnectionWithInvalidRemotePort() {
    int localPort = 5070, remotePort = 0;
    Exception receivedException = null;
    try {

      InetAddress localAddress = InetAddress.getByName("10.78.98.21");
      InetAddress remoteAddress = InetAddress.getByName("10.78.98.22");
      ; // remote address is null for this test
      InetSocketAddress localSocketAddress = new InetSocketAddress(localAddress, localPort);
      transportLayer =
          TransportLayerFactory.getInstance().getTransportLayer((a, b) -> {}, executorService);

      CompletableFuture<Connection> connectionFuture =
          transportLayer.getConnection(
              networkConfig, Transport.UDP, localAddress, localPort, remoteAddress, remotePort);

      Connection udpConnection = connectionFuture.get();

    } catch (Exception e) {
      receivedException = e;
    }

    String expectedExceptionMessage =
        "Invalid remoteport  value in DhruvaTransportLayer.getConnection , remotePort = "
            + remotePort;
    assertEquals(receivedException.getCause().getClass(), Exception.class);
    assertEquals(receivedException.getCause().getMessage(), expectedExceptionMessage);
  }

  @Test(
      enabled = false,
      description =
          "Tests the get connection summary for UDP connections when all connections are active")
  public void testGetConnectionSummary() {
    try {
      DsNetwork networkConfig = DsNetwork.getNetwork("Test");
      DsNetwork.setenv(env);
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
    try {
      DsNetwork networkConfig = DsNetwork.getNetwork("Test");
      DsNetwork.setenv(env);
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
