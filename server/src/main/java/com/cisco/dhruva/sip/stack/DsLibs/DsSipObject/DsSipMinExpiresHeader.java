// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the Min-expires header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Min-Expires  =  ( "Min-Expires" ) ":" 1*DIGIT
 * </pre> </code>
 */

// CAFFEINE 2.0 DEVELOPMENT - Fix bug CSCef18257
public final class DsSipMinExpiresHeader extends DsSipDateOrDeltaHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_MIN_EXPIRES;
  /** Header ID. */
  public static final byte sID = MIN_EXPIRES;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_MIN_EXPIRES;

  /** Default constructor. */
  public DsSipMinExpiresHeader() {
    super();
  }

  /**
   * Constructor used to set the contentLength header.
   *
   * @param value the contentLength
   */
  public DsSipMinExpiresHeader(int value) {
    super(value);
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
   */
  public DsByteString getCompactToken() {
    return sCompactToken;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_MIN_EXPIRES_TOKEN;
  }

  /**
   * Sets the expires value.
   *
   * @param value The expires value to be set.
   */
  // CAFFEINE 2.0 DEVELOPMENT - Fix bug CSCef18257
  public void setExpires(long value) {
    m_lDeltaSeconds = value;
  }

  /**
   * Retrieves the expires value.
   *
   * @return The expires value.
   */
  // CAFFEINE 2.0 DEVELOPMENT - Fix bug CSCef18257
  public long getExpires() {
    return m_lDeltaSeconds;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return MIN_EXPIRES;
  }
}
