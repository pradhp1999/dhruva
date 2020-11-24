/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import io.netty.channel.ChannelFuture;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public interface Connection {
  String COLON = ":";

  BiFunction<InetSocketAddress, InetSocketAddress, String> getConnectionSignature =
      (localSocketAddress, remoteSocketAddress) ->
          localSocketAddress.getAddress().getHostAddress()
              + COLON
              + +localSocketAddress.getPort()
              + COLON
              + remoteSocketAddress.getAddress().getHostAddress()
              + COLON
              + remoteSocketAddress.getPort();

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

  Map<String, String> connectionInfoMap();

  InetSocketAddress getLocalSocketAddress();

  InetSocketAddress getRemoteSocketAddress();

  enum STATE {
    CONNECTED,
    ACTIVE,
    INACTIVE,
    DISCONNECTED
  }
}
