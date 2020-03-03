/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import io.netty.channel.ChannelFuture;
import java.util.concurrent.CompletableFuture;

public interface Connection {

  public DsBindingInfo getConnectionInfo();

  /** @return transport type of this connection */
  public Transport getConnectionType();

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination.
   *
   * @param buffer the message bytes to send across
   * @return Returns a CompletableFuture indicating the send state. CompletableFuture will complete
   *     exceptionally if the send fails.
   */
  public CompletableFuture<Boolean> send(byte buffer[]);

  public Connection.STATE getConnectionState();

  public void setConnectionState();

  /**
   * Method is called when the any error happens on the connection. Transport layer takes care of
   * closing the connection so no explicit close is necessary.
   */
  public void onConnectionError(Throwable cause);

  public ChannelFuture closeConnection();

  void addReference();

  void removeReference();

  void updateTimeStamp();

  boolean shouldClose();

  int referenceCount();

  public enum STATE {
    ACTIVE,
    INACTIVE
  }
}
