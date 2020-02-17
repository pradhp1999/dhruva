// Copyright (c) 2005-2010 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.logging.log4j.Level;

/** Wraps the standard JDK DatagramSocket class. */
public class DsJDKDatagramSocket extends DsDatagramSocket {
  static InetAddress slocalHost;
  /** The datagram socket. */
  protected DatagramSocket impl;

  static {
    try {
      slocalHost = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      if (DsLog4j.wireCat.isEnabled(Level.ERROR))
        DsLog4j.wireCat.log(
            Level.ERROR,
            "UnknownHostException"
                + "caught during call to InetAddress.getLocalHost()."
                + " All calls to DatagramSocket.getLocalAddress() will"
                + " return an InetAddress with value 0.0.0.0 to specify"
                + " local host for the IP address. ",
            e);
      slocalHost = null;
    }
  }

  /**
   * Default constructor.
   *
   * @throws SocketException if thrown by the underlying socket
   */
  public DsJDKDatagramSocket() throws SocketException {
    this(DsNetwork.getDefault(), 0, null);
  } // default constructor

  /**
   * Constructor that takes a port to listen to.
   *
   * @param localPort the port to listent to
   * @throws SocketException if thrown by the underlying socket
   */
  public DsJDKDatagramSocket(int localPort) throws SocketException {
    this(DsNetwork.getDefault(), localPort, null);
  } // constructor

  /**
   * Constructor that takes a port to listen to, an InetAddress to listen on and a network.
   *
   * @param network the network associated with this socket
   * @param localPort the port to listent to
   * @param localAddress the InetAddress to listen on
   * @throws SocketException if thrown by the underlying socket
   */
  public DsJDKDatagramSocket(DsNetwork network, int localPort, InetAddress localAddress)
      throws SocketException {
    super(network);
    // don't have to check if slocalHost is null since in either case
    // we would just pass in null and get back the 0.0.0.0 IP address
    if (localAddress == null) impl = new DatagramSocket(localPort, slocalHost);
    else
      impl =
          NetObjectsFactory.getDatagramSocket(
              localPort, localAddress); // new DatagramSocket(localPort, localAddress);

    // KEVMO 08.11.05 CSCsb53394 add IP TypeOfService capability
    int tosValue =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_TOS_VALUE, DsConfigManager.PROP_TOS_VALUE_DEFAULT);
    if (!(tosValue < 0 || tosValue > 255)) {
      impl.setTrafficClass(tosValue);
      if (DsLog4j.socketCat.isEnabled(Level.DEBUG)) {
        DsLog4j.socketCat.log(Level.DEBUG, "IPTypeOfService: " + impl.getTrafficClass());
      }
    }
    int sendBuffer =
        (null == network) ? DEFAULT_SENDBUFSIZE : network.getSendBufferSize(DsSipTransportType.UDP);
    int recvBuffer =
        (null == network)
            ? DEFAULT_RECVBUFSIZE
            : network.getReceiveBufferSize(DsSipTransportType.UDP);
    if (recvBuffer > 0) setReceiveBufferSize(recvBuffer);
    if (sendBuffer > 0) setSendBufferSize(sendBuffer);
  } // constructor

  /**
   * Gets the send buffer size.
   *
   * @return the send buffer size.
   * @throws SocketException if thrown by the underlying socket
   */
  public int getSendBufferSize() throws SocketException {
    return impl.getSendBufferSize();
  }

  /**
   * Sets the underlying send buffer size of this datagram socket to the specified <code>size</code>
   * .
   *
   * @param size the new send buffer size for the underlying datagram socket, that need to be set.
   * @throws SocketException if there is an error while setting the send buffer size for the
   *     underlying datagram socket, or if the socket is already closed
   * @throws java.lang.IllegalArgumentException if the specified size is 0 or negative
   */
  public void setSendBufferSize(int size) throws SocketException {
    if (size < 1) {
      throw new IllegalArgumentException("The buffer size should be > 0");
    }
    impl.setSendBufferSize(size);
  }

  /**
   * Returns the underlying receive buffer size of this datagram socket.
   *
   * @return the underlying receive buffer size of this datagram socket.
   * @throws SocketException if there is an error while retrieving the receive buffer size for the
   *     underlying datagram socket, or if the socket is already closed
   */
  public int getReceiveBufferSize() throws SocketException {
    return impl.getReceiveBufferSize();
  }

  /**
   * Sets the underlying receive buffer size of this datagram socket to the specified <code>size
   * </code>.
   *
   * @param size the new receive buffer size for the underlying datagram socket, that need to be
   *     set.
   * @throws SocketException if there is an error while setting the receive buffer size for the
   *     underlying datagram socket, or if the socket is already closed
   * @throws java.lang.IllegalArgumentException if the specified size is 0 or negative
   */
  public void setReceiveBufferSize(int size) throws SocketException {
    if (size < 1) {
      throw new IllegalArgumentException("The buffer size should be > 0");
    }
    impl.setReceiveBufferSize(size);
  }

  /**
   * Returns the address of the remote host, to which this Datagram Socket is sending datagram
   * packets.
   *
   * @return the address of the remote host, to which this Datagram Socket is sending packets.
   *     Returns null if no packet is sent yet.
   */
  public InetAddress getRemoteAddress() {
    return impl.getInetAddress();
  }

  /**
   * Returns the port number of the remote host, to which this Datagram Socket is sending datagram
   * packets.
   *
   * @return the port number of the remote host, to which this Datagram Socket is sending packets.
   *     Returns -1 if no packet is sent yet.
   */
  public int getRemotePort() {
    return impl.getPort();
  }

  /**
   * The method used to connect to a Inetaddress and a port.
   *
   * @param address the InetAddress to connect to
   * @param port the port number to connect to
   */
  public void connect(InetAddress address, int port) {
    impl.connect(address, port);
  }

  /** Used to disconnect the socket connection. */
  public void disconnect() {
    impl.disconnect();
  }

  /**
   * The InetAddress is retrieved.
   *
   * @return the inetaddress which the socket is connected
   */
  public InetAddress getInetAddress() {
    return getRemoteAddress();
  }

  /**
   * Returns the address of the local host, from which this Datagram Socket is sending datagram
   * packets.
   *
   * @return the address of the local host, from which this Datagram Socket is sending packets.
   */
  public InetAddress getLocalAddress() {
    return impl.getLocalAddress();
  }

  /**
   * The port to which the socket is connected.
   *
   * @return the port to connect to
   */
  public int getPort() {
    return getRemotePort();
  }

  /**
   * Returns the port number of the local host, from which this Datagram Socket is sending datagram
   * packets.
   *
   * @return the port number of the remote host, from which this Datagram Socket is sending packets.
   */
  public int getLocalPort() {
    return impl.getLocalPort();
  }

  /**
   * Sends the datagram packet to the destination address specified in the address field of the
   * datagram packet. The first call to the send() method checks the destination address in the
   * datagram packet and connects to that address for transferring data packets to that address. In
   * the subsequent calls to the send() method, the address and port number in this packet is
   * compared with the address and port number that was specified in the first datagram packet, if
   * there is a mismatch then IllegalArgumentException is thrown as this operation is not allowed.
   * The destination address and port in the subsequent calls to send() should either match with the
   * corresponding values in the first datagram packet send or should be null and -1 respectively.
   *
   * @param packet The datagram packet to be send
   * @throws IOException if there is an error while sending the data
   * @throws java.lang.IllegalArgumentException if the packet address in subsequent calls to send()
   *     mismatches or if the packet is null
   */
  public synchronized void send(DatagramPacket packet)
      throws IOException, IllegalArgumentException {
    if (packet == null) {
      throw new IllegalArgumentException("The datagram packet is null");
    }
    if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
      byte[] pBytes = packet.getData();
      if (pBytes.length > 0 && pBytes[0] == 1) {
        // this is a STUN response, print the debug string instead of the binary data
        // qfang - 12.01.06 - CSCsg93324 - use packet's remote address in logging
        DsLog4j.wireCat.log(
            Level.INFO,
            "Sending binary UDP packet on "
                + getLocalAddress().getHostAddress()
                + ":"
                + getLocalPort()
                + ", destination "
                + packet.getAddress().getHostAddress()
                + ":"
                + packet.getPort()
                + "\n"
                + DsString.toStunDebugString(pBytes));
      } else {
        // qfang - 12.01.06 - CSCsg93324 - use packet's remote address in logging
        DsLog4j.wireCat.log(
            Level.DEBUG,
            "Sending UDP packet on "
                + getLocalAddress().getHostAddress()
                + ":"
                + getLocalPort()
                + ", destination "
                + packet.getAddress().getHostAddress()
                + ":"
                + packet.getPort()
                + "\n"
                + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(
                    DsByteString.newString(packet.getData(), 0, packet.getLength())));
      }
    } else if (DsLog4j.inoutCat.isEnabled(Level.DEBUG)) {
      DsLog4j.logInOutMessage(
          false,
          DsSipTransportType.UC_STR_UDP,
          packet.getAddress().getHostAddress(),
          packet.getPort(),
          getLocalAddress().getHostAddress(),
          getLocalPort(),
          packet.getData());
    }

    try {
      impl.send(packet);
    } catch (PortUnreachableException exc) {
      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
        DsLog4j.wireCat.log(
            Level.ERROR,
            "Caught PortUnreachableException while sending datagram packet on "
                + this
                + "\n"
                + exc.toString(),
            exc);
      }
    }
  }

  /**
   * Receives the incoming Datagram Packet on this Datagram Socket and set the information (data,
   * data length, remote address, remote port) in the specified datagram packet. This method blocks
   * until the datagram packet is received.
   *
   * @throws IOException if there is an error while receiving the datagram Packet or if the socket
   *     is already closed
   */
  public void receive(DatagramPacket packet) throws IOException {
    try {
      impl.receive(packet);
    } catch (PortUnreachableException exc) {
      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
        DsLog4j.wireCat.log(
            Level.ERROR,
            "Caught PortUnreachableException while calling receive on "
                + this
                + "\n"
                + exc.toString(),
            exc);
      }
    }
  }

  /**
   * Closes the socket and releases the resources.
   *
   * @throws IOException if there is an error while closing the socket or the socket is already
   *     closed.
   */
  public void close() throws IOException {
    if (impl != null) {
      impl.close();
    }
  }

  /**
   * Called by the Garbage Collector when there are no more references to this object and do the
   * cleanup and releases the acquired resources, if any.
   *
   * @throws Throwable Any exception thrown by the finalize method causes the finalization of this
   *     object to be halted, but is otherwise ignored
   */
  protected void finalize() throws Throwable {

    impl = null;
    super.finalize();
  }

  /**
   * Returns the underlying Datagram Socket.
   *
   * @return the underlying Datagram Socket.
   */
  public DatagramSocket getDatagramSocket() {
    return impl;
  }
} // Ends class
