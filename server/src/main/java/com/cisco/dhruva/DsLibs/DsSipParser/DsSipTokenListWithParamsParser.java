// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers.
 *
 * <p>
 *
 * <pre>
 *    Accept-Encoding
 *    Accept-Language
 *    Allow
 *    Allow-Events
 *    Content-Disposition
 *    Content-Encoding
 *    Content-Language
 *    Content-Length
 *    Content-Version
 *    Event
 *    In-Reply-To
 *    MIME-Version
 *    Max-Forwards
 *    Proxy-Require
 *    RSeq
 *    Require
 *    Subscription-State
 *    Supported
 *    Unsupported
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     SINGLE_VALUE
 * </pre>
 */
public final class DsSipTokenListWithParamsParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipTokenListWithParamsParser instance = new DsSipTokenListWithParamsParser();

  /** Singleton - disallow construction. */
  private DsSipTokenListWithParamsParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipTokenListWithParamsParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseTokenListWithParams(headerListener, headerType, data, offset, count);
  }
}
