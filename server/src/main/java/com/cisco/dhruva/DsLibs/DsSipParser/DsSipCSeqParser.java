// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    CSeq
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     CSEQ_NUMBER
 *     CSEQ_METHOD
 * </pre>
 */
public final class DsSipCSeqParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipCSeqParser instance = new DsSipCSeqParser();

  /** Singleton - disallow construction. */
  private DsSipCSeqParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipCSeqParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseCSeq(headerListener, headerType, data, offset, count);
  }
}
