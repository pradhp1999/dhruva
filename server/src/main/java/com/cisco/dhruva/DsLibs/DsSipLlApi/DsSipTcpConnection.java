// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.DsLibs.DsUtil.DsSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;

/**
 * Defines a concrete SIP connection that is used to send SIP messages across the network through
 * the underlying TCP socket. This concrete connection can be constructed through the {@link
 * DsSipConnectionFactory DsSipConnectionFactory} by passing appropriate parameter like transport
 * type and address.
 */
public class DsSipTcpConnection extends DsTcpConnection implements DsSipConnection {
  /**
   * Constructs a Sip aware TCP connection based on the specified binding info <code>binding</code>.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsSipTcpConnection(DsBindingInfo binding) throws IOException, SocketException {
    super(binding);
  }

  /**
   * Constructs a Sip aware TCP connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsSipTcpConnection(InetAddress anInetAddress, int aPortNo)
      throws IOException, SocketException {
    this(anInetAddress, aPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a Sip aware TCP connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsSipTcpConnection(InetAddress anInetAddress, int aPortNo, DsNetwork network)
      throws IOException, SocketException {
    super(anInetAddress, aPortNo, network);
  }

  /**
   * Constructs a Sip aware TCP connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>. It also binds the datagram socket
   * locally to the specified local address <code>lInetAddress</code> and local port number <code>
   * lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsSipTcpConnection(
      InetAddress lInetAddress, int lPort, InetAddress anInetAddress, int aPortNo)
      throws IOException, SocketException {
    this(lInetAddress, lPort, anInetAddress, aPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a Sip aware TCP connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>. It also binds the datagram socket
   * locally to the specified local address <code>lInetAddress</code> and local port number <code>
   * lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsSipTcpConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsNetwork network)
      throws IOException, SocketException {
    super(lInetAddress, lPort, anInetAddress, aPortNo, network);
  }

  /**
   * Constructs a Sip aware TCP connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code>. It also binds the datagram socket
   * locally to the specified local address <code>lInetAddress</code> and local port number <code>
   * lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @param doConnect <code>true</code> to connect to the destination.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsSipTcpConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsNetwork network,
      boolean doConnect)
      throws IOException, SocketException {
    super(lInetAddress, lPort, anInetAddress, aPortNo, network, doConnect);
  }

  /**
   * Constructs a Sip aware TCP connection based on the specified socket.
   *
   * @param socket a DsSSLSocket object
   * @throws IOException if the underlying socket throws this exception
   */
  protected DsSipTcpConnection(DsSocket socket) throws IOException {
    super(socket);
  }

  public final byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
    return _send(message, txn);
  }

  public final byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException {
    return _send(message, txn);
  }

  public final void send(byte[] message, DsSipServerTransaction txn) throws IOException {
    addMsgToQueue(message, txn);
  }

  public final void send(byte[] message, DsSipClientTransaction txn) throws IOException {
    addMsgToQueue(message, txn);
  }

  // consolidated send method
  private byte[] _send(DsSipMessage message, DsSipTransaction txn) throws IOException {
    debugLog(message);
    message.updateBinding(m_bindingInfo);

    message.setTimestamp();
    byte buffer[] = message.toByteArray();

    addMsgToQueue(buffer, txn);

    return buffer;
  }

  /**
   * Sends the specified SIP message across the network through the underlying TCP socket to the
   * desired destination. The message destination is specified in this connection's binding info.
   *
   * @param message the SIP message to send across
   * @return the sent message as byte array
   * @throws IOException if there is an I/O error while sending the message
   */
  public byte[] send(DsSipMessage message) throws IOException {
    return _send(message, null);
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info
    addMsgToQueue(message, txn);
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info
    addMsgToQueue(message, txn);
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info
    return send(message, txn);
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info
    return send(message, txn);
  }

  private final void debugLog(DsSipMessage message) {
    // full logging is done after wrinting message to queue
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG))
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "writing message to queue: callid: "
              + message.getCallId()
              + ", cseq: "
              + message.getCSeqMethod()
              + " "
              + message.getCSeqNumber());
  }
} // Ends class DsSipTcpConnection
