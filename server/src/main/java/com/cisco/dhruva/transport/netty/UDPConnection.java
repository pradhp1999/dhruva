/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.hanlder.AbstractChannelHandler;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class UDPConnection extends AbstractConnection {

  Logger logger = DhruvaLoggerFactory.getLogger(UDPConnection.class);

  public UDPConnection(
      Channel channel, DsNetwork networkConfig, AbstractChannelHandler channelHandler) {
    super(channel, networkConfig, Transport.UDP, channelHandler);
  }

  /** @return transport type of this connection */
  @Override
  public Transport getConnectionType() {
    return this.transport;
  }

  @Override
  public CompletableFuture<Boolean> send(byte[] buffer) {

    ByteBuf byteBuf = channel.alloc().buffer();
    byteBuf.writeBytes(buffer);

    DatagramPacket packet =
        new DatagramPacket(
            byteBuf,
            (InetSocketAddress) channel.remoteAddress(),
            (InetSocketAddress) channel.localAddress());

    ChannelFuture channelFuture = channel.writeAndFlush(packet);

    logger.info(
        "Sending Message on channel {} , message is {} ",
        channel,
        DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(new String(buffer)));
    CompletableFuture writeFuture = new CompletableFuture();

    channelFuture.addListener(
        future -> {
          if (channelFuture.isSuccess()) {
            writeFuture.complete(true);
          } else {
            writeFuture.completeExceptionally(future.cause());
          }
        });

    return writeFuture;
  }
}
