// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDatagramSocket;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamClosedEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamErrorEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsInputStreamEvent;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executor;

/**
 * A listener that implements a DsSipConnection by delegating to a DsSipUdpConnection created when
 * the listener is brought up. This class enables NAT traversal since the source port of responses
 * sent using this connection will match that of the destination port (listen point) of the request.
 *
 * <p>An instance of this class (cast as a connection) is returned from
 * DsSipTransportLayer.findListenConnection which is invoked from
 * DsSipServerTransactionImpl.getViaConnection when a SIP request's via header has an rport value
 * (UAC is behind a NAT).
 *
 * <p>findListenConnection will copy the destination host/port into the DsSipNatUdpListener's
 * threadlocal binding info so that for the current invocation, the send methods will sent to the
 * host/port specified in findListenConnection.
 */
class DsSipNatUdpListener extends DsUdpListener implements DsSipConnection {
  /** The thread local binding information. */
  private static ThreadLocal m_tlBindingInfo = new ThreadLocal();

  /** The connection. */
  private DsSipUdpConnection m_connection;

  /**
   * Convenience function that defaults to listening on the local host.
   *
   * @param port the local port number to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   * @throws UnknownHostException if the host address is not known
   */
  public DsSipNatUdpListener(int port, Executor work_queue)
      throws IOException, UnknownHostException {
    super(port, work_queue);
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param port the local port number to listen
   * @param address the local address to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   * @throws UnknownHostException if the host address is not known
   */
  public DsSipNatUdpListener(int port, InetAddress address, Executor work_queue)
      throws IOException, UnknownHostException {
    super(port, address, work_queue);
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param socket the already open socket address to listen on
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   */
  public DsSipNatUdpListener(DsDatagramSocket socket, Executor work_queue) throws IOException {
    super(socket, work_queue);
  }

  //  /////////////   DsNetwork versions

  /**
   * Convenience function that defaults to listening on the local host.
   *
   * @param network the network associated with this listener
   * @param port the local port number to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   * @throws UnknownHostException if the host address is not known
   */
  public DsSipNatUdpListener(DsNetwork network, int port, Executor work_queue)
      throws IOException, UnknownHostException {
    super(network, port, work_queue);
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param network the network associated with this listener
   * @param port the local port number to listen
   * @param address the local address to listen
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   * @throws UnknownHostException if the host address is not known
   */
  public DsSipNatUdpListener(DsNetwork network, int port, InetAddress address, Executor work_queue)
      throws IOException, UnknownHostException {
    super(network, port, address, work_queue);
  }

  /**
   * Listen for UDP packets on the specified InetAddress/port.
   *
   * @param network the network associated with this listener
   * @param socket the already open socket address to listen on
   * @param work_queue the work queue on which the work (packets) to be queued to be processed
   * @throws IOException if an I/O error occurs
   */
  public DsSipNatUdpListener(DsNetwork network, DsDatagramSocket socket, Executor work_queue)
      throws IOException {
    super(network, socket, work_queue);
  }

  /**
   * Creates the listener's socket.
   *
   * @throws IOException if the socket could not be opened, or the socket could not bind to the
   *     specified local port
   */
  protected void createSocket() throws IOException {
    super.createSocket();
    if (m_connection == null) {
      m_connection = new DsSipUdpConnection(m_network, m_socket);
    }
  }

  // //////////////////////////////////////////////

  public byte[] send(DsSipMessage message) throws IOException {
    m_connection.setBindingInfo(getBindingInfo());
    return m_connection.send(message);
  }

  public byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
    return m_connection.sendTo(message, getInetAddress(), getPortNo(), txn);
  }

  public byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException {
    return m_connection.sendTo(message, getInetAddress(), getPortNo(), txn);
  }

  public void send(byte[] message, DsSipServerTransaction txn) throws IOException {
    m_connection.sendTo(message, getInetAddress(), getPortNo(), txn);
  }

  public void send(byte[] message, DsSipClientTransaction txn) throws IOException {
    m_connection.sendTo(message, getInetAddress(), getPortNo(), txn);
  }

  public void sendTo(byte[] message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    m_connection.sendTo(message, addr, port, txn);
  }

  public void sendTo(byte[] message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    m_connection.sendTo(message, addr, port, txn);
  }

  public byte[] sendTo(DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    return m_connection.sendTo(message, addr, port, txn);
  }

  public byte[] sendTo(DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    return m_connection.sendTo(message, addr, port, txn);
  }

  public void send(byte buffer[]) throws IOException {
    m_connection.sendTo(buffer, getInetAddress(), getPortNo());
  }

  public void sendTo(byte buffer[], InetAddress addr, int port) throws IOException {
    m_connection.sendTo(buffer, addr, port);
  }

  /**
   * Returns the underlying datagram socket class.
   *
   * @return the underlying datagram socket class
   */
  public DsDatagramSocket getSocket() {
    return ((DsUdpConnection) m_connection).getSocket();
  }

  public void closeSocket() throws IOException {
    m_connection.closeSocket();
  }

  public DsBindingInfo getBindingInfo() {
    DsBindingInfo info = (DsBindingInfo) m_tlBindingInfo.get();
    if (info == null) {
      info = (DsBindingInfo) m_connection.getBindingInfo().clone();
      m_tlBindingInfo.set(info);
    }
    return info;
  }

  public InetAddress getInetAddress() {
    return getBindingInfo().getRemoteAddress();
  }

  public int getPortNo() {
    return getBindingInfo().getRemotePort();
  }

  public int getTransportType() {
    return m_connection.getTransportType();
  }

  public long getTimeStamp() {
    return m_connection.getTimeStamp();
  }

  public void setTimeout(long timeout) {
    m_connection.setTimeout(timeout);
  }

  public void addDsConnectionEventListener(DsConnectionEventListener listener) {
    m_connection.addDsConnectionEventListener(listener);
  }

  public void removeDsConnectionEventListener(DsConnectionEventListener listener) {
    m_connection.removeDsConnectionEventListener(listener);
  }

  public void addReference() {
    m_connection.addReference();
  }

  public void removeReference() {
    m_connection.removeReference();
  }

  public boolean shouldClose(long current_time) {
    return m_connection.shouldClose(current_time);
  }

  public void onDsInputStreamClosedEvent(DsInputStreamClosedEvent evt) {}

  public void onDsInputStreamErrorEvent(DsInputStreamErrorEvent evt) {}

  public void onDsInputStreamEvent(DsInputStreamEvent evt) {}

  /*
   * javadoc inherited
   */
  public DsSipConnectionEventInterface getEventInterface() {
    return null;
  }

  /*
   * javadoc inherited
   */
  public void setEventInterface(DsSipConnectionEventInterface eventInterface) {}

  /*
   * javadoc inherited
   */
  public boolean isPersistent() {
    return false;
  }

  /*
   * javadoc inherited
   */
  public Object getApplicationContext() {
    return null;
  }

  /*
   * javadoc inherited
   */
  public void setApplicationContext(Object context) {}

  /*
   * javadoc inherited
   */
  public void setConnectionOpt(
      boolean persistent,
      DsSipConnectionEventInterface callback,
      int inactivityTimer,
      Object cookie) {}

  /**
   * Checks if the stack is shutting down.
   *
   * @return <code>true</code> if the stack is shutting down and <code>false</code> for normal
   *     operation.
   */
  public boolean isShutingDown() {
    if (m_connection != null) {
      return m_connection.isShutingDown();
    }
    return false;
  }

  /**
   * Checks the DsConnection connecting state.
   *
   * @return it will return <code>true</code> if connection is in connecting state. <code>false
   *     </code> if not in connecting state.
   */
  public boolean isConnecting() {
    if (m_connection != null) {
      return m_connection.isConnecting();
    }
    return false;
  }

  /** Lock the DsConnection object. */
  public void lock() {
    if (m_connection != null) {
      m_connection.lock();
    }
    // Oops nothing to lock
  }

  /**
   * Acquires the lock if it is not held by another thread within the given waiting time and the
   * current thread has not been interrupted
   *
   * @param timeout waiting time in milliseconds.
   */
  public boolean trylock(long timeout) {
    if (m_connection != null) {
      return m_connection.trylock(timeout);
    }
    return false;
  }

  /** Unlock the DsConnection object. */
  public void unlock() {
    if (m_connection != null) {
      m_connection.unlock();
    }
  }

  /** changes the state to connected. */
  public void connected() {
    if (m_connection != null) {
      m_connection.connected();
    }
  }

  /** start the connection towards the destination. */
  public void initiateConnect() throws IOException, SocketException {
    if (m_connection != null) {
      m_connection.initiateConnect();
    }
  }
}
