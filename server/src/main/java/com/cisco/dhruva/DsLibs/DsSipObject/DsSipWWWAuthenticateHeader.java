// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.util.*;

/**
 * This class represents the WWW-Authenticate header as specified in RFC 3261. It provides methods
 * to build, access, modify, serialize and clone the header.
 *
 * <p>NOTE: From HTTP spec - User agents MUST take special care in parsing the WWW-Authenticate
 * field value if it contains more than one challenge, or if more than one WWW-Authenticate header
 * field is provided, since the contents of a challenge may itself contain a comma-separated list of
 * authentication parameters. Syntactically it is the same, but it gives semantic meaning.
 *
 * <p><b>Header ABNF:</b> WWW-Authenticate = "WWW-Authenticate" HCOLON challenge challenge =
 * ("Digest" LWS digest-cln *(COMMA digest-cln)) / other-challenge other-challenge = auth-scheme LWS
 * auth-param *(COMMA auth-param) digest-cln = realm / domain / nonce / opaque / stale / algorithm /
 * qop-options / auth-param realm = "realm" EQUAL realm-value realm-value = quoted-string domain =
 * "domain" EQUAL LDQUOT URI *( 1*SP URI ) RDQUOT URI = absoluteURI / abs-path nonce = "nonce" EQUAL
 * nonce-value nonce-value = quoted-string opaque = "opaque" EQUAL quoted-string stale = "stale"
 * EQUAL ( "true" / "false" ) algorithm = "algorithm" EQUAL ( "MD5" / "MD5-sess" / token )
 * qop-options = "qop" EQUAL LDQUOT qop-value *("," qop-value) RDQUOT qop-value = "auth" /
 * "auth-int" / token </pre>
 *
 * </code>
 */
public final class DsSipWWWAuthenticateHeader extends DsSipAuthenticateHeaderBase {
  /** Header token. */
  public static final DsByteString sToken = BS_WWW_AUTHENTICATE;
  /** Header ID. */
  public static final byte sID = WWW_AUTHENTICATE;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_WWW_AUTHENTICATE;

  /** Default constructor. */
  public DsSipWWWAuthenticateHeader() {
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
  public DsSipWWWAuthenticateHeader(byte[] value)
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
  public DsSipWWWAuthenticateHeader(byte[] value, int offset, int count)
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
  public DsSipWWWAuthenticateHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header from the specified authentication type and the specified authentication
   * data (that is the challenge info). It concatenates the authentication type and the
   * authentication data parts into in single byte array and then tries to parse the various
   * components into this header.
   *
   * @param type the authentication type for this header. This can be either BASIC or DIGEST.
   * @param data the authentication data (that is the challenge info)
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipWWWAuthenticateHeader(DsByteString type, DsByteString data)
      throws DsSipParserException, DsSipParserListenerException {
    super(type, data);
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
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
    return BS_WWW_AUTHENTICATE_TOKEN;
  }

  /**
   * Gets the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return WWW_AUTHENTICATE;
  }

  /**
   * Creates the corresponding Authorization header object with the specified type and the specified
   * credentials info <code>data</code>. The credentials info is not parsed here and is set to the
   * corresponding Authorization header as it is. This method should be used only if we know that
   * the specified credentials info is correct and we are not going to access the various parameters
   * separately from the returned Authorization header.
   *
   * @param type the type of the credentials (BASIC or DIGEST)
   * @param data the credentials info
   * @return the created authorization header.
   */
  public DsSipAuthorizationHeaderBase createAuthorization(DsByteString type, DsByteString data) {
    DsSipAuthorizationHeader header = new DsSipAuthorizationHeader();
    header.setType(type);
    header.setData(data);
    return header;
  }
}
