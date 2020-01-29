// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/**
 * This class provides methods to control a <code>DsBuckets</code> which is used to limit number of
 * tokens in a flow for specified time periods.
 */
public class DsThrottle {
  DsBuckets buckets;

  /**
   * This constructor creates a DsThrottle based on a DsBuckets. It will use that DsBuckets to do
   * flow throttling.
   *
   * @param theBuckets a DsBuckets which contains the constraints for the flow we want to throttle.
   * @throws DsException if theBuckets is null
   */
  public DsThrottle(DsBuckets theBuckets) throws DsException {
    if (theBuckets == null)
      throw new DsException("theBucket should not be null for the constructor of DsThrottle");
    buckets = theBuckets;
  }

  /** This method starts throttle. */
  public void start() {
    buckets.start();
  }

  /**
   * This method allows user code to reconfigure an existing throttle. User code has to call start()
   * to start the throttle with new configuration setting.
   *
   * @param setting a two-dimensional int array containing the configure value pairs for each of
   *     buckets intended to create. The first value in the pair is the time interval(in seconds)
   *     you want to limit number of tokens for and the second value is the number of tokens you
   *     want to allow for that time interval.
   * @throws DsException When the setting array is not a two-dimensional array or the values are not
   *     all positive or the buckets have not been created.
   */
  public void reConfigure(int[][] setting) throws DsException {
    if (buckets == null) {
      throw new DsException("No buckets have been created for this throttle");
    } else {
      // reConfigure() internally stops the buckets, so start() is needed
      buckets.reConfigure(setting);
    }
  }

  /** This method stops throttle and current settings will not take effect. */
  public void stop() {
    if (buckets != null) buckets.stop();
  }

  /**
   * This method returns the name of this throttle, which is the same as the name of buckets this
   * throttle is based on. This name should be set through DsBuckets.setName(String).
   *
   * @return the name of this throttle.
   */
  public String getName() {
    return buckets.getName();
  }
}
