// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import java.io.IOException;
import java.net.InetAddress;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/**
 * Defines the SSL Server Socket which extends the DsServerSocket and provides for the secure
 * communication using TLS/SSL protocols.
 */
public class DsSSLServerSocket extends DsServerSocket {

  private static final int BACKLOG = 50;
  // To avoid the casting to SSLServerSocket, we keep a reference to SSLServerSocket
  private SSLServerSocket m_sslServerSocket;

  /**
   * Constructs a TLS server socket on the specified port, using the specified key store.
   *
   * @param port the port on which to listen
   * @param keyStoreFile the key store file path which specifies the security policy and
   *     restrictions that will apply to this constructed socket.
   * @param password the password to access the stored keys in the specified key store
   * @throws DsSSLException if an error occurs while initializing the context from specified key
   *     store.
   * @throws IOException if there is a network I/O error
   */
  public DsSSLServerSocket(int port, String keyStoreFile, String password)
      throws DsSSLException, IOException {
    super();
    DsSSLContext context = new DsSSLContext(keyStoreFile, password);
    m_socket = m_sslServerSocket = context.createServerSocket(port);
  }

  /**
   * Constructs a TLS server socket on the specified port, using the key store and key store
   * password specified in the corresponding "ds.ssl.keyStore" and "ds.ssl.keyStorePassword" java
   * system properties.
   *
   * @param port the port on which to listen
   * @throws DsSSLException if an error occurs while initializing the context from specified key
   *     store.
   * @throws IOException if there is a network I/O error
   */
  public DsSSLServerSocket(int port) throws DsSSLException, IOException {
    super();
    DsSSLContext context = new DsSSLContext();
    m_socket = m_sslServerSocket = context.createServerSocket(port);
  }
  /**
   * Constructs a TLS server socket on the specified port, using the specified authentication
   * context.
   *
   * @param port the port on which to listen
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is invalid.
   * @throws IOException if there is a network I/O error.
   */
  public DsSSLServerSocket(int port, DsSSLContext context) throws DsSSLException, IOException {
    super();
    if (context == null) {
      throw new DsSSLException("Invalid context specified.");
    }
    m_socket = m_sslServerSocket = context.createServerSocket(port);
  }
  /**
   * Constructs a TLS server socket on the specified port, using the specified authentication
   * context and a specified backlog of connections.
   *
   * @param port the port on which to listen
   * @param backlog how many connections may be pending before the system should start rejecting new
   *     requests
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is invalid.
   * @throws IOException if there is a network I/O error.
   */
  public DsSSLServerSocket(int port, int backlog, DsSSLContext context)
      throws DsSSLException, IOException {
    super();
    if (context == null) {
      throw new DsSSLException("Invalid context specified.");
    }
    m_socket = m_sslServerSocket = context.createServerSocket(port, backlog);
  }

  /**
   * Constructs a TLS server socket on the specified port, using the specified authentication
   * context and a default backlog(50) of connections as well as a particular specified network
   * interface. This constructor is used on multihomed hosts, such as those used for firewalls or as
   * routers, to control through which interface a network service is provided.
   *
   * @param port the port on which to listen
   * @param address the address of the network interface through which connections will be accepted
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is invalid.
   * @throws IOException if there is a network I/O error.
   */
  public DsSSLServerSocket(int port, InetAddress address, DsSSLContext context)
      throws DsSSLException, IOException {
    this(port, BACKLOG, address, context);
  }

  /**
   * Constructs a TLS server socket on the specified port, using the specified authentication
   * context and a specified backlog of connections as well as a particular specified network
   * interface. This constructor is used on multihomed hosts, such as those used for firewalls or as
   * routers, to control through which interface a network service is provided.
   *
   * @param port the port on which to listen
   * @param backlog how many connections may be pending before the system should start rejecting new
   *     requests
   * @param address the address of the network interface through which connections will be accepted
   * @param context the context which specifies the security policy and restrictions that will apply
   *     to this constructed socket.
   * @throws DsSSLException if the specified context is invalid.
   * @throws IOException if there is a network I/O error.
   */
  public DsSSLServerSocket(int port, int backlog, InetAddress address, DsSSLContext context)
      throws DsSSLException, IOException {
    super();
    if (context == null) {
      throw new DsSSLException("Invalid context specified.");
    }
    m_socket = m_sslServerSocket = context.createServerSocket(port, backlog, address);
  }

  /**
   * Tells whether the session creation option is enabled or not, for the newly created sockets from
   * this server socket.
   *
   * @return <code>true</code> if the session creation option is enabled, <code>false</code>
   *     otherwise.
   */
  public boolean getEnableSessionCreation() {
    return m_sslServerSocket.getEnableSessionCreation();
  }

  /**
   * Allows to enable or disable the session creation option for the newly created sockets from this
   * server socket. If session creation option is disabled then the peer information will not be
   * available and by default this option is enabled.
   *
   * @param enableSession if <code>true</code> the session creation option will be enabled, disabled
   *     otherwise.
   */
  public void setEnableSessionCreation(boolean enableSession) {
    m_sslServerSocket.setEnableSessionCreation(enableSession);
  }

  /**
   * Tells whether this server socket requires client authentication. Returns <code>true</code> if
   * the incoming requests from the client need to be authenticated first. In this case the client
   * will send its certificate(s) and server will check if this client is trusted based on client
   * certificate(s). Returns <code>false</code>, if the client should not be authenticated and no
   * information, for the client certificate(s), would be available.
   *
   * @return <code>true</code> if the client authentication is required, <code>false</code>
   *     otherwise.
   */
  public boolean getNeedClientAuth() {
    return m_sslServerSocket.getNeedClientAuth();
  }

  /**
   * Sets whether this server socket should require client authentication. If set to <code>true
   * </code>, then the incoming requests from the client is authenticated first. In this case the
   * client will send its certificate(s) and server will check if this client is trusted based on
   * client certificate(s). If set to <code>false</code>, then the client will not be authenticated
   * and no information, for the client certificate(s), would be available.
   *
   * @param clientAuth <code>true</code> if the client authentication should be required, <code>
   *     false</code> otherwise.
   */
  public void setNeedClientAuth(boolean clientAuth) {
    m_sslServerSocket.setNeedClientAuth(clientAuth);
  }

  /**
   * Returns an array of all the supported cipher suite names for this server socket.
   *
   * @return an array of supported cipher suite names
   */
  public String[] getSupportedCipherSuites() {
    return m_sslServerSocket.getSupportedCipherSuites();
  }

  /**
   * Returns the list of cipher suites which are currently enabled for use by newly accepted
   * connections. If this has not been explicitly modified, a system-provided default guarantees a
   * minimum quality of service in all enabled cipher suites. There are several reasons why an
   * enabled cipher suite might not actually be used.
   *
   * <p>For example: the server socket might not have appropriate private keys available to it or
   * the cipher suite might be anonymous, precluding the use of client authentication, while the
   * server socket has been told to require that sort of authentication.
   *
   * @return the list of cipher suites which are currently enabled for use by newly accepted
   *     connections.
   */
  public String[] getEnabledCipherSuites() {
    return m_sslServerSocket.getEnabledCipherSuites();
  }

  /**
   * Sets the list of cipher suites which need to be enabled for use by newly accepted connections.
   * If this has not been explicitly modified, a system-provided default guarantees a minimum
   * quality of service in all enabled cipher suites. There are several reasons why an enabled
   * cipher suite might not actually be used.
   *
   * <p>For example: the server socket might not have appropriate private keys available to it or
   * the cipher suite might be anonymous, precluding the use of client authentication, while the
   * server socket has been told to require that sort of authentication.
   *
   * @param ciphers the list of cipher suites which need to be enabled for use by newly accepted
   *     connections.
   */
  public void setEnabledCipherSuites(String[] ciphers) {
    m_sslServerSocket.setEnabledCipherSuites(ciphers);
  }

  /**
   * Method which blocks on a port for requests.
   *
   * @return the socket which the server returns
   * @throws IOException thrown when there is an exception in socket I/O
   */
  public DsSocket accept() throws IOException {
    SSLSocket socket = (SSLSocket) m_socket.accept();
    return new DsSSLSocket(socket);
  }

  /**
   * Method used to close the server socket.
   *
   * @throws IOException thrown when the socket cannot be closed
   */
  public void close() throws IOException {
    super.close();
  }
} // ends class DsSSLServerSocket
