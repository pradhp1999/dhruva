// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMulticastSocket;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import java.io.IOException;
import java.net.InetAddress;
import org.apache.logging.log4j.Level;

/**
 * Defines a concrete SIP connection that is used to send SIP messages across the network through
 * the underlying UDP multicast datagram socket. This concrete connection can be constructed through
 * the {@link DsSipConnectionFactory DsSipConnectionFactory} by passing appropriate parameter like
 * transport type and address.
 */
public class DsSipMulticastConnection extends DsMulticastConnection implements DsSipConnection {
  // private static DsSipContentLengthHeader  ZeroContentLength = new DsSipContentLengthHeader(0);

  /**
   * Constructs a SIP aware UDP multicast connection based on the specified binding info.
   *
   * @param binding the binding info that contains the remote address an port number where to make
   *     connection to.
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsSipMulticastConnection(DsBindingInfo binding) throws IOException {
    super(binding);
  }

  /**
   * Constructs a SIP aware UDP multicast connection to the specified remote address <code>
   * anInetAddress</code> and the remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsSipMulticastConnection(InetAddress anInetAddress, int aPortNo) throws IOException {
    super(anInetAddress, aPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a SIP aware UDP multicast connection to the specified remote address <code>
   * anInetAddress</code> and the remote port number <code>aPortNo</code>.
   *
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsSipMulticastConnection(InetAddress anInetAddress, int aPortNo, DsNetwork network)
      throws IOException {
    super(anInetAddress, aPortNo, network);
  }

  /**
   * Constructs a SIP aware UDP multicast connection to the specified remote address <code>
   * anInetAddress</code> and the remote port number <code>aPortNo</code>. It also binds the
   * datagram socket locally to the specified local address <code>lInetAddress</code> and local port
   * number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsSipMulticastConnection(
      InetAddress lInetAddress, int lPort, InetAddress anInetAddress, int aPortNo)
      throws IOException {
    super(lInetAddress, lPort, anInetAddress, aPortNo, DsNetwork.getDefault());
  }

  /**
   * Constructs a SIP aware UDP multicast connection to the specified remote address <code>
   * anInetAddress</code> and the remote port number <code>aPortNo</code>. It also binds the
   * datagram socket locally to the specified local address <code>lInetAddress</code> and local port
   * number <code>lPort</code>.
   *
   * @param lInetAddress the address to bind to locally
   * @param lPort the port to bind to locally
   * @param anInetAddress the remote address to connect to
   * @param aPortNo the remote port number to connect to
   * @param network The network with which this connection is associated.
   * @throws IOException if there is an error while constructing the datagram socket
   */
  protected DsSipMulticastConnection(
      InetAddress lInetAddress,
      int lPort,
      InetAddress anInetAddress,
      int aPortNo,
      DsNetwork network)
      throws IOException {
    super(lInetAddress, lPort, anInetAddress, aPortNo, network);
  }

  /**
   * Constructs a SIP aware UDP multicast connection based on the specified datagram socket.
   *
   * @param socket a DsMulticastSocket object
   */
  protected DsSipMulticastConnection(DsMulticastSocket socket) {
    super(socket);
  }

  public final byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
    // TODO
    return send(message);
  }

  public final byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException {
    // TODO
    return send(message);
  }

  public final void send(byte[] message, DsSipServerTransaction txn) throws IOException {
    // TODO
    send(message);
  }

  public final void send(byte[] message, DsSipClientTransaction txn) throws IOException {
    // TODO
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

    if (DsLog4j.connectionCat.isEnabled(Level.DEBUG))
      DsLog4j.connectionCat.log(
          Level.DEBUG,
          new StringBuffer("Sending Message to address: ")
              .append(m_bindingInfo.getRemoteAddress().getHostAddress())
              .append(" on port ")
              .append(m_bindingInfo.getRemotePort())
              .append(DsSipTransportType.getTypeAsUCString(m_bindingInfo.getTransport()))
              .append('\n')
              .append(message)
              .toString());

    message.updateBinding(m_bindingInfo);

    /*
    // if the message doesn't have a content length, supply one
    DsSipContentLengthHeader contentLength =
    (DsSipContentLengthHeader)message.getHeader (DsSipConstants.CONTENT_LENGTH);
    if (contentLength == null)
    {
        if (message.getBody() == null)
            message.addHeader(ZeroContentLength, false, false);
        else
            message.addHeader(new DsSipContentLengthHeader(message.getBodyLength()),
                    false, false);
    }
    */
    message.setTimestamp();
    byte buffer[] = message.toByteArray();
    send(buffer);
    return buffer;
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
} // Ends Class
