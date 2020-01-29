// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * An abstract base class that represents the headers that can contain a sequence number and the SIP
 * method name. For example, CSeq header.
 */
public abstract class DsSipSeqMethodHeader extends DsSipSequenceHeader {
  /** Represents the sequence method name in this header. */
  protected DsByteString m_strMethod;

  /** Default constructor. */
  protected DsSipSeqMethodHeader() {
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
  protected DsSipSeqMethodHeader(byte[] value)
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
  protected DsSipSeqMethodHeader(byte[] value, int offset, int count)
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
  protected DsSipSeqMethodHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Construct this header with the specified sequence number and the method name.
   *
   * @param number the sequence number
   * @param method the method name
   */
  protected DsSipSeqMethodHeader(long number, DsByteString method) {
    super(number);
    m_strMethod = method;
  }

  /**
   * Construct this header with the specified sequence number and the method id.
   *
   * @param number the sequence number
   * @param method the method id
   */
  public DsSipSeqMethodHeader(long number, int method) {
    super(number);
    m_strMethod = DsSipMsgParser.getMethod(method);
  }

  /**
   * Sets the method name for this header.
   *
   * @param method the new method name value.
   */
  public void setMethod(DsByteString method) {
    m_strMethod = method;
  }

  /**
   * Sets the method name for this header having the specified method id. The method name that
   * corresponds to the specified method id will be set to this header.
   *
   * @param method the new method id value.
   */
  public void setMethod(int method) {
    m_strMethod = DsSipMsgParser.getMethod(method);
  }

  /**
   * Returns the method name for this header.
   *
   * @return the method name for this header.
   */
  public DsByteString getMethod() {
    return m_strMethod;
  }

  /**
   * Returns the method id of the method name for this header.
   *
   * @return the method id of the method name for this header.
   */
  public int getMethodID() {
    return DsSipMsgParser.getMethod(m_strMethod);
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header from which data members are copied
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipSeqMethodHeader source = (DsSipSeqMethodHeader) header;
    m_strMethod = source.m_strMethod;
  }

  /** This method makes a copy of the header. */
  /*
      public Object clone()
      {
          DsSipSeqMethodHeader clone = (DsSipSeqMethodHeader)super.clone();
          if (m_strMethod != null)
          {
              clone.m_strMethod = (DsByteString)m_strMethod.clone();
          }
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
    if (!super.equals(obj)) {
      return false;
    }
    DsSipSeqMethodHeader header = null;
    try {
      header = (DsSipSeqMethodHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if ((m_strMethod != null) && !m_strMethod.equals(header.m_strMethod)) {
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
    if (m_strMethod != null) {
      out.write(B_SPACE);
      m_strMethod.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    // out.write(DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_CSEQ_HEADER);
    DsTokenSipInteger.write32Bit(out, this.m_lNumber);

    // todo Is it necessary we look at the CSeq method?   Can we check the transaction type???
    int methodId = DsTokenSipMethodDictionary.getEncoding(getMethodID());
    if (methodId != DsTokenSipMethodDictionary.UNKNOWN) {
      out.write(methodId);
    } else {
      md.getEncoding(this.m_strMethod).write(out);
    }
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strMethod = null;
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
      case CSEQ_METHOD:
        m_strMethod = DsByteString.newInstance(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipSeqMethodHeader
