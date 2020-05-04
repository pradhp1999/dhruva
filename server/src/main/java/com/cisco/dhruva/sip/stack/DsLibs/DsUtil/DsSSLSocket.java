// Copyright (c) 2005-2009, 2015 by Cisco Systems, Inc.
// All rights reserved.
// CAFFEINE 2.0 import log4j

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert.DsCertificateHelper;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.slf4j.event.Level;

/**
 * Defines the SSL Socket which extends the DsSocket and provides for the secure communication using
 * TLS/SSL protocol.
 */
public class DsSSLSocket extends DsSocket {
  private Socket m_layeredSocket;

  /**
   * Constructs an SSL socket which wraps around the specified SSLSocket object. This socket will be
   * secured as per the security policy defined for the specified socket.
   *
   * @param socket the socket which needs to be wrapped as a DsSSLSocket
   * @throws DsSSLException if the specified socket is null.
   */
  public DsSSLSocket(SSLSocket socket) throws DsSSLException {
    this(socket, null);
  }

  /**
   * Constructs an SSL socket which wraps around the specified SSLSocket object. This socket will be
   * secured as per the security policy defined for the specified socket.
   *
   * @param socket the socket which needs to be wrapped as a DsSSLSocket
   * @param network The network with which this socket is associated.
   * @throws DsSSLException if the specified socket is null.
   */
  public DsSSLSocket(SSLSocket socket, DsNetwork network) throws DsSSLException {
    super();
    if (socket == null) {
      throw new DsSSLException("Invalid socket specified.");
    }
    m_socket = socket;
    init(network);
  }

  /**
   * Constructs an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. This socket will be secured as per the security policy defined in the specified context.
   *
   * @param host the server host
   * @param port the server port
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   * @throws UnknownHostException if the specified host couldn't be found
   */
  public DsSSLSocket(String host, int port, DsSSLContext context)
      throws DsSSLException, UnknownHostException, IOException {
    this(host, port, context, null);
  }

  /**
   * Constructs an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. This socket will be secured as per the security policy defined in the specified context.
   *
   * @param host the server host
   * @param port the server port
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @param network The network with which this socket is associated.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   * @throws UnknownHostException if the specified host couldn't be found
   */
  public DsSSLSocket(String host, int port, DsSSLContext context, DsNetwork network)
      throws DsSSLException, UnknownHostException, IOException {
    super();
    if (context == null) {
      throw new DsSSLException("Invalid context specified.");
    }
    m_socket = context.createSocket(host, port, network);
    init(network);
  }

  /**
   * Constructs an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. This socket will be secured as per the security policy defined in the specified context.
   *
   * @param host the server host
   * @param port the server port
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   */
  public DsSSLSocket(InetAddress host, int port, DsSSLContext context)
      throws DsSSLException, IOException {
    this(host, port, context, null);
  }

  /**
   * Constructs an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. This socket will be secured as per the security policy defined in the specified context.
   *
   * @param host the server host
   * @param port the server port
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @param network The network with which this socket is associated.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   */
  public DsSSLSocket(InetAddress host, int port, DsSSLContext context, DsNetwork network)
      throws DsSSLException, IOException {
    super();
    if (context == null) {
      throw new DsSSLException("Invalid context specified.");
    }
    m_socket = context.createSocket(host, port, network);
    init(network);
  }

  /**
   * Constructs an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. The client is bound to the specified network address <code>clientHost</code> and port
   * <code>clientPort</code>. This socket will be secured as per the security policy defined in the
   * specified context.
   *
   * @param host the server host
   * @param port the server port
   * @param clientHost the client host
   * @param clientPort the client port
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   * @throws UnknownHostException if the specified host couldn't be found
   */
  public DsSSLSocket(
      String host, int port, InetAddress clientHost, int clientPort, DsSSLContext context)
      throws DsSSLException, UnknownHostException, IOException {
    this(host, port, clientHost, clientPort, context, null);
  }

  /**
   * Constructs an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. The client is bound to the specified network address <code>clientHost</code> and port
   * <code>clientPort</code>. This socket will be secured as per the security policy defined in the
   * specified context.
   *
   * @param host the server host
   * @param port the server port
   * @param clientHost the client host
   * @param clientPort the client port
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @param network The network with which this socket is associated.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   * @throws UnknownHostException if the specified host couldn't be found
   */
  public DsSSLSocket(
      String host,
      int port,
      InetAddress clientHost,
      int clientPort,
      DsSSLContext context,
      DsNetwork network)
      throws DsSSLException, UnknownHostException, IOException {
    super();
    if (context == null) {
      throw new DsSSLException("Invalid context specified.");
    }
    m_socket = context.createSocket(host, port, clientHost, clientPort, network);
    init(network);
  }

  /**
   * Constructs an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. The client is bound to the specified network address <code>clientHost</code> and port
   * <code>clientPort</code>. This socket will be secured as per the security policy defined in the
   * specified context.
   *
   * @param host the server host
   * @param port the server port
   * @param clientHost the client host
   * @param clientPort the client port
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   */
  public DsSSLSocket(
      InetAddress host, int port, InetAddress clientHost, int clientPort, DsSSLContext context)
      throws DsSSLException, IOException {
    this(host, port, clientHost, clientPort, context, null);
  }

  /**
   * Constructs an SSL socket connected to an SSL ServerSocket at the specified network address and
   * port. The client is bound to the specified network address <code>clientHost</code> and port
   * <code>clientPort</code>. This socket will be secured as per the security policy defined in the
   * specified context.
   *
   * @param host the server host
   * @param port the server port
   * @param clientHost the client host
   * @param clientPort the client port
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @param network The network with which this socket is associated.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   */
  public DsSSLSocket(
      InetAddress host,
      int port,
      InetAddress clientHost,
      int clientPort,
      DsSSLContext context,
      DsNetwork network)
      throws DsSSLException, IOException {
    super();
    if (context == null) {
      throw new DsSSLException("Invalid context specified.");
    }
    m_socket = context.createSocket(host, port, clientHost, clientPort, network);
    init(network);
  }

  /**
   * Constructs an SSL socket layered over an existing socket connected to the named host, at the
   * given port. This constructor can be used when tunneling SSL through a proxy or when negotiating
   * the use of SSL over an existing socket. The host and port refer to the logical peer
   * destination. This socket will be secured as per the security policy defined in the specified
   * context.
   *
   * @param host the server host
   * @param port the server port
   * @param socket the existing socket
   * @param autoClose close the underlying socket when this socket is closed
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   */
  public DsSSLSocket(Socket socket, String host, int port, boolean autoClose, DsSSLContext context)
      throws DsSSLException, IOException {
    this(socket, host, port, autoClose, context, null);
  }

  /**
   * Constructs an SSL socket layered over an existing socket connected to the named host, at the
   * given port. This constructor can be used when tunneling SSL through a proxy or when negotiating
   * the use of SSL over an existing socket. The host and port refer to the logical peer
   * destination. This socket will be secured as per the security policy defined in the specified
   * context.
   *
   * @param host the server host
   * @param port the server port
   * @param socket the existing socket
   * @param autoClose close the underlying socket when this socket is closed
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @param network The network with which this socket is associated.
   * @throws DsSSLException if the specified context is null
   * @throws IOException if the connection can't be established
   */
  public DsSSLSocket(
      Socket socket,
      String host,
      int port,
      boolean autoClose,
      DsSSLContext context,
      DsNetwork network)
      throws DsSSLException, IOException {
    super();
    if (context == null) {
      throw new DsSSLException("Invalid context specified.");
    }
    m_socket = context.createSocket(socket, host, port, autoClose, network);
    init(network);
  }

  /**
   * Returns the the SSL Session in use by this connection. These can be long lived, and frequently
   * correspond to an entire login session for some user. The session specifies a particular cipher
   * suite which is being actively used by all connections in that session, as well as the
   * identities of the session's client and server.
   *
   * @return the the SSL Session in use by this connection.
   */
  public SSLSession getSession() {
    return (m_socket == null) ? null : ((SSLSocket) m_socket).getSession();
  }

  /** Wait for the handshake to complete. */
  public void startHandshake() throws IOException {
    /* If socket is in client mode,  the startHandshake would have already been called
      by the listener thread. see DsSSLContext::createSocket
    */
    if (m_socket != null && !((SSLSocket) m_socket).getUseClientMode()) {
      try {
        ((SSLSocket) m_socket).startHandshake();
        SSLSession sslsession = ((SSLSocket) m_socket).getSession();
        if (((SSLSocket) m_socket).getNeedClientAuth()) {
          Certificate[] certs = sslsession.getPeerCertificates();
          Certificate clientcert = certs[0];
          X509Certificate x509cert = (X509Certificate) clientcert;
          ArrayList<ArrayList<String>> certParsedInfo = DsCertificateHelper.getCertsInfo(certs);
          /*As of now isPeerNameAuthenticated is set to false.
          Create a new story to determine if the peer principal name is actually being authenticated.*/
          // TODO saevent-restructure log a TlsConnectionEvent here with peer certinfo
        } else {
          // TODO saevent-restructure log a TlsConnectionEvent here without peer certinfo
        }
      } catch (IOException e) {
        // TODO saevent-restructure log a TLSConnectionErrorEvent here for handshake failure
        throw e;
      }
    }
  }

  /**
   * Initializes the socket properties as per the associated network. * @param network The network
   * with which this socket is associated.
   */
  private void init(DsNetwork network) {
    if (network == null) {
      network = DsNetwork.getDefault();
    }

    m_network = network;
    int sendBuffer =
        (null == network) ? DEFAULT_SENDBUFSIZE : network.getSendBufferSize(DsSipTransportType.TLS);
    int recvBuffer =
        (null == network)
            ? DEFAULT_RECVBUFSIZE
            : network.getReceiveBufferSize(DsSipTransportType.TLS);

    if (null != m_socket) {
      try {
        if (recvBuffer > 0) m_socket.setReceiveBufferSize(recvBuffer);
        if (sendBuffer > 0) m_socket.setSendBufferSize(sendBuffer);

        if (null != network) {
          m_socket.setTcpNoDelay(m_network.getTcpNoDelay());
        }

        // KEVMO 8.11.05 CSCsb53394 add TOS support
        int tosValue =
            DsConfigManager.getProperty(
                DsConfigManager.PROP_TOS_VALUE, DsConfigManager.PROP_TOS_VALUE_DEFAULT);
        if (!(tosValue < 0 || tosValue > 255)) {
          m_socket.setTrafficClass(tosValue);
          if (DsLog4j.socketCat.isEnabled(Level.DEBUG)) {
            DsLog4j.socketCat.log(Level.DEBUG, "IPTypeOfService: " + m_socket.getTrafficClass());
          }
        }

        if (DsLog4j.socketCat.isEnabled(Level.DEBUG)) {
          DsLog4j.socketCat.log(Level.DEBUG, "Setting SO_TIMEOUT to: " + network.getSoTimeout());
        }
        m_socket.setSoTimeout(network.getSoTimeout());
      } catch (SocketException e) {
        if (DsLog4j.socketCat.isEnabled(Level.ERROR)) {
          DsLog4j.socketCat.log(
              Level.ERROR,
              "INVALID TOS Value: "
                  + DsConfigManager.getProperty(
                      DsConfigManager.PROP_TOS_VALUE, DsConfigManager.PROP_TOS_VALUE_DEFAULT),
              e);
        }
      } catch (Exception e) {
        if (DsLog4j.socketCat.isEnabled(Level.WARN)) {
          DsLog4j.socketCat.log(Level.WARN, "Exception on socket: ", e);
        }
      }
    }
  }

  /**
   * Set this SSL socket layered over an existing socket.
   *
   * @param socket an existing established socket.
   */
  public void setLayeredSocket(Socket socket) {
    m_layeredSocket = socket;
  }

  public long getHandShaketimeout() {
    return m_network.getTlsHandshakeTimeout();
  }

  /**
   * Method used to close the socket.
   *
   * @throws IOException thrown when the socket cannot be closed
   */
  public void close() throws IOException {
    if (null != m_layeredSocket) {
      m_layeredSocket.close();
    } else {
      m_socket.close();
    }
  }
}
