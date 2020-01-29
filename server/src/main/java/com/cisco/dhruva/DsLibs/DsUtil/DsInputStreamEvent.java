// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import com.cisco.dhruva.DsLibs.DsSipLlApi.*;

/**
 * Base class for input stream events. The base class is used to deliver the most common event: data
 * present.
 */
public class DsInputStreamEvent {
  /** The type of event. */
  private int m_Type;
  /** The source of this event. */
  private DsMessageReader m_Source;
  /** The observer source of this event. */
  private Object m_objectSource;

  /** Integer representation of a stream closed event. */
  public static final int TYPE_STREAM_CLOSED = 0;
  /** Integer representation of a stream error event. */
  public static final int TYPE_STREAM_ERROR = 1;
  /** Integer representation of a stream data event. */
  public static final int TYPE_STREAM_DATA = 2;

  /**
   * Returns the source of this event.
   *
   * @return the source of this event
   */
  public DsMessageReader getReaderSource() {
    return m_Source;
  }

  /**
   * Returns the observer source of this event.
   *
   * @return the observer source of this event
   */
  public Object getSource() {
    return m_objectSource;
  }

  /**
   * Returns the type of event.
   *
   * @return the type of event
   */
  public int getType() {
    return m_Type;
  }

  /**
   * Constructs DsInputStreamEvent object with the specified source and type.
   *
   * @param source the source of this event
   * @param type the type of event
   */
  protected DsInputStreamEvent(DsMessageReader source, int type) {
    m_Source = source;
    m_Type = type;
  }

  /**
   * Constructs DsInputStreamEvent object with the specified observer source and type.
   *
   * @param source the observer source of this event
   * @param type the type of event
   */
  protected DsInputStreamEvent(Object source, int type) {
    m_objectSource = source;
    m_Type = type;
  }

  /**
   * Constructs DsInputStreamEvent object with the specified source and the type.
   *
   * @param source the source of this event
   */
  public DsInputStreamEvent(DsMessageReader source) {
    this(source, DsInputStreamEvent.TYPE_STREAM_DATA);
  }
}
