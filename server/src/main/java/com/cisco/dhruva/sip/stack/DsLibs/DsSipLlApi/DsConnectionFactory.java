// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;

/**
 * Implement this interface to control the type of connections created by the UA stack's transport
 * layer.
 */
public interface DsConnectionFactory {
  /**
   * Connection factory method.
   *
   * @param binding the complete socket binding for the connection
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(DsBindingInfo binding) throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(InetAddress addr, int port, int transport)
      throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(
      InetAddress laddr, int lport, InetAddress addr, int port, int transport)
      throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * @param binding the complete socket binding for the connection
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(DsBindingInfo binding, DsSSLContext context)
      throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(InetAddress addr, int port, int transport, DsSSLContext context)
      throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(
      InetAddress laddr, int lport, InetAddress addr, int port, int transport, DsSSLContext context)
      throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * <p>Create a connection based on a socket.
   *
   * @param socket a DsSocket object
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(DsSocket socket) throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * <p>Create a connection based on a socket.
   *
   * @param socket a DsDatagramSocket object
   * @return the created connection.
   */
  DsConnection createConnection(DsDatagramSocket socket);

  /**
   * Connection factory method.
   *
   * <p>Create a connection based on a multicast socket.
   *
   * @param socket a DsMulticastSocket object
   * @return the created connection.
   */
  DsConnection createConnection(DsMulticastSocket socket);

  /**
   * Connection factory method.
   *
   * <p>Create a connection based on an SSL socket.
   *
   * @param socket a DsSSLSocket object
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   */
  DsConnection createConnection(DsSSLSocket socket) throws IOException;

  // ///////////////////////////////////////////////////////////////////

  /**
   * Connection factory method.
   *
   * @param network the network associated with this connection
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(DsNetwork network, InetAddress addr, int port, int transport)
      throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * @param network the network associated with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port, int transport)
      throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * @param network the network associated with this connection
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(
      DsNetwork network, InetAddress addr, int port, int transport, DsSSLContext context)
      throws IOException, SocketException;

  /**
   * Connection factory method.
   *
   * @param network the network associated with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException if the underlying sockets throws this exception
   * @throws SocketException if the underlying sockets throws this exception
   */
  DsConnection createConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port,
      int transport,
      DsSSLContext context)
      throws IOException, SocketException;

  /**
   * creates a connecting DsConnection Object.
   *
   * <p>This will be used to create a DsConnection object which will not try to connect to the raddr
   * and rport.
   *
   * @param network the network associated with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   */
  DsConnection createPendingConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress raddr,
      int rport,
      int transport,
      DsSSLContext context)
      throws IOException, SocketException;

  /**
   * creates a connecting DsConnection Object TLS NB Connection is returned
   *
   * @param socket DsSocket
   * @param context DsSSLContext
   * @return
   * @throws IOException
   */
  DsConnection createConnection(DsSocket socket, DsSSLContext context) throws IOException;
}
