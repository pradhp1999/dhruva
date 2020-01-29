// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/**
 * Holds performance data gathered from DsPerf, destined for the stat collector. Basically, this
 * class is just a struct that holds the count, time and name.
 */
public class DsPerfData {
  /** The number of calls. */
  public int count;
  /** The total time of the calls. */
  public long time;
  /** The name of the call. */
  public String name;

  /**
   * Constructor that takes all of the data members of this class.
   *
   * @param count the number of calls
   * @param time the total time of the calls
   * @param name the name of the call
   */
  public DsPerfData(int count, long time, String name) {
    this.count = count;
    this.time = time;
    this.name = name;
  }
}
