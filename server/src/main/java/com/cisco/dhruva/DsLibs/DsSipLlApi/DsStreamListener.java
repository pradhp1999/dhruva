// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.logging.log4j.Level;

/** This is the abstract base class for all stream-based listeners. */
public abstract class DsStreamListener extends DsTransportListener {
  /** The TCP listen port port to read messages from. */
  protected int localPort;
  /** The address of the local site. */
  protected InetAddress localAddress;
  /** The max size of the TCP queue. */
  protected static final int TCP_BACKLOG =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_TCP_BACKLOG, DsConfigManager.PROP_TCP_BACKLOG_DEFAULT);

  /** A reference to the connection table. */
  protected DsConnectionTable m_ConnectionTable;
  /** The factory that creates connections. */
  protected DsConnectionFactory m_ConnectionFactory;
  /** TCP socket input timeout. */
  protected long m_IncomingSocketTimeout;

  /**
   * Accepts incoming requests and creates connection on the incoming requests.
   *
   * @return the created connection
   * @throws IOException if an error occurs while accepting the incoming requests and creating
   *     connection
   */
  public abstract DsConnection accept() throws IOException;

  /**
   * Creates the binding information based on the transport type and the connection.
   *
   * @param network the network to associate with this listener
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsStreamListener(DsNetwork network) throws IOException {
    super(network);

    if (DsLog4j.wireCat.isEnabled(Level.DEBUG))
      DsLog4j.wireCat.log(Level.DEBUG, "DsStreamListener");
  }

  /**
   * Creates the binding information based on the transport type and the connection.
   *
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public DsStreamListener() throws IOException {
    super(); // will use default network

    if (DsLog4j.wireCat.isEnabled(Level.DEBUG))
      DsLog4j.wireCat.log(Level.DEBUG, "DsStreamListener");
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
  public void setIncomingConnectionTimeout(long time_seconds) {
    m_IncomingSocketTimeout = time_seconds;
  }

  /**
   * Creates a message reader.
   *
   * @param connection the connection for the message reader to read from
   * @throws IOException of thrown by the underlying connection
   */
  protected void createMessageReader(DsConnection connection) throws IOException {
    DsSipTransactionManager.getTransportLayer().createMessageReader(connection);
  }

  /**
   * Listens for new connections or data.
   *
   * @throws IOException if there is an error while listening on the server socket
   */
  protected void doListen() throws IOException {
    DsConnection incoming_connection;

    incoming_connection = accept();
    m_ConnectionTable.put(incoming_connection);

    try {
      createMessageReader(incoming_connection);
    } catch (IOException e) {
      if (DsLog4j.wireCat.isEnabled(Level.ERROR))
        DsLog4j.wireCat.log(Level.ERROR, "Can't create a reader to process messages: " + e);
    }
  }

  /**
   * Tells whether this stream listener is enabled for TLS transport type.
   *
   * @return <code>true</code> if enabled for TLS transport type, <code>false</code> otherwise
   */
  public boolean isSslEnabled() {
    return getTransport() == DsSipTransportType.TLS;
  }

  @Override
  protected String getLocalAddress() {
    if (localAddress != null) return localAddress.getHostAddress();
    return null;
  }

  @Override
  protected int getLocalPort() {
    return localPort;
  }
}
