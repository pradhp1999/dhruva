// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;

class DsSigcompDatagramSocketImpl {
  /**
   * Constructs a datagram socket and binds it to any available port on the local host machine.
   *
   * @throws SocketException if unable to create this Datagram socket.
   */
  public DsSigcompDatagramSocketImpl() throws SocketException {
    this(0, null);
  } // default constructor

  /**
   * Constructs a datagram socket and binds it to the specified port on the local host machine.
   *
   * @param port the local port number to which this datagram socket should be bound.
   * @throws SocketException if unable to create and bind this Datagram socket.
   */
  public DsSigcompDatagramSocketImpl(int port) throws SocketException {
    this(port, null);
  } // constructor

  /**
   * Constructs a datagram socket and binds it to the specified local port and the specified local
   * address. The specified port number <code>localPort</code> must be between 0 to 65535 inclusive.
   *
   * @param port the port number to which this datagram socket should be bound.
   * @param address the local address to which this datagram socket should be bound.
   * @throws SocketException if unable to create and bind this Datagram socket.
   */
  public DsSigcompDatagramSocketImpl(int port, InetAddress address) throws SocketException {
    if (port < 0 || port > 0xFFFF) {
      throw new IllegalArgumentException("Port number is out of range [0 - 0xFFFF]:" + port);
    }
    create();
    if (port == 0 && address == null) return; // don't bother if both are unspecified
    bind(port, address);
  } // constructor

  /**
   * Returns the local port number for this datagram socket.
   *
   * @return the local port number for this datagram socket.
   */
  public int getLocalPort() {
    return localPort;
  }

  /**
   * Returns the local address for this datagram socket.
   *
   * @return the local address for this datagram socket.
   */
  public InetAddress getLocalAddress() {
    return localAddress;
  }

  /** Initializes the native library. */
  private static native void init();

  /**
   * Creates a Datagram Socket and assigns the socket descriptor value to the member data <code>fd
   * </code>. This <code>fd</code> file descriptor for the underlying datagram socket is used for
   * further reference to the same socket.
   *
   * @throws SocketException if there is an error while creating the Datagram Socket
   */
  native void create() throws SocketException;

  /**
   * Connects the Datagram Socket, specified by the <code>fd</code> file descriptor,
   * to the specified <code>address</code> and <code<port</code>. Once the
   * destination address and port number is set, then all the datagram packets will
   * be sent to this address only, on subsequent calls to <code>send()</code> method.
   *
   * @throws SocketException if there is an error while making a connection to the
   *                      specified address and port number or if the socket is already
   *                      closed.
   */
  native void connect(InetAddress hostAddress, int port) throws SocketException;

  /**
   * Binds the Datagram Socket to the specified local <code>address</code> and
   * <code<port</code>. Once the socket is bound to the local address and port
   * number, then all the datagram packets will be sent through this address
   * only, on subsequent calls to <code>send()</code> method.
   *
   * @throws SocketException if there is an error while binding to the
   *                      specified address and port number or if the socket
   *                      is already closed.
   */
  native void bind(int port, InetAddress hostAddress) throws SocketException;

  /**
   * Sends the specified Datagram Packet through the Datagram Socket, specified by the <code>fd
   * </code> file descriptor, to the destination address specified in the <code>connection()</code>
   * method. If the destination address is not set by calling the <code>connection()</code> method,
   * it will throw a SocketException
   *
   * @throws SocketException if there is an error while sending datagram Packet to the destination
   *     address, or if no destination address is specified by making a prior call to <code>
   *     connection()</code> method, or if the socket is already closed
   */
  synchronized native void send(DatagramPacket packet) throws SocketException;

  /**
   * Sends the specified Datagram Packet through the Datagram Socket, specified by the <code>fd
   * </code> file descriptor, to the destination address specified in the <code>connection()</code>
   * method. If the destination address is not set by calling the <code>connection()</code> method,
   * it will throw a SocketException
   *
   * @throws SocketException if there is an error while sending datagram Packet to the destination
   *     address, or if no destination address is specified by making a prior call to <code>
   *     connection()</code> method, or if the socket is already closed
   */
  synchronized native void sendCompressed(DatagramPacket packet) throws SocketException;

  /**
   * Receives the incoming Datagram Packet on this Datagram Socket and set the information (data,
   * data length, remote address, remote port) in the specified datagram packet. This method blocks
   * until the datagram packet is received.
   *
   * @throws SocketException if there is an error while receiving the datagram Packet or if the
   *     socket is already closed
   */
  native void receive(DatagramPacket packet) throws SocketException;

  /** */
  native void receiveFeedback() throws SocketException;

  /**
   * Returns the underlying send buffer size of this datagram socket.
   *
   * @return the underlying send buffer size of this datagram socket.
   * @throws SocketException if there is an error while retrieving the send buffer size for the
   *     underlying datagram socket, or if the socket is already closed
   */
  native int getSendBufferSize() throws SocketException;

  /**
   * Sets the underlying send buffer size of this datagram socket to the specified <code>size</code>
   * .
   *
   * @param size the new send buffer size for the underlying datagram socket, that need to be set.
   * @throws SocketException if there is an error while setting the send buffer size for the
   *     underlying datagram socket, or if the socket is already closed
   */
  native void setSendBufferSize(int size) throws SocketException;

  /**
   * Returns the underlying receive buffer size of this datagram socket.
   *
   * @return the underlying receive buffer size of this datagram socket.
   * @throws SocketException if there is an error while retrieving the receive buffer size for the
   *     underlying datagram socket, or if the socket is already closed
   */
  native int getReceiveBufferSize() throws SocketException;

  /**
   * Sets the underlying receive buffer size of this datagram socket to the specified <code>size
   * </code>.
   *
   * @param size the new receive buffer size for the underlying datagram socket, that need to be
   *     set.
   * @throws SocketException if there is an error while setting the receive buffer size for the
   *     underlying datagram socket, or if the socket is already closed
   */
  native void setReceiveBufferSize(int size) throws SocketException;

  /**
   * Closes the Datagram Socket and releases the resources.
   *
   * @throws SocketException if there is an error while closing the socket or the socket is already
   *     closed.
   */
  native void close() throws SocketException;

  private long handle = -1; // parasoft-suppress MISC.AFP

  /** The local address to which this datagram socket is bound */
  private InetAddress localAddress;
  /** The local port number to which this datagram socket is bound */
  private int localPort;

  /**
   * This variable is checked on the native side. If set, native debug will be written to
   * DsSigcompDatagramSocketImpl.log.
   *
   * <p>Don't use in production code.
   */
  private static final int DEBUG =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SIGCOMP_DEBUG, DsConfigManager.PROP_SIGCOMP_DEBUG_DEFAULT);

  public static final int SIZE_PER_COMPARTMENT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SIGCOMP_SIZE_COMP, DsConfigManager.PROP_SIGCOMP_SIZE_COMP_DEFAULT);
  public static final int TOTAL_SIZE =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SIGCOMP_SIZE_TOTAL, DsConfigManager.PROP_SIGCOMP_SIZE_TOTAL_DEFAULT);
  public static final int UDVM_MEMORY =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SIGCOMP_UDVM_MEM, DsConfigManager.PROP_SIGCOMP_UDVM_MEM_DEFAULT);
  public static final int CYCLES_PER_BIT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SIGCOMP_CYC, DsConfigManager.PROP_SIGCOMP_CYC_DEFAULT);
  public static final int ID_USE_PORT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SIGCOMP_ID_PORT, DsConfigManager.PROP_SIGCOMP_ID_PORT_DEFAULT);

  public static final int COPY_BACK =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_SIGCOMP_COPYBACK, DsConfigManager.PROP_SIGCOMP_COPYBACK_DEFAULT);

  static {
    if (DsLog4j.connectionCat.isEnabled(Level.INFO))
      DsLog4j.connectionCat.info("pre native init()");
    init();
    if (DsLog4j.connectionCat.isEnabled(Level.INFO))
      DsLog4j.connectionCat.info("post native init()");
  }
} // Ends class
