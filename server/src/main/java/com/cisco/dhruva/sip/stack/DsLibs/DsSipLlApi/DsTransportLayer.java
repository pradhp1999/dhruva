// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamClosedEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamErrorEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamEventListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetworkProperties;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.transport.Connection;
import com.cisco.dhruva.transport.ConnectionKey;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.TransportLayer;
import com.cisco.dhruva.util.log.DhruvaLoggerFactory;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;
import retrofit2.http.HEAD;

/**
 * Handles all of the reading and writing of messages to and from sockets. You can specify the port
 * number, protocol, and host address to create the socket or the default values of 5060, UDP, and
 * the local host IP address will be used. An instance of this class is passed to the constructors
 * of the transaction manager.
 *
 * @see DsSipTransactionManager#DsSipTransactionManager(DsSipTransportLayer, DsSipRequestInterface)
 * @see DsSipTransactionManager#DsSipTransactionManager(DsSipTransportLayer, DsSipRequestInterface,
 *     int)
 */
public abstract class DsTransportLayer
    implements DsInputStreamEventListener, DsConnectionEventListener {

  // TODO:  Use the DsNetwork instead of these
  protected static final int DEFAULT_INCOMING_SOCK_TIMEOUT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_INCOMING_SOCKET_TIMEOUT,
          DsConfigManager.PROP_INCOMING_SOCKET_TIMEOUT_DEFAULT);

  protected static final int DEFAULT_OUTGOING_SOCK_TIMEOUT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_OUTGOING_SOCKET_TIMEOUT,
          DsConfigManager.PROP_OUTGOING_SOCKET_TIMEOUT_DEFAULT);

  protected int m_IncomingSocketTimeout = DEFAULT_INCOMING_SOCK_TIMEOUT;
  protected int m_OutgoingSocketTimeout = DEFAULT_OUTGOING_SOCK_TIMEOUT;

  /** The SSL context. */
  private DsSSLContext m_context = null;

  private TransportLayer dhruvaTransportLayer;

  private Logger logger = DhruvaLoggerFactory.getLogger(DsTransportLayer.class);

  // /////////////////////////////////////////////////
  //  Constructors
  //
  //

  /**
   * Constructs the transport layer with no listen points.
   *
   * @param network default network settings
   */
  public DsTransportLayer(
      DsNetwork network, MessageForwarder messageForwarder, TransportLayer transportLayer) {
    try {
      init(network, null, messageForwarder, transportLayer);
    } catch (DsException dse) {
      // this arises from the CloneNotSupportedException in init.. a horrible way
      //   to handle this but otherwise, we would have to change the API -dg
      throw new RuntimeException(dse.getMessage());
    }
  }

  private void init(
      DsNetwork defaultNetwork,
      DsSSLContext context,
      MessageForwarder messageForwarder,
      TransportLayer transportLayer)
      throws DsException {
    m_context = context;

    dhruvaTransportLayer = transportLayer;
    // use this context as the default for all networks, maintains backward compatability
    DsNetworkProperties.setDefaultSSLContext(context);
  }

  //  end Constructors
  // ////////////////////////////////////

  /**
   * Adds another port to listen to.
   *
   * @param network default network settings
   * @param port port number
   * @param transport transport type (UDP or TCP)
   * @param localAddress network address of host
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public void listenPort(DsNetwork network, int port, Transport transport, InetAddress localAddress)
      throws DsException, IOException {
    if (network == null) {
      network = DsNetwork.getDefault();
    }
    dhruvaTransportLayer.startListening(transport, network, localAddress, port, null);
  }

  /**
   * Sets the properties of the default system network.
   *
   * @param defaultNetwork the network whose properties are used to set those of the default system
   *     network
   * @deprecated ues {@link DsNetwork#getDefault()} and set its properties via
   */
  public void setDefaultNetwork(DsNetwork defaultNetwork) {
    if (defaultNetwork == null) {
      throw new IllegalArgumentException("default network cannot be null");
    }

    if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
      DsLog4j.connectionCat.warn(
          "default network properties are being reset with values corresponding to : "
              + defaultNetwork.dump()
              + "\n. This change will not be propagated to existing listeners/connections.");
    }
    DsNetwork.getDefault().updateProperties(defaultNetwork.getProperties());
  }

  /**
   * Returns the default network.
   *
   * @return the system default network
   * @deprecated use {@link DsNetwork#getDefault()}
   */
  public DsNetwork getDefaultNetwork() {
    return DsNetwork.getDefault();
  }

  /**
   * Check to see if the connection exists in the cache.
   *
   * @param remoteAddress the address to get the connection for
   * @param remotePort the port to check for
   * @param transport the transport to check for
   * @return the connection or null if a connection does not exist in the cached connections
   */
  public DsConnection findConnection(
      InetAddress remoteAddress, int remotePort, Transport transport) {

    return findConnection(null, 0, remoteAddress, remotePort, transport);
  }

  private DsConnection findConnection(
      InetAddress localAddress,
      int localPort,
      InetAddress remoteAddress,
      int remotePort,
      Transport transport) {

    CompletableFuture<Connection> connectionFuture =
        dhruvaTransportLayer.getCachedConnection(
            transport, localAddress, localPort, remoteAddress, remotePort);
    if (connectionFuture == null) {
      return null;
    }
    Connection connection;
    try {
      connection = connectionFuture.get();
    } catch (Exception exception) {
      logger.error(
          "Exception on getting Connection in DsTransportLayer getConnection for localAddress "
              + localAddress
              + " localPort "
              + localPort
              + " remoteAddress "
              + remoteAddress
              + " remotePort "
              + remotePort,
          " Transport " + transport,
          exception);
      return null;
    }

    return createSIPConnectionFromDhruvaTransportLayerConnection(connection);
  }

  /**
   * Check to see if the connection exists in the cache.
   *
   * @param info binding info to use to search for the connection
   * @return the connection or null if a connection does not exist in the cached connections
   */
  public DsConnection findConnection(DsBindingInfo info) {
    return findConnection(
        info.getLocalAddress(),
        info.getLocalPort(),
        info.getRemoteAddress(),
        info.getRemotePort(),
        info.getTransport());
  }

  /**
   * Check to see if the connection exists in the cache.
   *
   * @param info binding info to use to search for the connection
   * @return the connection or null if a connection does not exist in the cached connections
   */
  public DsConnection findConnection(DsBindingInfo info, boolean checkShutDown) {
    return findConnection(
        info.getLocalAddress(),
        info.getLocalPort(),
        info.getRemoteAddress(),
        info.getRemotePort(),
        info.getTransport());
  }

  /**
   * Used to search for a ListenKey (InetAddress, port, protocol) for a given transport.
   *
   * @param transport the transport type to check for
   * @return a ListenKey whose transport type is 'transport_type' or null if a listen key for the
   *     transport cannot be found
   */
  public ConnectionKey findListenKeyForTransport(Transport transport) {
    return dhruvaTransportLayer.findListenKeyForTransport(transport);
  }

  //    /**
  //     * Determine what transports that this transport layer is listening on.
  //     *
  //     * @return the transports that this transport layer is listening on represented as
  //     *         a set of Integers
  //     */
  //    private Set  getListeningTransports() {
  //        return m_ConnectionTable.getListeningTransports();
  //    }

  /**
   * Get connection object for the supplied local and remote address information in the specified
   * binding information. If one does not exist in the cache, a connection will be established.
   *
   * @param binding_info the binding info for which to obtain a connection
   * @return a DsConnection for the given binding info or null if a connection could not be obtained
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsConnection getConnection(DsBindingInfo binding_info)
      throws SocketException, DsException, UnknownHostException, IOException {
    return getConnection(
        binding_info.getNetwork(),
        binding_info.getLocalAddress(),
        binding_info.getLocalPort(),
        binding_info.getRemoteAddress(),
        binding_info.getRemotePort(),
        binding_info.getTransport());
  }

  /**
   * Get connection object for the supplied binding information. If one does not exist in the cache,
   * a connection will be established.
   *
   * @param addr the address to get the connection for
   * @param port the port to get the connection for
   * @param transport the transport to get the connection object for
   * @return the cached or newly established connection
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsConnection getConnection(InetAddress addr, int port, Transport transport)
      throws SocketException, DsException, UnknownHostException, IOException {
    return getConnection(DsNetwork.getDefault(), addr, port, transport);
  }

  /**
   * Get connection object for the supplied binding information. If one does not exist in the cache,
   * a connection will be established.
   *
   * @param network the network associated with the desired connection
   * @param addr the address to get the connection for
   * @param port the port to get the connection for
   * @param transport the transport to get the connection object for
   * @return the cached or newly established connection
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsConnection getConnection(
      DsNetwork network, InetAddress addr, int port, Transport transport)
      throws SocketException, DsException, UnknownHostException, IOException {
    return getConnection(
        network, null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, addr, port, transport);
  }

  /**
   * Get connection object for the supplied local and remote binding information. If one does not
   * exist in the cache, a connection will be established.
   *
   * @param laddr local address
   * @param lport local port
   * @param addr remote address
   * @param port remote port
   * @param transport the transport type used for this connection
   * @return the connection desired
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsConnection getConnection(
      InetAddress laddr, int lport, InetAddress addr, int port, Transport transport)
      throws SocketException, DsException, UnknownHostException, IOException {
    return getConnection(DsNetwork.getDefault(), laddr, lport, addr, port, transport);
  }

  /**
   * Get connection object for the supplied local and remote binding information. If one does not
   * exist in the cache, a connection will be established.
   *
   * @param network the network associated with the desired connection
   * @param localAddress local address
   * @param localPort local port
   * @param remoteAddress remote address
   * @param remotePort remote port
   * @param transport the transport type used for this connection
   * @return the connection desired
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsConnection getConnection(
      DsNetwork network,
      InetAddress localAddress,
      int localPort,
      InetAddress remoteAddress,
      int remotePort,
      Transport transport)
      throws SocketException, DsException, UnknownHostException, IOException {

    if (network == null) {
      network = DsNetwork.getDefault();
    }

    Connection connection = null;
    CompletableFuture<Connection> connectionFuture =
        dhruvaTransportLayer.getConnection(
            network, transport, localAddress, localPort, remoteAddress, remotePort);

    try {
      connection = connectionFuture.get(network.getTcpConnectionTimeout(), TimeUnit.MILLISECONDS);
    } catch (Exception exception) {
      logger.error(
          "Exception on getting Connection in DsTransportLayer getConnection for localAddress "
              + localAddress
              + " localPort "
              + localPort
              + " remoteAddress "
              + remoteAddress
              + " remotePort "
              + remotePort,
          " Transport " + transport,
          exception);

    }
    return createSIPConnectionFromDhruvaTransportLayerConnection(connection);
  }

  protected DsConnection createSIPConnectionFromDhruvaTransportLayerConnection(
      Connection connection) {
    DsConnection dsConnection = null;
    switch (connection.getConnectionType()) {
      case UDP:
        dsConnection = new DsSipUdpConnection(connection);
        break;
      case TCP:
        dsConnection = new DsSipTcpConnection(connection);
        break;
      case TLS:
        dsConnection = new DsSipTlsConnection(connection, null);
    }
    return dsConnection;
  }

  /**
   * Set the maximum number of TCP/TLS connections that this transport layer should support. When
   * the maximum number of connections is exceeded, the transport layer will no longer accept new
   * connections on TLS/TCP listeners. Once connections are shed due to a timeout, the transport
   * layer will start accepting connections again. Any changes made to this parameter will take
   * effect immediately. In other words, if max connections is exceeded after this call, TCP/TLS
   * listeners will no longer accept connections.
   *
   * @param max_connections the maximum number of connections
   */
  public void setMaxConnections(int max_connections) {
    dhruvaTransportLayer.setMaxConnections(max_connections);
  }

  /**
   * Returns the maximum number of TCP/TLS connections that this transport layer supports.
   *
   * @return the maximum number of TCP/TLS connections
   */
  public int getMaxConnections() {
    return dhruvaTransportLayer.getMaxConnections();
  }

  /**
   * Returns the timeout on incoming TCP/TLS connections. Once a connection is no longer referenced
   * by transactions, the connection will be closed and removed from internal tables after
   * 'time_seconds' seconds.
   *
   * @return the delay in seconds before closing the incoming connection after it is unreferenced.
   */
  public int getIncomingConnectionTimeout() {
    return m_IncomingSocketTimeout;
  }

  /**
   * Set the timeout on incoming TCP/TLS connections. Once a connection is no longer referenced by
   * transactions, the connection will be closed and removed from internal tables after
   * 'time_seconds' seconds.
   *
   * <p>The setting will apply only to connections created after this method is called.
   *
   * @param time_seconds the number of seconds to wait before closing the connection after it is
   *     unreferenced.
   */
  public void setIncomingConnectionTimeout(int time_seconds) {
    m_IncomingSocketTimeout = time_seconds;
  }

  /**
   * Returns the timeout on outgoing TCP/TLS connections. Once a connection is no longer referenced
   * by transactions, the connection will be closed and removed from internal tables after
   * 'time_seconds' seconds.
   *
   * @return the delay in seconds before closing the outgoing connection after it is unreferenced.
   */
  public int getOutgoingConnectionTimeout() {
    return m_OutgoingSocketTimeout;
  }

  /**
   * Set the timeout on outgoing TCP/TLS connections. Once a connection is no longer referenced by
   * transactions, the connection will be closed and removed from internal tables after
   * 'time_seconds' seconds.
   *
   * <p>The setting will apply only to connections created after this method is called.
   *
   * @param time_seconds the number of seconds to wait before closing the connection after it is
   *     unreferenced.
   */
  public void setOutgoingConnectionTimeout(int time_seconds) {
    m_OutgoingSocketTimeout = time_seconds;
  }

  /**
   * Returns the SSL context from the network or for this transport layer. Prefers network. May
   * return <code>null</code>.
   *
   * @return the SSL context for this transport layer
   */
  private DsSSLContext getSSLContext(DsNetwork network) {
    DsSSLContext context = null;

    if (network != null) {
      context = network.getSSLContext();
    }

    if (context == null) {
      context = m_context;
    }

    return context;
  }

  /**
   * Returns the SSL context for this transport layer.
   *
   * @return the SSL context for this transport layer
   */
  public DsSSLContext getSSLContext() {
    return m_context;
  }

  /**
   * Sets the SSL context for this transport layer.
   *
   * @param context the SSL context for this transport layer
   */
  public void setSSLContext(DsSSLContext context) {
    m_context = context;
  }

  /**
   * Callback for a DsInputStreamErrorEvent. Close and remove the connection for this stream.
   *
   * @param err the input stream error event
   */
  /*
  TODO implement connection table cleanup on exception
   */
  public void onDsInputStreamErrorEvent(DsInputStreamErrorEvent err) {}

  /** TODO implement connection table cleanup on exception */
  public void onDsInputStreamClosedEvent(DsInputStreamClosedEvent err) {}

  /**
   * TODO implement connection table cleanup on exception
   *
   * @param evt event to handle
   */
  public void onDsConnectionClosedEvent(DsConnectionClosedEvent evt) {}

  /**
   * TODO implement connection table cleanup on exception
   *
   * @param evt event to handle
   */
  public void onDsConnectionErrorEvent(DsConnectionErrorEvent evt) {}

  /**
   * TODO implement connection table cleanup on exception
   *
   * @param evt event to handle
   */
  public void onDsConnectionIcmpErrorEvent(DsConnectionIcmpErrorEvent evt) {}

  /**
   * Get the key objects associated with the listening ports setup.
   *
   * @return enumeration of keys associated with the listening ports for this transport
   */
  public Enumeration getListenKeys() {
    return dhruvaTransportLayer.getListenKeys();
  }

  /**
   * Determine whether or not we are listening on a given interface, port and transport.
   *
   * @param host the host name
   * @param port the port
   * @param transport the transport
   * @return <code>true</code> if there is a listening interface for the specied interface, port and
   *     transport.
   */
  public boolean isListenInterface(String host, int port, Transport transport) {
    Enumeration sockets = getListenKeys();
    ConnectionKey socket;
    InetAddress addr;

    while (sockets.hasMoreElements()) {
      try {
        socket = (ConnectionKey) sockets.nextElement();
        addr = InetAddress.getByName(host);
        if (socket.equals(new ConnectionKey(addr, port, transport))) {
          if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
            DsLog4j.connectionCat.info("Possible loop on local interface: " + socket);
          }
          return true;
        }
      } catch (Exception e) {
        if (DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
          DsLog4j.connectionCat.error("Exception in isListenInterface: ", e);
        }
      }
    }
    return false;
  }

  /**
   * Callback for a DsInputStreamEvent.
   *
   * @param event the input stream event
   */
  public void onDsInputStreamEvent(DsInputStreamEvent event) {
    // Even though this method is not used, it is needed until
    // the DsInputStream class is retired. It will never be called.
  }

  /*
  TODO
   */
  public void stop() {}

  /*
  TODO
   */
  public char[] dump() {

    return null;
  }

  // TODO take care of removing connection
  public void removeConnection(DsSipConnection connection) {}
}
