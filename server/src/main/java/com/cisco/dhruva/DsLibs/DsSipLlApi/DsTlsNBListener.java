// Copyright (c) 2008-2009, 2015 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import com.cisco.dhruva.DsLibs.DsUtil.NetObjectsFactory;
import com.cisco.dhruva.util.saevent.ConnectionSAEventBuilder;
import com.cisco.dhruva.util.saevent.SAEventConstants;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.Level;

/**
 * This class goes into an infinite loop of accepting requests for connections, and allocates the
 * work of processing the messages from that connection to another thread.
 */
public class DsTlsNBListener extends DsSslListener implements DsSelectable {
  /** The unique id of this listener. */
  protected int m_id;

  /** The name of this listener. */
  protected String m_name;

  /** The selection key associated with this listener. */
  protected SelectionKey m_sk;

  /** The server socket channel that is awaiting connections. */
  protected ServerSocketChannel m_channel;

  protected ConcurrentLinkedQueue<SocketChannel> pendinConnections;

  /*
   * javadoc inherited
   * This method is not used anymore
   */
  public DsConnection accept() throws IOException {
    SocketChannel sc = m_channel.accept();
    if (sc == null) return null;

    DsSocket socket = new DsSocket(sc.socket(), m_network);
    ConnectionSAEventBuilder.logConnectionEvent(
        SAEventConstants.CONNECT,
        SAEventConstants.TLS,
        SAEventConstants.IN,
        socket.getLocalAddress(),
        socket.getLocalPort(),
        socket.getRemoteInetAddress(),
        socket.getRemotePort());
    DsConnection conn =
        m_ConnectionFactory.createConnection(
            socket, m_context); // get Connection from conenction factory
    // this connection is complete connection, i.e tls channel established, add
    // CAF-nio conn.setWorkQueue(DsSipTransportLayer.getWorkQueue());
    conn.setTimeout(m_IncomingSocketTimeout);
    // lock the table before adding pending connection to connection table??
    /*this is not needed since connection is added in the DsConnection object itself, as soon as the handshake is completed
    m_ConnectionTable.put(conn);
       DsTlsNBConnection tlsConn = (DsTlsNBConnection)conn;
       tlsConn.initiateHandshake();
       tlsConn.connected();*/
    return conn;
  }

  /**
   * Constructor that creates an SSL listener.
   *
   * @param network the network to associate with this listener
   * @param port the listen port
   * @param address the listening address
   * @param connectionTable the connection table that contains all the created connections
   * @param connectionFactory object to use to create connections
   * @param context the SSL context that will be used to create new SSL connections
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException if no IP address for the local host could be found.
   */
  public DsTlsNBListener(
      DsNetwork network,
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory,
      DsSSLContext context)
      throws SocketException, UnknownHostException, IOException {
    super(network, port, address, connectionTable, connectionFactory, context);
    DsLog4j.connectionCat.debug("DsTlsNBListener(): context set " + context);
    m_name = "TLS-Listener." + port;
    m_id = port;
    pendinConnections = new ConcurrentLinkedQueue<>();
  }

  /*
   * javadoc inherited
   */
  public int getTransport() {
    return DsSipTransportType.TLS;
  }

  /**
   * Creates the listening socket.
   *
   * @throws IOException if ServerSocket couldn't be created
   */
  protected void createSocket() throws IOException {

    m_channel = NetObjectsFactory.getServerSocketChannel();
    m_channel.socket().setReuseAddress(true);

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "Creating nio TLS Listener ServerSocket on " + localAddress + ":" + localPort);
    }

    int backlog = 1024;
    m_channel.socket().bind(new InetSocketAddress(localAddress, localPort), backlog);
    m_channel.configureBlocking(false);

    DsSelector.register(this);
  }

  /**
   * Method used to close the server socket.
   *
   * @throws IOException if the socket cannot be closed
   */
  protected void closeSocket() throws IOException {
    if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
      DsLog4j.wireCat.log(Level.INFO, "Closing DsTlsNBListener channel");
    }

    m_channel.close();
    DsSelector.getInstance().getSelector().wakeup();
  }

  public SelectableChannel getChannel() {
    return m_channel;
  }

  public int getOperation() {
    return SelectionKey.OP_ACCEPT;
  }

  /**
   * Accept the tcp connection for given SelectionKey. Then enqueue this selectable to init
   * handshake
   *
   * @param sk
   */
  public void accept(SelectionKey sk) {
    try {
      if (sk == null) {
        // watch for initial call to run() - maybe we could handle this better
        // when this code gets polished a little
        return;
      }

      if (!sk.isAcceptable()) {
        if (DsLog4j.wireCat.isEnabled(Level.INFO)) {
          DsLog4j.wireCat.info("process() called but the key was not acceptable.");
        }
        return;
      }

      // accept the tcp connection and add it somewhere so that it can be used to initiate the
      // handshake by calling the process()
      SocketChannel sc = m_channel.accept();
      if (sc != null) {
        // put this socketChannel into concurrent queue, so that process() can use it to initiate
        // the handshake
        if (DsLog4j.connectionCat.isDebugEnabled()) {
          DsLog4j.connectionCat.debug(
              "TCP channel for TLS NIO connection was established and registered");
        }
        pendinConnections.add(sc);
      }
      // ChannelIO cio = ChannelIOSecure.getInstance(sc, false /* non-blocking */, sslContext);

      // NO NEED to reregister, since it is safe to accept in multiple concurrent threads
      // so we do not unregister
      // re-register interest for this key

    } catch (Exception e) {
      ServerSocket socket = null;
      if (m_channel != null) socket = m_channel.socket();
      if (DsLog4j.wireCat.isEnabled(Level.ERROR)) {
        DsLog4j.wireCat.error("Exception processing nio accept event in : " + socket, e);
      }
      // REFACTOR
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
    }
  }
  /*
   * call this to complete the handshake of all established tcp channels
   */
  public void process() {
    try {
      if (m_sk == null) return;
      if (!m_sk.isValid()) {
        ServerSocket socket = null;
        if (m_channel != null) socket = m_channel.socket();
        // REFACTOR
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
    }
    SocketChannel sc = pendinConnections.poll();
    if (sc == null) {
      return;
    }
    try {
      DsSocket socket = new DsSocket(sc.socket(), m_network);
      DsConnection conn = m_ConnectionFactory.createConnection(socket, m_context);
      conn.setTimeout(m_IncomingSocketTimeout);
      ConnectionSAEventBuilder.logConnectionEvent(
          SAEventConstants.CONNECT,
          SAEventConstants.TLS,
          SAEventConstants.IN,
          socket.getLocalAddress(),
          socket.getLocalPort(),
          socket.getRemoteInetAddress(),
          socket.getRemotePort());
    } catch (Exception ie) {
      DsLog4j.connectionCat.debug(
          "DsTlsNBListener():Error while processing TLS Handshake {}", () -> ie);
    }
  }

  public void setSelectionKey(SelectionKey sk) {
    m_sk = sk;
  }

  public void run() {
    process();
  }

  @Override
  public void abort() {}

  @Override
  public int getID() {
    return m_id;
  }

  @Override
  public SelectionKey getSelectionKey() {
    return m_sk;
  }
}
