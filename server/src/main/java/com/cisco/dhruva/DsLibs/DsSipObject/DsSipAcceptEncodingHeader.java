// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents a Accept-Encoding header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header. <br>
 * From HTTP RFC:<br>
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Accept-Encoding  =  "Accept-Encoding" ":" 1#( codings [ ";" "q" "=" qvalue ] )
 * codings          =  ( content-coding | "*" )
 * content-coding   =  token
 * </pre> </code>
 */
public final class DsSipAcceptEncodingHeader extends DsSipHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_ACCEPT_ENCODING;
  /** Header ID. */
  public static final byte sID = ACCEPT_ENCODING;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** <code>true</code> if a '*' is in place of the URI. */
  private boolean m_bWildCard;
  /** Coding string. */
  private DsByteString m_strCoding;
  /** q value parameter. */
  private float qvalue = (float) -1.0;

  /** The default constructor. */
  public DsSipAcceptEncodingHeader() {
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
  public DsSipAcceptEncodingHeader(byte[] value)
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
  public DsSipAcceptEncodingHeader(byte[] value, int offset, int count)
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
  public DsSipAcceptEncodingHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
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
    return BS_ACCEPT_ENCODING_TOKEN;
  }

  /**
   * Checks if there is a wild card entry in the header.
   *
   * @return <code>true</code> if there is a wildcard in the stream, <code>false</code> otherwise.
   */
  public boolean isWildCard() {
    return (m_bWildCard);
  }

  /**
   * Retrieves the content coding scheme.
   *
   * @return the content coding.
   * @see #setContentCoding
   */
  public DsByteString getContentCoding() {
    return m_strCoding;
  }

  /**
   * Sets the qvalue.
   *
   * @param val the q value for this header.
   * @see #getQValue
   */
  public void setQValue(float val) {
    qvalue = val;
  }

  /**
   * Gets the qvalue.
   *
   * @return the q value.
   * @see #setQValue
   */
  public float getQValue() {
    return qvalue;
  }

  /**
   * Sets the content coding scheme.
   *
   * @param aCoding Content coding to be set
   * @see #getContentCoding
   */
  public void setContentCoding(DsByteString aCoding) {
    if (aCoding != null && aCoding.equals("*")) {
      m_bWildCard = true;
    }
    m_strCoding = aCoding;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipAcceptEncodingHeader source = (DsSipAcceptEncodingHeader) header;

    m_bWildCard = source.m_bWildCard;
    m_strCoding = source.m_strCoding;
    qvalue = source.qvalue;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check.
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipAcceptEncodingHeader header = null;
    try {
      header = (DsSipAcceptEncodingHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_bWildCard != header.m_bWildCard) {
      return false;
    }
    if (!DsByteString.equals(m_strCoding, header.m_strCoding)) {
      return false;
    }
    return true;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return ACCEPT_ENCODING;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_strCoding != null) {
      m_strCoding.write(out);
      if (qvalue >= 0.0 && qvalue <= 1.0) {
        BS_QVALUE.write(out);
        out.write(DsByteString.getBytes(Float.toString(qvalue)));
      }
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(
            m_strCoding.append(BS_QVALUE).append(DsByteString.getBytes(Float.toString(qvalue))))
        .write(out);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed object like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_bWildCard = false;
    m_strCoding = null;
    qvalue = (float) -1.0;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case SINGLE_VALUE:
        setContentCoding(new DsByteString(buffer, offset, count));
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (BS_Q.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      try {
        qvalue = DsSipMsgParser.parseFloat(buffer, valueOffset, valueCount);
      } catch (NumberFormatException nfe) {
      }
    }
  }
} // Ends class DsSipAcceptEncodingHeader
