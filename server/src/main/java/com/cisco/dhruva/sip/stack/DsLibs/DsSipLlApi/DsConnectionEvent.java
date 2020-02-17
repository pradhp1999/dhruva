// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

/** Represents the base connection event. */
public class DsConnectionEvent {

  /** Connection closed event type. */
  public static final int TYPE_CONNECTION_CLOSED = 0;
  /** Connection error event type. */
  public static final int TYPE_CONNECTION_ERROR = 1;
  /** Connection ICMP error event type. */
  public static final int TYPE_CONNECTION_ICMP_ERROR = 2;

  private DsConnection m_Source;
  private int m_Type;

  /**
   * Returns the type of the Connection Event.
   *
   * @return the type of the Connection Event
   */
  public int getType() {
    return m_Type;
  }

  /**
   * Returns the source of this event.
   *
   * @return the source of this event
   */
  public DsConnection getSource() {
    return m_Source;
  }

  /**
   * Constructs the DsConnectionEvent object with the specified source and type.
   *
   * @param type the connection Event Type (Close, Error)
   * @param source the source of the Connection Event
   */
  protected DsConnectionEvent(int type, DsConnection source) {
    m_Type = type;
    m_Source = source;
  }
}
