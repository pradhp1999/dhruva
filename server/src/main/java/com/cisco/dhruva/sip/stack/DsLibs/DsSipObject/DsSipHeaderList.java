// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import gnu.trove.TLinkable;
import gnu.trove.TLinkedList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Defines a LinkedList representation of SIP Headers. It provides an efficient and performant
 * LinkedList implementation that derives from the GNU trove TLinkedList. It requires its members to
 * be of DsSipHeaderInterface type. The member elements can be either DsSipHeaderInterface.STRING
 * (represented by {@link DsSipHeaderString}), DsSipHeaderInterface.HEADER (represented by {@link
 * DsSipHeader} or DsSipHeaderInterface.LIST (represented by {@link DsSipHeaderList}).
 *
 * <p>Note that no one DsSipHeaderInterface element can be part of two lists at the same time. For
 * example, if <code>a</code> is an element of list <code>A</code>, then to add this element <code>a
 * </code> to another list <code>B</code>, the element <code>a</code> should be removed from list
 * <code>A</code> first. Also any element that needs to be added in a list, the next and previous
 * pointers to that element should be <code>null</code>. Refer {@link
 * DsSipHeaderInterface#setNext(TLinkable)} and {@link DsSipHeaderInterface#setPrevious(TLinkable)}.
 */
public class DsSipHeaderList extends TLinkedList
    implements DsSipHeaderListener, DsSipConstants, Cloneable, DsSipHeaderInterface, Serializable {
  /**
   * The specified header string may contain more than one header that are separated by comma. This
   * method tries to split these comma separated headers into individual headers and returns a list
   * of DsSipHeaderString objects.
   *
   * @param headers the header string that may contain more than one comma separated headers and
   *     needs to be split into a list of individual header strings.
   * @return a list of individual DsSipHeaderString objects, extracted from the specified <code>
   *     headers</code> string.
   * @throws DsSipParserException if there is an error while parsing the specified header string
   *     into individual header strings.
   * @throws DsSipParserListenerException if there is an error condition detected by Parser
   *     Listener, while parsing.
   */
  public static TLinkedList split(DsSipHeaderString headers)
      throws DsSipParserException, DsSipParserListenerException {
    Splitter splitter = Splitter.newInstance();
    return splitter.split(headers, headers.getHeaderID());
  }

  /**
   * Constructs the header list with the UNKNOWN_HEADER type. <br>
   * There won't be any limit to the number of headers that can be parsed into this header list.
   */
  public DsSipHeaderList() {
    this(-1, UNKNOWN_HEADER, BS_UNKNOWN);
  }

  /**
   * Constructs the header list with the specified header <code>type</code>. The name of headers
   * that will be members of this list would be retrieved from the specified header <code>type
   * </code> by invoking {@link DsSipMsgParser#getHeader(int)}. <br>
   * There won't be any limit to the number of headers that can be parsed into this header list.
   *
   * @param type the header ID of the type of headers that will be members of this list
   */
  public DsSipHeaderList(int type) {
    this(-1, type, DsSipMsgParser.getHeader(type));
  }

  /**
   * Constructs the header list with the specified header <code>type</code> and the specified header
   * name <code>token</code>. There won't be any limit to the number of headers that can be parsed
   * into this header list. <br>
   *
   * @param type the header ID of the type of headers that will be members of this list
   * @param token the name of the headers that will be members of this list.
   */
  public DsSipHeaderList(int type, DsByteString token) {
    this(-1, type, token);
  }

  /**
   * Constructs the header list with the specified <code>limit</code> and the specified header
   * <code>type</code>. The name of headers that will be members of this list would be retrieved
   * from the specified header <code>type</code> by invoking {@link DsSipMsgParser#getHeader(int)}.
   * <br>
   * The limit specifies the number of headers that will be added to this list while parsing the
   * headers from a byte array or byte stream.
   *
   * @param limit the max number of headers that may be parsed in to this list
   * @param type the header ID of the type of headers that will be members of this list
   */
  public DsSipHeaderList(int limit, int type) {
    this(limit, type, DsSipMsgParser.getHeader(type));
  }

  /**
   * Constructs the header list with the specified <code>limit</code>, the specified header <code>
   * type</code> and the specified header name <code>token</code>. <br>
   * The limit specifies the number of headers that will be added to this list while parsing the
   * headers from a byte array or byte stream.
   *
   * @param limit the max number of headers that may be parsed in to this list
   * @param type the header ID of the type of headers that will be members of this list
   * @param token the name of the headers that will be members of this list.
   */
  public DsSipHeaderList(int limit, int type, DsByteString token) {
    super();
    m_bLimit = (byte) limit;
    m_bType = (byte) type;
    m_strToken = (token == null) ? DsSipMsgParser.getHeader(type) : token;
  }

  /**
   * Tells the header ID of the type of headers that will be present in this list.
   *
   * @return the header ID.
   */
  public int getHeaderID() {
    return m_bType;
  }

  /**
   * Returns the complete header name for the type of headers that will be present in this list.
   *
   * @return the complete header name.
   */
  public DsByteString getToken() {
    return m_strToken;
  }

  /**
   * Tells whether this list contains the same type of headers as the specified <code>header</code>.
   * The two headers are supposed to be of same type if they have the same header name and same
   * integer header ID.
   *
   * @param header the header whose type needs to be compared with this header
   * @return <code>true</code> if this list contains the same type of headers as the specified
   *     <code>header</code>, <code>false</code> otherwise.
   */
  public boolean isType(DsSipHeaderInterface header) {
    return (m_bType == UNKNOWN_HEADER)
        ? (m_strToken.equalsIgnoreCase(header.getToken()))
        : (m_bType == header.getHeaderID());
  }

  /**
   * Tells whether the specified token matches the name of headers in this list. It should consider
   * the compact header name also.
   *
   * @param token the header name to recognize.
   * @return <code>true</code> if the specified token matches the name of headers in this list,
   *     <code>false</code> otherwise.
   */
  public boolean recognize(DsByteString token) {
    if (token != null) {
      if (token.equalsIgnoreCase(m_strToken)
          || (m_bType != UNKNOWN_HEADER
              && token.equalsIgnoreCase(DsSipMsgParser.getHeaderCompact(m_bType)))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the header at the start of this list. If the header at the start of the list is not in
   * the DsSipHeader form (it may be in DsSipHeaderString form), then it tries to parse the header
   * string into the DsSipHeader object of the corresponding type.
   *
   * @return header at the start of this list.
   * @throws DsSipParserException if there is an error while parsing the header string into
   *     DsSipHeader object.
   * @throws DsSipParserListenerException if there is an error condition detected by Parser
   *     Listener, while parsing.
   */
  public DsSipHeader getFirstHeader() throws DsSipParserException, DsSipParserListenerException {
    validate(1, true);
    return (DsSipHeader) getFirst();
  }

  /**
   * Returns the header at the end of this list. If the header at the end of the list is not in the
   * DsSipHeader form (it may be in DsSipHeaderString form), then it tries to parse the header
   * string into the DsSipHeader object of the corresponding type.
   *
   * @return header at the end of this list.
   * @throws DsSipParserException if there is an error while parsing the header string into
   *     DsSipHeader object.
   * @throws DsSipParserListenerException if there is an error condition detected by Parser
   *     Listener, while parsing.
   */
  public DsSipHeader getLastHeader() throws DsSipParserException, DsSipParserListenerException {
    validate(1, false);
    return (DsSipHeader) getLast();
  }

  /**
   * Removes the header at the start/head of this list. The header at the start/head can be in the
   * form of DsSipHeaderString and it may contain more than one headers as comma separated headers.
   * In that case, only the first header in the comma separated headers, will be removed.
   *
   * @return the removed header.
   */
  public DsSipHeaderInterface removeFirstHeader() {
    try {
      // try to split if its not unknown.
      if (m_bType != UNKNOWN_HEADER) split(1, true);
    } catch (DsSipParserException pe) {
    } catch (DsSipParserListenerException ple) {
    }
    return (DsSipHeaderInterface) removeFirst();
  }

  /**
   * Removes the header at the end/tail of this list. The header at the end/tail can be in the form
   * of DsSipHeaderString and it may contain more than one headers as comma separated headers. In
   * that case, only the last header in the comma separated headers, will be removed.
   *
   * @return the removed header.
   */
  public DsSipHeaderInterface removeLastHeader() {
    try {
      // try to split if its not unknown.
      if (m_bType != UNKNOWN_HEADER) split(1, false);
    } catch (DsSipParserException pe) {
    } catch (DsSipParserListenerException ple) {
    }
    return (DsSipHeaderInterface) removeLast();
  }

  public void writeEncoded(OutputStream out, DsTokenSipMessageDictionary md) throws IOException {
    DsSipHeaderInterface link = (DsSipHeaderInterface) _head;
    if (null != link) {
      link.writeEncoded(out, md);
      link = (DsSipHeaderInterface) link.getNext();
      while (null != link) {
        out.write(B_COMMA);
        link.writeEncoded(out, md);
        link = (DsSipHeaderInterface) link.getNext();
      }
    }
  }

  /**
   * Returns the value of this header list. If there are more than one header present in this list,
   * then all the header values will be returned as a single comma separated value. There will be no
   * end of line character or carriage return character at the end of this returned value.
   *
   * @return the DsByteString representation of the value of this header list.
   */
  public DsByteString getValue() {
    ByteBuffer buffer = ByteBuffer.newInstance(128);
    try {
      writeValue(buffer);
    } catch (IOException ioe) {
      // log?
    }
    return buffer.getByteString();
  }

  /**
   * Returns the value of this header list. If there are more than one header present in this list,
   * then all the header values will be returned as a single comma separated value. There will be no
   * end of line character or carriage return character at the end of this returned value. This is
   * an equivalent to invoking {@link #getValue() getValue()} except that it provides the concrete
   * implementations for not to copy or create a new DsByteString before returning the header value.
   * One possible scenario is in {@link DsSipHeaderString} where this method actually returns
   * reference to its object itself, which is derived from DsByteString. It provides for performance
   * but its toString() and toByteString() methods may not work as expected as DsSipHeaderString
   * class overrides the toString() and toByteString() methods. Invoking any of these overridden
   * methods on the returned value will actually invoke the methods on DsSipHeaderString object
   * itself and these methods will return the header name as well as the header value (name: value).
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValueNC() {
    return getValue();
  }

  /**
   * Writes the value of this header list to the specified <code>out</code> output stream. If there
   * are more than one header present in this list, then all the header values will be written as a
   * single comma separated value.
   *
   * @param out the byte output stream where this header list's value will be serialized.
   * @throws IOException if there is an error while writing on to the specified output stream.
   */
  public void writeValue(OutputStream out) throws IOException {
    DsSipHeaderInterface link = (DsSipHeaderInterface) _head;
    if (null != link) {
      link.writeValue(out);
      link = (DsSipHeaderInterface) link.getNext();
      while (null != link) {
        out.write(B_COMMA);
        link.writeValue(out);
        link = (DsSipHeaderInterface) link.getNext();
      }
    }
  }

  /*
      javadoc inherited
  */
  public Object clone() {
    DsSipHeaderList clone = null;
    try {
      clone = (DsSipHeaderList) super.clone();
    } catch (CloneNotSupportedException cne) {
    }
    if (clone != null) {
      clone._head = null;
      clone._tail = null;
      clone._size = 0;
      // CAFFEINE 2.0 DEVELOPMENT - set headerList clone's _prev and _next to null
      //   so that the cloned headerList will not have the linkage with previous list.
      clone._next = null; // break the link -kaiw
      clone._prev = null;
      DsSipHeaderInterface link = (DsSipHeaderInterface) getFirst();
      while (null != link) {
        clone.addLast(link.clone());
        link = (DsSipHeaderInterface) link.getNext();
      }
    }
    return clone;
  }

  /**
   * Writes all the headers in this list to the specified <code>out</code> output byte stream in its
   * SIP format. <br>
   * If the global option to serialize the headers in the compact form is set, then all the headers
   * will be serialized with the compact header name, otherwise full header name will be serialized
   * along with the value. <br>
   * Invoke {@link DsSipHeader#setCompact(boolean)} to set or reset this flag. It also serializes
   * the EOH character at the end of the header value.
   *
   * @param out the output stream where this header data (bytes) needs to be serialized.
   * @throws IOException if there is an error while writing to the specified output stream.
   */
  public void write(OutputStream out) throws IOException {
    DsSipHeaderInterface link = (DsSipHeaderInterface) getFirst();
    while (null != link) {
      link.write(out);
      link = (DsSipHeaderInterface) link.getNext();
    }
  }

  /**
   * Compares the specified list of headers with this header list. Returns <code>true</code> if both
   * are equal, otherwise returns <code>false</code>.
   *
   * @param comparator the headers list object that needs to be compared with this header list.
   * @return <code>true</code> if all the headers are equal, <code>false</code> otherwise.
   */
  public boolean equals(Object comparator) {
    try {
      return equals((DsSipHeaderList) comparator);
    } catch (ClassCastException cce) {
    }
    return false;
  }

  /**
   * Compares the specified list of headers with this header list. Returns <code>true</code> if both
   * are equal, otherwise returns <code>false</code>.
   *
   * @param headers the headers list object that needs to be compared with this header list.
   * @return <code>true</code> if all the headers are equal, <code>false</code> otherwise.
   */
  public boolean equals(DsSipHeaderList headers) {
    if (headers == this) return true;
    if (null == headers) return false;
    try {
      // Validate all the headers as DsSipHeader objects
      // and split any comma separated headers into
      // individual headers.
      validate();
      headers.validate();
    } catch (Exception exc) {
      return false;
    }

    if (size() != headers.size()) {
      return false;
    }

    DsSipHeaderInterface h1 = (DsSipHeaderInterface) getFirst();
    DsSipHeaderInterface h2 = (DsSipHeaderInterface) headers.getFirst();

    while (null != h1) {
      if (!h1.equals(h2)) return false;
      h1 = (DsSipHeaderInterface) h1.getNext();
      h2 = (DsSipHeaderInterface) h2.getNext();
    }
    return true;
  }

  /**
   * Writes all the headers in this list to the specified <code>out</code> output byte stream as
   * comma separated headers. <br>
   * If the global option to serialize the headers in the compact form is set, then the compact
   * header name will be serialized, otherwise full header name will be serialized along with the
   * value. <br>
   * Invoke {@link DsSipHeader#setCompact(boolean)} to set or reset this flag. It also serializes
   * the EOH character at the end of the header value.
   *
   * @param out the output stream where this header data (bytes) needs to be serialized.
   * @throws IOException if there is an error while writing to the specified output stream.
   */
  public void writeCommaSeparated(OutputStream out) throws IOException {
    DsByteString bs =
        (DsSipHeader.isCompact() && (m_bType != UNKNOWN_HEADER))
            ? DsSipMsgParser.getHeaderCompact(m_bType)
            : m_strToken;
    bs.write(out);
    out.write(B_COLON);
    out.write(B_SPACE);
    writeValue(out);
    BS_EOH.write(out);
  }

  /**
   * Returns the byte string representation for this header list.
   *
   * @return the byte string representation for this header list.
   */
  public DsByteString toByteString() {
    ByteBuffer buffer = ByteBuffer.newInstance();
    try {
      write(buffer);
    } catch (IOException exc) {
    }
    return buffer.getByteString();
  }

  /**
   * Returns the string representation for this header list.
   *
   * @return the string representation for this header list.
   */
  public String toString() {
    return toByteString().toString();
  }

  /**
   * Adds all the elements in the specified list to this list. The elements will be added at the
   * start of this list if the specified value <code>start</code> is true, otherwise all the
   * elements will be added at the end of this list. The specified list will be cloned before
   * addition to this list, if the value <code>clone</code> is true. In this case, the elements in
   * the specified list will remain intact. If the list is not cloned then the specified list will
   * be emptied, as any element can be member of only one list at the same instant of time.
   *
   * @param list the header list whose elements needs to be added in this list
   * @param start if <code>true</code> then elements will be added at the start of this list,
   *     otherwise at the end.
   * @param clone if <code>true</code> then the list will be cloned before adding it into this list.
   */
  public void addAll(DsSipHeaderList list, boolean start, boolean clone) {
    if (null == list || list.isEmpty()) {
      return; // nothing we can do
    }
    // clone it, if required
    if (clone) {
      list = (DsSipHeaderList) list.clone();
    }
    if (_size < 1) {
      _head = list._head;
      _tail = list._tail;
      _size = list._size;
    } else {
      if (start) {
        list._tail.setNext(_head);
        _head.setPrevious(list._tail);
        this._head = list._head;
      } else {
        _tail.setNext(list._head);
        list._head.setPrevious(_tail);
        this._tail = list._tail;
      }
      _size += list._size;
    }
    list._head = list._tail = null;
    list._size = 0;
  }

  /**
   * Splits all the elements in this list as single elements. In other words, there may be some
   * elements of the form DsSipHeaderString and that header string may in turn contain more than one
   * header(comma separated headers). This function will split such a header string to multiple
   * header strings leaving each header string containing at the most one header value.
   *
   * @throws DsSipParserException if there is an error while parsing the header string into
   *     individual header strings.
   * @throws DsSipParserListenerException if there is an error condition detected by Parser
   *     Listener, while parsing.
   */
  public void split() throws DsSipParserException, DsSipParserListenerException {
    split(-1, true);
  }

  /**
   * Splits up to the specified number of elements in this list as single elements. In other words,
   * there may be some elements of the form DsSipHeaderString and that header string may in turn
   * contain more than one header(comma separated headers). This function will split such a header
   * string to multiple header strings leaving each header string containing at the most one header
   * value.
   *
   * @param num the number of element up to which we need to iterate through and split any such
   *     string header
   * @param start if <code>true</code> then it iterates from the start, otherwise iterates through
   *     from the end.
   * @throws DsSipParserException if there is an error while parsing the header string into
   *     individual header strings.
   * @throws DsSipParserListenerException if there is an error condition detected by Parser
   *     Listener, while parsing.
   */
  public void split(int num, boolean start)
      throws DsSipParserException, DsSipParserListenerException {
    num = Math.min(_size, num);
    if (num == -1) num = Integer.MAX_VALUE;
    DsSipHeaderInterface link = null;
    if (start) {
      link = (DsSipHeaderInterface) _head;
      while (null != link && num-- >= 0) {
        if (link.getForm() == STRING) {
          TLinkable next = link.getNext();
          replace(link, split((DsSipHeaderString) link));
          link = (DsSipHeaderInterface) next;
        } else {
          link = (DsSipHeaderInterface) link.getNext();
        } // _else _if
      } // _while
    } else {
      link = (DsSipHeaderInterface) _tail;
      while (null != link && num-- >= 0) {
        if (link.getForm() == STRING) {
          TLinkable prev = link.getPrevious();
          replace(link, split((DsSipHeaderString) link));
          link = (DsSipHeaderInterface) prev;
        } else {
          link = (DsSipHeaderInterface) link.getPrevious();
        } // _else _if
      } // _while
    } // _else _if
  }

  /**
   * Validates all the headers in this header list as DsSipHeader objects and split any comma
   * separated headers into individual headers.
   *
   * @throws DsSipParserException if there is an error while parsing the header string into
   *     individual header strings or while parsing a header.
   * @throws DsSipParserListenerException if there is an error condition detected by Parser
   *     Listener, while parsing.
   */
  public void validate() throws DsSipParserException, DsSipParserListenerException {
    validate(-1, true);
  }

  /**
   * Validates the specified number of headers in this list, as DsSipHeader objects and split any
   * comma separated headers into individual headers, from the specified location (start or end).
   *
   * @param num the number of element up to which we need to iterate through and replace any header
   *     string to DsSipHeader object.
   * @param start if <code>true</code> then it iterates from the start, otherwise iterates through
   *     from the end.
   * @throws DsSipParserException if there is an error while parsing the header string into
   *     individual header strings or while parsing a header.
   * @throws DsSipParserListenerException if there is an error condition detected by Parser
   *     Listener, while parsing.
   */
  public void validate(int num, boolean start)
      throws DsSipParserException, DsSipParserListenerException {
    // try to split if its not unknown.
    if (m_bType != UNKNOWN_HEADER) split(num, start);

    num = Math.min(_size, num);
    if (num == -1) num = Integer.MAX_VALUE;

    DsSipHeader header = null;
    DsSipHeaderInterface link = null;
    if (start) {
      link = (DsSipHeaderInterface) _head;
      while (null != link && num-- >= 0) {
        if (link.getForm() == STRING) {
          DsSipHeaderString str = (DsSipHeaderString) link;
          header = DsSipHeader.newInstance(str.getHeaderID(), str.getToken());
          header.parse(str.data(), str.offset(), str.length());
          replace(link, header);
          link = header;
        }
        link = (DsSipHeaderInterface) link.getNext();
      }
    } else {
      link = (DsSipHeaderInterface) _tail;
      while (null != link && num-- >= 0) {
        if (link.getForm() == STRING) {
          DsSipHeaderString str = (DsSipHeaderString) link;
          header = DsSipHeader.newInstance(str.getHeaderID(), str.getToken());
          header.parse(str.data(), str.offset(), str.length());
          replace(link, header);
          link = header;
        }
        link = (DsSipHeaderInterface) link.getPrevious();
      }
    }
  }
  /**
   * Tells the form of this header.<br>
   * The form of this header list would always be DsSipHeaderInterface.LIST.
   */
  public int getForm() {
    return LIST;
  }

  /**
   * Appends all the headers in this list to the specified header list. All the headers in this list
   * will be cloned before appending to the specified <code>list</code>. <br>
   * Note: A DsSipHeaderInterface object can not be a member of two lists simultaneously. One should
   * take care in adding headers to the header list.
   *
   * @param list the header list in the SIP message to which this header needs to be appended.
   */
  public void appendToList(DsSipHeaderList list) {
    appendToList(list, true);
  }

  /**
   * Appends all the headers in this list to the specified header list. All the headers in this list
   * will be cloned before appending to the specified. <br>
   * Note: A DsSipHeaderInterface object can not be a member of two lists simultaneously. One should
   * take care in adding headers to the header list.
   *
   * @param list the header list in the SIP message to which this header needs to be appended.
   * @param clone This parameter is deprecated and is of no significance. Irrespective of this
   *     parameter value, the headers in this header list will be cloned before appending to the
   *     specified header <code>list</code>.
   */
  public void appendToList(DsSipHeaderList list, boolean clone) {
    DsSipHeaderInterface link = (DsSipHeaderInterface) getFirst();
    while (null != link) {
      list.addLast(link.clone());
      link = (DsSipHeaderInterface) link.getNext();
    }
  }

  /**
   * Sorts all the elements in this list as per the specified comparator.
   *
   * @param c the comparator that needs to be used for sorting.
   */
  public void sort(Comparator c) {
    Object a[] = toArray();
    Arrays.sort(a, c);
    clear();
    for (int j = 0; j < a.length; j++) {
      add(j, a[j]);
    }
  }

  /**
   * Returns the element next to this element in its contained list. This method is basically
   * required by the TLinkedList, as only then the DsSipHeaderList that implements TLinkable, can be
   * stored in the TLinkedList.
   *
   * @return the element next to this element, if any, otherwise returns null.
   */
  public final TLinkable getNext() {
    return _next;
  }

  /**
   * Returns the element prior to this element in its contained list. This method is basically
   * required by the TLinkedList, as only then the DsSipHeaderList that implements TLinkable, can be
   * stored in the TLinkedList.
   *
   * @return the element prior to this element, if any, otherwise returns null.
   */
  public final TLinkable getPrevious() {
    return _prev;
  }

  /**
   * Sets the element next to this element in the contained list. This method is basically required
   * by the TLinkedList, as only then the DsSipHeader that implements TLinkable, can be stored in
   * the TLinkedList. Note: It is recommended not to use this method directly as its usage may
   * corrupt the underlying linkedlist that contains this element.
   *
   * @param next the element that need to be set as the next element to this element.
   */
  public final void setNext(TLinkable next) {
    _next = next;
  }

  /**
   * Sets the element previous to this element in the contained list. This method is basically
   * required by the TLinkedList, as only then the DsSipHeader that implements TLinkable, can be
   * stored in the TLinkedList. Note: It is recommended not to use this method directly as its usage
   * may corrupt the underlying linkedlist that contains this element.
   *
   * @param prev the element that need to be set as the previous element to this element.
   */
  public final void setPrevious(TLinkable prev) {
    _prev = prev;
  }

  /**
   * Removes the specified element from this list. Note: This method should be invoked only if the
   * user knows this element is present in this list. This method assumes that this element is
   * present in this list.
   *
   * @param l the element that needs to be removed from this list.
   */
  public void delete(TLinkable l) {
    if (_head == l && _tail == l) {
      _head = _tail = null;
      _size = 0;
    } else if (_head == l) {
      TLinkable n = l.getNext();
      _head = n;
      if (null != n) {
        n.setPrevious(null);
      }
      _size--;
    } else if (_tail == l) {
      TLinkable p = l.getPrevious();
      _tail = p;
      if (null != p) {
        p.setNext(null);
      }
      _size--;
    } else {
      TLinkable n = l.getNext();
      TLinkable p = l.getPrevious();
      if (null != n) {
        n.setPrevious(p);
      }
      if (null != p) {
        p.setNext(n);
      }
      _size--;
    }
    l.setNext(null);
    l.setPrevious(null);
  }

  /**
   * Replaces the specified element <code>l</code> with the other element <code>m</code> in this
   * list. Note: This method should be invoked only if the user knows this element is present in
   * this list. This method assumes that this element is present in this list.
   *
   * @param l the element that needs to be replaced from this list.
   * @param m the new element that needs to be placed into this list.
   */
  public void replace(TLinkable l, TLinkable m) {
    if (_head == l && _tail == l) {
      _head = _tail = m;
      m.setNext(null);
      m.setPrevious(null);
    } else if (_head == l) {
      TLinkable n = l.getNext();
      _head = m;
      m.setNext(n);
      m.setPrevious(null);
      if (null != n) {
        n.setPrevious(m);
      }
    } else if (_tail == l) {
      TLinkable p = l.getPrevious();
      _tail = m;
      m.setNext(null);
      m.setPrevious(p);
      if (null != p) {
        p.setNext(m);
      }
    } else {
      TLinkable n = l.getNext();
      TLinkable p = l.getPrevious();
      m.setNext(n);
      m.setPrevious(p);
      if (null != n) {
        n.setPrevious(m);
      }
      if (null != p) {
        p.setNext(m);
      }
    }
    l.setNext(null);
    l.setPrevious(null);
  }

  /**
   * Removes the specified element from this list. Note: This method should be invoked only if the
   * user knows this element is present in this list. This method assumes that this element is
   * present in this list.
   *
   * @param l the element that needs to be replaced from this list.
   * @param list the new element that needs to be placed into this list.
   */
  public void replace(TLinkable l, TLinkedList list) {
    TLinkable listHead = (TLinkable) list.getFirst();
    TLinkable listTail = (TLinkable) list.getLast();
    int listSize = list.size();
    if (_head == l && _tail == l) {
      _head = listHead;
      _tail = listTail;
    } else if (_head == l) {
      _head = listHead;
      TLinkable next = l.getNext();
      listTail.setNext(next);
      next.setPrevious(listTail);
    } else if (_tail == l) {
      TLinkable prev = l.getPrevious();
      prev.setNext(listHead);
      listHead.setPrevious(prev);
      _tail = listTail;
    } else {
      TLinkable next = l.getNext();
      TLinkable prev = l.getPrevious();
      prev.setNext(listHead);
      listHead.setPrevious(prev);
      next.setPrevious(listTail);
      listTail.setNext(next);
    }
    _size += (listSize - 1);
  }

  /**
   * Returns an iterator positioned at <tt>index</tt>. Assuming that the list has a value at that
   * index, calling next() will retrieve and advance the iterator. Assuming that there is a value
   * before <tt>index</tt> in the list, calling previous() will retrieve it (the value at index - 1)
   * and move the iterator to that position. So, iterating from front to back starts at 0; iterating
   * from back to front starts at <tt>size()</tt>.
   *
   * @param index an <code>int</code> value.
   * @return a <code>ListIterator</code> value.
   */
  public ListIterator listIterator(int index) {
    return new Iterator(index);
  }

  /** A ListIterator that supports additions and deletions. */
  protected final class Iterator implements ListIterator {
    private int _pos = 0;
    private TLinkable _cur;
    private TLinkable _last;

    /**
     * Creates a new <code>Iterator</code> instance positioned at <tt>position</tt>.
     *
     * @param position an <code>int</code> value
     */
    Iterator(int position) {
      if (position < 0 || position > DsSipHeaderList.this._size)
        throw new IndexOutOfBoundsException(
            "Index: " + position + ", Size: " + DsSipHeaderList.this._size);
      _pos = position;
      if (position == 0) {
        _cur = DsSipHeaderList.this._head;
      } else if (position == DsSipHeaderList.this._size) {
        _cur = null;
      } else if (position < (DsSipHeaderList.this._size >> 1)) {
        int pos = 0;
        for (_cur = DsSipHeaderList.this._head; pos < position; pos++) {
          _cur = _cur.getNext();
        }
      } else {
        int pos = DsSipHeaderList.this._size - 1;
        for (_cur = DsSipHeaderList.this._tail; pos > position; pos--) {
          _cur = _cur.getPrevious();
        }
      }
    }

    /**
     * Insert <tt>linkable</tt> at the current position of the iterator. Calling next() after add()
     * will return the added object.
     *
     * @param linkable an object of type TLinkable.
     */
    public void add(Object linkable) {
      DsSipHeaderList.this.insert(_pos, linkable);
    }

    /**
     * True if a call to next() will return an object.
     *
     * @return a <code>boolean</code> value.
     */
    public boolean hasNext() {
      return _pos < DsSipHeaderList.this._size;
    }

    /**
     * True if a call to previous() will return a value.
     *
     * @return a <code>boolean</code> value.
     */
    public boolean hasPrevious() {
      return _pos > 0;
    }

    /**
     * Returns the value at the Iterator's index and advances the iterator.
     *
     * @return an <code>Object</code> value.
     * @throws NoSuchElementException if there is no next element.
     */
    public Object next() {
      // System.out.println("Next Called, Pos-[" + _pos + ", Last-[" +
      // ((_last==null)?"null":_last.toString())
      //                    + "], Next-[" +((_cur==null)?"null":_cur.toString()) + "]");
      if (_cur == null) {
        throw new NoSuchElementException();
      }
      ++_pos;
      _last = _cur;
      _cur = _cur.getNext();
      // System.out.println("-Next Called, Pos-[" + _pos + ", Last-[" +
      // ((_last==null)?"null":_last.toString())
      //                    + "], Next-[" +((_cur==null)?"null":_cur.toString()) + "]");
      return _last;
    }

    /**
     * returns the index of the next node.
     *
     * @return an <code>int</code> value
     */
    public int nextIndex() {
      return _pos;
    }

    /**
     * Returns the value before the Iterator's index and moves the iterator back one index.
     *
     * @return an <code>Object</code> value.
     * @throws NoSuchElementException if there is no previous element.
     */
    public Object previous() {
      // System.out.println("Previous Called, Pos-[" + _pos + ", Last-[" +
      // ((_last==null)?"null":_last.toString())
      //                    + "], Previous-[" +((_cur==null)?"null":_cur.toString()) + "]");
      if (_cur == null) {
        _last = null;
        _cur = DsSipHeaderList.this._tail;
      } else {
        _last = _cur;
        _cur = _cur.getPrevious();
      }
      if (null == _cur) {
        _cur = _last;
        throw new NoSuchElementException();
      }
      --_pos;
      // System.out.println("-Previous Called, Pos-[" + _pos + ", Last-[" +
      // ((_last==null)?"null":_last.toString())
      //                    + "], Previous-[" +((_cur==null)?"null":_cur.toString()) + "]");
      return (_last = _cur);
    }

    /**
     * Returns the previous element's index.
     *
     * @return an <code>int</code> value.
     */
    public int previousIndex() {
      return _pos - 1;
    }

    /** Removes the current element in the list and shrinks its size accordingly. */
    public void remove() {
      if (null == _last) {
        throw new IllegalStateException();
      }
      _cur = _last.getNext();
      delete(_last);
      --_pos;
      _last = null;
    }

    /**
     * Replaces the current element in the list with <tt>linkable</tt>.
     *
     * @param linkable an object of type TLinkable.
     */
    public void set(Object linkable) {
      if (null == _last) {
        throw new IllegalStateException();
      }
      _cur = _last.getNext();
      replace(_last, (TLinkable) linkable);
      _last = null;
    }
  } // Ends ListIterator

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipHeaderListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    header = null;
    if (m_bLimit == 0) // limit is reached, so no more headers
    {
      return null;
    } else {
      header = DsSipHeader.newInstance(headerId);
      if (m_bLimit != -1) m_bLimit--; // If limit is set, then decrement it.
    }
    return header;
  }

  /*
   * javadoc inherited.
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG)
      System.out.println("headerFound - type = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
    if (DsSipMessage.DEBUG)
      System.out.println(
          "headerFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");

    if (header != null) {
      addLast(header);
      header = null;
    }
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
    if (m_bLimit == 0) // limit is reached, so no more headers
    {
      return;
    } else {
      addLast(
          new DsSipUnknownHeader(
              new DsByteString(buffer, nameOffset, nameCount),
              new DsByteString(buffer, valueOffset, valueCount)));
      if (m_bLimit != -1) m_bLimit--; // If limit is set, then decrement it.
    }
  }
  // public final static void main(String args[]) throws Exception
  //     {
  //         byte[] F5 =    ("INVITE sip:UserB@there.com SIP/2.0\r\n" +
  //                         "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
  //                         "Via: SIP/2.0/UDP here.com:5060\r\n" +
  //                         "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //                         "From: BigGuy <sip:UserA@here.com>\r\n" +
  //                         "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //                         "Call-ID: 12345601@here.com\r\n" +
  //                         "CSeq: 1 INVITE\r\n" +
  //                         "Contact: <sip:UserA@100.101.102.103>;q=0.4\r\n" +
  //                         "Contact: <sip:UserA@100.101.102.103>;q=0.1\r\n" +
  //                         "Contact: <sip:UserA@100.101.102.103>;q=0.7\r\n" +
  //                         "Contact: <sip:UserA@100.101.102.103>;q=0.2\r\n" +
  //                         "Content-Type: application/sdp\r\n" +
  //                         "Content-Length: 147\r\n" +
  //                         "\r\n" +
  //                         "v=0\r\n" +
  //                         "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //                         "s=Session SDP\r\n" +
  //                         "c=IN IP4 100.101.102.103\r\n" +
  //                         "t=0 0\r\n" +
  //                         "m=audio 49172 RTP/AVP 0\r\n" +
  //                         "a=rtpmap:0 PCMU/8000\r\n").getBytes();
  //         DsSipMessage msg = DsSipMessage.createMessage(F5);

  //         DsSipHeaderList contacts =  msg.getHeadersValidate(DsSipConstants.CONTACT);
  //         System.out.println(contacts);
  //         contacts.sort(new contactHeaderComparator());
  //         System.out.println(contacts);
  //     }

  private byte m_bLimit; // limit = -1, means no limit
  private byte m_bType;
  private DsByteString m_strToken;
  private DsSipHeader header;
  private TLinkable _next;
  private TLinkable _prev;
}

final class Splitter implements DsSipHeaderListener, DsSipElementListener {
  private static ThreadLocal factory = new SplitterInitializer();
  TLinkedList headers;

  static Splitter newInstance() {
    Splitter splitter = (Splitter) factory.get();
    splitter.reset();
    return splitter;
  }

  TLinkedList split(DsByteString value, int type)
      throws DsSipParserException, DsSipParserListenerException {
    if (type == DsSipConstants.UNKNOWN_HEADER) {
      throw new IllegalArgumentException("The UNKNOW_HEADER can not be splitted");
    }
    headers = new TLinkedList();
    DsSipMsgParser.parseHeader(this, type, value.data(), value.offset(), value.length());
    return headers;
  }

  private void reset() {
    headers = null;
  }
  ////////////////////////////////////////////////////////////////////////////////
  // DsSipHeaderListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    return this;
  }

  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("headerFound - type = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
      System.out.println(
          "headerFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
    }
    if (null != headers) {
      DsSipHeaderString hdr = new DsSipHeaderString(headerId, buffer, offset, count);
      headers.addLast(hdr);
    }
  }

  public void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException {
    if (null != headers) {
      DsSipHeaderString hdr =
          new DsSipHeaderString(
              DsSipConstants.UNKNOWN_HEADER,
              buffer,
              nameOffset,
              nameCount,
              buffer,
              valueOffset,
              valueCount);
      headers.addLast(hdr);
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

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
}

final class SplitterInitializer extends ThreadLocal {
  protected Object initialValue() {
    return new Splitter();
  }
}

// class contactHeaderComparator implements Comparator{
//     /**
//      * The method implements the compare method of the Interface
//      * Comparator.
//      */
//     public int compare(Object o1 , Object o2)
//     {

//         //if (Log.isEnabledFor(Level.DEBUG))
//         //Log.log(Level.DEBUG, "compare called with " +
//         //DsProxyUtils.headerToString((DsSipContactHeader)o1) +", "
//         //+ DsProxyUtils.headerToString((DsSipContactHeader)o2));

//         float first = ((DsSipContactHeader)o1).getQvalue();
//         float second = ((DsSipContactHeader)o2).getQvalue();

//         if (first < second)
//             return 1;
//         else if (first > second)
//             return -1;
//         else
//                return 0;

//     }
// }
