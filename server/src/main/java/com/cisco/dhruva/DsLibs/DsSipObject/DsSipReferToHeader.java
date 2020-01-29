// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.util.*;

/**
 * This class represents the Refer-To header as specified in the REFER draft
 * (draft-ietf-sip-refer-06.txt). It provides methods to build, access, modify, serialize and clone
 * the Refer-To header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Refer-To =  ("Refer-To" / "r") HCOLON (name-addr / addr-spec) *(SEMI generic-param)
 * </pre> </code>
 */
public final class DsSipReferToHeader extends DsSipNameAddressHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_REFER_TO;
  /** Header ID. */
  public static final byte sID = REFER_TO;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_REFER_TO;

  /** Default constructor. */
  public DsSipReferToHeader() {
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
  public DsSipReferToHeader(byte[] value)
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
  public DsSipReferToHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    super();
    parse(value);
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
  public DsSipReferToHeader(DsByteString value)
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
  public DsSipReferToHeader(DsByteString nameAddress, DsParameters parameters)
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
  public DsSipReferToHeader(DsSipNameAddress nameAddress, DsParameters parameters) {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this Refer-To the specified <code>nameAddress</code>.
   *
   * @param nameAddress the name address for this Refer-To
   */
  public DsSipReferToHeader(DsSipNameAddress nameAddress) {
    this(nameAddress, null);
  }

  /**
   * Constructs this Refer-To the specified <code>uri</code> and the specified <code>parameters
   * </code>.
   *
   * @param uri the uri for this Refer-To
   * @param parameters the list of parameters for this header.
   */
  public DsSipReferToHeader(DsURI uri, DsParameters parameters) {
    super(uri, parameters);
  }

  /**
   * Constructs this Refer-To the specified <code>uri</code>.
   *
   * @param uri the uri for this Refer-To
   */
  public DsSipReferToHeader(DsURI uri) {
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
    return BS_REFER_TO_TOKEN;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return REFER_TO;
  }
}
