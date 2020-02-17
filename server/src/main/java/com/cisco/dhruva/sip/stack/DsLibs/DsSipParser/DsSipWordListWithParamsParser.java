// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    P-Visited-Network-ID
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     SINGLE_VALUE
 * </pre>
 */
public final class DsSipWordListWithParamsParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipWordListWithParamsParser instance = new DsSipWordListWithParamsParser();

  /** Singleton - disallow construction. */
  private DsSipWordListWithParamsParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipWordListWithParamsParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseWordListWithParams(headerListener, headerType, data, offset, count);
  }
}
