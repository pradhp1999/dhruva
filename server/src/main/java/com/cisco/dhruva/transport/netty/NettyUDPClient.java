/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.hanlder.UDPChannelHandler;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

public class NettyUDPClient extends AbstractClient {

  private Logger logger = DhruvaLoggerFactory.getLogger(NettyUDPClient.class);

  public NettyUDPClient(
      DsNetwork networkConfig,
      MessageForwarder messageForwarder,
      ExecutorService executorService,
      MetricService metricService)
      throws Exception {
    super(networkConfig, messageForwarder, executorService, metricService);
    channelHandler =
        new UDPChannelHandler(messageForwarder, networkConfig, executorService, metricService);
    channelHandler.setServerMode(false);
    channelInitializer = new BaseChannelInitializer(networkConfig, channelHandler);
    createBootStrap();
  }

  @Override
  public Transport getTransport() {
    return Transport.UDP;
  }
}
