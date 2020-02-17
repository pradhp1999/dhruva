// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Server
 *    User-Agent
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     COMMENT
 *     TYPE
 *     SUB_TYPE
 * </pre>
 */
public final class DsSipServerUserAgentParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipServerUserAgentParser instance = new DsSipServerUserAgentParser();

  /** Singleton - disallow construction. */
  private DsSipServerUserAgentParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipServerUserAgentParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseServerUserAgent(headerListener, headerType, data, offset, count);
  }
}
