// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

/**
 * Represents the Connection error event which is generated when an error occurs on an existing
 * connection either on the server side or on the client side.
 */
public class DsConnectionErrorEvent extends DsConnectionEvent {
  /**
   * Constructs the DsConnectionErrorEvent object.
   *
   * @param source the source of the event
   * @param exc the exception that resulted in this event
   */
  public DsConnectionErrorEvent(DsConnection source, Exception exc) {
    super(TYPE_CONNECTION_ERROR, source);
    m_Exc = exc;
  }

  /**
   * Constructs the DsConnectionErrorEvent object.
   *
   * @param type the connection event type
   * @param source the source of the event
   * @param exc the exception that resulted in this event
   */
  protected DsConnectionErrorEvent(int type, DsConnection source, Exception exc) {
    super(type, source);
    m_Exc = exc;
  }
  /**
   * Get the exception that resulted in this event.
   *
   * @return the error that resulted in this event
   */
  public Exception getException() {
    return m_Exc;
  }

  private Exception m_Exc;
}
