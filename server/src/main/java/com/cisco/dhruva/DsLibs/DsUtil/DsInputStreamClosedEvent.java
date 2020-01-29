// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import com.cisco.dhruva.DsLibs.DsSipLlApi.*;
import java.io.*;

/** Represents the Input Stream Closed event. */
public class DsInputStreamClosedEvent extends DsInputStreamEvent {
  /**
   * Constructs the DsInputStreamClosedEvent object with the specified source.
   *
   * @param source the source of this event
   */
  public DsInputStreamClosedEvent(DsMessageReader source) {
    super(source, TYPE_STREAM_CLOSED);
  }
}
