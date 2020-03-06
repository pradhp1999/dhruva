// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSocket;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.apache.logging.log4j.Level;

/**
 * This class goes into an infinite loop of accepting requests for connections, and allocates the
 * work of processing the messages from that connection to another thread.
 */
@SuppressFBWarnings
public class DsTcpListener extends DsStreamListener {
  /** The socket on which we listen for requests. */
  protected ServerSocket serverSocket;

  // ///////////////////////////////////////////////////////
  //  Constructors
  //

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
  public DsTcpListener(
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
  public DsTcpListener(
      DsNetwork network,
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory)
      throws SocketException, UnknownHostException, IOException {
    super(network);

    // create the TCP socket
    localPort = port;
    localAddress = address;
    m_ConnectionTable = connectionTable;
    m_ConnectionFactory = connectionFactory;
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
  public DsTcpListener(
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
  public DsTcpListener(
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory)
      throws SocketException, UnknownHostException, IOException {
    this(DsNetwork.getDefault(), port, address, connectionTable, connectionFactory);
  }

  // ///////////////////////////////////////////////////////

  /*
   * javadoc inherited
   */
  public DsConnection accept() throws IOException {
    DsSocket socket = new DsSocket(serverSocket.accept(), m_network);
    DsConnection connection = m_ConnectionFactory.createConnection(socket);
    DsBindingInfo bi = connection.getBindingInfo();
    bi.setNetwork(m_network);
    connection.setTimeout(m_IncomingSocketTimeout);
    return connection;
  }

  /*
   * javadoc inherited
   */
  public int getTransport() {
    return DsSipTransportType.TCP;
  }

  /*
   * javadoc inherited
   */
  protected void createSocket() throws IOException {
    serverSocket = new ServerSocket(localPort, TCP_BACKLOG, localAddress);
  }

  /*
   * javadoc inherited
   */
  protected void closeSocket() throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.debug("DsTcpListener.closeSocket() called.");
    }

    serverSocket.close();

    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.info(
          "DsTcpListener.closeSocket() - serverSocket.isClosed() = " + serverSocket.isClosed());
    }
  }
}
