// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.*;

/** Represents an error event in an Input Stream. */
public class DsInputStreamErrorEvent extends DsInputStreamEvent {
  /** The exception. */
  private Exception m_Exc;

  /**
   * Constructs DsInputStreamEvent object with the specified observer source and type.
   *
   * @param source the observer source of this event
   * @param type the type of event
   */
  protected DsInputStreamErrorEvent(Object source, int type) {
    super(source, type);
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
