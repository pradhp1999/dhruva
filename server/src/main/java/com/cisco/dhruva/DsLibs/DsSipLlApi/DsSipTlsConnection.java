// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.DsLibs.DsUtil.DsSSLSocket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;

/**
 * Defines a concrete SIP connection that is used to SIP messages across the network through the
 * underlying TLS socket.
 *
 * <p>This concrete connection can be constructed through the {@link DsSipConnectionFactory
 * DsSipConnectionFactory} by passing appropriate parameter like transport type and address.
 */
public class DsSipTlsConnection extends DsTlsConnection implements DsSipConnection {
  /**
   * Constructs a SIP aware TLS connection based on the specified binding info <code>binding</code>
   * and the specified SSL Context <code>context</code>.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @param context the SSL context that should be used to construct the SSL socket.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsSipTlsConnection(DsBindingInfo binding, DsSSLContext context)
      throws IOException, SocketException {
    super(binding, context);
  }

  /**
   * Constructs a SIP aware TLS connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code> based on the specified SSL Context
   * <code>context</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsSipTlsConnection(InetAddress anInetAddress, int aPortNo, DsSSLContext context)
      throws IOException, SocketException {
    super(anInetAddress, aPortNo, context, DsNetwork.getDefault());
  }

  /**
   * Constructs a SIP aware TLS connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code> based on the specified SSL Context
   * <code>context</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsSipTlsConnection(
      InetAddress anInetAddress, int aPortNo, DsSSLContext context, DsNetwork network)
      throws IOException, SocketException {
    super(anInetAddress, aPortNo, context, network);
  }

  /**
   * Constructs a SIP aware TLS connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code> based on the specified SSL Context
   * <code>context</code>. It also binds the datagram socket locally to the specified local address
   * <code>lInetAddress</code> and local port number <code>lPort</code>. The specified SSL context
   * shouldn't be <code>null</code>. IllegalArgumentException will be thrown if the specified
   * context is <code>null</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsSipTlsConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsSSLContext context)
      throws IOException, SocketException {
    super(lInetAddress, lPort, anInetAddress, aPortNo, context, DsNetwork.getDefault());
  }

  /**
   * Constructs a SIP aware TLS connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code> based on the specified SSL Context
   * <code>context</code>. It also binds the datagram socket locally to the specified local address
   * <code>lInetAddress</code> and local port number <code>lPort</code>. The specified SSL context
   * shouldn't be <code>null</code>. IllegalArgumentException will be thrown if the specified
   * context is <code>null</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsSipTlsConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsSSLContext context,
      DsNetwork network)
      throws IOException, SocketException {
    super(lInetAddress, lPort, anInetAddress, aPortNo, context, network);
  }

  /**
   * Constructs a SIP aware TLS connection to the specified remote address <code>anInetAddress
   * </code> and the remote port number <code>aPortNo</code> based on the specified SSL Context
   * <code>context</code>. It also binds the datagram socket locally to the specified local address
   * <code>lInetAddress</code> and local port number <code>lPort</code>. The specified SSL context
   * shouldn't be <code>null</code>. IllegalArgumentException will be thrown if the specified
   * context is <code>null</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @param doConnect <code>true</code> to connect to the destination
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the SSL socket
   */
  protected DsSipTlsConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsSSLContext context,
      DsNetwork network,
      boolean doConnect)
      throws IOException, SocketException {
    super(lInetAddress, lPort, anInetAddress, aPortNo, context, network, doConnect);
  }

  /**
   * Constructs a SIP aware SSL connection based on the specified SSL socket.
   *
   * @param socket a DsSSLSocket object
   * @throws IOException if the underlying socket throws this exception
   */
  protected DsSipTlsConnection(DsSSLSocket socket) throws IOException {
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

  /**
   * Sends the specified SIP message across the network through the underlying TLS socket to the
   * desired destination. The message destination is specified in this connection's binding info.
   *
   * @param message the SIP message to send across
   * @return the sent message as byte array
   * @throws IOException if there is an I/O error while sending the message
   */
  public byte[] send(DsSipMessage message) throws IOException {
    return _send(message, null);
  }

  // consolidated send method
  private byte[] _send(DsSipMessage message, DsSipTransaction txn) throws IOException {
    debugLog(message);

    message.updateBinding(m_bindingInfo);
    message.setTimestamp();

    updateTimeStamp();

    byte buffer[] = message.toByteArray();

    addMsgToQueue(buffer, txn);

    return buffer;
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info

    send(message, txn);
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info

    send(message, txn);
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
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "writing message to queue: callid: "
              + message.getCallId()
              + ", cseq: "
              + message.getCSeqMethod()
              + " "
              + message.getCSeqNumber());
    }
  }
}
