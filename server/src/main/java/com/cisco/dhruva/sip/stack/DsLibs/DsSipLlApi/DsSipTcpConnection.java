// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.transport.Connection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

public class DsSipTcpConnection extends DsTcpConnection implements DsSipConnection {
  /**
   * Constructs a Sip aware TCP connection based on the specified binding info <code>binding</code>.
   *
   * @param connection the binding info that contains the remote address and port number where to
   *     make connection to.
   * @throws IOException if the underlying socket throws this exception
   * @throws SocketException if there is an error while constructing the TCP socket
   */
  protected DsSipTcpConnection(Connection connection) {
    super(connection);
  }

  public final byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
    return _send(message, txn);
  }

  public final byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException {
    return _send(message, txn);
  }

  public final void send(byte[] message, DsSipServerTransaction txn) throws IOException {
    send(message);
  }

  public final void send(byte[] message, DsSipClientTransaction txn) throws IOException {
    send(message);
  }

  // consolidated send method
  private byte[] _send(DsSipMessage message, DsSipTransaction txn) throws IOException {
    message.updateBinding(bindingInfo);
    message.setTimestamp();
    byte buffer[] = message.toByteArray();
    send(buffer);
    return buffer;
  }

  /**
   * Sends the specified SIP message across the network through the underlying TCP socket to the
   * desired destination. The message destination is specified in this connection's binding info.
   *
   * @param message the SIP message to send across
   * @return the sent message as byte array
   * @throws IOException if there is an I/O error while sending the message
   */
  public byte[] send(DsSipMessage message) throws IOException {
    return _send(message, null);
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info
    send(message, txn);
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info
    send(message, txn);
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info
    return send(message, txn);
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    // TODO: throw exn if port and addr don't match binding info
    return send(message, txn);
  }
} // Ends class DsSipTcpConnection
