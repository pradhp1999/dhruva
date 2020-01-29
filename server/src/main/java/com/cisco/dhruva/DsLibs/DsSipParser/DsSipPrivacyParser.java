// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Privacy
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     SINGLE_VALUE
 * </pre>
 */
public final class DsSipPrivacyParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipPrivacyParser instance = new DsSipPrivacyParser();

  /** Singleton - disallow construction. */
  private DsSipPrivacyParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipPrivacyParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parsePrivacy(headerListener, headerType, data, offset, count);
  }
}
