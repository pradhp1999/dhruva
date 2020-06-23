/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.util.concurrent.TimeUnit;

public abstract class ScheduledTaskDelay {

  protected TimeUnit sourceTimeUnit = null;
  protected long time = 0;

  public ScheduledTaskDelay(long time, TimeUnit sourceTimeUnit) {
    this.time = time;
    if (sourceTimeUnit == null) {
      throw new NullPointerException("sourceTimeUnit cannot be null");
    }
    this.sourceTimeUnit = sourceTimeUnit;
  }

  public long getTaskDelay(TimeUnit timeUnit) {
    return timeUnit.convert(time, sourceTimeUnit);
  }
}
