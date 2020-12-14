/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.Transport;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.ConcurrentHashMap;

public class BootStrapFactory {

  private static final BootStrapFactory bootStrapFactory = new BootStrapFactory();

  private static final String NETWORK_CLIENT_BOOTSTRAP = "client";
  private static final String NETWORK_SERVER_BOOTSTRAP = "server";

  private ConcurrentHashMap<DsNetwork, ConcurrentHashMap<String, AbstractBootstrap>>
      bootStrapNetworkMap = new ConcurrentHashMap<>();

  public static BootStrapFactory getInstance() {
    return bootStrapFactory;
  }

  public Bootstrap getClientBootStrap(
      Transport transport, DsNetwork networkConfig, ChannelInitializer channelInitializer)
      throws Exception {
    ConcurrentHashMap<String, AbstractBootstrap> networkClientMap;
    switch (transport) {
      case UDP:
        networkClientMap =
            bootStrapNetworkMap.computeIfAbsent(networkConfig, k -> new ConcurrentHashMap<>());

        return (Bootstrap)
            networkClientMap.computeIfAbsent(
                NETWORK_CLIENT_BOOTSTRAP,
                k ->
                    new Bootstrap()
                        .channel(NioDatagramChannel.class)
                        .handler(channelInitializer)
                        .group(EventLoopGroupFactory.getInstance(Transport.UDP, networkConfig)));

      case TLS:
        networkClientMap =
            bootStrapNetworkMap.computeIfAbsent(networkConfig, k -> new ConcurrentHashMap<>());

        return (Bootstrap)
            networkClientMap.computeIfAbsent(
                NETWORK_CLIENT_BOOTSTRAP,
                k ->
                    new Bootstrap()
                        .channel(NioSocketChannel.class)
                        .handler(channelInitializer)
                        .group(EventLoopGroupFactory.getInstance(Transport.TLS, networkConfig)));

      default:
        throw new Exception("Transport " + transport + " not supported in getClientBootStrap");
    }
  }

  public AbstractBootstrap getServerBootStrap(
      Transport transport, DsNetwork networkConfig, ChannelInitializer channelInitializer) {
    ConcurrentHashMap<String, AbstractBootstrap> networkServerMap;
    switch (transport) {
      case UDP:
        networkServerMap =
            bootStrapNetworkMap.computeIfAbsent(networkConfig, k -> new ConcurrentHashMap<>());
        return networkServerMap.computeIfAbsent(
            NETWORK_SERVER_BOOTSTRAP,
            k ->
                new Bootstrap()
                    .channel(NioDatagramChannel.class)
                    .handler(channelInitializer)
                    .group(EventLoopGroupFactory.getInstance(Transport.UDP, networkConfig)));

      case TLS:
        networkServerMap =
            bootStrapNetworkMap.computeIfAbsent(networkConfig, k -> new ConcurrentHashMap<>());
        return networkServerMap.computeIfAbsent(
            NETWORK_SERVER_BOOTSTRAP,
            k ->
                new ServerBootstrap()
                    .channel(NioServerSocketChannel.class)
                    .childHandler(channelInitializer)
                    .group(EventLoopGroupFactory.getInstance(Transport.TLS, networkConfig)));
    }

    return null;
  }

  /*
  only used for Testing
   */
  public void setUdpBootstrap(Bootstrap udpBootstrap, DsNetwork networkConfig) {
    ConcurrentHashMap<String, AbstractBootstrap> networkMap;
    networkMap = new ConcurrentHashMap<>();
    networkMap.putIfAbsent(NETWORK_CLIENT_BOOTSTRAP, udpBootstrap);
    networkMap.putIfAbsent(NETWORK_SERVER_BOOTSTRAP, udpBootstrap);
    this.bootStrapNetworkMap.putIfAbsent(networkConfig, networkMap);
  }
}
