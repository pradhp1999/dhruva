// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.DsPerf;
import gnu.trove.TLinkable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents a light-weight header object. This header string class contains the header
 * name and header value, and provides the minimum required methods.
 */
public final class DsSipHeaderString extends DsByteString implements DsSipHeaderInterface {
  private TLinkable _next;
  private TLinkable _prev;
  private byte m_bType = DsSipConstants.UNKNOWN_HEADER;
  private DsByteString m_strToken;

  /**
   * Constructs this header string with the specified header <code>id</code>, and the specified
   * <code>value</code>. The name of the header is looked up.
   *
   * @param id the integer header id
   * @param value the value of the header
   */
  public DsSipHeaderString(int id, DsByteString value) {
    this(id, DsSipMsgParser.getHeader(id), value.data, value.offset, value.count);
  }

  /**
   * Constructs this header string with the specified header <code>id</code>, header <code>name
   * </code> and the specified <code>value</code>.
   *
   * @param id the integer header id
   * @param name the name of the header
   * @param value the value of the header
   */
  public DsSipHeaderString(int id, DsByteString name, DsByteString value) {
    this(id, name, value.data, value.offset, value.count);
  }

  /**
   * Constructs this header string with the specified header <code>id</code>, header <code>name
   * </code> and the specified <code>value</code>. Where <code>nameOffset</code> specifies the
   * offset in the <code>name</code> byte array and <code>nameCount</code> specifies the number of
   * bytes in the <code>name</code> byte array that comprise the header name. And <code>offset
   * </code> specifies the offset in the <code>value</code> byte array and <code>count</code>
   * specifies the number of bytes in the <code>value</code> byte array that comprise the header
   * value.
   *
   * @param id the integer header id
   * @param name the byte array containing name of the header
   * @param nameOffset the offset in the <code>name</code> byte array
   * @param nameCount the number of bytes in the <code>name</code> byte array that comprise the
   *     header name.
   * @param value the byte array containing value of the header
   * @param offset the offset in the <code>value</code> byte array
   * @param count the number of bytes in the <code>name</code> byte array that comprise the header
   *     value.
   */
  public DsSipHeaderString(
      int id, byte[] name, int nameOffset, int nameCount, byte[] value, int offset, int count) {
    this(id, new DsByteString(name, nameOffset, nameCount), value, offset, count);
  }

  /**
   * Constructs this header string with the specified header <code>name</code> and the specified
   * <code>value</code>. Where <code>nameOffset</code> specifies the offset in the <code>name</code>
   * byte array and <code>nameCount</code> specifies the number of bytes in the <code>name</code>
   * byte array that comprise the header name. And <code>offset</code> specifies the offset in the
   * <code>value</code> byte array and <code>count</code> specifies the number of bytes in the
   * <code>value</code> byte array that comprise the header value. The header id will be retrieved
   * based on the header name.
   *
   * @param name the byte array containing name of the header
   * @param nameOffset the offset in the <code>name</code> byte array
   * @param nameCount the number of bytes in the <code>name</code> byte array that comprise the
   *     header name.
   * @param value the byte array containing value of the header
   * @param offset the offset in the <code>value</code> byte array
   * @param count the number of bytes in the <code>name</code> byte array that comprise the header
   *     value.
   */
  public DsSipHeaderString(
      byte[] name, int nameOffset, int nameCount, byte[] value, int offset, int count) {
    this(new DsByteString(name, nameOffset, nameCount), value, offset, count);
  }

  /**
   * Constructs this header string with the specified header <code>id</code>, and the specified
   * <code>value</code>. Where <code>offset</code> specifies the offset in the <code>value</code>
   * byte array and <code>count</code> specifies the number of bytes in the <code>value</code> byte
   * array that comprise the header value. The header name will be retrieved based on the header id.
   *
   * @param id the integer header id
   * @param value the byte array containing value of the header
   * @param offset the offset in the <code>value</code> byte array
   * @param count the number of bytes in the <code>name</code> byte array that comprise the header
   *     value.
   */
  public DsSipHeaderString(int id, byte[] value, int offset, int count) {
    this(id, DsSipMsgParser.getHeader(id), value, offset, count);
  }

  /**
   * Constructs this header string with the specified header <code>name</code> and the specified
   * <code>value</code>. Where <code>offset</code> specifies the offset in the <code>value</code>
   * byte array and <code>count</code> specifies the number of bytes in the <code>value</code> byte
   * array that comprise the header value. The header id will be retrieved based on the header name.
   *
   * @param name the name of the header
   * @param value the byte array containing value of the header
   * @param offset the offset in the <code>value</code> byte array
   * @param count the number of bytes in the <code>name</code> byte array that comprise the header
   *     value.
   */
  public DsSipHeaderString(DsByteString name, byte[] value, int offset, int count) {
    this(DsSipMsgParser.getHeader(name), name, value, offset, count);
  }

  /**
   * Constructs this header string with the specified header <code>id</code>, header <code>name
   * </code> and the specified <code>value</code>. Where <code>offset</code> specifies the offset in
   * the <code>value</code> byte array and <code>count</code> specifies the number of bytes in the
   * <code>value</code> byte array that comprise the header value.
   *
   * @param id the integer header id
   * @param name the name of the header
   * @param value the byte array containing value of the header
   * @param offset the offset in the <code>value</code> byte array
   * @param count the number of bytes in the <code>name</code> byte array that comprise the header
   *     value.
   */
  public DsSipHeaderString(int id, DsByteString name, byte[] value, int offset, int count) {
    super(value, offset, count);
    m_strToken = name;
    m_bType = (byte) id;
  }

  /**
   * Constructs this header string with the specified header <code>value</code>. Where <code>offset
   * </code> specifies the offset in the <code>value</code> byte array and <code>count</code>
   * specifies the number of bytes in the <code>value</code> byte array that comprise the header
   * value. The header ID would be UNKNOWN_HEADER and the header name would be UNKNOWN.
   *
   * @param value the byte array containing value of the header
   * @param offset the offset in the <code>value</code> byte array
   * @param count the number of bytes in the <code>name</code> byte array that comprise the header
   *     value.
   */
  public DsSipHeaderString(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Constructs this header string with the specified header <code>value</code>. The header ID would
   * be UNKNOWN_HEADER and the header name would be UNKNOWN.
   *
   * @param value the value of the header
   */
  public DsSipHeaderString(String value) {
    super(value);
  }

  /**
   * Returns the DsSipHeaderString next to this string in the containing TLinkedList. This method is
   * basically required by the TLinkedList, as only then the DsSipHeaderString that implements
   * TLinkable, can be stored in the TLinkedList.
   *
   * @return the DsSipHeaderString next to this string in the containing TLinkedList.
   */
  public final TLinkable getNext() {
    return _next;
  }

  /**
   * Returns the DsSipHeaderString previous to this string in the containing TLinkedList. This
   * method is basically required by the TLinkedList, as only then the DsSipHeaderString that
   * implements TLinkable, can be stored in the TLinkedList.
   *
   * @return the DsSipHeaderString previous to this string in the containing TLinkedList.
   */
  public final TLinkable getPrevious() {
    return _prev;
  }

  /**
   * Sets the DsSipHeaderString next to this string. This method is basically required by the
   * TLinkedList, as only then the DsSipHeaderString that implements TLinkable, can be stored in the
   * TLinkedList. Note: It is recommended not to use this method directly as its usage may corrupt
   * the underlying linkedlist that contains this DsSipHeaderString.
   *
   * @param next the DsSipHeaderString that need to be set as the next DsSipHeaderString object in
   *     the containing TLinkedList of this DsSipHeaderString.
   */
  public final void setNext(TLinkable next) {
    _next = next;
  }

  /**
   * Sets the DsSipHeaderString previous to this string. This method is basically required by the
   * TLinkedList, as only then the DsSipHeaderString that implements TLinkable, can be stored in the
   * TLinkedList. Note: It is recommended not to use this method directly as its usage may corrupt
   * the underlying linkedlist that contains this DsSipHeaderString.
   *
   * @param prev the DsSipHeaderString that need to be set as the previous DsSipHeaderString object
   *     in the containing TLinkedList of this DsSipHeaderString.
   */
  public final void setPrevious(TLinkable prev) {
    _prev = prev;
  }

  /**
   * Returns the clone of this header string.
   *
   * @return the clone of this header string.
   */
  public Object clone() {
    DsSipHeaderString clone = null;
    try {
      clone = (DsSipHeaderString) super.clone();
    } catch (CloneNotSupportedException cne) {
    }
    if (clone != null) {
      clone._next = null;
      clone._prev = null;
    }

    return clone;
  }

  /**
   * Compares the specified string header object with this string header. Returns <code>true</code>
   * if both are equal, otherwise returns <code>false</code>.
   *
   * @param obj the header object that needs to be compared with this header.
   * @return <code>true</code> if the headers are equal, <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    try {
      return equals((DsSipHeaderString) obj);
    } catch (ClassCastException cce) {
    }
    return false;
  }

  /**
   * Compares the specified string header object with this string header. Returns <code>true</code>
   * if both are equal, otherwise returns <code>false</code>.
   *
   * @param header the string header object that needs to be compared with this header.
   * @return <code>true</code> if the headers are equal, <code>false</code> otherwise.
   */
  public boolean equals(DsSipHeaderString header) {
    if (header == this) return true;
    if (null == header) return false;

    if (m_bType != header.m_bType) return false;
    if (m_bType == DsSipConstants.UNKNOWN_HEADER && !equals(m_strToken, header.m_strToken))
      return false;
    if (!equals(this, header)) return false;
    return true;
  }

  /**
   * Validates this header string into a DsSipHeader object. It parses the string value as per the
   * semantics of the corresponding header semantics.
   *
   * @return An instance of DsSipHeader of the corresponding type.
   * @throws DsSipParserException if there is an error while parsing the header string as per the
   *     corresponding header semantics.
   * @throws DsSipParserListenerException if there is an error condition detected by Parser
   *     Listener, while parsing.
   */
  public DsSipHeader validate() throws DsSipParserException, DsSipParserListenerException {
    DsSipHeader header = DsSipHeader.newInstance(m_bType, m_strToken);
    header.parse(data, offset, count);
    return header;
  }

  /**
   * Tells the header ID of this header string.
   *
   * @return the integer header ID value.
   */
  public int getHeaderID() {
    return m_bType;
  }

  /**
   * Returns the complete header name for this header.
   *
   * @return the complete header name for this header.
   */
  public DsByteString getToken() {
    return m_strToken;
  }

  /**
   * Sets the header ID of this header string.
   *
   * @param type the header ID for this header string.
   */
  public void setHeaderID(byte type) {
    m_bType = type;
  }

  /**
   * Sets the header name for this header string.
   *
   * @param token the new header name for this header string
   */
  public void setToken(DsByteString token) {
    m_strToken = token;
  }

  /**
   * Tells whether this header is of the same type as the specified <code>header</code>. The two
   * headers are supposed to be of same type if they have the same header name and same integer
   * header ID.
   *
   * @param header the header whose type needs to be compared with this header
   * @return <code>true</code> if this header is of same type as the specified <code>header</code>,
   *     <code>false</code> otherwise.
   */
  public boolean isType(DsSipHeaderInterface header) {
    return (m_bType == DsSipConstants.UNKNOWN_HEADER)
        ? (m_strToken.equalsIgnoreCase(header.getToken()))
        : (m_bType == header.getHeaderID());
  }

  /**
   * Tells whether the specified token matches the header name of this header. It should consider
   * the compact header name also.
   *
   * @param token the header name to match against.
   * @return <code>true</code> if the specified token matches this header name, <code>false</code>
   *     otherwise.
   */
  public boolean recognize(DsByteString token) {
    if (token != null) {
      if (token.equalsIgnoreCase(m_strToken)
          || (m_bType != DsSipConstants.UNKNOWN_HEADER
              && token.equalsIgnoreCase(DsSipMsgParser.getHeaderCompact(m_bType)))) {
        return true;
      }
    }
    return false;
  }

  public void writeEncoded(OutputStream out, DsTokenSipMessageDictionary md) throws IOException {
    // todo I'm not sure this is right
    md.getEncoding(createCopy(data, offset, count));
  }

  /**
   * Writes this header to the specified <code>out</code> output byte stream in its SIP format. <br>
   * If the global option to serialize the headers in the compact form is set, then this header will
   * be serialized with the compact header name, otherwise full header name will be serialized along
   * with the value. <br>
   * It also serializes the EOH character at the end of the header value. Invoke {@link
   * DsSipHeader#setCompact(boolean)} to set or reset this flag.
   *
   * @param out the output stream where this header data (bytes) needs to be serialized.
   * @throws IOException if there is an error while writing to the specified output stream.
   */
  public void write(OutputStream out) throws IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.HEADER_WRITE);
    DsByteString token = m_strToken;
    if (m_bType != DsSipConstants.UNKNOWN_HEADER) {
      token =
          (DsSipHeader.isCompact())
              ? DsSipMsgParser.getHeaderCompact(m_bType)
              : DsSipMsgParser.getHeader(m_bType);
    }
    if (token != null) {
      token.write(out);
      out.write(DsSipConstants.B_COLON);
      out.write(DsSipConstants.B_SPACE);
    }
    out.write(data, offset, count);
    DsSipConstants.BS_EOH.write(out);
    if (DsPerf.ON) DsPerf.stop(DsPerf.HEADER_WRITE);
  }

  /**
   * Writes the value of this header string to the specified <code>out</code> output stream.
   *
   * @param out the byte output stream where this header string value will be serialized.
   * @throws IOException if there is an error while writing on to the specified output stream.
   */
  public void writeValue(OutputStream out) throws IOException {
    out.write(data, offset, count);
  }

  /**
   * Returns a String representation of this header.
   *
   * @return a String representation of this header.
   */
  public String toString() {
    DsByteString bs = toByteString();
    String s = "";
    if (bs != null) {
      s = bs.toString();
    }
    return s;
  }

  /**
   * Returns a DsByteString representation of this header.
   *
   * @return a DsByteString representation of this header.
   */
  public DsByteString toByteString() {
    ByteBuffer buffer = ByteBuffer.newInstance(100);
    try {
      write(buffer);
    } catch (IOException ioe) {
      // We may never enter in this Exception catch block.
      // May be we were able to write some bytes, before getting Exception.
      // If so return whatever we were able to write.
    }
    return buffer.getByteString();
  }

  /**
   * Tells the form of this header.<br>
   * The form of this header and its sub-classes would always be DsSipHeaderInterface.STRING.
   */
  public int getForm() {
    return STRING;
  }

  /**
   * Appends this header to the specified header list. This header is first cloned before appending
   * to the <code>list</code> if the specified flag <code>clone</code> is <code>true</code>. <br>
   * Note: A DsSipHeaderInterface object can not be a member of two lists simultaneously. One should
   * take care in adding headers to the header list.
   *
   * @param list the header list in the SIP message to which this header needs to be appended.
   * @param clone tells whether this header should be cloned before adding to the specified <code>
   *     list</code>. If <code>true</code>, then the clone of this header is appended to the list,
   *     otherwise this header object itself is appended to the list.
   */
  public void appendToList(DsSipHeaderList list, boolean clone) {
    if (null != list) {
      if (clone) {
        list.addLast(this.clone());
      } else {
        list.addLast(this);
      }
    }
  }

  /**
   * This is an convenient method that is relevant only in case this header is of <code>LIST</code>
   * form. Tells whether this header list is empty or not.
   *
   * @return <code>true</code> if this header list is empty, <code>false</code> otherwise.
   */
  public boolean isEmpty() {
    return false;
  }

  /**
   * Returns the value of this header. There will be no end of line character or carriage return
   * character at the end of this returned value. Note: It every time creates a new DsByteString
   * object but points to the same underlying byte array.
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValue() {
    return new DsByteString(data, offset, count);
  }

  /**
   * Returns the value (No Copy) of this header. There will be no end of line character or carriage
   * return character at the end of this returned value. It actually returns reference to this
   * object itself, which is derived from DsByteString and also overrides the toString() and
   * toByteString() methods. Invoking any of the overridden methods on the returned value will
   * actually invoke the methods on this header string. These methods will return this header name
   * as well as the header value (name: value).
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValueNC() {
    return this;
  }

  /**
   * Returns the int value stored in this header.
   *
   * @return the int value stored in this header
   * @throws DsSipParserException if the value can not be parsed properly.
   * @throws DsSipParserListenerException if there is a problem with the parsed data.
   */
  public int parseIntValue() throws DsSipParserException, DsSipParserListenerException {
    IntHdrListener headerListener = new IntHdrListener();
    // just use any int header type - jsm
    DsSipMsgParser.parseHeader(headerListener, DsSipConstants.MAX_FORWARDS, data, offset, count);

    return headerListener.getValue();
  }
} // Ends class DsSipHeaderString

/** Header listener for parsing. */
final class IntHdrListener implements DsSipHeaderListener, DsSipElementListener {
  private int m_value = -1;

  public IntHdrListener() {}

  public int getValue() {
    return m_value;
  }

  /////////////////////
  // Header Listener //
  /////////////////////

  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    return this;
  }

  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    // ignore
  }

  public void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException {
    // ignore
  }

  //////////////////////
  // Element Listener //
  //////////////////////

  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    // never parse further
    return null;
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    if (elementId == DsSipConstants.SINGLE_VALUE) {
      m_value = DsSipMsgParser.parseInt(buffer, offset, count);
    }
  }

  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    // ignore
  }
}
