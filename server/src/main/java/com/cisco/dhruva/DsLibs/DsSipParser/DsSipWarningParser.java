// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Warning
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     WARN_CODE
 *     HOST
 *     WARN_TEXT
 * </pre>
 */
public final class DsSipWarningParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipWarningParser instance = new DsSipWarningParser();

  /** Singleton - disallow construction. */
  private DsSipWarningParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipWarningParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseWarning(headerListener, headerType, data, offset, count);
  }
}
