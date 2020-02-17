// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Authentication-Info
 *    Authorization
 *    Proxy-Authenticate
 *    Proxy-Authorization
 *    WWW-Authenticate
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     SINGLE_VALUE
 *     BASIC_COOKIE
 * </pre>
 */
public final class DsSipAuthParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipAuthParser instance = new DsSipAuthParser();

  /** Singleton - disallow construction. */
  private DsSipAuthParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipAuthParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseAuth(headerListener, headerType, data, offset, count);
  }
}
