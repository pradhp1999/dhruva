// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    P-Charging-Function-Addresses
 *    P-Charging-Vector
 * </pre>
 *
 * This parser fires events for parameters only.
 */
public final class DsSipParamsParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipParamsParser instance = new DsSipParamsParser();

  /** Singleton - disallow construction. */
  private DsSipParamsParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipParamsParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseParams(headerListener, headerType, data, offset, count);
  }
}
