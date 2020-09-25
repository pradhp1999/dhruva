/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import io.netty.channel.ChannelFuture;
import java.util.concurrent.CompletableFuture;

public interface Connection {

  DsBindingInfo getConnectionInfo();

  /** @return transport type of this connection */
  Transport getConnectionType();

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination.
   *
   * @param buffer the message bytes to send across
   * @return Returns a CompletableFuture indicating the send state. CompletableFuture will complete
   *     exceptionally if the send fails.
   */
  CompletableFuture<Boolean> send(byte[] buffer);

  Connection.STATE getConnectionState();

  void setConnectionState();

  /**
   * Method is called when the any error happens on the connection. Transport layer takes care of
   * closing the connection so no explicit close is necessary.
   */
  void onConnectionError(Throwable cause);

  ChannelFuture closeConnection();

  void addReference();

  void removeReference();

  void updateTimeStamp();

  boolean shouldClose();

  int referenceCount();

  enum STATE {
    CONNECTED,
    ACTIVE,
    INACTIVE,
    DISCONNECTED
  }
}
