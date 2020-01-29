// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents the MIME-Version header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * MIME-Version =  "MIME-Version" ":" 1*DIGIT "." 1*DIGIT
 * </pre> </code>
 */
public final class DsSipMIMEVersionHeader extends DsSipFloatHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_MIME_VERSION;
  /** Header ID. */
  public static final byte sID = MIME_VERSION;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_MIME_VERSION;

  /** Default constructor. */
  public DsSipMIMEVersionHeader() {
    super();
  }

  /**
   * Constructs this header with the specified MIME version <code>value</code>.
   *
   * @param value the MIME version value for this header.
   */
  public DsSipMIMEVersionHeader(float value) {
    super(value);
  }

  /**
   * The method returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * The method Returns the token which is the compact name of the header.
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
    return BS_MIME_VERSION_TOKEN;
  }

  /**
   * Sets the MIME version value of this header to the specified float <code>value</code>. It in
   * turn calls {@link DsSipFloatHeader#setFloatValue(float)} method.
   *
   * @param value the new MIME version value for this header.
   */
  public void setVersion(float value) {
    setFloatValue(value);
  }

  /**
   * Retrieves the MIME version value for this header.
   *
   * @return the MIME version value for this header.
   */
  public float getVersion() {
    return getFloatValue();
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return MIME_VERSION;
  }
}
