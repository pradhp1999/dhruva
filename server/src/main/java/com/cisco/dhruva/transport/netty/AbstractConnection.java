/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.transport.netty;

import com.cisco.dhruva.config.network.NetworkConfig;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractConnection implements Connection {

  Channel channel;
  private AtomicInteger m_ReferenceCount = new AtomicInteger(0);
  private NetworkConfig networkConfig;
  protected long m_Timeout;
  private long m_TimeStamp;
  private DsBindingInfo connectionBindingInfo;
  private InetSocketAddress localSocketAddress;
  private InetSocketAddress remoteSocketAddress;
  Transport transport;
  private Logger logger = DhruvaLoggerFactory.getLogger(AbstractConnection.class);

  public AbstractConnection(Channel channel, NetworkConfig networkConfig, Transport transport) {
    this.channel = channel;
    this.networkConfig = networkConfig;
    m_Timeout = networkConfig.connectionCacheConnectionIdleTimeout();
    this.localSocketAddress = (InetSocketAddress) channel.localAddress();
    this.remoteSocketAddress = (InetSocketAddress) channel.remoteAddress();
    this.transport = transport;
    connectionBindingInfo =
        new DsBindingInfo(
            localSocketAddress.getAddress(),
            localSocketAddress.getPort(),
            remoteSocketAddress.getAddress(),
            remoteSocketAddress.getPort(),
            this.transport.ordinal());
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

  /** Increments the reference count for this connection. */
  @Override
  public void addReference() {
    m_ReferenceCount.incrementAndGet();
  }

  /**
   * Decrement the reference count, and if it falls to zero, update the timestamp on the connection
   */
  @Override
  public void removeReference() {

    m_ReferenceCount.decrementAndGet();
    updateTimeStamp();

    if (m_ReferenceCount.get() < 0) {
      logger.warn(
          "Connection {} removeReference: negative count! m_ReferenceCount == {} ",
          this,
          m_ReferenceCount);
    }
  }

  /**
   * Update the timestamp of this connection. Should be done when message is sent or received from
   * this socket.
   */
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
    return m_ReferenceCount.get() == 0 && (m_Timeout < (current_time - m_TimeStamp) / 1000);
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

  @Override
  public String toString() {
    return "localAddress " + channel.localAddress() + " , remoteAddress " + channel.remoteAddress();
  }
}