// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents the Content-Length header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Content-Length  =  ( "Content-Length" | "l" ) ":" 1*DIGIT
 * </pre> </code>
 */
public final class DsSipContentLengthHeader extends DsSipIntegerHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_CONTENT_LENGTH;
  /** Header ID. */
  public static final byte sID = CONTENT_LENGTH;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_CONTENT_LENGTH_C;

  /** Default constructor. */
  public DsSipContentLengthHeader() {
    super();
  }

  /**
   * Constructor used to set the contentLength header.
   *
   * @param value the contentLength
   */
  public DsSipContentLengthHeader(int value) {
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
    return (isCompact()) ? BS_CONTENT_LENGTH_C_TOKEN : BS_CONTENT_LENGTH_TOKEN;
  }

  /**
   * Sets the content Length.
   *
   * @param length The length to be set
   */
  public void setContentLength(int length) {
    setIntegerValue(length);
  }

  /**
   * Retrieves the content length.
   *
   * @return The length of the content.
   */
  public int getContentLength() {
    return m_iValue;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CONTENT_LENGTH;
  }
}
