// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represent the SIP RAck header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p>Method names are case sensitive. New spec allows for extension methods.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * RAck             =  "RAck" ":" response-num CSeq-num Method
 * response-num     =  1*DIGIT
 * CSeq-num         =  1*DIGIT
 * Method           =  "INVITE" | "ACK" | "OPTIONS" | "BYE" | "CANCEL" | "REGISTER" | extension-method
 * extension-method =  token
 * </pre> </code>
 */
public final class DsSipRAckHeader extends DsSipSeqMethodHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_RACK;
  /** Header ID. */
  public static final byte sID = RACK;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** The CSeq value */
  private long m_lCSeq;

  /** Default constructor. */
  public DsSipRAckHeader() {
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
  public DsSipRAckHeader(byte[] value) throws DsSipParserException, DsSipParserListenerException {
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
  public DsSipRAckHeader(byte[] value, int offset, int count)
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
  public DsSipRAckHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified response number <code>resNumber</code>, CSeq number
   * <code>seqNumber</code> and the specified <code>method</code> name.
   *
   * @param resNumber the response number
   * @param seqNumber the CSeq number
   * @param method the method name
   */
  public DsSipRAckHeader(long resNumber, long seqNumber, DsByteString method) {
    super(resNumber, method);
    validate(seqNumber);
    m_lCSeq = seqNumber;
  }

  /**
   * Constructs this header with the specified response number <code>resNumber</code>, CSeq number
   * <code>seqNumber</code> and the specified <code>method</code> id.
   *
   * @param resNumber the response number
   * @param seqNumber the CSeq number
   * @param method the method id
   */
  public DsSipRAckHeader(long resNumber, long seqNumber, int method) {
    super(resNumber, method);
    validate(seqNumber);
    m_lCSeq = seqNumber;
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name
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
    return BS_RACK_TOKEN;
  }

  /**
   * Sets the specified CSeq number to this header.
   *
   * @param seqNumber the new CSeq number
   */
  public void setCSeqNumber(long seqNumber) {
    validate(seqNumber);
    m_lCSeq = seqNumber;
  }

  /**
   * Retrieves the CSeq number.
   *
   * @return the sequence number
   */
  public long getCSeqNumber() {
    return m_lCSeq;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return RACK;
  }
  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipRAckHeader source = (DsSipRAckHeader) header;
    m_lCSeq = source.m_lCSeq;
  }

  /*
   * This method makes a copy of the header.
   */
  /*
      public Object clone()
      {
          DsSipRAckHeader clone = (DsSipRAckHeader)super.clone();
          clone.m_lCSeq = m_lCSeq;
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
    DsSipRAckHeader header = null;
    try {
      header = (DsSipRAckHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_lCSeq != header.m_lCSeq) {
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
    out.write(DsByteString.getBytes(Long.toString(m_lNumber)));
    out.write(B_SPACE);
    out.write(DsByteString.getBytes(Long.toString(m_lCSeq)));
    if (m_strMethod != null) {
      out.write(B_SPACE);
      m_strMethod.write(out);
    }
  }

  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case CSEQ_METHOD:
        setMethod(new DsByteString(buffer, offset, count));
        break;
      case CSEQ_NUMBER:
        setCSeqNumber(DsSipMsgParser.parseLong(buffer, offset, count));
        break;
      case RESPONSE_NUMBER:
        setNumber(DsSipMsgParser.parseLong(buffer, offset, count));
        break;
    }
  }
}
