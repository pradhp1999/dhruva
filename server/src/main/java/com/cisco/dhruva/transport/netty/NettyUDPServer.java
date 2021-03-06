/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.ChannelEventsListener;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Server;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.hanlder.UDPChannelHandler;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public class NettyUDPServer implements Server {

  private final BaseChannelInitializer channelInitializer;
  private AbstractBootstrap udpBootstrap;
  private MessageForwarder messageForwarder;
  private DsNetwork networkConfig;
  private UDPChannelHandler udpChannelHandler;

  public NettyUDPServer(
      MessageForwarder messageForwarder,
      DsNetwork networkConfig,
      ExecutorService executorService,
      MetricService metricService) {
    this.networkConfig = networkConfig;
    udpChannelHandler =
        new UDPChannelHandler(messageForwarder, networkConfig, executorService, metricService);
    udpChannelHandler.setServerMode(true);
    udpChannelHandler.messageForwarder(messageForwarder);
    channelInitializer = new BaseChannelInitializer(networkConfig, udpChannelHandler);
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

  @Override
  public void addConnectionEventHandler(ChannelEventsListener connectionEventHandler) {
    udpChannelHandler.subscribeForChannelEvents(connectionEventHandler);
  }
}
