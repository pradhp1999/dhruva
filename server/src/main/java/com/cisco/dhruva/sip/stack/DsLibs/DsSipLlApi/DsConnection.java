// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamEventListener;
import com.cisco.dhruva.transport.Transport;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;

/**
 * This interface defines a connection that is used to send data across the network through the
 * underlying socket.
 */
public interface DsConnection extends DsInputStreamEventListener {

  /** Buffer time to wait for acquiring lock on a DsConnection object */
  long LOCK_TRYING_DURATION_BUFFER = 1000;

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  CompletableFuture<Boolean> sendAsync(byte[] buffer) throws IOException;

  /**
   * Closes the underlying socket.
   *
   * @throws IOException if there is an error while closing the socket or if the socket is already
   *     closed
   */
  void closeSocket() throws IOException;

  /**
   * Returns the binding information associated with this connection.
   *
   * @return the binding information associated with this connection
   */
  DsBindingInfo getBindingInfo();

  /**
   * Returns InetAddress associated with this connection. This address specifies the remote or
   * destination address.
   *
   * @return the remote InetAddress associated with this connection
   */
  InetAddress getInetAddress();

  /**
   * Return the remote or destination port associated with this connection.
   *
   * @return the remote port associated with this connection
   */
  int getPortNo();

  /**
   * Returns the transport type of this connection.
   *
   * @return the transport type of this connection
   */
  Transport getTransportType();

  /**
   * Adds the specified listener to this connection's listener list, that will be notified for the
   * DsConnectionEvent(s), if any.
   *
   * @param listener The listener to be added that in turn will receive DsConnectionEvent(s)
   *     notifications, if any
   */
  void addDsConnectionEventListener(DsConnectionEventListener listener);

  /**
   * Removes the specified listener from this connection's listener list, that will no longer be
   * notified for the DsConnectionEvent(s), if any.
   *
   * @param listener The listener to be removed and will no longer receive DsConnectionEvent(s)
   *     notifications, if any
   */
  void removeDsConnectionEventListener(DsConnectionEventListener listener);

  /** Increments the reference count for this connection. */
  void addReference();

  /**
   * Decrement the reference count, and if it falls to zero, update the timestamp on the connection.
   */
  void removeReference();

  /**
   * Given the current time, returns whether this connection should be closed and removed.
   *
   * @param current_time the current time in milliseconds
   * @return <code>true</code> if this connection should be closed and removed, otherwise returns
   *     <code>false</code>.
   */
  boolean shouldClose(long current_time);

  void setTimeout(int timeout);
} // Ends interface
