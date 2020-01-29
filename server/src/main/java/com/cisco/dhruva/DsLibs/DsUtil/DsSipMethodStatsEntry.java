/*------------------------------------------------------------------
 * DsSipMethodStatsEntry.java
 *
 * October 2003, Rajeev Narang
 *
 * Copyright (c) 2003 by cisco Systems, Inc.
 *------------------------------------------------------------------
 */

package com.cisco.dhruva.DsLibs.DsUtil;

/**
 * This class provides the data structure for storing the statistics data for the incoming and
 * outgoing SIP requests. The counters are stored per method for each direction (in/out). This class
 * is based on the DsSipMethodStatsEntry of sipMethodStatsTable defined in ietf draft
 * SIP-COMMON-MIB.
 */
public class DsSipMethodStatsEntry {
  public static int sipStatsInviteIns;
  public static int sipStatsInviteOuts;
  public static int sipStatsAckIns;
  public static int sipStatsAckOuts;
  public static int sipStatsByeIns;
  public static int sipStatsByeOuts;
  public static int sipStatsCancelIns;
  public static int sipStatsCancelOuts;
  public static int sipStatsOptionsIns;
  public static int sipStatsOptionsOuts;
  public static int sipStatsRegisterIns;
  public static int sipStatsRegisterOuts;

  /** Constructor. */
  public DsSipMethodStatsEntry() {}
}
