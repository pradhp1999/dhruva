// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents a Allow header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header. <br>
 * Method names are case sensitive. New spec allows for extension methods.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Allow            =  "Allow" ":" 1#Method
 * Method           =  "INVITE" | "ACK" | "OPTIONS" | "BYE" | "CANCEL" | "REGISTER" | extension-method
 * extension-method =  token
 * </pre> </code>
 */
public final class DsSipAllowHeader extends DsSipStringHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_ALLOW;
  /** Header ID. */
  public static final byte sID = ALLOW;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** Default constructor. */
  public DsSipAllowHeader() {
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
  public DsSipAllowHeader(byte[] value) {
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
  public DsSipAllowHeader(byte[] value, int offset, int count) {
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
  public DsSipAllowHeader(DsByteString value) {
    super(value);
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name
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
    return BS_ALLOW_TOKEN;
  }

  /**
   * Retrieves the method information in the allow header.
   *
   * @return The method information
   * @see #setMethod(DsByteString)
   */
  public DsByteString getMethod() {
    return getValue();
  }

  /**
   * Sets the method in the allow header.
   *
   * @param aMethod the method name
   * @see #getMethod
   */
  public void setMethod(DsByteString aMethod) {
    setValue(aMethod);
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return ALLOW;
  }
}
