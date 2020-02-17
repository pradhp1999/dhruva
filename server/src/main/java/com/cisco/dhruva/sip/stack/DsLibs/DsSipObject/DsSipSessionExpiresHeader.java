// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents a Session-Expires header. It provides methods to build, access, modify,
 * serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Session-Expires  =  ("Session-Expires" | "x") ":" delta-seconds [refresher]
 * delta-seconds    =  1*DIGIT
 * refresher        =  ";" "refresher" "=" "uas"|"uac"
 * </pre> </code>
 */
public final class DsSipSessionExpiresHeader extends DsSipDateOrDeltaHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_SESSION_EXPIRES;
  /** Header ID. */
  public static final byte sID = SESSION_EXPIRES;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_SESSION_EXPIRES_C;

  /** The refresher. */
  private DsByteString m_strRefresher;

  /** Default constructor. */
  public DsSipSessionExpiresHeader() {
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
  public DsSipSessionExpiresHeader(byte[] value)
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
  public DsSipSessionExpiresHeader(byte[] value, int offset, int count)
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
  public DsSipSessionExpiresHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified session expires value.
   *
   * @param deltaSeconds the seconds
   */
  public DsSipSessionExpiresHeader(long deltaSeconds) {
    super(deltaSeconds);
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
    return (isCompact()) ? BS_SESSION_EXPIRES_C_TOKEN : BS_SESSION_EXPIRES_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return SESSION_EXPIRES;
  }
  /**
   * Sets the date for expiration.
   *
   * @param date the date information.
   */
  public void setDate(DsDate date) {
    // Does nothing as this header can not have a sip date
  }

  /**
   * Sets the session expires value to the specified value. It will be set only if deltaSeconds >=
   * 0.
   *
   * @param deltaSeconds the number of seconds
   */
  public void setDeltaSeconds(long deltaSeconds) {
    if (deltaSeconds < 0) {
      return;
    }
    m_lDeltaSeconds = deltaSeconds;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header from which data members are copied.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipSessionExpiresHeader source = (DsSipSessionExpiresHeader) header;
    m_strRefresher = source.m_strRefresher;
  }

  /*
   * This method makes a copy of the header.
   */
  /*
      public Object clone()
      {
          DsSipSessionExpiresHeader clone = (DsSipSessionExpiresHeader)super.clone();
          clone.m_strRefresher = m_strRefresher;
          return clone;
      }
  */
  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    DsSipSessionExpiresHeader header = null;
    try {
      header = (DsSipSessionExpiresHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_lDeltaSeconds != header.m_lDeltaSeconds) {
      return false;
    }
    if ((m_strRefresher == null || m_strRefresher.length() == 0)
        && (header.m_strRefresher == null || header.m_strRefresher.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strRefresher == null || !m_strRefresher.equalsIgnoreCase(header.m_strRefresher)) {
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
    out.write(DsIntStrCache.intToBytes(m_lDeltaSeconds));
    if (m_strRefresher != null) {
      out.write(B_SEMI);
      BS_REFRESHER.write(out);
      out.write(B_EQUAL);
      m_strRefresher.write(out);
    }
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strRefresher = null;
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
    if (BS_REFRESHER.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      // can use new lower if uac and uas ever make it to DsByteString
      m_strRefresher = new DsByteString(buffer, valueOffset, valueCount);
    } else {
      super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    }
  }
} // Ends DsSipSessionExpiresHeader
