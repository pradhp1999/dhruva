// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDatagramSocket;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDatagramSocketFactory;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsJDKDatagramSocket;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsString;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import org.apache.logging.log4j.Level;

/** Creates a stream of UDP packets to process. */
public class DsUdpListener extends DsPacketListener {
  /** The datagram socket where this listener will be listening on. */
  protected DsDatagramSocket m_socket;

  /**
   * How to receive a packet.
   *
   * @param packet a datagram packet that should be filled with data
   * @throws IOException if an I/O error occurs
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   */
  public void receive(DatagramPacket packet) throws SocketException, IOException {
    m_socket.receive(packet);

    if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
      InetAddress addr = packet.getAddress();
      String pStr = null;
      String binary = "";

      if (packet.getLength() > 0 && packet.getData()[0] == 0) {
        // This is a STUN request, print the debug string, not the binary data
        byte[] pBytes = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, pBytes, 0, pBytes.length);
        pStr = DsString.toStunDebugString(pBytes);
        binary = "binary ";
      } else {
        pStr = DsByteString.newString(packet.getData(), 0, packet.getLength());
      }

      DsLog4j.wireCat.log(
          Level.INFO,
          "Received "
              + binary
              + "UDP packet on "
              + m_socket.getLocalAddress().getHostAddress()
              + ":"
              + m_socket.getLocalPort()
              + " ,source "
              + ((addr != null) ? addr.getHostAddress() : "null")
              + ":"
              + packet.getPort()
              + "\n"
              + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(pStr)
              + "\n--- end of packet ---\n");
    } else if (DsLog4j.inoutCat.isEnabled(Level.DEBUG)) {
      DsLog4j.logInOutMessage(
          true,
          DsSipTransportType.UC_STR_UDP,
          packet.getAddress().getHostAddress(),
          packet.getPort(),
          m_socket.getLocalAddress().getHostAddress(),
          m_socket.getLocalPort(),
          packet.getData());
    }
  }

  /**
   * Convenience function that defaults to listening on the local host.
   *
   * @param port the local port number to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   * @throws UnknownHostException if the host address is not known
   */
  public DsUdpListener(int port, Executor work_queue) throws IOException, UnknownHostException {
    this(port, InetAddress.getLocalHost(), work_queue);
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param port the local port number to listen
   * @param address the local address to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   * @throws UnknownHostException if the host address is not known
   */
  public DsUdpListener(int port, InetAddress address, Executor work_queue)
      throws IOException, UnknownHostException {
    super(work_queue);

    // create the UDP socket
    m_port = port;
    m_address = address;
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param socket the already open socket address to listen on
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   */
  public DsUdpListener(DsDatagramSocket socket, Executor work_queue) throws IOException {
    super(work_queue);
    m_socket = socket;
    m_port = socket.getLocalPort();
    m_address = socket.getLocalAddress();
  }

  //  /////////////   DsNetwork versions

  /**
   * Convenience function that defaults to listening on the local host.
   *
   * @param network the network to associate with this listener
   * @param port the local port number to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   * @throws UnknownHostException if the host address is not known
   */
  public DsUdpListener(DsNetwork network, int port, Executor work_queue)
      throws IOException, UnknownHostException {
    this(network, port, InetAddress.getLocalHost(), work_queue);
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param network the network to associate with this listener
   * @param port the local port number to listen
   * @param address the local address to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   * @throws UnknownHostException if the host address is not known
   */
  public DsUdpListener(DsNetwork network, int port, InetAddress address, Executor work_queue)
      throws IOException, UnknownHostException {
    super(network, work_queue);

    // create the UDP socket
    m_port = port;
    m_address = address;
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param network the network to associate with this listener
   * @param socket the already open socket address to listen on
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   */
  public DsUdpListener(DsNetwork network, DsDatagramSocket socket, Executor work_queue)
      throws IOException {
    super(network, work_queue);
    m_socket = socket;
    m_port = socket.getLocalPort();
    m_address = socket.getLocalAddress();
  }

  /**
   * Returns the transport type of this listener.
   *
   * @return the transport type of this listener
   */
  public int getTransport() {
    return DsSipTransportType.UDP;
  }

  /**
   * Close the listener's socket.
   *
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected void closeSocket() throws IOException {
    m_socket.close();
    m_socket = null;
  }

  /**
   * Create the listener's socket.
   *
   * @throws IOException if the socket could not be opened, or the socket could not bind to the
   *     specified local port
   */
  protected void createSocket() throws IOException {
    if (m_socket == null) {
      // Here I can't just go straight to the factory, since for NAT
      //  traversal, connected (icmp) datagram sockets won't work (since
      //  connected datagrams can only receive packets from the address
      //  they are connected to)   -dg
      if (m_network.getCompressionType() == DsNetwork.NET_COMP_SIGCOMP) {
        // this may be an issue (will compression and NAT work together??)
        //   maybe not since the udvm isn't thread safe (btw send and receive)
        //   threads
        m_socket = DsDatagramSocketFactory.create(m_network, m_address, m_port);
      } else {
        m_socket = new DsJDKDatagramSocket(m_network, m_port, m_address);
      }
    }
  }
}
