// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import java.io.IOException;
import java.io.OutputStream;

/** Base class for integer based SIP headers. */
public abstract class DsSipIntegerHeader extends DsSipHeader {
  /** Contains the integer value of this header. */
  protected int m_iValue;

  /** Default constructor. */
  protected DsSipIntegerHeader() {
    super();
  }

  /**
   * Constructs this header with the specified integer <code>value</code>.
   *
   * @param value the integer value for this header.
   */
  public DsSipIntegerHeader(int value) {
    super();
    m_iValue = value;
  }

  /**
   * Sets the integer value of this header to the specified integer <code>value</code>.
   *
   * @param value the new integer value for this header.
   */
  public void setIntegerValue(int value) {
    m_iValue = value;
  }

  /**
   * Returns the integer value of this header.
   *
   * @return the integer value of this header.
   */
  public int getIntegerValue() {
    return m_iValue;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipIntegerHeader source = (DsSipIntegerHeader) header;
    m_iValue = source.m_iValue;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check.
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipIntegerHeader header = null;
    try {
      header = (DsSipIntegerHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_iValue != header.m_iValue) {
      return false;
    }
    return true;
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(String.valueOf(m_iValue)).write(out);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    // out.write(DsByteString.getBytes(Integer.toString(m_iValue)));
    out.write(DsIntStrCache.intToBytes(m_iValue));
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
        try {
          m_iValue = DsSipMsgParser.parseInt(buffer, offset, count);
        } catch (NumberFormatException nfe) {
          throw new DsSipParserListenerException(
              "Exception while constructing the numerical value: ", nfe);
        }
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipIntegerHeader
