// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * This class represents a AuthenticationInfo header as specified in RFC 3261. It provides methods
 * to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * AuthenticationInfo =  Authentication-info HCOLON 1#( digest - nextnonce )
 * nextnonce          =  nextnonce EQUAL nonce-value
 * </pre> </code>
 */
public final class DsSipAuthenticationInfoHeader extends DsSipAuthHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_AUTHENTICATION_INFO;
  /** Header ID. */
  public static final byte sID = AUTHENTICATION_INFO;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_AUTHENTICATION_INFO;

  /** Default constructor. */
  public DsSipAuthenticationInfoHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipAuthenticationInfoHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipAuthenticationInfoHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   * If there is an exception during parsing phase, it will set the invalid flag of this header and
   * retain the various components that it already parsed. One should check the valid flag before
   * retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipAuthenticationInfoHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name.
   */
  public DsByteString getCompactToken() {
    return sToken;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_AUTHENTICATION_INFO_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return AUTHENTICATION_INFO;
  }
}
