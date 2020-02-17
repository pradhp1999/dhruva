// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;
import org.apache.logging.log4j.Level;

/**
 * The connection factory for the SIP protocol. This implementation of the DsConnectionFactory
 * creates DsSipConnection(s).
 */
public class DsSipConnectionFactory implements DsConnectionFactory {
  private static final boolean IS_NON_BLOCKING_TCP =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NON_BLOCKING_TCP, DsConfigManager.PROP_NON_BLOCKING_TCP_DEFAULT);

  private static final boolean IS_NON_BLOCKING_TLS =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NON_BLOCKING_TLS, DsConfigManager.PROP_NON_BLOCKING_TLS_DEFAULT);

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
    DsNetwork network = binding.getNetworkReliably();
    DsConnection connection = null;
    switch (binding.getTransport()) {
      case DsSipTransportType.UDP:
        switch (network.getDatagramConnectionStrategy()) {
          case (DsNetwork.DGRAM_PER_ENDPOINT):
            if (network.getCompressionType() == DsNetwork.NET_COMP_SIGCOMP) {
              DsSipUdpSigcompConnection c = new DsSipUdpSigcompConnection(binding);
              c.receiveFeedback();
              connection = c;
            } else {
              connection = new DsSipUdpConnection(binding);
            }
            break;
          case (DsNetwork.DGRAM_PER_THREAD):
            if (network.getCompressionType() == DsNetwork.NET_COMP_SIGCOMP) {
              connection = DsSipUdpSigcompConnection.getThreadLocal(binding);
            } else {
              connection = DsSipUdpConnection.getThreadLocal(binding);
            }
            break;
        }
        break;
      case DsSipTransportType.TCP:
        if (IS_NON_BLOCKING_TCP) {
          connection = new DsSipTcpNBConnection(binding);
        } else {
          connection = new DsSipTcpConnection(binding);
        }
        break;
      case DsSipTransportType.TLS:
        if (IS_NON_BLOCKING_TLS) {
          connection = new DsSipTlsNBConnection(binding, context);
        } else {
          connection = new DsSipTlsConnection(binding, context);
        }
        break;
      case DsSipTransportType.MULTICAST:
        connection = new DsSipMulticastConnection(binding);
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
      return new DsSipTlsConnection((DsSSLSocket) socket);
    } else {
      if (IS_NON_BLOCKING_TCP) {
        return new DsSipTcpNBConnection(socket);
      } else {
        return new DsSipTcpConnection(socket);
      }
    }
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsDatagramSocket socket) {
    return new DsSipUdpConnection(socket);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsMulticastSocket socket) {
    return new DsSipMulticastConnection(socket);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsSSLSocket socket) throws IOException {
    return new DsSipTlsConnection(socket);
  }

  // //////////////////////////////////////////////////////

  /**
   * Connection factory method.
   *
   * @param network the network to associate with the connection
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @return the created connection.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if thrown by the underlying socket
   */
  public DsConnection createConnection(DsNetwork network, InetAddress addr, int port, int transport)
      throws IOException, SocketException {
    return createConnection(network, addr, port, transport, null);
  }

  /**
   * Connection factory method.
   *
   * @param network the network to associate with the connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @return the created connection.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if thrown by the underlying socket
   */
  public DsConnection createConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port, int transport)
      throws IOException, SocketException {
    return createConnection(network, laddr, lport, addr, port, transport, null);
  }

  /**
   * Connection factory method.
   *
   * @param network the network to associate with the connection
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if thrown by the underlying socket
   */
  public DsConnection createConnection(
      DsNetwork network, InetAddress addr, int port, int transport, DsSSLContext context)
      throws IOException, SocketException {
    if (network == null) network = DsNetwork.getDefault();
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.debug(
          "In DsSipConnectionFactory.createConnection("
              + "DsNetwork network("
              + network
              + "), InetAddress addr("
              + addr
              + "), int port("
              + port
              + "), int transport("
              + transport
              + "), DsSSLContext context("
              + context
              + ")");
    }

    DsConnection connection = null;
    switch (transport) {
      case DsSipTransportType.UDP:
        switch (network.getDatagramConnectionStrategy()) {
          case (DsNetwork.DGRAM_PER_ENDPOINT):
            if (network.getCompressionType() == DsNetwork.NET_COMP_SIGCOMP) {
              DsSipUdpSigcompConnection c = new DsSipUdpSigcompConnection(network, addr, port);
              c.receiveFeedback();
              connection = c;
            } else {
              if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
                DsLog4j.connectionCat.debug("Creating DsSipUdpConnection");
              }
              connection = new DsSipUdpConnection(network, addr, port);
            }
            break;
          case (DsNetwork.DGRAM_PER_THREAD):
            if (network.getCompressionType() == DsNetwork.NET_COMP_SIGCOMP) {
              connection = DsSipUdpSigcompConnection.getThreadLocal(network, addr, port);
            } else {
              if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
                DsLog4j.connectionCat.debug(
                    "Getting Thread Local DsSipUdpConnection NOT setting local port and address");
              }

              connection = DsSipUdpConnection.getThreadLocal(network, addr, port);
            }
            break;
        }
        break;
      case DsSipTransportType.TCP:
        if (IS_NON_BLOCKING_TCP) {
          connection = new DsSipTcpNBConnection(addr, port, network);
        } else {
          connection = new DsSipTcpConnection(addr, port, network);
        }
        break;
      case DsSipTransportType.TLS:
        if (IS_NON_BLOCKING_TLS) {
          connection = new DsSipTlsNBConnection(addr, port, context, network);
        } else {
          connection = new DsSipTlsConnection(addr, port, context, network);
        }
        break;
      case DsSipTransportType.MULTICAST:
        connection = new DsSipMulticastConnection(addr, port, network);
        break;
    }
    return connection;
  }

  /**
   * Connection factory method.
   *
   * @param network the network to associate with the connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the address to connect to
   * @param port the port number to connect to
   * @param transport the transport to use
   * @param context the security context to apply to the connection
   * @return the created connection.
   * @throws IOException if thrown by the underlying socket
   * @throws SocketException if thrown by the underlying socket
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
    return createConnectionHelper(network, laddr, lport, addr, port, transport, context, true);
  }

  private DsConnection createConnectionHelper(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port,
      int transport,
      DsSSLContext context,
      boolean doConnect)
      throws IOException, SocketException {
    if (network == null) network = DsNetwork.getDefault();
    DsConnection connection = null;
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.debug(
          "In DsSipConnectionFactory.creatConnectionHelper("
              + "DsNetwork network("
              + network
              + "), InetAddress laddr("
              + laddr
              + "), int lport("
              + lport
              + "), InetAddress addr("
              + addr
              + "), int port("
              + port
              + "), int transport("
              + transport
              + "), DsSSLContext context("
              + context
              + "), doConnect("
              + doConnect
              + ")");
    }

    switch (transport) {
      case DsSipTransportType.UDP:
        switch (network.getDatagramConnectionStrategy()) {
          case (DsNetwork.DGRAM_PER_ENDPOINT):
            if (network.getCompressionType() == DsNetwork.NET_COMP_SIGCOMP) {
              DsSipUdpSigcompConnection c =
                  new DsSipUdpSigcompConnection(network, laddr, lport, addr, port, doConnect);
              c.receiveFeedback();
              connection = c;
            } else {
              if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
                DsLog4j.connectionCat.debug("Creating DsSipUdpConnection");
              }
              connection = new DsSipUdpConnection(network, laddr, lport, addr, port, doConnect);
            }
            break;
          case (DsNetwork.DGRAM_PER_THREAD):
            if (network.getCompressionType() == DsNetwork.NET_COMP_SIGCOMP) {
              if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
                DsLog4j.connectionCat.debug(
                    "Creating DsSipUdpSigcompConnection NOT setting local address and port");
              }
              connection = DsSipUdpSigcompConnection.getThreadLocal(network, addr, port);
            } else {
              if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
                DsLog4j.connectionCat.debug(
                    "Getting Thread Local DsSipUdpConnection setting local port and address");
              }
              connection = DsSipUdpConnection.getThreadLocal(network, laddr, lport, addr, port);
            }
            break;
        }
        break;
      case DsSipTransportType.TCP:
        if (IS_NON_BLOCKING_TCP) {
          connection = new DsSipTcpNBConnection(laddr, lport, addr, port, network, doConnect);
        } else {
          connection = new DsSipTcpConnection(laddr, lport, addr, port, network, doConnect);
        }
        break;
      case DsSipTransportType.TLS:
        if (IS_NON_BLOCKING_TLS) {
          connection =
              new DsSipTlsNBConnection(laddr, lport, addr, port, context, network, doConnect);
        } else {
          connection =
              new DsSipTlsConnection(laddr, lport, addr, port, context, network, doConnect);
        }
        break;
      case DsSipTransportType.MULTICAST:
        connection = new DsSipMulticastConnection(laddr, lport, addr, port, network);
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
    return createConnectionHelper(network, laddr, lport, raddr, rport, transport, context, false);
  }

  /*
   * javadoc inherited
   */
  public DsConnection createConnection(DsSocket socket, DsSSLContext context) throws IOException {

    return new DsSipTlsNBConnection(socket, context);
  }
} // Ends DsSipConnectionFactory
