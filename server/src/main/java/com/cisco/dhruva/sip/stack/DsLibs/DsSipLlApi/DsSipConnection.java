// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipMessage;
import java.io.IOException;

/**
 * This interface defines a SIP specific connection that is used to send data across the network
 * through the underlying socket
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

  void send(byte[] message) throws IOException;
}
