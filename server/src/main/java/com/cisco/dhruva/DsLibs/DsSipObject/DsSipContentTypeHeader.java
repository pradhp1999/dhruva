// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * This class represents the Content-Type header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Content-Type =  ( "Content-Type" | "c" ) ":" media-type
 * media-type   =  type "/" subtype *( ";" parameter )
 * type         =  token
 * subtype      =  token
 * parameter    =  attribute "=" value        ; is value optional? We treat it that way.
 * attribute    =  token
 * value        =  token | quoted-string
 * </pre> </code>
 */
public final class DsSipContentTypeHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_CONTENT_TYPE;
  /** Header ID. */
  public static final byte sID = CONTENT_TYPE;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_CONTENT_TYPE_C;

  /** The content type */
  private DsByteString m_strType;
  /** The content subtype */
  private DsByteString m_strSubType;

  /** Default constructor. */
  public DsSipContentTypeHeader() {
    super();
  }

  /**
   * Constructor used to accept a Type and the sub-type.
   *
   * @param type the type
   * @param subType the sub type
   */
  public DsSipContentTypeHeader(DsByteString type, DsByteString subType) {
    super();
    m_strType = type;
    m_strSubType = subType;
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
  public DsSipContentTypeHeader(byte[] value)
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
  public DsSipContentTypeHeader(byte[] value, int offset, int count)
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
  public DsSipContentTypeHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Sets the media type for this header.
   *
   * @param contentType the media type. It should be in the type/subtype format.
   */
  public void setMediaType(DsByteString contentType) {
    // CAFFEINE 2.0 DEVELOPMENT - Take care of null argument in DsSipContentTypeHeader class.
    if (contentType == null) return;
    int i = contentType.indexOf('/');
    if (i != -1) {
      m_strType = contentType.substring(0, i);
      m_strSubType = contentType.substring((i + 1));
    } else {
      m_strType = contentType;
    }
  }

  /**
   * Returns the media type for this header. The returned value will be in type/subtype format.
   *
   * @return the media type for this header.
   */
  public DsByteString getMediaType() {
    int size = (m_strType == null) ? 0 : m_strType.length();
    size += (m_strSubType == null) ? 0 : m_strSubType.length();
    size++; // for '/'
    byte[] bytes = new byte[size];
    size = 0;
    if (m_strType != null) {
      m_strType.appendTo(bytes);
      size = m_strType.length();
    }
    bytes[size] = (byte) '/';
    size++;
    if (m_strSubType != null) {
      m_strSubType.appendTo(bytes, size);
      size = m_strSubType.length();
    }
    return new DsByteString(bytes);
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
    return (isCompact()) ? BS_CONTENT_TYPE_C_TOKEN : BS_CONTENT_TYPE_TOKEN;
  }

  /**
   * Retrieves the content type information.
   *
   * @return the content type.
   */
  public DsByteString getType() {
    return m_strType;
  }

  /**
   * Retrieves the content-subtype information.
   *
   * @return the content sub-type.
   */
  public DsByteString getSubType() {
    return (m_strSubType);
  }

  /**
   * Sets the content-type information.
   *
   * @param atype the content type.
   */
  public void setType(DsByteString atype) {
    m_strType = atype;
  }

  /**
   * Sets the content-sub type information.
   *
   * @param aSubtype the content sub-type.
   */
  public void setSubType(DsByteString aSubtype) {
    m_strSubType = aSubtype;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipContentTypeHeader source = (DsSipContentTypeHeader) header;
    m_strType = source.m_strType;
    m_strSubType = source.m_strSubType;
  }

  /*
   * This method makes a copy of the header.
   */
  /*
      public Object clone()
      {
          DsSipContentTypeHeader clonedHeader = (DsSipContentTypeHeader)super.clone();
          // the following was not done in the 4.* releases:
          clonedHeader.m_strType = m_strType;
          clonedHeader.m_strSubType = m_strSubType;
          return clonedHeader;
      }
  */
  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CONTENT_TYPE;
  }

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

    if (obj == null) {
      return false;
    }
    DsSipContentTypeHeader header = null;
    try {
      header = (DsSipContentTypeHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if ((m_strType == null || m_strType.length() == 0)
        && (header.m_strType == null || header.m_strType.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strType == null || !m_strType.equals(header.m_strType)) {
      return false;
    }

    if ((m_strSubType == null || m_strSubType.length() == 0)
        && (header.m_strSubType == null || header.m_strSubType.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strSubType == null || !m_strSubType.equals(header.m_strSubType)) {
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
    if (m_strType != null) {
      m_strType.write(out);
    }
    if (m_strSubType != null) {
      out.write(B_SLASH);
      m_strSubType.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(getMediaType()).write(out);
    writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strType = null;
    m_strSubType = null;
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
      case TYPE:
        setType(DsByteString.newInstance(buffer, offset, count));
        break;
      case SUB_TYPE:
        setSubType(DsByteString.newInstance(buffer, offset, count));
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipContentTypeHeader
