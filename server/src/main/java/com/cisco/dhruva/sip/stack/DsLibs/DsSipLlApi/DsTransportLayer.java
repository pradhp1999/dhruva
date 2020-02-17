// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamClosedEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamErrorEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamEventListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetworkProperties;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSocket;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsTimer;
import com.cisco.dhruva.util.saevent.ConnectionSAEventBuilder;
import com.cisco.dhruva.util.saevent.EventBase.EventLevel;
import com.cisco.dhruva.util.saevent.SAEventConstants;
import com.cisco.dhruva.util.saevent.dataparam.InvalidConnectionDataParam;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import org.apache.logging.log4j.Level;

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
  private static final boolean IS_NON_BLOCKING_TCP =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NON_BLOCKING_TCP, DsConfigManager.PROP_NON_BLOCKING_TCP_DEFAULT);

  private static final boolean IS_NON_BLOCKING_TLS =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_NON_BLOCKING_TLS, DsConfigManager.PROP_NON_BLOCKING_TLS_DEFAULT);

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
  private static int inputBufferSize = 2048;

  /** The table that contains all of the connections. */
  protected DsConnectionTable m_ConnectionTable = null;

  /** The listening address of the transport layer. */
  private InetAddress m_ListeningAddress;
  /** The listening port of the transport layer. */
  private int m_ListeningPort;
  /** The listening transport. */
  private int m_ListeningTransport;

  /** The listener that receives events when a listen port is removed. */
  private RemoveListenPortListener removeListener;
  /** The SSL context. */
  private DsSSLContext m_context = null;

  /** The connection factory. */
  private DsConnectionFactory m_ConnectionFactory;
  /** Queue of work to process. */
  private Executor workQueue;

  /*
   * Lock on m_ConnectionTable access.
   */
  private ReentrantLock mTableLock;

  static {
    // TODO: integrate with DsNetwork
    String sz = System.getProperty("com.dynamicsoft.DsLibs.DsSipLlApi.inputBufferSize", "0");
    int size = Integer.parseInt(sz);
    if (size > 0) {
      inputBufferSize = size;
    }
  }

  // /////////////////////////////////////////////////
  //  Constructors
  //
  //

  /**
   * Constructs the transport layer with no listen points.
   *
   * @param queue the queue used to process work
   * @param factory the connection factory
   */
  public DsTransportLayer(Executor queue, DsConnectionFactory factory) {
    try {
      init(null, queue, factory, null);
    } catch (DsException dse) {
      // this arises from the CloneNotSupportedException in init.. a horrible way
      //   to handle this but otherwise, we would have to change the API -dg
      throw new RuntimeException(dse.getMessage());
    }
  }

  /**
   * Constructs the transport layer with no listen points.
   *
   * @param network default network settings
   * @param queue the queue used to process work
   * @param factory the connection factory
   */
  public DsTransportLayer(DsNetwork network, Executor queue, DsConnectionFactory factory) {
    try {
      init(network, queue, factory, null);
    } catch (DsException dse) {
      // this arises from the CloneNotSupportedException in init.. a horrible way
      //   to handle this but otherwise, we would have to change the API -dg
      throw new RuntimeException(dse.getMessage());
    }
  }

  /**
   * Constructs the transport layer with the specified transport parameters.
   *
   * @param queue the queue used to process work
   * @param factory the connection factory
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public DsTransportLayer(
      Executor queue, DsConnectionFactory factory, int port, int transport, InetAddress addr)
      throws DsException, IOException {
    this(null, queue, factory, port, transport, addr, null);
  }

  /**
   * Constructs the transport layer with the specified transport parameters and the specified key
   * store to activate the TLS/SSL transport type. Initiate the SSL context to the specified key
   * store. All the SSL sockets connections created in this transport layer would have this SSL
   * context.
   *
   * @param queue the queue used to process work
   * @param factory the connection factory
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @param keyStoreFile the path for the key store file which contains the private/public key
   *     information
   * @param password the password for the keys in the key store.
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public DsTransportLayer(
      Executor queue,
      DsConnectionFactory factory,
      int port,
      int transport,
      InetAddress addr,
      String keyStoreFile,
      String password)
      throws DsException, IOException, DsSSLException {
    this(null, queue, factory, port, transport, addr, new DsSSLContext(keyStoreFile, password));
  }

  /**
   * Constructs the transport layer with the specified transport parameters and SSL context for the
   * TLS/SSL transport type.
   *
   * @param queue the queue used to process work
   * @param factory the connection factory
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @param context the SSL context which will be used to initiate the SSL connections.
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public DsTransportLayer(
      Executor queue,
      DsConnectionFactory factory,
      int port,
      int transport,
      InetAddress addr,
      DsSSLContext context)
      throws DsException, IOException, DsSSLException {
    this(null, queue, factory, port, transport, addr, context);
  }

  /**
   * Constructs the transport layer with the specified transport parameters and SSL context for the
   * TLS/SSL transport type.
   *
   * @param defaultNetwork default network settings
   * @param queue the queue used to process work
   * @param factory the connection factory
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public DsTransportLayer(
      DsNetwork defaultNetwork,
      Executor queue,
      DsConnectionFactory factory,
      int port,
      int transport,
      InetAddress addr)
      throws DsException, IOException {
    this(defaultNetwork, queue, factory, port, transport, addr, null);
  }

  /**
   * Constructs the transport layer with the specified transport parameters and SSL context for the
   * TLS/SSL transport type.
   *
   * @param defaultNetwork default network settings
   * @param queue the queue used to process work
   * @param factory the connection factory
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @param keyStoreFile the path for the key store file which contains the private/public key
   *     information
   * @param password the password for the keys in the key store.
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public DsTransportLayer(
      DsNetwork defaultNetwork,
      Executor queue,
      DsConnectionFactory factory,
      int port,
      int transport,
      InetAddress addr,
      String keyStoreFile,
      String password)
      throws DsException, IOException, DsSSLException {
    this(
        defaultNetwork,
        queue,
        factory,
        port,
        transport,
        addr,
        new DsSSLContext(keyStoreFile, password));
  }

  /**
   * Constructs the transport layer with the specified transport parameters and SSL context for the
   * TLS/SSL transport type.
   *
   * @param defaultNetwork default network settings
   * @param queue the queue used to process work
   * @param factory the connection factory
   * @param port port number to listen on
   * @param transport transport type
   * @param addr network address of host
   * @param context the SSL context which will be used to initiate the SSL connections.
   * @throws DsSSLException if an error occurs while initiating the SSL context
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public DsTransportLayer(
      DsNetwork defaultNetwork,
      Executor queue,
      DsConnectionFactory factory,
      int port,
      int transport,
      InetAddress addr,
      DsSSLContext context)
      throws DsException, IOException, DsSSLException {

    init(defaultNetwork, queue, factory, context);

    if ((port != DsBindingInfo.LOCAL_PORT_UNSPECIFIED)
        && (transport != DsBindingInfo.BINDING_TRANSPORT_UNSPECIFIED)
        && (addr != null)) {
      listenPort(port, transport, addr);
    }
  }

  private void init(
      DsNetwork defaultNetwork, Executor queue, DsConnectionFactory factory, DsSSLContext context)
      throws DsException {
    workQueue = queue;
    m_ConnectionFactory = factory;
    m_context = context;

    // use this context as the default for all networks, maintains backward compatability
    DsNetworkProperties.setDefaultSSLContext(context);

    m_ConnectionTable = new DsConnectionTable();
    mTableLock = new ReentrantLock();
    if (defaultNetwork == null) {
      if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
        DsLog4j.connectionCat.info("using default default network (user's is null)");
      }
    }
    /*
    try
    {
        this.m_defaultNetwork = defaultNetwork != null ? defaultNetwork : (DsNetwork) DsNetwork.getDefault().clone();
    }
    catch (CloneNotSupportedException exc)
    {
        throw new DsException(exc);

    }
    */
    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.info("default network settings are:\n" + DsNetwork.getDefault().dump());
    }

    removeListener = new RemoveListenPortListener();
  }

  //  end Constructors
  // ////////////////////////////////////

  /**
   * Tell this transport layer how to create connections.
   *
   * @param factory the connection factory
   */
  protected void setConnectionFactory(DsConnectionFactory factory) {
    m_ConnectionFactory = factory;
  }

  /**
   * Adds another port to listen to.
   *
   * @param network default network settings
   * @param port port number
   * @param transport transport type (UDP or TCP)
   * @param addr network address of host
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public void listenPort(DsNetwork network, int port, int transport, InetAddress addr)
      throws DsException, IOException {
    if (network == null) network = DsNetwork.getDefault();

    DsTransportListener listener = m_ConnectionTable.getTransportListener(addr, port, transport);

    if (listener != null) {
      if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
        DsLog4j.connectionCat.info(
            "listenPort: already listening on  "
                + DsSipTransportType.intern(transport)
                + ":"
                + addr
                + ":"
                + port);
      }
      return;
    }

    m_ListeningAddress = addr;
    m_ListeningPort = port;
    m_ListeningTransport = transport;

    switch (transport) {
      case DsSipTransportType.MULTICAST:
        listener = createMulticastListener(network, port, addr, workQueue);
        break;

      case DsSipTransportType.UDP:
        listener = createUdpListener(network, port, addr, workQueue);
        break;

      case DsSipTransportType.TCP:
        listener = createTcpListener(network, port, addr, m_ConnectionTable, m_ConnectionFactory);
        ((DsStreamListener) listener).setIncomingConnectionTimeout(m_IncomingSocketTimeout);
        break;

      case DsSipTransportType.TLS:
        DsSSLContext context = getSSLContext(network);
        if (context == null) {
          throw new DsException(
              "This transport layer is not enabled to support the TLS/SSL transport type.");
        }
        listener =
            createSslListener(network, port, addr, m_ConnectionTable, m_ConnectionFactory, context);
        ((DsStreamListener) listener).setIncomingConnectionTimeout(m_IncomingSocketTimeout);
        break;
    }

    listener.setActive(true);
    m_ConnectionTable.addTransportListener(addr, port, transport, listener);
  }

  /**
   * Sets the properties of the default system network.
   *
   * @param defaultNetwork the network whose properties are used to set those of the default system
   *     network
   * @deprecated ues {@link DsNetwork#getDefault()} and set its properties via {@link
   *     DsNetwork#updateProperties()}
   */
  public void setDefaultNetwork(DsNetwork defaultNetwork) {
    if (defaultNetwork == null)
      throw new IllegalArgumentException("default network cannot be null");

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
   * Adds another port to listen to.
   *
   * @param port port number
   * @param transport transport type (UDP or TCP)
   * @param addr network address of host
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public void listenPort(int port, int transport, InetAddress addr)
      throws DsException, IOException {
    listenPort(DsNetwork.getDefault(), port, transport, addr);
  }

  /**
   * Adds another port to listen to. Sets all values to their defaults:<br>
   *
   * <blockquote>
   *
   * port = 5060<br>
   * transport = UDP<br>
   * address = blank InetAddress<br>
   *
   * </blockquote>
   *
   * @throws DsException if an error occurs
   * @throws IOException if an I/O error occurs
   */
  public void listenPort() throws DsException, IOException {
    listenPort(5060, DsSipTransportType.UDP, InetAddress.getByName("localhost"));
  }

  /**
   * Stop listening on the specified port, protocol and interface after the given timeout.
   *
   * @param port the port
   * @param transport the transport type
   * @param addr the interface
   * @param timeoutSeconds the time in seconds to wait before actually closing the connection
   * @return <code>true</code> if a socket was found and removed for these parameters, otherwise
   *     returns <code>false</code>
   */
  public boolean removeListenPort(int port, int transport, InetAddress addr, int timeoutSeconds) {
    boolean removed = false;
    DsTransportListener listener = m_ConnectionTable.getTransportListener(addr, port, transport);

    if (listener != null) {
      listener.setPendingClosure(true);
      m_ConnectionTable.removeTransportListener(addr, port, transport);

      /* set timer to actually close the connection */
      System.out.println("Scheduled for an Remove listen event");
      DsTimer.schedule(timeoutSeconds * 1000, removeListener, listener);
      removed = true;
    }

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "Removing listener: " + listener);
    }

    return removed;
  }

  /**
   * Dump the content of connection table. Useful for debugging.
   *
   * @return a debug string
   * @deprecated This method is for debugging purpose only and is unsupported
   */
  public String dump() {
    String ret = DsNetwork.getDefault().dump() + "\n" + m_ConnectionTable.dump();
    return ret;
  }

  /** Close all connections and stop all transport listeners. */
  public void stop() {
    m_ConnectionTable.stop();
    // DG-TCP - fixme later workQueue.destroy();
  }

  /** Close all connection and clear the connection table. */
  public void closeAllConnections() {
    m_ConnectionTable.clearConnections();
  }

  /** Restart all listeners. */
  void init() {
    // DG-TCP - fixme later workQueue.init();
    m_ConnectionTable.init();
  }

  /**
   * Check to see if the connection exists in the cache.
   *
   * @param addr the address to get the connection for
   * @param port the port to check for
   * @param transport the transport to check for
   * @return the connection or null if a connection does not exist in the cached connections
   */
  public DsConnection findConnection(InetAddress addr, int port, int transport) {
    return m_ConnectionTable.get(addr, port, transport);
  }

  /**
   * Check to see if the connection exists in the cache.
   *
   * @param info binding info to use to search for the connection
   * @return the connection or null if a connection does not exist in the cached connections
   */
  public DsConnection findConnection(DsBindingInfo info) {
    return m_ConnectionTable.get(info);
  }

  /**
   * Check to see if the connection exists in the cache.
   *
   * @param info binding info to use to search for the connection
   * @return the connection or null if a connection does not exist in the cached connections
   */
  public DsConnection findConnection(DsBindingInfo info, boolean checkShutDown) {
    return m_ConnectionTable.get(info, checkShutDown);
  }

  /**
   * Used to search for a ListenKey (InetAddress, port, protocol) for a given transport.
   *
   * @param transport_type the transport type to check for
   * @return a ListenKey whose transport type is 'transport_type' or null if a listen key for the
   *     transport cannot be found
   */
  public DsTransportLayer.ListenKey findListenKeyForTransport(int transport_type) {
    return m_ConnectionTable.findListenKeyForTransport(transport_type);
  }

  /**
   * A HACK for NAT traversal: if a response has an rport parameter it should be bound locally to
   * the addr/port that the request was received on. We don't have this information for stateless
   * responses, so here choose a listen point at random in hopes of eventually finding the right
   * source port.
   *
   * <p>UDP listen points implement DsConnection.
   */
  DsTransportLayer.ListenKey selectRandomUDPListenPoint() {
    return m_ConnectionTable.selectRandomUDPListenPoint();
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
  public DsConnection getConnection(InetAddress addr, int port, int transport)
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
  public DsConnection getConnection(DsNetwork network, InetAddress addr, int port, int transport)
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
      InetAddress laddr, int lport, InetAddress addr, int port, int transport)
      throws SocketException, DsException, UnknownHostException, IOException {
    return getConnection(DsNetwork.getDefault(), laddr, lport, addr, port, transport);
  }

  /**
   * Inserts the connecting Connection into the DsConnectionTable. Note: it is expected the caller
   * of this method gets lock on mTableLock before calling this method
   *
   * @param network the network associated with the desired connection.
   * @param laddr local address
   * @param lport local port
   * @param raddr remote address
   * @param rport remote port
   * @param transport lhe transport type used for this connection
   * @return retuns connecting connection
   * @throws IOExpection
   */
  private DsConnection createPendingConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress raddr, int rport, int transport)
      throws IOException {
    DsConnection pendingConnection = null;
    try {
      pendingConnection =
          m_ConnectionFactory.createPendingConnection(
              network, laddr, lport, raddr, rport, transport, getSSLContext(network));
      /*
       * For UDP transport if connection strategy is DGRAM_PER_THREAD then
       * Dont insert it into the DsConnectionTable. Created connection
       * will be inserted it into connection thread hashmap.
       */
      if (!((DsSipTransportType.UDP == transport)
          && (network.getDatagramConnectionStrategy() == DsNetwork.DGRAM_PER_THREAD))) {
        m_ConnectionTable.put(pendingConnection);
      }
    } catch (Exception ex) {
      if (DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionCat.log(Level.ERROR, "Failed to create the pending connection", ex);
      }
      throw ex;
    }
    return pendingConnection;
  }

  /** wrapper method to unlock the table. */
  private void checkAndUnlockTable() {
    try {
      if (mTableLock.isLocked() == false) {
        return;
      }
      mTableLock.unlock();
    } catch (IllegalMonitorStateException ex) {
      if (DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionCat.log(Level.ERROR, "Failed to unlock the table ", ex);
      }
    }
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
   * @return the connection desired
   * @throws SocketException if there is an error while creating the socket for the specified
   *     transport type
   * @throws UnknownHostException if the host address is not known
   * @throws IOException if error occurs while creating a message reader for stream protocol
   * @throws DsException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsConnection getConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port, int transport)
      throws SocketException, DsException, UnknownHostException, IOException {
    boolean Level_DEBUG = DsLog4j.connectionCat.isEnabled(Level.DEBUG);

    if (Level_DEBUG) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTransportLayer.getConnection");
    }

    if (network == null) network = DsNetwork.getDefault();

    /*
     * get the active connection without any locks in DsTransportLayer
     * for faster access.
     */
    DsConnection connection = m_ConnectionTable.get(laddr, lport, addr, port, transport);
    DsConnection pendingConnection = null;
    if (connection != null) {
      if (Level_DEBUG) {
        DsLog4j.connectionCat.log(Level.DEBUG, "getConnection returning " + connection);
      }
      return validateConnectionOnTransport(connection, transport);
    }

    /*
     * This try block is to make sure the lock on mTableLock is
     * is always unlocked. Any expection caught by this
     * try block will be rethrowed.
     * in finally block lock on mTableLock will be removed.
     */
    try {
      /*
       * Acquire table lock before fetching the active / connecting connection from the connectionTable.
       */
      mTableLock.lock();
      connection =
          m_ConnectionTable.getActiveOrConnectingConnection(laddr, lport, addr, port, transport);
      if (connection == null) {
        /* Need to create new connection */
        if (Level_DEBUG) {
          DsLog4j.connectionCat.log(Level.DEBUG, "connection to port " + port + " not in cache");
        }
        if (!network.isOutgoingConnection()) {
          if (Level_DEBUG) {
            DsLog4j.connectionCat.log(
                Level.DEBUG, "Outgoing connections are not allowed on this network.");
          }
          throw new SocketException(
              "Outgoing connections are not allowed on this network-[" + network.getName() + "]");
        }
        /*
         * For UDP transport with its connection strategy is set to
         * DsNetwork.DGRAM_PER_THREAD, then, connections will not be
         * inserted into the DsConnectionTable and also it is not a
         * blocking call like TCP / TLS. So instead of create
         * pendingConnection directly create the normal Connection to
         * the destination.
         *
         * Bypassing create Pending connection logic for UDP as it is
         * not a blocking call and also Pending connection creates key
         * with local port as 0 when adding to the connection table this
         * will create problem in case of via having rport for sending
         * response.
         */
        if (transport == DsSipTransportType.UDP) {
          connection =
              m_ConnectionFactory.createConnection(network, laddr, lport, addr, port, transport);
          if (network.getDatagramConnectionStrategy() != DsNetwork.DGRAM_PER_THREAD) {
            m_ConnectionTable.put(connection);
          }

        } else {
          /*
           * Create a DsConnection object without initating the connection towards destination.
           */
          pendingConnection = createPendingConnection(network, laddr, lport, addr, port, transport);
          /*
           * acquire lock on the pendingConnection to block other threads who are try to create
           * Connection towards the same destination. These blocked threads will be unlocked
           * Once connection is established (see the else if isConnecting block)
           */
          pendingConnection.lock();
          /*
           * Now unlock the table, so that other threads that are trying to create connection
           * towards different destinations can process.
           */
          mTableLock.unlock();
          /*
           * Now continue on the pendingConnection to complete the connection.
           * Once pendingConnection is moved to connect state then
           * initiatePendingConnection will unlock it.
           */
          connection =
              initiatePendingConnection(
                  pendingConnection, transport, network, laddr, lport, addr, port);
        }
      } else if (connection.isConnecting()) {
        /*
         * Got PendingConnection. Unlock the table and try to get the lock on
         * pendingConnection. trylock will get lock once initiatePendingConnection
         * is done its processing.
         */
        mTableLock.unlock();
        if (Level_DEBUG) {
          DsLog4j.connectionCat.log(
              Level.DEBUG,
              "Waiting for connection to establish: [" + connection.getBindingInfo() + "]");
        }
        /*
         * Try to acquire lock on pendingConnection with in
         * a duration of TCP setup connection time with 1 second additional duration.
         * a duration of TCP setup connection time + TLS handshake time in
         * case of TLS. if lock is acquired with in this duration,
         * then null connection will be returned
         */
        long lockDuration = DsConnection.LOCK_TRYING_DURATION_BUFFER;
        lockDuration += network.getTcpConnectionTimeout();
        if (transport == DsSipTransportType.TLS) {
          lockDuration += network.getTlsHandshakeTimeout();
        }
        if (connection.trylock(lockDuration)) {
          /*
           * Got lock on pending connection. Now unlock it and
           * get the connection from the connectionTable.
           */
          connection.unlock();
          /*
           * fetch connection again from the connection table
           * DsConnection object will be returned if connection
           * is successfully established.
           */
          connection = m_ConnectionTable.get(laddr, lport, addr, port, transport);
          if (connection == null && DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
            DsLog4j.connectionCat.log(
                Level.ERROR,
                "Failed to get connection after waiting , localAddress"
                    + laddr
                    + ", localPort:"
                    + lport
                    + " remoteAddress:"
                    + addr
                    + " remotePort:"
                    + port
                    + " transport:"
                    + transport);
          }
        } else {
          /*
           * Failed to get the lock on connection. return null.
           */
          String transportStr = SAEventConstants.UDP;
          if (transport == DsSipTransportType.TLS) {
            transportStr = SAEventConstants.TLS;
          } else if (transport == DsSipTransportType.TCP) {
            transportStr = SAEventConstants.TCP;
          }
          ConnectionSAEventBuilder.logConnectionEvent(
              SAEventConstants.CONNECTION_LOCK_TIMEDOUT,
              transportStr,
              SAEventConstants.OUT,
              laddr,
              lport,
              addr,
              port);
          if (DsLog4j.connectionCat.isEnabled(Level.ERROR)) {
            DsLog4j.connectionCat.log(
                Level.ERROR,
                "pending connection lock timedout"
                    + "Local Address:"
                    + laddr
                    + "Local Port"
                    + lport
                    + "Remote Address:"
                    + addr
                    + "Remote Port"
                    + port
                    + transportStr);
          }
          connection = null;
        }
      }
    } catch (Exception allEx) {
      throw allEx;
    } finally {
      checkAndUnlockTable();
      if (pendingConnection != null) {
        /*
         * this will unlock the connection object if connection is failed to connect
         * to the destination.
         */
        pendingConnection.unlock();
      }
    }
    if (Level_DEBUG) {
      DsLog4j.connectionCat.log(Level.DEBUG, "getConnection returning " + connection);
    }

    return validateConnectionOnTransport(connection, transport);
  }

  /**
   * Preventive check to make sure that returned Connection is of same transport as called This
   * should never happen
   *
   * <p>Method Checks validates the connection objects transport type against the queried one
   * Generates an alarm and returns null if there is a transport mismatch
   */
  DsConnection validateConnectionOnTransport(DsConnection connection, int transport) {

    if (connection != null && connection.getTransportType() != transport) {
      DsLog4j.connectionCat.error("Connection table returned a inavlid connection " + connection);

      InvalidConnectionDataParam dataParam =
          new InvalidConnectionDataParam.Builder()
              .eventType("InvalidConnection")
              .eventInfo("Connection table returned a invalid connection")
              .connection(connection.toString())
              .eventLevel(EventLevel.err)
              .build();
      // REFACTOR
      //      SIPConnectionTableMBeanImpl.sendNotification(
      //          SAEventConstants.GENERATE_ALARM_AND_EVENT, dataParam);

      return null;
    }

    return connection;
  }

  private DsConnection initiatePendingConnection(
      DsConnection pendingConnection,
      int transport,
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port)
      throws SocketException, DsException, UnknownHostException, IOException {
    DsConnection connection = null;
    boolean Level_DEBUG = DsLog4j.connectionCat.isEnabled(Level.DEBUG);
    try {
      switch (transport) {
        case DsSipTransportType.TLS:
          pendingConnection.initiateConnect();
          pendingConnection.setTimeout(m_OutgoingSocketTimeout);
          createMessageReader(pendingConnection, IS_NON_BLOCKING_TLS);
          connection = pendingConnection;
          connection.connected();
          break;
        case DsSipTransportType.TCP:
          pendingConnection.initiateConnect();
          pendingConnection.setTimeout(m_OutgoingSocketTimeout);
          createMessageReader(pendingConnection, IS_NON_BLOCKING_TCP);
          connection = pendingConnection;
          connection.connected();
          break;
        case DsSipTransportType.UDP:
          if (Level_DEBUG) {
            DsLog4j.connectionCat.log(Level.DEBUG, "getConnection transport UDP");
          }
          pendingConnection.initiateConnect();
          if (Level_DEBUG) {
            DsLog4j.connectionCat.log(
                Level.DEBUG, "getConnection transport connection = " + connection);
          }
          // if the policy is one/per thread, then don't use the connection table
          if (network.getDatagramConnectionStrategy() != DsNetwork.DGRAM_PER_THREAD) {
            pendingConnection.addDsConnectionEventListener(this);
          }
          connection = pendingConnection;
          connection.connected();
          break;
        default:
          connection =
              m_ConnectionFactory.createConnection(network, laddr, lport, addr, port, transport);
          connection.addDsConnectionEventListener(this);
          m_ConnectionTable.put(connection);
          break;
      }
    } catch (Exception ex) {
      /*
       * If connection is failed then remove the connection from
       * the table.
       */
      m_ConnectionTable.remove(pendingConnection);
      throw ex;
    }
    return connection;
  }

  /**
   * Removes an existing connection from the transport layer performing a close on the connection.
   *
   * @param connection to remove
   */
  protected void removeConnection(DsConnection connection) {
    if (connection != null) {
      if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionCat.log(Level.DEBUG, "removing connection: " + connection);
      }

      if (m_ConnectionTable.get(connection.getBindingInfo(), false) != null) {
        m_ConnectionTable.remove(connection);
      }
    }
  }

  /**
   * Begin monitoring a new connection for incoming messages or, in the case of a server socket, new
   * connections.
   *
   * @param connection the connection to being monitoring
   * @throws IOException
   */
  void monitorConnection(DsConnection connection) throws IOException {
    if (connection.getTransportType() == DsSipTransportType.TCP) {
      connection.setTimeout(m_OutgoingSocketTimeout);
      createMessageReader(connection, IS_NON_BLOCKING_TCP);
    } else if (connection.getTransportType() == DsSipTransportType.TLS) {
      connection.setTimeout(m_OutgoingSocketTimeout);
      createMessageReader(connection, IS_NON_BLOCKING_TLS);
    }
  }

  /**
   * Apply a message reader to a given input stream.
   *
   * @param is the new input stream
   * @param queue where to put incoming message data
   * @param socket the new socket
   * @param binfo the binding information to copy onto incoming messages
   * @return the created message reader of the proper subclass
   */
  protected abstract DsMessageReader createMessageReader(
      InputStream is, Executor queue, DsSocket socket, DsBindingInfo binfo);

  /**
   * Apply an Multicast listener to a given port and interface.
   *
   * @param network the network to associate with the listener
   * @param port the port
   * @param addr the interface
   * @param queue where to put incoming message data
   * @return the created ilistener of the proper subclass
   * @throws IOException if thrown by the underlying socket
   * @throws UnknownHostException if the host cannot be found
   */
  protected abstract DsMulticastListener createMulticastListener(
      DsNetwork network, int port, InetAddress addr, Executor queue)
      throws IOException, UnknownHostException;

  /**
   * Apply an UDP listener to a given port and interface.
   *
   * @param network the DsNetwork object to use for this listsener
   * @param port the port
   * @param addr the interface
   * @param queue where to put incoming message data
   * @return the created ilistener of the proper subclass
   * @throws IOException if thrown when creating the listsener
   * @throws UnknownHostException if thrown when creating the listsener
   */
  protected abstract DsUdpListener createUdpListener(
      DsNetwork network, int port, InetAddress addr, Executor queue)
      throws IOException, UnknownHostException;

  /**
   * Apply an SSL listener to a given port and interface.
   *
   * @param network the DsNetwork object to use for this listsener
   * @param port the port
   * @param address the interface
   * @param connectionTable where to cache connections
   * @param connectionFactory how to create connections
   * @param context the security context to apply to new connections
   * @return the created ilistener of the proper subclass
   * @throws SocketException if thrown when creating the listsener
   * @throws IOException if thrown when creating the listsener
   * @throws UnknownHostException if thrown when creating the listsener
   */
  protected abstract DsSslListener createSslListener(
      DsNetwork network,
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory,
      DsSSLContext context)
      throws SocketException, UnknownHostException, IOException;

  /**
   * Apply a TCP listener to a given port and interface.
   *
   * @param network the DsNetwork object to use for this listsener
   * @param port the port
   * @param address the interface
   * @param connectionTable where to cache connections
   * @param connectionFactory how to create connections
   * @return the created ilistener of the proper subclass
   * @throws SocketException if thrown when creating the listsener
   * @throws IOException if thrown when creating the listsener
   * @throws UnknownHostException if thrown when creating the listsener
   */
  protected abstract DsTcpListener createTcpListener(
      DsNetwork network,
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory)
      throws SocketException, UnknownHostException, IOException;

  /**
   * Apply a message reader to this connection.
   *
   * @param connection the connection to which to apply the reader
   * @throws IOException if thrown by the underlying socket
   */
  protected void createMessageReader(DsConnection connection) throws IOException {
    createMessageReader(connection, false);
  }

  /**
   * Apply a message reader to this connection.
   *
   * @param connection the connection to which to apply the reader.
   * @param nonBlocking if this message reader should be non-blocking and depends on Selector
   *     notifications to read messages. The reader in this case is the connection itself. This
   *     option currently is available only in TCP.
   * @throws IOException if thrown by the underlying socket.
   */
  protected void createMessageReader(DsConnection connection, boolean nonBlocking)
      throws IOException {
    DsMessageReader messageReader = null;
    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.info(
          "DsTransportLayer.createMessageReader():nonBlocking = " + nonBlocking);
    }

    if (connection.getTransportType() == DsSipTransportType.TCP && nonBlocking) {
      if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
        DsLog4j.connectionCat.info("DsTransportLayer.createMessageReader():nonBlocking TCP");
      }

      connection.addDsConnectionEventListener(this);
    } else if (connection.getTransportType() == DsSipTransportType.TLS && nonBlocking) {
      if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
        DsLog4j.connectionCat.info("DsTransportLayer.createMessageReader():nonBlocking TLS");
      }

      connection.addDsConnectionEventListener(this);
    } else if (connection.getTransportType() == DsSipTransportType.TCP
        || connection.getTransportType() == DsSipTransportType.TLS) {
      DsSocket socket = (DsSocket) ((DsTcpConnection) connection).getSocket();

      try {
        socket.checkConnection();
      } catch (IOException ex) {
        m_ConnectionTable.remove(connection);
        throw ex;
      }
      BufferedInputStream reader =
          new BufferedInputStream(socket.getInputStream(), inputBufferSize);
      // We will pass the same reference of binding info to be able to set the
      // SSL session info on the binding info.
      // --            DsBindingInfo binding_info =
      // (DsBindingInfo)connection.getBindingInfo().clone();
      DsBindingInfo binding_info = (DsBindingInfo) connection.getBindingInfo();
      messageReader = createMessageReader(reader, workQueue, socket, binding_info);

      // now register me so that I will be notified when the stream is closed or gets an I/O error
      messageReader.addDsConnectionClosedEventListener(this);
      messageReader.addDsInputStreamEventListener(this);

      // and also notify the connection if either of the above events occur
      messageReader.addDsConnectionClosedEventListener(connection);
      messageReader.addDsInputStreamEventListener(connection);
      messageReader.doStart();
    }
  }

  /**
   * Apply a message reader to a given socket channel.
   *
   * @param socket the new socket.
   * @param queue where to put incoming message data.
   * @param binfo the binding information to copy onto incoming messages.
   * @return the created message reader of the proper subclass
   */
  /*
      protected DsMessageReader createMessageReader(Executor queue,
                                                      DsSocket socket, DsBindingInfo binfo)
              throws IOException
      {
          if (DsLog4j.connectionCat.isEnabledFor(Level.INFO))
          {
              DsLog4j.connectionCat.info("DsTransportLayer.createMessageReader():Creating Selectable Message Reader");
          }
          return new DsSelectableReader(queue, socket, binfo);
      }
  */
  /*
  private void createUdpListener(DsUdpConnection connection) throws IOException
  {
      if (connection.getTransportType() == DsSipTransportType.UDP)
      {
          DsUdpListener udpListener = new DsUdpListener((DsDatagramSocket)connection.getSocket(), workQueue);
          udpListener.setActive(true);
      }
  }
  */

  /**
   * Get the last listening transport that was specified. If more than 1 listen port was specified
   * or removed, this function should not be relied upon. Use getListenKeys instead
   *
   * @return the last listening transport type.
   * @throws DsException when there is more than 1 listen transport specified
   * @see #getListenKeys()
   */
  public int getListeningTransport() throws DsException {
    if (m_ConnectionTable.getListeningTransportsCount() > 1) {
      throw new DsException(
          "More than 1 listenport specified. The last Listening Transport is invalid!");
    }

    return m_ListeningTransport;
  }

  /**
   * Get the last listening port that was specified. If more than 1 listen port was specified or
   * removed this function should not be relied upon. Use getListenKeys instead
   *
   * @return the last listening port number.
   * @throws DsException when there is more than 1 listen port specified
   * @see #getListenKeys()
   */
  public int getListeningPort() throws DsException {
    if (m_ConnectionTable.getListeningTransportsCount() > 1) {
      throw new DsException(
          "More than 1 listenport specified. The last Listening Port is invalid!");
    }

    return m_ListeningPort;
  }

  /**
   * Gets the hostname of the the last InetAddress that was specified If more than 1 listen port was
   * specified or removed this function should not be relied upon. Use getListenKeys instead
   *
   * @return the last listening address
   * @throws DsException when there is more than 1 listen address specified
   * @see #getListenKeys()
   */
  public String getListeningAddress() throws DsException {
    if (m_ConnectionTable.getListeningTransportsCount() > 1) {
      throw new DsException(
          "More than 1 listenport specified. The last Listening Address is invalid!");
    }

    String address = null;

    try {
      address = m_ListeningAddress.getHostAddress();
    } catch (Throwable e) {
    }

    return address;
  }

  /**
   * Set the time in seconds used to check the connection table for connections that should be
   * removed. Connections are removed after they are no longer referenced by transactions and the
   * timeout set on the connection has expired. TCP/TLS timeouts for are controlled by
   * setIncomingConnectionTimeout and setOutgoingConnectionTimeout. Cleanup of other connections are
   * controlled by the timer set here. The default value is 60 seconds.
   *
   * @param aSeconds number of seconds to wait between checks of the connection table
   */
  public void setCleanupInterval(int aSeconds) {
    m_ConnectionTable.setCleanupInterval(aSeconds);
  }

  /**
   * Retrieve the cleanup interval.
   *
   * @return the cleanup interval
   */
  public int getCleanupInterval() {
    return m_ConnectionTable.getCleanupInterval();
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
    m_ConnectionTable.setMaxConnections(max_connections);
  }

  /**
   * Returns the maximum number of TCP/TLS connections that this transport layer supports.
   *
   * @return the maximum number of TCP/TLS connections
   */
  public int getMaxConnections() {
    return m_ConnectionTable.getMaxConnections();
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
   * Tells the stack about the MTU value. If MTU is not set and a request is bigger than 1300 bytes
   * or when the request is within 200 bytes of the path MTU, TCP should be used instead of UDP to
   * avoid fragmentation of the message over UDP.
   *
   * <p>Note that you can get and set this value per network.
   *
   * @param MTUValue the value of MTU.
   */
  public void setMTU(int MTUValue) {
    DsNetworkProperties.setMTUDefaultSize(MTUValue);
  }

  /**
   * Returns the current MTU value. If MTU is not set and a request is bigger than 1300 bytes or
   * when the request is within 200 bytes of the path MTU, TCP should be used instead of UDP to
   * avoid fragmentation of the message over UDP.
   *
   * <p>Note that you can get and set this value per network.
   *
   * @return the current MTU Value.
   */
  public int getMTU() {
    return DsNetworkProperties.getMTUDefaultSize();
  }

  /**
   * Callback for a DsInputStreamErrorEvent. Close and remove the connection for this stream.
   *
   * @param err the input stream error event
   */
  public void onDsInputStreamErrorEvent(DsInputStreamErrorEvent err) {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTransportLayer.onDsInputStreamErrorEvent");
    }

    DsMessageReader source = err.getReaderSource();
    DsSocket aSocket = source.getSocket();
    DsConnection aConnection =
        m_ConnectionTable.get(
            aSocket.getRemoteInetAddress(),
            aSocket.getRemotePort(),
            source.isSslEnabled() ? DsSipTransportType.TLS : DsSipTransportType.TCP,
            false); // don't check for shutdown

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "DsTransportLayer.onDsInputStreamErrorEvent" + " removing connection == " + aConnection);
    }

    if (aConnection != null) {
      m_ConnectionTable.remove(aConnection);
    }

    source.removeDsInputStreamEventListener(this);
    // source.abort ();
    if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
      DsLog4j.connectionCat.warn(err.getException().toString(), err.getException());
    }
  }

  /**
   * Callback for a DsInputStreamClosedEvent. Close and remove the connection for this stream.
   *
   * @param err the closed event
   */
  public void onDsInputStreamClosedEvent(DsInputStreamClosedEvent err) {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTransportLayer.onDsInputStreamClosedEvent");
    }

    DsMessageReader source = err.getReaderSource();
    DsSocket aSocket = source.getSocket();

    DsConnection aConnection =
        m_ConnectionTable.get(
            aSocket.getRemoteInetAddress(),
            aSocket.getRemotePort(),
            source.isSslEnabled() ? DsSipTransportType.TLS : DsSipTransportType.TCP,
            false); // don't check for shutdown

    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.log(
          Level.INFO,
          "DsTransportLayer.onDsInputStreamClosedEvent " + "removing connection == " + aConnection);
    }

    if (aConnection != null) {
      m_ConnectionTable.remove(aConnection);
    }

    // source.abort ();
    source.removeDsInputStreamEventListener(this);
  }

  /**
   * Handle a connection close event.
   *
   * @param evt event to handle
   */
  public void onDsConnectionClosedEvent(DsConnectionClosedEvent evt) {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTransportLayer.onDsConnectionClosedEvent");
    }

    DsConnection aConnection = evt.getSource();

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "DsTransportLayer.onDsConnectionClosedEvent " + "removing connection == " + aConnection);
    }

    if (aConnection != null) {
      m_ConnectionTable.remove(aConnection);
      ((DsAbstractConnection) aConnection).removeDsConnectionEventListener(this);
    }
  }

  /**
   * Handle a connection error event.
   *
   * @param evt event to handle
   */
  public void onDsConnectionErrorEvent(DsConnectionErrorEvent evt) {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "DsTransportLayer.onDsConnectionErrorEvent");
    }

    DsConnection aConnection = evt.getSource();

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "DsTransportLayer.onDsConnectionErrorEvent " + "removing connection == " + aConnection);
    }

    if (aConnection != null) {
      m_ConnectionTable.remove(aConnection);
      ((DsAbstractConnection) aConnection).removeDsConnectionEventListener(this);
    }
  }

  /**
   * Handle a connection ICMP error event in case of UDP.
   *
   * @param evt event to handle
   */
  public void onDsConnectionIcmpErrorEvent(DsConnectionIcmpErrorEvent evt) {
    DsConnection connection = evt.getSource();
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "DsTransportLayer.onDsConnectionIcmpErrorEvent(): "
              + "removing connection == "
              + connection);
    }
    if (null != connection) {
      connection.removeDsConnectionEventListener(this);
      m_ConnectionTable.remove(connection);
    }
  }

  /**
   * Get the key objects associated with the listening ports setup.
   *
   * @return enumeration of keys associated with the listening ports for this transport
   */
  public Enumeration getListenKeys() {
    return m_ConnectionTable.getListenKeys();
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
  public boolean isListenInterface(String host, int port, int transport) {
    Enumeration sockets = getListenKeys();
    DsTransportLayer.ListenKey socket;
    InetAddress addr;

    while (sockets.hasMoreElements()) {
      try {
        socket = (DsTransportLayer.ListenKey) sockets.nextElement();
        addr = InetAddress.getByName(host);
        if (socket.equals(new DsTransportLayer.ListenKey(addr, port, transport))) {
          if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
            DsLog4j.connectionCat.info("Possible loop on local interface: " + socket);
          }
          return true;
        }
      } catch (Exception e) {
        if (DsLog4j.connectionCat.isEnabled(Level.ERROR))
          DsLog4j.connectionCat.error("Exception in isListenInterface: ", e);
      }
    }
    return false;
  }

  /** Class that removes listen port listeners. */
  private class RemoveListenPortListener implements DsEvent {
    public void run(Object arg) {
      if (arg instanceof DsTransportListener) {
        try {
          System.out.println("executing remove listen event");
          ((DsTransportListener) arg).setActive(false);
        } catch (Throwable t) {
          if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
            DsLog4j.connectionCat.warn("run: exception closing transport listener", t);
          }
        }
      } else {
        if (DsLog4j.connectionCat.isEnabled(Level.WARN)) {
          DsLog4j.connectionCat.warn("RemoveListenPortListener: wrong argument type.");
        }
      }
    }
  }

  /** This class implements the key used to store listening sockets. */
  public static class ListenKey {
    /** The local address. */
    public InetAddress m_lInetAddress;
    /** The local port. */
    public int m_lPort;
    /** The remote address. */
    public InetAddress m_InetAddress;
    /** The remote port. */
    public int m_Port;
    /** The transport type. */
    public int m_TransportType;

    /** Default constructor. */
    public ListenKey() {}

    /**
     * Constructs the listen key with the specified information.
     *
     * @param netAddress the interface which the socket is listening on
     * @param port the listening port
     * @param transportType the listening transport type
     */
    public ListenKey(InetAddress netAddress, int port, int transportType) {
      m_lInetAddress = null;
      m_lPort = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;
      m_InetAddress = netAddress;
      m_Port = port;
      m_TransportType = transportType;
    }

    /**
     * Constructs the listen key with the specified information.
     *
     * @param lnetAddress the local interface
     * @param lport the local port
     * @param netAddress the remote interface
     * @param port the remote port
     * @param transportType the listening transport type
     */
    public ListenKey(
        InetAddress lnetAddress, int lport, InetAddress netAddress, int port, int transportType) {
      m_lInetAddress = lnetAddress;
      m_lPort = lport;
      m_InetAddress = netAddress;
      m_Port = port;
      m_TransportType = transportType;
    }

    /**
     * Sets the specified information in this listen key.
     *
     * @param netAddress the interface which the socket is listening on
     * @param port the listening port
     * @param transportType the listening transport type
     */
    public void set(InetAddress netAddress, int port, int transportType) {
      m_lInetAddress = null;
      m_lPort = DsBindingInfo.LOCAL_PORT_UNSPECIFIED;
      m_InetAddress = netAddress;
      m_Port = port;
      m_TransportType = transportType;
    }

    /**
     * Sets the specified information in this listen key.
     *
     * @param lnetAddress the local interface
     * @param lport the local port
     * @param netAddress the remote interface
     * @param port the remote port
     * @param transportType the listening transport type
     */
    public void set(
        InetAddress lnetAddress, int lport, InetAddress netAddress, int port, int transportType) {
      m_lInetAddress = lnetAddress;
      m_lPort = lport;
      m_InetAddress = netAddress;
      m_Port = port;
      m_TransportType = transportType;
    }

    /**
     * Get the transport type.
     *
     * @return transport type
     */
    public int getTransport() {
      return m_TransportType;
    }

    /**
     * Get the InetAddress.
     *
     * @return the InetAddress
     */
    public InetAddress getInetAddress() {
      return m_InetAddress;
    }

    /**
     * Get the port.
     *
     * @return the port
     */
    public int getPort() {
      return m_Port;
    }

    /**
     * Equality test.
     *
     * @param obj a ListenKey object.
     * @return <code>true</code> if this key equals 'obj,' otherwise false
     */
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof ListenKey)) return false;

      ListenKey aKey = (ListenKey) obj;
      if (aKey.m_Port != m_Port) return false;
      if (aKey.m_TransportType != m_TransportType) return false;
      // m_InetAddress should not be null. But we check it anyway
      if (aKey.m_InetAddress == null || !(aKey.m_InetAddress.equals(m_InetAddress))) return false;
      // if one local port is not specified, we treat it as equal
      if ((aKey.m_lPort != m_lPort)
          && (aKey.m_lPort != DsBindingInfo.LOCAL_PORT_UNSPECIFIED)
          && (m_lPort != DsBindingInfo.LOCAL_PORT_UNSPECIFIED)) return false;
      // if one local address is not specified, we treat it as equal
      if ((aKey.m_lInetAddress != null)
          && (m_lInetAddress != null)
          && !(aKey.m_lInetAddress.equals(m_lInetAddress))) return false;

      return true;
    }

    /**
     * Determine the hash code.
     *
     * @return the hash code
     */
    public int hashCode() {
      // return m_Port * 2 + m_TransportType + m_InetAddress.hashCode();
      return ((m_Port + m_TransportType) * 3) * m_InetAddress.hashCode();
    }

    /**
     * Get a string representation.
     *
     * @return the string representation
     */
    public String toString() {
      StringBuffer buffer =
          new StringBuffer(64)
              .append(m_TransportType)
              .append(':')
              .append(m_lInetAddress == null ? null : m_lInetAddress.getHostAddress())
              .append(':')
              .append(m_lPort)
              .append(':')
              .append(m_InetAddress == null ? null : m_InetAddress.getHostAddress())
              .append(':')
              .append(m_Port);
      return buffer.toString();
    }
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

  public int getNumConns() {
    return m_ConnectionTable.getNumConns();
  }

  /**
   * getConnectionTable content of connection table to the tabular data. This method is for REST API
   * v1/runtime/connections
   *
   * @return a TabularData
   */
  public TabularData getConnectionTableDetails() {
    return m_ConnectionTable.getConnectionTableDetails();
  }

  /**
   * getConnectionTableSummary content of connection table to the Composite data. This method is for
   * REST API v1/runtime/connections/summary
   *
   * @return a CompositeData
   */
  public CompositeData getConnectionTableSummary() {
    return m_ConnectionTable.getConnectionTableSummary();
  }

  public DsConnectionTable getConnectionTable() {
    return m_ConnectionTable;
  }
}
