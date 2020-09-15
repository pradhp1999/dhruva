/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class BootStrapFactory {

  private static final BootStrapFactory bootStrapFactory = new BootStrapFactory();
  private Bootstrap udpClientBootstrap;
  private AbstractBootstrap udpServerBootstrap;
  private AbstractBootstrap tlsServerBootstrap;

  private final Object lock = new Object();

  public static BootStrapFactory getInstance() {
    return bootStrapFactory;
  }

  public Bootstrap getClientBootStrap(
      Transport transport, DsNetwork networkConfig, BaseChannelInitializer baseChannelInitializer) {
    switch (transport) {
      case UDP:
        synchronized (lock) {
          if (udpClientBootstrap == null) {
            udpClientBootstrap =
                new Bootstrap()
                    .channel(NioDatagramChannel.class)
                    .handler(baseChannelInitializer)
                    .group(EventLoopGroupFactory.getInstance(Transport.UDP, networkConfig));
          }
        }

        return udpClientBootstrap;
    }

    return null;
  }

  public AbstractBootstrap getServerBootStrap(
      Transport transport, DsNetwork networkConfig, ChannelInitializer baseChannelInitializer) {

    switch (transport) {
      case UDP:
        synchronized (lock) {
          if (udpServerBootstrap == null) {
            udpServerBootstrap =
                new Bootstrap()
                    .channel(NioDatagramChannel.class)
                    .handler(baseChannelInitializer)
                    .group(EventLoopGroupFactory.getInstance(Transport.UDP, networkConfig));
          }
        }
        return udpServerBootstrap;

      case TLS:
        synchronized (lock) {
          if (tlsServerBootstrap == null) {
            tlsServerBootstrap =
                new ServerBootstrap()
                    .channel(NioServerSocketChannel.class)
                    .childHandler(baseChannelInitializer)
                    .group(EventLoopGroupFactory.getInstance(Transport.TLS, networkConfig));
          }
        }

        return tlsServerBootstrap;
    }

    return null;
  }

  /*
  only used for Testing
   */
  public void setUdpBootstrap(Bootstrap udpBootstrap) {
    this.udpClientBootstrap = udpBootstrap;
    this.udpServerBootstrap = udpBootstrap;
  }
}
