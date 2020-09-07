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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.net.InetSocketAddress;

public class TLSChannelHandler extends AbstractChannelHandler {

  private Logger logger = DhruvaLoggerFactory.getLogger(TLSChannelHandler.class);

  public TLSChannelHandler(
      MessageForwarder messageForwarder,
      DsNetwork network,
      ExecutorService executorService,
      MetricService metricService) {
    super(messageForwarder, network, executorService, metricService);
  }

  @Override
  protected Transport getTransport() {
    return Transport.TLS;
  }

  @Override
  public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {}

  @Override
  public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {}

  @Override
  public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {}

  @Override
  public void channelRead(ChannelHandlerContext channelHandlerContext, Object receivedObject)
      throws Exception {

    if (receivedObject != null && receivedObject.getClass().isArray()) {
      Channel channel = channelHandlerContext.channel();
      InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
      InetSocketAddress remoteAddress = (InetSocketAddress) channel.remoteAddress();
      DsBindingInfo bindingInfo =
          new DsBindingInfo(
              localAddress.getAddress(),
              localAddress.getPort(),
              remoteAddress.getAddress(),
              remoteAddress.getPort(),
              Transport.UDP);
      bindingInfo.setNetwork(network);
      byte[] messageBytes = (byte[]) receivedObject;

      logger.info(
          "Tls packet received from {} , Message is {} ",
          () -> bindingInfo,
          () -> DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(new String(messageBytes)));
      messageForwarder.processMessage(messageBytes, bindingInfo);
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext channelHandlerContext) throws Exception {}

  @Override
  public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object o)
      throws Exception {}

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext channelHandlerContext)
      throws Exception {}

  @Override
  public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {}

  @Override
  public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {}
}
