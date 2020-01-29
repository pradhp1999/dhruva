// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the Subscription-Expires header as specified in RFC 3261. It provides
 * methods to access, modify, serialize and clone the header. Subscription-Expires Header grammar:
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Subscription-Expires =  "Subscription-Expires" ":"
 *                            ( SIP-date | delta-seconds )
 *                            *( ";" subexp-params )
 * subexp-params        =  "reason" "=" reason-code | generic-param
 * reason-code          =  "migration" | "maint" | "refused" | "timeout"
 *                           | reason-extension
 * reason-extension     =  token
 * </pre> </code>
 */
public final class DsSipSubscriptionExpiresHeader extends DsSipDateOrDeltaHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_SUBSCRIPTION_EXPIRES;
  /** Header ID. */
  public static final byte sID = SUBSCRIPTION_EXPIRES;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_SUBSCRIPTION_EXPIRES;

  /** Parameters. */
  private DsParameters m_paramTable;

  /** Default constructor. */
  public DsSipSubscriptionExpiresHeader() {
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
  public DsSipSubscriptionExpiresHeader(byte[] value)
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
  public DsSipSubscriptionExpiresHeader(byte[] value, int offset, int count)
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
  public DsSipSubscriptionExpiresHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructor which accepts the seconds.
   *
   * @param aDeltaSeconds the seconds.
   */
  public DsSipSubscriptionExpiresHeader(long aDeltaSeconds) {
    super(aDeltaSeconds);
  }

  /**
   * Constructor with a DsDate.
   *
   * @param pDate the date of expiration.
   */
  public DsSipSubscriptionExpiresHeader(DsDate pDate) {
    super(pDate);
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
    return BS_SUBSCRIPTION_EXPIRES_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return SUBSCRIPTION_EXPIRES;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header from which data members are copied.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipSubscriptionExpiresHeader source = (DsSipSubscriptionExpiresHeader) header;
    m_paramTable = source.m_paramTable;
  }

  /** This method makes a copy of the header. */
  public Object clone() {
    DsSipSubscriptionExpiresHeader clone = (DsSipSubscriptionExpiresHeader) super.clone();
    if (m_paramTable != null) {
      clone.m_paramTable = (DsParameters) m_paramTable.clone();
    }
    return clone;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    DsSipSubscriptionExpiresHeader header = null;
    try {
      header = (DsSipSubscriptionExpiresHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_paramTable != null && header.m_paramTable != null) {
      if (!m_paramTable.equals(header.m_paramTable)) {
        return false;
      }
    } else if (m_paramTable == null && header.m_paramTable != null) {
      if (!header.m_paramTable.isEmpty()) {
        return false;
      }
    } else if (header.m_paramTable == null && m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        return false;
      }
    }
    // else both null - ok
    return true;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    byte[] bytes =
        (m_bDate)
            ? DsByteString.getBytes(m_Date.getDateAsString())
            : DsIntStrCache.intToBytes(m_lDeltaSeconds);
    out.write(bytes);
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    if (m_paramTable != null) {
      m_paramTable.reInit();
    }
  }

  /**
   * Tells whether this header has any parameters.
   *
   * @return <code>true</code> if there are any parameters, <code>false</code> otherwise.
   */
  public boolean hasParameters() {
    return (m_paramTable != null && !m_paramTable.isEmpty());
  }

  /**
   * Returns the parameters that are present in this header.
   *
   * @return the parameters that are present in this header.
   */
  public DsParameters getParameters() {
    return m_paramTable;
  }

  /**
   * Sets the specified parameters for this header. It will override the existing parameters only if
   * the specified parameters object is not null. To remove the parameters from this header use
   * {@link #removeParameters()}.
   *
   * @param paramTable the new parameters object that need to be set for this header.
   */
  public void setParameters(DsParameters paramTable) {
    if (paramTable != m_paramTable) {
      m_paramTable = paramTable;
    }
  }

  /** Removes any existing parameters in this header. */
  public void removeParameters() {
    if (m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        m_paramTable.clear();
      }
      m_paramTable = null;
    }
  }

  /**
   * Tells whether this header contains a parameter with the specified parameter <code>name</code>.
   *
   * @param key the name of the parameter that needs to be checked.
   * @return <code>true</code> if a parameter with the specified name is present, <code>false</code>
   *     otherwise.
   */
  public boolean hasParameter(DsByteString key) {
    return (m_paramTable != null && m_paramTable.isPresent(key));
  }

  /**
   * Returns the parameter value for the parameter with the specified <code>name</code>, if present,
   * otherwise returns null.
   *
   * @param name the name of the parameter that needs to be retrieved
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns null.
   */
  public DsByteString getParameter(DsByteString name) {
    DsByteString value = null;
    if (m_paramTable != null && !m_paramTable.isEmpty()) {
      value = m_paramTable.get(name);
    }
    return value;
  }

  /**
   * Sets the specified name-value parameter in this header. It will override the existing value of
   * the parameter, if already present.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   */
  public void setParameter(DsByteString name, DsByteString value) {
    if (m_paramTable == null) {
      m_paramTable = new DsParameters();
    }
    m_paramTable.put(name, value);
  }

  /**
   * Removes the parameter with the specified <code>name</code>, if present.
   *
   * @param name the name of the parameter that needs to be removed
   */
  public void removeParameter(DsByteString name) {
    if (m_paramTable != null) {
      m_paramTable.remove(name);
    }
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
    setParameter(
        new DsByteString(buffer, nameOffset, nameCount),
        new DsByteString(buffer, valueOffset, valueCount));
  }
} // Ends class DsSipSubscriptionExpiresHeader
