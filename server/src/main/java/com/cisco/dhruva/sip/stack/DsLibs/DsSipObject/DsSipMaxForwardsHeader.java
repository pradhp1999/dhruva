// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the Max-Forwards header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Max-Forwards  =  "Max-Forwards" ":" 1*DIGIT
 * </pre> </code>
 */
public final class DsSipMaxForwardsHeader extends DsSipIntegerHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_MAX_FORWARDS;
  /** Header ID. */
  public static final byte sID = MAX_FORWARDS;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_MAX_FORWARDS;

  /** Default constructor. */
  public DsSipMaxForwardsHeader() {
    super();
  }

  /**
   * Constructs this header with the specified max forwards integer <code>value</code>. <code>value
   * </code>. Any value greater than 255 will result in this value being set to 255. Any value &LT 0
   * will be set to 0.
   *
   * @param value the max forwards integer value for this header.
   */
  public DsSipMaxForwardsHeader(int value) {
    super();
    setMaxForwards(value);
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
    return BS_MAX_FORWARDS_TOKEN;
  }

  /**
   * Sets the max forwards value of this header to the specified integer <code>value</code>. Any
   * value greater than 255 will result in this value being set to 255. Any value &LT 0 will be set
   * to 0. It in turn calls {@link DsSipIntegerHeader#setIntegerValue(int)} method.
   *
   * @param value the new max forwards integer value for this header.
   */
  public void setMaxForwards(int value) {
    if (value > MAX_FORWARDS_MAX_VALUE) {
      setIntegerValue(MAX_FORWARDS_MAX_VALUE);
    } else if (value < MAX_FORWARDS_MIN_VALUE) {
      setIntegerValue(MAX_FORWARDS_MIN_VALUE);
    } else {
      setIntegerValue(value);
    }
  }

  /**
   * Returns the max forwards value of this header.It in turn calls {@link
   * DsSipIntegerHeader#getIntegerValue()} method.
   *
   * @return the max forwards integer value of this header.
   */
  public int getMaxForwards() {
    return m_iValue;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return MAX_FORWARDS;
  }
}
