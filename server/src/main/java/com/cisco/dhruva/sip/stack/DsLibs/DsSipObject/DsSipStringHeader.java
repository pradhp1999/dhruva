// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

/** Represents a base class for all the SIP headers that has only one string component. */
public abstract class DsSipStringHeader extends DsSipHeader {
  /** Stores the value of this header as a serialized DsByteString. */
  protected DsByteString m_strValue;

  /** Default constructor. */
  protected DsSipStringHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  protected DsSipStringHeader(byte[] value) {
    this(new DsByteString(value));
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  protected DsSipStringHeader(byte[] value, int offset, int count) {
    this(new DsByteString(value, offset, count));
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  protected DsSipStringHeader(DsByteString value) {
    super();
    setValue(value);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header.
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * Before parsing, this header re-initializes its data members and keep setting its various
   * components as it parses them. If there is an exception during parsing phase, it will set the
   * invalid flag of this header and retain the various components that it already parsed. One
   * should check the valid flag before retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public void parse(byte[] value) {
    parse(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header.
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * Before parsing, this header re-initializes its data members and keep setting its various
   * components as it parses them. If there is an exception during parsing phase, it will set the
   * invalid flag of this header and retain the various components that it already parsed. One
   * should check the valid flag before retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public void parse(byte[] value, int offset, int count) {
    parse(new DsByteString(value, offset, count));
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header.
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * Before parsing, this header re-initializes its data members and keep setting its various
   * components as it parses them. If there is an exception during parsing phase, it will set the
   * invalid flag of this header and retain the various components that it already parsed. One
   * should check the valid flag before retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public void parse(DsByteString value) {
    setValue(value);
  }

  /**
   * Retuns the value of this header. If this header is changed since the last updation of this
   * header's byte string value, then this byte string value is updated again before returning. Also
   * there will be no end of line character or carriage return character at the end of this returned
   * value. <br>
   * Note: Calling this method constructs a new DsByteString object every time. So be cautious.
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValue() {
    return m_strValue;
  }

  /**
   * Sets the value of this header. It will also reset the changed flag to false as we assume that
   * we are setting the updated value of the header.
   *
   * @param value the value of this header.
   */
  public void setValue(String value) {
    setValue(new DsByteString(value));
  }

  /**
   * Sets the byte string value of this header. It will also reset the changed flag to false as we
   * assume that we are setting the updated value of the header.
   *
   * @param value the value of this header.
   */
  public void setValue(DsByteString value) {
    m_strValue = value;
  }

  /**
   * Sets the byte string value of this header. It will also reset the changed flag to false as we
   * assume that we are setting the updated value of the header.
   *
   * @param buffer the byte array containing the value of this header.
   */
  public void setValue(byte[] buffer) {
    setValue(buffer, 0, buffer.length);
  }

  /**
   * Sets the byte string value of this header. It will also reset the changed flag to false as we
   * assume that we are setting the updated value of the header.
   *
   * @param buffer the byte array containing the value of this header.
   * @param off the offset of the value of this header in the specified byte array <code>buffer
   *     </code>.
   * @param count the total number of bytes that comprise the value of this header starting the
   *     offset <code>off</code> in the specified byte array <code>buffer</code>.
   */
  public void setValue(byte[] buffer, int off, int count) {
    setValue(new DsByteString(buffer, off, count));
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(m_strValue).write(out);
  }

  /**
   * Serializes this header in its SIP format. If the flag <code>compact</code> is set, then this
   * header will be serialized with the compact header. name, otherwise full header name will be
   * serialized. Invoke {@link DsSipHeader#setCompact(boolean)} to set or reset this flag.
   *
   * @param out the output stream where this header data (bytes) needs to be serialized.
   * @throws IOException if there is an error while writing to the specified output stream.
   */
  public void write(OutputStream out) throws IOException {
    DsByteString bs = getTokenC();
    if (bs != null) {
      bs.write(out);
    }
    writeValue(out);
    BS_EOH.write(out);
  }

  /**
   * Writes the value of this header to the specified <code>out</code> output stream. By default,
   * this method first update the byte string value of this header, if changed, then writes this
   * byte string value to the output stream.<br>
   * To write directly to the output stream, the sub-classes should override this method and write
   * the header value directly to the specified output stream.
   *
   * @param out the byte output stream where this headers' value will be serialized.
   * @throws IOException if there is an error while writing on to the specified output stream.
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_strValue != null) {
      m_strValue.write(out);
    }
  }

  // CAFFEINE 2.0 DEVELOPMENT - Unit test cases for three new Content-* headers. Fix DsSipConstants
  // definitions.
  protected void copy(DsSipHeader header) {
    DsSipStringHeader source = (DsSipStringHeader) header;
    super.copy(header);
    this.m_strValue = source.m_strValue;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipStringHeader header = null;
    try {
      header = (DsSipStringHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (!DsByteString.equals(m_strValue, header.m_strValue)) {
      return false;
    }
    return true;
  }

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  /**
   * Check if the header contains the specific option tag (such BS_100rel)
   *
   * @param tagValue the tag to be checked against
   * @return the true will be returned if the header contains the tag
   */
  public boolean containsTag(DsByteString tagValue) {
    StringTokenizer st = new StringTokenizer(getValue().toString(), ", ");
    while (st.hasMoreTokens()) {
      String value = st.nextToken();
      if (value != null && value.equals(tagValue.toString())) {
        return true;
      }
    }
    return false;
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
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }
    if (elementId == SINGLE_VALUE) {
      setValue(buffer, offset, count);
    }
  }
} // Ends class DsSipStringHeader
