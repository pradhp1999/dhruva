// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * This class represents the P-Preferred-Identity header as specified in RFC 3325. It provides
 * methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * P-Preferred-Identity =  "P-Preferred-Identity" HCOLON PPreferredID-value
 *                          *(COMMA PPreferredID-value)
 * PPreferredID-value = name-addr / addr-spec
 * </pre> </code>
 */
public final class DsSipPPreferredIdentityHeader extends DsSipNameAddressHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_P_PREFERRED_IDENTITY;
  /** Header ID. */
  public static final byte sID = P_PREFERRED_IDENTITY;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** Default constructor. */
  public DsSipPPreferredIdentityHeader() {
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
  public DsSipPPreferredIdentityHeader(byte[] value)
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
  public DsSipPPreferredIdentityHeader(byte[] value, int offset, int count)
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
  public DsSipPPreferredIdentityHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified <code>nameAddress</code> and the specified <code>
   * parameters</code>. The name address value is first parsed into a valid DsSipNameAddress.
   *
   * @param nameAddress the name address for this header.
   * @param parameters the list of parameters for this header.
   * @throws DsSipParserException if there is an error while parsing the nameAddress value
   */
  public DsSipPPreferredIdentityHeader(DsByteString nameAddress, DsParameters parameters)
      throws DsSipParserException {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this header with the specified <code>nameAddress</code> and the specified <code>
   * parameters</code>.
   *
   * @param nameAddress the name address for this header.
   * @param parameters the list of parameters for this header.
   */
  public DsSipPPreferredIdentityHeader(DsSipNameAddress nameAddress, DsParameters parameters) {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this header with the specified <code>nameAddress</code>.
   *
   * @param nameAddress the name address for this header.
   */
  public DsSipPPreferredIdentityHeader(DsSipNameAddress nameAddress) {
    this(nameAddress, null);
  }

  /**
   * Constructs this header with the specified <code>uri</code> and the specified <code>parameters
   * </code>.
   *
   * @param uri the uri for this header.
   * @param parameters the list of parameters for this header.
   */
  public DsSipPPreferredIdentityHeader(DsURI uri, DsParameters parameters) {
    super(uri, parameters);
  }

  /**
   * Constructs this header with the specified <code>uri</code>.
   *
   * @param uri the uri for this header.
   */
  public DsSipPPreferredIdentityHeader(DsURI uri) {
    super(uri);
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
    return BS_P_PREFERRED_IDENTITY_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return P_PREFERRED_IDENTITY;
  }

  public static void main(String[] args) {
    try {
      // --            byte[] bytes = read();
      byte[] bytes =
          "c1 <sip:63.113.46.75:5060;transport=UDP>;methods=\"MESSAGE,NOTIFY,SUBSCRIBE\""
              .getBytes();
      DsSipPPreferredIdentityHeader header = new DsSipPPreferredIdentityHeader(bytes);
      System.out.println();
      System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
      System.out.println();
      header.write(System.out);
      System.out.println();
      System.out.println();
      System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
      System.out.println();
      DsSipPPreferredIdentityHeader clone = (DsSipPPreferredIdentityHeader) header.clone();
      clone.write(System.out);
      System.out.println();
      System.out.println();
      System.out.println(
          "<<<<<<<<<<<<<<<<< (HEADER == CLONE) = "
              + header.equals(clone)
              + " >>>>>>>>>>>>>>>>>>>>");
      System.out.println();
      System.out.println();
      System.out.println(
          "<<<<<<<<<<<<<<<<< (CLONE == HEADER) = "
              + clone.equals(header)
              + " >>>>>>>>>>>>>>>>>>>>");
      System.out.println();
    } catch (Exception e) {
      e.printStackTrace();
    }
  } // Ends main()
}
