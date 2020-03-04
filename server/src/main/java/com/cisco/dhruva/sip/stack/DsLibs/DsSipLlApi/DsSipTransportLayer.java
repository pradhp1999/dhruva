// Copyright (c) 2005-2011 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.transport.MessageForwarder;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.transport.TransportLayer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This class handles all interaction with the reading and sending of messages from a socket. You
 * can specify the port number, protocol , and host address to create the socket or the default
 * values of 5060, UDP, host IP will be used. An instance of this class is passed to the
 * constructors of the transaction manager, mid-level call manager and high-level call manager.
 *
 * <h3>Constructing the Transport Layer</h3>
 *
 * The constructors that automatically start listening have been deprecated. There is a race
 * condition that can occur if the stack receives a packet before the transaction manager has been
 * created. If this happens, the stack will 'hang' and no messages will be processed. This would be
 * a typical case if you were to re-start a server on a busy network.
 *
 * <p>To avoid this race condition, simply create the transport layer first, then the transaction
 * manager, then begin listening for SIP messages. For Example:
 *
 * <p>
 *
 * <pre><code>
 *     // First create a transport layer
 *     DsSipTransportLayer transportLayer = new DsSipTransportLayer();
 *
 *     try
 *     {
 *         // Then use that transport layer to create the transaction manager
 *         DsSipTransactionManager transactionManager = new DsSipTransactionManager(transportLayer, null);
 *
 *         // Now that there is a transaction manager, it is OK to begin listening
 *         transportLayer.listenPort(port, transportType, host);
 *     }
 *     catch (DsException)
 *     {
 *         // handle exception
 *     }
 * </code></pre>
 *
 * <p>Then use appropriate methods to set the addition parameters.
 *
 * <p>
 *
 * <h3>Settting a Local Outbound Proxy</h3>
 *
 * RFC 3263 (SIP: Locating SIP Servers) specifies how a URI is resolved into a list of locations (IP
 * address, port, transport) to which to try to send a request. The URI specifies a TARGET (IP
 * address or fully qualified domain name) and optionally, a transport and port.
 * setLocalOutboundProxy(DsSipURL)} method allows the application code to specify the URI of a local
 * outbound proxy. If a request arrives at the transport layer without a Route header, one will be
 * constructed and added to the request using this URI. If a Route header is present in the message
 * when it reaches the transport layer, the local outbound proxy Route header will not be added.
 * This will cause the message to be routed based in the top Route header and not the local outbound
 * proxy URI.
 *
 * <p>The application code may want to control the destination of requests without regard to the
 * content of the SIP message. To accomplish this, the method #setLocalOutboundProxy(DsBindingInfo)
 * can be used. When the proxy binding is set in this way, the Route header of the local outbound
 * proxy will still be inserted if a Route is not present but the TARGET, port and transport of the
 * provided DsBindingInfo will be used to override the Route header TARGET, port and transport.
 */
public class DsSipTransportLayer extends DsTransportLayer {
  private static boolean m_closeConnOnTimeout =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CLOSE_CONNECTION_ON_TIMEOUT,
          DsConfigManager.PROP_CLOSE_CONNECTION_ON_TIMEOUT_DEFAULT);

  private DsBindingInfo m_proxyBinding;
  private DsSipURL m_proxyURL;

  static {
    // CAFFEINE 2.0 DEVELOPMENT - Adding XTCP feature.
    boolean tcpTcpFlowControl =
        DsConfigManager.getProperty(DsConfigManager.PROP_XTCP, DsConfigManager.PROP_XTCP_DEFAULT);
    // use a queue that blocks the IO thread when it becomes full exerting TCP backpressure
    //    to the network
  }

  // /////////////////////////////
  //  Constructors
  // /////////////////////////////

  /** Constructs the transport layer with no listen ports. */
  public DsSipTransportLayer() {
    this(null, null, null);
  }

  // ///////////////   DsNetwork versions of constructors

  /**
   * Constructs the transport layer with no listen ports.
   *
   * @param defaultNetwork the network to use as the default
   */
  public DsSipTransportLayer(
      DsNetwork defaultNetwork, MessageForwarder messageForwarder, TransportLayer transportLayer) {
    super(defaultNetwork, messageForwarder, transportLayer);
    /*
       This is safe if there is only one transport layer,
       which the class claims in the above comments.
    */
  }

  //  end Constructors
  // ////////////////////////////////

  public DsConnection getConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port,
      Transport transport)
      throws SocketException, DsException, UnknownHostException, IOException {
    return this.getConnection(network, laddr, lport, addr, port, transport, false);
  }

  /**
   * Get connection object for the supplied local and remote binding information. If one does not
   * exist in the cache, a connection will be established.
   *
   * @param network the network associated with the desired connection
   * @param laddr local address
   * @param lport local port
   * @param addr remote address
   * @param port remote port
   * @param transport the transport type used for this connection
   * @param forceListener if the listening socket associated with laddr and lport should be used by
   *     this connection for sending data
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
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port,
      Transport transport,
      boolean forceListener)
      throws SocketException, DsException, UnknownHostException, IOException {
    if (network == null) {
      network = DsNetwork.getDefault();
    }
    return super.getConnection(network, laddr, lport, addr, port, transport);
  }

  /**
   * Looks up the listen connection for the given parameters
   *
   * @param network the network associated with the connection
   * @param laddr local address
   * @param lport local port number
   * @param addr network address of host
   * @param port port number to listen on
   * @param transport transport type (UDP or TCP)
   * @return the looked up connection
   * @throws DsException if laddr is null or lport equals DsBindingInfo.LOCAL_PORT_UNSPECIFIED
   */
  public DsSipConnection findListenConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port,
      Transport transport)
      throws DsException {

    // TODO take care of this
    /* if (network != null) {
      if ((laddr == null || lport == DsBindingInfo.LOCAL_PORT_UNSPECIFIED)
          && transport == Transport.UDP
          && network.isBehindNAT()) {
        DsUdpListener listener = network.getUdpListener();
        if (listener != null) {
          laddr = listener.m_address;
          lport = listener.m_port;
        }
      }
    }

    if (laddr == null) {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.debug("local address must be specified for NAT Traversal");
      }

      throw new DsException("local address must be specified for NAT Traversal");
    }
    if (lport == DsBindingInfo.LOCAL_PORT_UNSPECIFIED) {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.debug("local port must be specified for NAT Traversal");
      }

      throw new DsException("local port must be specified for NAT Traversal");
    }

    DsSipConnection connection =
        (DsSipConnection) m_ConnectionTable.getTransportListener(laddr, lport, transport);

    return (DsSipConnection) validateConnectionOnTransport(connection, transport);
    */
    return null;
  }

  /**
   * If a local outbound proxy is set, get a connection to it.
   *
   * @return a connection to the local outbound proxy or null if it is not set.
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsConnection getConnection()
      throws SocketException, DsException, UnknownHostException, IOException {
    DsConnection connection = null;
    if (m_proxyURL != null) {
      DsBindingInfo info = m_proxyURL.getBindingInfo();
      if (m_proxyBinding.isRemoteAddressSet()) {
        info.setRemoteAddress(m_proxyBinding.getRemoteAddress());
      }
      if (m_proxyBinding.isRemotePortSet()) {
        info.setRemotePort(m_proxyBinding.getRemotePort());
      }
      if (m_proxyBinding.isTransportSet()) {
        info.setTransport(m_proxyBinding.getTransport());
      }

      connection = getConnection(info);
    } else if (m_proxyBinding != null) {
      connection = getConnection(m_proxyBinding);
    }

    return connection;
  }

  /**
   * Get the proxy port set.
   *
   * @return the local proxy port or DsBindingInfo.REMOTE_PORT_UNSPECIFIED if no port has been set
   */
  public int getLocalProxyPort() {
    int port = DsBindingInfo.REMOTE_PORT_UNSPECIFIED;
    if (m_proxyBinding != null) {
      if (m_proxyBinding.isRemotePortSet()) {
        port = m_proxyBinding.getRemotePort();
      }
    } else if (m_proxyURL != null) {
      if (m_proxyURL.hasPort()) {
        return m_proxyURL.getPort();
      }
    }

    return port;
  }

  /**
   * Get the local proxy transport.
   *
   * @return the local proxy transport or DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED if no
   *     transport has been set
   */
  public Transport getLocalProxyTransport() {
    Transport transport = DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED;
    if (m_proxyBinding != null) {
      if (m_proxyBinding.isTransportSet()) {
        transport = m_proxyBinding.getTransport();
      }
    } else if (m_proxyURL != null) {
      // GOGONG - 07.13.05 - Return binding transport unspecified if outgoing transport type is not
      // specified
      Transport transportParam = m_proxyURL.getTransportParam();
      if (transportParam != Transport.NONE) {
        return transportParam;
      }
    }

    return transport;
  }

  /**
   * Get the local proxy address.
   *
   * @return the address of the local outbound proxy represented as a String or null if it has not
   *     been set.
   */
  public String getLocalProxyAddress() {
    String host = null;
    if (m_proxyBinding != null) {
      if (m_proxyBinding.isRemoteAddressSet()) {
        host = m_proxyBinding.getRemoteAddressStr();
      }
    } else if (m_proxyURL != null) {
      DsByteString host_ = m_proxyURL.getHost();
      if (host_ == null) {
        host = null;
      } else {
        host = m_proxyURL.getHost().toString();
      }
    }

    return host;
  }

  /**
   * Copy a route header into this request for routing to the local outbound proxy.
   *
   * @param request the SIP request to add the Route header to.
   */
  void setLocalProxyRoute(DsSipRequest request) {
    if (m_proxyURL != null) {
      if (request.getHeader(DsSipConstants.ROUTE) == null) {
        // clone the URI since it might be modified by the application
        request.addHeader(
            new DsSipRouteHeader((DsSipURL) m_proxyURL.clone()),
            true, // start
            false); // clone
      }
    }

    if (m_proxyBinding != null) {
      request.setBindingInfo((DsBindingInfo) m_proxyBinding.clone());
    }
  }

  /**
   * Gets the value of the close connection on SIP transaction timeout parameter.
   *
   * @return the close connection on SIP transaction timeout parameter
   */
  public static boolean getCloseConnectionOnTimeout() {
    return m_closeConnOnTimeout;
  }

  /**
   * Sets the value of the close connection on SIP transaction timeout parameter.
   *
   * @param closeConn use <code>true</code> to close TCP/TLS connections after SIP transaction
   *     timeouts, use <code>false</code> to leave them open
   */
  public static void setCloseConnectionOnTimeout(boolean closeConn) {
    m_closeConnOnTimeout = closeConn;
  }
}
