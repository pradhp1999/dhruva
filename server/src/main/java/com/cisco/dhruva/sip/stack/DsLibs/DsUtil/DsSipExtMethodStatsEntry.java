/*------------------------------------------------------------------
 * DsSipExtMethodStatsEntry.java
 *
 * October 2003, Rajeev Narang
 *
 * Copyright (c) 2003 by cisco Systems, Inc.
 *------------------------------------------------------------------
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * This class provides the data structure for storing the count of incoming and outgoing messages of
 * a particular SIP request 'method'. This class is based on the SipStatsExtMethodEntry of
 * sipStatsExtMethodTable defined in ietf draft SIP-COMMON-MIB.
 */
public class DsSipExtMethodStatsEntry {
  public String method;
  public int ins;
  public int outs;

  /** Constructor. */
  public DsSipExtMethodStatsEntry() {}

  public void printVal() {
    System.out.println("method= " + this.method + " ins =" + this.ins + " outs =" + this.outs);
  }
}
