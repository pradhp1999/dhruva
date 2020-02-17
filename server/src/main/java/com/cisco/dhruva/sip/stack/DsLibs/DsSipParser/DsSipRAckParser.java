// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    RAck
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     RESPONSE_NUMBER
 *     CSEQ_NUMBER
 *     CSEQ_METHOD
 * </pre>
 */
public final class DsSipRAckParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipRAckParser instance = new DsSipRAckParser();

  /** Singleton - disallow construction. */
  private DsSipRAckParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipRAckParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseRAck(headerListener, headerType, data, offset, count);
  }
}
