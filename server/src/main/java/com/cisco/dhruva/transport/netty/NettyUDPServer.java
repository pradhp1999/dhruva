/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Server;
import com.cisco.dhruva.transport.Transport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public class NettyUDPServer implements Server {

  private final BaseChannelInitializer channelInitializer;
  private Bootstrap udpBootstrap;
  private MessageForwarder messageForwarder;
  private NetworkConfig networkConfig;

  public NettyUDPServer(MessageForwarder messageForwarder, NetworkConfig networkConfig) {
    this.networkConfig = networkConfig;
    channelInitializer =
        ChannelInitializerFactory.getInstance()
            .getChannelInitializer(Transport.UDP, messageForwarder);
    this.udpBootstrap =
        BootStrapFactory.getInstance()
            .getServerBootStrap(Transport.UDP, networkConfig, channelInitializer);
  }

  @Override
  public void startListening(InetAddress address, int port, CompletableFuture serverStartFuture) {
    ChannelFuture channelFuture = udpBootstrap.bind(address, port);
    channelFuture.addListener(
        bindFuture -> {
          if (bindFuture.isSuccess()) {
            Channel channel = ((ChannelFuture) bindFuture).channel();
            serverStartFuture.complete(channel);
          } else {
            serverStartFuture.completeExceptionally(bindFuture.cause());
          }
        });
  }
}
