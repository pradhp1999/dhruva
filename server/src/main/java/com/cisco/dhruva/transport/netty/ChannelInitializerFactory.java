/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;

public class ChannelInitializerFactory {

  private static ChannelInitializerFactory channelInitializerFactory =
      new ChannelInitializerFactory();
  private static BaseChannelInitializer udpChannelInitializer;

  public static ChannelInitializerFactory getInstance() {
    return channelInitializerFactory;
  }

  public BaseChannelInitializer getChannelInitializer(
      Transport transportType, MessageForwarder messageForwarder) {

    BaseChannelInitializer channelInitializer = null;

    switch (transportType) {
      case UDP:
        if (udpChannelInitializer == null) {
          synchronized (this) {
            if (udpChannelInitializer == null) {
              udpChannelInitializer = new BaseChannelInitializer();
              udpChannelInitializer.messageHandler(
                  ChannelHandlerFactory.getInstance().getChannelHandler(Transport.UDP, messageForwarder));
            }
          }
        }
        channelInitializer = udpChannelInitializer;
        break;
    }
    return channelInitializer;
  }
}
