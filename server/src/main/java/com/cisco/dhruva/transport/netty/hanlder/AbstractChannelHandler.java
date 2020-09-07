/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty.hanlder;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.ChannelEventsListener;
import com.cisco.dhruva.transport.Connection.STATE;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.event.Event;
import com.cisco.dhruva.util.log.event.Event.DIRECTION;
import com.cisco.dhruva.util.log.event.Event.EventSubType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractChannelHandler implements ChannelInboundHandler {

  private final MetricService metricService;
  private Set<ChannelEventsListener> channelListeners = new ConcurrentHashMap().newKeySet();
  protected ExecutorService executorService;
  protected java.util.concurrent.ExecutorService eventNotificationExecutor;
  private Logger logger = DhruvaLoggerFactory.getLogger(AbstractChannelHandler.class);
  protected MessageForwarder messageForwarder;
  protected DsNetwork network;
  protected boolean serverMode;

  AbstractChannelHandler(
      MessageForwarder messageForwarder,
      DsNetwork network,
      ExecutorService executorService,
      MetricService metricService) {
    this.messageForwarder = messageForwarder;
    this.executorService = executorService;
    this.network = network;
    this.metricService = metricService;
    if (!executorService.isExecutorServiceRunning(ExecutorType.NETTY_EVENT_NOTIFICATION_HANDLER)) {
      executorService.startExecutorService(ExecutorType.NETTY_EVENT_NOTIFICATION_HANDLER, 2);
    }
    eventNotificationExecutor =
        executorService.getExecutorThreadPool(ExecutorType.NETTY_EVENT_NOTIFICATION_HANDLER);
  }

  public void subscribeForChannelEvents(ChannelEventsListener listener) {
    channelListeners.add(listener);
  }

  public void unsubscribeChannelEvents(ChannelEventsListener listener) {
    channelListeners.remove(listener);
  }

  private void notifyChannelEventsToListeners(Throwable cause) {
    eventNotificationExecutor.submit(
        () -> {
          channelListeners.forEach(
              listener -> {
                listener.onException(cause);
              });
        });
  }

  public AbstractChannelHandler messageForwarder(MessageForwarder messageForwarder) {
    this.messageForwarder = messageForwarder;
    return this;
  }

  public MessageForwarder messageForwarder() {
    return this.messageForwarder;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.error("Exception in the Channelhandler for Channel " + ctx.channel(), cause);
    notifyChannelEventsToListeners(cause);
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
    InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
    if (localAddress != null && remoteAddress != null) {
      emitEvent(localAddress, remoteAddress);
      metricService.sendConnectionMetric(
          localAddress.getAddress().getHostAddress(),
          localAddress.getPort(),
          remoteAddress.getAddress().getHostAddress(),
          remoteAddress.getPort(),
          getTransport(),
          serverMode ? DIRECTION.IN : DIRECTION.OUT,
          STATE.CONNECTED);
    }
  }

  protected abstract Transport getTransport();

  private void emitEvent(InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
    logger.emitEvent(
        EventSubType.UDPCONNECTION.getEventType(),
        EventSubType.UDPCONNECTION,
        "Connection active",
        buildConnectionInfoMap(localAddress, remoteAddress, this.serverMode));
  }

  @NotNull
  public static HashMap<String, String> buildConnectionInfoMap(
      InetSocketAddress localAddress, InetSocketAddress remoteAddress, boolean serverMode) {
    return Maps.newHashMap(
        ImmutableMap.of(
            Event.LOCALIP,
            localAddress.getAddress().getHostAddress(),
            Event.LOCALPORT,
            String.valueOf(localAddress.getPort()),
            Event.REMOTEIP,
            remoteAddress.getAddress().getHostAddress(),
            Event.REMOTEPORT,
            String.valueOf((remoteAddress.getPort())),
            Event.DIRECTION,
            serverMode ? "IN" : "OUT"));
  }

  public void setServerMode(boolean serverMode) {
    this.serverMode = serverMode;
  }
}
