// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;

/** A sigcomp compression capable datagram socket. */
public class DsSigcompDatagramSocket extends DsDatagramSocket {
  /** The underlying impl. */
  DsSigcompDatagramSocketImpl impl;

  /** If the compressed data is copied back use this threadlocal. */
  private static BufferInitializer m_buffer = new BufferInitializer();

  /** The maximum size of a compressed message. */
  public static final int MAX_COMPRESSED_SIZE = 4096;

  /**
   * Constructor.
   *
   * @throws SocketException if the is an exception on the socket
   */
  public DsSigcompDatagramSocket() throws SocketException {
    this(null, 0, null);
  }

  /**
   * Constructor. Bind locally.
   *
   * @param localPort the port to which to bind locally to.
   * @throws SocketException if the is an exception on the socket
   */
  public DsSigcompDatagramSocket(int localPort) throws SocketException {
    this(null, localPort, null);
  }

  /**
   * Constructor. Bind locally. Network specific.
   *
   * @param network the network settings.
   * @param localPort the port to which to bind locally to.
   * @param localAddress the address to which to bind locally to.
   * @throws SocketException if the is an exception on the socket
   */
  public DsSigcompDatagramSocket(DsNetwork network, int localPort, InetAddress localAddress)
      throws SocketException {
    super(network);
    impl = new DsSigcompDatagramSocketImpl(localPort, localAddress);
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
   * Return the size of the UDP send buffer.
   *
   * @return the size of the UDP send buffer.
   * @throws SocketException if the is an exception on the socket
   */
  public int getSendBufferSize() throws SocketException {
    return impl.getSendBufferSize();
  }

  /**
   * Set the size of the UDP send buffer.
   *
   * @param size the size of the UDP send buffer.
   * @throws java.lang.IllegalArgumentException if the buffer size is less than or equal to 0.
   * @throws SocketException if the is an exception on the socket
   */
  public void setSendBufferSize(int size) throws SocketException {
    if (size < 1) {
      throw new IllegalArgumentException("The buffer size should be > 0");
    }
    impl.setSendBufferSize(size);
  }

  /**
   * Return the size of the UDP receive buffer.
   *
   * @return the size of the UDP receive buffer.
   * @throws SocketException if the is an exception on the socket
   */
  public int getReceiveBufferSize() throws SocketException {
    return impl.getReceiveBufferSize();
  }

  /**
   * Set the size of the UDP receive buffer.
   *
   * @param size the size of the UDP receive buffer.
   * @throws java.lang.IllegalArgumentException if the buffer size is less than or equal to 0.
   * @throws SocketException if the is an exception on the socket
   */
  public void setReceiveBufferSize(int size) throws SocketException {
    if (size < 1) {
      throw new IllegalArgumentException("The buffer size should be > 0");
    }
    impl.setReceiveBufferSize(size);
  }

  /** Not implemented. sigcomp datagram sockets are not connected. */
  public void connect(InetAddress address, int port) throws IOException {}

  /** Not implemented. sigcomp datagram sockets are not connected. */
  public void disconnect() {}

  /**
   * Not implemented. sigcomp datagram sockets are not connected.
   *
   * @return null
   */
  public InetAddress getInetAddress() {
    return null;
  }

  /**
   * Return the interface to which this socket is locally bound.
   *
   * @return the interface to which this socket is locally bound.
   */
  public InetAddress getLocalAddress() {
    return impl.getLocalAddress();
  }

  /**
   * Not implemented. sigcomp datagram sockets are not connected.
   *
   * @return -1.
   */
  public int getPort() {
    return -1;
  }

  /**
   * Return the port to which this socket is locally bound.
   *
   * @return the port to which this socket is locally bound.
   */
  public int getLocalPort() {
    return impl.getLocalPort();
  }

  /**
   * Send a packet uncompressed.
   *
   * @param packet the pack to send.
   * @throws IOException if there is an IO Exception sending the packet.
   * @throws java.lang.IllegalArgumentException if the packet is null.
   */
  public synchronized void send(DatagramPacket packet)
      throws IOException, IllegalArgumentException {

    if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
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
                  logString(packet.getData(), 0, packet.getLength())));
    }
    impl.send(packet);
  }

  /**
   * Send a packet uncompressed.
   *
   * @param packet the pack to send.
   * @throws IOException if there is an IO Exception sending the packet.
   * @throws java.lang.IllegalArgumentException if the packet is null.
   */
  public synchronized void sendCompressed(DatagramPacket packet)
      throws IOException, IllegalArgumentException {
    if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
      // qfang - 12.01.06 - CSCsg93324 - use packet's remote address in logging
      DsLog4j.wireCat.log(
          Level.DEBUG,
          "COMPRESSING and Sending UDP packet on "
              + getLocalAddress().getHostAddress()
              + ":"
              + getLocalPort()
              + ", destination "
              + packet.getAddress().getHostAddress()
              + ":"
              + packet.getPort()
              + "\n"
              + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(
                  logString(packet.getData(), 0, packet.getLength())));
    }

    // If the compression lib is configured to copy compressed data back
    //   into the packet's bufffer, ensure that there is enough space to do
    //   so since buffer may grow on compression..
    if (DsSigcompDatagramSocketImpl.COPY_BACK != 0) {
      byte[] buf = (byte[]) m_buffer.get();
      System.arraycopy(packet.getData(), packet.getOffset(), buf, 0, packet.getLength());
      packet.setData(buf);
    }

    impl.sendCompressed(packet);

    if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
      if (DsSigcompDatagramSocketImpl.COPY_BACK != 0) {
        DsLog4j.wireCat.log(
            Level.DEBUG,
            "Post-Compress(size="
                + packet.getLength()
                + "): Sent compressed datagram packet on "
                + this
                + "\n"
                + logString(packet.getData(), 0, packet.getLength()));
      } else {
        DsLog4j.wireCat.log(Level.DEBUG, "You must sniff to see the compressed message bytes.");
      }
    }
  }

  /**
   * Return the packet data as a String. Used for debug. If the packet is compressed during send and
   * the compressed data is copied back to the packet, the format is set to a hex dump style.
   *
   * @return the packet data as a String.
   */
  private String logString(byte[] buffer, int offset, int count) {
    String ret = "";
    if ((offset >= 0) && (count > 0) && buffer != null && (buffer.length >= (offset + count))) {
      if ((buffer[offset] & 0xf8) == 0xf8) {
        ret = DsString.toSnifferDisplay(buffer, offset, count);
      } else {
        // ret = DsString.toSnifferDisplay(buffer, offset, count);
        ret = DsByteString.newString(buffer, offset, count);
      }
    }
    return ret;
  }

  /**
   * Read a packet. All data will be received uncompressed.
   *
   * @throws IOException if there is an IO Exception receiving the packet.
   */
  public void receive(DatagramPacket packet) throws IOException {
    impl.receive(packet);
  }

  /**
   * Block receiving sigcomp feedback packets. The packets are digested internally by the native
   * implementation. This is just a convenient way to apply a thread (vs. starting a thread in the
   * native code). This call will block until there is an IOException.
   *
   * @throws IOException when this socket is closed.
   */
  public void receiveFeedback() throws IOException {
    impl.receiveFeedback();
  }

  /**
   * Close the underlying socket.
   *
   * @throws IOException when there is an IO Exception on the underlying call to close.
   */
  public void close() throws IOException {
    if (impl != null) {
      impl.close();
    }
  }

  /** Close the socket. */
  protected void finalize() throws Throwable {
    super.finalize();

    impl = null;
  }
} // Ends class

class BufferInitializer extends ThreadLocal {
  protected Object initialValue() {
    return new byte[DsSigcompDatagramSocket.MAX_COMPRESSED_SIZE];
  }
}
