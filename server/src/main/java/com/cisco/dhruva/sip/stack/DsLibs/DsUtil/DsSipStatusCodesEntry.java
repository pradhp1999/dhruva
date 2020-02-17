/*------------------------------------------------------------------
 * DsSipStatusCodesEntry.java
 *
 * October 2003, Rajeev Narang
 *
 * Copyright (c) 2003 by cisco Systems, Inc.
 *------------------------------------------------------------------
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * This class provides the data structure for storing the count of incoming and outgoing messages of
 * a particular 'status' code. This class is based on the DsSipStatusCodesEntry of
 * sipStatusCodesTable defined in ietf draft SIP-COMMON-MIB.
 */
public class DsSipStatusCodesEntry {
  public String statusCode;
  public int ins;
  public int outs;

  /** Constructor. */
  public DsSipStatusCodesEntry() {}

  public void printVal() {
    System.out.println(
        "statusCode= " + this.statusCode + " ins =" + this.ins + " outs =" + this.outs);
  }

  /**
   * This function searches an array of DsSipStatusCodesEntry structures for an entry corresponding
   * to the given status code passed as a parameter. Returns an entry if found, otherwise returns
   * null.
   *
   * @param cStatusArr <code>An array of DsSipStatusCodesEntry</code>
   * @param sCode <code>status code</code> to be searched
   */
  public static DsSipStatusCodesEntry findStatusCodesEntry(
      DsSipStatusCodesEntry[] cStatusArr, String sCode) {
    if (cStatusArr == null || sCode == null) {
      return null;
    }
    for (int i = 0; i < cStatusArr.length; i++) {
      if (cStatusArr[i] != null && cStatusArr[i].statusCode.compareTo(sCode) == 0) {
        return cStatusArr[i];
      }
    }
    return null;
  }
}
