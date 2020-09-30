// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.Transport;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.event.Level;

/**
 * An abstraction for the connection. It maintains reference count and list of DsConnectionEvent(s)
 * listeners.
 */
abstract class DsAbstractConnection implements DsConnection {

  /** The connection event listeners. */
  private HashSet m_ConnectionEventListeners = new HashSet();

  protected Connection connection;

  /** Binding information for this connection. */
  protected DsBindingInfo bindingInfo;

  private long timeout;

  public DsAbstractConnection(Connection connection) {
    this.connection = connection;
    this.bindingInfo = connection.getConnectionInfo();
  }

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public CompletableFuture<Boolean> sendAsync(byte buffer[]) throws IOException {
    CompletableFuture<Boolean> sendFuture = connection.send(buffer);
    updateTimeStamp();
    return sendFuture;
  }

  /*
  TODO: Change future block to callback
   */
  public void sendSync(byte buffer[]) throws IOException {
    CompletableFuture<Boolean> sendFuture = sendAsync(buffer);
    try {
      sendFuture.get(DsNetwork.getConnectionWriteTimeoutInMilliSeconds(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new IOException(e);
    }
  }

  /**
   * Returns the binding information associated with this connection.
   *
   * @return the binding information associated with this connection
   */
  public DsBindingInfo getBindingInfo() {
    return bindingInfo;
  }

  /**
   * Set the binding info - useful for unconnected datagram sockets.
   *
   * @param info the binding information for this connection
   */
  void setBindingInfo(DsBindingInfo info) {
    bindingInfo = info;
  }

  /**
   * Returns InetAddress associated with this connection. This address specifies the remote or
   * destination address.
   *
   * @return the remote InetAddress associated with this connection
   */
  public InetAddress getInetAddress() {
    return bindingInfo.getRemoteAddress();
  }

  /**
   * Return the remote or destination port associated with this connection.
   *
   * @return the remote port associated with this connection
   */
  public int getPortNo() {
    return bindingInfo.getRemotePort();
  }

  /**
   * Returns the transport type of this connection.
   *
   * @return the transport type of this connection
   */
  public Transport getTransportType() {
    return bindingInfo.getTransport();
  }

  /**
   * Set the timeout in seconds for this connection. This time is used to tell the connection how
   * long it should wait after it is unreferenced before closing.
   *
   * @param timeout the time in seconds to wait before closing
   */
  /*
  TODO implement timeout
  */
  @Override
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  /** Increments the reference count for this connection. */
  public void addReference() {
    connection.addReference();
  }

  /**
   * Decrement the reference count, and if it falls to zero, update the timestamp on the connection
   */
  public void removeReference() {
    connection.removeReference();
  }

  /**
   * Update the timestamp of this connection. Should be done when message is sent or received from
   * this socket.
   */
  public void updateTimeStamp() {
    connection.updateTimeStamp();
  }

  /**
   * Given the current time, returns whether this connection should be closed and removed.
   *
   * @return <code>true</code> if this connection should be closed and removed, otherwise returns
   *     <code>false</code>.
   */
  public boolean shouldClose(long current_time) {
    return connection.shouldClose();
  }

  /**
   * Closes the underlying socket.
   *
   * @throws IOException if there is an error while closing the socket or if the socket is already
   *     closed
   */
  public void closeSocket() throws IOException {
    connection.closeConnection();
  }

  /**
   * Returns the string representation of this connection showing the connection information and the
   * reference count.
   *
   * @return the string representation of this connection
   */
  public String toString() {
    return bindingInfo.toString() + " ref count:  " + connection.referenceCount();
  }

  /**
   * Callback for DsInputStreamErrorEvent.
   *
   * @param evt event that callback is handling
   */
  public void onDsInputStreamErrorEvent(DsInputStreamErrorEvent evt) {
    // handle an error on the stream like a close as well
    notifyListeners(new DsConnectionErrorEvent((DsConnection) this, evt.getException()));
    /*
    TODO
     */
    // evt.getReaderSource().removeDsInputStreamEventListener(this);
  }

  /**
   * Callback for DsInputStreamClosedEvent.
   *
   * @param evt event that callback is handling
   */
  public void onDsInputStreamClosedEvent(DsInputStreamClosedEvent evt) {
    notifyListeners(new DsConnectionClosedEvent((DsConnection) this));
    // TODO
    // evt.getReaderSource().removeDsInputStreamEventListener(this);
  }

  /**
   * Callback for DsInputStreamEvent.
   *
   * @param evt event that callback is handling
   */
  public void onDsInputStreamEvent(DsInputStreamEvent evt) {
    // this is a data event -- we ignore it
  }

  /**
   * Adds the specified listener to this connection's listener list, that will be notified for the
   * DsConnectionEvent(s), if any.
   *
   * @param listener The listener to be added that in turn will receive DsConnectionEvent(s)
   *     notifications, if any
   */
  public void addDsConnectionEventListener(DsConnectionEventListener listener) {
    if (m_ConnectionEventListeners == null) {
      return;
    }

    synchronized (m_ConnectionEventListeners) {
      m_ConnectionEventListeners.add(listener);
    }
  }

  /**
   * Removes the specified listener from this connection's listener list, that will no longer be
   * notified for the DsConnectionEvent(s), if any.
   *
   * @param listener The listener to be removed and will no longer receive DsConnectionEvent(s)
   *     notifications, if any
   */
  public void removeDsConnectionEventListener(DsConnectionEventListener listener) {
    if (m_ConnectionEventListeners == null) {
      return;
    }

    synchronized (m_ConnectionEventListeners) {
      m_ConnectionEventListeners.remove(listener);
    }
  }

  /**
   * Notify registered listeners that an event has occurred
   *
   * @param event the event that we are propagating
   */
  public void notifyListeners(DsConnectionEvent event) {
    if (m_ConnectionEventListeners == null) {
      return;
    }

    Object listeners[] = null;

    synchronized (m_ConnectionEventListeners) {
      listeners = m_ConnectionEventListeners.toArray();
    }

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "notifyListeners(): Notifying the connection event listeners of the connection event");
    }

    int i;
    int len = listeners.length;
    switch (event.getType()) {
      case DsConnectionEvent.TYPE_CONNECTION_CLOSED:
        for (i = 0; i < len; i++) {
          ((DsConnectionEventListener) listeners[i])
              .onDsConnectionClosedEvent((DsConnectionClosedEvent) event);
        }
        break;

      case DsConnectionEvent.TYPE_CONNECTION_ERROR:
        for (i = 0; i < len; i++) {
          ((DsConnectionEventListener) listeners[i])
              .onDsConnectionErrorEvent((DsConnectionErrorEvent) event);
        }
        break;
      case DsConnectionEvent.TYPE_CONNECTION_ICMP_ERROR:
        break;
    }
  }
}
