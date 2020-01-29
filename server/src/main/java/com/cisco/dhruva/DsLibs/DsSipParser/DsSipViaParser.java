// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Via
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     PROTOCOL_NAME
 *     PROTOCOL_VERSION
 *     TRANSPORT
 *     HOST
 *     PORT
 *     COMMENT
 * </pre>
 */
public final class DsSipViaParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipViaParser instance = new DsSipViaParser();

  /** Singleton - disallow construction. */
  private DsSipViaParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipViaParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseVia(headerListener, headerType, data, offset, count);
  }
}
