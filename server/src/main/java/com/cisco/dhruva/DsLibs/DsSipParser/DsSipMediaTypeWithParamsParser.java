// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Accept
 *    Content-Type
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     TYPE
 *     SUB_TYPE
 * </pre>
 */
public final class DsSipMediaTypeWithParamsParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipMediaTypeWithParamsParser instance = new DsSipMediaTypeWithParamsParser();

  /** Singleton - disallow construction. */
  private DsSipMediaTypeWithParamsParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipMediaTypeWithParamsParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseMediaTypeWithParams(headerListener, headerType, data, offset, count);
  }
}
