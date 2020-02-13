/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.transport.Client;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.ConnectionCache;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public class NettyUDPClient implements Client {

  private BaseChannelInitializer channelInitializer;
  private Bootstrap bootstrap;
  private NetworkConfig networkConfig;
  private ConnectionCache connectionCache;

  public NettyUDPClient(NetworkConfig networkConfig, MessageForwarder messageForwarder) {
    this.networkConfig = networkConfig;
    channelInitializer =
        ChannelInitializerFactory.getInstance()
            .getChannelInitializer(Transport.UDP, messageForwarder);
    bootstrap =
        BootStrapFactory.getInstance()
            .getClientBootStrap(Transport.UDP, networkConfig, channelInitializer);
    connectionCache = ConnectionCache.getInstance();
  }

  @Override
  public CompletableFuture<Connection> getConnection(
      SocketAddress localSocketAddress, SocketAddress remoteSocketAddress) {

    ChannelFuture channelFuture = bootstrap.connect(remoteSocketAddress, localSocketAddress);

    CompletableFuture<Connection> completableChannelFuture = new CompletableFuture();

    channelFuture.addListener(
        future -> {
          if (future.isSuccess()) {
            completableChannelFuture.complete(
                new UDPConnection(((ChannelFuture) future).channel(), networkConfig));
          } else {
            completableChannelFuture.completeExceptionally(future.cause());
          }
        });

    return completableChannelFuture;
  }
}
