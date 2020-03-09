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

  ChannelHandler channelHandler;

  public BaseChannelInitializer channelHanlder(ChannelHandler channelHandler) {
    this.channelHandler = channelHandler;
    return this;
  }

  public ChannelHandler getChannelHandler() {
    return channelHandler;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(channelHandler);
  }
}
