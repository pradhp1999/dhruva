// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;

/** Represents an error event in an Input Stream. */
public class DsInputStreamErrorEvent extends DsInputStreamEvent {
  /** The exception. */
  private Exception m_Exc;

  /**
   * Constructs the DsInputStreamErrorEvent object with the specified source and exception.
   *
   * @param source the source of this event
   * @param exc the error exception
   */
  public DsInputStreamErrorEvent(DsMessageReader source, Exception exc) {
    super(source, TYPE_STREAM_ERROR);
    m_Exc = exc;
  }

  /**
   * Returns the exception value.
   *
   * @return the exception value
   */
  public Exception getException() {
    return m_Exc;
  }
}
