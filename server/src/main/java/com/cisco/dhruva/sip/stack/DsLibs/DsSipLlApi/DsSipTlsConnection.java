// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLContext;
import com.cisco.dhruva.transport.Connection;
import java.io.IOException;
import java.net.InetAddress;

/**
 * Defines a concrete SIP connection that is used to SIP messages across the network through the
 * underlying TLS socket.
 *
 * <p>This concrete connection can be constructed through the {@link DsSipConnectionFactory
 * DsSipConnectionFactory} by passing appropriate parameter like transport type and address.
 */

/*
TODO : Skeleton for now , has to be built properly

 */
public class DsSipTlsConnection extends DsTlsConnection implements DsSipConnection {

  protected DsSipTlsConnection(Connection connection, DsSSLContext context) {
    super(connection, context);
  }

  public final byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
    return _send(message, txn);
  }

  public final byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException {
    return _send(message, txn);
  }

  public final void send(byte[] message, DsSipServerTransaction txn) throws IOException {
    send(message, txn);
  }

  public final void send(byte[] message, DsSipClientTransaction txn) throws IOException {
    send(message, txn);
  }

  /**
   * Sends the specified SIP message across the network through the underlying TLS socket to the
   * desired destination. The message destination is specified in this connection's binding info.
   *
   * @param message the SIP message to send across
   * @return the sent message as byte array
   * @throws IOException if there is an I/O error while sending the message
   */
  public byte[] send(DsSipMessage message) throws IOException {
    return _send(message, null);
  }

  // consolidated send method
  private byte[] _send(DsSipMessage message, DsSipTransaction txn) throws IOException {
    message.updateBinding(bindingInfo);
    message.setTimestamp();

    updateTimeStamp();

    byte buffer[] = message.toByteArray();

    send(buffer);

    return buffer;
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
}
