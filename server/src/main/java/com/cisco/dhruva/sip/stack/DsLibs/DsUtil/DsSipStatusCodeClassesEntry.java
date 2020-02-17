/*------------------------------------------------------------------
 * DsSipStatusCodeClassesEntry.java
 *
 * September 2003, Rajeev Narang
 *
 * Copyright (c) 2003 by cisco Systems, Inc.
 *------------------------------------------------------------------
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * This class provides the data structure for storing the statistics data for the incoming/outgoing
 * SIP response messages. The counters are stored per status code class. For e.g all incoming 1xx
 * status codes are counted in sipStatsInfoClassIns. This class is based on the
 * DsSipStatusCodeClassesEntry of sipStatusCodeClassesTable defined in ietf draft SIP-COMMON-MIB.
 */
public class DsSipStatusCodeClassesEntry {
  public static int sipStatsInfoClassIns;
  public static int sipStatsInfoClassOuts;
  public static int sipStatsSuccessClassIns;
  public static int sipStatsSuccessClassOuts;
  public static int sipStatsRedirClassIns;
  public static int sipStatsRedirClassOuts;
  public static int sipStatsReqFailClassIns;
  public static int sipStatsReqFailClassOuts;
  public static int sipStatsServerFailClassIns;
  public static int sipStatsServerFailClassOuts;
  public static int sipStatsGlobalFailClassIns;
  public static int sipStatsGlobalFailClassOuts;
  public static int sipStatsOtherClassesIns;
  public static int sipStatsOtherClassesOuts;

  /** Constructor. */
  public DsSipStatusCodeClassesEntry() {}
}
