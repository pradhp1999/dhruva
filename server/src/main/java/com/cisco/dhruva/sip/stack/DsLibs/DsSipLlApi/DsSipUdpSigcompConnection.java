// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsDatagramSocket;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSigcompNackException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import org.apache.logging.log4j.Level;

/** For dynamicsoft use only. A compressing connection. */
class DsSipUdpSigcompConnection extends DsUdpSigcompConnection implements DsSipConnection {
  static final void clearThreadLocal() throws SocketException {
    tlConnection.set(null);
  }

  static final DsSipUdpSigcompConnection getThreadLocal(DsBindingInfo binding)
      throws SocketException {
    DsSipUdpSigcompConnection c = (DsSipUdpSigcompConnection) tlConnection.get();
    if (c == null) {
      c = new DsSipUdpSigcompConnection(binding);
      c.receiveFeedback();
      tlConnection.set(c);
    }

    // update the binding info of the connection
    //    to reflect the address  that was resolved
    //    from the SIP message
    DsBindingInfo info = c.getBindingInfo();
    info.setRemoteAddress(binding.getRemoteAddress());
    info.setRemotePort(binding.getRemotePort());
    return c;
  }

  static final DsSipUdpSigcompConnection getThreadLocal(
      DsNetwork network, InetAddress addr, int port) throws SocketException {
    DsSipUdpSigcompConnection c = (DsSipUdpSigcompConnection) tlConnection.get();
    if (c == null) {
      c = new DsSipUdpSigcompConnection(network, addr, port);
      c.receiveFeedback();
      tlConnection.set(c);
    }
    // update the binding info of the connection
    //    to reflect the address  that was resolved
    //    from the SIP message
    DsBindingInfo info = c.getBindingInfo();
    info.setRemoteAddress(addr);
    info.setRemotePort(port);
    return c;
  }

  static final DsSipUdpSigcompConnection getThreadLocal(
      DsNetwork network, InetAddress laddr, int lport, InetAddress addr, int port)
      throws SocketException {
    DsSipUdpSigcompConnection c = (DsSipUdpSigcompConnection) tlConnection.get();
    if (c == null) {
      c = new DsSipUdpSigcompConnection(network, laddr, lport, addr, port);
      c.receiveFeedback();
      tlConnection.set(c);
    }
    // update the binding info of the connection
    //    to reflect the address  that was resolved
    //    from the SIP message
    DsBindingInfo info = c.getBindingInfo();
    info.setRemoteAddress(addr);
    info.setRemotePort(port);
    return c;
  }

  private static ThreadLocal tlConnection = new ThreadLocal();

  /**
   * Constructs a SIP aware UDP connection based on the specified binding info.
   *
   * @param binding the binding info that contains the remote address an port number where to make
   *     connection to.
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsSipUdpSigcompConnection(DsBindingInfo binding) throws SocketException {
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
  protected DsSipUdpSigcompConnection(InetAddress addr, int port) throws SocketException {
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
  protected DsSipUdpSigcompConnection(InetAddress laddr, int lport, InetAddress addr, int port)
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
  protected DsSipUdpSigcompConnection(DsNetwork network, InetAddress addr, int port)
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
  protected DsSipUdpSigcompConnection(
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
   * @param doConnect <code>true</code> to connect the destination
   * @throws SocketException if there is an error while constructing the datagram socket
   */
  protected DsSipUdpSigcompConnection(
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
  protected DsSipUdpSigcompConnection(DsDatagramSocket socket) {
    super(socket);
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
              .append(m_bindingInfo.getRemoteAddress().getHostAddress())
              .append(" on port ")
              .append(m_bindingInfo.getRemotePort())
              .append(DsSipTransportType.getTypeAsUCString(m_bindingInfo.getTransport()))
              .append('\n')
              .append(message)
              .toString());
    }

    message.setTimestamp();
    byte[] buffer = null;
    if (message.shouldCompress()) {
      buffer = message.toByteArray();
      buffer =
          sendToCompressed(buffer, m_bindingInfo.getRemoteAddress(), m_bindingInfo.getRemotePort());
      message.setCompressedData(buffer);
    } else {
      buffer = message.toByteArray();
      try {
        sendTo(buffer, m_bindingInfo.getRemoteAddress(), m_bindingInfo.getRemotePort());
      } catch (DsSigcompNackException ne) {
        // this only gets thrown if a message is compressed and a NACK
        //   has been received on the compartment
        DsLog4j.connectionCat.log(
            Level.INFO, "caught nack exception, clearing compressed bytes and resending");

        // setting finalised to false will cause recursive call to enter the shouldCompress == true
        //    block
        message.setFinalised(false);
        send(message);

        //  reset
        message.setFinalised(true);
      }
    }

    message.updateBinding(m_bindingInfo);

    return buffer;
  }

  public void closeSocket() throws IOException {
    if (isThreadLocal()) clearThreadLocal();
    super.closeSocket();
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    byte[] buffer = null;
    message.setTimestamp();
    if (message.shouldCompress()) {
      buffer = message.toByteArray();
      buffer = sendToCompressed(buffer, addr, port);
      message.setCompressedData(buffer);
    } else {
      buffer = message.toByteArray();
      try {
        sendTo(buffer, addr, port);
      } catch (DsSigcompNackException ne) {
        DsLog4j.connectionCat.log(
            Level.INFO, "caught nack exception, clearing compressed bytes and resending");
        message.setFinalised(false);
        sendTo(message, addr, port, txn);
        message.setFinalised(true);
      }
    }
    return buffer;
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    byte[] buffer = null;
    message.setTimestamp();
    if (message.shouldCompress()) {
      buffer = message.toByteArray();
      buffer = sendToCompressed(buffer, addr, port);
      message.setCompressedData(buffer);
    } else {
      buffer = message.toByteArray();
      try {
        sendTo(buffer, addr, port);
      } catch (DsSigcompNackException ne) {
        DsLog4j.connectionCat.log(
            Level.INFO, "caught nack exception, clearing compressed bytes and resending");
        message.setFinalised(false);
        sendTo(message, addr, port, txn);
        message.setFinalised(true);
      }
    }
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
