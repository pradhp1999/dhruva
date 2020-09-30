/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.ChannelEventsListener;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.netty.hanlder.AbstractChannelHandler;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import com.cisco.dhruva.util.log.event.Event;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractConnection implements Connection {

  Channel channel;
  private AtomicInteger referenceCount = new AtomicInteger(0);
  private DsNetwork networkConfig;
  protected long m_Timeout;
  private long m_TimeStamp;
  private DsBindingInfo connectionBindingInfo;
  private InetSocketAddress localSocketAddress;
  private InetSocketAddress remoteSocketAddress;
  protected AbstractChannelHandler channelHandler;
  Transport transport;
  private Logger logger = DhruvaLoggerFactory.getLogger(AbstractConnection.class);
  public final String ConnectionSignature = "connectionSignature";

  public AbstractConnection(
      Channel channel,
      DsNetwork networkConfig,
      Transport transport,
      AbstractChannelHandler channelHandler) {
    this.channel = channel;
    this.networkConfig = networkConfig;
    m_Timeout = networkConfig.connectionCacheConnectionIdleTimeout();
    this.localSocketAddress = (InetSocketAddress) channel.localAddress();
    this.remoteSocketAddress = (InetSocketAddress) channel.remoteAddress();
    this.transport = transport;
    this.channelHandler = channelHandler;
    connectionBindingInfo =
        new DsBindingInfo(
            localSocketAddress.getAddress(),
            localSocketAddress.getPort(),
            remoteSocketAddress.getAddress(),
            remoteSocketAddress.getPort(),
            this.transport);
  }

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination.
   *
   * @param buffer the message bytes to send across
   * @return Returns a CompletableFuture indicating the send state. CompletableFuture will complete
   *     exceptionally if the send fails.
   */
  @Override
  public CompletableFuture<Boolean> send(byte[] buffer) {
    ChannelFuture channelFuture = channel.writeAndFlush(buffer);

    return getCompletableFutureFromNettyFuture(channelFuture);
  }

  public void subscribeForChannelEvents(ChannelEventsListener listener) {
    channelHandler.subscribeForChannelEvents(listener);
  }

  public void unsubscribeChannelEvents(ChannelEventsListener listener) {
    channelHandler.unsubscribeChannelEvents(listener);
  }

  /** Increments the reference count for this connection. */
  @Override
  public void addReference() {
    referenceCount.incrementAndGet();
  }

  /**
   * Decrement the reference count, and if it falls to zero, update the timestamp on the connection
   */
  @Override
  public void removeReference() {

    referenceCount.decrementAndGet();
    updateTimeStamp();

    if (referenceCount.get() < 0) {
      logger.warn(
          "Connection {} removeReference: negative count! m_ReferenceCount == {} ",
          this,
          referenceCount);
    }
  }

  public int referenceCount() {
    return referenceCount.get();
  }

  /**
   * Update the timestamp of this connection. Should be done when message is sent or received from
   * this socket.
   */
  @Override
  public void updateTimeStamp() {
    m_TimeStamp = System.currentTimeMillis();
  }

  /**
   * Given the current time, returns whether this connection should be closed and removed.
   *
   * @return <code>true</code> if this connection should be closed and removed, otherwise returns
   *     <code>false</code>.
   */
  @Override
  public boolean shouldClose() {
    long current_time = System.currentTimeMillis();
    return referenceCount.get() == 0 && (m_Timeout < (current_time - m_TimeStamp) / 1000);
  }

  @Override
  public STATE getConnectionState() {
    STATE state;
    if (channel.isActive()) {
      state = STATE.ACTIVE;
    } else {
      state = STATE.INACTIVE;
    }
    return state;
  }

  @Override
  public DsBindingInfo getConnectionInfo() {
    return connectionBindingInfo;
  }

  @Override
  public void setConnectionState() {}

  /**
   * Method is called when the any error happens on the connection. Transport layer takes care of
   * closing the connection so no explicit close is necessary.
   *
   * @param cause
   */
  @Override
  public void onConnectionError(Throwable cause) {}

  @Override
  public ChannelFuture closeConnection() {
    return channel.close();
  }

  void logMessage(byte[] buffer) {
    logger.setMDC(
        ConnectionSignature, getConnectionSignature.apply(localSocketAddress, remoteSocketAddress));
    logger.info(
        "Sending Message on channel {} , message is {} ",
        channel,
        DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(new String(buffer)));
  }

  CompletableFuture getCompletableFutureFromNettyFuture(ChannelFuture channelFuture) {
    CompletableFuture writeFuture = new CompletableFuture();

    channelFuture.addListener(
        future -> {
          if (channelFuture.isSuccess()) {
            writeFuture.complete(true);
          } else {
            writeFuture.completeExceptionally(future.cause());
          }
        });
    return writeFuture;
  }

  @Override
  public HashMap<String, String> connectionInfoMap() {
    return Maps.newHashMap(
        ImmutableMap.of(
            Event.LOCALIP,
            localSocketAddress.getAddress().getHostAddress(),
            Event.LOCALPORT,
            String.valueOf(localSocketAddress.getPort()),
            Event.REMOTEIP,
            remoteSocketAddress.getAddress().getHostAddress(),
            Event.REMOTEPORT,
            String.valueOf((remoteSocketAddress.getPort())),
            ConnectionSignature,
            getConnectionSignature.apply(localSocketAddress, remoteSocketAddress)));
  }

  @Override
  public InetSocketAddress getLocalSocketAddress() {
    return localSocketAddress;
  }

  @Override
  public InetSocketAddress getRemoteSocketAddress() {
    return remoteSocketAddress;
  }

  @Override
  public boolean equals(Object connection) {
    return this.channel.equals(((AbstractConnection) connection).channel);
  }

  @Override
  public String toString() {
    return "localAddress " + channel.localAddress() + " , remoteAddress " + channel.remoteAddress();
  }
}
