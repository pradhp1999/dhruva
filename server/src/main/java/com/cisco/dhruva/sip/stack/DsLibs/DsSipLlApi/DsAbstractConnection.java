// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Level;

/**
 * An abstraction for the connection. It maintains reference count and list of DsConnectionEvent(s)
 * listeners.
 */
abstract class DsAbstractConnection implements DsConnection {
  /** 60 second default timeout. */
  private static final int DEFAULT_TIMEOUT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CONNECTION_TIMEOUT, DsConfigManager.PROP_CONNECTION_TIMEOUT_DEFAULT);

  /** The last time this connection was used. */
  private long m_TimeStamp;

  /** The connection event listeners. */
  private HashSet m_ConnectionEventListeners = new HashSet();

  /** The lock for counting references. */
  private Object m_referenceLock = new Object();

  /** Binding information for this connection. */
  protected DsBindingInfo m_bindingInfo;

  /** Timeout value for this connection, in seconds. */
  protected long m_Timeout = DEFAULT_TIMEOUT;

  /**
   * <code>true</code> if the responsibility to close the connection on inactivity lies with
   * application.
   */
  protected boolean m_isPersistent;

  /** Opaque object set by application, returned on Connection inactivity callback. */
  protected Object
      m_applicationContext; // set by application, returned on Connection inactivity callback

  /** Connection event callback interface. */
  protected DsSipConnectionEventInterface m_eventInterface;

  /** The number of references to this connection. */
  protected int m_ReferenceCount = 0;

  /** <code>true</code> if this connection has been closed because the queue overflowed. */
  protected boolean m_isShutdown;

  /** <code>true</code> if this connection is in connecting state. */
  protected boolean m_isConnecting = false;

  /** Lock for this object. */
  private ReentrantLock m_lock = new ReentrantLock();

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public abstract void send(byte buffer[]) throws IOException;

  /**
   * Returns the underlying socket associated with this connection.
   *
   * @return the underlying socket associated with this connection
   */
  // public abstract Object getSocket();

  /**
   * Returns the binding information associated with this connection.
   *
   * @return the binding information associated with this connection
   */
  public DsBindingInfo getBindingInfo() {
    return m_bindingInfo;
  }

  /**
   * Set the binding info - useful for unconnected datagram sockets.
   *
   * @param info the binding information for this connection
   */
  void setBindingInfo(DsBindingInfo info) {
    m_bindingInfo = info;
  }

  /**
   * Returns InetAddress associated with this connection. This address specifies the remote or
   * destination address.
   *
   * @return the remote InetAddress associated with this connection
   */
  public InetAddress getInetAddress() {
    return m_bindingInfo.getRemoteAddress();
  }

  /**
   * Return the remote or destination port associated with this connection.
   *
   * @return the remote port associated with this connection
   */
  public int getPortNo() {
    return m_bindingInfo.getRemotePort();
  }

  /**
   * Returns the transport type of this connection.
   *
   * @return the transport type of this connection
   */
  public int getTransportType() {
    return m_bindingInfo.getTransport();
  }

  /**
   * Tells when the last time this connection was used.
   *
   * @return the difference, measured in milliseconds, between the last time this connection was
   *     used and midnight, January 1, 1970 UTC
   */
  public long getTimeStamp() {
    return m_TimeStamp;
  }

  /**
   * Set the timeout in seconds for this connection. This time is used to tell the connection how
   * long it should wait after it is unreferenced before closing.
   *
   * @param timeout the time in seconds to wait before closing
   */
  public void setTimeout(long timeout) {
    m_Timeout = timeout;
  }

  /*
   * javadoc inherited
   */
  public boolean isPersistent() {
    return m_isPersistent;
  }

  /*
   * javadoc inherited
   */
  public void setApplicationContext(Object context) {
    m_applicationContext = context;
  }

  /*
   * javadoc inherited
   */
  public Object getApplicationContext() {
    return m_applicationContext;
  }

  /*
   * javadoc inherited
   */
  public void setEventInterface(DsSipConnectionEventInterface eventInterface) {
    if (m_isPersistent && eventInterface == null) {
      throw new IllegalArgumentException(
          "eventInterface must not be null for persistent connections.");
    }

    m_eventInterface = eventInterface;
  }

  /*
   * javadoc inherited
   */
  public DsSipConnectionEventInterface getEventInterface() {
    return m_eventInterface;
  }

  /*
   * javadoc inherited
   */
  public void setConnectionOpt(
      boolean isPersistent,
      DsSipConnectionEventInterface callback,
      int inactivityTimer,
      Object cookie) {
    if (isPersistent && null == callback) {
      throw new IllegalArgumentException(
          "The callback interface must not be null for persistent connections.");
    }
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "Connection Persistence setConnectionOpt(): isPersistent: = "
              + isPersistent
              + "inactivity timer = "
              + inactivityTimer);
    }

    m_isPersistent = isPersistent;
    m_eventInterface = callback;
    m_Timeout = inactivityTimer;
    m_applicationContext = cookie;
  }

  /** Increments the reference count for this connection. */
  public void addReference() {
    synchronized (m_referenceLock) {
      ++m_ReferenceCount;
    }

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG, this + " addReference: m_ReferenceCount == " + m_ReferenceCount);
    }
  }

  /**
   * Decrement the reference count, and if it falls to zero, update the timestamp on the connection
   */
  public void removeReference() {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG))
      DsLog4j.connectionCat.log(Level.DEBUG, "removeReference");
    synchronized (m_referenceLock) {
      --m_ReferenceCount;
      if (m_ReferenceCount == 0) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG))
          DsLog4j.connectionCat.log(
              Level.DEBUG, this + " removeReference: m_ReferenceCount == " + m_ReferenceCount);
        updateTimeStamp();
      }

      if (m_ReferenceCount < 0) {
        if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
          DsLog4j.connectionCat.log(
              Level.WARN,
              this + " removeReference: negative count! m_ReferenceCount == " + m_ReferenceCount);
          DsLog4j.connectionCat.warn(
              this + " removeReference: negative count! m_ReferenceCount == " + m_ReferenceCount);
        }
      }
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
  public boolean shouldClose(long current_time) {
    if (m_isConnecting) {
      return false;
    }
    if (m_isPersistent) {
      if (m_Timeout < (current_time - m_TimeStamp) / 1000) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG,
              "Connection Persistence shouldClose(): calling inactivity timer callback");
        }
        this.updateTimeStamp();
        m_eventInterface.inactivityTimerExpired(this, m_applicationContext);
      }
    } else {
      return m_ReferenceCount == 0 && (m_Timeout < (current_time - m_TimeStamp) / 1000);
    }
    return false;
  }

  /**
   * Closes the underlying socket.
   *
   * @throws IOException if there is an error while closing the socket or if the socket is already
   *     closed
   */
  public abstract void closeSocket() throws IOException;

  /**
   * Returns the string representation of this connection showing the connection information and the
   * reference count.
   *
   * @return the string representation of this connection
   */
  public String toString() {
    return m_bindingInfo.toString() + " ref count:  " + m_ReferenceCount;
  }

  /**
   * Callback for DsInputStreamErrorEvent.
   *
   * @param evt event that callback is handling
   */
  public void onDsInputStreamErrorEvent(DsInputStreamErrorEvent evt) {
    // handle an error on the stream like a close as well
    notifyListeners(new DsConnectionErrorEvent((DsConnection) this, evt.getException()));
    evt.getReaderSource().removeDsInputStreamEventListener(this);
  }

  /**
   * Callback for DsInputStreamClosedEvent.
   *
   * @param evt event that callback is handling
   */
  public void onDsInputStreamClosedEvent(DsInputStreamClosedEvent evt) {
    notifyListeners(new DsConnectionClosedEvent((DsConnection) this));
    evt.getReaderSource().removeDsInputStreamEventListener(this);
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

    int i = 0;
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

  /**
   * Checks if the stack is shutting down.
   *
   * @return <code>true</code> if the stack is shutting down and <code>false</code> for normal
   *     operation.
   */
  public boolean isShutingDown() {
    return m_isShutdown;
  }

  /**
   * Checks the DsConnection connecting state.
   *
   * @return it will return <code>true</code> if connection is in connecting state. <code>false
   *     </code> if not in connecting state.
   */
  public boolean isConnecting() {
    return m_isConnecting;
  }

  /** Lock the DsConnection object. */
  public void lock() {
    m_lock.lock();
  }

  /**
   * Acquires the lock if it is not held by another thread within the given waiting time and the
   * current thread has not been interrupted
   *
   * @param timeout waiting time in milliseconds.
   */
  public boolean trylock(long timeout) {
    try {
      return m_lock.tryLock(timeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException intex) {
      return false;
    }
  }

  /** Unlock the DsConnection object. */
  public void unlock() {
    try {
      m_lock.unlock();
    } catch (IllegalMonitorStateException imse) {
      // Ignoring this.
    }
  }

  /** sets the DsConnection to connected state. */
  public void connected() {
    m_isConnecting = false;
  }
} // Ends class
