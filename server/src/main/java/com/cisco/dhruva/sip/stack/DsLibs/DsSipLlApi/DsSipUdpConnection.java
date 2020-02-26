// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDatagramSocket;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import org.apache.logging.log4j.Level;

/**
 * Defines a concrete SIP connection that is used to send SIP messages across the network through
 * the underlying UDP datagram socket. This concrete connection can be constructed through the
 * {@link DsSipConnectionFactory DsSipConnectionFactory} by passing appropriate parameter like
 * transport type and address.
 */
public class DsSipUdpConnection extends DsUdpConnection implements DsSipConnection {

  // thread local container for Hashmaps for ip to connection mapping
  private static ThreadLocal tlConnMaps = new ThreadLocal();

  /**
   * Get this thread's Hashmap that contains the udp connection objects associated with this thread.
   * This method will create one if one doesn't exist.
   */
  private static HashMap getThreadLocalMap() {
    HashMap conns = (HashMap) tlConnMaps.get();
    if (conns == null) {
      conns = new HashMap();
      tlConnMaps.set(conns);
    }
    return conns;
  }

  /**
   * Gets a DsSipUdpConnection associated with the current thread based upon the network, addresses
   * and ports specified.
   *
   * @param laddr the local address to use to look up and the address that will be used for
   *     address/connection mapping
   * @param lport the local port to use during creation of a new udp connection
   * @param raddr the remote address with which to associate a new connection
   * @param rport the remote port with which to associate a new connection
   * @throws SocektException if problems occur during socket creation
   */
  private static DsSipUdpConnection getConnectionFromTLMap(
      DsNetwork network, InetAddress laddr, int lport, InetAddress raddr, int rport)
      throws SocketException {
    HashMap map = getThreadLocalMap();
    DsSipUdpConnection conn = null;
    InetAddress resolvedAddr = null;

    if (network == null) {
      network = DsNetwork.getDefault();
    }

    if (laddr != null) {
      // look up the connection and return it if it is in the map
      conn = (DsSipUdpConnection) map.get(laddr);
      if (conn != null) {
        return conn;
      } else {
        resolvedAddr = laddr;
      }
    } else {
      // see if there is already an entry for a null address
      conn = (DsSipUdpConnection) map.get(laddr);
      if (conn != null) {
        return conn;
      }
    }

    if (resolvedAddr == null) {
      // get the network associated listener
      DsUdpListener listener = network.getUdpListener();
      if (listener != null) {
        if (listener.m_address != null) {
          // now check to see if the listener address is in the map
          conn = (DsSipUdpConnection) map.get(listener.m_address);
          if (conn != null) {
            return conn;
          } else {
            resolvedAddr = listener.m_address;
          }
        }
      }
    }
    // get a connection based upon the resolved address and given port
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "In thread: "
              + Thread.currentThread()
              + ". Creating Thread Local DsSipUdpConnection"
              + " object using network("
              + network
              + ") local address("
              + resolvedAddr
              + ") local port("
              + lport
              + ") remote address("
              + raddr
              + ") remote port("
              + rport
              + ")");
    }
    // we HAVE to use the constructor taking both local and remote ports and addresses
    // here because the constructor only taking one pair assumes it is the remote one.
    conn = new DsSipUdpConnection(network, resolvedAddr, lport, raddr, rport);

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "In thread: "
              + Thread.currentThread()
              + "\nPutting addr("
              + resolvedAddr
              + ") for connection:\n"
              + conn);
    }
    map.put(resolvedAddr, conn);
    return conn;
  }

  /**
   * Removes the connection object from the thread local Hashmap.
   *
   * @param addr the address that maps to the connection object for this thread that is to be
   *     removed
   */
  static final void clearThreadLocal(InetAddress addr) {
    getThreadLocalMap().remove(addr);
  }

  /**
   * Gets the udp connection object matching the given binding info from this thread's local
   * Hashmap. A connection will be created and added to the Hashmap if one is not already associated
   * with the local address in the given binding info. The network and local and remote addresses
   * and ports will be set in the connection object's binding info after retrieval or creation.
   *
   * @param binding the binding info to match when looking up a connection or to be used when one is
   *     not found
   * @throws SocketException if problems occur during connection object creation
   */
  static final DsSipUdpConnection getThreadLocal(DsBindingInfo binding) throws SocketException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "In getThreadLocal(DsBindingInfo)");
    }

    DsSipUdpConnection conn =
        getConnectionFromTLMap(
            binding.getNetwork(),
            binding.getLocalAddress(),
            binding.getLocalPort(),
            binding.getRemoteAddress(),
            binding.getRemotePort());
    DsBindingInfo info = conn.getBindingInfo();
    info.setRemoteAddress(binding.getRemoteAddress());
    info.setRemotePort(binding.getRemotePort());
    info.setLocalAddress(binding.getLocalAddress());
    info.setLocalPort(binding.getLocalPort());
    info.setNetwork(binding.getNetwork());
    return conn;
  }

  /**
   * Gets the udp connection object matching the given network object's listener's local address
   * from this thread's local Hashmap. A connection will be created and added to the Hashmap if one
   * is not already associated with that local address. The network and remote address and port will
   * be set in the connection object's binding info after retrieval or creation. The network
   * object's associated listener's local addresss is used during connection creation since no local
   * address is presented in this method's signature.
   *
   * @param network the network to use during connection creation
   * @param addr the remote address to set in the connection's binding info
   * @param port the remote port to set in the connection's bindinfo
   * @throws SocketException if problems occur during connection object creation
   */
  static final DsSipUdpConnection getThreadLocal(DsNetwork network, InetAddress addr, int port)
      throws SocketException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(Level.DEBUG, "In getThreadLocal(DsNetwork, InetAddress, int)");
    }

    DsSipUdpConnection conn =
        getConnectionFromTLMap(network, null, DsBindingInfo.LOCAL_PORT_UNSPECIFIED, addr, port);
    DsBindingInfo info = conn.getBindingInfo();
    info.setRemoteAddress(addr);
    info.setRemotePort(port);
    info.setNetwork(network);
    return conn;
  }

  /**
   * Gets the udp connection object matching the given local address from this thread's local
   * Hashmap. A connection will be created and added to the Hashmap if one is not already associated
   * with that local address. The network and local and remote addresses and ports will be set in
   * the connection object's binding info after retrieval or creation.
   *
   * @param network the network to use during connection creation
   * @param laddr the local address to set in the connection's binding info
   * @param lport the local port to set in the connection's bindinfo
   * @param raddr the remote address to set in the connection's binding info
   * @param rport the remote port to set in the connection's bindinfo
   * @throws SocketException if problems occur during connection object creation
   */
  static final DsSipUdpConnection getThreadLocal(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port)
      throws SocketException {
    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          "In getThreadLocal(DsNetwork, InetAddress local, int lport, InetAddress remote , int rport)");
    }

    DsSipUdpConnection conn = getConnectionFromTLMap(network, laddr, lport, addr, port);
    DsBindingInfo info = conn.getBindingInfo();
    info.setRemoteAddress(addr);
    info.setRemotePort(port);
    info.setLocalAddress(laddr);
    info.setLocalPort(lport);
    info.setNetwork(network);
    return conn;
  }

  /**
   * Constructs a SIP aware UDP connection based on the specified binding info.
   *
   * @param binding the binding info that contains the remote address an port number where to make
   *     connection to.
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsSipUdpConnection(DsBindingInfo binding) throws SocketException {
    super(binding);
  }

  /**
   * Constructs a SIP aware UDP connection to the specified remote address <code>addr</code> and the
   * remote port number <code>port</code>.
   *
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsSipUdpConnection(InetAddress addr, int port) throws SocketException {
    super(addr, port);
  }

  /**
   * Constructs a SIP aware UDP connection to the specified remote address <code>addr</code> and the
   * remote port number <code>port</code>. It also binds the datagram socket locally to the
   * specified local address <code>laddr</code> and local port number <code>lport</code>.
   *
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsSipUdpConnection(InetAddress laddr, int lport, InetAddress addr, int port)
      throws SocketException {
    super(laddr, lport, addr, port);
  }

  /**
   * Constructs a SIP aware UDP connection to the specified remote address <code>addr</code> and the
   * remote port number <code>port</code>.
   *
   * @param network the network to associate with this connection
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsSipUdpConnection(DsNetwork network, InetAddress addr, int port)
      throws SocketException {
    super(network, addr, port);
  }

  /**
   * Constructs a SIP aware UDP connection to the specified remote address <code>addr</code> and the
   * remote port number <code>port</code>. It also binds the datagram socket locally to the
   * specified local address <code>laddr</code> and local port number <code>lport</code>.
   *
   * @param network the network to associate with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsSipUdpConnection(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port)
      throws SocketException {
    super(network, laddr, lport, addr, port);
  }

  /**
   * Constructs a SIP aware UDP connection to the specified remote address <code>addr</code> and the
   * remote port number <code>port</code>. It also binds the datagram socket locally to the
   * specified local address <code>laddr</code> and local port number <code>lport</code>.
   *
   * @param network the network to associate with this connection
   * @param laddr the address to bind to locally
   * @param lport the port to bind to locally
   * @param addr the remote address to connect to
   * @param port the remote port number to connect to
   * @param doConnect <code>true</code> to connect to the destination
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsSipUdpConnection(
      DsNetwork network,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port,
      boolean doConnect)
      throws SocketException {
    super(network, laddr, lport, addr, port, doConnect);
  }

  /**
   * Constructs a SIP aware UDP connection based on the specified datagram socket.
   *
   * @param socket a DsDatagramSocket object
   */
  protected DsSipUdpConnection(DsDatagramSocket socket) {
    super(socket);
  }

  /**
   * Constructs a SIP aware UDP connection based on the specified datagram socket.
   *
   * @param network the network to associate with this connection
   * @param socket a DsDatagramSocket object
   */
  protected DsSipUdpConnection(DsNetwork network, DsDatagramSocket socket) {
    super(socket);
    m_bindingInfo.setNetwork(network);
  }

  public final byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    return send(message);
  }

  public final byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    return send(message);
  }

  public final void send(byte[] message, DsSipServerTransaction txn) throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    send(message);
  }

  public final void send(byte[] message, DsSipClientTransaction txn) throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    send(message);
  }

  /**
   * Sends the specified SIP message across the network through the underlying datagram socket to
   * the desired destination. The message destination is specified in this connection's binding
   * info.
   *
   * @param message the SIP message to send across
   * @return the sent message as byte array
   * @throws IOException if there is an I/O error while sending the message
   */
  public byte[] send(DsSipMessage message) throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.log(
          Level.INFO,
          new StringBuffer("Sending Message to address: ")
              .append(m_bindingInfo.getRemoteAddress())
              .append(" port ")
              .append(m_bindingInfo.getRemotePort())
              .append(DsSipTransportType.getTypeAsUCString(m_bindingInfo.getTransport()))
              .append('\n')
              .append(message)
              .toString());
    }

    byte buffer[];

    message.setTimestamp();
    buffer = message.toByteArray();

    sendTo(buffer, m_bindingInfo.getRemoteAddress(), m_bindingInfo.getRemotePort());

    message.updateBinding(m_bindingInfo);

    return buffer;
  }

  public void closeSocket() throws IOException {
    if (DsLog4j.connectionCat.isEnabled(Level.INFO)) {
      DsLog4j.connectionCat.log(Level.INFO, "closeSocket on " + this.toString());
    }

    if (isThreadLocal()) {
      InetAddress addr = m_bindingInfo.getLocalAddress();
      if (addr != null) {
        if (DsLog4j.connectionCat.isEnabled(Level.DEBUG)) {
          DsLog4j.connectionCat.log(
              Level.DEBUG, "Removing thread local connection from map(" + addr + ")");
        }
        clearThreadLocal(addr);
      }
    }
    super.closeSocket();
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    message.setTimestamp();
    // TODO  -- deal case if there is a queue here like in TCP version
    byte[] buffer = message.toByteArray();
    sendTo(buffer, addr, port);
    return buffer;
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    message.setTimestamp();
    // TODO  -- deal case if there is a queue here like in TCP version
    byte[] buffer = message.toByteArray();
    sendTo(buffer, addr, port);
    return buffer;
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    sendTo(message, addr, port);
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    sendTo(message, addr, port);
  }
}
