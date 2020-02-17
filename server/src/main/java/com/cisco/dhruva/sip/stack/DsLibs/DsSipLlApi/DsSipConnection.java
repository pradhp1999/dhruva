// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import java.io.IOException;
import java.net.InetAddress;

/**
 * This interface defines a SIP specific connection that is used to send data across the network
 * through the underlying socket. A concrete connection can be constructed through the {@link
 * DsSipConnectionFactory DsSipConnectionFactory} by passing appropriate parameter like transport
 * type and address.
 */
public interface DsSipConnection extends DsConnection {
  /**
   * Sends the specified SIP message. The message destination is specified in this connection's
   * binding info.
   *
   * @param message the message to send.
   * @return the sent message as a byte array.
   * @throws IOException if there is an I/O error while sending the message.
   */
  byte[] send(DsSipMessage message) throws IOException;

  /**
   * Sends the specified SIP message. The message destination is specified in this connection's
   * binding info.
   *
   * @param message the message to send.
   * @param txn the transaction to notify if there is a transport exception sending the message
   * @return the sent message as a byte array.
   * @throws IOException if there is an I/O error while sending the message.
   */
  byte[] send(DsSipMessage message, DsSipServerTransaction txn) throws IOException;

  /**
   * Sends the specified SIP message. The message destination is specified in this connection's
   * binding info.
   *
   * @param message the message to send.
   * @param txn the transaction to notify if there is a transport exception sending the message
   * @return the sent message as a byte array.
   * @throws IOException if there is an I/O error while sending the message.
   */
  byte[] send(DsSipMessage message, DsSipClientTransaction txn) throws IOException;

  /**
   * Sends the specified bytes. The message destination is specified in this connection's binding
   * info.
   *
   * @param message the message to send.
   * @param txn the transaction to notify if there is a transport exception sending the message
   * @throws IOException if there is an I/O error while sending the message.
   */
  void send(byte[] message, DsSipServerTransaction txn) throws IOException;

  /**
   * Sends the specified bytes. The message destination is specified in this connection's binding
   * info.
   *
   * @param message the message to send.
   * @param txn the transaction to notify if there is a transport exception sending the message
   * @throws IOException if there is an I/O error while sending the message.
   */
  void send(byte[] message, DsSipClientTransaction txn) throws IOException;

  /**
   * Sends the specified bytes.
   *
   * @param message the message to send.
   * @param addr the destination address.
   * @param port the destination port.
   * @param txn the transaction to notify if there is a transport exception sending the message
   * @throws IOException if there is an I/O error while sending the message.
   */
  void sendTo(byte[] message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException;

  /**
   * Sends the specified bytes.
   *
   * @param message the message to send.
   * @param addr the destination address.
   * @param port the destination port.
   * @param txn the transaction to notify if there is a transport exception sending the message
   * @throws IOException if there is an I/O error while sending the message.
   */
  void sendTo(byte[] message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException;

  /**
   * Sends the specified message.
   *
   * @param message the message to send.
   * @param addr the destination address.
   * @param port the destination port.
   * @param txn the transaction to notify if there is a transport exception sending the message
   * @throws IOException if there is an I/O error while sending the message.
   * @return the sent message as a byte array.
   */
  byte[] sendTo(DsSipMessage message, InetAddress addr, int port, DsSipServerTransaction txn)
      throws IOException;

  /**
   * Sends the specified message.
   *
   * @param message the message to send.
   * @param addr the destination address.
   * @param port the destination port.
   * @param txn the transaction to notify if there is a transport exception sending the message
   * @throws IOException if there is an I/O error while sending the message.
   * @return the sent message as a byte array.
   */
  byte[] sendTo(DsSipMessage message, InetAddress addr, int port, DsSipClientTransaction txn)
      throws IOException;
}
