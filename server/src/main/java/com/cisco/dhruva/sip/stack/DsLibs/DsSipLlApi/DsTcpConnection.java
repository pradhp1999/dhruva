// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.
package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsPreParseData;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIoThreadPool;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageStatistics;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSocket;
import com.cisco.dhruva.util.saevent.ConnectionSAEventBuilder;
import com.cisco.dhruva.util.saevent.SAEventConstants;
import gnu.trove.TLinkable;
import gnu.trove.TLinkedList;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * TCP socket. This concrete connection can be constructed through the {@link
 * DsDefaultConnectionFactory DsDefaultConnectionFactory} by passing appropriate parameter like
 * transport type and address.
 */
public class DsTcpConnection extends DsAbstractConnection {
  /** The underlying output stream. */
  protected OutputStream tcpOutStream;
  /** The underlying socket. */
  protected DsSocket m_socket;

  /** The ordered list of messages to be sent over this connection. */
  protected TLinkedList m_sendQueue = new TLinkedList();

  /** The logger for connection information. */
  private static Logger cat = DsLog4j.connectionCat;
  // REFACTOR
  // protected static final Logger saLogger = LogManager.getLogger(REPMBeanImpl.saEventLoggerName);

  /** Static I/O Thread Pool for all TCP based connections. */
  private static DsIoThreadPool m_pool;

  /** The maximum number of message that can queue up before the connection is closed. */
  protected int m_maxQueueSize = 8000; // Setting this size > 0, in case we miss to set this.

  /** TLS maximum buffer queue* */
  protected int m_maxBuffer = 0;

  /** TLS buffer queue threshold* */
  protected int m_threshold = 0;

  /** last Notification sent time* */
  protected static long lastSendQNotificationTime = 0;

  /** <code>true</code> there is an I/O thread sending messages over this connection. */
  private volatile boolean m_isThreadWorking;

  private boolean m_sendQueueHighThresholdReached = false;

  /** <code>true</code> flag to hold threshold status. */
  protected boolean m_isThreshold = false;

  private static final int thresholdWindowInPercentage = 10;

  /** <code>true</code> if this connection has been closed because the queue overflowed. */
  //    private boolean m_isShutdown;

  /** Connection Closer thread counter. */
  private static int counter = 0;

  public static int consecutiveThreadDumpIntervalSeconds =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_QUEUE_ALARM_INTERVAL,
          DsConfigManager.PROP_QUEUE_ALARM_INTERVAL_DEFAULT);
  public static long lastThreadDumpGenTime = 0;

  /**
   * The flag that tells whether the SIP messages should go directly to the wire or should be put to
   * an output queue and then use one of the IO Thread from the IO Thread pool to send these
   * messages out to the wire.
   */
  private static final boolean m_noQueue =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NO_TCP_QUEUE, DsConfigManager.PROP_NO_TCP_QUEUE_DEFAULT);

  static {
    int minIoThreads =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_MIN_IO_THREADS, DsConfigManager.PROP_MIN_IO_THREADS_DEFAULT);
    m_pool = new DsIoThreadPool(minIoThreads);
    // Set this reference to the DsConfigManager so that it can be used to
    // show the status of these threads to the user through CLI.
    DsConfigManager.setIOThreadPool(m_pool);
  }

  /** Protected default constructor for the derived classes. */
  protected DsTcpConnection() {}

  /**
   * Closes the underlying socket.
   *
   * @throws IOException if there is an error while closing the socket or if the socket is already
   *     closed
   */
  public synchronized void closeSocket() throws IOException {
    int localPort;
    if (m_socket != null) {
      localPort = m_socket.getLocalPort();
      m_socket.close();
      // notify the socket get closed
      // localport is coming zero if we use bindinginfo so socket is
      // used and if we use socket for local ip its coming zero so
      // bindinginfo is used
      ConnectionSAEventBuilder.logConnectionEvent(
          SAEventConstants.DISCONNECT,
          SAEventConstants.TLS,
          null,
          m_bindingInfo.getLocalAddress(),
          localPort,
          m_bindingInfo.getRemoteAddress(),
          m_bindingInfo.getRemotePort());

      m_socket = null;
    }
  }

  /**
   * Returns the underlying socket.
   *
   * @return the underlying socket.
   */
  public DsSocket getSocket() {
    return m_socket;
  }

  /**
   * Constructs a TCP connection based on the specified binding info <code>binding</code>.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpConnection(DsBindingInfo binding) throws IOException, SocketException {
    this(
        binding.getLocalAddress(),
        binding.getLocalPort(),
        binding.getRemoteAddress(),
        binding.getRemotePort(),
        binding.getNetwork());
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpConnection(InetAddress anInetAddress, int aPortNo)
      throws IOException, SocketException {
    this(
        null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, anInetAddress, aPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpConnection(InetAddress anInetAddress, int aPortNo, DsNetwork network)
      throws IOException, SocketException {
    this(null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, anInetAddress, aPortNo, network);
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>. It also binds the datagram socket locally to the
   * specified local address <code>lInetAddress</code> and local port number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpConnection(
      InetAddress lInetAddress, int lPort, InetAddress anInetAddress, int aPortNo)
      throws IOException, SocketException {
    this(lInetAddress, lPort, anInetAddress, aPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a TCP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>. It also binds the datagram socket locally to the
   * specified local address <code>lInetAddress</code> and local port number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsNetwork network)
      throws IOException, SocketException {
    init(lInetAddress, lPort, anInetAddress, aPortNo, network);
  }

  protected void init(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsNetwork network)
      throws IOException {
    try {
      if (m_bindingInfo == null) {
        m_bindingInfo =
            new DsBindingInfo(lInetAddress, lPort, anInetAddress, aPortNo, DsSipTransportType.TCP);
      }
      m_bindingInfo.setNetwork(network);

      m_socket = new DsSocket(lInetAddress, lPort, anInetAddress, aPortNo, network);
      m_bindingInfo.updateBindingInfo(m_socket);
      tcpOutStream = m_socket.getOutputStream();
      m_socket.setTCPWriteTimeout();

      updateTimeStamp();

      if (network == null) {
        network = DsNetwork.getDefault();
      }
      m_maxQueueSize = network.getMaxOutputQueueSize();
      m_maxBuffer = network.getMaxbuffer();
      m_threshold = network.getThreshold();

    } catch (IOException e) {
      ConnectionSAEventBuilder.logConnectionErrorEvent(
          e.getMessage(),
          SAEventConstants.TCP,
          m_bindingInfo.getLocalAddress(),
          m_bindingInfo.getLocalPort(),
          m_bindingInfo.getRemoteAddress(),
          m_bindingInfo.getRemotePort(),
          SAEventConstants.OUT);
      // REFACTOR
      //      DsProxyTcpConnectException tcpConnectException =
      //          new DsProxyTcpConnectException(e, m_bindingInfo);
      //      e.addSuppressed(tcpConnectException);
      throw e;
    }
  }

  public void initiateConnect() throws IOException, SocketException {
    if (m_socket != null) {
      if (DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionCat.log(
            Level.ERROR, "Connection already connected [ " + m_bindingInfo + "]");
      }
      return;
    }
    init(
        m_bindingInfo.getLocalAddress(),
        m_bindingInfo.getLocalPort(),
        m_bindingInfo.getRemoteAddress(),
        m_bindingInfo.getRemotePort(),
        m_bindingInfo.getNetwork());
  }

  /**
   * Constructs a connecting TCP connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @param doConnect if it is false create connecting DsConnection else create normal DsConnection
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsTcpConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsNetwork network,
      boolean doConnect)
      throws IOException, SocketException {
    if (doConnect) {
      init(lInetAddress, lPort, anInetAddress, aPortNo, network);
    } else {
      m_isConnecting = true;
      m_bindingInfo =
          new DsBindingInfo(lInetAddress, lPort, anInetAddress, aPortNo, DsSipTransportType.TCP);
      m_bindingInfo.setNetwork(network);
      m_socket = null;
      tcpOutStream = null;
      updateTimeStamp();
      if (network == null) {
        network = DsNetwork.getDefault();
      }
      m_maxQueueSize = network.getMaxOutputQueueSize();
    }
  }

  /**
   * Constructs a TCP connection based on the specified socket.
   *
   * @param socket a DsSSLSocket object
   * @throws IOException if there is an error with the underlying socket
   */
  DsTcpConnection(DsSocket socket) throws IOException {
    tcpOutStream = socket.getOutputStream();
    socket.setTCPWriteTimeout();
    m_bindingInfo =
        new DsBindingInfo(
            socket.getLocalAddress(),
            socket.getLocalPort(),
            socket.getRemoteInetAddress(),
            socket.getRemotePort(),
            DsSipTransportType.TCP);

    DsNetwork network = socket.getNetwork();
    m_bindingInfo.setNetwork(network);
    m_socket = socket;
    updateTimeStamp();

    if (network == null) {
      network = DsNetwork.getDefault();
    }
    m_maxQueueSize = network.getMaxOutputQueueSize();
    m_maxBuffer = network.getMaxbuffer();
    m_threshold = network.getThreshold();
  }

  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public void send(byte buffer[]) throws IOException {
    updateTimeStamp();

    if (m_noQueue) {
      sendNoQueue(buffer);
    } else {
      addMsgToQueue(buffer, null);
    }
  }

  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException {
    // TODO: exception here if addr and port don't match
    send(buffer);
  }

  public boolean triggerGenerateThreadDump(String threadDumpInfo) {
    long timeDiffSeconds = System.currentTimeMillis() / 1000 - lastThreadDumpGenTime;
    boolean threadDumpStatus = false;
    // REFACTOR
    //    if (timeDiffSeconds > consecutiveThreadDumpIntervalSeconds) {
    //      threadDumpStatus = JVMManagement.getInstance().writeThreadDumpToFile(threadDumpInfo);
    //      lastThreadDumpGenTime = System.currentTimeMillis() / 1000;
    //    } else
    //      DsLog4j.connectionCat.debug(
    //          "Time gap between threaddump generation is "
    //              + timeDiffSeconds
    //              + ". Hence not generating threaddump");
    return threadDumpStatus;
  }

  /**
   * For DsIoThreads to use to process the sending of message asynchronously. This method should not
   * be called other than from the DsIoThread main loop. This method synchronizes on the send queue.
   */
  public Runnable getQueuedMsg() {
    // We don't need to process further messages, if any,
    // if the connection is in shut down mode.
    if (isShutingDown()) return null;

    synchronized (m_sendQueue) {
      if (m_sendQueue.size() == 0) {
        m_isThreadWorking = false;
        return null;
      }
      Runnable msg = (Runnable) m_sendQueue.removeFirst();
      /*
       if threshold limit is already reached and buffer queue drops below 90% of threshold,
       create a info Alarm
      */
      if (m_isThreshold == true
          && m_sendQueue.size() < (int) (m_threshold - m_threshold / thresholdWindowInPercentage)) {
        m_isThreshold = false;
        DsLog4j.connectionCat.info(
            "ConnectionSendQueueReachedLowerThreshold : TCP/TLS Send Buffer Queue size dropped down to lower threshold limit. currentBufferQueueSize = "
                + m_sendQueue.size()
                + ", lowThresholdMark = "
                + (int) (m_threshold - m_threshold / thresholdWindowInPercentage)
                + ".");

        //
        // REFACTOR
      }
      return msg;
    }
  }

  /**
   * For enqueuing message to be send from a DsIoThread in the future. This method synchronizes on
   * the send queue.
   *
   * @param buffer the message to be sent
   * @param txn the transaction associated with this message, use <code>null</code> if stateless
   * @throws IOException if the queue is full (or the connection was shutdown). If this happens, the
   *     contents of the queue will be logged and the connection closed.
   */
  protected void addMsgToQueue(byte[] buffer, DsSipTransaction txn) throws IOException {
    MessageToSend msg = new MessageToSend(buffer, txn);

    boolean newThread = false;

    if (m_isShutdown) {
      throw new IOException("This connection was already shutdown due to an earlier error.");
    }

    synchronized (m_sendQueue) {
      if (m_isShutdown) {
        throw new IOException("This connection was already shutdown due to an earlier error.");
      }
      DsLog4j.connectionCat.info(
          "Total size is  "
              + m_maxBuffer
              + ", Threshold is  "
              + m_threshold
              + ", Current size : "
              + m_sendQueue.size());
      // if size of the sendQueue reached maxBuffer size, send a Notification
      if (m_sendQueue.size() == m_maxBuffer) {
        try {
          /*
           * Remove the connection before marking the connection as shutdown.
           */
          DsSipTransactionManager.getTransportLayer().removeConnection(this);
        } catch (Exception e2) {
          if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
            DsLog4j.connectionCat.warn("Exception removing connection ", e2);
          }
        }
        // set the shutdown flag so there will be no more message on this connection
        DsLog4j.connectionCat.error(
            "ConnectionSendQueueReachedMaxBufferLimit : TCP/TLS Send Buffer Queue size reached Max size. currentBufferQueueSize = "
                + m_sendQueue.size()
                + ", highThresholdMark = "
                + m_maxBuffer
                + ".");
        // REFACTOR
        //        try {
        //          Notification n =
        //              new Notification(
        //                  "TLSQueueEvent",
        //                  "ConnectionSendQueueReachedMaxBufferLimit object",
        //                  1,
        //                  "TCP/TLS Send Buffer Queue size reached Max size. currentBufferQueueSize
        // = "
        //                      + m_sendQueue.size()
        //                      + ", highThresholdMark = "
        //                      + m_threshold
        //                      + ", maxBufferQueueSize = "
        //                      + m_maxBuffer
        //                      + ".");
        //          String threadDumpInfo = "TLSSendBuffer" + UUID.randomUUID().toString();
        //          boolean threadDumpStatus = triggerGenerateThreadDump(threadDumpInfo);

        //          TLSQueueEventDataParam dataParam =
        //              new TLSQueueEventDataParam.Builder()
        //                  .eventType("TLSQueueOverFlow")
        //                  .eventInfo(QueueMBeanImpl.maxLength)
        //                  .queueName("TLS Send Buffer Queue")
        //                  .queueSize(m_sendQueue.size())
        //                  .threshold(m_threshold)
        //                  .queueMaxSize(m_maxBuffer)
        //                  .transport(
        //                      DsStringConversion.getProtocolConversionToString(
        //                          m_bindingInfo.getTransport()))
        //                  .bindingInfo(
        //                      "Local Address : "
        //                          + m_bindingInfo.getLocalAddress().toString().replaceAll("/", "")
        //                          + "("
        //                          + m_bindingInfo.getLocalPort()
        //                          + "), Remote Address : "
        //                          + m_bindingInfo.getRemoteAddress().toString().replaceAll("/",
        // "")
        //                          + "("
        //                          + m_bindingInfo.getRemotePort()
        //                          + ")")
        //                  .threadDumpStatus(threadDumpStatus)
        //                  .build();
        //          if (threadDumpStatus) {
        //            dataParam.setThreadDumpInfo(threadDumpInfo);
        //          }
        //          n.setUserData(dataParam);
        //          QueueMBeanImpl.getInstance().sendNotification(n);
        //        } catch (NotCompliantMBeanException | MalformedObjectNameException |
        // OpenDataException e) {
        //          DsLog4j.connectionCat.info("TLS Buffer Queue Threshold Notification sending
        // failed...");
        //        }
        m_isShutdown = true;

        startCleaner();

        throw new IOException("TCP Send Queue is full, connection in shutdown mode.");
      }

      m_sendQueue.addLast(msg);
      /*
       if threshold alarm is cleared and TLS Buffer Queue reaches threshold then create an Alarm
      */
      if (m_isThreshold == false && m_sendQueue.size() >= m_threshold) {
        DsLog4j.connectionCat.error(
            "ConnectionSendQueueReachedHigherThreshold : TCP/TLS Send Buffer Queue size reached higher threshold limit. currentBufferQueueSize = "
                + m_sendQueue.size()
                + ", highThresholdMark = "
                + m_threshold
                + ".");
        //        try {
        //          Notification n =
        //              new Notification(
        //                  "TLSQueueEvent",
        //                  "ConnectionSendQueueReachedHigherThreshold object",
        //                  1,
        //                  "TCP/TLS Send Buffer Queue size reached higher threshold limit.
        // currentBufferQueueSize = "
        //                      + m_sendQueue.size()
        //                      + ", highThresholdMark = "
        //                      + m_threshold
        //                      + ", maxBufferQueueSize = "
        //                      + m_maxBuffer
        //                      + ".");
        //          String threadDumpInfo = "TLSSendBuffer" + UUID.randomUUID().toString();
        //          boolean threadDumpStatus = triggerGenerateThreadDump(threadDumpInfo);
        //
        //          TLSQueueEventDataParam dataParam =
        //              new TLSQueueEventDataParam.Builder()
        //                  .eventType("TLSQueueThresholdExceeded")
        //                  .eventInfo(QueueMBeanImpl.highThreshold)
        //                  .queueName("TLS Send Buffer Queue")
        //                  .queueSize(m_sendQueue.size())
        //                  .threshold(m_threshold)
        //                  .queueMaxSize(m_maxBuffer)
        //                  .transport(
        //                      DsStringConversion.getProtocolConversionToString(
        //                          m_bindingInfo.getTransport()))
        //                  .bindingInfo(
        //                      "Local Address : "
        //                          + m_bindingInfo.getLocalAddress().toString().replaceAll("/", "")
        //                          + "("
        //                          + m_bindingInfo.getLocalPort()
        //                          + "), Remote Address : "
        //                          + m_bindingInfo.getRemoteAddress().toString().replaceAll("/",
        // "")
        //                          + "("
        //                          + m_bindingInfo.getRemotePort()
        //                          + ")")
        //                  .threadDumpStatus(threadDumpStatus)
        //                  .build();
        //          if (threadDumpStatus) {
        //            dataParam.setThreadDumpInfo(threadDumpInfo);
        //          }
        //
        //          n.setUserData(dataParam);
        //          QueueMBeanImpl.getInstance().sendNotification(n);
        //        } catch (NotCompliantMBeanException | MalformedObjectNameException |
        // OpenDataException e) {
        //          DsLog4j.connectionCat.info("TLS Buffer Queue Threshold Notification sending
        // failed...");
        //        }
        m_isThreshold = true;
      }

      DsLog4j.connectionCat.info("Message is added, size is  " + m_sendQueue.size());

      if (!m_isThreadWorking) {
        m_isThreadWorking = true;
        newThread = true;
      }
    }

    // there was no thread working this connection, so we need to create a new one
    // and send it on its way.
    if (newThread) {
      m_pool.assign(this);
    }

    // otherwise there is already a thread working on this connection, and it will pick
    // up this new work when it finishes the old work.
  }

  void startCleaner() {
    Thread cleaner =
        new Thread("Connection-Closer-" + counter++) {
          public void run() {
            cleanup(new IOException("TCP Send Queue is full, connection closed."));
          }
        };
    cleaner.start();
  }

  private void cleanup(IOException e) {
    try {
      synchronized (m_sendQueue) {
        if (m_isShutdown == false) {
          /*
           * This will happen when it is called from DsTcpConnection.MessageToSend.run
           * exception path.
           */
          try {
            /*
             * Remove the connection before marking the connection as shutdown.
             */
            DsSipTransactionManager.getTransportLayer().removeConnection(this);
          } catch (Exception e2) {
            if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
              DsLog4j.connectionCat.warn("Exception removing connection ", e2);
            }
          }
        }

        // set the shutdown flag so there will be no more message on this connection
        m_isShutdown = true;
      }

      MessageToSend msg;
      while (m_sendQueue.size() > 0) {
        synchronized (m_sendQueue) {
          msg = (MessageToSend) m_sendQueue.removeFirst();
        }
        msg.abort(e);

        byte[] msgBytes = msg.getMsg();

        try {
          DsPreParseData ppData = DsSipMsgParser.preParse(msgBytes);

          if (ppData.isRequest()) {
            DsMessageStatistics.logRequest(
                DsMessageLoggingInterface.REASON_STREAM_CLOSED,
                DsMessageLoggingInterface.DIRECTION_OUT,
                msgBytes,
                DsSipMsgParser.getMethod(ppData.getMethod()),
                m_bindingInfo);
          } else {
            DsMessageStatistics.logResponse(
                DsMessageLoggingInterface.REASON_STREAM_CLOSED,
                DsMessageLoggingInterface.DIRECTION_OUT,
                msgBytes,
                ppData.getResponseCode().parseInt(),
                DsSipConstants.UNKNOWN,
                m_bindingInfo);
          }
        } catch (DsSipParserException pe) {
          DsMessageStatistics.logRequest(
              DsMessageLoggingInterface.REASON_STREAM_CLOSED,
              DsMessageLoggingInterface.DIRECTION_OUT,
              msgBytes,
              DsSipConstants.UNKNOWN,
              m_bindingInfo);
        }
      }
    } finally {
      // Connection is already removed from the table.
    }
  }

  private void sendNoQueue(byte[] buffer) throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG, new StringBuffer("Sending Message binding info: ").append(m_bindingInfo));
    }

    if (DsDebugTransportImpl.set()) {
      InetAddress laddr = m_bindingInfo.getLocalAddress();
      int lport = m_bindingInfo.getLocalPort();
      InetAddress addr = m_bindingInfo.getRemoteAddress();
      int port = m_bindingInfo.getRemotePort();
      DsDebugTransportImpl.messageOut(
          DsDebugTransport.POS_CONNECTION,
          m_bindingInfo.getTransport(),
          buffer,
          laddr,
          lport,
          addr,
          port);
    }
    // GOGONG 10/20/05 Wire log message when sending it via tcp
    if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
      DsLog4j.wireCat.log(
          Level.INFO,
          "Sending message on "
              + m_bindingInfo
              + "\n"
              + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(
                  DsByteString.newString(buffer)));
    } else if (DsLog4j.inoutCat.isEnabled(Level.INFO)) {
      DsLog4j.logInOutMessage(false, m_bindingInfo, buffer);
    }

    synchronized (tcpOutStream) {
      /*
       * Calculate the write operation time by getting the current time
       * before and after write. if write operation time is exceeding the
       * DsSocket.TCP_WRITE_TIMEOUT (i.e SO_SNDTIMEO) then throw
       * IOException to close the connection.
       */
      long preWriteTime = TimeUnit.MILLISECONDS.toMillis(new Date().getTime());
      long postWriteTime = 0;
      int writeTime = 0;
      try {
        tcpOutStream.write(buffer);
      } catch (SocketException se) {
        /*
         * one of the reason for write throwing SocketException is due
         * to SO_SNDTIMEO, Resource temporarily unavailable (Write
         * failed) when peer closes the connection.
         */
        postWriteTime = TimeUnit.MILLISECONDS.toMillis(new Date().getTime());
        writeTime = (int) (postWriteTime - preWriteTime);
        if (DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
          DsLog4j.connectionCat.error(
              "Failed to write the message on connection [ "
                  + m_socket.getSocketInfo()
                  + " ]"
                  + " Socket write duration [ "
                  + writeTime
                  + " ]",
              se);
        }
        if (isSocketWriteTimeOut(writeTime)) {
          sendSocketWriteTimeOutNotification(writeTime, se);
        }
        throw se;
      }
      postWriteTime = TimeUnit.MILLISECONDS.toMillis(new Date().getTime());
      writeTime = (int) (postWriteTime - preWriteTime);
      if (isSocketWriteTimeOut(writeTime)) {
        sendSocketWriteTimeOutNotification(writeTime, null);
      }
      tcpOutStream.flush();
    }
  }

  private boolean isSocketWriteTimeOut(int writeTime) {
    return ((DsSocket.SET_TCP_WRITE_TIMEOUT) && (writeTime >= DsSocket.TCP_WRITE_TIMEOUT * 1000));
  }

  private void sendSocketWriteTimeOutNotification(int writeTime, IOException e) throws IOException {
    int bytesInSendQueue = m_socket.getBytesInSocketSendBuffer();
    String[] alarmParams = new String[2];
    alarmParams[0] = "SocketWriteTimeout";
    alarmParams[1] =
        "Failed to write the message on connection [ "
            + m_socket.getSocketInfo()
            + " ]"
            + " bytes in socket send buffer [ "
            + bytesInSendQueue
            + " ]"
            + " Socket write duration [ "
            + writeTime
            + " ]";
    // REFACTOR
    //    try {
    //      SIPMBeanImpl.sendNotification(SAEventConstants.SOCKET_WRITE_TIMEDOUT, alarmParams);
    //    } catch (Exception ex) {
    //      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
    //        DsLog4j.wireCat.log(
    //            Level.ERROR,
    //            "Error in sending socket write timeout alarm [ " + m_socket.getSocketInfo() + "
    // ]",
    //            ex);
    //      }
    //    }
    if (e != null) {
      throw e;
    } else {
      throw new IOException(
          "Failed to write the message on connection [ "
              + m_socket.getSocketInfo()
              + " ]"
              + " bytes in socket send buffer [ "
              + bytesInSendQueue
              + " ]"
              + " Socket write duration [ "
              + writeTime
              + " ]");
    }
  }

  class MessageToSend implements TLinkable, Runnable {
    private byte[] m_msg;
    private DsSipTransaction m_txn;

    private TLinkable m_next;
    private TLinkable m_previous;

    public MessageToSend(byte[] msg, DsSipTransaction txn) {
      m_msg = msg;
      m_txn = txn;
    }

    public void run() {
      try {
        sendNoQueue(m_msg);
      } catch (IOException e) {
        if (cat.isEnabled(Level.WARN)) {
          cat.warn(
              "IOException while Sending Message binding info for socket "
                  + m_bindingInfo
                  + "exception:"
                  + e);
        }
        abort(e);
        cleanup(e);
      } catch (Exception e) {
        if (cat.isEnabled(Level.WARN)) {
          cat.warn(
              " Exception while Sending Message binding info for socket "
                  + m_bindingInfo
                  + "exception:"
                  + e);
        }
        IOException ioe = new IOException(e.getMessage());
        abort(ioe);
        cleanup(ioe);
      }
    }

    // for logging
    protected byte[] getMsg() {
      return m_msg;
    }

    public void abort(IOException e) {
      if (m_txn != null) {
        m_txn.onIOException(e);
      }
    }

    public final TLinkable getNext() {
      return m_next;
    }

    public final TLinkable getPrevious() {
      return m_previous;
    }

    public final void setNext(TLinkable linkable) {
      m_next = linkable;
    }

    public final void setPrevious(TLinkable linkable) {
      m_previous = linkable;
    }
  }
}
