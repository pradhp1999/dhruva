// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Authentication-Info
 * </pre>
 *
 * This parser fires events for parameters only.
 */
public final class DsSipAuthParamsParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipAuthParamsParser instance = new DsSipAuthParamsParser();

  /** Singleton - disallow construction. */
  private DsSipAuthParamsParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipAuthParamsParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseAuthParams(headerListener, headerType, data, offset, count);
  }
}
