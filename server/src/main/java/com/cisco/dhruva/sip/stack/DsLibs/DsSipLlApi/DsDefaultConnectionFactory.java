// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;

/**
 * The default connection factory. This implementation of the DsConnectionFactory creates
 * DsConnection(s).
 */
public class DsDefaultConnectionFactory implements DsConnectionFactory {

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsBindingInfo binding) throws IOException, SocketException {
    return createConnection(binding, null);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(InetAddress addr, int port, int transport)
      throws IOException, SocketException {
    return createConnection(DsNetwork.getDefault(), addr, port, transport);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(
      InetAddress laddr, int lport, InetAddress addr, int port, int transport)
      throws IOException, SocketException {
    return createConnection(DsNetwork.getDefault(), laddr, lport, addr, port, transport);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsBindingInfo binding, DsSSLContext context)
      throws IOException, SocketException {
    DsConnection connection = null;
    switch (binding.getTransport()) {
      case DsSipTransportType.UDP:
        connection = new DsUdpConnection(binding);
        break;
      case DsSipTransportType.TCP:
        connection = new DsTcpConnection(binding);
        break;
      case DsSipTransportType.TLS:
        connection = new DsTlsConnection(binding, context);
        break;
      case DsSipTransportType.MULTICAST:
        connection = new DsMulticastConnection(binding);
        break;
    }
    return connection;
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(
      InetAddress addr, int port, int transport, DsSSLContext context)
      throws IOException, SocketException {
    return createConnection(DsNetwork.getDefault(), addr, port, transport, context);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(
      InetAddress laddr, int lport, InetAddress addr, int port, int transport, DsSSLContext context)
      throws IOException, SocketException {
    return createConnection(DsNetwork.getDefault(), laddr, lport, addr, port, transport, context);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsSocket socket) throws IOException, SocketException {
    if (socket instanceof DsSSLSocket) {
      return new DsTlsConnection((DsSSLSocket) socket);
    } else {
      return new DsTcpConnection(socket);
    }
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsDatagramSocket socket) {
    return new DsUdpConnection(socket);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsMulticastSocket socket) {
    return new DsMulticastConnection(socket);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsSSLSocket socket) throws IOException {
    return new DsTlsConnection(socket);
  }

  // //////////////////////////////////////

  /**
   * Connection factory method.
   *
   * @param network the network object for this connection
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @return the created connection.
   * @throws IOException when the underlying socket throws this exception
   * @throws SocketException when the underlying socket throws this exception
   */
  public DsConnection createConnection(DsNetwork network, InetAddress addr, int port, int transport)
      throws IOException, SocketException {
    return createConnection(network, addr, port, transport, null);
  }

  /**
   * Connection factory method.
   *
   * @param network the network object for this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @return the created connection.
   * @throws IOException when the underlying socket throws this exception
   * @throws SocketException when the underlying socket throws this exception
   */
  public DsConnection createConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port, int transport)
      throws IOException, SocketException {
    return createConnection(network, laddr, lport, addr, port, transport, null);
  }

  /**
   * Connection factory method.
   *
   * @param network the network object for this connection
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException when the underlying socket throws this exception
   * @throws SocketException when the underlying socket throws this exception
   */
  public DsConnection createConnection(
      DsNetwork network, InetAddress addr, int port, int transport, DsSSLContext context)
      throws IOException, SocketException {
    DsConnection connection = null;
    switch (transport) {
      case DsSipTransportType.UDP:
        connection = new DsUdpConnection(network, addr, port);
        break;
      case DsSipTransportType.TCP:
        connection = new DsTcpConnection(addr, port, network);
        break;
      case DsSipTransportType.TLS:
        connection = new DsTlsConnection(addr, port, context, network);
        break;
      case DsSipTransportType.MULTICAST:
        connection = new DsMulticastConnection(addr, port, network);
        break;
    }
    return connection;
  }

  /**
   * Connection factory method.
   *
   * @param network the network object for this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException when the underlying socket throws this exception
   * @throws SocketException when the underlying socket throws this exception
   */
  public DsConnection createConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port,
      int transport,
      DsSSLContext context)
      throws IOException, SocketException {
    DsConnection connection = null;
    switch (transport) {
      case DsSipTransportType.UDP:
        connection = new DsUdpConnection(network, laddr, lport, addr, port);
        break;
      case DsSipTransportType.TCP:
        connection = new DsTcpConnection(laddr, lport, addr, port, network);
        break;
      case DsSipTransportType.TLS:
        connection = new DsTlsConnection(laddr, lport, addr, port, context, network);
        break;
      case DsSipTransportType.MULTICAST:
        connection = new DsMulticastConnection(laddr, lport, addr, port, network);
        break;
    }
    return connection;
  }

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
  public DsConnection createPendingConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress raddr,
      int rport,
      int transport,
      DsSSLContext context)
      throws IOException, SocketException {
    DsConnection connection = null;
    switch (transport) {
      case DsSipTransportType.UDP:
        connection = new DsUdpConnection(network, laddr, lport, raddr, rport, false);
        break;
      case DsSipTransportType.TCP:
        connection = new DsTcpConnection(laddr, lport, raddr, rport, network, false);
        break;
      case DsSipTransportType.TLS:
        connection = new DsTlsConnection(laddr, lport, raddr, rport, context, network, false);
        break;
    }
    return connection;
  }

  @Override
  public DsConnection createConnection(DsSocket socket, DsSSLContext context) throws IOException {

    return new DsSipTlsNBConnection(socket, context);
  }
} // Ends DsDefaultConnectionFactory
