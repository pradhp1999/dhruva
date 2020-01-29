// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Call-ID
 *    Organization
 *    Priority
 *    Subject
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     SINGLE_VALUE
 * </pre>
 */
public final class DsSipStringHeaderParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipStringHeaderParser instance = new DsSipStringHeaderParser();

  /** Singleton - disallow construction. */
  private DsSipStringHeaderParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipStringHeaderParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseStringHeader(headerListener, headerType, data, offset, count);
  }
}
