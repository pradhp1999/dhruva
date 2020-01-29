// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents the XApplicationContext header as specified internally by dynamicsoft. It
 * provides methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * XApplicationContext =  "X-Application-Context: ":" TEXT-UTF8-TRIM
 * TEXT-UTF8-TRIM      =  ; no LWS on either side
 * </pre> </code>
 */
public final class DsSipXApplicationContextHeader extends DsSipStringHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_X_APPLICATION_CONTEXT;
  /** Header ID. */
  public static final byte sID = X_APPLICATION_CONTEXT;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_X_APPLICATION_CONTEXT;

  /** Default constructor. */
  public DsSipXApplicationContextHeader() {
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
  public DsSipXApplicationContextHeader(byte[] value) {
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
  public DsSipXApplicationContextHeader(byte[] value, int offset, int count) {
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
  public DsSipXApplicationContextHeader(DsByteString value) {
    super(value);
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
    return BS_X_APPLICATION_CONTEXT_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return X_APPLICATION_CONTEXT;
  }
}
