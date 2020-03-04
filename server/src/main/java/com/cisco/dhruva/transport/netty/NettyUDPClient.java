/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.hanlder.UDPChannelHandler;
import io.netty.channel.ChannelFuture;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public class NettyUDPClient extends AbstractClient {

  public NettyUDPClient(
      DsNetwork networkConfig, MessageForwarder messageForwarder, ExecutorService executorService) {
    this.networkConfig = networkConfig;
    channelInitializer = new BaseChannelInitializer();

    channelHandler = new UDPChannelHandler(messageForwarder, executorService);
    channelInitializer.channelHanlder(channelHandler);

    bootstrap =
        BootStrapFactory.getInstance()
            .getClientBootStrap(Transport.UDP, networkConfig, channelInitializer);
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
                new UDPConnection(
                    ((ChannelFuture) future).channel(), networkConfig, channelHandler));
          } else {
            completableChannelFuture.completeExceptionally(future.cause());
          }
        });

    return completableChannelFuture;
  }
}
