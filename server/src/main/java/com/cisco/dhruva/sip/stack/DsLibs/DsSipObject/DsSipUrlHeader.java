// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.*;
import java.io.*;

/** Base class for URL based headers. */
public abstract class DsSipUrlHeader extends DsSipParametricHeader {
  /** The URI for this header. */
  protected DsURI m_URI;

  /** Default constructor. */
  protected DsSipUrlHeader() {
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
  protected DsSipUrlHeader(byte[] value) throws DsSipParserException, DsSipParserListenerException {
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
  protected DsSipUrlHeader(byte[] value, int offset, int count)
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
  protected DsSipUrlHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified URI.
   *
   * @param aURI the URI for this header.
   */
  protected DsSipUrlHeader(DsURI aURI) {
    super();
    m_URI = aURI;
  }

  /**
   * Retrieves the URI information.
   *
   * @return the URI.
   */
  public DsURI getURI() {
    return (m_URI);
  }

  /**
   * Sets the URI information.
   *
   * @param aURI the URI.
   */
  public void setURI(DsURI aURI) {
    m_URI = aURI;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy to this.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipUrlHeader source = (DsSipUrlHeader) header;
    m_URI = source.m_URI;
  }

  /** This method makes a copy of the header. */
  public Object clone() {
    DsSipUrlHeader clonedHeader = (DsSipUrlHeader) super.clone();
    clonedHeader.m_URI = m_URI == null ? null : (DsURI) m_URI.clone();
    return clonedHeader;
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
    DsSipUrlHeader header = null;
    try {
      header = (DsSipUrlHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_URI != null) {
      if (header.m_URI == null) {
        return false;
      }
      if (!m_URI.equals(header.m_URI)) {
        return false;
      }
    } else {
      if (header.m_URI != null) {
        return false;
      }
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
    if (m_URI != null) {
      out.write(B_LABRACE);
      m_URI.write(out);
      out.write(B_RABRACE);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    // todo param processing may be broken in URIGenericEncoder

    // The URI Encoder was not working correctly, but the name addr one works, so I am
    // just using that encoder for now - jsm
    DsSipNameAddress nameAddr = new DsSipNameAddress(DsByteString.BS_EMPTY_STRING, getURI());
    nameAddr.setBrackets(true);
    DsTokenSipNameAddressEncoder nameAddrEncoder = new DsTokenSipNameAddressEncoder(nameAddr);

    nameAddrEncoder.writeEncoded(out, md);

    this.writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    if (m_URI != null) {
      m_URI.reInit();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG)
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
    if (DsSipMessage.DEBUG)
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
    if (DsSipMessage.DEBUG) System.out.println();
    switch (elementId) {
      case SIP_URL:
        m_URI = new DsSipURL();
        return m_URI;
      case SIPS_URL:
        m_URI = new DsSipURL(true);
        return m_URI;
      case TEL_URL:
        m_URI = new DsTelURL();
        return m_URI;
      case HTTP_URL:
      case UNKNOWN_URL:
        m_URI = new DsURI();
        return m_URI;
      default:
        return null;
    }
  }
} // Ends class DsSipUrlHeader
