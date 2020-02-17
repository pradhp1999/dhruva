// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * This is the abstraction that allows threads belonging to a DsWorkQueue to know how to process a
 * unit of work.
 */
public interface DsUnitOfWork extends Runnable {
  /** How to process the unit of work. */
  public void process();

  /** How to abort the unit of work. */
  public void abort();
}
