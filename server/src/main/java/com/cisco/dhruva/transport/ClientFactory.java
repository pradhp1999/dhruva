/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.netty.NettyUDPClient;

public class ClientFactory {

  private static ClientFactory clientFactory = new ClientFactory();
  private Client udpClient;
  private Object lock = new Object();

  public static ClientFactory newInstance() {
    clientFactory = new ClientFactory();
    return clientFactory;
  }

  public static ClientFactory getInstance() {
    return clientFactory;
  }

  public Client getClient(
      Transport transport,
      DsNetwork networkConfig,
      MessageForwarder messageForwarder,
      ExecutorService executorService,
      MetricService metricService)
      throws Exception {
    Client client = null;
    switch (transport) {
      case UDP:
        if (udpClient == null) {
          synchronized (lock) {
            if (udpClient == null) {
              udpClient =
                  new NettyUDPClient(
                      networkConfig, messageForwarder, executorService, metricService);
            }
          }
        }
        client = udpClient;
        break;
      default:
        throw new Exception("Transport " + transport.name() + " not supported");
    }
    return client;
  }
}
