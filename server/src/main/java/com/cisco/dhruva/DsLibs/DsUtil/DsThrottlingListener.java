// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/**
 * Implement this interface if you want to get notified of throttling events. Throttling is useful
 * when you want to control the number of tokens in a flow for a specified time period. When the
 * flow exceeds that limit or it goes below the limit after exceeding it, the implementation class
 * will be notified through the methods declared below.
 *
 * <p>To use throttling in UA itself, you have to first create a <code>DsBuckets</code> and then
 * register the implementation of this interface by calling <code>
 * DsConfigManager.registerThrottlingListener()</code>. When throttling events happen as indicated
 * by <code>DsBuckets.isOK()</code>, call <code>DsConfigManager.raiseThrottlingInEffect()</code> or
 * <code>DsConfigManager.raiseThrottlingNotInEffect()</code>. The registered implementing class of
 * this DsThrottlingListener interface will be notified of those throttling events by <code>
 * DsConfigManager</code>.
 *
 * <p>To use throttling in user application code, you have to implement your own
 * registration/notification mechanism besides creating a <code>DsBuckets</code>.
 */
public interface DsThrottlingListener {
  /**
   * This method will be called when the flow exceeds the limit and throttling starts to take
   * effect.
   *
   * @param throttleName the name of throttle which takes effect
   */
  public void flowExceedsThreshold(String throttleName);
  /**
   * This method will be called when the flow goes back to be below the limit after exceeds the
   * limit.
   *
   * @param throttleName the name of throttle which ceases to take effect.
   */
  public void flowOKAgain(String throttleName);
}
