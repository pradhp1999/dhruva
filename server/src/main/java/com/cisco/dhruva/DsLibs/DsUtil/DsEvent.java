// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/** Interface that must be implemented to take an action when a timer is fired. */
public interface DsEvent {
  /**
   * Handler for timer event.
   *
   * @param argument argument supplied to DsTimer.schedule
   * @see DsTimer#schedule
   * @see DsDiscreteTimerMgr#schedule
   */
  public void run(Object argument);
}
