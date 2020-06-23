/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.netty.NettyUDPServer;

public class ServerFactory {

  private Server udpServer;
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
      Transport transport,
      MessageForwarder messageForwarder,
      DsNetwork networkConfig,
      ExecutorService executorService,
      MetricService metricService)
      throws Exception {
    Server server = null;
    switch (transport) {
      case UDP:
        if (udpServer == null) {
          synchronized (lock) {
            if (udpServer == null) {
              udpServer =
                  new NettyUDPServer(
                      messageForwarder, networkConfig, executorService, metricService);
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
}
