// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/**
 * This class represents a In-Reply-To header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * In-Reply-To =  ( "In-Reply-To" ) ":" 1#callid
 * callid      =  token [ "@" token ] <br>
 * </pre> </code>
 */
public final class DsSipInReplyToHeader extends DsSipStringHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_IN_REPLY_TO;
  /** Header ID. */
  public static final byte sID = IN_REPLY_TO;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_IN_REPLY_TO;

  /** Default constructor. */
  public DsSipInReplyToHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipInReplyToHeader(byte[] value) {
    super(value);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipInReplyToHeader(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipInReplyToHeader(DsByteString value) {
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
    return BS_IN_REPLY_TO_TOKEN;
  }

  /**
   * Sets the call id value in this header.
   *
   * @param callId the new call id value in this header.
   */
  public void setCallId(DsByteString callId) {
    setValue(callId);
  }

  /**
   * Returns the the call id value in this header.
   *
   * @return the call id value in this header.
   */
  public DsByteString getCallId() {
    return getValue();
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return IN_REPLY_TO;
  }
}
