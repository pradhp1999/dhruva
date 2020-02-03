/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.hanlder.UDPChannelHandler;
import io.netty.channel.ChannelHandler;

public class ChannelHandlerFactory {

  private static ChannelHandler udpChannelHander = new UDPChannelHandler();

  public static ChannelHandler getInstance(Transport transport, MessageForwarder messageForwarder) {
    ChannelHandler channelHandler = null;
    switch (transport) {
      case UDP:
        channelHandler = udpChannelHander;
        ((UDPChannelHandler) channelHandler).messageForwarder(messageForwarder);
        break;
    }
    return channelHandler;
  }
}