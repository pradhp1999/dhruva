// Copyright (c) 2008-2011 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipBufferStream;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLSession;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * TLS socket. This connection represents a Non-Blocking TLS connection and uses "java.nio" package
 * from JDK v1.4.x to provide Non-Blocking behaviour. It also requires SSL packages introduced in
 * JDK 1.5.
 *
 * <p>This concrete connection can be constructed through the {@link DsDefaultConnectionFactory
 * DsDefaultConnectionFactory} by passing appropriate parameter like transport type and address.
 */
public class DsTlsNBConnection extends DsTcpNBConnection {
  /** The logger for connection information. */
  private static Logger cat = DsLog4j.connectionCat;

  private static AtomicLong connectionFailureCount = new AtomicLong(0);
  private static AtomicLong readWriteFailureCount = new AtomicLong(0);
  private DsSSLContext m_context;
  private DsSSLEngine m_engine;
  private AtomicBoolean addedToConnectionTable = new AtomicBoolean(false);
  private static final boolean IS_CRLCHECK_ENABLED =
      DsConfigManager.getProperty(
          DsConfigManager.IS_CRLCHECK_ENABLED, DsConfigManager.IS_CRLCHECK_ENABLED_DEFAULT);
  /** Protected default constructor for the derived classes. */
  protected DsTlsNBConnection() {
    super();
  }

  public void setDsSSLEngine(DsSSLEngine dsSSLEngine) {
    this.m_engine = dsSSLEngine;
  }

  public void setDsSSLContext(DsSSLContext dsSSLContext) {
    this.m_context = dsSSLContext;
  }

  /**
   * Closes the underlying socket (channel). Also removes it from the connection table.
   *
   * @throws IOException this implementation of the interface DOES NOT throw the IOExcpetion if
   *     there is an error while closing the socket or if the socket is already closed. It simply
   *     logs the occurrence since we are no longer interested in using the socket.
   */
  public void closeSocket() throws IOException {
    int localPort;
    if (m_channel != null) {

      String socketInfo = null;
      try {
        if (m_socket != null) {
          socketInfo = m_socket.getSocketInfo();
          localPort = m_socket.getLocalPort();

          // this close will remove the socket from the Selector
          m_channel.close();
          // Information for connection get closed
          // localport is coming zero if we use bindinginfo so socket is
          // used and if we use socket for local ip its coming zero so
          // bindinginfo is used
          // TODO saevent-restructure log a ConnectionEvent here to indicate disconnect
        }
        if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
          DsLog4j.connectionCat.info("closeSocket() - socket" + socketInfo + " closed");
        }
      } catch (Exception e) {
        // ignore any exceptions, since we are done anyway
        if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
          DsLog4j.connectionCat.info(
              "closeSocket() - exception closing socket: " + socketInfo + "Exception is :", e);
        }
      }

      m_channel = null;
      m_socket = null;

      m_transportLayer.m_ConnectionTable.remove(this);
    } else {
      if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
        DsLog4j.connectionCat.info("closeSocket() - channel already null,");
      }
    }
  }

  /**
   * Constructs a TLS connection based on the specified binding info <code>binding</code>.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @param context the SSL context that should be used to construct the SSL socket.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TLS socket
   */
  protected DsTlsNBConnection(DsBindingInfo binding, DsSSLContext context)
      throws IOException, SocketException {
    this(
        binding.getLocalAddress(),
        binding.getLocalPort(),
        binding.getRemoteAddress(),
        binding.getRemotePort(),
        context,
        binding.getNetwork());
  }

  /**
   * Constructs a TLS connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>.
   *
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TLS socket
   */
  protected DsTlsNBConnection(
      InetAddress rInetAddress, int rPortNo, DsSSLContext context, DsNetwork network)
      throws IOException, SocketException {
    this(null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, rInetAddress, rPortNo, context, network);
  }

  /**
   * Constructs a TLS connection to the specified remote address <code>rInetAddress</code> and the
   * remote port number <code>rPortNo</code>. It also binds the datagram socket locally to the
   * specified local address <code>lInetAddress</code> and local port number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TLS socket
   */
  protected DsTlsNBConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress rInetAddress,
      int rPortNo,
      DsSSLContext context,
      DsNetwork network)
      throws IOException, SocketException {
    this();
    init(lInetAddress, lPort, rInetAddress, rPortNo, context, network);
  }

  /**
   * Constructs a TLS connection to the specified remote address <code>rInetAddress</code> and the
   * remote port number <code>rPortNo</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @param doConnect <code>true</code> to connect to the destination
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TLS socket
   */
  protected DsTlsNBConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress rInetAddress,
      int rPortNo,
      DsSSLContext context,
      DsNetwork network,
      boolean doConnect)
      throws IOException, SocketException {
    this();
    if (doConnect) {
      init(lInetAddress, lPort, rInetAddress, rPortNo, context, network);
    } else {
      m_isConnecting = true;
      m_context = context;
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "DsTlsNBConnection():begin");
        DsLog4j.connectionCat.log(
            Level.DEBUG,
            "DsTlsNBConnection(): lInetAddress: "
                + lInetAddress
                + "; lPort: "
                + lPort
                + "; rInetAddress "
                + rInetAddress
                + "; rPortNo"
                + rPortNo);
      }
      m_channel = null;
      m_socket = null;
      m_bindingInfo =
          new DsBindingInfo(lInetAddress, lPort, rInetAddress, rPortNo, DsSipTransportType.TLS);
      m_bindingInfo.setNetwork(network);
      updateTimeStamp();
    }
  }

  /**
   * Constructs a TLS connection based on the specified socket. this will be pending connection, i.e
   * only tcp channel will be established
   *
   * @param socket a DsSocket object
   * @throws IOException if there is an error with the underlying socket
   */
  DsTlsNBConnection(DsSocket socket, DsSSLContext context) throws IOException {

    this();
    m_socket = socket;
    m_context = context;
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTlsNBConnection(socket):begin");
    }

    m_bindingInfo =
        new DsSSLBindingInfo(
            socket.getLocalAddress(),
            socket.getLocalPort(),
            socket.getRemoteInetAddress(),
            socket.getRemotePort(),
            null);
    m_bindingInfo.setNetwork(socket.getNetwork());

    m_channel = socket.getSocket().getChannel();
    m_channel.configureBlocking(false);
    SSLEngine engine;
    SSLSession sslSession;
    updateTimeStamp();
    engine = m_context.getSSLEngine();
    cat.debug("DsTlsNBConnection(socket): created engine from context set");
    engine.setUseClientMode(false);
    m_engine = NetObjectsFactory.getDsSSLEngine(m_channel, engine);
    // get lock on m_engine to make sure that channel is closed if handshake fails
    // and no message is read from threads spawned by DsSelector for incoming
    // messages
    synchronized (m_engine) {
      try {

        m_engine.setHandshakeTimeout(m_socket.getNetwork().getTlsHandshakeTimeout());

        startHandshake(m_socket.getNetwork().getTlsHandshakeTimeout());
        // Reached here means handshake is completed, add this connection to connection
        // table if not added

        sslSession = m_engine.getSSLSession();
        ((DsSSLBindingInfo) m_bindingInfo).setSession(sslSession);
        DsTlsUtil.setPeerVerificationStatus(sslSession, (DsSSLBindingInfo) m_bindingInfo, true);
        if (!addedToConnectionTable.getAndSet(true)) {
          m_transportLayer.m_ConnectionTable.put(this);
          cat.debug(
              "DsTlsNBConnection:doRead() Added to connection table {}",
              () -> m_socket.getSocketInfo());
        }

        if (m_context.getNeedClientAuth()) {
          Certificate[] certs = sslSession.getPeerCertificates();
          X509Certificate clientCert = (X509Certificate) certs[0];
          cat.debug(
              "DsTlsNBConnection(): TLS Channel established for ["
                  + m_channel.getLocalAddress()
                  + " "
                  + m_channel.getRemoteAddress()
                  + "]");
          // TODO saevent-restructure log a TlsConnectionEvent here with peer certinfo
        } else {
          // TODO saevent-restructure log a TlsConnectionEvent here without peer certinfo
        }
      } catch (IOException e) {
        Throwable th = e;
        if (th.getCause() != null) {
          while (th.getCause() != null) {
            th = th.getCause();
          }
        }
        Exception rootException = new Exception(th);
        // TODO saevent-restructure log a TLSConnectionErrorEvent here
        if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
          DsLog4j.connectionCat.log(Level.WARN, "DsTlsNBConnection(): I/O Exception: ", e);
        }
        closeSocket();
        connectionFailureCount.incrementAndGet();
        throw e;
      }

      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "DsTlsNBConnection(socket):end");
      }
    }
  }
  /**
   * initiates handshake for pending outbound connection. Use this only after tcp connection is
   * established
   *
   * @throws IOException
   */
  public void initiateHandshake() throws IOException {
    SSLEngine engine;
    updateTimeStamp();
    try {
      this.lock();
      engine = m_context.getSSLEngine();
      cat.debug("DsTlsNBConnection(socket): created engine from context set");
      engine.setUseClientMode(false);
      m_engine = NetObjectsFactory.getDsSSLEngine(m_channel, engine);
      m_engine.setHandshakeTimeout(m_socket.getNetwork().getTlsHandshakeTimeout());
      startHandshake(m_socket.getNetwork().getTlsHandshakeTimeout());
      SSLSession sslSession = m_engine.getSSLSession();
      if (m_context.getNeedClientAuth()) {
        X509Certificate clientCert = (X509Certificate) sslSession.getPeerCertificates()[0];
        cat.debug(
            "DsTlsNBConnection(): TLS Channel established for ["
                + m_channel.getLocalAddress()
                + " "
                + m_channel.getRemoteAddress()
                + "]");
        // TODO saevent-restructure log a TLSConnectionEvent here with peercert info
      } else {
        // TODO saevent-restructure log a TLSConnectionEvent here without peercert info
      }

      // register the channel once handshake is completed

    } catch (IOException e) {
      Throwable th = e;
      if (th.getCause() != null) {
        while (th.getCause() != null) {
          th = th.getCause();
        }
      }
      Exception rootException = new Exception(th);
      // TODO saevent-restructure log a TLSConnectionErrorEvent here for handshake failure
      if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionCat.log(Level.WARN, "DsTlsNBConnection(): I/O Exception: ", e);
      }
      closeSocket();
      connectionFailureCount.incrementAndGet();
      throw e;
    } finally {
      this.unlock();
    }

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTlsNBConnection(socket):end");
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
        m_context,
        m_bindingInfo.getNetwork());
  }

  protected void init(
      InetAddress lInetAddress,
      int lPort,
      InetAddress rInetAddress,
      int rPortNo,
      DsSSLContext context,
      DsNetwork network)
      throws IOException {

    Socket socket = null;
    // fix for issue CSCsr74483 - Socket leak due to the Socket Exception.
    try {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "DsTlsNBConnection():begin");
        DsLog4j.connectionCat.log(
            Level.DEBUG,
            "DsTlsNBConnection(): lInetAddress: "
                + lInetAddress
                + "; lPort: "
                + lPort
                + "; rInetAddress "
                + rInetAddress
                + "; rPortNo"
                + rPortNo);
      }
      m_channel = NetObjectsFactory.getSocketChannel();
      socket = m_channel.socket();

      // bind has to be done before connect or else the connect will
      // cause an OS chosen bind to occur
      socket.bind(new InetSocketAddress(lInetAddress, lPort));
      // we could connect via selection process as reads and write are done
      // but we choose not to because it simply won't add to speed of getting
      // up a connection.

      // CSCto35150: TCP connection setup takes a long time to fail
      // call connect on the socket instead of the channel. this gives the ability to
      // set connection setup timeout.
      // m_channel.connect(new InetSocketAddress(rInetAddress, rPortNo));
      socket.connect(
          new InetSocketAddress(rInetAddress, rPortNo), network.getTcpConnectionTimeout());
      // TODO saevent-restructure log a ConnectionEvent here
      m_socket = new DsSocket(socket, network);
      if (m_bindingInfo == null) {
        m_bindingInfo =
            new DsBindingInfo(lInetAddress, lPort, rInetAddress, rPortNo, DsSipTransportType.TLS);
      }
      m_bindingInfo.setNetwork(network);
      m_bindingInfo.updateBindingInfo(m_socket);
      m_channel.finishConnect();
      m_channel.configureBlocking(false);

      SSLEngine engine;
      updateTimeStamp();

      if (m_context != null) {
        engine = m_context.getSSLEngine();
        cat.debug("DsTlsNBConnection(): created engine from existing context");
      } else {
        m_context = new DsSSLContext();
        engine = m_context.getSSLEngine();
        cat.error("DsTlsNBConnection(): created engine from new context");
      }
      engine.setUseClientMode(true);
      m_engine = NetObjectsFactory.getDsSSLEngine(m_channel, engine);
      m_engine.setHandshakeTimeout(network.getTlsHandshakeTimeout());

      startHandshake(network.getTlsHandshakeTimeout());
      // Since channel is registered after handshake is complete selector should not
      // spawn IOWorker threads for any data on the channel
      // If we have reached here, it means handshake is successful
      SSLSession sslSession = m_engine.getSSLSession();
      Certificate[] peerCertificates = sslSession.getPeerCertificates();
      if (peerCertificates != null && peerCertificates.length > 0) {
        X509Certificate serverCert = (X509Certificate) peerCertificates[0];
        cat.debug(
            "DsTlsNBConnection(): TLS Channel established for ["
                + m_channel.getLocalAddress()
                + " "
                + m_channel.getRemoteAddress()
                + "]");
        // Currently no direct way to get if serverAuth is enabled
        // TODO saevent-restructure log a TlsConnectionEvent here with peer certinfo
      } else {
        // TODO saevent-restructure log a TlsConnectionEvent here without peer certinfo
      }

      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "DsTlsNBConnection():end");
      }

    } catch (SocketException se) {
      // TODO saevent-restructure log a ConnectionErrorEvent here
      if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionCat.log(Level.WARN, "DsTlsNBConnection(): Socket Exception: ", se);
      }

      try {
        if (m_channel != null) {
          m_channel.close();
        }
      } catch (IOException ioe) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG, "DsTlsNBConnection(): I/O Exception on close: ", ioe);
        }
      }
      connectionFailureCount.incrementAndGet();
      // REFACTOR
      //      DsProxyTcpConnectException tcpConnectException =
      //          new DsProxyTcpConnectException(se, m_bindingInfo);
      //      se.addSuppressed(tcpConnectException);
      throw se;
    } catch (IOException e) {
      // send SAEvent for root cause
      connectionFailureCount.incrementAndGet();
      Throwable th = e;
      if (th.getCause() != null) {
        while (th.getCause() != null) {
          th = th.getCause();
        }
      }
      Exception rootException = new Exception(th);
      // TODO saevent-restructure log a TLSConnectionErrorEvent here
      if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionCat.log(Level.WARN, "DsTlsNBConnection(): I/O Exception: ", e);
      }
      if (m_channel != null) {
        m_channel.close();
      }
      // REFACTOR
      //      DsProxyError proxyError =
      //          new DsProxyTlsConnectException(
      //              rootException,
      //              context,
      //              lInetAddress,
      //              lPort,
      //              m_bindingInfo.getRemoteAddress(),
      //              m_bindingInfo.getRemotePort());
      //      if (proxyError != null) e.addSuppressed(proxyError);
      throw e;
    }
  }

  /** Get Connection Failure Count and reset the count to zero */
  public static long getTlsConnectionFailure() {
    long failures = connectionFailureCount.getAndSet(0);
    return failures;
  }

  /** Get Read Write Failures and reset the count to zero */
  public static long getTlsReadWriteFailure() {
    long failures = readWriteFailureCount.getAndSet(0);
    return failures;
  }

  /**
   * Async handshaking is initiated. The invoking thread is blocked till handshake is completed or
   * till handshake times out
   *
   * @param timeout set the handshake timeout
   */
  public void startHandshake(int timeout) throws IOException {
    synchronized (m_engine) {
      register();
      m_engine.read();
      if (m_engine.getHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING) {
        try {
          m_engine.wait(timeout);
          // can reach here if wait timeout, notified due to handshake completed or Some
          // handshake exception
        } catch (InterruptedException e) {
          throw new IOException("DsTlsNBConnection:Handshake Failure due to interruptedException");
        }
        if (m_engine.getHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING) {
          if (m_engine.getHandshakeException() != null) {
            throw m_engine.getHandshakeException();
          } else {
            cat.error("DsTlsNBConnection: Tls Handshake timed out");
            throw new IOException("DsTlsNBConnection: Tls Handshake Timeout");
          }
        } else if (m_engine.getHandshakeException() != null) {
          throw m_engine.getHandshakeException();
        }
      }
    }
  }
  /**
   * Sends the specified data buffer across the network through the underlying socket to the desired
   * destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public void send(byte buffer[]) throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          new StringBuffer("Sending Message binding info (NB): ").append(m_bindingInfo));
    }
    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.log(
          Level.INFO,
          new StringBuffer("Sending Message (NB): ")
              .append(
                  DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(
                      DsByteString.newString(buffer))));
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

    updateTimeStamp();

    IOException exc = null;
    synchronized (m_writeLock) {
      ByteBuffer bb = getNetBB();
      bb.clear();
      bb.put(buffer);
      // fix for issue CSCsr74483 - Socket leak due to the Socket Exception.
      try {
        // Try writing to the socket anyway. It will return immediately
        // regardless, whether any bytes are written to the socket.
        if (!doWrite(bb)) {
          DsSelector.addInterestOps(m_sk, SelectionKey.OP_WRITE);
        }
      } catch (IOException e) {
        exc = e;

        if (cat.isEnabled(Level.WARN)) {
          String socketInfo = null;
          if (m_socket != null) socketInfo = m_socket.getSocketInfo();
          cat.warn(
              "Exception while writing to the socket channel for socket "
                  + socketInfo
                  + " : closing socket channel ",
              exc);
        }

        try {
          closeSocket();
        } catch (Exception ioe) {
          if (cat.isEnabled(Level.INFO)) {
            cat.info("Exception while closing the channel:", ioe);
          }
        }
      }
    }

    // move the notify out of the critical section to avoid deadlock
    if (exc != null) {
      notifyListeners(new DsConnectionClosedEvent(this));
      throw exc;
    }
  }

  /**
   * Read the bytes from the network and queue them for processing. We do not expect to have
   * multiple threads calling this method at the same time, but it can happen so this method is now
   * synchronized.
   *
   * @throws Exception if there is an exception during reading of the socket channel or processing
   *     the message.
   */
  protected synchronized int doRead() throws Exception {
    int count;
    ByteBuffer bb = getNetBB();
    bb.clear();
    // get a lock on engine during handshake only
    synchronized (m_engine) {
      ByteBuffer decrypted = m_engine.getAppRecvBuffer();
      count = m_engine.read();
      decrypted.flip();
      bb.put(decrypted);
      decrypted.compact();
      bb.flip();
    }
    if (count < 0) {
      m_engine.close();
      m_sk.cancel();
      closeSocket();
    } else {

      if (m_queuedInBB != null) {
        m_queuedInBB.flip();
        bb = combineBytes(m_queuedInBB, bb);
      }
      if (m_engine.getHandshakeStatus() == HandshakeStatus.NOT_HANDSHAKING
          && !(addedToConnectionTable.getAndSet(true))) {
        m_transportLayer.m_ConnectionTable.put(this);
        cat.debug(
            "DsTlsNBConnection:doRead() Added to connection table {}",
            () -> m_socket.getSocketInfo());
      }
      frameMessages(bb);
    }
    return count;
  }

  /**
   * Frames SIP messages from the byte buffer that is passed in.
   *
   * @param bb the byte buffer from which to frame SIP messages.
   * @throws BufferUnderflowException when there are no more SIP messages to frame
   * @throws Exception if there is any other kind of exception while framing a message
   */
  protected void frameMessages(ByteBuffer bb) throws Exception {
    byte msg[];

    try {
      while ((msg = DsSipBufferStream.readMsg(bb)) != null) {

        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.debug("Composed Message:\n" + DsByteString.newString(msg));
        }

        DsSipTransportLayer.getWorkQueue()
            .execute(m_mbFactory.createMessageBytes(msg, (DsBindingInfo) m_bindingInfo.clone()));
      }

      // all old bytes have been read now
      m_queuedInBB = null;
    } catch (BufferUnderflowException bue) {
      queueRemainingInBytes(bb);
    }
  }

  /**
   * Writes the stored data (if any) and then writes the data stored in the thread-local network
   * output buffer. If there is any data left, it stores that data to be written later.
   *
   * @throws IOException if there is an exception when writing the data to the channel.
   */
  protected boolean doWrite(ByteBuffer bb) throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.debug("NB-Conn_" + getID() + ": Enter doWrite()");
    }

    synchronized (m_writeLock) {
      bb.flip();
      if (m_channel == null) return false;

      ByteBuffer encrypted = m_engine.getAppSendBuffer();
      try {
        encrypted.put(bb);
      } catch (BufferOverflowException boe) {
        m_engine.close();
        throw new IOException("TLS NIO : Send buffer Overflow, closing connection");
      }
      int bytesLeft = m_engine.write();

      if (bytesLeft > 0) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.debug("NB-Conn_" + getID() + ": bytesLeft" + bytesLeft);
        }

        // make a new array with just the bytes that did not make it
        // queueRemainingOutBytes(bb);

        // There is more data to write on this channel
        // Later, we will register for OP_WRITE

        // actually that is not true if we got here through the user calling send()
        return false;
      }

      bb.clear();
    }
    return true;
  }

  //////////////////////////////////
  // BEGIN DsUnitOfWork Interface //
  //////////////////////////////////

  public void process() {
    // check if something special is needed HERE
    try {
      if (!m_sk.isValid()) {
        closeSocket();
        return;
      }

      if (m_sk.isReadable()) {
        doRead();
      }

      if (m_sk.isWritable()) {
        synchronized (m_writeLock) {
          if (m_engine.getNetSendBuffer().position() > 0) m_engine.flush();
        }
      }

      // re-register interest in this channel
      DsSelector.addInterestOps(m_sk, SelectionKey.OP_READ);

      if (m_engine.getNetSendBuffer().position() > 0) {
        if (cat.isDebugEnabled()) {
          cat.debug(
              "Registered for WRITE interest Remaining ,"
                  + m_engine.getNetSendBuffer().remaining());
        }
        // There are still bytes to write.  We must re-register for write interest
        // This can happen when we get a callback to read while we are waiting to write
        DsSelector.addInterestOps(m_sk, SelectionKey.OP_WRITE);
      }
    } catch (Exception exc) {
      if (cat.isEnabled(Level.WARN)) {
        String socketInfo = null;
        if (m_socket != null) socketInfo = m_socket.getSocketInfo();
        cat.warn(
            "TlsNBConnection(): Exception while accessing the socket channel in socket "
                + socketInfo
                + " : closing socket channel , Exception is ",
            exc);
      }
      if (exc.getMessage() != null && (!exc.getMessage().contains("close_notify"))) {
        readWriteFailureCount.incrementAndGet();
      }

      try {
        closeSocket();
      } catch (Exception ioe) {
        if (cat.isEnabled(Level.INFO)) {
          cat.info("Exception while closing the channel:", ioe);
        }
      }
      notifyListeners(new DsConnectionClosedEvent(this));
    }
  }

  ////////////////////////////////
  // END DsUnitOfWork Interface //
  ////////////////////////////////
}
