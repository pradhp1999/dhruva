/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.netty.NettyUDPServer;
import com.cisco.dhruva.transport.netty.ssl.NettyTLSServer;

public class ServerFactory {

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
      MetricService metricService,
      ChannelEventsListener connectionCacheEventHandler)
      throws Exception {
    switch (transport) {
      case UDP:
        Server udpServer =
            new NettyUDPServer(messageForwarder, networkConfig, executorService, metricService);
        udpServer.addConnectionEventHandler(connectionCacheEventHandler);
        return udpServer;

      case TLS:
        Server tlsServer =
            new NettyTLSServer(messageForwarder, networkConfig, executorService, metricService);
        tlsServer.addConnectionEventHandler(connectionCacheEventHandler);
        return tlsServer;

      default:
        throw new Exception("Transport " + transport.name() + " not supported");
    }
  }
}
