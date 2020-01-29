// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *     Treats header as a name/value pair only
 * </pre>
 *
 * This parser fires events through the unknownFound() interface.
 */
public final class DsSipUnknownHeaderParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipUnknownHeaderParser instance = new DsSipUnknownHeaderParser();

  /** Singleton - disallow construction. */
  private DsSipUnknownHeaderParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipUnknownHeaderParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseUnknownHeader(headerListener, headerType, data, offset, count);
  }
}
