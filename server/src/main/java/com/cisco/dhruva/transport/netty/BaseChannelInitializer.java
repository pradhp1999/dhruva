/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;

public class BaseChannelInitializer extends ChannelInitializer {

  ChannelHandler channelHandler;
  private final DsNetwork networkconfig;

  public BaseChannelInitializer(DsNetwork networkConfig, ChannelHandler channelHandler) {
    this.networkconfig = networkConfig;
    this.channelHandler = channelHandler;
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
