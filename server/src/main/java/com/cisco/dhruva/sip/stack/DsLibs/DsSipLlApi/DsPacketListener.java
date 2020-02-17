// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.*;
import org.apache.logging.log4j.Level;

/** Abstract base class for all packet-oriented protocols. */
public abstract class DsPacketListener extends DsTransportListener {
  /** Messages smalle than this number of bytes are treated as empty keep alive messages. */
  private static int MAX_KEEP_ALIVE_SIZE;

  /**
   * The maximum length of a packet that can be handled by packet listeners.
   *
   * @deprecated gets value from DsNetwork now
   */
  public static int MAX_PACKET_LENGTH = 64 * 1024;

  /** The port number this packet listener will be listening on. */
  protected int m_port;

  /** The local address this packet listener will be listening on. */
  protected InetAddress m_address;

  /** Where to enqueue incoming packets. */
  protected Executor m_workQueue;

  /** The single listener packet, each listener has exactly one that gets reused. */
  protected DatagramPacket
      m_packet; // = new DatagramPacket(new byte[MAX_PACKET_LENGTH], MAX_PACKET_LENGTH);

  private final int m_maxPacketLength;

  static {
    MAX_KEEP_ALIVE_SIZE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_MAX_KEEP_ALIVE_SIZE,
            DsConfigManager.PROP_MAX_KEEP_ALIVE_SIZE_DEFAULT);
  }

  public static void setMaxKeepAliveSize(int size) {
    MAX_KEEP_ALIVE_SIZE = size;
  }

  /**
   * Constructs a packet listener with the specified work queue where incoming packets (work) will
   * be queued for service.
   *
   * @param network the network associated with this connection
   * @param work_queue which queue to place the work (packets) on
   * @throws IOException if the underlying socket throws this exception
   */
  public DsPacketListener(DsNetwork network, Executor work_queue) throws IOException {
    super(network);
    m_workQueue = work_queue;
    m_maxPacketLength = network.getMaxUdpPacketSize();
    m_packet = new DatagramPacket(new byte[m_maxPacketLength], m_maxPacketLength);
  }

  /**
   * Constructs a packet listener with the specified work queue where incoming packets (work) will
   * be queued for service.
   *
   * @param work_queue which queue to place the work (packets) on
   * @throws IOException if the underlying socket throws this exception
   */
  public DsPacketListener(Executor work_queue) throws IOException {
    this(DsNetwork.getDefault(), work_queue);
  }

  /**
   * How to receive a packet.
   *
   * @param packet a datagram packet that should be filled with data
   * @throws SocketException
   * @throws IOException
   */
  public abstract void receive(DatagramPacket packet) throws SocketException, IOException;

  /**
   * Creates and returns a new DsMessageBytes object from the supplied data.
   *
   * @param bytes the message
   * @param binfo the binding information to associate with this message
   * @return the created message bytes object
   */
  protected DsMessageBytes createMessageBytes(byte[] bytes, DsBindingInfo binfo) {
    if (bytes.length == 0) {
      // empty packet
      return new DsEmptyMessageBytes(bytes, binfo);
    } else if (bytes[0] != 0) {
      if (bytes.length < MAX_KEEP_ALIVE_SIZE) {
        // treat as an empty packet
        return new DsEmptyMessageBytes(bytes, binfo);
      } else {
        // SIP message
        return new DsSipMessageBytes(bytes, binfo);
      }
    } else {
      // STUN message
      return new DsStunMessageBytes(bytes, binfo);
    }
  }

  /*
   * javadoc inherited.
   */
  protected void doListen() throws IOException {
    m_packet.setLength(m_maxPacketLength);
    receive(m_packet);

    int length = m_packet.getLength();

    // if (length == 0)
    // {
    // if (DsLog4j.wireCat.isEnabledFor(Level.WARN))
    // DsLog4j.wireCat.warn("Received 0 length packet (decompression failure?)");
    // return;
    // }

    byte msgBytes[] = new byte[length];

    System.arraycopy(m_packet.getData(), 0, msgBytes, 0, length);

    DsBindingInfo bi =
        new DsBindingInfo(
            m_address,
            m_port,
            m_packet.getAddress(),
            m_packet.getPort(),
            DsSipTransportType.UDP,
            m_pendingClosure);

    if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
      DsLog4j.wireCat.info(
          "UDP Packet is : "
              + DsSipMessage.maskAndWrapSIPMessageToSingleLineOutput(
                  DsByteString.newString(m_packet.getData(), 0, length)));
    }

    bi.setNetwork(m_network);
    try {
      /* TODO:  figure out how we can count dropped packets
       */
      m_workQueue.execute(createMessageBytes(msgBytes, bi));
    } catch (Exception exc) {
      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
        DsLog4j.wireCat.error("Exception enqueue packet ", exc);
      }
    }
  }

  @Override
  protected String getLocalAddress() {
    if (m_address != null) return m_address.getHostAddress();
    return null;
  }

  @Override
  protected int getLocalPort() {
    return m_port;
  }
}
