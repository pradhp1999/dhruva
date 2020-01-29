// Copyright (c) 2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.DsLibs.DsUtil.DsSocket;
import java.io.*;
import java.net.*;
import org.apache.logging.log4j.Level;

/**
 * Defines a concrete SIP connection that is used to send SIP messages across the network through
 * the underlying TCP socket. This connection represents a Non-Blocking TCP connection and uses
 * "java.nio" package from J2sdk v1.4.x to provide Non-Blocking behaviour. It uses the asynchronous
 * SSLEngine to make it a TLS connection.
 *
 * <p>This concrete connection can be constructed through the {@link DsSipConnectionFactory
 * DsSipConnectionFactory} by passing appropriate parameters like transport type, address and SSL
 * Context.
 */
public class DsSipTlsNBConnection extends DsTlsNBConnection implements DsSipConnection {
  /** Protected default constructor for the derived classes. */
  protected DsSipTlsNBConnection() {
    super();
  }

  /**
   * Constructs a TLS connection based on the specified binding info <code>binding</code>.
   *
   * @param binding the binding info that contains the remote address and port number where to make
   *     connection to.
   * @param context the SSL context that should be used to construct the SSL socket.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TLS socket
   */
  protected DsSipTlsNBConnection(DsBindingInfo binding, DsSSLContext context)
      throws IOException, SocketException {
    super(binding, context);
  }

  /**
   * Constructs a TLS connection to the specified remote address <code>anInetAddress</code> and the
   * remote port number <code>aPortNo</code>.
   *
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TLS socket
   */
  protected DsSipTlsNBConnection(
      InetAddress rInetAddress, int rPortNo, DsSSLContext context, DsNetwork network)
      throws IOException, SocketException {
    super(rInetAddress, rPortNo, context, network);
  }

  /**
   * Constructs a TLS connection to the specified remote address <code>rInetAddress</code> and the
   * remote port number <code>rPortNo</code>. It also binds the datagram socket locally to the
   * specified local address <code>lInetAddress</code> and local port number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param rInetAddress the remote address to connect to
   * @param rPortNo the remote port number to connect to
   * @param context the SSL context that should be used to construct the SSL socket.
   * @param network The network with which this connection is associated.
   * @param doConnect <code>true</code> to connect to the destination
   * @throws IOException if there is an error with the underlying socket
   * @throws SocketException if there is an error while constructing the TLS socket
   */
  protected DsSipTlsNBConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress rInetAddress,
      int rPortNo,
      DsSSLContext context,
      DsNetwork network,
      boolean doConnect)
      throws IOException, SocketException {
    super(lInetAddress, lPort, rInetAddress, rPortNo, context, network, doConnect);
  }

  /**
   * Constructs a TLS connection based on the specified socket.
   *
   * @param socket a DsSocket object
   * @throws IOException if there is an error with the underlying socket
   */
  protected DsSipTlsNBConnection(DsSocket socket, DsSSLContext context) throws IOException {
    super(socket, context);
  }

  public final void sendTo(byte[] buffer, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    send(buffer);
  }

  public final void sendTo(byte[] buffer, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    send(buffer);
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    byte buffer[] = message.toByteArray();
    send(buffer);
    return buffer;
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    byte buffer[] = message.toByteArray();
    send(buffer);
    return buffer;
  }

  public final byte[] send(DsSipMessage message) throws IOException {
    debugLog(message);
    message.setTimestamp();
    byte buffer[] = message.toByteArray();
    send(buffer);
    return buffer;
  }

  @Override
  public byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
    debugLog(message);
    message.setTimestamp();
    byte buffer[] = message.toByteArray();
    send(buffer);
    return buffer;
  }

  @Override
  public byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException {
    debugLog(message);
    message.setTimestamp();
    byte buffer[] = message.toByteArray();
    send(buffer);
    return buffer;
  }

  @Override
  public void send(byte[] message, DsSipServerTransaction txn) throws IOException {
    send(message);
  }

  @Override
  public void send(byte[] message, DsSipClientTransaction txn) throws IOException {
    send(message);
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
