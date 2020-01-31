/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

import com.cisco.dhruva.config.network.NetworkConfig;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TransportLayerTest {

  TransportLayer transportLayer;
  @InjectMocks
  MessageForwarder handler;

  @Mock
  Environment env= new MockEnvironment();

  @Test(
      enabled = false,
      description =
          "Testing the Server Socket creating success scenario for UDP and receiving a Message in handler")
  public void testStartListeningSuccessUDP() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    try {

      CompletableFuture<Boolean> startListenFuture =
          transportLayer.startListening(
              Transport.UDP, networkConfig, InetAddress.getByName("0.0.0.0"), 5060, handler);
      Boolean startListening = startListenFuture.get();
      assertEquals(startListening, Boolean.TRUE, "TransportLayer.startListening returned false");
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
      description =
          "Testing the Server Socket creating success scenario for UDP and receiving a Message in handler")
  public void testStartListeningFailureUDP() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    Exception expectedException = null; // Initialize this to the expected exception
    try {

      CompletableFuture<Boolean> startListenFuture =
          transportLayer.startListening(
              Transport.UDP, networkConfig, InetAddress.getByName("0.0.0.0"), 5060, handler);
      Boolean startListening = null;
      Throwable receivedException = null;
      try {
        startListening = startListenFuture.get();
      } catch (ExecutionException e) {
        receivedException = e.getCause();
      }
      if (startListening != null) {
        Assert.assertEquals(
            startListening,
            Boolean.TRUE,
            "TransportLayer.startListening returned true while expecting Exception");
      }

      Assert.assertNotNull(receivedException);
      Assert.assertEquals(
          receivedException,
          expectedException,
          "Received Exception cause and Expected cause are different");

    } catch (Exception e) {
      Assert.fail("Exception thrown in TransportLayer.startListening" + e.getMessage(), e);
    }
  }

  @Test(
      enabled = false,
      description =
          "Testing the messageHandler , Test creates a Server Socket , and data is simluated using Mocked socket channel , And"
              + "Checked if the same message is received at the handle")
  public void testMessageHanlderUDP() {
    NetworkConfig networkConfig = new NetworkConfig(env);
    ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
    byte[] expectedByte = new byte[] {'1', '2', '3'};

    try {
      CompletableFuture<Boolean> startListenFuture =
          transportLayer.startListening(
              Transport.UDP, networkConfig, InetAddress.getByName("0.0.0.0"), 5060, handler);
      Boolean startListening = startListenFuture.get();
      assertEquals(startListening, Boolean.TRUE, "TransportLayer.startListening returned false");
//      verify(handler).processMessage(captor.capture());
      byte[] receivedMessage = captor.getValue();
      assertEquals(
          receivedMessage, expectedByte, "Message Received in the Message handler is incorrect");
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
      CompletableFuture<Connection> connectionFuture =
          transportLayer.getConnection(
              networkConfig,
              Transport.UDP,
              InetAddress.getByName("0.0.0.0"),
              5060,
              InetAddress.getByName("1.1.1.1"),
              5060);
      Connection udpConnection = connectionFuture.get();
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
