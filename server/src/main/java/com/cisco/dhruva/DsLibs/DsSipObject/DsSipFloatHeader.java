// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/** An abstract base class for all the headers whose value can be a float value. */
public abstract class DsSipFloatHeader extends DsSipHeader {
  /** Represents the float value of this header. */
  protected float m_fValue;

  /** Default constructor. */
  protected DsSipFloatHeader() {
    super();
  }

  /**
   * Constructs this header with the specified float <code>value</code>.
   *
   * @param value the float value of this header.
   */
  public DsSipFloatHeader(float value) {
    super();
    m_fValue = value;
  }

  /**
   * Sets the float value for this header to the specified <code>value</code>.
   *
   * @param value the new float value of this header.
   */
  public void setFloatValue(float value) {
    m_fValue = value;
  }

  /**
   * Returns the float value of this header.
   *
   * @return the float value of this header.
   */
  public float getFloatValue() {
    return m_fValue;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header from which data members are copied
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipFloatHeader source = (DsSipFloatHeader) header;
    m_fValue = source.m_fValue;
  }

  /*
   * This method makes a copy of the header
   */
  /*
      public Object clone()
      {
          DsSipFloatHeader clone = (DsSipFloatHeader)super.clone();
          clone.m_fValue = m_fValue;
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

    if (obj == null) {
      return false;
    }
    DsSipFloatHeader header = null;
    try {
      header = (DsSipFloatHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_fValue != header.m_fValue) {
      return false;
    }
    return true;
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(Float.toString(m_fValue)).write(out);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    out.write(DsByteString.getBytes(Float.toString(m_fValue)));
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
          m_fValue = DsSipMsgParser.parseFloat(buffer, offset, count);
        } catch (NumberFormatException nfe) {
          throw new DsSipParserListenerException(
              "Exception while constructing the float value: ", nfe);
        }
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipFloatHeader
