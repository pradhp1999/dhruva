/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty.hanlder;

import static com.cisco.dhruva.transport.Transport.TLS;
import static com.cisco.dhruva.transport.Transport.UDP;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import com.cisco.dhruva.service.MetricService;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.ChannelEventsListener;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.Connection.STATE;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.LogContext;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.event.Event;
import com.cisco.dhruva.util.log.event.Event.DIRECTION;
import com.cisco.dhruva.util.log.event.Event.ErrorType;
import com.cisco.dhruva.util.log.event.Event.EventSubType;
import com.cisco.dhruva.util.log.event.Event.EventType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractChannelHandler implements ChannelInboundHandler {

  private final MetricService metricService;
  private Set<ChannelEventsListener> channelListeners = new ConcurrentHashMap().newKeySet();
  protected ExecutorService executorService;
  protected java.util.concurrent.ExecutorService eventNotificationExecutor;
  private static Logger logger = DhruvaLoggerFactory.getLogger(AbstractChannelHandler.class);
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

  private void notifyEventTask(Consumer<ChannelEventsListener> consumer) {
    eventNotificationExecutor.submit(
        () -> {
          for (ChannelEventsListener listener : channelListeners) {
            consumer.accept(listener);
          }
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
    HashMap<String, String> connectionInfo = buildConnectionInfoMap(ctx, serverMode);
    connectionInfo.put("exception", cause.getClass().toString());
    connectionInfo.put("exceptionMessage", cause.getMessage());

    logger.emitEvent(
        EventType.CONNECTION,
        getEventSubTypeFromTransport(),
        ErrorType.ConnectionError,
        "Exception " + cause.getMessage() + " in Channel handler , closing the connection",
        connectionInfo,
        cause);

    notifyEventTask(channelEventsListener -> channelEventsListener.onException(cause));
    // close the connection
    ctx.close();
  }

  private EventSubType getEventSubTypeFromTransport() {
    return getTransport() == UDP
        ? EventSubType.UDPCONNECTION
        : getTransport() == TLS ? EventSubType.TLSCONNECTION : EventSubType.UDPCONNECTION;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
    InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();

    notifyConnectionActiveEvent(ctx, localAddress, remoteAddress);

    if (localAddress != null && remoteAddress != null) {
      emitEvent(ctx, null, "Channel active");
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

  @Override
  public void userEventTriggered(ChannelHandlerContext channelHandlerContext, Object event)
      throws Exception {

    HashMap<String, String> channelInfo =
        buildConnectionInfoMap(channelHandlerContext, this.serverMode);
    logger.logWithContext("Event " + event + " on Channel ", channelInfo);
    if (event instanceof SslHandshakeCompletionEvent) {
      SslHandshakeCompletionEvent sslHandshakeCompletionEvent = (SslHandshakeCompletionEvent) event;

      if (sslHandshakeCompletionEvent.isSuccess()) {
        logger.emitEvent(
            EventType.CONNECTION, EventSubType.TLSCONNECTION, "SSLHandshakeSuccess", channelInfo);
      } else {
        logger.emitEvent(
            EventType.CONNECTION,
            EventSubType.TLSCONNECTION,
            ErrorType.SslHandShakeFailed,
            sslHandshakeCompletionEvent.cause().getMessage(),
            channelInfo,
            sslHandshakeCompletionEvent.cause());
      }
    }
  }

  @Override
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    logger.error(
        "Channel Writability changed for channel {} writablity is {} ",
        ctx.channel(),
        ctx.channel().isWritable());
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    logger.info("Handler added to channel {}", ctx.channel());
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    logger.info("Handler removed from channel {}", ctx.channel());
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    logger.info("Channel Read Complete {}", ctx.channel());
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) {
    logger.info("Channel Registered {} ", ctx.channel());
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    logger.info("Channel Unregistered {}", ctx.channel());
  }

  private void notifyConnectionActiveEvent(
      ChannelHandlerContext ctx, InetSocketAddress localAddress, InetSocketAddress remoteAddress)
      throws Exception {
    Connection connection;
    if (remoteAddress != null) {
      connection = getTransport().getConnection(ctx.channel(), network, this);
      notifyEventTask(
          channelEventsListener ->
              channelEventsListener.connectionActive(
                  localAddress, remoteAddress, getTransport(), connection));
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
    InetSocketAddress localAddress =
        (InetSocketAddress) channelHandlerContext.channel().localAddress();
    InetSocketAddress remoteAddress =
        (InetSocketAddress) channelHandlerContext.channel().remoteAddress();

    notifyEventTask(
        channelEventsListener ->
            channelEventsListener.connectionInActive(localAddress, remoteAddress, getTransport()));

    if (localAddress != null && remoteAddress != null) {
      emitEvent(channelHandlerContext, ErrorType.ConnectionInActive, "Channel inactive");
      metricService.sendConnectionMetric(
          localAddress.getAddress().getHostAddress(),
          localAddress.getPort(),
          remoteAddress.getAddress().getHostAddress(),
          remoteAddress.getPort(),
          getTransport(),
          serverMode ? DIRECTION.IN : DIRECTION.OUT,
          STATE.DISCONNECTED);
    }
  }

  protected abstract Transport getTransport();

  private void emitEvent(ChannelHandlerContext ctx, ErrorType errorType, String message) {
    logger.emitEvent(
        getEventSubTypeFromTransport().getEventType(),
        getEventSubTypeFromTransport(),
        errorType,
        message,
        buildConnectionInfoMap(ctx, this.serverMode),
        null);
  }

  public static HashMap<String, String> buildConnectionInfoMap(
      ChannelHandlerContext ctx, boolean serverMode) {
    if (ctx != null) {
      InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
      InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
      return buildConnectionInfoMap(serverMode, localAddress, remoteAddress);
    }
    return Maps.newHashMap();
  }

  @NotNull
  static HashMap<String, String> buildConnectionInfoMap(
      boolean serverMode, InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
    HashMap<String, String> connectionMap =
        Maps.newHashMap(
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

    connectionMap.put(
        LogContext.CONNECTION_SIGNATURE,
        Connection.getConnectionSignature.apply(localAddress, remoteAddress));

    addConnectionSignatureToMDC(localAddress, remoteAddress);
    return connectionMap;
  }

  private static void addConnectionSignatureToMDC(
      InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
    logger.setMDC(
        LogContext.CONNECTION_SIGNATURE,
        Connection.getConnectionSignature.apply(localAddress, remoteAddress));
  }

  public DsBindingInfo getDsBindingInfo(
      InetSocketAddress localAddress, InetSocketAddress remoteAddress) {
    DsBindingInfo bindingInfo =
        new DsBindingInfo(
            localAddress.getAddress(),
            localAddress.getPort(),
            remoteAddress.getAddress(),
            remoteAddress.getPort(),
            getTransport());
    bindingInfo.setNetwork(network);
    return bindingInfo;
  }

  protected void channelReadCleanup() {
    logger.clearMDC();
  }

  InetSocketAddress getRemoteAddress(ChannelHandlerContext ctx) {
    return (InetSocketAddress) ctx.channel().remoteAddress();
  }

  InetSocketAddress getLocalAddress(ChannelHandlerContext ctx) {
    return (InetSocketAddress) ctx.channel().localAddress();
  }

  public void setServerMode(boolean serverMode) {
    this.serverMode = serverMode;
  }
}
