// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;

/**
 * Defines a concrete class to support connection persistencei for Udp Nat'd environments. Created
 * based on the binding info in the request. This instance is stored in the connection table but
 * wraps around the listener passed in at time of instantiation. When a STUN request comes in, the
 * timestamp is updated to reflect the last access time.
 */
class DsUdpConnectionWrapper extends DsAbstractConnection implements DsSipConnection {
  private DsSipNatUdpListener m_listenerConnection;

  /**
   * Used to create a connection wrapper associated with a specific listener and binding info.
   *
   * @param binfo binding info to associate this connection with
   * @param listener listener with which to associate this connection and that will be used to send
   *     data over the wire
   * @throws IllegalArgumentException if either arguments are null
   */
  protected DsUdpConnectionWrapper(DsBindingInfo binfo, DsPacketListener listener) {
    if (binfo == null) {
      throw new IllegalArgumentException("null binding info");
    }
    if (listener == null) {
      throw new IllegalArgumentException("null listener");
    }
    m_bindingInfo = binfo;
    m_listenerConnection = (DsSipNatUdpListener) listener;
  }

  public void initiateConnect() throws IOException, SocketException {
    if (m_listenerConnection != null) {
      m_listenerConnection.initiateConnect();
    }
  }

  public void closeSocket() throws IOException {
    // do nothing because the listener still has to function after
    // this connection goes away
  }

  public void send(byte buffer[]) throws IOException {
    m_listenerConnection.sendTo(buffer, getInetAddress(), getPortNo());
  }

  public void send(byte buffer[], DsSipClientTransaction txn) throws IOException {
    m_listenerConnection.sendTo(buffer, getInetAddress(), getPortNo(), txn);
  }

  public void send(byte buffer[], DsSipServerTransaction txn) throws IOException {
    m_listenerConnection.sendTo(buffer, getInetAddress(), getPortNo(), txn);
  }

  public byte[] send(DsSipMessage message) throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          new StringBuffer("Sending Message to address: ")
              .append(m_bindingInfo.getRemoteAddress().getHostAddress())
              .append(" on port ")
              .append(m_bindingInfo.getRemotePort())
              .append(DsSipTransportType.getTypeAsUCString(m_bindingInfo.getTransport()))
              .append('\n')
              .append(message)
              .toString());
    }

    byte buffer[];

    DsTokenSipDictionary dictionary = message.shouldEncode();
    message.setTimestamp();
    if (dictionary != null) {
      buffer = message.toEncodedByteString(dictionary).toByteArray();
    } else {
      buffer = message.toByteArray();
    }

    sendTo(buffer, getInetAddress(), getPortNo());

    message.updateBinding(m_bindingInfo);

    return buffer;
  }

  public byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException {
    return sendTo(message, getInetAddress(), getPortNo(), txn);
  }

  public byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
    return sendTo(message, getInetAddress(), getPortNo(), txn);
  }

  public void sendTo(byte[] message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    m_listenerConnection.sendTo(message, addr, port, txn);
  }

  public void sendTo(byte[] message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    m_listenerConnection.sendTo(message, addr, port, txn);
  }

  public void sendTo(byte[] message, InetAddress addr, int port) throws IOException {
    m_listenerConnection.sendTo(message, addr, port);
  }

  public byte[] sendTo(DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    return m_listenerConnection.sendTo(message, addr, port, txn);
  }

  public byte[] sendTo(DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    return m_listenerConnection.sendTo(message, addr, port, txn);
  }
}
