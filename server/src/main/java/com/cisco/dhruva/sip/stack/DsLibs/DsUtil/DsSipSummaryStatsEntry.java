/*------------------------------------------------------------------
 * DsSipSummaryStatsEntry.java
 *
 * October 2003, Rajeev Narang
 *
 * Copyright (c) 2003 by cisco Systems, Inc.
 *------------------------------------------------------------------
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * This class provides the data structure for storing the summary statistics data for the
 * incoming/outgoing SIP request and response messages. The field for total no. of transactions is
 * also present. This class is based on the DsSipSummaryStatsEntry of sipSummaryStatsTable defined
 * in ietf draft SIP-COMMON-MIB.
 */
public class DsSipSummaryStatsEntry {
  public static int m_sipSummaryInRequests;
  public static int m_sipSummaryOutRequests;
  public static int m_sipSummaryInResponses;
  public static int m_sipSummaryOutResponses;
  public static int m_sipSummaryTotalTransactions;

  /** Constructor. */
  public DsSipSummaryStatsEntry() {}
}
