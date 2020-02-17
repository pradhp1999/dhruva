// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import org.apache.logging.log4j.Level;

/** Creates a stream of udp packets to process. */
public class DsMulticastListener extends DsPacketListener {
  /** The multicast socket. */
  protected MulticastSocket m_socket;

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

    if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
      DatagramSocket s = (DatagramSocket) m_socket;

      DsLog4j.wireCat.log(
          Level.INFO,
          "Received Multicast packet on "
              + s.getLocalAddress()
              + ":"
              + s.getLocalPort()
              + "\n"
              + DsByteString.newString(packet.getData(), 0, packet.getLength())
              + "\n--- end of packet ---\n");
    }
  }

  /**
   * Convenience function that defaults to listening on the local host.
   *
   * @param port the local port number to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException any I/O problems during the construction
   * @throws UnknownHostException when the hostname cannot be resolved
   */
  public DsMulticastListener(int port, Executor work_queue)
      throws IOException, UnknownHostException {
    this(DsNetwork.getDefault(), port, work_queue);
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param port the local port number to listen
   * @param address the local address to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException any I/O problems during the construction
   * @throws UnknownHostException when the hostname cannot be resolved
   */
  public DsMulticastListener(int port, InetAddress address, Executor work_queue)
      throws IOException, UnknownHostException {
    this(DsNetwork.getDefault(), port, address, work_queue);
  }

  /**
   * Convenience function that defaults to listening on the local host.
   *
   * @param network the network associated with this listener
   * @param port the local port number to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException any I/O problems during the construction
   * @throws UnknownHostException when the hostname cannot be resolved
   */
  public DsMulticastListener(DsNetwork network, int port, Executor work_queue)
      throws IOException, UnknownHostException {
    this(network, port, InetAddress.getLocalHost(), work_queue);
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param network the network associated with this listener
   * @param port the local port number to listen
   * @param address the local address to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException any I/O problems during the construction
   * @throws UnknownHostException when the hostname cannot be resolved
   */
  public DsMulticastListener(DsNetwork network, int port, InetAddress address, Executor work_queue)
      throws IOException, UnknownHostException {
    super(network, work_queue);
    m_port = port;
    m_address = address;
  }

  /**
   * Returns the transport type of this listener.
   *
   * @return the transport type of this listener
   */
  public int getTransport() {
    return DsSipTransportType.MULTICAST;
  }

  /**
   * Close the socket the listener is working on.
   *
   * @throws IOException if error occurs while closing the socket
   */
  protected void closeSocket() throws IOException {
    m_socket.close();
  }

  /**
   * Create a new socket.
   *
   * @throws IOException if error occurs while creating the socket
   */
  protected void createSocket() throws IOException {
    m_socket = new MulticastSocket(m_port);
    m_socket.joinGroup(m_address);
    m_socket.setTimeToLive(1);
  }
}
