// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.NetObjectsFactory;
import com.cisco.dhruva.util.saevent.ConnectionSAEventBuilder;
import com.cisco.dhruva.util.saevent.SAEventConstants;
import java.io.*;
import java.net.*;
import org.apache.logging.log4j.Level;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * TLS socket.
 *
 * <p>This concrete connection can be constructed through the {@link DsDefaultConnectionFactory
 * DsDefaultConnectionFactory} by passing appropriate parameter like transport type and address.
 */
public class DsTlsConnection extends DsTcpConnection {

  private DsSSLContext m_context;
  /**
   * Constructs a TLS connection based on the specified binding info <code>binding</code> and the
   * specified SSL Context <code>context</code>.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @param context the SSL context that should be used to construct the SSL socket.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsTlsConnection(DsBindingInfo binding, DsSSLContext context)
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
   * remote port number <code>aPortNo</code> based on the specified SSL Context <code>context</code>
   * .
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsTlsConnection(InetAddress anInetAddress, int aPortNo, DsSSLContext context)
      throws IOException, SocketException {
    this(
        null,
        DsBindingInfo.LOCAL_PORT_UNSPECIFIED,
        anInetAddress,
        aPortNo,
        context,
        DsNetwork.getDefault());
  }

  /**
   * Constructs a TLS connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code> based on the specified SSL Context <code>context</code>
   * .
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsTlsConnection(
      InetAddress anInetAddress, int aPortNo, DsSSLContext context, DsNetwork network)
      throws IOException, SocketException {
    this(null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, anInetAddress, aPortNo, context, network);
  }

  /**
   * Constructs a TLS connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code> based on the specified SSL Context <code>context</code>
   * . It also binds the datagram socket locally to the specified local address <code>lInetAddress
   * </code> and local port number <code>lPort</code>. The specified SSL context shouldn't be <code>
   * null</code>. IllegalArgumentException will be thrown if the specified context is <code>null
   * </code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsTlsConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsSSLContext context)
      throws IOException, SocketException {
    this(lInetAddress, lPort, anInetAddress, aPortNo, context, DsNetwork.getDefault());
  }

  /**
   * Constructs a TLS connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code> based on the specified SSL Context <code>context</code>
   * . It also binds the datagram socket locally to the specified local address <code>lInetAddress
   * </code> and local port number <code>lPort</code>. The specified SSL context shouldn't be <code>
   * null</code>. IllegalArgumentException will be thrown if the specified context is <code>null
   * </code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsTlsConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsSSLContext context,
      DsNetwork network)
      throws IOException, SocketException {
    init(lInetAddress, lPort, anInetAddress, aPortNo, context, network);
  }

  protected void init(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsSSLContext context,
      DsNetwork network)
      throws IOException {
    InetAddress localAddress = null;
    try {
      if (null == context) {
        throw new IllegalArgumentException("Invalid SSL context: null");
      }
      m_context = context;
      // m_socket =  new DsSSLSocket(anInetAddress, aPortNo, lInetAddress, lPort, context, network);
      m_socket =
          NetObjectsFactory.getSocket(
              anInetAddress, aPortNo, lInetAddress, lPort, context, network);
      if (m_bindingInfo == null) {
        m_bindingInfo =
            new DsSSLBindingInfo(
                lInetAddress, lPort, anInetAddress, aPortNo, ((DsSSLSocket) m_socket).getSession());
      }
      m_bindingInfo.setNetwork(network);
      m_bindingInfo.updateBindingInfo(m_socket);
      ConnectionSAEventBuilder.logConnectionEvent(
          SAEventConstants.CONNECT,
          SAEventConstants.TLS,
          SAEventConstants.OUT,
          m_socket.getLocalAddress(),
          m_socket.getLocalPort(),
          m_socket.getRemoteInetAddress(),
          m_socket.getRemotePort());
      // Notify Tls connection open
      m_bindingInfo.setLocalAddress(m_socket.getLocalAddress());
      tcpOutStream = m_socket.getOutputStream();
      m_socket.setTCPWriteTimeout();

      updateTimeStamp();

      if (network == null) {
        network = DsNetwork.getDefault();
      }
      m_maxQueueSize = network.getMaxOutputQueueSize();
      m_threshold = network.getThreshold();
      m_maxBuffer = network.getMaxbuffer();

    } catch (IOException e) {
      if (m_bindingInfo != null) {
        InetAddress socketLocalAddress = null;
        if (m_socket != null) {
          socketLocalAddress = m_socket.getLocalAddress();
        }
        localAddress =
            ConnectionSAEventBuilder.getDefaultLocalAddress(
                socketLocalAddress, m_bindingInfo.getLocalAddress(), lInetAddress);
        ConnectionSAEventBuilder.logConnectionErrorEvent(
            e.getMessage(),
            SAEventConstants.TLS,
            localAddress,
            m_bindingInfo.getLocalPort(),
            m_bindingInfo.getRemoteAddress(),
            m_bindingInfo.getRemotePort(),
            SAEventConstants.OUT);
      } else {
        ConnectionSAEventBuilder.logConnectionErrorEvent(
            e.getMessage(),
            SAEventConstants.TLS,
            lInetAddress,
            lPort,
            anInetAddress,
            aPortNo,
            SAEventConstants.OUT);
      }

      // REFACTOR
      //      DsProxyError proxyError =
      //          new DsProxyTlsConnectException(e, context, lInetAddress, lPort, anInetAddress,
      // aPortNo);
      //      if (proxyError != null) e.addSuppressed(proxyError);
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
        m_context,
        m_bindingInfo.getNetwork());
  }

  /**
   * Constructs a connecting TLS connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code> based on the specified SSL Context
   * <code>context</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @param doConnect if it is false create connecting DsConnection else create normal DsConnection
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsTlsConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsSSLContext context,
      DsNetwork network,
      boolean doConnect)
      throws IOException, SocketException {
    if (doConnect) {
      init(lInetAddress, lPort, anInetAddress, aPortNo, context, network);
    } else {
      if (null == context) {
        throw new IllegalArgumentException("Invalid SSL context: null");
      }
      m_isConnecting = true;
      m_socket = null;
      m_context = context;
      m_bindingInfo = new DsSSLBindingInfo(lInetAddress, lPort, anInetAddress, aPortNo, null);
      m_bindingInfo.setNetwork(network);
      tcpOutStream = null;
      updateTimeStamp();
      if (network == null) {
        network = DsNetwork.getDefault();
      }
      m_maxQueueSize = network.getMaxOutputQueueSize();
    }
  }

  /**
   * Constructs a SSL connection based on the specified SSL socket.
   *
   * @param socket a DsSSLSocket object
   * @throws IOException if thrown by the underlying socket
   */
  DsTlsConnection(DsSSLSocket socket) throws IOException {
    m_bindingInfo =
        new DsSSLBindingInfo(
            socket.getLocalAddress(),
            socket.getLocalPort(),
            socket.getRemoteInetAddress(),
            socket.getRemotePort(),
            null);
    // -- This may cause this thread to block, use null instead and let the Message Reader set this
    // session.
    // --                                      socket.getSession());
    DsNetwork network = socket.getNetwork();
    m_bindingInfo.setNetwork(network);
    m_socket = socket;
    tcpOutStream = socket.getOutputStream();
    socket.setTCPWriteTimeout();
    updateTimeStamp();

    if (network == null) {
      network = DsNetwork.getDefault();
    }
    m_maxQueueSize = network.getMaxOutputQueueSize();
    m_threshold = network.getThreshold();
    m_maxBuffer = network.getMaxbuffer();
  }
}
