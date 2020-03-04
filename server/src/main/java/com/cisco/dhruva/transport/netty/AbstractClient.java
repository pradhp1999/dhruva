/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Client;
import com.cisco.dhruva.transport.netty.hanlder.AbstractChannelHandler;
import io.netty.bootstrap.Bootstrap;

public abstract class AbstractClient implements Client {

  protected BaseChannelInitializer channelInitializer;
  protected Bootstrap bootstrap;
  protected DsNetwork networkConfig;
  protected AbstractChannelHandler channelHandler;
}
