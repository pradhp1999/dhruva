/*------------------------------------------------------------------
 * DsSipAcceptContactHeader
 *
 * July 2003, kimle
 *
 * Copyright (c) 2003, 2006 by cisco Systems, Inc.
 * All rights reserved.
 *------------------------------------------------------------------
 */
package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * This class represents the class for Accept-Contact header as specified in caller prefs.
 *
 * <p>It provides methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Accept-Contact  =  ("Accept-Contact" / "a") HCOLON acrc-value
 *                    *(COMMA ac-value)
 * acrc-value      =  "*" *(SEMI acrc-params)
 * acrc-params     =  feature-param / req-param
 *                       / explicit-param / generic-param
 *                     ;;feature param from RFC XXXX
 *                     ;;generic-param from RFC 3261
 * req-param       =  "require"
 * explicit-param  =  "explicit"
 * </pre> </code>
 */
public final class DsSipAcceptContactHeader extends DsSipAcceptRejectContactHeader {
  /** Header token. */
  public static DsByteString sToken = BS_ACCEPT_CONTACT;
  /** Header token plus colon. */
  public static DsByteString sTokenC = BS_ACCEPT_CONTACT_TOKEN;
  /** Header ID. */
  public static int sID = ACCEPT_CONTACT;

  /** Default constructor. */
  protected DsSipAcceptContactHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.
   *
   * <p>The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipAcceptContactHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Constructs this header with the specified value.
   *
   * <p>The byte array <code>value</code> should be the value part
   *
   * <p>(data after the colon) of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipAcceptContactHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.
   *
   * <p>The specified byte string <code>value</code> should be the value part (data after the colon)
   * of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipAcceptContactHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
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
    return BS_ACCEPT_CONTACT_C;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public DsByteString getTokenC() {
    return (isCompact()) ? BS_ACCEPT_CONTACT_C_TOKEN : BS_ACCEPT_CONTACT_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public int getHeaderID() {
    return sID;
  }
}
