// Copyright (c) 2005-2010, 2015 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsEOFException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Executor;
import org.apache.logging.log4j.Level;

/**
 * Abstract base class for reading and processing incoming messages. This class runs in its own
 * thread. Keep waiting for messages to come on the socket input stream, frame and construct the
 * message and put the constructed message in its work processor queue to be processed.
 */
public abstract class DsMessageReader implements Runnable {
  private static long m_readerId = 0;

  /** The DsSocket associated with the reader. */
  private DsSocket socket;
  /** The binding information to keep with the message. */
  private DsBindingInfo bindingInfo;
  /** For I/O error notifications. */
  private Vector socketListeners;
  /** For closed socket notifications. */
  private Vector closedEventListeners;

  /** Where to get the message from. */
  private InputStream stream;

  // DG-TCP
  /** Where to put incoming work items. */
  private Executor workQueue;

  private boolean handshakeDone;
  private static Timer sslHandshakeTimer;
  private static Object lock = new Object();
  private static final boolean IS_CRLCHECK_ENABLED =
      DsConfigManager.getProperty(
          DsConfigManager.IS_CRLCHECK_ENABLED, DsConfigManager.IS_CRLCHECK_ENABLED_DEFAULT);

  /**
   * Constructs message reader object with the specified parameters.
   *
   * @param str the input stream to read messages from
   * @param queue the work queue where to put messages to be processed
   * @param sock the socket associated with input stream
   * @param binfo the binding info of the incoming message
   */
  public DsMessageReader(InputStream str, Executor queue, DsSocket sock, DsBindingInfo binfo) {
    workQueue = queue;
    stream = str;
    socket = sock;
    bindingInfo = binfo;
    socketListeners = new Vector();
    closedEventListeners = new Vector();
  }

  /**
   * Returns the socket where the messages are coming.
   *
   * @return the socket where the messages are coming
   */
  public DsSocket getSocket() {
    return socket;
  }

  /**
   * Tells whether this message reader is SSL/TLS enabled. In other words, whether the messages are
   * coming through the SSL/TLS transport
   *
   * @return <code>true</code> if the messages are coming through the SSL/TLS transport
   */
  public boolean isSslEnabled() {
    return bindingInfo.getTransport() == DsSipTransportType.TLS;
  }

  /** Starts this thread to start reading and processing incoming messages. */
  void doStart() {
    new Thread(this, "DsMessageReader-" + m_readerId++).start();
  }

  /**
   * Frame the next message from the stream.
   *
   * @param str the input stream to read from
   * @return a single framed message
   * @throws EOFException if the underlying input stream throws this exception
   * @throws IOException if the underlying input stream throws this exception
   */
  protected abstract byte[] frameMsg(InputStream str) throws EOFException, IOException;

  /**
   * Given a copy of the binding info and the byte array, create a unit of work to be enqueued.
   *
   * @param bytes the framed bytes of the message
   * @param binfo a copy of this reader's binding information
   * @return the created message bytes object
   */
  protected abstract DsMessageBytes createMessageBytes(byte[] bytes, DsBindingInfo binfo);

  /**
   * Read a message from the underlying stream and enqueue it. The base class implementation simply
   * calls frameMsg, then createMessageBytes.
   *
   * @param info a copy of this reader's binding information.
   * @return the unit of work that was created.
   * @throws EOFException if the underlying input stream throws this exception.
   * @throws IOException if the underlying input stream throws this exception.
   */
  protected DsUnitOfWork doRead(DsBindingInfo info) throws IOException, EOFException {
    byte msgBytes[] = frameMsg(stream);

    // GOGONG 10/20/05 Wire log message when receiving it via tcp
    if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
      DsLog4j.wireCat.log(
          Level.INFO,
          "Received message on "
              + info
              + "\n "
              + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput((new String(msgBytes))));
    } else if (DsLog4j.inoutCat.isEnabled(Level.DEBUG)) {
      DsLog4j.logInOutMessage(true, info, msgBytes);
    }

    return createMessageBytes(msgBytes, info);
  }

  // This setter is to set the sslHandshakeTimer for the DsSipIncomingTlsHandshakeTimeoutTest and
  // not to be used by other code
  public static void setSslHandshakeTimer(Timer timer) {
    sslHandshakeTimer = timer;
  }

  /** Runs this thread to start reading and processing incoming messages. Calls doRead in a loop. */
  public void run() {
    TimerTask task = null;
    try {
      // Check if this reader would be reading from the TLS/SSL socket.
      // If so then we would need to get the SSL Session info from the
      // SSL Socket and set that info to Binding Info.
      // This session info in the BindingInfo is being used by the logging
      // code to check for the peer information.
      if (isSslEnabled()) {
        // Instantiate the Cleaner Timer, if not already instantiated
        synchronized (lock) {
          if (sslHandshakeTimer == null) {
            // GOGONG 07.31.06 CSCsd90062 - Creates a new timer whose associated thread will be
            // specified to run as a daemon.
            sslHandshakeTimer = new Timer(true);
          }
        }
        // schedule the cleanup timer task
        task =
            new TimerTask() {
              public void run() {
                if (!handshakeDone) {
                  try {
                    String socketInfo = null;
                    long handshakeTimeout = -1;
                    if (socket != null) {
                      socketInfo = socket.getSocketInfo();
                      handshakeTimeout = ((DsSSLSocket) socket).getHandShaketimeout();
                    }
                    DsLog4j.wireCat.error(
                        "Closing socket because of tlshandshaketimeout"
                            + "\t"
                            + handshakeTimeout
                            + "for "
                            + socketInfo,
                        new Exception(("Closing socket (TLS handshake timeout)")));
                    stream.close();
                    // Also close the socket
                    socket.close();
                  } catch (Exception ioe) {
                    if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
                      DsLog4j.wireCat.error("Exception while closing the socket:", ioe);
                    }
                  }
                }
              }
            };
        sslHandshakeTimer.schedule(task, ((DsSSLSocket) socket).getHandShaketimeout());
        try {
          DsSSLSocket sslSocket = (DsSSLSocket) socket;
          DsSSLBindingInfo sslBinding = (DsSSLBindingInfo) bindingInfo;
          bindingInfo.updateBindingInfo(socket);
          sslSocket.startHandshake();

          sslBinding.setSession(sslSocket.getSession());
          DsTlsUtil.setPeerVerificationStatus(sslSocket.getSession(), sslBinding, true);
        } catch (ClassCastException cce) {
          if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
            DsLog4j.wireCat.error(
                "SSLSocket or SSL Binding Info is not being used in TLS/SSL reader", cce);
          }
          // do nothing and continue. The session info will not be available.
        }

        // we can just cancel the timer as we know the handshake is done
        // at this point.
        task.cancel();
        // set the flag.
        handshakeDone = true;
      } // _if

      // Now we just frame the messages here.  Then, they are put onto the
      // work queue, just like the UDP bytes.  They are removed from the queue
      // parsed, and then put through the Transaction Manager. - jsm
      while (true) {
        workQueue.execute(doRead((DsBindingInfo) bindingInfo.clone()));
      }
    }
    // CAFFEINE 2.0 DEVELOPMENT - catch more specific exception
    catch (DsEOFException e) {
      if (DsLog4j.wireCat.isEnabled(Level.WARN)) {
        if (e.getErrorCode() == DsEOFException.EC_FIRST_READ) {
          DsLog4j.wireCat.warn(
              "End of stream encountered. Most likely the connection is closed by the other side.");
        } else {
          String socketInfo = null;
          if (socket != null) socketInfo = socket.getSocketInfo();
          DsLog4j.wireCat.warn(
              "EOF exception while reading socket channel from socket "
                  + socketInfo
                  + " : closing  socket channel ",
              e);
        }
      }

      try {
        stream.close();
        // Also close the socket
        if (socket != null) socket.close();
      } catch (Exception ioe) {
        if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
          DsLog4j.wireCat.error("Exception while closing the socket:", ioe);
        }
      }

      notifySocketClosedEventListeners(this);

      return;
    } catch (EOFException e) {
      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
        String socketInfo = null;
        if (socket != null) socketInfo = socket.getSocketInfo();
        DsLog4j.wireCat.error(
            "EOF exception while reading socket channel from socket "
                + socketInfo
                + " : closing  socket channel ",
            e);
      }

      try {
        stream.close();
        // Also close the socket
        if (socket != null) socket.close();
      } catch (Exception ioe) {
        if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
          DsLog4j.wireCat.error("Exception while closing the socket:", ioe);
        }
      }

      notifySocketClosedEventListeners(this);
    } catch (IOException e) {
      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
        String socketInfo = null;
        if (socket != null) socketInfo = socket.getSocketInfo();
        String customMsg =
            "I/O exception while reading socket channel from socket, Binding Info : LocalAddress = "
                + bindingInfo.getLocalAddress()
                + " LocalPort = "
                + bindingInfo.getLocalPort()
                + " RemoteAddress = "
                + bindingInfo.getRemoteAddress()
                + " RemotePort = "
                + bindingInfo.getRemotePort()
                + " : closing  socket channel, Socket info : "
                + socketInfo;
        DsLog4j.wireCat.error(customMsg, e);
      }

      try {
        stream.close();
        // Also close the socket
        if (socket != null) socket.close();
      } catch (Exception ioe) {
        if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
          DsLog4j.wireCat.error("Exception while closing the socket:", ioe);
        }
      }

      notifySocketEventListeners(e);
    } catch (Throwable t) {
      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
        String socketInfo = null;
        if (socket != null) socketInfo = socket.getSocketInfo();
        DsLog4j.wireCat.error(
            "Exception while reading socket channel from socket "
                + socketInfo
                + " : closing  socket channel ",
            t);
      }

      try {
        stream.close();
        // Also close the socket
        if (socket != null) socket.close();
      } catch (Exception ioe) {
        if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
          DsLog4j.wireCat.error("Exception while closing the socket:", ioe);
        }
      }

      notifySocketEventListeners(new IOException(t.toString()));
    } finally {
      if (task != null) {
        task.cancel();
      }
    }
  }

  private void notifySocketClosedEventListeners(DsMessageReader source) {
    DsInputStreamClosedEvent closedEvent = new DsInputStreamClosedEvent(source);
    Object listeners[] = closedEventListeners.toArray();

    for (int i = 0; i < listeners.length; i++) {
      ((DsInputStreamEventListener) listeners[i]).onDsInputStreamClosedEvent(closedEvent);
    }
  }

  private void notifySocketEventListeners(IOException exc) {
    DsInputStreamErrorEvent event = new DsInputStreamErrorEvent(this, exc);
    Object listeners[] = socketListeners.toArray();

    for (int i = 0; i < listeners.length; i++) {
      // System.out.println("Notifying " + listeners [i]);
      ((DsInputStreamEventListener) listeners[i]).onDsInputStreamErrorEvent(event);
    }
  }

  /**
   * Add an input stream event listener.
   *
   * @param listener the input stream event listener
   */
  public void addDsInputStreamEventListener(DsInputStreamEventListener listener) {
    socketListeners.add(listener);
  }

  /**
   * Remove an input stream event listener.
   *
   * @param listener the input stream event listener
   */
  public void removeDsInputStreamEventListener(DsInputStreamEventListener listener) {
    socketListeners.remove(listener);
  }

  /**
   * Add an input stream connection closed event listener.
   *
   * @param listener the input stream event listener
   */
  public void addDsConnectionClosedEventListener(DsInputStreamEventListener listener) {
    closedEventListeners.add(listener);
  }

  /**
   * Add an input stream connection closed event listener.
   *
   * @param listener the input stream event listener
   */
  public void removeDsSipConnectionClosedEventListener(DsInputStreamEventListener listener) {
    closedEventListeners.remove(listener);
  }
}
