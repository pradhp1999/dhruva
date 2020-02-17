// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * This class represents the Replaces header as specified in RFC 3891. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Replaces        =  "Replaces" HCOLON callid *(SEMI replaces-param)
 * replaces-param  =  to-tag / from-tag / early-flag / generic-param
 * to-tag          =  "to-tag" EQUAL token
 * from-tag        =  "from-tag" EQUAL token
 * early-flag      =  "early-only"
 * </pre> </code>
 */
// CAFFEINE 2.0 DEVELOPMENT - (EDCS-304264)
//   Create an abstract parent class DsSipDialogIDWithParams.
//   All common part of the DsSipJoinHeader and DsSipReplaces headers are moved to this class to
// reduce code redudancy.
//   Added feature support for Join and Replaces Header
public final class DsSipReplacesHeader extends DsSipDialogIDWithParamsHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_REPLACES;
  /** Header ID. */
  public static final byte sID = REPLACES;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_REPLACES;

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-304264) Support Join and Replace header
  /** Header token plus colon */
  public static DsByteString sTokenC = BS_REPLACES_TOKEN;

  /** Default constructor. */
  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-304264) Support Join and Replace header.
  protected DsSipReplacesHeader() {
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
  public DsSipReplacesHeader(byte[] value)
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
  public DsSipReplacesHeader(byte[] value, int offset, int count)
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
  public DsSipReplacesHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
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
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name
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
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-304264) Support Join and Replace header
    return sTokenC;
  }

  /*
   CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to DsSipDialogIDWithParamsHeader.java
   The following methods were moved to DsSipDialogIDWithParamsHeader.java:
      public DsByteString getCallId()
      public void setCallId(DsByteString callId)
      public void setToTag(DsByteString toTag)
      public DsByteString getToTag()
      public void removeToTag()
      public void setFromTag(DsByteString fromTag)
      public DsByteString getFromTag()
      public void removeFromTag()
      public void setEarlyOnly()
      public boolean isEarlyOnly()
      public void removeEarlyOnly()
      public void writeValue(OutputStream out) throws IOException
      protected void copy(DsSipHeader header)
  */

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-304264) Support Join and Replace header
    return sID;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    // CAFFEINE 2.0 DEVELOPMENT - (EDCS-304264) Support Join and Replace header
    if (!(obj instanceof DsSipReplacesHeader)) {
      return false;
    }
    return super.equals(obj);
  }
  /*
   CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to DsSipDialogIDWithParamsHeader.java
   The following methods were moved to DsSipDialogIDWithParamsHeader.java:
      public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md) throws IOException
      public void reInit()
      public void elementFound(int contextId, int elementId,  byte[] buffer,
                               int offset, int count, boolean valid)
          throws DsSipParserListenerException
  */
}
