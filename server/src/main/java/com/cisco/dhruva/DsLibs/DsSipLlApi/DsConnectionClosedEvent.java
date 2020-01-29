// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

/**
 * Represents the Connection closed event which is generated when a connection is closed either on
 * the server side or on the client side.
 */
public class DsConnectionClosedEvent extends DsConnectionEvent {

  /**
   * Constructs an DsConnectionClosedEvent object.
   *
   * @param source the source of this event
   */
  public DsConnectionClosedEvent(DsConnection source) {
    super(TYPE_CONNECTION_CLOSED, source);
  }
}
