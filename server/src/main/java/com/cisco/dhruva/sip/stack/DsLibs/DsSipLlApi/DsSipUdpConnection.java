// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.transport.Connection;
import java.io.IOException;
import java.net.InetAddress;
import org.slf4j.event.Level;

public class DsSipUdpConnection extends DsUdpConnection implements DsSipConnection {

  protected DsSipUdpConnection(Connection connection) {
    super(connection);
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
      DsLog4j.connectionCat.info("Trying to send message to address {} \n {}",bindingInfo,message);
    }

    byte buffer[];
    message.setTimestamp();
    buffer = message.toByteArray();
    super.send(buffer);
    message.updateBinding(bindingInfo);

    return buffer;
  }

  public final byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException {
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

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    message.setTimestamp();
    // TODO  -- deal case if there is a queue here like in TCP version
    return send(message);
  }

  public final byte[] sendTo(
      DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    message.setTimestamp();
    return send(message);
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    super.send(message);
  }

  public final void sendTo(byte[] message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException {
    // TODO  -- deal case if there is a queue here like in TCP version
    super.send(message);
  }
}
