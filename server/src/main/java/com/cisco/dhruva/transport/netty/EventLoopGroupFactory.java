/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.config.DhruvaThreadNames;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.util.concurrent.ThreadFactory;

public class EventLoopGroupFactory {

  private static NioEventLoopGroup udpEventLoopGroup;

  private static NioEventLoopGroup tlsEventLoopGroup;

  private static Object lock = new Object();

  public static EventLoopGroup getInstance(Transport transport, DsNetwork networkConfig) {

    EventLoopGroup eventLoopGroup = null;
    switch (transport) {
      case UDP:
        if (udpEventLoopGroup == null) {
          synchronized (lock) {
            if (udpEventLoopGroup == null) {
              ThreadFactory threadFactory =
                  new DefaultThreadFactory(DhruvaThreadNames.getUdpEventloopThreadName());
              eventLoopGroup =
                  new NioEventLoopGroup(networkConfig.udpEventPoolThreadCount(), threadFactory);
              udpEventLoopGroup = (NioEventLoopGroup) eventLoopGroup;
            }
          }
        }
        break;

      case TLS:
        if (tlsEventLoopGroup == null) {
          synchronized (lock) {
            if (tlsEventLoopGroup == null) {
              ThreadFactory threadFactory =
                  new DefaultThreadFactory(DhruvaThreadNames.getTlsEventloopThreadName());
              eventLoopGroup =
                  new NioEventLoopGroup(networkConfig.tlsEventPoolThreadCount(), threadFactory);
              tlsEventLoopGroup = (NioEventLoopGroup) eventLoopGroup;
            }
          }
        }
        break;
    }

    return eventLoopGroup;
  }
}
