// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.
package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;

/**
 * Defines a concrete connection that is used to send data across the network through the underlying
 * UDP multicast datagram socket. This concrete connection can be constructed through the {@link
 * DsConnectionFactory DsConnectionFactory} by passing appropriate parameter like transport type and
 * address.
 */
public class DsMulticastConnection extends DsUdpConnection {
  /**
   * Constructs a UDP multicast connection based on the specified binding info.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsMulticastConnection(DsBindingInfo binding) throws IOException {
    this(
        binding.getLocalAddress(),
        binding.getLocalPort(),
        binding.getRemoteAddress(),
        binding.getRemotePort(),
        binding.getNetwork());
  }

  /**
   * Constructs a UDP multicast connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsMulticastConnection(InetAddress anInetAddress, int aPortNo) throws IOException {
    this(
        null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, anInetAddress, aPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a UDP multicast connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsMulticastConnection(InetAddress anInetAddress, int aPortNo, DsNetwork network)
      throws IOException {
    this(null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, anInetAddress, aPortNo, network);
  }

  /**
   * Constructs a UDP multicast connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>. It also binds the datagram socket
   * locally to the specified local address <code>lInetAddress</code> and local port number <code>
   * lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsMulticastConnection(
      InetAddress lInetAddress, int lPort, InetAddress anInetAddress, int aPortNo)
      throws IOException {
    this(lInetAddress, lPort, anInetAddress, aPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a UDP multicast connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>. It also binds the datagram socket
   * locally to the specified local address <code>lInetAddress</code> and local port number <code>
   * lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsMulticastConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsNetwork network)
      throws IOException {
    m_bindingInfo =
        new DsBindingInfo(
            lInetAddress, lPort, anInetAddress, aPortNo, DsSipTransportType.MULTICAST);

    m_socket = new DsMulticastSocket(lPort, network);
    m_bindingInfo.setLocalPort(((DsMulticastSocket) m_socket).getLocalPort());
    m_bindingInfo.setLocalAddress(((DsMulticastSocket) m_socket).getLocalAddress());
    m_bindingInfo.setNetwork(network);
  }

  /**
   * Constructs a UDP multicast connection based on the specified datagram socket.
   *
   * @param socket a DsDatagramSocket object
   */
  DsMulticastConnection(DsMulticastSocket socket) {
    m_bindingInfo =
        new DsBindingInfo(
            socket.getLocalAddress(),
            socket.getLocalPort(),
            socket.getInetAddress(),
            socket.getPort(),
            DsSipTransportType.MULTICAST);
    m_socket = socket;
    m_bindingInfo.setNetwork(socket.getNetwork());
    updateTimeStamp();
  }

  /**
   * Sends the specified data buffer across the network through the underlying multicast datagram
   * socket to the desired destination. The data destination is specified in this connection's
   * binding info.
   *
   * @param buffer the message bytes to send across
   * @param addr the address to send to
   * @param port the port to send to
   * @throws IOException if there is an I/O error while sending the message
   */
  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException {
    DatagramPacket aPacket = makeDatagramPacket(buffer, addr, port);
    // got this from the old code
    byte ttl = 1;
    DsMulticastSocket msocket = (DsMulticastSocket) m_socket;

    if (DsDebugTransportImpl.set()) {
      InetAddress laddr = msocket.getLocalAddress();
      int lport = msocket.getLocalPort();
      DsDebugTransportImpl.messageOut(
          DsDebugTransport.POS_CONNECTION,
          DsSipTransportType.MULTICAST,
          buffer,
          laddr,
          lport,
          addr,
          port);
    }

    msocket.send(aPacket, ttl);
  }

  public void send(byte[] buffer) throws IOException {
    sendTo(buffer, m_bindingInfo.getRemoteAddress(), m_bindingInfo.getRemotePort());
  }
} // Ends class DsMulticastConnection
