// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipHeaderDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import gnu.trove.TLinkable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * This class is the superclass of all SIP headers specified in RFC 3261. When creating header
 * classes of your own, you probably want to inherit from one of the subclasses if this class (or
 * even a further subclass), but you probably do not want to inherit from a leaf class.
 *
 * <p>For performance reasons, each header type is assigned the integer ID. This header ID can be
 * retrieved by calling {@link DsSipHeader#getHeaderID() DsSipHeader.getHeaderID()}. For better
 * performance, use header ID as the parameter when accessing a header, rather than its DsByteString
 * name.
 */
public abstract class DsSipHeader
    implements Serializable,
        Cloneable,
        DsSipConstants,
        DsSipElementListener,
        DsSipHeaderListener,
        DsSipHeaderInterface {
  /** The header factory to create new headers. */
  private static DsSipHeaderFactory s_headerFactory = new DsSipHeaderFactory();

  /**
   * The static flag that specifies whether all the headers (The header classes that are derived
   * from the DsSipHeader class) should be serialized with the compact header. If <code>true</code>
   * then the compact header names will be used while serializing the headers. By default, this flag
   * is off, meaning the headers will be serialized with their full names.
   */
  private static boolean s_bCompact = DsConfigManager.COMPACT_HEADER_USAGE_DEFAULT;

  /**
   * Tells whether this header is parsed already. In case, user calls parse and specify the value to
   * parse. The value may contain more than one header separated by ','. We would be interested in
   * parsing only the first header.
   */
  private boolean m_bParsed;

  /** Next header. */
  private TLinkable _next;
  /** Previous header. */
  private TLinkable _prev;

  /**
   * Sets the default global flag that tells whether the SIP Headers be serialized with their
   * compact form. If set to <code>true</code>, then all the SIP headers will be serialized with
   * their compact header name, otherwise they will be serialized with their full header name.<br>
   * By default, this flag is false and while serializing, the complete header. name is used for all
   * the SIP headers.<br>
   * An application might want to set this flag, for performance considerations.
   *
   * @param compact if <code>true</code>, then all the SIP headers will be serialized with their
   *     compact header name, otherwise they will be serialized with their full header name.
   */
  public static void setCompact(boolean compact) {
    s_bCompact = compact;
  }

  /**
   * Tells whether the SIP Headers be serialized with their compact form. If set to <code>true
   * </code>, then all the SIP headers will be serialized with their compact header name, otherwise
   * they will be serialized with their full header name.<br>
   * By default, this flag is false and while serializing, the complete header. name is used for all
   * the SIP headers.<br>
   * An application might want to set this flag, for performance considerations. To set this flag,
   * use {@link #setCompact(boolean) }.
   *
   * @return <code>true</code> if all the SIP headers serialize with their compact header name,
   *     <code>false</code> otherwise.
   */
  public static boolean isCompact() {
    return s_bCompact;
  }

  /**
   * Utility function that accepts a linked list of DsSipHeader objects and returns a new linked
   * list containing the clones of the DsSipHeader objects contained in the passed in list. Returns
   * <code>null</code> if the passed list <code>headerList</code> is either <code>null</code> or
   * empty.
   *
   * @param headerList the list of headers that needs to be cloned.
   * @return the newly constructed list of clones of the headers that were contained in the passed
   *     list <code>headerList</code>.
   */
  public static LinkedList cloneHeaderList(LinkedList headerList) {
    int size = headerList.size();

    if (headerList == null || size < 1) {
      return null;
    }

    LinkedList list = new LinkedList();

    if (size == 1) {
      list.addLast(((DsSipHeader) headerList.getFirst()).clone());
    } else if (size == 2) {
      list.addLast(((DsSipHeader) headerList.getFirst()).clone());
      list.addLast(((DsSipHeader) headerList.getLast()).clone());
    } else {
      ListIterator iter = headerList.listIterator(0);
      while (iter.hasNext()) {
        list.addLast(((DsSipHeader) iter.next()).clone());
      }
    }

    return list;
  }

  /**
   * Utility function that accepts a linked list of DsSipHeader objects and returns a new linked
   * list containing the clones of the DsSipHeader objects contained in the passed in list. Returns
   * <code>null</code> if the passed list <code>headerList</code> is either <code>null</code> or
   * empty.
   *
   * @param headerList the list of headers that needs to be cloned.
   * @return the newly constructed list of clones of the headers that were contained in the passed
   *     list <code>headerList</code>.
   */
  public static DsSipHeaderList cloneHeaderList(DsSipHeaderList headerList) {
    if (headerList == null || headerList.size() < 1) {
      return null;
    }

    DsSipHeaderList list = new DsSipHeaderList();

    DsSipHeaderInterface header = (DsSipHeaderInterface) headerList.getFirst();
    while (header != null) {
      list.addLast(header.clone());
      header = (DsSipHeaderInterface) header.getNext();
    }
    return list;
  }

  /**
   * Gets the header factory used to create new SIP headers.
   *
   * @return the header factory used to create new SIP headers
   */
  public static DsSipHeaderFactory getHeaderFactory() {
    return s_headerFactory;
  }

  /**
   * Sets the header factory used to create new SIP headers.
   *
   * @param factory the header factory used to create new SIP headers
   */
  public static void setHeaderFactory(DsSipHeaderFactory factory) {
    s_headerFactory = factory;
  }

  /**
   * A factory to create headers of the correct subtype. If the headerId is not known then
   * DsSipUnknown header will be returned.
   *
   * <p>This is just a wrapper for the same method in the DsSipHeaderFactory that was set for this
   * class.
   *
   * @param headerId the header type to create the proper subtype from
   * @return a new DsSipHeader of the proper subclass.
   * @see DsSipHeaderFactory#newInstance(int)
   */
  public static DsSipHeader newInstance(int headerId) {
    return s_headerFactory.newInstance(headerId);
  }

  /**
   * A factory to create headers of the correct subtype. If the headerId is not known then
   * DsSipUnknown header will be returned. With this version of newInstance(), the name of the
   * header will be populated in the unknown header.
   *
   * <p>Note that you should use the int version of this method where possible. This will save a
   * lookup, but use this one if you would have to do the lookup anyway or for unknown headers.
   *
   * <p>This is just a wrapper for the same method in the DsSipHeaderFactory that was set for this
   * class.
   *
   * @param headerName the name of the header to create the proper subtype from
   * @return a new DsSipHeader of the proper subclass.
   * @see DsSipHeaderFactory#newInstance(DsByteString)
   */
  public static DsSipHeader newInstance(DsByteString headerName) {
    return s_headerFactory.newInstance(headerName);
  }

  /**
   * A factory to create headers of the correct subtype. If the headerId is not known then
   * DsSipUnknown header will be returned. With this version of newInstance(), the name of the
   * header will be populated in the unknown header.
   *
   * <p>Note that you should use the int version of this method where possible. This will save a
   * lookup, but use this one if you would have to do the lookup anyway or for unknown headers.
   *
   * <p>This is just a wrapper for the same method in the DsSipHeaderFactory that was set for this
   * class.
   *
   * @param id the ID of the header to create.
   * @param name the name of the header to create the proper subtype from
   * @return a new DsSipHeader of the proper subclass.
   * @see DsSipHeaderFactory#newInstance(int, DsByteString)
   */
  public static DsSipHeader newInstance(int id, DsByteString name) {
    return s_headerFactory.newInstance(id, name);
  }

  /**
   * Parses and creates a linked list of headers of the specified type from the specified byte
   * array. The byte array <code>value</code> should be the value part (data after the colon) of the
   * header(s) and the various headers are separated by ','.<br>
   *
   * @param type the type of header that needs to be parsed. All the known headers are assigned an
   *     integer id (refer {@link DsSipConstants}).
   * @param bytes the value part of the header(s) that needs to be parsed into a list of header.
   * @return the parsed header(s) of the specified type.
   * @throws DsSipParserException if there is an error while parsing the specified value into list
   *     of header(s).
   * @throws DsSipParserListenerException if there is an error condition detected by the header
   *     listener, while parsing.
   */
  public static DsSipHeaderList createHeaderList(int type, byte[] bytes)
      throws DsSipParserListenerException, DsSipParserException {
    return createHeaderList(type, bytes, 0, bytes.length);
  }

  /**
   * Parses and creates a linked list of headers of the specified type from the specified byte
   * array. The byte array <code>value</code> should be the value part (data after the colon) of the
   * header(s) and the various headers are separated by ','.<br>
   *
   * @param type the type of header that needs to be parsed. All the known headers are assigned an
   *     integer id (refer {@link DsSipConstants}).
   * @param bytes the value part of the header(s) that needs to be parsed into a list of header.
   * @param offset the offset in the specified byte array, where from the header(s) value, that
   *     needs to be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     headers.
   * @return the parsed header(s) of the specified type.
   * @throws DsSipParserException if there is an error while parsing the specified value into list
   *     of header(s).
   * @throws DsSipParserListenerException if there is an error condition detected by the header
   *     listener, while parsing.
   */
  public static DsSipHeaderList createHeaderList(int type, byte[] bytes, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipHeaderList headers = new DsSipHeaderList(type);
    DsSipMsgParser.parseHeader((DsSipHeaderListener) headers, type, bytes, offset, count);
    return headers;
  }

  /**
   * Parses and creates a linked list of headers from the specified byte array. The byte array
   * <code>value</code> should be in the (Name: Value) format and the various headers are separated
   * by ','.<br>
   *
   * @param bytes the byte array containing the comma separated headers that need to be parsed into
   *     an header list.
   * @param offset the offset in the specified byte array, where from the header(s) , that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     headers.
   * @return the list of the parsed header(s).
   * @throws DsSipParserException if there is an error while parsing the specified value into list
   *     of header(s).
   * @throws DsSipParserListenerException if there is an error condition detected by the header
   *     listener, while parsing.
   */
  public static DsSipHeaderList createHeaderList(byte[] bytes, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipHeaderList headers = new DsSipHeaderList();
    DsSipMsgParser.parseHeader(headers, bytes, offset, count);
    return headers;
  }

  /**
   * Parses and creates an header of the specified type from the specified byte array. The byte
   * array <code>value</code> should be the value part (data after the colon) of the header.<br>
   * If there are more than one header present in the specified value, say comma separated, then
   * only the first header will be parsed and returned.
   *
   * @param type the type of header that needs to be parsed. All the known headers are assigned an
   *     integer id (refer {@link DsSipConstants}).
   * @param bytes the value part of the header that needs to be parsed.
   * @param offset the offset in the specified byte array, where from the header value, that needs
   *     to be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     header.
   * @return the parsed header of the specified type.
   * @throws DsSipParserException if there is an error while parsing the specified value into into
   *     an header.
   * @throws DsSipParserListenerException if there is an error condition detected by the header
   *     listener, while parsing.
   */
  public static DsSipHeader createHeader(int type, byte[] bytes, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipHeader header = DsSipHeader.newInstance(type);
    header.parse(bytes, offset, count);
    return header;
  }

  /**
   * Parses and creates an header of the specified type from the specified byte array. The byte
   * array <code>value</code> should be the value part (data after the colon) of the header.<br>
   * If there are more than one header present in the specified value, say comma separated, then
   * only the first header will be parsed and returned.
   *
   * @param type the type of header that needs to be parsed. All the known headers are assigned an
   *     integer id (refer {@link DsSipConstants}).
   * @param bytes the value part of the header that needs to be parsed.
   * @return the parsed header of the specified type.
   * @throws DsSipParserException if there is an error while parsing the specified value into into
   *     an header.
   * @throws DsSipParserListenerException if there is an error condition detected by the header
   *     listener, while parsing.
   */
  public static DsSipHeader createHeader(int type, byte[] bytes)
      throws DsSipParserListenerException, DsSipParserException {
    return createHeader(type, bytes, 0, bytes.length);
  }

  /**
   * Parses and creates an header of the specified type from the specified byte array. The byte
   * array <code>value</code> should be the value part (data after the colon) of the header.<br>
   * If there are more than one header present in the specified value, say comma separated, then
   * only the first header will be parsed and returned.
   *
   * @param name the name of the header.
   * @param value the value part of the header that needs to be parsed.
   * @return the parsed header of the specified type.
   * @throws DsSipParserException if there is an error while parsing the specified value into into
   *     an header.
   * @throws DsSipParserListenerException if there is an error condition detected by the header
   *     listener, while parsing.
   */
  public static DsSipHeader createHeader(DsByteString name, DsByteString value)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipHeader header = DsSipHeader.newInstance(name);
    header.parse(value);
    return header;
  }

  /**
   * Parses and creates an header of the specified type from the specified byte array. The byte
   * array <code>value</code> should be the value part (data after the colon) of the header.<br>
   * If there are more than one header present in the specified value, say comma separated, then
   * only the first header will be parsed and returned.
   *
   * @param id the type of header that needs to be parsed. All the known headers are assigned an
   *     integer id (refer {@link DsSipConstants}).
   * @param name the name of the header.
   * @param value the value part of the header that needs to be parsed.
   * @return the parsed header of the specified type.
   * @throws DsSipParserException if there is an error while parsing the specified value into into
   *     an header.
   * @throws DsSipParserListenerException if there is an error condition detected by the header
   *     listener, while parsing.
   */
  public static DsSipHeader createHeader(int id, DsByteString name, DsByteString value)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipHeader header = DsSipHeader.newInstance(id, name);
    header.parse(value);
    return header;
  }

  /**
   * Parses and creates an header from the specified byte array. The byte array <code>value</code>
   * should be in the (Name: Value) format.<br>
   * If there are more than one header present in the specified value, say comma separated, then
   * only the first header will be parsed and returned.
   *
   * @param bytes the byte array containing the header that needs to be parsed.
   * @param offset the offset in the specified byte array, where from the header, that needs to be
   *     parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     header.
   * @return the parsed header.
   * @throws DsSipParserException if there is an error while parsing the specified value into into
   *     an header.
   * @throws DsSipParserListenerException if there is an error condition detected by the header
   *     listener, while parsing.
   */
  public static DsSipHeader createHeader(byte[] bytes, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipHeaderList headers = new DsSipHeaderList(1, UNKNOWN_HEADER);
    DsSipMsgParser.parseHeader(headers, bytes, offset, count);
    DsSipHeader header = (DsSipHeader) headers.getFirst();
    return header;
  }

  /*
  CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
      The origianl super() calling will eventually call down to the child and set child's private date member.

      protected void init() {}
  */

  /** Default constructor. */
  protected DsSipHeader() {
    /*
    CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
    The origianl super() calling will eventually call down to the child and set child's private date member.

    init();
    */
  }

  /*
   CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
       The origianl super() calling will eventually call down to the child and set child's private date member.

  protected DsSipHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException

  protected DsSipHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException

  protected DsSipHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException
   */

  /**
   * Returns the header next to this header. This method is basically required by the TLinkedList,
   * as only then the DsSipHeader that implements TLinkable, can be stored in the TLinkedList.
   *
   * @return the header next to this header, if any, otherwise returns null.
   */
  public final TLinkable getNext() {
    return _next;
  }

  /**
   * Returns the header previous to this header. This method is basically required by the
   * TLinkedList, as only then the DsSipHeader that implements TLinkable, can be stored in the
   * TLinkedList.
   *
   * @return the header previous to this header, if any, otherwise returns null.
   */
  public final TLinkable getPrevious() {
    return _prev;
  }

  /**
   * Sets the header next to this header. This method is basically required by the TLinkedList, as
   * only then the DsSipHeader that implements TLinkable, can be stored in the TLinkedList. Note: It
   * is recommended not to use this method directly as its usage may corrupt the underlying
   * linkedlist that contains this header.
   *
   * @param next the header that need to be set as the next header to this header.
   */
  public final void setNext(TLinkable next) {
    _next = next;
  }

  /**
   * Sets the header previous to this header. This method is basically required by the TLinkedList,
   * as only then the DsSipHeader that implements TLinkable, can be stored in the TLinkedList. Note:
   * It is recommended not to use this method directly as its usage may corrupt the underlying
   * linkedlist that contains this header.
   *
   * @param prev the header that need to be set as the previous header to this header.
   */
  public final void setPrevious(TLinkable prev) {
    _prev = prev;
  }

  /**
   * Tells the form of this header.<br>
   * The form of this header and its sub-classes would always be DsSipHeaderInterface.HEADER.
   */
  public final int getForm() {
    return HEADER;
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
  public final void appendToList(DsSipHeaderList list, boolean clone) {
    if (null != list) {
      if (clone) {
        list.addLast(this.clone());
      } else {
        list.addLast(this);
      }
    }
  }

  /**
   * This is an convenient method that is relevant only in case this header. is of <code>LIST</code>
   * form. Tells whether this header list is empty or not.
   *
   * @return <code>true</code> if this header list is empty, <code>false</code> otherwise.
   */
  public final boolean isEmpty() {
    return false;
  }

  /**
   * Tells whether the specified token matches the header name of this header. It should consider
   * the compact header name also.
   *
   * @param token the header name to recognize.
   * @return <code>true</code> if the specified token matches this header name, <code>false</code>
   *     otherwise.
   */
  public final boolean recognize(DsByteString token) {
    if (token != null) {
      if (token.equalsIgnoreCase(getToken())
          || (getHeaderID() != UNKNOWN_HEADER && token.equalsIgnoreCase(getCompactToken()))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tells whether this header is of the same type as the specified <code>header</code>. The two
   * headers are supposed to be of same type if they have the same header name and same integer
   * header ID.
   *
   * @param header the header whose type needs to be compared with this header.
   * @return <code>true</code> if this header is of same type as the specified <code>header</code>,
   *     <code>false</code> otherwise.
   */
  public final boolean isType(DsSipHeaderInterface header) {
    int id = getHeaderID();
    return (id == UNKNOWN_HEADER)
        ? (getToken().equalsIgnoreCase(header.getToken()))
        : (id == header.getHeaderID());
  }

  /**
   * Returns the complete name of this header.
   *
   * @return the complete name of this header.
   */
  public abstract DsByteString getToken();

  /**
   * Returns the compact name of this header.
   *
   * @return the compact name of this header.
   */
  public abstract DsByteString getCompactToken();

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public abstract DsByteString getTokenC();

  /**
   * Compares the specified header object with this header. Returns <code>true</code> if both are
   * equal, otherwise returns <code>false</code>.
   *
   * @param obj the header object that needs to be compared with this header.
   * @return <code>true</code> if the headers are equal, <code>false</code> otherwise
   */
  public abstract boolean equals(Object obj);

  /**
   * Returns the unique header ID of this header.
   *
   * @return the unique header ID of this header.
   */
  public abstract int getHeaderID();

  /**
   * Returns a deep copy of this header object.
   *
   * @return a deep copy of this header object.
   */
  public Object clone() {
    DsSipHeader clone = null;
    try {
      clone = (DsSipHeader) super.clone();
    } catch (CloneNotSupportedException cne) {
    }
    if (clone != null) {
      clone._next = null;
      clone._prev = null;
    }

    return clone;
  }

  /**
   * Writes this header to the specified <code>out</code> output byte stream in its SIP format. <br>
   * If the global option to serialize the headers in the compact form is set, then this header will
   * be serialized with the compact header name, otherwise full header name will be serialized along
   * with the value. Invoke {@link DsSipHeader#setCompact(boolean)} to set or reset this flag. <br>
   * It also serializes the EOH character at the end of the header value.
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
   * Retuns the value of this header. There will be no end of line character or carriage return
   * character at the end of this returned value. <br>
   * Note: Calling this method constructs a new DsByteString object every time. So be cautious.
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValue() {
    ByteBuffer buffer = ByteBuffer.newInstance(100);
    try {
      writeValue(buffer);
    } catch (IOException ioe) {
      // We may never enter in this Exception catch block.
      // May be we were able to write some bytes, before getting Exception.
      // If so return whatever we were able to write.
    }
    return buffer.getByteString();
  }

  /**
   * Returns the value (No Copy) of this header. There will be no end of line character or carriage
   * return character at the end of this returned value. This is an equivalent to invoking {@link
   * #getValue() getValue()} except that it provides the concrete implementations for not to copy or
   * create a new DsByteString before returning the header value. One possible scenario is in {@link
   * DsSipHeaderString} where this method actually returns reference to its object itself, which is
   * derived from DsByteString. It provides for performance but its toString() and toByteString()
   * methods may not work as expected as DsSipHeaderString class overrides the toString() and
   * toByteString() methods. Invoking any of these overridden methods on the returned value will
   * actually invoke the methods on DsSipHeaderString object itself and these methods will return
   * the header name as well as the header value (name: value).
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValueNC() {
    return getValue();
  }

  public void writeEncodedHeaderName(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    byte[] headerId = DsTokenSipHeaderDictionary.getEncoding(this.getHeaderID());
    if (headerId != null) {
      out.write(headerId);
    } else {
      out.write(DsTokenSipConstants.TOKEN_SIP_UNKNOWN_HEADER);
      md.getEncoding(getToken()).write(out);

      // md.getEncoding(this.getToken()).write(out);
    }
  }

  public void writeEncoded(OutputStream out, DsTokenSipMessageDictionary md) throws IOException {
    writeEncodedHeaderName(out, md);
    writeEncodedValue(out, md);
  }

  public abstract void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException;

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this header can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    m_bParsed = true;
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
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public void parse(byte[] value) throws DsSipParserException, DsSipParserListenerException {
    parse(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header.
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   * Before parsing, this header re-initializes its data members and keep setting its various
   * components as it parses them. If there is an exception during parsing phase, it will set the
   * invalid flag of this header and retain the various components that it already parsed. One
   * should check the valid flag before retrieving the various components of this header. <br>
   * <b>Note:</b> Its the responsibility of the user code to invoke {@link #reInit()} before parsing
   * an already parsed header to avoid the mix-up of the previously parsed elements in this header.
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
  public void parse(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    m_bParsed = false;
    DsSipMsgParser.parseHeader(this, getHeaderID(), value, offset, count);
    m_bParsed = true;
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
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public void parse(DsByteString value) throws DsSipParserException, DsSipParserListenerException {
    parse(value.data(), value.offset(), value.length());
  }

  /**
   * Writes the value of this header to the specified <code>out</code> output stream.
   *
   * @param out the byte output stream where this headers' value will be serialized.
   * @throws IOException if there is an error while writing on to the specified output stream.
   */
  public abstract void writeValue(OutputStream out) throws IOException;

  /**
   * Copy the members of the specified header to this header.
   *
   * @param source the header whose members need to be copied in this header.
   */
  protected void copy(DsSipHeader source) {
    m_bParsed = source.m_bParsed;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println();
    }
    return null;
  }

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
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("parameterFound - contextId = [" + contextId + "]");
      System.out.println(
          "parameterFound - name [offset, count] = [" + nameOffset + " ," + nameCount + "]");
      System.out.println(
          "parameterFound - name = ["
              + DsByteString.newString(buffer, nameOffset, nameCount)
              + "]");
      System.out.println(
          "parameterFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "parameterFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      System.out.println();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipHeaderListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    if (m_bParsed) return null;
    if (DsSipMessage.DEBUG) {
      System.out.println("headerBegin = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
    }
    if (headerId != getHeaderID()) {
      throw new DsSipParserListenerException(
          "Can not parse header other than " + getToken() + " header");
    }
    return this;
  }

  /*
   * javadoc inherited.
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    if (m_bParsed) return;
    if (DsSipMessage.DEBUG) {
      System.out.println("headerFound - type = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
      System.out.println(
          "headerFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
    }
    if (headerId != getHeaderID()) {
      throw new DsSipParserListenerException(
          "Can not parse header other than [" + getToken() + "] header");
    }
    m_bParsed = true;
  }

  /*
   * javadoc inherited.
   */
  public void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException {
    // only useful for unknown headers - ignore
    // override if interested in these events
  }

  /**
   * Utility to read from the System.in and return the byte array on end of System.in Its helpful in
   * debugging.
   *
   * @return an array of bytes read from standard in.
   * @throws IOException if there is an exception reading from the stream.
   */
  protected static final byte[] read() throws IOException {
    int readInt;
    byte[] bytes2Array = null;
    try (ByteBuffer bytes = ByteBuffer.newInstance(1024)) {

      readInt = System.in.read();
      while (readInt != -1) {
        bytes.write(readInt);
        readInt = System.in.read();
      }
      bytes2Array = bytes.toByteArray();
    } catch (IOException ie) {

    }
    return bytes2Array;
  }
} // Ends class DsSipHeader
