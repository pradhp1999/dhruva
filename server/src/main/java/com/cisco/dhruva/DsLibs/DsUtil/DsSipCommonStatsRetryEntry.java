/*------------------------------------------------------------------
 * DsSipCommonStatsRetryEntry.java
 *
 * October 2003, Rajeev Narang
 *
 * Copyright (c) 2003 by cisco Systems, Inc.
 *------------------------------------------------------------------
 */

package com.cisco.dhruva.DsLibs.DsUtil;

/**
 * This class provides the data structure for storing the statistics data for the outgoing SIP
 * requests and responses. The counters are stored per method and per cumulative "final" and "non
 * final" responses. This class is based on the DsSipCommonStatsRetryEntry of
 * sipCommonStatsRetryTable defined in ietf draft SIP-COMMON-MIB.
 */
public class DsSipCommonStatsRetryEntry {
  public static int sipStatsRetryInvites;
  public static int sipStatsRetryByes;
  public static int sipStatsRetryCancels;
  public static int sipStatsRetryRegisters;
  public static int sipStatsRetryOptions;
  public static int sipStatsRetryFinalResponses;
  public static int sipStatsRetryNonFinalResponses;

  /** Constructor. */
  public DsSipCommonStatsRetryEntry() {}
}
