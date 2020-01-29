// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents the Proxy-Authenticate header as specified in RFC 3261. It provides methods
 * to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Proxy-Authenticate  =  "Proxy-Authenticate" HCOLON challenge
 * challenge           =  ("Digest" LWS digest-cln *(COMMA digest-cln)) / other-challenge
 * other-challenge     =  auth-scheme LWS auth-param *(COMMA auth-param)
 * digest-cln          =  realm / domain / nonce
 *                         / opaque / stale / algorithm
 *                         / qop-options / auth-param
 * realm               =  "realm" EQUAL realm-value
 * realm-value         =  quoted-string
 * domain              =  "domain" EQUAL LDQUOT URI *( 1*SP URI ) RDQUOT
 * URI                 =  absoluteURI / abs-path
 * nonce               =  "nonce" EQUAL nonce-value
 * nonce-value         =  quoted-string
 * opaque              =  "opaque" EQUAL quoted-string
 * stale               =  "stale" EQUAL ( "true" / "false" )
 * algorithm           =  "algorithm" EQUAL ( "MD5" / "MD5-sess" / token )
 * qop-options         =  "qop" EQUAL LDQUOT qop-value *("," qop-value) RDQUOT
 * qop-value           =  "auth" / "auth-int" / token
 * </pre> </code>
 */
public final class DsSipProxyAuthenticateHeader extends DsSipAuthenticateHeaderBase
    implements Serializable, Cloneable {
  /** Header token. */
  public static final DsByteString sToken = BS_PROXY_AUTHENTICATE;
  /** Header ID. */
  public static final byte sID = PROXY_AUTHENTICATE;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  public static final byte sFixedFormatHeaderId =
      DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_AUTHENTICATION_HEADER;

  /** Default constructor. */
  public DsSipProxyAuthenticateHeader() {
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
  public DsSipProxyAuthenticateHeader(byte[] value)
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
  public DsSipProxyAuthenticateHeader(byte[] value, int offset, int count)
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
  public DsSipProxyAuthenticateHeader(DsByteString value)
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
   * @param data the authentication data (that is the challenge info).
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipProxyAuthenticateHeader(DsByteString type, DsByteString data)
      throws DsSipParserException, DsSipParserListenerException {
    super(type, data);
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return the complete token name.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return the complete token name.
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
    return BS_PROXY_AUTHENTICATE_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return PROXY_AUTHENTICATE;
  }

  /**
   * Creates the corresponding Authorization header object with the specified type and the specified
   * credentials info <code>data</code>. The credentials info is not parsed here and is set to the
   * corresponding Authorization header as it is. This method should be used only if we know that
   * the specified credentials info is correct and we are not going to access the various parameters
   * separately from the returned Authorization header.
   *
   * @param type the type of the credentials (BASIC or DIGEST).
   * @param data the credentials info.
   * @return the create authorization header.
   */
  public DsSipAuthorizationHeaderBase createAuthorization(DsByteString type, DsByteString data) {
    DsSipProxyAuthorizationHeader header = new DsSipProxyAuthorizationHeader();
    header.setType(type);
    header.setData(data);
    return header;
  }

  public void writeEncodedHeaderName(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (this.getType().equalsIgnoreCase("digest")) {
      if (getParamCount() != 3) {
        super.writeEncodedHeaderName(out, md);
      } else {
        out.write(sFixedFormatHeaderId);
      }
    } else {
      super.writeEncodedHeaderName(out, md);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    // System.out.println("Writing proxyAuthenticate- "+this.toByteString());

    if (this.getType().equalsIgnoreCase("digest")) {
      if (getParamCount() != 3) {
        super.writeEncodedValue(out, md);
      } else {
        // this is more that just a get method, there are important side effect. - jsm
        getChallengeInfo();

        // realm
        DsByteString realm = getParameter(BS_REALM);
        if (realm.charAt(0) == '"') {
          md.getEncoding(realm.substring(1, realm.length() - 1)).write(out);
        } else {
          md.getEncoding(realm).write(out);
        }

        // domain
        DsByteString domain = getParameter(BS_DOMAIN);
        if (domain.charAt(0) == '"') {
          md.getEncoding(domain.substring(1, domain.length() - 1)).write(out);
        } else {
          md.getEncoding(domain).write(out);
        }

        // nonce
        DsByteString nonce = getParameter(BS_NONCE);
        if (nonce.charAt(0) == '"') {
          md.getEncoding(nonce.substring(1, nonce.length() - 1)).write(out);
        } else {
          md.getEncoding(nonce).write(out);
        }
      }
    } else {
      super.writeEncodedValue(out, md);
    }
  }
}
