// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.transport.Transport;
import java.net.*;

/** For dynamicsoft use only. An interface used to debug message flow withing the stack. */
public interface DsDebugTransport {
  /** Position: message in the listener. */
  public static final int POS_LISTENER = 0;
  /** Position: mesage in the DATAI worker. */
  public static final int POS_DATAIWORKER = 1;
  /** Position: mesage in the Connection. */
  public static final int POS_CONNECTION = 2;

  /**
   * Called when a message is received.
   *
   * @param pos the position.
   * @param transport the transport.
   * @param message the message.
   * @param laddr the local address.
   * @param lport the local port.
   * @param addr the remote address.
   * @param port the remote port.
   */
  void messageIn(
      int pos,
      Transport transport,
      byte[] message,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port);

  /**
   * Called when a message is sent.
   *
   * @param pos the position.
   * @param transport the transport.
   * @param message the message.
   * @param laddr the local address.
   * @param lport the local port.
   * @param addr the remote address.
   * @param port the remote port.
   */
  void messageOut(
      int pos,
      int transport,
      byte[] message,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port);
}
