// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Expires
 *    Retry-After
 *    Session-Expires
 *    Subscription-Expires
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     DELTA_SECONDS
 *     SIP_DATE
 *     COMMENT
 * </pre>
 */
public final class DsSipDateOrLongParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipDateOrLongParser instance = new DsSipDateOrLongParser();

  /** Singleton - disallow construction. */
  private DsSipDateOrLongParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipDateOrLongParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseDateOrLong(headerListener, headerType, data, offset, count);
  }
}
