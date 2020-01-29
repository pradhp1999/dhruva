// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import com.cisco.dhruva.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.DsLibs.DsUtil.DsLog4j;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.openmbean.*;
import org.apache.logging.log4j.Level;

/**
 * This class keeps track of listening connections, incoming connections and outgoing connections.
 * Is responsible for turning off TCP listeners when the maximum connection count is reached, and
 * turning them back on when connections are released.
 *
 * <p>It also is responsible for updating the DsSipServerLocator class with a Set of currently
 * supported transports.
 */
public final class DsConnectionTable {
  private static final boolean IS_NON_BLOCKING_TCP;
  private static final boolean IS_NON_BLOCKING_TLS;

  private static final boolean m_smallMaps;
  private static final int m_defaultCleanupInterval;

  private static final int DEFAULT_MAX_CONNECTIONS;
  private int m_MaxConnections = DEFAULT_MAX_CONNECTIONS;
  private Set m_TransportSet = new HashSet();
  private boolean m_tcpActive = true;
  private Object m_ListeningLock = new Object();
  private Hashtable m_ListeningTransports;
  private Object m_TableLock = new Object();
  private int m_TCPCount; // = 0;
  private ConcurrentHashMap m_Table;
  private int m_CleanupInterval;
  public int connectionCountUDP = 0;
  public int connectionCountTCP = 0;
  public int connectionCountTLS = 0;
  private int samplingInterval = 1;
  // qfang - 07.05.06 - CSCse63190 switch to JDK's Timer implementation
  private TimerTask m_sweepTimer; //  =  null;
  private TimerTask connectionCountTimer;
  // GOGONG 04.12.06 CSCsd90062 - create a timer daemon thread
  //     so that the parent thread can be shut down without waiting for this non-daemon thread.
  // qfang - 07.05.06 - CSCse63190 switch to JDK's Timer implementation
  private Timer m_Timer = new Timer(true);

  // below variables use for /runtime/connections REST API
  String[] connectionTableItemNames = {
    "localIP",
    "localPORT",
    "remoteIP",
    "remotePORT",
    "transport",
    "connectionState",
    "sendMessageQueueCount"
  };
  String[] connectionTableItemDescription = {
    "Local IP",
    "Local PORT",
    "Remote IP",
    "Remote PORT",
    "Transport",
    "connection State",
    "Send queued Message Count"
  };
  OpenType[] connectionTableItemType = {
    SimpleType.STRING,
    SimpleType.INTEGER,
    SimpleType.STRING,
    SimpleType.INTEGER,
    SimpleType.STRING,
    SimpleType.STRING,
    SimpleType.INTEGER
  };

  CompositeType connectionTableComposite = null;
  TabularType connectionTableType = null;
  TabularData connTableData = null;

  // below variables use for /runtime/connections/Summary REST API
  String[] connectionTableSummaryItemNames = {
    "numberOfConnections",
    "udpConnectionCount",
    "udpActiveConnections",
    "udpShuttingDownConnections",
    "udpConnectingConnections",
    "tcpConnectionCount",
    "tcpActiveConnections",
    "tcpShuttingDownConnections",
    "tcpConnectingConnections",
    "tlsConnectionCount",
    "tlsActiveConnections",
    "tlsShuttingDownConnections",
    "tlsConnectingConnections"
  };

  String[] connectionTableSummaryItemDescription = {
    "number Of Connections",
    "udp connection Count",
    "udp active Connections",
    "udp shuttingDown Connections",
    "udp connecting  Connections",
    "tcp connection Count",
    "tcp active Connections",
    "tcp shuttingDown Connections",
    "tcp connecting Connections",
    "tls connection Count",
    "tls active Connections",
    "tls shuttingDown Connections",
    "tls connecting Connections"
  };

  OpenType[] connectionTableSummaryItemType = {
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER,
    SimpleType.INTEGER
  };

  CompositeType connectionTableSummaryComposite = null;

  static {
    m_defaultCleanupInterval =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_SWEEP_TIMER_TIMEOUT,
            DsConfigManager.PROP_SWEEP_TIMER_TIMEOUT_DEFAULT);

    m_smallMaps =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_SMALL_MAPS, DsConfigManager.PROP_SMALL_MAPS_DEFAULT);

    IS_NON_BLOCKING_TCP =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_NON_BLOCKING_TCP, DsConfigManager.PROP_NON_BLOCKING_TCP_DEFAULT);

    IS_NON_BLOCKING_TLS =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_NON_BLOCKING_TLS, DsConfigManager.PROP_NON_BLOCKING_TLS_DEFAULT);

    if (IS_NON_BLOCKING_TCP || IS_NON_BLOCKING_TLS) {
      DEFAULT_MAX_CONNECTIONS = 256000;
    } else {
      DEFAULT_MAX_CONNECTIONS = 256;
    }
  }

  /** ThreadLocal key factory initializer. */
  private static TableListenKeyInitializer keyFactory = new TableListenKeyInitializer();

  /**
   * Constructor.
   *
   * @param cleanup_seconds the table cleanup interval
   */
  public DsConnectionTable(int cleanup_seconds) {
    if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionMCat.log(Level.DEBUG, "new connection table");
    }

    m_CleanupInterval = cleanup_seconds;

    if (!m_smallMaps && (IS_NON_BLOCKING_TCP || IS_NON_BLOCKING_TLS)) {
      m_Table = new ConcurrentHashMap(32768, .60f);
    } else {
      m_Table = new ConcurrentHashMap();
    }
    m_ListeningTransports = new Hashtable();
    m_sweepTimer = new SweepTimer();
    m_Timer.schedule(m_sweepTimer, m_CleanupInterval * 1000L, m_CleanupInterval * 1000L);
    connectionCountTimer = new ConnectionsCount();
    m_Timer.schedule(connectionCountTimer, 0, samplingInterval * 1000);
  }

  /**
   * Default constructor that sets the cleanup interval to 60 seconds by default.
   *
   * <p>Every 60 seconds the connection table is swept for connections that should be closed. An
   * individual connection's DsConnection.shouldClose(current_time) is used to determine when a
   * connection should be closed and removed from the table. The default value is set via {@link
   * DsConfigManager#PROP_SWEEP_TIMER_TIMEOUT_DEFAULT}.
   */
  public DsConnectionTable() {
    this(m_defaultCleanupInterval);

    try {

      connectionTableComposite =
          new CompositeType(
              "ConnectionsDetails",
              "ConnectionsDetails attributes",
              connectionTableItemNames,
              connectionTableItemDescription,
              connectionTableItemType);

      connectionTableType =
          new TabularType(
              "SIPConnectionsDetails",
              "Connections Details information",
              connectionTableComposite,
              connectionTableItemNames);

      connectionTableSummaryComposite =
          new CompositeType(
              "ConnectionTableSummary",
              "ConnectionTableSummary attributes",
              connectionTableSummaryItemNames,
              connectionTableSummaryItemDescription,
              connectionTableSummaryItemType);
    } catch (Exception e) {
      if (DsLog4j.connectionMCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionMCat.log(
            Level.ERROR, "Initilisation of tabular data type for connection table.", e);
      }
    }
  }

  /**
   * Get the connection associated with InetAddress, port and protocol.
   *
   * @param anInetAddress address of connection
   * @param aPort port of connection
   * @param transportType the transport type of the connection
   * @return found connection or null if no connection is found
   */
  public DsConnection get(InetAddress anInetAddress, int aPort, int transportType) {
    return get(null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, anInetAddress, aPort, transportType);
  }

  /**
   * Get the connection associated with InetAddress, port and protocol.
   *
   * @param anInetAddress address of connection
   * @param aPort port of connection
   * @param transportType the transport type of the connection
   * @param checkShutDown whether to check if this connection is already in shut down mode. If so
   *     then return null.
   * @return found connection or null if no connection is found
   */
  public DsConnection get(
      InetAddress anInetAddress, int aPort, int transportType, boolean checkShutDown) {
    return get(
        null,
        DsBindingInfo.LOCAL_PORT_UNSPECIFIED,
        anInetAddress,
        aPort,
        transportType,
        checkShutDown);
  }

  /**
   * Get the connection associated with binding info.
   *
   * @param info binding information for the connection
   * @return info connection or null if no connection is found
   */
  public DsConnection get(DsBindingInfo info) {
    return get(
        info.getLocalAddress(),
        info.getLocalPort(),
        info.getRemoteAddress(),
        info.getRemotePort(),
        info.getTransport());
  }

  /**
   * Get the connection associated with binding info.
   *
   * @param info binding information for the connection
   * @param checkShutDown whether to check if this connection is already in shut down mode. If so
   *     then return null.
   * @return info connection or null if no connection is found
   */
  public DsConnection get(DsBindingInfo info, boolean checkShutDown) {
    return get(
        info.getLocalAddress(), info.getLocalPort(),
        info.getRemoteAddress(), info.getRemotePort(),
        info.getTransport(), checkShutDown);
  }

  /**
   * Get the connection associated with InetAddress, port and protocol.
   *
   * @param lInetAddress local address of connection
   * @param lPort local port of connection
   * @param anInetAddress address of connection
   * @param aPort port of connection
   * @param transportType the transport type of the connection
   * @return found connection or null if no connection is found
   */
  public DsConnection get(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPort,
      int transportType) {
    return get(lInetAddress, lPort, anInetAddress, aPort, transportType, true);
  }

  /**
   * Get the connection associated with InetAddress, port and protocol.
   *
   * @param lInetAddress local address of connection
   * @param lPort local port of connection
   * @param anInetAddress address of connection
   * @param aPort port of connection
   * @param transportType the transport type of the connection
   * @param checkShutDown whether to check if this connection is already in shut down mode. If so
   *     then return null.
   * @return found connection or null if no connection is found
   */
  public DsConnection get(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPort,
      int transportType,
      boolean checkShutDown) {
    CTableListenKey aKey = (CTableListenKey) keyFactory.get();
    aKey.set(lInetAddress, lPort, anInetAddress, aPort, transportType);

    if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionMCat.log(
          Level.DEBUG,
          "Looking for: ["
              + lInetAddress
              + ", "
              + lPort
              + ", "
              + anInetAddress
              + ", "
              + aPort
              + ", "
              + transportType
              + "]");
    }
    synchronized (m_TableLock) {
      Object obj = m_Table.get(aKey);
      DsConnection connection = (DsConnection) obj;
      if (connection != null && connection.isConnecting()) {
        if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionMCat.log(Level.DEBUG, "The existing connection is in connecting state");
        }
        connection = null;
      } else if (checkShutDown && connection != null) {
        try {
          DsAbstractConnection absCon = (DsAbstractConnection) connection;
          // If shutting down then it is of no use and better treat it
          // as if we have found no connection.
          if (absCon.isShutingDown()) {
            if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
              DsLog4j.connectionMCat.log(
                  Level.DEBUG,
                  "The existing connection is in shuting down mode and is no more good");
            }
            connection = null;
          }
        } catch (ClassCastException cce) {
          if (DsLog4j.connectionMCat.isEnabled(Level.INFO)) {
            DsLog4j.connectionMCat.log(
                Level.INFO, "Checking for shutdown mode for non DsAbstractConnection");
          }
        }
      }

      if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionMCat.log(Level.DEBUG, "get returning " + connection);
      }

      return connection;
    }
  }

  /**
   * Get the active or connecting connection associated with InetAddress, port and protocol.
   *
   * @param lInetAddress local address of connection
   * @param lPort local port of connection
   * @param anInetAddress address of connection
   * @param aPort port of connection
   * @param transportType the transport type of the connection
   * @return found connection or null if no connection is found
   */
  public DsConnection getActiveOrConnectingConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPort,
      int transportType) {
    CTableListenKey aKey = (CTableListenKey) keyFactory.get();
    aKey.set(lInetAddress, lPort, anInetAddress, aPort, transportType);

    if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionMCat.log(
          Level.DEBUG,
          "Looking for: ["
              + lInetAddress
              + ", "
              + lPort
              + ", "
              + anInetAddress
              + ", "
              + aPort
              + ", "
              + transportType
              + "]");
    }
    synchronized (m_TableLock) {
      Object obj = m_Table.get(aKey);
      DsConnection connection = (DsConnection) obj;
      if (connection != null) {
        // If shutting down then it is of no use and better treat it
        // as if we have found no connection.
        if (connection.isShutingDown()) {
          if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
            DsLog4j.connectionMCat.log(
                Level.DEBUG, "The existing connection is in shuting down mode and is no more good");
          }
          connection = null;
        }
      }

      if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionMCat.log(Level.DEBUG, "get returning " + connection);
      }

      return connection;
    }
  }

  /**
   * Dump content of connection table to the log file. Useful for debugging.
   *
   * @deprecated This method is for debugging purpose only and is unsupported.
   * @return a debug string
   */
  public String dump() {
    StringBuffer buf = new StringBuffer(128);

    buf.append("-----------------------------------------------\n");
    buf.append("-- DsConnectionTable.dump  --------------------\n");
    buf.append("-----------------------------------------------\n");
    synchronized (this) {
      Iterator key_iter = m_Table.keySet().iterator();
      buf.append("Connection keys: \n");
      buf.append("<><><><><><><>\n");
      while (key_iter.hasNext()) {
        buf.append(((CTableListenKey) key_iter.next()).toString());
        buf.append("\n");
      }
      Iterator values_it = m_Table.values().iterator();
      buf.append("Connections: \n");
      buf.append("<><><><><><><>\n");
      while (values_it.hasNext()) {
        buf.append(((DsConnection) values_it.next()).toString());
        buf.append("\n");
      }
      values_it = m_ListeningTransports.values().iterator();
      buf.append("Transport Listeners: \n");
      buf.append("<><><><><><><><><><>\n");
      while (values_it.hasNext()) {
        buf.append(values_it.next());
        buf.append("\n");
      }
    }

    String ret = buf.toString();

    if (DsLog4j.connectionMCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionMCat.log(Level.INFO, ret);
    }

    return ret;
  }

  /** Stop all listeners and timers, close all connections and clear the connection table. */
  void stop() {
    setListenersActive(false);
    closeAllConnections();
    if (m_Timer != null) {
      m_Timer.cancel();
    }
    if (m_sweepTimer != null) {
      m_sweepTimer.cancel();
    }
    m_TCPCount = 0;
    connectionCountUDP = 0;
    connectionCountTCP = 0;
    connectionCountTLS = 0;
    m_Table.clear();
  }

  /** close all connections and clear the connection table. */
  void clearConnections() {
    closeAllConnections();
    m_TCPCount = 0;
    connectionCountUDP = 0;
    connectionCountTCP = 0;
    connectionCountTLS = 0;
    m_Table.clear();
  }

  /** Restart listeners and timers. */
  void init() {
    m_Table = new ConcurrentHashMap();
    m_sweepTimer = new SweepTimer();

    // GOGONG 04.12.06 CSCsd90062 - create a timer daemon thread
    //     so that the parent thread can be shut down without waiting for this non-daemon thread.
    // qfang - 07.05.06 - CSCse63190 switch to JDK's Timer implementation
    m_Timer = new Timer(true);
    m_Timer.schedule(m_sweepTimer, m_CleanupInterval * 1000L, m_CleanupInterval * 1000L);
    setListenersActive(true);
  }

  /**
   * Puts a connection into the list overriding any previous connection with same address and port.
   * For TCP or TLS connections, potentially increments the total number of connections and, if max
   * is reached, disables new incoming connections.
   *
   * @param connection connection to put into the table
   */
  public void put(DsConnection connection) {
    DsBindingInfo aBindingInfo = connection.getBindingInfo();
    int transport_type = aBindingInfo.getTransport();
    InetAddress localAddr = aBindingInfo.getLocalAddress();
    int localPort = aBindingInfo.getLocalPort();

    CTableListenKey key =
        new CTableListenKey(
            localAddr,
            localPort,
            aBindingInfo.getRemoteAddress(),
            aBindingInfo.getRemotePort(),
            transport_type);
    if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG))
      DsLog4j.connectionMCat.log(
          Level.DEBUG, "Add connection. key: " + key + " connection: " + connection);

    synchronized (m_TableLock) {
      Object previous = m_Table.put(key, connection);

      if (previous == null
          && ((transport_type == DsSipTransportType.TLS)
              || (transport_type == DsSipTransportType.TCP))) {
        ++m_TCPCount;
      }
      if (previous == null) {
        switch (transport_type) {
          case DsSipTransportType.TCP:
            connectionCountTCP++;
            break;
          case DsSipTransportType.TLS:
            connectionCountTLS++;
            break;
          case DsSipTransportType.UDP:
            connectionCountUDP++;
            break;
        }
      }
      if (m_TCPCount >= m_MaxConnections) {
        if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG))
          DsLog4j.connectionMCat.log(
              Level.DEBUG,
              "m_TCPCount("
                  + m_TCPCount
                  + ") >= m_MaxConnections("
                  + m_MaxConnections
                  + ") de-activating TCP listeners");
        setTCPActive(false);
      }
    }

    if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG))
      DsLog4j.connectionMCat.log(Level.DEBUG, "put m_TCPCount == " + m_TCPCount);
  }

  /**
   * Set the number of TCP/TLS connections after which TCP/TLS listeners will be de-activated.
   *
   * @param max_connections the max number of TCP/TLS connections
   */
  public void setMaxConnections(int max_connections) {
    synchronized (m_ListeningLock) {
      m_MaxConnections = max_connections;
      if (m_TCPCount >= m_MaxConnections) {
        if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionMCat.log(
              Level.DEBUG,
              "setMaxConnections: m_TCPCount("
                  + m_TCPCount
                  + ") >= m_MaxConnections("
                  + m_MaxConnections
                  + ") de-activating TCP listeners");
        }

        setTCPActive(false);
      } else {
        if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionMCat.log(
              Level.DEBUG,
              "setMaxConnections: m_TCPCount("
                  + m_TCPCount
                  + ") < m_MaxConnections("
                  + m_MaxConnections
                  + ") activating TCP listeners");
        }

        setTCPActive(true);
      }
    }
  }

  /**
   * Returns the maximum number of connections that this connection table supports.
   *
   * @return the maximum number of TCP/TLS connections
   */
  public int getMaxConnections() {
    synchronized (m_ListeningLock) {
      return m_MaxConnections;
    }
  }

  /**
   * Removes the specified connection and closes its socket. If the connection transport is TCP or
   * TLS, the tcp connection count is decremented and the listening TCP server sockets are
   * potentially re-activated.
   *
   * @param connection connection to remove
   */
  public void remove(DsConnection connection) {
    remove(connection, false);
  }

  /**
   * Removes the specified connection and closes its socket. If the connection transport is TCP or
   * TLS, the tcp connection count is decremented and the listening TCP server sockets are
   * potentially re-activated.
   *
   * @param connection connection to remove
   * @param notClose will not call closeSocket if notClose is true
   */
  public void remove(DsConnection connection, boolean notClose) {
    DsBindingInfo aBindingInfo = connection.getBindingInfo();
    int transport_type = aBindingInfo.getTransport();
    InetAddress localAddr = aBindingInfo.getLocalAddress();
    int localPort = aBindingInfo.getLocalPort();

    CTableListenKey key = (CTableListenKey) keyFactory.get();
    key.set(
        localAddr,
        localPort,
        aBindingInfo.getRemoteAddress(),
        aBindingInfo.getRemotePort(),
        transport_type);

    if (DsLog4j.connectionMCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionMCat.log(
          Level.INFO, "remove connection. key: " + key + " connection: " + connection);
    }

    synchronized (m_TableLock) {
      if (m_Table.remove(key) != null) {
        if ((transport_type == DsSipTransportType.TLS)
            || (transport_type == DsSipTransportType.TCP)) {
          --m_TCPCount;
        }
        switch (transport_type) {
          case DsSipTransportType.TCP:
            connectionCountTCP--;
            break;
          case DsSipTransportType.TLS:
            connectionCountTLS--;
            break;
          case DsSipTransportType.UDP:
            connectionCountUDP--;
            break;
        }
        if (connection.isConnecting()) {
          if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG))
            DsLog4j.connectionMCat.log(Level.DEBUG, "Removed connecting connection");
          // Skip all the other processing.
          return;
        }

      } else {
        if (DsLog4j.connectionMCat.isEnabled(Level.INFO)) {
          DsLog4j.connectionMCat.info("ConnectionTable.remove key " + key + " not found");
        }
      }
      if (connection.isConnecting()) {
        // Skip all the other processing.
        return;
      }

      if (notClose == false) {
        closeSocket(connection);
      }

      if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionMCat.log(Level.DEBUG, "remove m_TCPCount == " + m_TCPCount);
      }

      if (m_TCPCount < m_MaxConnections) {
        if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionMCat.log(
              Level.DEBUG,
              "m_TCPCount("
                  + m_TCPCount
                  + ") < m_MaxConnections("
                  + m_MaxConnections
                  + ") activating TCP listeners");
        }

        setTCPActive(true);
      }
    }

    connection = null;
    key = null;
  }

  /**
   * Sets the cleanup interval for connections. At a period defined by 'aSeconds,' this class will
   * query (using DsConnection.shouldClose) each DsConnection to determine whether it should be
   * closed and removed from the table.
   *
   * @param aSeconds the period in seconds for the cleanup timer
   */
  public void setCleanupInterval(int aSeconds) {
    m_CleanupInterval = aSeconds;

    if (m_sweepTimer != null) {
      m_sweepTimer.cancel();
    }
    m_sweepTimer = new SweepTimer();
    m_Timer.schedule(m_sweepTimer, m_CleanupInterval * 1000L, m_CleanupInterval * 1000L);

    if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG))
      DsLog4j.connectionMCat.log(
          Level.DEBUG, "setCleanupInterval m_CleanupInterval == " + aSeconds);
  }

  /**
   * Determine the cleanup interval described in setCleanupInterval.
   *
   * @return the cleanup interval
   */
  public int getCleanupInterval() {
    return m_CleanupInterval;
  }

  /**
   * Determine the set of transport that are represented in the set of socket listeners.
   *
   * @return the transports that this transport layer is listening on represented as a set of
   *     Integers
   */
  Set getListeningTransports() {
    synchronized (m_ListeningLock) {
      HashSet ret_set = new HashSet();
      Enumeration listen_keys = getListenKeys();
      while (listen_keys.hasMoreElements()) {
        DsConnectionTable.CTableListenKey next =
            (DsConnectionTable.CTableListenKey) listen_keys.nextElement();
        ret_set.add(new Integer(next.getTransport()));
      }
      return ret_set;
    }
  }

  /**
   * Used to search for a CTableListenKey (InetAddress, port, protocol) for a given transport.
   *
   * @param transport_type the transport type to check for
   * @return a CTableListenKey whose transport type is 'transport_type' or null if a listen key for
   *     the transport cannot be found
   */
  CTableListenKey findListenKeyForTransport(int transport_type) {
    CTableListenKey ret_key = null;
    synchronized (m_ListeningLock) {
      Enumeration listen_keys = getListenKeys();
      while (listen_keys.hasMoreElements()) {
        DsConnectionTable.CTableListenKey next =
            (DsConnectionTable.CTableListenKey) listen_keys.nextElement();

        if (next.getTransport() == transport_type) {
          ret_key = next;
          break;
        }
      }

      if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionMCat.log(
            Level.DEBUG, "findCTableListenKeyForTransport returning " + ret_key);
      }
    }
    return ret_key;
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
    ArrayList lst = new ArrayList(3);
    synchronized (m_ListeningLock) {
      Enumeration listen_keys = getListenKeys();
      while (listen_keys.hasMoreElements()) {
        DsConnectionTable.CTableListenKey next =
            (DsConnectionTable.CTableListenKey) listen_keys.nextElement();

        if (next.getTransport() == DsSipTransportType.UDP) {
          lst.add(next);
        }
      }
      int size = lst.size();

      if (size == 0) {
        return null;
      } else if (size == 1) {
        return (DsTransportLayer.ListenKey) lst.get(0);
      } else {
        // here, I don't really care about thread safety of m_natctr since
        //    it's just for pseudo random selection
        return (DsTransportLayer.ListenKey) lst.get(m_natctr++ % lst.size());
      }
    }
  }

  private static int m_natctr = 0;

  /**
   * Get the number of listening sockets.
   *
   * @return the number of listening sockets
   */
  int getListeningTransportsCount() {
    synchronized (m_ListeningLock) {
      return m_ListeningTransports.size();
    }
  }

  /**
   * Remove the provided socket from the list of listening sockets. Updates the DsSipServerLocator's
   * list of supported transports. Note that this method does not close the connection.
   *
   * @param socket the socket which to remove
   */
  void removeTransportListener(InetAddress host_addr, int port, int transport) {
    synchronized (m_ListeningLock) {
      CTableListenKey key = (CTableListenKey) keyFactory.get();
      key.set(host_addr, port, transport);
      m_ListeningTransports.remove(key);

      m_TransportSet = getListeningTransports();
      DsSipResolverUtils.setGlobalSupportedTransports(new HashSet(m_TransportSet));

      if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionMCat.log(Level.DEBUG, "removeTransportListener removing " + key);
      }
    }
  }

  /**
   * Find a listening socket for the given binding information.
   *
   * @param inet_address the InetAddress to search for
   * @param port the port to search for
   * @param transport the transport to search for
   * @return the socket found or null if no socket was found
   */
  public DsTransportListener getTransportListener(
      InetAddress inet_address, int port, int transport) {
    synchronized (m_ListeningLock) {
      CTableListenKey key = (CTableListenKey) keyFactory.get();
      key.set(inet_address, port, transport);
      DsTransportListener ret_listener = (DsTransportListener) m_ListeningTransports.get(key);

      if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionMCat.log(Level.DEBUG, "getTransportListener returning " + ret_listener);
      }

      return ret_listener;
    }
  }

  /**
   * Add a listening socket. If the transport is TLS or TCP, its listening state will be set to
   * reflect the current number of active TCP connections (i.e. if max connections is exceeded, the
   * socket will be set inactive). The DsSipServerLocator class's list of supported transports is
   * updated.
   *
   * @param host_addr the InetAddress of the new listener
   * @param port the port of the new listener
   * @param transport the transport of the new listener
   * @param socket the new listener
   */
  void addTransportListener(
      InetAddress host_addr, int port, int transport, DsTransportListener listener) {
    synchronized (m_ListeningLock) {
      if ((transport == DsSipTransportType.TCP) || (transport == DsSipTransportType.TLS)) {
        try {
          listener.setActive(m_tcpActive);
        } catch (IOException e) {
          if (DsLog4j.connectionMCat.isEnabled(Level.WARN)) {
            DsLog4j.connectionMCat.warn(
                "addTransportListener: error trying setActive("
                    + m_tcpActive
                    + ") on listener "
                    + listener,
                e);
          }
        }
      }
      CTableListenKey key = new CTableListenKey(host_addr, port, transport);

      if (DsLog4j.connectionMCat.isEnabled(Level.DEBUG)) {
        DsLog4j.connectionMCat.log(Level.DEBUG, "addTransportListener adding " + key);
      }

      m_ListeningTransports.put(key, listener);
      m_TransportSet.add(new Integer(transport));
      DsSipResolverUtils.setGlobalSupportedTransports(new HashSet(m_TransportSet));
    }
  }

  /**
   * Set the TCP/TLS listeners to active or inactive.
   *
   * @param active set to <code>true</code> to turn on listeners or <code>false</code> to turn off
   *     listeners
   */
  private void setTCPActive(boolean active) {
    synchronized (m_ListeningLock) {
      if (m_tcpActive == active) {
        return;
      }

      Enumeration listen_keys = getListenKeys();
      while (listen_keys.hasMoreElements()) {
        DsConnectionTable.CTableListenKey next =
            (DsConnectionTable.CTableListenKey) listen_keys.nextElement();
        int transport_type = next.getTransport();
        if ((transport_type == DsSipTransportType.TCP)
            || (transport_type == DsSipTransportType.TLS)) {
          DsTransportListener listener = (DsTransportListener) m_ListeningTransports.get(next);
          if (listener != null) {
            try {
              listener.setActive(active);
            } catch (IOException e) {
              if (DsLog4j.connectionMCat.isEnabled(Level.WARN)) {
                DsLog4j.connectionMCat.warn(
                    "addTransportListener: error trying setActive("
                        + m_tcpActive
                        + ") on listener "
                        + listener,
                    e);
              }
            }
          }
        }
      }
      m_tcpActive = active;
    }
  }

  /** set listeners active or inactive */
  private void setListenersActive(boolean active) {
    synchronized (m_ListeningLock) {
      Enumeration listen_keys = getListenKeys();
      while (listen_keys.hasMoreElements()) {
        DsConnectionTable.CTableListenKey next =
            (DsConnectionTable.CTableListenKey) listen_keys.nextElement();
        DsTransportListener listener = (DsTransportListener) m_ListeningTransports.get(next);
        if (listener != null) {
          try {
            listener.setActive(active);
          } catch (IOException e) {
            if (DsLog4j.connectionMCat.isEnabled(Level.WARN)) {
              DsLog4j.connectionMCat.warn(
                  "addTransportListener: error trying setActive("
                      + m_tcpActive
                      + ") on listener "
                      + listener,
                  e);
            }
          }
        }
      }
    }
  }

  /** Close all connections. */
  private void closeAllConnections() {
    synchronized (m_TableLock) {
      Collection collection = m_Table.values();
      Iterator iterator = collection.iterator();
      while (iterator.hasNext()) {
        DsConnection connection = (DsConnection) iterator.next();
        iterator.remove();
        closeSocket(connection);
        connection = null;
      }
    }
  }

  /** Get the key objects associated with the listening ports setup. */
  Enumeration getListenKeys() {
    return m_ListeningTransports.keys();
  }

  /**
   * Close this connection's socket.
   *
   * @param connection the connection to close
   */
  public void closeSocket(DsConnection connection) {
    try {
      DsBindingInfo aBindingInfo = connection.getBindingInfo();
      // Remove the Connection Association, if it exists
      // for this connection.
      DsSipConnectionAssociations.dissociate(aBindingInfo);

      if (DsLog4j.connectionMCat.isEnabled(Level.INFO)) {
        DsLog4j.connectionMCat.log(
            Level.INFO,
            "Removing connection and closing socket going to connection == "
                + connection
                + "("
                + connection.getInetAddress().getHostAddress()
                + ':'
                + connection.getPortNo()
                + ';'
                + connection.getTransportType()
                + ")");
      }
      connection.closeSocket();
    } catch (Exception e) {
      if (DsLog4j.connectionMCat.isEnabled(Level.WARN)) {
        DsLog4j.connectionMCat.warn("closeSocket: remove socket close error", e);
      }
    }
  }

  public int getNumConns() {
    return m_Table.size();
  }

  /** This class implements the key used to store listening sockets. */
  public static class CTableListenKey extends DsTransportLayer.ListenKey {
    /** Default constructor. */
    public CTableListenKey() {
      super();
    }

    /**
     * Constructs table listen key with the specified listening address, port and transport type.
     *
     * @param netAddress the interface which the socket is listening on
     * @param port the listening port
     * @param transportType the listening transport type
     */
    public CTableListenKey(InetAddress netAddress, int port, int transportType) {
      super(netAddress, port, transportType);
    }

    /**
     * Constructs table listen key with the specified local and remote address.
     *
     * @param laddr the local interface
     * @param lport the local port
     * @param addr the remote interface
     * @param port the remote port
     * @param transport the listening transport type
     */
    public CTableListenKey(
        InetAddress laddr, int lport, InetAddress addr, int port, int transport) {
      super(laddr, lport, addr, port, transport);
    }
  }

  /**
   * This class is used to implement a 60 second (default) timer which checks the connection table
   * for connections that are no longer referenced.
   */
  // qfang - 07.05.06 - CSCse63190 switch to JDK's Timer implementation
  private class SweepTimer extends TimerTask {
    /**
     * This method handles connection cleanup for incoming and outgoing connections. It should never
     * be called directly. This is the callback for timer events. It is called at a period defined
     * by the value set in setCleanupInterval.
     *
     * <p>This method will call DsConnection.shouldClose for each DsConnection to determine whether
     * it should be closed and removed from the table.
     *
     * <p>The TCP connection count is updated, and TCP listening is potentially re-activated after
     * connections are removed.
     */
    public void run() {
      long time = System.currentTimeMillis();

      synchronized (m_TableLock) {
        Collection collection = m_Table.values();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
          DsConnection connection = (DsConnection) iterator.next();
          int transport_type = connection.getTransportType();
          if (connection.shouldClose(time)) {
            iterator.remove();
            switch (transport_type) {
              case DsSipTransportType.TCP:
                --m_TCPCount;
                connectionCountTCP--;
                break;
              case DsSipTransportType.TLS:
                --m_TCPCount;
                connectionCountTLS--;
                break;
              case DsSipTransportType.UDP:
                connectionCountUDP--;
                break;
              default:
                break;
            }
            closeSocket(connection);
          }
          connection = null;
        }
      }

      if (m_TCPCount < m_MaxConnections) {
        setTCPActive(true);
      }
    }
  }

  private class ConnectionsCount extends TimerTask {
    int counter = 0;
    public int maxConnectionCountUDP = 0;
    public int maxConnectionCountTCP = 0;
    public int maxConnectionCountTLS = 0;

    public void run() {
      counter++;
      if (connectionCountUDP > maxConnectionCountUDP) maxConnectionCountUDP = connectionCountUDP;
      if (connectionCountTCP > maxConnectionCountTCP) maxConnectionCountTCP = connectionCountTCP;
      if (connectionCountTLS > maxConnectionCountTLS) maxConnectionCountTLS = connectionCountTLS;
      if (counter == DsConfigManager.NOTIFICATION_INTERVAL) {
        sendConnectionCountNotification(
            maxConnectionCountUDP, maxConnectionCountTCP, maxConnectionCountTLS);
        counter = 0;
        reset();
      }
    }

    public void reset() {
      maxConnectionCountUDP = 0;
      maxConnectionCountTCP = 0;
      maxConnectionCountTLS = 0;
    }
  }

  /**
   * getConnectionTable content of connection table to the tabular data.
   *
   * <p>This method is for REST API v1/runtime/connections
   *
   * @return TabularData @OutPut contents={localIP=ip, localPORT=port, remoteIP=ip, remotePORT=port,
   *     transport, connectionState, sendMessageQueueCount}
   */
  public TabularData getConnectionTableDetails() {
    DsConnection connection = null;
    String connectionState = null;

    try {

      connTableData = new TabularDataSupport(connectionTableType);
      Collection collection = m_Table.values();
      Iterator iterator = collection.iterator();
      DsBindingInfo aBindingInfo = null;
      DsAbstractConnection absCon = null;
      DsTcpConnection queuedMsgCount = null;
      String transport = null;
      CompositeData compData = null;
      int sendMessageQueueCount = 0;

      while (iterator.hasNext()) {
        connection = (DsConnection) iterator.next();
        aBindingInfo = connection.getBindingInfo();
        absCon = (DsAbstractConnection) connection;

        sendMessageQueueCount = 0;
        transport = DsSipTransportType.getTypeAsString(aBindingInfo.getTransport());
        if (transport.equals(DsSipTransportType.STR_TLS)) {
          queuedMsgCount = (DsTcpConnection) absCon;
          sendMessageQueueCount = queuedMsgCount.m_sendQueue.size();
        }

        if (absCon.isConnecting() == true) {
          connectionState = "connecting";
        } else if (absCon.isShutingDown() == true) {
          connectionState = "shuttingDown";
        } else {
          connectionState = "active";
        }

        Object[] connectionTableItemValues = {
          aBindingInfo.getLocalAddress().getHostAddress(),
          aBindingInfo.getLocalPort(),
          aBindingInfo.getRemoteAddressStr(),
          aBindingInfo.getRemotePort(),
          transport,
          connectionState,
          sendMessageQueueCount
        };
        compData =
            new CompositeDataSupport(
                connectionTableComposite, connectionTableItemNames, connectionTableItemValues);
        connTableData.put(compData);
      }
    } // end of try
    catch (ClassCastException cce) {
      if (DsLog4j.connectionMCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionMCat.log(
            Level.ERROR,
            "getConnectionTabledetails: Exception for Connection : " + connection,
            cce);
      }
    } catch (Exception e) {
      if (DsLog4j.connectionMCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionMCat.log(
            Level.ERROR, "getConnectionTabledetails: Error reading Connection : " + connection, e);
      }
    }

    return connTableData;
  }

  /**
   * getConnectionTableSummary content of connection table to the Composite data.
   *
   * <p>This method is for REST API v1/runtime/connections/summary
   *
   * @return a CompositeData contents={TcpActiveConnections, TlsActiveConnections,
   *     udpActiveConnections, numberOfConnections, tcpconnectionCount, tcpshuttigDownConnections,
   *     tlsconnectionCount, tlsshuttigDownConnections, udpconnectionCount,
   *     udpshuttigDownConnections}
   */
  public CompositeData getConnectionTableSummary() {
    CompositeData compositeSummaryData = null;
    DsConnection connection = null;
    try {

      Collection collection = m_Table.values();
      Iterator iterator = collection.iterator();
      DsBindingInfo aBindingInfo = null;
      DsAbstractConnection absCon = null;

      int udpConn = 0;
      int udpActiveConn = 0;
      int udpShuttingDownConn = 0;
      int tcpConn = 0;
      int tcpActiveConn = 0;
      int tcpShuttingDownConn = 0;
      int tlsConn = 0;
      int tlsActiveConn = 0;
      int tlsShuttingDownConn = 0;
      int tcpConnectingConnections = 0;
      int udpConnectingConnections = 0;
      int tlsConnectingConnections = 0;

      while (iterator.hasNext()) {
        connection = (DsConnection) iterator.next();
        aBindingInfo = connection.getBindingInfo();
        absCon = (DsAbstractConnection) connection;

        switch (aBindingInfo.getTransport()) {
          case DsSipTransportType.UDP:
            udpConn++;
            if (absCon.isConnecting()) {
              udpConnectingConnections++;
            } else if (absCon.isShutingDown()) {
              udpShuttingDownConn++;
            } else {
              udpActiveConn++;
            }
            break;
          case DsSipTransportType.TCP:
            tcpConn++;
            if (absCon.isConnecting()) {
              tcpConnectingConnections++;
            } else if (absCon.isShutingDown()) {
              tcpShuttingDownConn++;
            } else {
              tcpActiveConn++;
            }
            break;
          case DsSipTransportType.TLS:
            tlsConn++;
            if (absCon.isConnecting()) {
              tlsConnectingConnections++;
            } else if (absCon.isShutingDown()) {
              tlsShuttingDownConn++;
            } else {
              tlsActiveConn++;
            }
        }
      }

      Object[] connectionTableSummaryItemValues = {
        m_Table.size(),
        udpConn,
        udpActiveConn,
        udpShuttingDownConn,
        udpConnectingConnections,
        tcpConn,
        tcpActiveConn,
        tcpShuttingDownConn,
        tcpConnectingConnections,
        tlsConn,
        tlsActiveConn,
        tlsShuttingDownConn,
        tlsConnectingConnections
      };
      compositeSummaryData =
          new CompositeDataSupport(
              connectionTableSummaryComposite,
              connectionTableSummaryItemNames,
              connectionTableSummaryItemValues);
    } // end of try
    catch (ClassCastException cce) {
      if (DsLog4j.connectionMCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionMCat.log(
            Level.ERROR,
            "getConnectionTableSummary: Exception error Connection : " + connection,
            cce);
      }
    } catch (Exception e) {
      if (DsLog4j.connectionMCat.isEnabled(Level.ERROR)) {
        DsLog4j.connectionMCat.log(
            Level.ERROR, "getConnectionTableSummary : Error reading Connection : " + connection, e);
      }
    }

    return compositeSummaryData;
  }

  public void sendConnectionCountNotification(
      int maxConnectionCountUDP, int maxConnectionCountTCP, int maxConnectionCountTLS) {
    // REFACTOR
    //    LinkedHashMap UDPdata = new LinkedHashMap();
    //    UDPdata.put(SIPMBean.CONNECTION, "UDP");
    //    UDPdata.put(SIPMBean.COUNT, maxConnectionCountUDP);
    //
    //    LinkedHashMap TCPdata = new LinkedHashMap();
    //    TCPdata.put(SIPMBean.CONNECTION, "TCP");
    //    TCPdata.put(SIPMBean.COUNT, maxConnectionCountTCP);
    //
    //    LinkedHashMap TLSdata = new LinkedHashMap();
    //    TLSdata.put(SIPMBean.CONNECTION, "TLS");
    //    TLSdata.put(SIPMBean.COUNT, maxConnectionCountTLS);

    //    SIPConnectionTableMBeanImpl.sendNotification(
    //        SIPMBean.CONNECTION_COUNT_METRICES_NOTIFICATION, UDPdata);
    //    SIPConnectionTableMBeanImpl.sendNotification(
    //        SIPMBean.CONNECTION_COUNT_METRICES_NOTIFICATION, TCPdata);
    //    SIPConnectionTableMBeanImpl.sendNotification(
    //        SIPMBean.CONNECTION_COUNT_METRICES_NOTIFICATION, TLSdata);
  }

  public int getConnectionCountUDP() {
    return connectionCountUDP;
  }

  public int getConnectionCountTCP() {
    return connectionCountTCP;
  }

  public int getConnectionCountTLS() {
    return connectionCountTLS;
  }
} // End class DsConnectionTable

/** A ThreadLocal initializer class for the listen keys. */
class TableListenKeyInitializer extends ThreadLocal {
  public Object initialValue() {
    return new DsConnectionTable.CTableListenKey();
  }
} // ends class TableListenKeyInitializer
