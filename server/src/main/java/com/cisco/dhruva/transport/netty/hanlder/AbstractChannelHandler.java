/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty.hanlder;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.transport.ChannelEventsListener;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractChannelHandler implements ChannelInboundHandler {

  private Set<ChannelEventsListener> channelListeners = new ConcurrentHashMap().newKeySet();
  protected ExecutorService executorService;
  protected java.util.concurrent.ExecutorService eventNotificationExecutor;
  private Logger logger = DhruvaLoggerFactory.getLogger(AbstractChannelHandler.class);
  protected MessageForwarder messageForwarder;

  AbstractChannelHandler(MessageForwarder messageForwarder, ExecutorService executorService) {
    this.messageForwarder = messageForwarder;
    this.executorService = executorService;
    executorService.startExecutorService(ExecutorType.NETTY_EVENT_NOTIFICATION_HANDLER, 2);
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
    logger.error("Exception in the UDPChannelHandler for Channel " + ctx.channel(), cause);
    notifyChannelEventsToListeners(cause);
  }
}
