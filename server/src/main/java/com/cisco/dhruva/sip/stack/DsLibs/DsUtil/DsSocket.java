// Copyright (c) 2005-2009, 2015 by Cisco Systems, Inc.
// All rights reserved.
// CAFFEINE 2.0 import log4j
package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.*;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.net.SocketImpl;

/** Class which creates an observable socket. */
public class DsSocket {
  /** The underlying java socket. */
  protected Socket m_socket;

  /** The type of network this socket is associated with. */
  protected DsNetwork m_network;

  /** The default TCP/TLS send buffer size. */
  protected static int DEFAULT_SENDBUFSIZE =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_TCP_SEND_BUFFER, DsConfigManager.PROP_TCP_SEND_BUFFER_DEFAULT);
  /** The default TCP/TLS receive buffer size. */
  protected static int DEFAULT_RECVBUFSIZE =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_TCP_REC_BUFFER, DsConfigManager.PROP_TCP_REC_BUFFER_DEFAULT);

  /** The default TCP/TLS receive buffer size. */
  protected static int CONN_TIMEOUT =
      DsConfigManager.getProperty(
              DsConfigManager.PROP_WAIT_FOR_CONNECTION,
              DsConfigManager.PROP_WAIT_FOR_CONNECTION_DEFAULT)
          * 1000;

  /** The default TCP/TLS receive buffer size. */
  protected static int SSL_HANDSHAKE_TIMEOUT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SSL_HANDSHAKE_TIMEOUT,
          DsConfigManager.PROP_SSL_HANDSHAKE_TIMEOUT_DEFAULT);

  /*
   * the default value of SO_SNDTIMEO (socket write timeout)
   */
  public static int TCP_WRITE_TIMEOUT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_TCP_WRITE_TIMEOUT, DsConfigManager.PROP_TCP_WRITE_TIMEOUT_DEFAULT);

  /*
   * <code>true</code> to configure the SO_SNDTIMEO
   */
  public static boolean SET_TCP_WRITE_TIMEOUT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SET_TCP_WRITE_TIMEOUT,
          DsConfigManager.PROP_SET_TCP_WRITE_TIMEOUT_DEFAULT);

  /* ms_getImplSocketMethod will point to private getImpl method of Socket. */
  protected static Method ms_getImplSocketMethod;
  /* ms_getFileDescriptionMethod will point to private getFileDescriptor method of SocketImpl */
  protected static Method ms_getFileDescriptionMethod;
  /* ms_fdSocketField will point to private fd field of FileDescriptor */
  protected static Field ms_fdSocketField;
  /*
   * <code> true <\code> if there are no SecureManager exception
   * seen when using reflection to get the private Methods / Fields of
   * socket related classes.
   */
  protected static boolean ms_isNativeSocketAllowed;

  /* Holds the fd number associated with m_socket.*/
  protected int mNativeFd = -1;

  static {
    try {
      /*
       * Get access to socket internal Methods / Fields using reflection.
       * These are need to get the fd number of m_socket and call
       * some native socket operations like setting the SO_SNDTIMEO
       * which are not possible to do with Java socket API
       */
      ms_getImplSocketMethod = Socket.class.getDeclaredMethod("getImpl");
      ms_getImplSocketMethod.setAccessible(true);
      ms_getFileDescriptionMethod = SocketImpl.class.getDeclaredMethod("getFileDescriptor");
      ms_getFileDescriptionMethod.setAccessible(true);
      ms_fdSocketField = FileDescriptor.class.getDeclaredField("fd");
      ms_fdSocketField.setAccessible(true);
      ms_isNativeSocketAllowed = true;
    } catch (Exception ex) {

      DsLog4j.connectionCat.error("Failed to load socket file descriptor", ex);

      ms_isNativeSocketAllowed = false;
    }
  }

  /*
   * Set the SO_SNDTIMEO of the m_socket using
   * NativeSocket.
   *
   * TODO handle tcp timeout
   */
  public void setTCPWriteTimeout() {}

  /**
   * Returns the default send buffer size for all the TCP/TLS sockets.
   *
   * @return the default send buffer size for all the TCP/TLS sockets.
   */
  public static int getDefaultSendBufferSize() {
    return DEFAULT_SENDBUFSIZE;
  }

  /**
   * Sets the default send buffer size for the TCP/TLS sockets to the specified <code>size</code>.
   *
   * <p>Once this default size is set, then all the newly constructed TCP/TLS sockets will inherit
   * this buffer size, if they are created with no specified network. To begin with, this value is
   * set by the java system property "com.dynamicsoft.DsLibs.DsSipLlApi.tcpSendBufferSize". If no
   * such java system property is set, then the OS specified default values will be in effect. This
   * default value will be applied only if a TCP/TLS socket is constructed with no specified
   * network. If the network is specified then, {@link DsNetwork#getSendBufferSize(int)} for TCP
   * will be used to set the send buffer size.
   *
   * @param size the new default value for the send buffer size for the TCP/TLS sockets.
   * @throws IllegalArgumentException if the specified size is 0 or negative
   */
  public static void setDefaultSendBufferSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("The Buffer size should be > 0");
    }
    DEFAULT_SENDBUFSIZE = size;
  }

  /**
   * Returns the default receive buffer size for all the TCP/TLS sockets.
   *
   * @return the default receive buffer size for all the TCP/TLS sockets.
   */
  public static int getDefaultReceiveBufferSize() {
    return DEFAULT_RECVBUFSIZE;
  }

  /**
   * Sets the default receive buffer size for all the TCP/TLS sockets to the specified <code>size
   * </code>.
   *
   * <p>Once this default size is set, then all the newly constructed TCP/TLS sockets will inherit
   * this buffer size, if they are created with no specified network. To begin with, this value is
   * set by the java system property "com.dynamicsoft.DsLibs.DsSipLlApi.tcpReceiveBufferSize". If no
   * such java system property is set, then the OS specified default values will be in effect. This
   * default value will be applied only if a TCP/TLS socket is constructed with no specified
   * network. If the network is specified then, {@link DsNetwork#getReceiveBufferSize(int)} for TCP
   * will be used to set the receive buffer size.
   *
   * @param size the new default value for the receive buffer size for the TCP/TLS sockets.
   * @throws IllegalArgumentException if the specified size is 0 or negative
   */
  public static void setDefaultReceiveBufferSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("The Buffer size should be > 0");
    }
    DEFAULT_RECVBUFSIZE = size;
  }

  /**
   * Common initialization code for all constructors.
   *
   * @param aSocket the client socket.
   * @param network The network with which this socket is associated.
   */
  private void init(Socket aSocket, DsNetwork network) {
    if (network == null) {
      network = DsNetwork.getDefault();
    }

    m_network = network;
    m_socket = aSocket;
    int sendBuffer =
        (null == network) ? DEFAULT_SENDBUFSIZE : network.getSendBufferSize(DsSipTransportType.TCP);
    int recvBuffer =
        (null == network)
            ? DEFAULT_RECVBUFSIZE
            : network.getReceiveBufferSize(DsSipTransportType.TCP);

    if (null != m_socket) {
      try {
        if (recvBuffer > 0) m_socket.setReceiveBufferSize(recvBuffer);
        if (sendBuffer > 0) m_socket.setSendBufferSize(sendBuffer);

        if (null != network) {
          m_socket.setTcpNoDelay(m_network.getTcpNoDelay());
        }

        // KEVMO - 08.09.05 CSCsb53394 Add IP_HEADER TOS Value
        int tosValue =
            DsConfigManager.getProperty(
                DsConfigManager.PROP_TOS_VALUE, DsConfigManager.PROP_TOS_VALUE_DEFAULT);
        if (!(tosValue < 0 || tosValue > 255)) {
          m_socket.setTrafficClass(tosValue);

          DsLog4j.socketCat.debug("IPTypeOfService: " + m_socket.getTrafficClass());
        }

        DsLog4j.socketCat.debug("Setting SO_TIMEOUT to: " + network.getSoTimeout());

        m_socket.setSoTimeout(network.getSoTimeout());
      } catch (SocketException e) {

        DsLog4j.socketCat.error(
            "INVALID TOS Value: "
                + DsConfigManager.getProperty(
                    DsConfigManager.PROP_TOS_VALUE, DsConfigManager.PROP_TOS_VALUE_DEFAULT),
            e);

      } catch (Exception e) {

        DsLog4j.socketCat.warn("Exception on socket: ", e);
      }
    }
  }

  /**
   * The default constructor to allow class extension and assigns null to the underlying java
   * socket.
   */
  protected DsSocket() {
    init(null, null);
  }

  /**
   * Constructor which accepts a socket.
   *
   * @param aSocket the client socket which is to be made observable
   */
  public DsSocket(Socket aSocket) {
    init(aSocket, null);
  }

  /**
   * Constructor which accepts a socket and the network type.
   *
   * @param aSocket the client socket.
   * @param network The network with which this socket is associated.
   */
  public DsSocket(Socket aSocket, DsNetwork network) {
    init(aSocket, network);
  }

  /**
   * Constructor which accepts a port number and an address.
   *
   * @param address the address to connect to
   * @param port the port to connect to
   * @throws IOException thrown when the socket cannot be created
   */
  public DsSocket(InetAddress address, int port) throws IOException {
    this(address, port, null);
  }

  /**
   * Construct a socket which accepts a host name and a port number.
   *
   * @param host the host name
   * @param port the port number
   * @throws IOException thrown when there is an error in creating the socket
   */
  public DsSocket(String host, int port) throws IOException {
    this(host, port, null);
  }

  /**
   * Constructor which accepts both local and remote port numbers and addresses.
   *
   * @param address the address to connect to
   * @param port the port to connect to
   * @param lAddr local address the socket is bound to
   * @param lPort local port the socket is bound to
   * @throws IOException thrown when the socket cannot be created
   */
  public DsSocket(InetAddress lAddr, int lPort, InetAddress address, int port) throws IOException {
    this(lAddr, lPort, address, port, null);
  }

  /**
   * Constructor which accepts a port number and an address.
   *
   * @param address the address to connect to
   * @param port the port to connect to
   * @param network The network with which this socket is associated.
   * @throws IOException thrown when the socket cannot be created
   */
  @SuppressFBWarnings(value = {"UNENCRYPTED_SOCKET"})
  public DsSocket(InetAddress address, int port, DsNetwork network) throws IOException {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(address, port), CONN_TIMEOUT);
    init(socket, network);
  }

  /**
   * Construct a socket which accepts a host name and a port number.
   *
   * @param host the host name
   * @param port the port number
   * @param network The network with which this socket is associated.
   * @throws IOException thrown when there is an error in creating the socket
   */
  @SuppressFBWarnings(value = {"UNENCRYPTED_SOCKET"})
  public DsSocket(String host, int port, DsNetwork network) throws IOException {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host, port), CONN_TIMEOUT);
    init(socket, network);
  }

  /**
   * Constructor which accepts both local and remote port numbers and addresses.
   *
   * @param address the address to connect to
   * @param port the port to connect to
   * @param lAddr local address the socket is bound to
   * @param lPort local port the socket is bound to
   * @param network The network with which this socket is associated.
   * @throws IOException thrown when the socket cannot be created
   */
  public DsSocket(InetAddress lAddr, int lPort, InetAddress address, int port, DsNetwork network)
      throws IOException {
    // Socket socket = new Socket();
    Socket socket = NetObjectsFactory.getSocket();
    socket.bind(new InetSocketAddress(lAddr, lPort));
    socket.connect(new InetSocketAddress(address, port), CONN_TIMEOUT);
    init(socket, network);
  }

  /**
   * Method used to retrieve the Input Stream.
   *
   * @return the input stream which is observable
   * @throws IOException thrown when the stream cannot be created
   */
  public InputStream getInputStream() throws IOException {
    return m_socket.getInputStream();
  }

  /**
   * Method used to retrieve the Output Stream.
   *
   * @return the output stream which is observable
   * @throws IOException thrown when the stream cannot be created
   */
  public OutputStream getOutputStream() throws IOException {
    return m_socket.getOutputStream();
  }

  /**
   * Method used to close the socket.
   *
   * @throws IOException thrown when the socket cannot be closed
   */
  public void close() throws IOException {
    m_socket.close();
  }

  /**
   * Method used to retrieve the InetAddress to which the socket is connected.
   *
   * @return the address to which the socket is connected
   */
  public InetAddress getRemoteInetAddress() {
    return m_socket.getInetAddress();
  }

  /**
   * Method used to retrieve the port to which the socket is tied to.
   *
   * @return the port number
   */
  public int getRemotePort() {
    return m_socket.getPort();
  }

  /**
   * Method used to retrieve the InetAddress to which the socket is bound.
   *
   * @return the address to which the socket is connected
   */
  public InetAddress getLocalAddress() {
    return m_socket.getLocalAddress();
  }

  /**
   * Method used to retrieve the port to which the socket is bound to.
   *
   * @return the port number
   */
  public int getLocalPort() {
    return m_socket.getLocalPort();
  }

  // checks if a socket has been closed

  /**
   * Checks if the connection is available.
   *
   * @return true if available, false otherwise
   * @throws IOException
   */
  public boolean checkConnection() throws IOException {
    m_socket.getInputStream().available();
    return true;
  }

  /**
   * Returns the underlying java socket.
   *
   * @return the underlying java socket
   */
  public Socket getSocket() {
    return m_socket;
  }

  /**
   * Returns the network with which this socket is associated.
   *
   * @return the network with which this socket is associated.
   */
  public DsNetwork getNetwork() {
    return m_network;
  }

  /**
   * Set the time to wait, in seconds, before giving up when trying to establish a TCP/TLS
   * connection. 0 means infinite wait, and is the default behavior.
   *
   * @param seconds the number of seconds to wait before giving up on a call to connect, 0 is
   *     infinite
   */
  public static void setTimeout(int seconds) {
    if (seconds < 0) {
      seconds = 0;
    }

    CONN_TIMEOUT = seconds * 1000;
  }

  /**
   * Set the time to wait, in seconds, before giving up when trying to do tls handshake connection.
   * 0 means infinite wait, and is the default behavior.
   *
   * @param seconds the number of seconds to wait before giving up time handshake, 0 is infinite
   */
  public static void setTLSHandshakeTimeout(int seconds) {
    if (seconds < 0) {
      seconds = 0;
    }

    SSL_HANDSHAKE_TIMEOUT = seconds * 1000;
  }

  /**
   * Get the Socket detail in a String representation
   *
   * @return Socket detail in String
   */
  public String getSocketInfo() {
    return "LocalAddress = "
        + getLocalAddress()
        + " LocalPort = "
        + getLocalPort()
        + " RemoteAddress = "
        + getRemoteInetAddress().getHostAddress()
        + " RemotePort = "
        + getRemotePort();
  }
}
