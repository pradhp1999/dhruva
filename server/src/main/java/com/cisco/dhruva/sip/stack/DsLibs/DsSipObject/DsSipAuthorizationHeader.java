// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * This class represents a Authorization header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Authorization     =  "Authorization" HCOLON credentials
 * credentials       =  ("Digest" LWS digest-response) / other-response
 * digest-response   =  dig-resp *(COMMA dig-resp)
 * dig-resp          =  username / realm / nonce / digest-uri
 *                       / dresponse / algorithm / cnonce
 *                       / opaque / message-qop
 *                       / nonce-count / auth-param
 * username          =  "username" EQUAL username-value
 * username-value    =  quoted-string
 * digest-uri        =  "uri" EQUAL LDQUOT digest-uri-value RDQUOT
 * digest-uri-value  =  rquest-uri ; Equal to request-uri as specified by HTTP/1.1
 * message-qop       =  "qop" EQUAL qop-value
 * cnonce            =  "cnonce" EQUAL cnonce-value
 * cnonce-value      =  nonce-value
 * nonce-count       =  "nc" EQUAL nc-value
 * nc-value          =  8LHEX
 * dresponse         =  "response" EQUAL request-digest
 * request-digest    =  LDQUOT 32LHEX RDQUOT
 * auth-param        =  auth-param-name EQUAL ( token / quoted-string )
 * auth-param-name   =  token
 * other-response    =  auth-scheme LWS auth-param *(COMMA auth-param)
 * auth-scheme       =  token
 * </pre> </code>
 */
public final class DsSipAuthorizationHeader extends DsSipAuthorizationHeaderBase {
  /** Header token. */
  public static final DsByteString sToken = BS_AUTHORIZATION;
  /** Header ID. */
  public static final DsByteString sCompactToken = sToken;
  /** Compact header token. */
  public static final byte sID = AUTHORIZATION;

  /** Default constructor. */
  public DsSipAuthorizationHeader() {
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
  public DsSipAuthorizationHeader(byte[] value)
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
  public DsSipAuthorizationHeader(byte[] value, int offset, int count)
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
  public DsSipAuthorizationHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header from the specified authentication type and the specified authentication
   * data (that is the credentials info. It concatenates the authentication type and the
   * authentication data parts into in single byte array and then tries to parse the various
   * components into this header.
   *
   * @param type the authentication type for this header. This can be either BASIC or DIGEST.
   * @param data the authentication data (that is the credentials info)
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipAuthorizationHeader(DsByteString type, DsByteString data)
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
    return BS_AUTHORIZATION_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return AUTHORIZATION;
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            byte[] bytes = read();
  //            DsSipAuthorizationHeader header = new DsSipAuthorizationHeader(bytes);
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            header.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //
  //            DsSipAuthorizationHeader clone = (DsSipAuthorizationHeader) header.clone();
  //            clone.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< (HEADER == CLONE) = "
  //                                                    + header.equals(clone)
  //                                                    +" >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< (CLONE == HEADER) = "
  //                                                    + clone.equals(header)
  //                                                    +" >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("Extension Parameters = " + header.getParameters());
  //            System.out.println();
  //            System.out.println("username = " + header.getParameter(new
  // DsByteString("username")));
  //            System.out.println();
  //            System.out.println("realm = " + header.getParameter(new DsByteString("realm")));
  //            System.out.println();
  //            System.out.println("nonce = " + header.getParameter(new DsByteString("nonce")));
  //            System.out.println();
  //            System.out.println("response = " + header.getParameter(new
  // DsByteString("response")));
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()
}
