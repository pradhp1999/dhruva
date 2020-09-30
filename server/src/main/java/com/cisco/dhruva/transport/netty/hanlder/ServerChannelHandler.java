package com.cisco.dhruva.transport.netty.hanlder;

import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

public class ServerChannelHandler implements ChannelHandler {

  Logger logger = DhruvaLoggerFactory.getLogger(ServerChannelHandler.class);

  @Override
  public void handlerAdded(ChannelHandlerContext channelHandlerContext) throws Exception {}

  @Override
  public void handlerRemoved(ChannelHandlerContext channelHandlerContext) throws Exception {}

  @Override
  public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable)
      throws Exception {
    logger.error("Exception in ServerChannel " + channelHandlerContext.channel(), throwable);
  }
}
