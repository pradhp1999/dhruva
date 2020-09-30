package com.cisco.dhruva.transport.netty.ssl;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.AbstractClient;
import com.cisco.dhruva.transport.netty.hanlder.StreamChannelHandler;
import com.cisco.dhruva.transport.netty.ssl.TLSChannelInitializer.ChannelType;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;

public class NettyTLSClient extends AbstractClient {

  private Logger logger = DhruvaLoggerFactory.getLogger(NettyTLSClient.class);

  public NettyTLSClient(
      DsNetwork networkConfig,
      MessageForwarder messageForwarder,
      ExecutorService executorService,
      MetricService metricService)
      throws Exception {
    super(networkConfig, messageForwarder, executorService, metricService);

    channelHandler =
        new StreamChannelHandler(
            messageForwarder, networkConfig, Transport.TLS, executorService, metricService);
    channelHandler.setServerMode(false);
    channelInitializer =
        new TLSChannelInitializer(networkConfig, channelHandler, ChannelType.CLIENT);
    createBootStrap();
  }

  @Override
  public Transport getTransport() {
    return Transport.TLS;
  }
}
