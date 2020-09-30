/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty.hanlder;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.channel.ChannelHandlerContext;

public class StreamChannelHandler extends AbstractChannelHandler {

  private Logger logger = DhruvaLoggerFactory.getLogger(StreamChannelHandler.class);

  Transport transport;

  public StreamChannelHandler(
      MessageForwarder messageForwarder,
      DsNetwork network,
      Transport transport,
      ExecutorService executorService,
      MetricService metricService) {
    super(messageForwarder, network, executorService, metricService);
    this.transport = transport;
  }

  @Override
  protected Transport getTransport() {
    return Transport.TLS;
  }

  @Override
  public void channelRead(ChannelHandlerContext channelHandlerContext, Object receivedObject) {

    if (receivedObject != null && receivedObject.getClass().isArray()) {
      DsBindingInfo bindingInfo =
          getDsBindingInfo(
              getLocalAddress(channelHandlerContext), getRemoteAddress(channelHandlerContext));
      byte[] messageBytes = (byte[]) receivedObject;

      logger.logWithContext(
          getTransport().name()
              + " packet received from "
              + bindingInfo
              + ", Message is "
              + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(new String(messageBytes)),
          buildConnectionInfoMap(channelHandlerContext, serverMode));
      messageForwarder.processMessage(messageBytes, bindingInfo);
    }
    channelReadCleanup();
  }
}
