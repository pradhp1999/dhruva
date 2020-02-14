/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.transport.netty.NettyUDPServer;
import io.netty.bootstrap.Bootstrap;

public class ServerFactory {

  private Server udpServer;
  private Bootstrap udpBootstrap = new Bootstrap();
  private Object lock = new Object();
  private static ServerFactory serverFactory = new ServerFactory();

  public static ServerFactory newInstance() {
    serverFactory = new ServerFactory();
    return serverFactory;
  }

  public static ServerFactory getInstance() {
    return serverFactory;
  }

  public Server getServer(
      Transport transport, MessageForwarder messageForwarder, NetworkConfig networkConfig)
      throws Exception {
    Server server = null;
    switch (transport) {
      case UDP:
        if (udpServer == null) {
          synchronized (lock) {
            if (udpServer == null) {
              udpServer = new NettyUDPServer(messageForwarder, networkConfig, udpBootstrap);
            }
          }
        }
        server = udpServer;
        break;
      default:
        throw new Exception("Transport " + transport.name() + " not supported");
    }
    return server;
  }

  public void setUDPBootstrap(Bootstrap bootstrap) {
    this.udpBootstrap = bootstrap;
  }
}
