/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty.hanlder;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.SIPMessageGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import org.testng.annotations.Test;

public class UDPChannelHandlerTest {

  private ExecutorService executorService = new ExecutorService("DhruvaSipServer");

  @Test(
      enabled = true,
      description =
          "Testing the UDPChannelHanlder readMethod using EmbeddedChannel,"
              + " Data is pushed to the channel using EmbeddedChannel and then "
              + "Message Handler action is verified using MessageForwarder")
  public void testChannelRead() {
    EmbeddedChannel embeddedChannel = spy(EmbeddedChannel.class);

    String inviteMessage = SIPMessageGenerator.getInviteMessage("Guru");
    byte[] inviteBytes = inviteMessage.getBytes();
    ByteBuf byteBuf = Unpooled.copiedBuffer(inviteBytes);
    InetSocketAddress remoteAddress = new InetSocketAddress("10.78.98.21", 5060);
    InetSocketAddress localAddress = new InetSocketAddress("10.78.98.20", 5070);

    DatagramPacket inviteDatagramPacket = new DatagramPacket(byteBuf, localAddress, remoteAddress);

    final byte[][] receivedMessageBytes = new byte[1][1];
    final DsBindingInfo[] receivedBindingInfo = new DsBindingInfo[1];

    // UDPChannelHandler should forward the message to MessageForwarder and we will assert the
    // received data
    MessageForwarder messageForwarder =
        (messageBytes, bindingInfo) -> {
          receivedMessageBytes[0] = messageBytes;
          receivedBindingInfo[0] = bindingInfo;
          System.out.println(bindingInfo);
        };

    doReturn(localAddress).when(embeddedChannel).localAddress();

    ChannelHandler udpChannelHandler = new UDPChannelHandler(messageForwarder,null, executorService);
    embeddedChannel.pipeline().addLast(udpChannelHandler);

    embeddedChannel.writeOneInbound(inviteDatagramPacket);

    assertHandlerData(
        inviteBytes, receivedMessageBytes[0], receivedBindingInfo[0], localAddress, remoteAddress);
  }

  @Test(
      enabled = true,
      description =
          "Testing the UDPChannelHanlder readMethod using EmbeddedChannel for multiple packets for"
              + " different serverchannels Data is pushed to the channel using EmbeddedChannel and then "
              + "Message Handler action is verified using MessageForwarder")
  public void testChannelReadForDifferentDataGramPackets() {
    EmbeddedChannel embeddedChannel1 = spy(EmbeddedChannel.class);
    EmbeddedChannel embeddedChannel2 = spy(EmbeddedChannel.class);

    String inviteMessage1 = SIPMessageGenerator.getInviteMessage("Guru");
    byte[] inviteBytes1 = inviteMessage1.getBytes();
    ByteBuf byteBuf1 = Unpooled.copiedBuffer(inviteBytes1);
    InetSocketAddress remoteAddress1 = new InetSocketAddress("10.78.98.21", 5060);
    InetSocketAddress localAddress1 = new InetSocketAddress("10.78.98.20", 5070);

    String inviteMessage2 = SIPMessageGenerator.getInviteMessage("Guru");
    byte[] inviteBytes2 = inviteMessage2.getBytes();
    ByteBuf byteBuf2 = Unpooled.copiedBuffer(inviteBytes2);
    InetSocketAddress remoteAddress2 = new InetSocketAddress("10.78.98.21", 5061);
    InetSocketAddress localAddress2 = new InetSocketAddress("10.78.98.20", 5071);

    DatagramPacket inviteDatagramPacket1 =
        new DatagramPacket(byteBuf1, localAddress1, remoteAddress1);

    DatagramPacket inviteDatagramPacket2 =
        new DatagramPacket(byteBuf2, localAddress2, remoteAddress2);

    final byte[][] receivedMessageBytes = new byte[1][1];
    final DsBindingInfo[] receivedBindingInfo = new DsBindingInfo[1];

    // UDPChannelHandler should forward the message to MessageForwarder and we will assert the
    // received data
    MessageForwarder messageForwarder =
        (messageBytes, bindingInfo) -> {
          receivedMessageBytes[0] = messageBytes;
          receivedBindingInfo[0] = bindingInfo;
          System.out.println(bindingInfo);
        };

    doReturn(localAddress1).when(embeddedChannel1).localAddress();
    doReturn(localAddress2).when(embeddedChannel2).localAddress();

    ChannelHandler udpChannelHandler = new UDPChannelHandler(messageForwarder,null, executorService);
    embeddedChannel1.pipeline().addLast(udpChannelHandler);
    embeddedChannel2.pipeline().addLast(udpChannelHandler);

    // Send 1st packet and assert
    embeddedChannel1.writeOneInbound(inviteDatagramPacket1);
    assertHandlerData(
        inviteBytes1,
        receivedMessageBytes[0],
        receivedBindingInfo[0],
        localAddress1,
        remoteAddress1);

    // Send 2nd packet and assert
    embeddedChannel2.writeOneInbound(inviteDatagramPacket2);
    assertHandlerData(
        inviteBytes2,
        receivedMessageBytes[0],
        receivedBindingInfo[0],
        localAddress2,
        remoteAddress2);
  }

  private void assertHandlerData(
      byte[] expectedInviteBytes,
      byte[] actualInviteBytes,
      DsBindingInfo actualBindingInfo,
      InetSocketAddress expectedLocalAddress,
      InetSocketAddress expectedRemoteAddress) {
    assertEquals(
        actualInviteBytes,
        expectedInviteBytes,
        "Received message and Message added to Channel Does not match");
    assertEquals(
        actualBindingInfo.getRemoteAddress(),
        expectedRemoteAddress.getAddress(),
        "Remote Address does not match");
    assertEquals(
        actualBindingInfo.getLocalAddress(),
        expectedLocalAddress.getAddress(),
        "Local Address does not match");
    assertEquals(
        actualBindingInfo.getRemotePort(),
        expectedRemoteAddress.getPort(),
        "Remote Port does not match");
    assertEquals(
        actualBindingInfo.getLocalPort(),
        expectedLocalAddress.getPort(),
        "Local Port does not match");
    assertEquals(actualBindingInfo.getTransport(), Transport.UDP, "Transport does not match");
  }
}
