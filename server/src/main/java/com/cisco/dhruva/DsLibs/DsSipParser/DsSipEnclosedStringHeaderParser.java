package com.cisco.dhruva.DsLibs.DsSipParser;

/**
 * A SIP header parser that parses headers with BNF that matches the following headers:
 *
 * <p>
 *
 * <pre>
 *    Content-ID
 * </pre>
 *
 * This parser fires events for the following element IDs:
 *
 * <pre>
 *     SINGLE_VALUE
 * </pre>
 */
public final class DsSipEnclosedStringHeaderParser implements DsSipHeaderParserInterface {
  /** The singleton. */
  private static DsSipEnclosedStringHeaderParser instance = new DsSipEnclosedStringHeaderParser();

  /** Singleton - disallow construction. */
  private DsSipEnclosedStringHeaderParser() {}

  /**
   * Get the singleton.
   *
   * @return the only instance of this header parser
   */
  public static DsSipEnclosedStringHeaderParser getInstance() {
    return instance;
  }

  /*
   * javadoc inherited
   */
  public void parseHeader(
      DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipMsgParser.parseEnclosedStringHeader(headerListener, headerType, data, offset, count);
  }
}
