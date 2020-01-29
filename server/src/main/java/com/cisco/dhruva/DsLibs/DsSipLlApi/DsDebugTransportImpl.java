// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import java.net.*;

/**
 * For dynamicsoft use only. The application code may register here to receive transport debug data.
 */
public final class DsDebugTransportImpl {
  /** The singleton. */
  static DsDebugTransport impl;

  /**
   * Check to see if a debug transport listener is set.
   *
   * @return <code>true</code> if a debug transport listener is set, otherwise returns <code> false
   *     </code>.
   */
  public static boolean set() {
    return impl != null;
  }

  /**
   * Set a debug transport listener.
   *
   * @param listener the debug transport listener.
   */
  public static void set(DsDebugTransport listener) {
    impl = listener;
  }

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
  public static void messageIn(
      int pos,
      int transport,
      byte[] message,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port) {
    if (impl == null) return;
    impl.messageIn(pos, transport, message, laddr, lport, addr, port);
  }

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
  public static void messageOut(
      int pos,
      int transport,
      byte[] message,
      InetAddress laddr,
      int lport,
      InetAddress addr,
      int port) {
    if (impl == null) return;
    impl.messageOut(pos, transport, message, laddr, lport, addr, port);
  }
}
