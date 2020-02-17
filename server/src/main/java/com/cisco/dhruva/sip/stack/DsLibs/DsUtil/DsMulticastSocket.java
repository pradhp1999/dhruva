// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import java.io.*;
import java.net.*;
import org.apache.logging.log4j.Level;

/** An observable Multicast socket. */
public class DsMulticastSocket extends DsJDKDatagramSocket {
  /**
   * Default Constructor.
   *
   * @throws IOException thrown when there is an error in creating the socket
   */
  public DsMulticastSocket() throws IOException {
    super();
    impl = new MulticastSocket();
    if (DEFAULT_RECVBUFSIZE > 0) setReceiveBufferSize(DEFAULT_RECVBUFSIZE);
    if (DEFAULT_SENDBUFSIZE > 0) setSendBufferSize(DEFAULT_SENDBUFSIZE);
  }

  /**
   * Constructor used to connect to a port.
   *
   * @param port the port to connect to
   * @throws IOException thrown when there is an error in creating the socket
   */
  public DsMulticastSocket(int port) throws IOException {
    this(port, null);
  }

  /**
   * Constructor used to connect to a port.
   *
   * @param port the port to connect to
   * @param network The network with which this socket is associated.
   * @throws IOException thrown when there is an error in creating the socket
   */
  public DsMulticastSocket(int port, DsNetwork network) throws IOException {
    super();
    m_network = network;
    impl = new MulticastSocket(port);
    int sendBuffer =
        (null == network) ? DEFAULT_SENDBUFSIZE : network.getSendBufferSize(DsSipTransportType.UDP);
    int recvBuffer =
        (null == network)
            ? DEFAULT_RECVBUFSIZE
            : network.getReceiveBufferSize(DsSipTransportType.UDP);
    if (recvBuffer > 0) setReceiveBufferSize(recvBuffer);
    if (sendBuffer > 0) setSendBufferSize(sendBuffer);
  }

  /**
   * Method used to join the multicast group.
   *
   * @param mcastAddress the address to join to
   * @throws IOException thrown when the group cannot be joined
   */
  public void joinGroup(InetAddress mcastAddress) throws IOException {
    ((MulticastSocket) impl).joinGroup(mcastAddress);
  }

  /**
   * Method used to leave the multicast group.
   *
   * @param mcastaddr the address to leave
   * @throws IOException thrown when there is an error in leaving the group
   */
  public void leaveGroup(InetAddress mcastaddr) throws IOException {
    ((MulticastSocket) impl).leaveGroup(mcastaddr);
  }

  /**
   * Method used to send the datagrampacket to the multicast address.
   *
   * @param p the data to be sent
   * @param ttl the time to live
   * @throws IOException thrown when there is an error in sending the data
   */
  public void send(DatagramPacket p, byte ttl) throws IOException {
    if (DsLog4j.threadCat.isEnabled(Level.DEBUG)) {
      DsLog4j.threadCat.log(
          Level.DEBUG,
          "Sending the following multicast packet on "
              + (MulticastSocket) impl
              + "\n"
              + DsByteString.newString(p.getData(), 0, p.getLength())
              + "\n--- end of packet ---");
    }
    ((MulticastSocket) impl).send(p, ttl);
  }

  /**
   * Sets the time to live for the multicast packets sent on this socket.
   *
   * @param ttl the time to live
   * @throws IOException
   */
  public void setTimeToLive(int ttl) throws IOException {
    ((MulticastSocket) impl).setTimeToLive(ttl);
  }

  /**
   * Method used to retrieve the time to live for the packets sent on this socket.
   *
   * @return the time to live
   * @throws IOException
   */
  public int getTimeToLive() throws IOException {
    return ((MulticastSocket) impl).getTimeToLive();
  }
}
