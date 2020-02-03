/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class BaseChannelInitializer extends ChannelInitializer {

  ChannelHandler messageHandler;

  public BaseChannelInitializer messageHandler(ChannelHandler messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  public ChannelHandler getMessageHandler() {
    return messageHandler;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(messageHandler);
  }
}
