// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * This class represents the Referred-By header as specified in RFC 3892. It provides methods to
 * build, access, modify, serialize and clone the Referred-By header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 *     Referred-By  =  ("Referred-By" / "b") HCOLON referrer-uri
 *                    *( SEMI (referredby-id-param / generic-param) )
 *     referrer-uri = ( name-addr / addr-spec )
 *     referredby-id-param = "cid" EQUAL sip-clean-msg-id
 *     sip-clean-msg-id = LDQUOT dot-atom "@" (dot-atom / host) RDQUOT
 *     dot-atom = atom *( "." atom )
 *     atom     = 1*( alphanum / "-" / "!" / "%" / "*" /
 * </pre> </code>
 */
public final class DsSipReferredByHeader extends DsSipNameAddressHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_REFERRED_BY;
  /** Header ID. */
  public static final byte sID = REFERRED_BY;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_REFERRED_BY_C;

  /** Default constructor. */
  public DsSipReferredByHeader() {
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
  public DsSipReferredByHeader(byte[] value)
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
  public DsSipReferredByHeader(byte[] value, int offset, int count)
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
  public DsSipReferredByHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this Refer-To the specified <code>nameAddress</code> and the specified <code>
   * parameters</code>. The name address value is first parsed into a valid DsSipNameAddress.
   *
   * @param nameAddress the name address for this Refer-To
   * @param parameters the list of parameters for this header.
   * @throws DsSipParserException if there is an error while parsing the nameAddress value
   */
  public DsSipReferredByHeader(DsByteString nameAddress, DsParameters parameters)
      throws DsSipParserException {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this Refer-To the specified <code>nameAddress</code> and the specified <code>
   * parameters</code>.
   *
   * @param nameAddress the name address for this Refer-To
   * @param parameters the list of parameters for this header.
   */
  public DsSipReferredByHeader(DsSipNameAddress nameAddress, DsParameters parameters) {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this Refer-To the specified <code>nameAddress</code>.
   *
   * @param nameAddress the name address for this Refer-To
   */
  public DsSipReferredByHeader(DsSipNameAddress nameAddress) {
    this(nameAddress, null);
  }

  /**
   * Constructs this Refer-To the specified <code>uri</code> and the specified <code>parameters
   * </code>.
   *
   * @param uri the uri for this Refer-To
   * @param parameters the list of parameters for this header.
   */
  public DsSipReferredByHeader(DsURI uri, DsParameters parameters) {
    super(uri, parameters);
  }

  /**
   * Constructs this Refer-To the specified <code>uri</code>.
   *
   * @param uri the uri for this Refer-To
   */
  public DsSipReferredByHeader(DsURI uri) {
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
    return (isCompact()) ? BS_REFERRED_BY_C_TOKEN : BS_REFERRED_BY_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return REFERRED_BY;
  }

  /**
   * Sets the cid parameter
   *
   * @param cid the cid parameter value
   */
  public void setCid(DsByteString cid) {
    setParameter(BS_CID, cid);
  }

  /**
   * Gets the cid parameter.
   *
   * @return the cid parameter value
   */
  public DsByteString getCid() {
    return getParameter(BS_CID);
  }

  /** Method used to remove the cid parameter. */
  public void removeCid() {
    removeParameter(BS_CID);
  }
}
