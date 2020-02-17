// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipInteger;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An abstract base class that represents the headers that can contain a sequence number. For
 * example, RSeq header.
 */
public abstract class DsSipSequenceHeader extends DsSipHeader {
  private static final long MASK = 0xFFFFFFFF00000000L;

  /** Contains the value of the sequence number for this header. */
  protected long m_lNumber;

  /** Default constructor. */
  protected DsSipSequenceHeader() {
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
  protected DsSipSequenceHeader(byte[] value)
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
  protected DsSipSequenceHeader(byte[] value, int offset, int count)
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
  protected DsSipSequenceHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Construct this RSeq header with the specified sequence number.
   *
   * @param number the sequence number
   */
  protected DsSipSequenceHeader(long number) {
    super();
    setNumber(number);
  }

  /**
   * Sets the sequence number of this header.
   *
   * @param number the new sequence number for this header.
   */
  public void setNumber(long number) {
    validate(number);
    m_lNumber = number;
  }

  /**
   * Returns the sequence number of this header.
   *
   * @return the sequence number of this header.
   */
  public long getNumber() {
    return m_lNumber;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipSequenceHeader source = (DsSipSequenceHeader) header;
    m_lNumber = source.m_lNumber;
  }

  /// **
  // * This method makes a copy of the header.
  // */
  /*
      public Object clone()
      {
          DsSipSequenceHeader clone = (DsSipSequenceHeader)super.clone();
          clone.m_lNumber = m_lNumber;
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
    DsSipSequenceHeader header = null;
    try {
      header = (DsSipSequenceHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_lNumber != header.m_lNumber) {
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
    out.write(DsIntStrCache.intToBytes(m_lNumber));
  }

  /**
   * Validate the specified number as it should not be greater than unsigned 32-bit integer value.
   *
   * @param number the number that needs to be validated.
   */
  protected final void validate(long number) {
    if ((number & MASK) > 0) {
      throw new IllegalArgumentException(
          "[" + number + "] - should not be " + "greater than unsigned 32-bit");
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    DsTokenSipInteger.write32Bit(out, this.m_lNumber);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_lNumber = 0;
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
      case CSEQ_NUMBER:
      case SINGLE_VALUE:
        //            case RES_NUMBER:
        setNumber(DsSipMsgParser.parseLong(buffer, offset, count));
        break;
    }
  }
} // Ends class DsSipSequenceHeader
