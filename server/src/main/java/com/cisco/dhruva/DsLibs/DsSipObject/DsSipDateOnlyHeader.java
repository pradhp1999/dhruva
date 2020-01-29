// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/** An abstract base class for the headers that contain a SIP date. */
public abstract class DsSipDateOnlyHeader extends DsSipHeader {
  /** Represents SIP date value. */
  protected DsDate m_Date;

  /** Default Constructor. */
  protected DsSipDateOnlyHeader() {
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
  protected DsSipDateOnlyHeader(byte[] value)
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
  protected DsSipDateOnlyHeader(byte[] value, int offset, int count)
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
  protected DsSipDateOnlyHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this Date header with the specified <code>date</code>.
   *
   * @param date the date object that needs to be in this header.
   */
  public DsSipDateOnlyHeader(DsDate date) {
    super();
    setDate(date);
  }

  /**
   * Returns the underlying DsDate object in this header.
   *
   * @return the underlying DsDate object in this header.
   */
  public DsDate getDate() {
    return m_Date;
  }

  /**
   * Sets the underlying DsDate object to the specified <code>date</code>.
   *
   * @param date the new date that needs to be set in this header.
   */
  public void setDate(DsDate date) {
    m_Date = date;
  }

  /**
   * Tries to parse the specified <code>date</code> byte string into a DsDate object and set it to
   * this header.
   *
   * @param date the byte string value of the date that needs to be set to this header.
   * @throws DsSipParserException if there is an error while parsing the specified byte string
   *     <code>date</code> into a DsDate object.
   */
  public void setDate(DsByteString date) throws DsSipParserException {
    setDate(date.toString());
  }

  /**
   * Tries to parse the specified <code>date</code> string into a DsDate object and set it to this
   * header.
   *
   * @param aDate the string value of the date that needs to be set to this header.
   * @throws DsSipParserException if there is an error while parsing the specified string <code>date
   *     </code> into a DsDate object.
   */
  public void setDate(String aDate) throws DsSipParserException {
    DsDate date = new DsDate();
    date.constructDsDate(aDate);
    setDate(date);
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipDateHeader source = (DsSipDateHeader) header;
    m_Date = source.m_Date;
  }

  /**
   * Returns a clone of this header object.
   *
   * @return a clone of this header object.
   */
  public Object clone() {
    DsSipDateOnlyHeader clone = (DsSipDateOnlyHeader) super.clone();
    if (m_Date != null) {
      clone.m_Date = (DsDate) m_Date.clone();
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
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipDateOnlyHeader header = null;
    try {
      header = (DsSipDateOnlyHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_Date != null) {
      if (header.m_Date == null) {
        return false;
      }
      if (!m_Date.equals(header.m_Date)) {
        return false;
      }
    } else {
      if (header.m_Date != null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Writes the value of this header to the specified <code>out</code> output stream.
   *
   * @param out the byte output stream where this headers' value will be serialized.
   * @throws IOException if there is an error while writing on to the specified output stream.
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_Date != null) {
      String s = m_Date.getDateAsString();
      if (null != s) {
        out.write(DsByteString.getBytes(s));
      }
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(m_Date.getDateAsString()).write(out);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_Date = null;
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
    if (elementId == SIP_DATE || elementId == SINGLE_VALUE) {
      // TODO: may be we should be setting the date elements directly.
      try {
        setDate(DsByteString.newString(buffer, offset, count));
      } catch (Exception e) {
        throw new DsSipParserListenerException("Exception while constructing the SIP date", e);
      }
    }
  }
} // Ends class DsSipDateOnlyHeader
