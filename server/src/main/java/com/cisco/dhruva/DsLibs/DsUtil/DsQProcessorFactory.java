// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/** A factory to create DsQProcessors. */
public class DsQProcessorFactory {
  /**
   * The factory method for creating DsQProcessors.
   *
   * @param work the queue to look for work on
   * @return the newly created queue processor object
   */
  public DsQProcessor makeProcessor(DsWorkQueue work) {
    return new DsQProcessor(work);
  }
}
