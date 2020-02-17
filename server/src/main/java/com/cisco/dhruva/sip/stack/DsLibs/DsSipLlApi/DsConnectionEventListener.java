// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

/** This interface provides handlers for the connection close event and connection error event. */
public interface DsConnectionEventListener {
  /**
   * Handle a connection close event.
   *
   * @param evt event to handle
   */
  void onDsConnectionClosedEvent(DsConnectionClosedEvent evt);

  /**
   * Handle a connection error event.
   *
   * @param evt event to handle
   */
  void onDsConnectionErrorEvent(DsConnectionErrorEvent evt);

  /**
   * Handle a connection ICMP error event in case of UDP.
   *
   * @param evt event to handle
   */
  void onDsConnectionIcmpErrorEvent(DsConnectionIcmpErrorEvent evt);
}
