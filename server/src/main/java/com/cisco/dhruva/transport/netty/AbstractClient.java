/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.ChannelEventsListener;
import com.cisco.dhruva.transport.Client;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.netty.hanlder.AbstractChannelHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import java.net.SocketAddress;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractClient implements Client {

  protected ChannelInitializer channelInitializer;
  protected Bootstrap bootstrap;
  protected DsNetwork networkConfig;
  protected AbstractChannelHandler channelHandler;

  public AbstractClient(
      DsNetwork networkConfig,
      MessageForwarder messageForwarder,
      ExecutorService executorService,
      MetricService metricService) {
    this.networkConfig = networkConfig;
  }

  protected void createBootStrap() throws Exception {
    bootstrap =
        BootStrapFactory.getInstance()
            .getClientBootStrap(getTransport(), networkConfig, channelInitializer);
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
                getTransport()
                    .getConnection(
                        ((ChannelFuture) future).channel(), networkConfig, channelHandler));
          } else {
            completableChannelFuture.completeExceptionally(future.cause());
          }
        });

    return completableChannelFuture;
  }

  @Override
  public void addConnectionEventHandler(ChannelEventsListener connectionEventHandler) {
    channelHandler.subscribeForChannelEvents(connectionEventHandler);
  }
}
