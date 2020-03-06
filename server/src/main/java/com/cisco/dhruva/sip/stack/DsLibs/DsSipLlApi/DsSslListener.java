// Copyright (c) 2005, 2015 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLSocket;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.*;
import javax.net.ssl.SSLSocket;

/**
 * This class goes into an infinite loop of accepting requests for connections, and allocates the
 * work of processing the messages from that connection to another thread.
 */
@SuppressFBWarnings
public class DsSslListener extends DsStreamListener {
  // --    SSLServerSocket serverSocket;
  ServerSocket serverSocket;
  DsSSLContext m_context;
  /*
   * javadoc inherited
   */
  public DsConnection accept() throws IOException {
    Socket s = serverSocket.accept();
    SSLSocket sslSocket =
        m_context.createSocket(
            s, s.getInetAddress().getHostAddress(), s.getPort(), true, true, m_network);
    DsSSLSocket socket = new DsSSLSocket(sslSocket, m_network);
    socket.setLayeredSocket(s);

    // --        DsSocket socket =  new DsSSLSocket((SSLSocket)serverSocket.accept(), m_network);
    DsConnection connection = m_ConnectionFactory.createConnection(socket);
    DsBindingInfo bi = connection.getBindingInfo();
    bi.updateBindingInfo(socket);
    bi.setNetwork(m_network);
    // information for tls socket connection acceptance
    // TODO saevent-restructure log a connectionEvent here

    connection.setTimeout(m_IncomingSocketTimeout);
    return connection;
  }

  /**
   * Constructor that creates an SSL listener.
   *
   * @param network the network to associate with this listener
   * @param portNumber the listen port
   * @param connectionTable the connection table that contains all the created connections
   * @param connectionFactory object to use to create connections
   * @param context the SSL context that will be used to create new SSL connections
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException if no IP address for the local host could be found.
   */
  public DsSslListener(
      DsNetwork network,
      int portNumber,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory,
      DsSSLContext context)
      throws SocketException, UnknownHostException, IOException {
    this(
        network,
        portNumber,
        InetAddress.getLocalHost(),
        connectionTable,
        connectionFactory,
        context);
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
  public DsSslListener(
      DsNetwork network,
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory,
      DsSSLContext context)
      throws SocketException, UnknownHostException, IOException {
    super(network);
    // create the SSL socket
    m_context = context;
    localPort = port;
    localAddress = address;
    m_ConnectionFactory = connectionFactory;
    m_ConnectionTable = connectionTable;
  }

  /**
   * Constructs SSL listener with the specified SSL context, connection table and the listen port
   * number.
   *
   * @param portNumber the listen port
   * @param connectionTable the connection table that contains all the created connections
   * @param connectionFactory object to use to create connections
   * @param context the SSL context that will be used to create new SSL connections
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException if no IP address for the local host could be found.
   */
  public DsSslListener(
      int portNumber,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory,
      DsSSLContext context)
      throws SocketException, UnknownHostException, IOException {
    this(
        DsNetwork.getDefault(),
        portNumber,
        InetAddress.getLocalHost(),
        connectionTable,
        connectionFactory,
        context);
  }

  /**
   * Constructs SSL listener with the specified SSL context, connection table, listen address and
   * the listen port number.
   *
   * @param port the listen port
   * @param address the listening address
   * @param connectionTable the connection table that contains all the created connections
   * @param connectionFactory object to use to create connections
   * @param context the SSL context that will be used to create new SSL connections
   * @throws SocketException not thrown any more, but its there for backward compatibility and may
   *     be removed in the next release
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   */
  public DsSslListener(
      int port,
      InetAddress address,
      DsConnectionTable connectionTable,
      DsConnectionFactory connectionFactory,
      DsSSLContext context)
      throws SocketException, UnknownHostException, IOException {
    this(DsNetwork.getDefault(), port, address, connectionTable, connectionFactory, context);
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
    // --        serverSocket = m_context.createServerSocket(localPort, 50, localAddress);
    serverSocket = new ServerSocket(localPort, TCP_BACKLOG, localAddress);
  }

  /**
   * Method used to close the server socket.
   *
   * @throws IOException if the socket cannot be closed
   */
  protected void closeSocket() throws IOException {
    serverSocket.close();
  }
}
