// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Timestamp
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     SINGLE_VALUE
 *     DELAY
 * </pre>
 */
public final class DsSipTimestampParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipTimestampParser instance = new DsSipTimestampParser();

  /** Singleton - disallow construction. */
  private DsSipTimestampParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipTimestampParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseTimestamp(headerListener, headerType, data, offset, count);
  }
}
