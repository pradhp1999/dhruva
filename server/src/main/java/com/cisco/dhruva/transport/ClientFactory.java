/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.netty.NettyUDPClient;
import com.cisco.dhruva.transport.netty.ssl.NettyTLSClient;

public class ClientFactory {

  private static ClientFactory clientFactory = new ClientFactory();
  private Client udpClient;
  private final Object lock = new Object();
  private Client tlsClient;

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
    Client client;
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
      case TLS:
        if (tlsClient == null) {
          synchronized (lock) {
            if (tlsClient == null) {
              tlsClient =
                  new NettyTLSClient(
                      networkConfig, messageForwarder, executorService, metricService);
            }
          }
        }
        client = tlsClient;
        break;
      default:
        throw new Exception("Transport " + transport.name() + " not supported as Client");
    }
    return client;
  }
}
