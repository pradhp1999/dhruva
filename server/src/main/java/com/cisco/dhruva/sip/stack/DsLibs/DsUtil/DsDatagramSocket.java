// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.io.*;
import java.net.*;

/** This is an observable DatagramSocket and holds an java.net.DatagramSocket internally. */
public abstract class DsDatagramSocket {
  /** Constant defining the datagram packet buffer size. */
  protected static final int BUFFER_SIZE = 65536;
  /** The network associated with this datagram socket. */
  protected DsNetwork m_network;
  /** The default UDP send buffer size. */
  protected static int DEFAULT_SENDBUFSIZE =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_UDP_SEND_BUFFER, DsConfigManager.PROP_UDP_SEND_BUFFER_DEFAULT);
  /** The default UDP receive buffer size. */
  protected static int DEFAULT_RECVBUFSIZE =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_UDP_REC_BUFFER, DsConfigManager.PROP_UDP_REC_BUFFER_DEFAULT);

  /**
   * Returns the default send buffer size for all the datagram sockets.
   *
   * @return the default send buffer size for all the datagram sockets.
   */
  public static int getDefaultSendBufferSize() {
    return DEFAULT_SENDBUFSIZE;
  }

  /**
   * Sets the default send buffer size for the datagram sockets to the specified <code>size</code>.
   * <br>
   * Once this default size is set, then all the newly constructed datagram sockets will inherit
   * this buffer size, if they are created with no specified network. To begin with, this value is
   * set by the java system property "com.dynamicsoft.DsLibs.DsSipLlApi.udpSendBufferSize". If no
   * such java system property is set, then the OS specified default values will be in effect. This
   * default value will be applied only if a datagram socket is constructed with no specified
   * network. If the network is specified then, {@link DsNetwork#getSendBufferSize(int)} for UDP
   * will be used to set the send buffer size.
   *
   * @param size the new default value for the send buffer size for the datagram sockets.
   * @throws IllegalArgumentException if the specified size is 0 or negative
   */
  public static void setDefaultSendBufferSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("The Buffer size should be > 0");
    }
    DEFAULT_SENDBUFSIZE = size;
  }

  /**
   * Returns the default receive buffer size for all the datagram sockets.
   *
   * @return the default receive buffer size for all the datagram sockets.
   */
  public static int getDefaultReceiveBufferSize() {
    return DEFAULT_RECVBUFSIZE;
  }

  /**
   * Sets the default receive buffer size for all the datagram sockets to the specified <code>size
   * </code>. <br>
   * Once this default size is set, then all the newly constructed datagram sockets will inherit
   * this buffer size, if they are created with no specified network. To begin with, this value is
   * set by the java system property "com.dynamicsoft.DsLibs.DsSipLlApi.udpReceiveBufferSize". If no
   * such java system property is set, then the OS specified default values will be in effect. This
   * default value will be applied only if a datagram socket is constructed with no specified
   * network. If the network is specified then, {@link DsNetwork#getReceiveBufferSize(int)} for UDP
   * will be used to set the receive buffer size.
   *
   * @param size the new default value for the receive buffer size for the datagram sockets.
   * @throws IllegalArgumentException if the specified size is 0 or negative
   */
  public static void setDefaultReceiveBufferSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("The Buffer size should be > 0");
    }
    DEFAULT_RECVBUFSIZE = size;
  }

  /**
   * The default constructor.
   *
   * @throws SocketException thrown when there is an exception in creating the socket
   */
  protected DsDatagramSocket() throws SocketException {
    this(null);
  }

  /**
   * Constructor that takes a network.
   *
   * @param network the network to associate with this socket
   * @throws SocketException thrown when there is an exception in creating the socket
   */
  protected DsDatagramSocket(DsNetwork network) throws SocketException {
    m_network = network;
  }

  /**
   * Used to connect to a Inetaddress and a port.
   *
   * @param address the InetAddress to connect to
   * @param port the port number to connect to
   * @throws IOException propagated if the underlying sockets throws this exception
   */
  public abstract void connect(InetAddress address, int port) throws IOException;

  /** Used to disconnect the socket connection. */
  public abstract void disconnect();

  /**
   * The InetAddress is retrieved.
   *
   * @return the inetaddress which the socket is connected
   */
  public abstract InetAddress getInetAddress();

  /**
   * The local address is retrieved to which the socket is connected.
   *
   * @return the local address
   */
  public abstract InetAddress getLocalAddress();

  /**
   * The port to which the socket is connected.
   *
   * @return the port to connect to
   */
  public abstract int getPort();

  /**
   * The port to which the socket is connected.
   *
   * @return the local port which the socket is connected to
   */
  public abstract int getLocalPort();

  /**
   * Used to send a datagram packet.
   *
   * @param packet the packet to be sent
   * @throws IOException thrown when there is an exception in sending the data
   */
  public abstract void send(DatagramPacket packet) throws IOException;

  /**
   * Used to receive a datagram packet.
   *
   * @param packet the packet to be sent
   * @throws IOException thrown when there is an exception in receiving the data
   */
  public abstract void receive(DatagramPacket packet) throws IOException;

  /**
   * Used to close the socket.
   *
   * @throws IOException if the socket is already closed
   */
  public abstract void close() throws IOException;

  /**
   * Return the string representation of this datagram socket attributes.
   *
   * @return the string representation of this datagram socket attributes.
   */
  public String toString() {
    StringBuffer buffer = new StringBuffer(64);
    InetAddress addr = getInetAddress();
    InetAddress laddr = getLocalAddress();

    buffer.append("[ Remote Address = " + ((addr == null) ? "null" : addr.getHostAddress()));
    buffer.append(", Remote Port = " + getPort());
    buffer.append(", Local Address = " + ((laddr == null) ? "null" : laddr.getHostAddress()));
    buffer.append(", Local Port = " + getLocalPort() + " ]");
    return buffer.toString();
  }

  /*
   * javadoc inherited.
   */
  protected void finalize() throws Throwable {
    close();

    super.finalize();
  }

  /**
   * Returns the network with which this socket is associated.
   *
   * @return the network with which this socket is associated.
   */
  public DsNetwork getNetwork() {
    return m_network;
  }
}
