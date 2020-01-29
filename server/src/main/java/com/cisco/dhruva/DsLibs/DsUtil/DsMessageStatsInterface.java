// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/**
 * Implement this interface and supply it to DsMessageStatistics by calling its
 * setMessageStatsInterface() in order to get notified about interval rollover.
 */
public interface DsMessageStatsInterface {
  /**
   * This method will be called by DsMessageStatistics after user application invokes
   * DsMessageStatistics.setMessageStatsInterface() with the implementation of this interface.
   * Implement this method to handle interval rollover.
   */
  public void onIntervalRollover();
}
