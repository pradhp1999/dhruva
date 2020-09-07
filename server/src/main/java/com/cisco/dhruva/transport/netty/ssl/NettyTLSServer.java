/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.transport.netty.ssl;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Server;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.BootStrapFactory;
import com.cisco.dhruva.transport.netty.hanlder.TLSChannelHandler;
import com.cisco.dhruva.transport.netty.ssl.NettySSLContextFactory.SSLContextType;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

public class NettyTLSServer implements Server {

  private final ChannelInitializer channelInitializer;
  private AbstractBootstrap tlsBootstrap;
  private MessageForwarder messageForwarder;
  private DsNetwork networkConfig;
  private TLSChannelHandler tlsChannelHander;

  public NettyTLSServer(
      MessageForwarder messageForwarder,
      DsNetwork networkConfig,
      ExecutorService executorService,
      MetricService metricService)
      throws Exception {
    this.networkConfig = networkConfig;
    tlsChannelHander =
        new TLSChannelHandler(messageForwarder, networkConfig, executorService, metricService);
    tlsChannelHander.setServerMode(true);
    tlsChannelHander.messageForwarder(messageForwarder);
    channelInitializer =
        new TLSChannelInitializer(networkConfig, tlsChannelHander, SSLContextType.SERVER);
    this.tlsBootstrap =
        BootStrapFactory.getInstance()
            .getServerBootStrap(Transport.TLS, networkConfig, channelInitializer);
  }

  @Override
  public void startListening(InetAddress address, int port, CompletableFuture serverStartFuture) {
    ChannelFuture channelFuture = tlsBootstrap.bind(address, port);
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
