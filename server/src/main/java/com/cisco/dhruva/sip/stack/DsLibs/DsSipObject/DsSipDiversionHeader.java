// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the Diversion header as specified in draft-levy-sip-diversion-08. It
 * provides methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Diversion = "Diversion" ":" 1# (name-addr *( ";" diversion_params ))
 * diversion-params    = diversion-reason |
 *                       diversion-counter |
 *                       diversion-limit |
 *                       diversion-privacy |
 *                       diversion-screen |
 *                       diversion-extension
 * diversion-reason    = "reason" "="
 *                       ( "unknown" |
 *                         "user-busy" |
 *                         "no-answer" |
 *                         "unavailable" |
 *                         "unconditional" |
 *                         "time-of-day" |
 *                         "do-not-disturb" |
 *                         "deflection" |
 *                         "follow-me" |
 *                         "out-of-service" |
 *                         "away" |
 *                         token |
 *                         quoted-string )
 * diversion-counter   = "counter" "=" 1*2DIGIT
 * diversion-limit     = "limit" "=" 1*2DIGIT
 * diversion-privacy   = "privacy" "=" ( "full" | "name" | "uri" | "off" | token | quoted-string )
 * diversion-screen    = "screen" "=" ( "yes" | "no" | token | quoted-string )
 * diversion-extension = token ["=" (token | quoted-string)]
 * </pre> </code>
 */
public final class DsSipDiversionHeader extends DsSipNameAddressHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_DIVERSION;
  /** Header ID. */
  public static final byte sID = DIVERSION;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  // CAFFEINE 2.0 Diversion Header development
  private static final DsByteString STR_REASON = new DsByteString(";reason=");
  private static final DsByteString STR_COUNTER = new DsByteString(";counter=");
  private static final DsByteString STR_LIMIT = new DsByteString(";limit=");
  private static final DsByteString STR_SCREEN = new DsByteString(";screen=");
  private static final DsByteString STR_PRIVACY = new DsByteString(";privacy=");

  /** Holds the cid value for this header. */
  protected DsByteString m_reason = null;

  protected int m_counter = -1;
  protected int m_limit = -1;
  protected DsByteString m_screen = null;
  protected DsByteString m_privacy = null;

  /** Default constructor. */
  public DsSipDiversionHeader() {
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
  public DsSipDiversionHeader(byte[] value)
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
  public DsSipDiversionHeader(byte[] value, int offset, int count)
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
  public DsSipDiversionHeader(DsByteString value)
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
  public DsSipDiversionHeader(DsByteString nameAddress, DsParameters parameters)
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
  public DsSipDiversionHeader(DsSipNameAddress nameAddress, DsParameters parameters) {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this header with the specified <code>nameAddress</code>.
   *
   * @param nameAddress the name address for this header.
   */
  public DsSipDiversionHeader(DsSipNameAddress nameAddress) {
    // CAFFEINE 2.0 Diversion Header development
    super();
    this.m_nameAddress = nameAddress;
  }

  /**
   * Constructs this header with the specified <code>uri</code> and the specified <code>parameters
   * </code>.
   *
   * @param uri the uri for this header.
   * @param parameters the list of parameters for this header.
   */
  public DsSipDiversionHeader(DsURI uri, DsParameters parameters) {
    super(uri, parameters);
  }

  /**
   * Constructs this Diversion header with the specified <code>uri</code>.
   *
   * @param uri the uri for this Diversion header.
   * @throws DsException does not throw.
   */
  // CAFFEINE 2.0 Diversion Header development
  public DsSipDiversionHeader(DsURI uri) throws DsException {
    super();
    if (m_nameAddress == null) {
      m_nameAddress = new DsSipNameAddress();
    }
    m_nameAddress.setURI(uri);
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
    return BS_DIVERSION_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return DIVERSION;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  // CAFFEINE 2.0 Diversion Header development
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipDiversionHeader source = (DsSipDiversionHeader) header;
    this.m_reason = source.m_reason;
    this.m_counter = source.m_counter;
    this.m_limit = source.m_limit;
    this.m_screen = source.m_screen;
    this.m_privacy = source.m_privacy;
  }

  /**
   * Returns a deep copy of the header object and all of the other elements on the list that it is
   * associated with. NOTE: This behavior will change when the deprecated methods are removed and it
   * will just clone the single header.
   *
   * @return the cloned JOIN header object
   */
  public Object clone() {
    DsSipDiversionHeader clone = (DsSipDiversionHeader) super.clone();
    clone.m_reason = ((m_reason == null) ? null : m_reason.copy());
    clone.m_counter = m_counter;
    clone.m_limit = m_limit;
    clone.m_screen = ((m_screen == null) ? null : m_screen.copy());
    clone.m_privacy = ((m_privacy == null) ? null : m_privacy.copy());
    return clone;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof DsSipDiversionHeader)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    DsSipDiversionHeader header = null;
    header = (DsSipDiversionHeader) obj;

    if (m_counter != header.m_counter) {
      return false;
    }
    if (m_limit != header.m_limit) {
      return false;
    }
    if ((m_reason == null || m_reason.length() == 0)) {
      if (!(header.m_reason == null || header.m_reason.length() == 0)) {
        return false;
      }
    } else if (!m_reason.equals(header.m_reason)) {
      return false;
    }
    if ((m_screen == null || m_screen.length() == 0)) {
      if (!(header.m_screen == null || header.m_screen.length() == 0)) {
        return false;
      }
    } else if (!m_screen.equals(header.m_screen)) {
      return false;
    }
    if ((m_privacy == null || m_privacy.length() == 0)) {
      if (!(header.m_privacy == null || header.m_privacy.length() == 0)) {
        return false;
      }
    } else if (!m_privacy.equals(header.m_privacy)) {
      return false;
    }
    return true;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_nameAddress != null) {
      m_nameAddress.setBrackets(true);
      m_nameAddress.write(out);
    }
    if (m_reason != null && m_reason.length() > 0) {
      STR_REASON.write(out);
      m_reason.write(out);
    }
    if (m_counter >= 0) {
      STR_COUNTER.write(out);
      out.write(DsByteString.getBytes(Integer.toString(m_counter)));
    }
    if (m_limit >= 0) {
      STR_LIMIT.write(out);
      out.write(DsByteString.getBytes(Integer.toString(m_limit)));
    }
    if (m_screen != null && m_screen.length() > 0) {
      STR_SCREEN.write(out);
      m_screen.write(out);
    }
    if (m_privacy != null && m_privacy.length() > 0) {
      STR_PRIVACY.write(out);
      m_privacy.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  /**
   * Gets the reason parameter value
   *
   * @return the reason value
   */
  public DsByteString getReason() {
    return m_reason;
  }

  /**
   * Sets the reason parameter value
   *
   * @param reason the reason value
   */
  public void setReason(DsByteString reason) {
    this.m_reason = reason;
  }

  /**
   * Gets the counter parameter value
   *
   * @return the counter value
   */
  public int getCounter() {
    return m_counter;
  }

  /**
   * Sets the counter parameter value
   *
   * @param counter the counter value
   */
  public void setCounter(int counter) {
    this.m_counter = counter;
  }

  /**
   * Gets the limit parameter value
   *
   * @return the limit value
   */
  public int getLimit() {
    return m_limit;
  }

  /**
   * Sets the limit parameter value
   *
   * @param limit the limit value
   */
  public void setLimit(int limit) {
    this.m_limit = limit;
  }

  /**
   * Gets the screen parameter value
   *
   * @return the screen value
   */
  public DsByteString getScreen() {
    return m_screen;
  }

  /**
   * Sets the screen parameter value
   *
   * @param screen the screen value
   */
  public void setScreen(DsByteString screen) {
    this.m_screen = screen;
  }

  /**
   * Gets the privacy parameter value
   *
   * @return the privacy value
   */
  public DsByteString getPrivacy() {
    return m_privacy;
  }

  /**
   * Sets the privacy parameter value
   *
   * @param privacy the privacy value
   */
  public void setPrivacy(DsByteString privacy) {
    this.m_privacy = privacy;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (BS_REASON.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_reason = new DsByteString(buffer, valueOffset, valueCount);
    } else if (BS_COUNTER.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      try {
        setCounter(DsSipMsgParser.parseInt(buffer, valueOffset, valueCount));
      } catch (NumberFormatException nfe) {
        throw new DsSipParserListenerException("Exception while constructing the int value: ", nfe);
      }
    } else if (BS_LIMIT.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      try {
        setLimit(DsSipMsgParser.parseInt(buffer, valueOffset, valueCount));
      } catch (NumberFormatException nfe) {
        throw new DsSipParserListenerException("Exception while constructing the int value: ", nfe);
      }
    } else if (BS_SCREEN.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_screen = new DsByteString(buffer, valueOffset, valueCount);
    } else if (BS_PRIVACY.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_privacy = new DsByteString(buffer, valueOffset, valueCount);
    } else {
      super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    }
  }
}
