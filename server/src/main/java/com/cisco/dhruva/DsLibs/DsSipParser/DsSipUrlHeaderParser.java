// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Alert-Info
 *    Call-Info
 *    Contact
 *    Error-Info
 *    From
 *    Record-Route
 *    Remote-Party-ID
 *    Reply-To
 *    Route
 *    To
 *    Translate
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     WILDCARD
 *     DISPLAY_NAME
 *     SIP_URL, SIPS_URL, TEL_URL, HTTP_URL, CID_URL, or UNKNOWN_URL
 * </pre>
 */
public final class DsSipUrlHeaderParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipUrlHeaderParser instance = new DsSipUrlHeaderParser();

  /** Singleton - disallow construction. */
  private DsSipUrlHeaderParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipUrlHeaderParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseUrlHeader(headerListener, headerType, data, offset, count);
  }
}
