// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;
import org.apache.logging.log4j.Level;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * UDP datagram socket. This concrete connection can be constructed through the {@link
 * DsConnectionFactory DsConnectionFactory} by passing appropriate parameter like transport type and
 * address.
 */
public class DsUdpConnection extends DsAbstractConnection {
  /** <code>true</code> if this connection is a thread local connection. */
  private boolean m_isThreadLocal;

  /** ThreadLocal initialized for datagram packets. */
  private static ThreadLocal tlPacket = new DatagramPacketInitializer();

  /** The underlying socket. */
  protected DsDatagramSocket m_socket;

  /**
   * Resets and returns the ThreadLocal datagram packet.
   *
   * @param data the new byte array
   * @param remoteAddress the remote address
   * @param remotePort the remote port
   * @return the reset ThreadLocal datagram packet
   */
  protected static DatagramPacket makeDatagramPacket(
      byte[] data, InetAddress remoteAddress, int remotePort) {
    DatagramPacket packet = (DatagramPacket) tlPacket.get();

    // reset the packet info
    packet.setData(data);
    packet.setLength(data.length);
    packet.setAddress(remoteAddress);
    packet.setPort(remotePort);

    return packet;
  }

  /** Default constructor. */
  protected DsUdpConnection() {}

  /**
   * Returns the underlying socket.
   *
   * @return the underlying socket.
   */
  public DsDatagramSocket getSocket() {
    return m_socket;
  }

  /**
   * Closes the underlying socket.
   *
   * @throws IOException if there is an error while closing the socket or if the socket is already
   *     closed
   */
  public synchronized void closeSocket() throws IOException {
    if (m_socket != null) {
      m_socket.close();
      m_socket = null;
    }
  }

  /**
   * Constructs a UDP connection based on the specified binding info.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpConnection(DsBindingInfo binding) throws SocketException {
    this(
        binding.getNetwork(),
        binding.getLocalAddress(),
        binding.getLocalPort(),
        binding.getRemoteAddress(),
        binding.getRemotePort());
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpConnection(InetAddress anInetAddress, int aPortNo) throws SocketException {
    this(
        DsNetwork.getDefault(), null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, anInetAddress, aPortNo);
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>. It also binds the datagram socket locally to the
   * specified local address <code>lInetAddress</code> and local port number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpConnection(
      InetAddress lInetAddress, int lPort, InetAddress anInetAddress, int aPortNo)
      throws SocketException {
    this(DsNetwork.getDefault(), lInetAddress, lPort, anInetAddress, aPortNo);
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>.
   *
   * @param network the network to associate with this connection
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpConnection(DsNetwork network, InetAddress anInetAddress, int aPortNo)
      throws SocketException {
    this(network, null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, anInetAddress, aPortNo);
  }

  /**
   * Constructs a UDP connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>. It also binds the datagram socket locally to the
   * specified local address <code>laddr</code> and local port number <code>lPort</code>.
   *
   * @param network the network to associate with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress anInetAddress, int aPortNo)
      throws SocketException {
    init(network, laddr, lport, anInetAddress, aPortNo);
  }

  protected void init(
      DsNetwork network, InetAddress laddr, int lport, InetAddress anInetAddress, int aPortNo)
      throws SocketException {
    m_isThreadLocal = (network.getDatagramConnectionStrategy() == DsNetwork.DGRAM_PER_THREAD);

    if (m_bindingInfo == null) {
      m_bindingInfo =
          new DsBindingInfo(laddr, lport, anInetAddress, aPortNo, DsSipTransportType.UDP);
    }

    InetAddress resolvedAddr = null;

    if ((laddr == null) && (network != null)) {
      // get the network associated listener
      DsUdpListener listener = network.getUdpListener();
      if (listener != null) {
        if (listener.m_address != null) {
          resolvedAddr = listener.m_address;
        }
      }
    } else {
      resolvedAddr = laddr;
    }

    m_socket = DsDatagramSocketFactory.create(network, resolvedAddr, lport);
    m_bindingInfo.setLocalPort(m_socket.getLocalPort());
    m_bindingInfo.setLocalAddress(m_socket.getLocalAddress());
    m_bindingInfo.setNetwork(network);
    m_bindingInfo.setLocalEphemeralPort(m_socket.getLocalPort());

    updateTimeStamp();

    // --    NAT Traversal algorithm doesn't work with connected datagram sockets
    // --    Its okey if we don't connect regular datagram sockets, as anyway we
    // --    won't be getting ICMP error messages on these regular datagram sockets
    // --    untill supported by the underlying JDK implementation.
    // --        m_socket.connect(anInetAddress, aPortNo);
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
        m_bindingInfo.getNetwork(),
        m_bindingInfo.getLocalAddress(),
        m_bindingInfo.getLocalPort(),
        m_bindingInfo.getRemoteAddress(),
        m_bindingInfo.getRemotePort());
  }

  /**
   * Constructs a connecting UDP connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>.
   *
   * @param network the network to associate with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param doConnect if it is false create connecting DsConnection else create normal DsConnection
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsUdpConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress anInetAddress,
      int aPortNo,
      boolean doConnect)
      throws SocketException {
    if (doConnect) {
      init(network, laddr, lport, anInetAddress, aPortNo);
    } else {
      m_isConnecting = true;
      m_isThreadLocal = (network.getDatagramConnectionStrategy() == DsNetwork.DGRAM_PER_THREAD);
      m_bindingInfo =
          new DsBindingInfo(laddr, lport, anInetAddress, aPortNo, DsSipTransportType.UDP);
      m_bindingInfo.setNetwork(network);
      m_socket = null;
      updateTimeStamp();
    }
  }

  /**
   * Constructs a UDP connection based on the specified datagram socket.
   *
   * @param socket a DsDatagramSocket object
   */
  DsUdpConnection(DsDatagramSocket socket) {
    m_bindingInfo =
        new DsBindingInfo(
            socket.getLocalAddress(),
            socket.getLocalPort(),
            socket.getInetAddress(),
            socket.getPort(),
            DsSipTransportType.UDP);
    m_bindingInfo.setNetwork(socket.getNetwork());
    m_socket = socket;
    updateTimeStamp();
  }

  /**
   * Returns <code>true</code> if this connection is a thread local connection.
   *
   * @return <code>true</code> if this connection is a thread local connection
   */
  protected final boolean isThreadLocal() {
    return m_isThreadLocal;
  }

  /**
   * Sends the specified data buffer across the network through the underlying datagram socket to
   * the desired destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @throws IOException if there is an I/O error while sending the message
   */
  public void send(byte buffer[]) throws IOException {
    sendTo(buffer, m_bindingInfo.getRemoteAddress(), m_bindingInfo.getRemotePort());
  }

  /**
   * Sends the specified data buffer across the network through the underlying datagram socket to
   * the desired destination. The data destination is specified in this connection's binding info.
   *
   * @param buffer the message bytes to send across
   * @param addr destination address
   * @param port destination port
   * @throws IOException if there is an I/O error while sending the message
   */
  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException {

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG, new StringBuffer("Sending Message binding info: ").append(m_bindingInfo));
    }

    DatagramPacket aPacket = makeDatagramPacket(buffer, addr, port);

    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      InetAddress laddr = m_socket.getLocalAddress();
      int lport = m_socket.getLocalPort();
      DsLog4j.connectionCat.log(
          Level.INFO,
          new StringBuffer("local address: ")
              .append(laddr.getHostAddress())
              .append(':')
              .append(lport)
              .append("\nremote address: ")
              .append(addr.getHostAddress())
              .append(':')
              .append(port)
              .toString());
    }

    if (DsDebugTransportImpl.set()) {
      InetAddress laddr = m_socket.getLocalAddress();
      int lport = m_socket.getLocalPort();
      DsDebugTransportImpl.messageOut(
          DsDebugTransport.POS_CONNECTION,
          DsSipTransportType.UDP,
          buffer,
          laddr,
          lport,
          addr,
          port);
    }

    try {
      if (DsPerf.ON) DsPerf.start(DsPerf.SOCKET_SEND);
      m_socket.send(aPacket);
      if (DsPerf.ON) DsPerf.stop(DsPerf.SOCKET_SEND);
    } catch (DsSigcompNackException sne) {
      throw sne;
    } catch (SocketException se) {
      if (DsLog4j.connectionCat.isEnabled(Level.WARN))
        DsLog4j.connectionCat.log(
            Level.WARN,
            "ICMP Error while sending Message on UDP socket " + m_socket + " Exception is :",
            se);

      // if this is a threadlocal socket, we are not in the connection
      // table
      if (isThreadLocal()) closeSocket();
      throw se;
    } catch (Throwable t) {
      // TOOD:  this can spin forever?  someone should figure out what we want to
      // do!  -dg
      if (DsLog4j.connectionCat.isEnabled(Level.WARN))
        DsLog4j.connectionCat.log(
            Level.WARN,
            "Exception while sending Message on UDP socket " + m_socket + " Exception is :",
            t);
    }
  }
} // Ends class DsUdpConnection

class DatagramPacketInitializer extends ThreadLocal {
  protected Object initialValue() {
    // just use dummy values, they will be reset before use.
    return new DatagramPacket(new byte[1], 1);
  }
}
