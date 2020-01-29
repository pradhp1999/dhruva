// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.DsLibs.DsUtil.DsNetwork;
import java.io.IOException;
import org.apache.logging.log4j.Level;

/**
 * This is the base class for classes which are transport listeners. The base class handles the
 * administrative functions of closing and restarting. The specifics of what it means to create a
 * server socket, listen on the socket and close the socket are delegated to derived classes. This
 * class's main purpose is to centralize exception handling and restart synchronization for all
 * transport listeners.
 */
public abstract class DsTransportListener implements Runnable {
  /**
   * The max number of times to try ServerSocket constructor -- worst case is when the old server
   * socket is not fully closed. Retries should fail quickly.
   */
  private static final int MAX_TRIES = 20;
  /** The max tries for closing a server socket. */
  private static final int MAX_CLOSE_TRIES = 3;

  /** Set to true if user calls close on us. */
  private boolean m_shouldClose = false;
  /** Prevent race cond on close, init. */
  private boolean m_running = false;
  /** Thread id across instances of this class. */
  private static long m_tid = 0;

  /** Is this listener pending closure. */
  protected boolean m_pendingClosure = false;
  /** Synchronization object. */
  protected Object m_lock = new Object();
  /** Prevent race cond on close, init. */
  protected boolean m_active = false;

  /** Network configuration for this listen point. */
  protected DsNetwork m_network;

  /**
   * Creates a new transport listener with supplied network.
   *
   * @param network the network to associate with this listener
   * @throws IllegalArgumentException if network is null
   */
  protected DsTransportListener(DsNetwork network) {
    if (network == null) throw new IllegalArgumentException("null passed in for network parameter");
    m_network = network;
  }

  /** Creates a new transport listener with the default network. */
  protected DsTransportListener() {
    this(DsNetwork.getDefault());
  }

  /**
   * Mark this listener for shutdown. Sets a flag which is forwarded to the call to doListen.
   *
   * @param closing set to <code>true</code> to shutdown
   */
  public void setPendingClosure(boolean closing) {
    m_pendingClosure = closing;
  }

  /**
   * Convenience function for starting or stopping this listener which takes a boolean and tracks
   * current state.
   *
   * @param active if <code>true</code> calls initialize and starts this listener in a new thread,
   *     otherwise calls doClose. If desired state == current state, this method does nothing.
   * @throws IOException if there is an error either while initializing the connection when <code>
   *     active = true</code> or while closing the connection when <code>active = false</code>
   */
  public void setActive(boolean active) throws IOException {
    synchronized (m_lock) {
      if (active == m_active) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG,
              "DsTransportListener.setActive("
                  + active
                  + ") is already "
                  + active
                  + ".  Ignoring.");
        }

        return;
      }

      if (active) {
        m_active = true;
        initialize();
        new Thread(this, "DsTransportListener-" + m_tid++).start();
      } else {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG, "DsTransportListener.setActive - closing listener.");
        }

        m_active = false;

        doClose();
      }
    }
  }

  /**
   * Re-initialize the server socket. This method will try to call the derived class's createSocket
   * a maximum of three times.
   *
   * <p>Used to re-initialize the server socket after close has been called. After initialize, do
   * 'new Thread(obj).start()'
   *
   * @throws IOException if ServerSocket constructor fails after MAX_TRIES
   * @throws DsException if Listener can not be added to network.
   */
  public void initialize() throws IOException {
    IOException io_exc = null;
    boolean connected = false;
    int retry_count = 0;

    synchronized (m_lock) {
      m_shouldClose = false;
      m_network.addListener(this);

      // break if tried to create server socket MAX_TRIES times
      // or connected == true
      while (true) {
        try {
          createSocket();
          connected = true;
        } catch (IOException ioe) {
          io_exc = ioe;
        } finally {
          if (connected || (++retry_count == MAX_TRIES)) {
            break;
          }
        }
      }
    }

    if (!connected) {
      m_network.removeListener(this);
      // if we aren't connected io_exc shouldn't be null
      if (io_exc != null) {
        // REFACTOR
        //        SIPListenerMBeanImpl.sendNotification(
        //            SIPListenerMBean.LISTENER_EXCEPTION,
        //            new String[] {
        //              "Error creating Listen Socket "
        //                  + " localAddress = "
        //                  + getLocalAddress()
        //                  + " localPort = "
        //                  + getLocalPort()
        //                  + " Transport= "
        //                  + DsSipTransportType.getTypeAsString(getTransport())
        //                  + " Exception is "
        //                  + io_exc.getMessage()
        //            });
        throw io_exc;
      } else {
        //        SIPListenerMBeanImpl.sendNotification(
        //            SIPListenerMBean.LISTENER_EXCEPTION,
        //            new String[] {
        //              "Error createing Listen socket "
        //                  + " localAddress = "
        //                  + getLocalAddress()
        //                  + " localPort = "
        //                  + getLocalPort()
        //                  + " Transport = "
        //                  + DsSipTransportType.getTypeAsString(getTransport())
        //            });
        throw new IOException("DsTransportListener: error constructing server socket");
      }
    }
  }

  /**
   * Close the underlying server socket and potentially cause the thread we are running in to exit.
   *
   * @throws IOException if ServerSocket fails after three times
   */
  public void doClose() throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTransportListener.doClose - closing listener.");
    }

    IOException io_exc = null;
    boolean closed = false;
    int retry_count = 0;

    synchronized (m_lock) {
      m_shouldClose = true;
      m_network.removeListener(this);
      while (true) {
        try {
          if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
            DsLog4j.connectionCat.log(
                Level.DEBUG, "DsTransportListener.doClose - calling closeSocket().");
          }

          closeSocket();
          closed = true;
        } catch (IOException ioe) {
          if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
            DsLog4j.connectionCat.log(Level.DEBUG, "Exception: ", ioe);
          }

          io_exc = ioe;
        } finally {
          if (closed || (++retry_count == MAX_CLOSE_TRIES)) break;
        }
      }
    }

    if (!closed) {
      if (io_exc != null) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG,
              "DsTransportListener.doClose - listener close failed, exception encountered.");
        }

        throw io_exc;
      } else {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG,
              "DsTransportListener.doClose - listener close failed, max tries exceeded.");
        }

        throw new IOException("DsTransportListener: error closing server socket");
      }
    } else {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(
            Level.DEBUG, "DsTransportListener.doClose - listener closed successfully.");
      }
    }
  }

  /**
   * Accepts new connections until close is called. This method will return if doClose is called
   * without a call to initialize before the internal synchronization lock is obtained again.
   */
  public void run() {
    // if we are already running, return
    synchronized (m_lock) {
      if (m_running) {
        return;
      }
      m_running = true;
    }

    while (m_running) {
      try {
        // block on IO
        doListen();
      } catch (IOException exc) {
        // TODO handle
        DsLog4j.connectionCat.log(
            Level.ERROR,
            "Exception in socket  listening on adress "
                + getLocalAddress()
                + " port "
                + getLocalPort()
                + " Transport ="
                + DsSipTransportType.getTypeAsString(getTransport())
                + "Exception is ",
            exc);
        //        SIPListenerMBeanImpl.sendNotification(
        //            SIPListenerMBean.LISTENER_ACCEPT_EXCEPTION,
        //            new String[] {
        //              "Exception in socket listening on adress "
        //                  + getLocalAddress()
        //                  + " port "
        //                  + getLocalPort()
        //                  + " Transport= "
        //                  + DsSipTransportType.getTypeAsString(getTransport())
        //                  + " Exception info= "
        //                  + exc.getMessage()
        //            });
        // exc.printStackTrace();
      } catch (Throwable exc) {
        DsLog4j.connectionCat.log(
            Level.ERROR,
            "Exception in socket listening on adress "
                + getLocalAddress()
                + " port "
                + getLocalPort()
                + " Transport= "
                + DsSipTransportType.getTypeAsString(getTransport()),
            exc);
        //        SIPListenerMBeanImpl.sendNotification(
        //            SIPListenerMBean.LISTENER_ACCEPT_EXCEPTION,
        //            new String[] {
        //              "Exception in socket listening on adress "
        //                  + getLocalAddress()
        //                  + " port "
        //                  + getLocalPort()
        //                  + "Transport= "
        //                  + DsSipTransportType.getTypeAsString(getTransport())
        //                  + " Exception info= "
        //                  + exc.getMessage()
        //            });
        // exc.printStackTrace();
      } finally {
        synchronized (m_lock) {
          if (m_shouldClose) {
            m_running = false;
          }
        }
      }
    }
  }

  /**
   * Returns the transport type of this listener.
   *
   * @return the transport type of this listener
   */
  public abstract int getTransport();

  /**
   * Abstract method used to create the listening socket.
   *
   * @throws IOException if ServerSocket couldn't be created
   */
  protected abstract void createSocket() throws IOException;

  /**
   * Abstract method used to listen for new connections or data. Called from with in this class's
   * run method.
   *
   * @throws IOException if there is an error while listening on the server socket
   */
  protected abstract void doListen() throws IOException;

  /**
   * Abstract method used to close the listening socket.
   *
   * @throws IOException if there is an error while closing the server socket
   */
  protected abstract void closeSocket() throws IOException;

  /**
   * Abstract method to get the local IP address the listener is listening on
   *
   * @return listener IP address in String
   */
  protected abstract String getLocalAddress();

  /**
   * Abstract method to get the local port the listener is listening on
   *
   * @return port on which the listener is listening
   */
  protected abstract int getLocalPort();
}
