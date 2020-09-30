/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty.hanlder;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;

public class UDPChannelHandler extends AbstractChannelHandler {

  private Logger logger = DhruvaLoggerFactory.getLogger(UDPChannelHandler.class);

  public UDPChannelHandler(
      MessageForwarder messageForwarder,
      DsNetwork network,
      ExecutorService executorService,
      MetricService metricService) {
    super(messageForwarder, network, executorService, metricService);
  }

  @Override
  protected Transport getTransport() {
    return Transport.UDP;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof DatagramPacket) {
      DatagramPacket receivedPacket = (DatagramPacket) msg;
      ByteBuf receivedMessage = receivedPacket.content();
      byte[] messageBytes = new byte[receivedMessage.readableBytes()];
      receivedMessage.readBytes(messageBytes);

      DsBindingInfo bindingInfo = getDsBindingInfo(getLocalAddress(ctx), receivedPacket.sender());

      String logString;
      if (messageBytes.length > 0 && messageBytes[0] == 0) {
        logString = DsByteString.toStunDebugString(messageBytes);
      } else {
        logString = new String(messageBytes);
      }
      logger.logWithContext(
          "UDP packet received from "
              + bindingInfo
              + ", Message is "
              + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(logString),
          buildConnectionInfoMap(serverMode, getLocalAddress(ctx), receivedPacket.sender()));
      messageForwarder.processMessage(messageBytes, bindingInfo);
    } else {
      String errorMessage =
          "Invalid message type in ChannelRead, This should never happen, expecting DatagramPacket but received "
              + msg;
      logger.error(errorMessage, new Exception(errorMessage));
    }

    channelReadCleanup();
    ReferenceCountUtil.release(msg);
  }
}
