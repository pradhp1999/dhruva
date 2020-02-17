// Copyright (c) 2005-2011 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipURL;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsQueueInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSocket;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsThread;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsWorkQueue;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Level;

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
 * address or fully qualified domain name) and optionally, a transport and port. The {@link
 * #setLocalOutboundProxy(DsSipURL)} method allows the application code to specify the URI of a
 * local outbound proxy. If a request arrives at the transport layer without a Route header, one
 * will be constructed and added to the request using this URI. If a Route header is present in the
 * message when it reaches the transport layer, the local outbound proxy Route header will not be
 * added. This will cause the message to be routed based in the top Route header and not the local
 * outbound proxy URI.
 *
 * <p>The application code may want to control the destination of requests without regard to the
 * content of the SIP message. To accomplish this, the method {@link
 * #setLocalOutboundProxy(DsBindingInfo)} can be used. When the proxy binding is set in this way,
 * the Route header of the local outbound proxy will still be inserted if a Route is not present but
 * the TARGET, port and transport of the provided DsBindingInfo will be used to override the Route
 * header TARGET, port and transport.
 */
public class DsSipTransportLayer extends DsTransportLayer {
  private static boolean m_closeConnOnTimeout =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_CLOSE_CONNECTION_ON_TIMEOUT,
          DsConfigManager.PROP_CLOSE_CONNECTION_ON_TIMEOUT_DEFAULT);

  private static final boolean IS_NON_BLOCKING_TCP =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NON_BLOCKING_TCP, DsConfigManager.PROP_NON_BLOCKING_TCP_DEFAULT);

  private static final boolean IS_NON_BLOCKING_TLS =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NON_BLOCKING_TLS, DsConfigManager.PROP_NON_BLOCKING_TLS_DEFAULT);

  private DsBindingInfo m_proxyBinding;
  private DsSipURL m_proxyURL;

  /** Maximum number of worker threads to allocate for processing the work queue. */
  private static final int MAX_WORKER_THREADS = 20;

  /** Maximum length of the work queue. */
  private static final int MAX_WORK_QUEUE = 2000;
  /** Queue of work to process. */
  private static Executor workQueue;

  /** The way to create new connections. */
  private static DsConnectionFactory m_ConnectionFactory = new DsSipConnectionFactory();

  // GOGONG 04.12.06 CSCsd90062 - keep track number fo threads being created
  // Used for creating nice Thread names.
  private static int nr = 0;

  static {
    // CAFFEINE 2.0 DEVELOPMENT - Adding XTCP feature.
    boolean tcpTcpFlowControl =
        DsConfigManager.getProperty(DsConfigManager.PROP_XTCP, DsConfigManager.PROP_XTCP_DEFAULT);
    // use a queue that blocks the IO thread when it becomes full exerting TCP backpressure
    //    to the network
    if (tcpTcpFlowControl) {
      int nthreads =
          DsConfigManager.getProperty(DsConfigManager.PROP_XTCP_THREADS, MAX_WORKER_THREADS);
      int qlength = DsConfigManager.getProperty(DsConfigManager.PROP_XTCP_QLEN, MAX_WORK_QUEUE);
      workQueue =
          new ThreadPoolExecutor(
              nthreads,
              nthreads,
              0L,
              TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<Runnable>(qlength));

      ((ThreadPoolExecutor) workQueue)
          .setThreadFactory(
              new ThreadFactory() {
                public Thread newThread(Runnable r) {
                  // GOGONG 04.12.06 CSCsd90062 - specify a name to thread for debugging purpose
                  return new DsThread(r, "DsSipTransportLayer-" + (nr++));
                }
              });
    } else {
      workQueue = new DsWorkQueue(DsWorkQueue.DATA_IN_QNAME, MAX_WORK_QUEUE, MAX_WORKER_THREADS);
      DsConfigManager.registerQueue((DsQueueInterface) workQueue);
    }
  }

  // /////////////////////////////
  //  Constructors
  // /////////////////////////////

  /** Constructs the transport layer with no listen ports. */
  public DsSipTransportLayer() {
    this(null);
  }

  /**
   * Constructs the transport layer with the specified transport parameters.
   *
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   * @deprecated see class comment.
   */
  public DsSipTransportLayer(int port, int transport, InetAddress addr)
      throws DsException, IOException {
    this(null, port, transport, addr);
  }

  /**
   * Constructs the transport layer with the specified transport parameters and the specified key
   * store to activate the TLS/SSL transport type. Initiate the SSL context to the specified key
   * store. All the SSL sockets connections created in this transport layer would have this SSL
   * context.
   *
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @param keyStoreFile the path for the key store file which contains the private/public key
   *     information
   * @param password the password for the keys in the key store.
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   * @deprecated see class comment.
   */
  public DsSipTransportLayer(
      int port, int transport, InetAddress addr, String keyStoreFile, String password)
      throws DsException, IOException, DsSSLException {
    this(null, port, transport, addr, keyStoreFile, password);
  }

  /**
   * Constructs the transport layer with the specified transport parameters and SSL context for the
   * TLS/SSL transport type.
   *
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @param context the SSL context which will be used to initiate the SSL connections.
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   * @deprecated see class comment.
   */
  public DsSipTransportLayer(int port, int transport, InetAddress addr, DsSSLContext context)
      throws DsException, IOException, DsSSLException {
    this(null, port, transport, addr, context);
  }

  // ///////////////   DsNetwork versions of constructors

  /**
   * Constructs the transport layer with no listen ports.
   *
   * @param defaultNetwork the network to use as the default
   */
  public DsSipTransportLayer(DsNetwork defaultNetwork) {
    super(defaultNetwork, workQueue, m_ConnectionFactory);
    /*
       This is safe if there is only one transport layer,
       which the class claims in the above comments.
    */
    DsSipServerLocator.setConnectionTable(m_ConnectionTable);
    DsSipServerLocator.setConnectionFactory(m_ConnectionFactory);

    DsTcpNBConnection.setTransportLayer(this);
  }

  /**
   * Constructs the transport layer with the specified transport parameters.
   *
   * @param defaultNetwork the network to use as the default
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   * @deprecated see class comment.
   */
  public DsSipTransportLayer(DsNetwork defaultNetwork, int port, int transport, InetAddress addr)
      throws DsException, IOException {
    super(defaultNetwork, workQueue, m_ConnectionFactory, port, transport, addr);
    /*
       This is safe if there is only one transport layer,
       which the class claims in the above comments.
    */
    DsSipServerLocator.setConnectionTable(m_ConnectionTable);
    DsSipServerLocator.setConnectionFactory(m_ConnectionFactory);

    DsTcpNBConnection.setTransportLayer(this);
  }

  /**
   * Constructs the transport layer with the specified transport parameters and the specified key
   * store to activate the TLS/SSL transport type. Initiate the SSL context to the specified key
   * store. All the SSL sockets connections created in this transport layer would have this SSL
   * context.
   *
   * @param defaultNetwork the network to use as the default
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @param keyStoreFile the path for the key store file which contains the private/public key
   *     information
   * @param password the password for the keys in the key store.
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   * @deprecated see class comment.
   */
  public DsSipTransportLayer(
      DsNetwork defaultNetwork,
      int port,
      int transport,
      InetAddress addr,
      String keyStoreFile,
      String password)
      throws DsException, IOException, DsSSLException {
    super(
        defaultNetwork,
        workQueue,
        m_ConnectionFactory,
        port,
        transport,
        addr,
        keyStoreFile,
        password);
    /*
       This is safe if there is only one transport layer,
       which the class claims in the above comments.
    */
    DsSipServerLocator.setConnectionTable(m_ConnectionTable);
    DsSipServerLocator.setConnectionFactory(m_ConnectionFactory);

    DsTcpNBConnection.setTransportLayer(this);
  }

  /**
   * Constructs the transport layer with the specified transport parameters and SSL context for the
   * TLS/SSL transport type.
   *
   * @param defaultNetwork the network to use as the default
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @param context the SSL context which will be used to initiate the SSL connections.
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   * @deprecated see class comment.
   */
  public DsSipTransportLayer(
      DsNetwork defaultNetwork, int port, int transport, InetAddress addr, DsSSLContext context)
      throws DsException, IOException, DsSSLException {
    super(defaultNetwork, workQueue, m_ConnectionFactory, port, transport, addr, context);
    /*
       This is safe if there is only one transport layer,
       which the class claims in the above comments.
    */
    DsSipServerLocator.setConnectionTable(m_ConnectionTable);
    DsSipServerLocator.setConnectionFactory(m_ConnectionFactory);

    DsTcpNBConnection.setTransportLayer(this);
  }

  //  end Constructors
  // ////////////////////////////////

  protected void setConnectionFactory(DsConnectionFactory factory) {
    super.setConnectionFactory(factory);
    DsSipServerLocator.setConnectionFactory(factory);
  }

  public DsConnection getConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port, int transport)
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
      int transport,
      boolean forceListener)
      throws SocketException, DsException, UnknownHostException, IOException {
    if (network == null) {
      network = DsNetwork.getDefault();
    }

    if (transport == DsSipTransportType.UDP && (network.isBehindNAT() || forceListener)) {
      synchronized (this) {
        DsSipConnection conn = findListenConnection(network, laddr, lport, addr, port, transport);

        if (conn != null) {
          DsBindingInfo bi = (DsBindingInfo) conn.getBindingInfo().clone();
          bi.setRemoteAddress(addr);
          bi.setRemotePort(port);

          DsConnection udpconn = m_ConnectionTable.get(bi);

          if (udpconn == null) {
            if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
              DsLog4j.connectionCat.debug(
                  "Enabling Persistent Connection getConnection() by adding"
                      + " listener-connection wrapper to connection table;"
                      + " binding info:\n"
                      + bi);
            }

            // this should not ever be needed because the listener's connection id
            // should never be non-null
            //      bi.setConnectionId(null);
            udpconn = new DsUdpConnectionWrapper(bi, (DsUdpListener) conn);
            udpconn.updateTimeStamp();
            udpconn.setTimeout(m_OutgoingSocketTimeout);
            m_ConnectionTable.put(udpconn);
          } else if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
            DsLog4j.connectionCat.debug(
                "returning existing listener-connection wrapper from connection table: " + udpconn);
          }

          if (DsLog4j.connectionCat.isEnabled(Level.WARN) && bi.getConnectionId() != null) {
            DsLog4j.connectionCat.warn(
                "listener connection ID is non-null: " + bi.getConnectionId().toString());
          }

          return validateConnectionOnTransport(udpconn, transport);
        }

        if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
          DsLog4j.connectionCat.warn(
              "no listener associated with this NAT'd network, local port and address combination: "
                  + network.getName()
                  + ":"
                  + laddr
                  + ":"
                  + lport);
        }

        return conn;
      }
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
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port, int transport)
      throws DsException {
    if (network != null) {
      if ((laddr == null || lport == DsBindingInfo.LOCAL_PORT_UNSPECIFIED)
          && transport == DsSipTransportType.UDP
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
  }

  protected DsMessageReader createMessageReader(
      InputStream is, Executor queue, DsSocket socket, DsBindingInfo binfo) {
    return new DsSipMessageReader(is, queue, socket, binfo);
  }

  protected DsMulticastListener createMulticastListener(
      DsNetwork network, int port, InetAddress addr, Executor queue)
      throws IOException, UnknownHostException {
    return new DsMulticastListener(network, port, addr, queue);
  }

  protected DsUdpListener createUdpListener(
      DsNetwork network, int port, InetAddress addr, Executor queue)
      throws IOException, UnknownHostException {
    // Create a listen point that is also capable of sending.
    // Since any element that receives an rport parameter in a Via header
    // must be able to send a response sourced from the listen point, we
    // create a DsSipNatUdpListener here.
    // We leave the DsUdpListener alone because a UDP listener should not
    // necessarily know anything about sending.
    return new DsSipNatUdpListener(network, port, addr, queue);
  }

  protected DsSslListener createSslListener(
      DsNetwork network,
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory,
      DsSSLContext context)
      throws SocketException, UnknownHostException, IOException {
    if (IS_NON_BLOCKING_TLS) {
      return new DsTlsNBListener(
          network, port, address, connectionTable, connectionFactory, context);
    } else {
      return new DsSslListener(network, port, address, connectionTable, connectionFactory, context);
    }
  }

  protected DsTcpListener createTcpListener(
      DsNetwork network,
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory)
      throws SocketException, UnknownHostException, IOException {
    if (IS_NON_BLOCKING_TCP) {
      return new DsTcpNBListener(network, port, address, connectionTable, connectionFactory);
    } else {
      return new DsTcpListener(network, port, address, connectionTable, connectionFactory);
    }
  }

  /**
   * Set the local outbound proxy binding (overrides the URI of {@link
   * #setLocalOutboundProxy(DsSipURL)}.
   *
   * <p>The application code may want to control the destination of requests without regard to the
   * content of the SIP message. When the proxy binding is set using this method, the Route header
   * of the local outbound proxy will still be inserted if a Route is not present but the TARGET,
   * port and transport of the provided DsBindingInfo will be used to override the Route header
   * TARGET, port and transport.
   *
   * @see #setLocalOutboundProxy(DsSipURL)
   * @param binding binding information
   */
  public void setLocalOutboundProxy(DsBindingInfo binding) {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG))
      DsLog4j.connectionCat.debug("Setting Local Outbound Proxy Binding - " + binding);

    if (binding == null) {
      m_proxyBinding = null;
    } else {
      m_proxyBinding = (DsBindingInfo) binding.clone();
    }
  }

  /**
   * Set the local outbound proxy binding -- overrides the URI of {@link
   * #setLocalOutboundProxy(DsSipURL)}. This method calls {@link
   * #setLocalOutboundProxy(DsBindingInfo)}.
   *
   * @see #setLocalOutboundProxy(DsBindingInfo)
   * @param proxyaddr the network address of the local proxy
   * @param port port number local proxy is listening on
   * @param transport transport type (UDP or TCP)
   */
  public void setLocalOutboundProxy(InetAddress proxyaddr, int port, int transport) {
    setLocalOutboundProxy(new DsBindingInfo(proxyaddr, port, transport));
  }

  /**
   * Causes the transport layer to send all messages through a local proxy.
   *
   * @deprecated local address and port are ignored. Use {@link
   *     #setLocalOutboundProxy(DsBindingInfo)}.
   * @param laddr local address
   * @param lport local port number
   * @param proxyaddr the network address of the local proxy
   * @param port port number local proxy is listening on
   * @param transport transport type (UDP or TCP)
   */
  public void setLocalOutboundProxy(
      InetAddress laddr, int lport, InetAddress proxyaddr, int port, int transport) {
    setLocalOutboundProxy(proxyaddr, port, transport);
  }

  /**
   * Set or unset the local outbound proxy. Passing a propertly constructed SIP URL causes the
   * transport layer to send all messages through a local proxy. Passing null causes the local
   * outbound proxy to be unset.
   *
   * <p>The application code is responsible for setting the lr parameter of this SIP URL when the
   * local outbound proxy is known to be a loose router or leaving it unset when the local outbound
   * proxy is known to be a strict router. The provided URL is cloned by this method.
   *
   * <p>RFC 3263 (SIP: Locating SIP Servers) specifies how a URI is resolved into a list of
   * locations (IP address, port, transport) to which to try to send a request. The URI specifies a
   * TARGET (IP address or fully qualified domain name) and optionally, a transport and port. This
   * method allows the application code to specify the URI of a local outbound proxy. If a request
   * arrives at the transport layer without a Route header, one will be constructed and added to the
   * request using this URI. If a Route header is present in the message when it reaches the
   * transport layer, the local outbound proxy Route header will not be added. This will cause the
   * message to be routed based in the top Route header and not the local outbound proxy URI.
   *
   * @see #setLocalOutboundProxy(DsBindingInfo)
   * @param url the url the SIP URL to use in the Route header used to route to the local outbound
   *     proxy. The value should be set to null to unset the local outbound proxy.
   */
  public void setLocalOutboundProxy(DsSipURL url) {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG))
      DsLog4j.connectionCat.debug("Setting Local Outbound Proxy URL - " + url);
    if (url != null) {
      m_proxyURL = (DsSipURL) url.clone();
    } else {
      m_proxyURL = null;
    }
  }

  /**
   * Determine whether or not a local outbound proxy is set.
   *
   * @return <code>true</code> if a local outbound proxy was set, otherwise return <code>false
   *     </code>
   */
  public boolean isLocalProxySet() {
    return (m_proxyURL != null) || (m_proxyBinding != null);
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
  public int getLocalProxyTransport() {
    int transport = DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED;
    if (m_proxyBinding != null) {
      if (m_proxyBinding.isTransportSet()) {
        transport = m_proxyBinding.getTransport();
      }
    } else if (m_proxyURL != null) {
      // GOGONG - 07.13.05 - Return binding transport unspecified if outgoing transport type is not
      // specified
      int transportParam = m_proxyURL.getTransportParam();
      if (transportParam != DsSipTransportType.NONE) {
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

  /** Needed by DsSipMessageReader. */
  static Executor getWorkQueue() {
    return workQueue;
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
