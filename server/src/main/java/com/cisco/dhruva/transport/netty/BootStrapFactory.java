/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.transport.Transport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class BootStrapFactory {

  private static BootStrapFactory bootStrapFactory = new BootStrapFactory();
  private Bootstrap udpBootstrap;
  private Object lock = new Object();

  public static BootStrapFactory getInstance() {
    return bootStrapFactory;
  }

  public Bootstrap getClientBootStrap(
      Transport transport,
      NetworkConfig networkConfig,
      BaseChannelInitializer baseChannelInitializer) {
    switch (transport) {
      case UDP:
        synchronized (lock) {
          if (udpBootstrap == null) {
            udpBootstrap =
                new Bootstrap()
                    .channel(NioDatagramChannel.class)
                    .handler(baseChannelInitializer)
                    .group(EventLoopGroupFactory.getInstance(Transport.UDP, networkConfig));
          }
        }

        return udpBootstrap;
    }

    return null;
  }

  public Bootstrap getServerBootStrap(
      Transport transport,
      NetworkConfig networkConfig,
      BaseChannelInitializer baseChannelInitializer) {

    switch (transport) {
      case UDP:
        synchronized (lock) {
          if (udpBootstrap == null) {
            udpBootstrap =
                new Bootstrap()
                    .channel(NioDatagramChannel.class)
                    .handler(baseChannelInitializer)
                    .group(EventLoopGroupFactory.getInstance(Transport.UDP, networkConfig));
          }
        }

        return udpBootstrap;
    }

    return null;
  }

  public void setUdpBootstrap(Bootstrap udpBootstrap) {
    this.udpBootstrap = udpBootstrap;
  }
}
