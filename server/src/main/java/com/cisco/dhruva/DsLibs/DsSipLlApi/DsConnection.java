// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;

/**
 * This interface defines a connection that is used to send data across the network through the
 * underlying socket. A concrete connection can be constructed through the {@link
 * DsConnectionFactory DsConnectionFactory} by passing appropriate parameter like transport type and
 * address.
 */
public interface DsConnection extends DsInputStreamEventListener {

  /** Buffer time to wait for acquiring lock on a DsConnection object */
  public static final long LOCK_TRYING_DURATION_BUFFER = 1000;

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public void send(byte buffer[]) throws IOException;

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in the addr and port.
   *
   * @param buffer the message bytes to send across
   * @param addr the address to send the message to
   * @param port the port to send the message to
   * @throws IOException if there is an I/O error while sending the message
   */
  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException;

  /**
   * Closes the underlying socket.
   *
   * @throws IOException if there is an error while closing the socket or if the socket is already
   *     closed
   */
  public void closeSocket() throws IOException;

  /**
   * Returns the binding information associated with this connection.
   *
   * @return the binding information associated with this connection
   */
  public DsBindingInfo getBindingInfo();

  /**
   * Returns InetAddress associated with this connection. This address specifies the remote or
   * destination address.
   *
   * @return the remote InetAddress associated with this connection
   */
  public InetAddress getInetAddress();

  /**
   * Return the remote or destination port associated with this connection.
   *
   * @return the remote port associated with this connection
   */
  public int getPortNo();

  /**
   * Returns the transport type of this connection.
   *
   * @return the transport type of this connection
   */
  public int getTransportType();

  /**
   * Tells when the last time this connection was used.
   *
   * @return the difference, measured in milliseconds, between the last time this connection was
   *     used and midnight, January 1, 1970 UTC
   */
  public long getTimeStamp();

  /**
   * Set the timeout in seconds for this connection. This time is used to tell the connection how
   * long it should wait after it is unreferenced before closing.
   *
   * @param timeout the time in seconds to wait before closing
   */
  public void setTimeout(long timeout);

  /**
   * Adds the specified listener to this connection's listener list, that will be notified for the
   * DsConnectionEvent(s), if any.
   *
   * @param listener The listener to be added that in turn will receive DsConnectionEvent(s)
   *     notifications, if any
   */
  public void addDsConnectionEventListener(DsConnectionEventListener listener);

  /**
   * Removes the specified listener from this connection's listener list, that will no longer be
   * notified for the DsConnectionEvent(s), if any.
   *
   * @param listener The listener to be removed and will no longer receive DsConnectionEvent(s)
   *     notifications, if any
   */
  public void removeDsConnectionEventListener(DsConnectionEventListener listener);

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

  /**
   * Set the application context for this connection. This context is used to return the application
   * context, when the inactivity timer expires for this connection and it is a persistent
   * connection.
   *
   * @param context the application context
   */
  public void setApplicationContext(Object context);

  /**
   * Get the application context for this connection. Set only in case of a persistent connection.
   *
   * @return the application context associated with this connection
   */
  public Object getApplicationContext();

  /**
   * Check if the connection type is set as persistent by application.
   *
   * @return <code>true</code> if the connection is persistent, else <code>false</code>.
   */
  public boolean isPersistent();

  /**
   * Set the event interface for this connection. This interface is used to callback application in
   * case of an event on this connection.
   *
   * @param eventInterface the object to call back when there is an inactivity timeout
   * @throws IllegalArgumentException the connection type is persistent and eventInterface is <code>
   *     null</code>.
   */
  public void setEventInterface(DsSipConnectionEventInterface eventInterface);

  /**
   * Get the callback interface object passed earlier by the application.
   *
   * @return connection event interface, may return <code>null</code> only if the connection is not
   *     persistent
   */
  public DsSipConnectionEventInterface getEventInterface();

  /**
   * Set the connection properties as desired by the application for persistent connections.
   *
   * @param isPersistent connection type, <code>true</code> = persistent, <code>false</code> =
   *     transient
   * @param callback callback interface handle
   * @param inactivityTimer inactivity timeout, in seconds
   * @param cookie application context/state info
   * @throws IllegalArgumentException the connection type is persistent and <code>callback</code> is
   *     <code>null</code>.
   */
  public void setConnectionOpt(
      boolean isPersistent,
      DsSipConnectionEventInterface callback,
      int inactivityTimer,
      Object cookie);

  /**
   * Checks if the stack is shutting down.
   *
   * @return <code>true</code> if the stack is shutting down and <code>false</code> for normal
   *     operation.
   */
  public boolean isShutingDown();

  /**
   * Checks the DsConnection connecting state.
   *
   * @return it will return <code>true</code> if connection is in connecting state. <code>false
   *     </code> if not in connecting state.
   */
  public boolean isConnecting();

  /** Lock the DsConnection object. */
  public void lock();

  /**
   * Acquires the lock if it is not held by another thread within the given waiting time and the
   * current thread has not been interrupted
   *
   * @param timeout waiting time in milliseconds.
   */
  public boolean trylock(long timeout);

  /** Unlock the DsConnection object. */
  public void unlock();

  /** set the DsConnection to connected state. */
  public void connected();

  /** start the connection towards the end-point or destination. */
  public void initiateConnect() throws IOException, SocketException;

  /**
   * Update the timestamp of this connection. Should be done when message is sent or received from
   * this socket.
   */
  public default void updateTimeStamp() {
    // do nothing
  }
} // Ends interface
