/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty.hanlder;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import java.net.InetSocketAddress;

public class UDPChannelHandler extends AbstractChannelHandler {

  private Logger logger = DhruvaLoggerFactory.getLogger(UDPChannelHandler.class);

  public UDPChannelHandler(MessageForwarder messageForwarder, ExecutorService executorService) {
    super(messageForwarder, executorService);
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    logger.info("Channel Registered ", ctx.channel());
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    logger.info("Channel Unregistered ", ctx.channel());
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    logger.info("Channel Active ", ctx.channel());
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    logger.info("Channel InActive ", ctx.channel());
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof DatagramPacket) {
      DatagramPacket receivedPacket = (DatagramPacket) msg;
      ByteBuf receivedMessage = receivedPacket.content();
      byte[] messageBytes = new byte[receivedMessage.readableBytes()];
      ((ByteBuf) receivedMessage).readBytes(messageBytes);

      Channel channel = ctx.channel();
      InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
      InetSocketAddress remoteAddress = receivedPacket.sender();
      DsBindingInfo bindingInfo =
          new DsBindingInfo(
              localAddress.getAddress(),
              localAddress.getPort(),
              remoteAddress.getAddress(),
              remoteAddress.getPort(),
              Transport.UDP);
      String logString;
      if (messageBytes.length > 0 && messageBytes[0] == 0) {
        logString = DsByteString.toStunDebugString(messageBytes);
      } else {
        logString = new String(messageBytes);
      }
      logger.info(
          "UDP packet received from {} , Message is {} ",
          bindingInfo,
          DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(logString));
      messageForwarder.processMessage(messageBytes, bindingInfo);
    } else {
      String errorMessage =
          "Invalid message type in ChannelRead expecting DatagramPacket but received " + msg;
      logger.error(errorMessage, new Exception(errorMessage));
    }

    ReferenceCountUtil.release(msg);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    logger.info("Channel Read Complete ", ctx.channel());
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    logger.info("User event triggerred for channel ", ctx.channel());
  }

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    logger.error(
        "Channel Writability changed for channel {} writablity is {} ",
        ctx.channel(),
        ctx.channel().isWritable());
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    logger.info("Handler added to channel ", ctx.channel());
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    logger.info("Handler removed from channel ", ctx.channel());
  }
}
