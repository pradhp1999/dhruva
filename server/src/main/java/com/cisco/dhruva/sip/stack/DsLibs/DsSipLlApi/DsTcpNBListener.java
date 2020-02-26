// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import org.apache.logging.log4j.Level;

/**
 * This class goes into an infinite loop of accepting requests for connections, and allocates the
 * work of processing the messages from that connection to another thread. This creates Non-Blocking
 * TCP connections.
 */
public class DsTcpNBListener extends DsTcpListener implements DsSelectable {
  /** The unique id of this listener. */
  protected int m_id;

  /** The selection key associated with this listener. */
  protected SelectionKey m_sk;

  /** The server socket channel that is awaiting connections. */
  protected ServerSocketChannel m_channel;

  /** The max size of the TCP queue when using nio. */
  protected static final int NON_BLOCKING_TCP_BACKLOG =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_TCP_BACKLOG, DsConfigManager.PROP_NON_BLOCKING_TCP_BACKLOG_DEFAULT);

  /**
   * Constructs TCP listener with the specified connection table and the listen port number.
   *
   * @param network the network to associate with this listener
   * @param portNumber the listen port
   * @param connectionTable the connection table that contains all the created connections
   * @param connectionFactory the factory used to create connections
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException if no IP address for the local host could be found.
   */
  public DsTcpNBListener(
      DsNetwork network,
      int portNumber,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory)
      throws SocketException, UnknownHostException, IOException {
    this(network, portNumber, InetAddress.getLocalHost(), connectionTable, connectionFactory);
  }

  /**
   * Constructs TCP listener with the specified connection table, listen address and the listen port
   * number.
   *
   * @param network the network to associate with this listener
   * @param port the listen port
   * @param address the listening address
   * @param connectionTable the connection table that contains all the created connections
   * @param connectionFactory the factory used to create connections
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   */
  public DsTcpNBListener(
      DsNetwork network,
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory)
      throws SocketException, UnknownHostException, IOException {
    super(network, port, address, connectionTable, connectionFactory);

    if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
      DsLog4j.wireCat.log(Level.DEBUG, "DsTcpNBListener");
    }

    m_id = port;
  }

  /**
   * Constructs TCP listener with the specified connection table and the listen port number.
   *
   * @param portNumber the listen port
   * @param connectionTable the connection table that contains all the created connections
   * @param connectionFactory the factory used to create connections
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException if no IP address for the local host could be found.
   */
  public DsTcpNBListener(
      int portNumber, DsConnectionTable connectionTable, DsConnectionFactory connectionFactory)
      throws SocketException, UnknownHostException, IOException {
    this(
        DsNetwork.getDefault(),
        portNumber,
        InetAddress.getLocalHost(),
        connectionTable,
        connectionFactory);
  }

  /**
   * Constructs TCP listener with the specified connection table, listen address and the listen port
   * number.
   *
   * @param port the listen port
   * @param address the listening address
   * @param connectionTable the connection table that contains all the created connections
   * @param connectionFactory the factory used to create connections
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   */
  public DsTcpNBListener(
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory)
      throws SocketException, UnknownHostException, IOException {
    this(DsNetwork.getDefault(), port, address, connectionTable, connectionFactory);
  }

  /*
   * javadoc inherited
   */
  protected void createMessageReader(DsConnection connection) throws IOException {
    if (DsLog4j.wireCat.isEnabled(Level.TRACE)) {
      DsLog4j.wireCat.log(Level.TRACE, "Creating NB Message Reader");
    }

    DsSipTransactionManager.getTransportLayer().createMessageReader(connection, true);

    if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
      DsLog4j.wireCat.log(Level.DEBUG, "Created NB Message Reader");
    }
  }

  /*
   * javadoc inherited
   */
  public DsConnection accept() throws IOException {
    SocketChannel sc = m_channel.accept();

    if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
      DsLog4j.wireCat.log(Level.INFO, "Accepted a connection [" + sc + "]");
    }

    if (null == sc) return null;

    Socket s = sc.socket();
    DsSocket socket = new DsSocket(s, m_network);
    DsConnection connection = m_ConnectionFactory.createConnection(socket);
    connection.setTimeout(m_IncomingSocketTimeout);
    m_ConnectionTable.put(connection);
    // Notify the acceptance of tcp connection
    // TODO saevent-restructure add a ConnectionEvent here
    return connection;
  }

  /*
   * javadoc inherited
   */
  protected void createSocket() throws IOException {
    if (DsLog4j.wireCat.isEnabled(Level.DEBUG)) {
      DsLog4j.wireCat.log(
          Level.DEBUG, "Creating listening socket(addr/port): " + localAddress + "/" + localPort);
    }
    m_channel = NetObjectsFactory.getServerSocketChannel();
    m_channel.socket().setReuseAddress(true);

    m_channel
        .socket()
        .bind(new InetSocketAddress(localAddress, localPort), NON_BLOCKING_TCP_BACKLOG);
    if (m_id == 0) {
      m_id = m_channel.socket().getLocalPort();
    }

    m_channel.configureBlocking(false);
  }

  /*
   * javadoc inherited
   */
  public void setActive(boolean active) throws IOException {
    synchronized (m_lock) {
      if (active == m_active) return;

      if (active) {
        m_active = true;
        initialize();
        DsSelector.register(this);
      } else {
        m_active = false;
        doClose();
      }
    }
  }

  /*
   * javadoc inherited
   */
  protected void closeSocket() throws IOException {
    if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
      DsLog4j.wireCat.log(
          Level.INFO, "Closing DsTcpNBListener channel for socket " + m_channel.socket());
    }

    m_channel.close();
    DsSelector.getInstance().getSelector().wakeup();
  }

  //////////////////////////////////
  // BEGIN DsSelectable Interface //
  //////////////////////////////////

  public int getID() {
    return m_id;
  }

  public SelectableChannel getChannel() {
    return m_channel;
  }

  public int getOperation() {
    return SelectionKey.OP_ACCEPT;
  }

  public SelectionKey getSelectionKey() {
    return m_sk;
  }

  public void setSelectionKey(SelectionKey sk) {
    m_sk = sk;
  }

  ////////////////////////////////
  // END DsSelectable Interface //
  ////////////////////////////////

  //////////////////////////////////
  // BEGIN DsUnitOfWork Interface //
  //////////////////////////////////

  public void abort() {}

  public void run() {
    process();
  }

  public void process() {
    if (m_sk == null) {
      // watch for initial call to run() - maybe we could handle this better
      // when this code gets polished a little
      return;
    }

    try {
      if (!m_sk.isValid()) {
        ServerSocket socket = null;
        if (m_channel != null) socket = m_channel.socket();
        //        SIPListenerMBeanImpl.sendNotification(
        //            SIPListenerMBean.LISTENER_EXCEPTION,
        //            new String[] {
        //              "Selection Key is not valid closing ServerSocket LocalAddress = "
        //                  + getLocalAddress()
        //                  + " LocalPort = "
        //                  + getLocalPort()
        //            });
        closeSocket();
        return;
      }

      if (!m_sk.isAcceptable()) {
        // nothing to do if this key is not acceptable for some reason.
        if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
          // This does happen on startup, so only log at debug level
          DsLog4j.wireCat.info("process() called but the key was not acceptable.");
        }
        return;
      }

      // Loop to avoid many thread changes and selections during high connection rates
      while (accept() != null) {
        if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
          DsLog4j.wireCat.info("TCP-Listener." + m_id + ": Accepted new TCP connection.");
        }
      }

    } catch (Exception e) {
      ServerSocket socket = null;
      if (m_channel != null) socket = m_channel.socket();
      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
        DsLog4j.wireCat.error("Exception processing nio accept event in : " + socket, e);
      }
      //      SIPListenerMBeanImpl.sendNotification(
      //          SIPListenerMBean.LISTENER_ACCEPT_EXCEPTION,
      //          new String[] {
      //            "Exception processing nio accept event in ServerSocket LocalAddress = "
      //                + getLocalAddress()
      //                + " LocalPort = "
      //                + getLocalPort()
      //                + " Exception is "
      //                + e.getMessage()
      //          });
    } finally {
      DsSelector.interestOps(m_sk, SelectionKey.OP_ACCEPT);
    }
  }

  ////////////////////////////////
  // END DsUnitOfWork Interface //
  ////////////////////////////////
}
