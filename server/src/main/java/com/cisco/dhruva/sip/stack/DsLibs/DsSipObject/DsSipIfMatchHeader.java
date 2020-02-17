// ////////////////////////////////////////////////////////////////
// FILENAME:    DsSipIfMatchHeader.java
//
// MODULE:      DsSipObject
//
// COPYRIGHT:
// ============== copyright 2003 Cisco Systems ============
// ============== Kai Wang (kaiw@cisco.com) ===============
// ////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the If-Match header as specified in draft-ietf-simple-publish-03.txt. It
 * provides methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * SIP-If-Match   =  "SIP-If-Match":" entity-tag
 * entity-tag =  token
 * </pre> </code>
 */
public final class DsSipIfMatchHeader extends DsSipStringHeader {
  /** Header ID. */
  public static int sID = IF_MATCH;

  /** Default constructor. */
  public DsSipIfMatchHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipIfMatchHeader(byte[] value) {
    super(value);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipIfMatchHeader(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipIfMatchHeader(DsByteString value) {
    super(value);
  }

  /**
   * The method returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return BS_IF_MATCH;
  }

  /**
   * The method Returns the token which is the compact name of the header.
   *
   * @return The compact token name
   */
  public DsByteString getCompactToken() {
    return BS_IF_MATCH;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_IF_MATCH_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return IF_MATCH;
  }
}
