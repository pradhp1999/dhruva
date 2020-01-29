// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

/**
 * Represents the Connection error event which is generated when an ICMP error occurs on an existing
 * connection either on the server side or on the client side.
 */
public class DsConnectionIcmpErrorEvent extends DsConnectionErrorEvent {
  /**
   * Constructs the DsConnectionIcmpErrorEvent object.
   *
   * @param source the source of the event
   * @param exc the exception that resulted in this event
   */
  public DsConnectionIcmpErrorEvent(DsConnection source, Exception exc) {
    super(TYPE_CONNECTION_ICMP_ERROR, source, exc);
  }
}
