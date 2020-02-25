// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import gnu.trove.TLinkable;
import gnu.trove.TLinkedList;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/** This class provides the functionality to parse and generate SIP parameters. */
public class DsParameters extends TLinkedList
    implements Serializable, DsSipConstants, Cloneable, DsSipElementListener {
  /** Denotes the delimiter byte in the parameter list. */
  private byte m_bDelimiter = B_SEMI;
  /**
   * Denotes the flag that tells whether the serialized bytes should start with the delimiter byte.
   */
  private boolean m_bStartWithDelimiter = true;

  /**
   * Its the utility method used to convert a char array into String and trim the white spaces.
   *
   * @param ba the character array need to be converted into String
   * @param pStart the start index
   * @param pLen the number of characters need to be converted into String.
   * @return the converted String after removing the white spaces
   */
  public static String trimToString(char[] ba, int pStart, int pLen) {
    int start = pStart, end = pStart + pLen - 1;

    while (Character.isWhitespace(ba[start])) {
      start++;
    }

    while (Character.isWhitespace((ba[end])) || ba[end] == '\0') {
      end--;
    }

    return new String(ba, start, end - start + 1);
  }

  /** Default constructor. Starts with delimiter is <code>true</code> by default. */
  public DsParameters() {
    super();
  }

  /**
   * Constructor that sets the starts with delimiter value.
   *
   * @param flag set to <code>false</code> to serlialize without the delimieter first.
   */
  public DsParameters(boolean flag) {
    super();
    m_bStartWithDelimiter = flag;
  }

  /**
   * Constructs and parses the name-value pairs from the specified String into this object.
   *
   * @param value the input value from where name-value pairs need to be parsed
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsParameters(String value) throws DsSipParserException, DsSipParserListenerException {
    this(DsByteString.getBytes(value));
  }

  /**
   * Constructs and parses the name-value pairs from the specified DsByteString into this object.
   *
   * @param value the input value from where name-value pairs need to be parsed
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsParameters(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs and parses the name-value pairs from the specified byte array into this object.
   *
   * @param value the input byte buffer from where name-value pairs need to be parsed
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsParameters(byte[] value) throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Constructs and parses the name-value pairs from the specified byte array into this object.
   *
   * @param value the input byte buffer from where name-value pairs need to be parsed
   * @param offset the offset of the parameters in the specified buffer
   * @param count the number of bytes to be looked starting from the offset in the specified buffer
   *     for parsing.
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public DsParameters(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    super();
    parse(value, offset, count);
  }

  /**
   * Parses the name-value pairs from the specified String into this object.
   *
   * @param value the input value from where name-value pairs need to be parsed
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public void parse(String value) throws DsSipParserException, DsSipParserListenerException {
    parse(DsByteString.getBytes(value));
  }

  /**
   * Parses the name-value pairs from the specified byte array into this object.
   *
   * @param value the input byte buffer from where name-value pairs need to be parsed
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public void parse(byte[] value) throws DsSipParserException, DsSipParserListenerException {
    parse(value, 0, value.length);
  }

  /**
   * Parses the name-value pairs from the specified byte array into this object.
   *
   * @param value the input byte buffer from where name-value pairs need to be parsed
   * @param offset the offset of the parameters in the specified buffer
   * @param count the number of bytes to be looked starting from the offset in the specified buffer
   *     for parsing.
   * @throws DsSipParserException if there is an error while parsing the parameters
   * @throws DsSipParserListenerException if this object, as parser listener, found some problem
   *     while parsing the parameters.
   */
  public void parse(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipMsgParser.parseParameters(this, UNKNOWN_HEADER, value, offset, count);
  }

  /**
   * Sets the delimiter byte. This is the byte that is used for separating various name-value
   * parameters.
   *
   * @param delimiter the delimiter byte
   * @see #getDelimiter()
   */
  public void setDelimiter(byte delimiter) {
    m_bDelimiter = delimiter;
  }

  /**
   * Returns the delimiter byte.
   *
   * @return the delimiter byte.
   * @see #setDelimiter(byte)
   */
  public byte getDelimiter() {
    return m_bDelimiter;
  }

  /**
   * Tells whether the delimiter will be serialized at the start, while serializing this parameters
   * table.
   *
   * @return <code>true</code> if the delimiter will be serialized at the start, <code>false</code>
   *     otherwise
   * @see #startWithDelimiter(boolean)
   */
  public boolean startsWithDelimiter() {
    return m_bStartWithDelimiter;
  }

  /**
   * Sets whether the delimiter should be serialized at the start, while serializing this parameters
   * table.
   *
   * @param flag if <code>true</code> then the delimiter will be serialized at the start. If <code>
   *     false</code> then delimiter will not be serialized at the start.
   * @see #startsWithDelimiter()
   */
  public void startWithDelimiter(boolean flag) {
    m_bStartWithDelimiter = flag;
  }

  /**
   * Returns a String representation of the parameters separated by the delimiter byte.
   *
   * @return a String representation of the parameters separated by the delimiter byte.
   */
  public DsByteString getValue() {
    return toByteString();
  }

  /**
   * Sets the specified key-value pair as the parameter in this parameter table. If a parameter with
   * the specified key already exists, then replaces that parameter value with this new specified
   * value. The null values are not allowed. If either the key or the value is null, this method
   * will do nothing.
   *
   * @param key the parameter key
   * @param value the parameter value
   */
  public void put(DsByteString key, DsByteString value) {
    if (key != null && value != null) {
      DsParameter param = find(key);
      if (param != null) {
        param.setValue(value);
      } else {
        addLast(new DsParameter(key, value));
      }
    }
  }

  /**
   * Removes the parameter for the specified key, if present.
   *
   * @param key the key whose value is to be removed
   * @return the DsByteString parameter that was removed
   */
  public DsByteString remove(DsByteString key) {
    DsParameter param = (DsParameter) getFirst();
    while (null != param) {

      if (param.equals_key(key)) {
        if (_size == 1) {
          _head = _tail = null;
        } else {
          TLinkable n = param.getNext();
          TLinkable p = param.getPrevious();
          if (null != p) {
            p.setNext(n);
          }
          if (null != n) {
            n.setPrevious(p);
          }
          if (_head == param) {
            _head = n;
          } else if (_tail == param) {
            _tail = p;
          }
        }
        param.setNext(null);
        param.setPrevious(null);
        _size--;
        return param.getValue();
      }
      param = (DsParameter) param.getNext();
    }
    return null;
  }

  /**
   * Returns the value corresponding to the specified <code>key</code>. Returns <code>null</code>,
   * if there is no corresponding value in this table.
   *
   * @param key key of the value to retrieve
   * @return value to be retrieved
   */
  public DsByteString get(DsByteString key) {
    DsParameter param = find(key);
    return (param != null) ? param.getValue() : null;
  }

  /**
   * Returns the value corresponding to the specified <code>key</code>. Returns <code>null</code>,
   * if there is no corresponding value in this table. Use this get method only if you do not
   * already have a DsByteString key and only have a String key.
   *
   * @param key key of the value to retrieve
   * @return value to be retrieved
   */
  public DsByteString get(String key) {
    DsParameter param = find(key);
    return (param != null) ? param.getValue() : null;
  }

  /**
   * Tells whether there is no parameter entry in this table.
   *
   * @return true when there is no value, false otherwise
   */
  public boolean isEmpty() {
    return !(size() > 0);
  }

  /**
   * Tells whether the specified <code>key</code> is present in this table.
   *
   * @param key the key that needs to be searched
   * @return true if present, false otherwise
   */
  public boolean isPresent(DsByteString key) {
    return (find(key) != null);
  }

  /**
   * Serializes these parameters table to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream
   * @throws IOException if there is an error while writing to the output stream
   */
  public void write(OutputStream out) throws IOException {
    boolean first = !m_bStartWithDelimiter;
    DsParameter param = (DsParameter) getFirst();
    while (param != null) {
      if (!first) {
        out.write(m_bDelimiter);
      } else {
        first = false;
      }
      param.key.write(out);
      if (param.value.length() > 0) {
        out.write(B_EQUAL);
        param.value.write(out);
      }
      param = (DsParameter) param.getNext();
    }
  }

  /**
   * Returns a DsByteString representation of the parameters separated by ';'.
   *
   * @return a DsByteString representation of the parameters separated by ';'.
   */
  public DsByteString toByteString() {
    ByteBuffer buffer = ByteBuffer.newInstance();
    try {
      write(buffer);
    } catch (IOException ioe) {
    }
    return buffer.getByteString();
  }
  /**
   * Returns a String representation of the parameters separated by ';'.
   *
   * @return a String representation of the parameters separated by ';'.
   */
  public String toString() {
    return toByteString().toString();
  }

  /**
   * Tells whether the specified DsParameters object is equal to this object.
   *
   * @param toCompare the object to compare to
   * @return <b>True</b> if both are equal <b>False</b> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    DsParameters toCompare = null;
    try {
      toCompare = (DsParameters) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (size() != toCompare.size()) {
      return false;
    }
    DsParameter p1 = (DsParameter) getFirst();
    DsParameter p2 = (DsParameter) toCompare.getFirst();
    DsParameter p3 = null;
    boolean equals = false;
    while (p1 != null) {
      p3 = p2;
      equals = false;
      while (p3 != null) {
        if (p1.equals(p3)) {
          equals = true;
          break;
        }
        p3 = (DsParameter) p3.getNext();
      }
      if (!equals) {
        return false;
      }
      p1 = (DsParameter) p1.getNext();
    }
    return true;
  }

  /**
   * Returns a deep copy of this object.
   *
   * @return the cloned object.
   */
  public Object clone() {
    DsParameters clone = null;
    try {
      clone = (DsParameters) super.clone();
    } catch (CloneNotSupportedException cne) {
    }
    if (clone != null) {
      clone._head = null;
      clone._tail = null;
      clone._size = 0;
      DsParameter param = (DsParameter) _head;
      while (param != null) {
        clone.addLast(param.clone());
        param = (DsParameter) param.getNext();
      }
    }
    return clone;
  }

  /**
   * Clears all the member data and made this object reusable. The various components (sub-elements)
   * of this object can be set again. In this case, we reuse already constructed table.
   */
  public void reInit() {
    clear();
  }

  /**
   * Removes the specified element from this list.
   *
   * <p>Note: This method should be invoked only if the user knows this element is present in this
   * list. This method assumes that this element is present in this list.
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
   * Removes the specified element from this list.
   *
   * <p>Note: This method should be invoked only if the user knows this element is present in this
   * list. This method assumes that this element is present in this list.
   *
   * @param l the element that needs to be replaced from this list.
   * @param m the new element that needs to be placed into this list.
   */
  public void replace(TLinkable l, TLinkable m) {
    if (_head == l && _tail == l) {
      _head = _tail = m;
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
   * Removes the specified element from this list.
   *
   * <p>Note: This method should be invoked only if the user knows this element is present in this
   * list. This method assumes that this element is present in this list.
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
   * @param index an <code>int</code> value
   * @return a <code>ListIterator</code> value
   */
  public ListIterator listIterator(int index) {
    return new Iterator(index);
  }

  /**
   * Returns the value corresponding to the specified <code>key</code>. Returns <code>null</code>,
   * if there is no corresponding value in this table.
   *
   * @param key key of the value to retrieve
   * @return value to be retrieved
   */
  protected DsParameter find(DsByteString key) {
    DsParameter param = (DsParameter) _head;
    while (param != null) {
      if (param.key.equalsIgnoreCase(key)) {
        return param;
      }
      param = (DsParameter) param.getNext();
    }
    return null;
  }

  /**
   * Returns the value corresponding to the specified <code>key</code>. Returns <code>null</code>,
   * if there is no corresponding value in this table.
   *
   * @param key key of the value to retrieve
   * @return value to be retrieved
   */
  protected DsParameter find(String key) {
    DsParameter param = (DsParameter) _head;
    while (param != null) {
      if (param.key.equalsIgnoreCase(key)) {
        return param;
      }
      param = (DsParameter) param.getNext();
    }
    return null;
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
      if (position < 0 || position > DsParameters.this._size)
        throw new IndexOutOfBoundsException(
            "Index: " + position + ", Size: " + DsParameters.this._size);
      _pos = position;
      if (position == 0) {
        _cur = DsParameters.this._head;
      } else if (position == DsParameters.this._size) {
        _cur = null;
      } else if (position < (DsParameters.this._size >> 1)) {
        int pos = 0;
        for (_cur = DsParameters.this._head; pos < position; pos++) {
          _cur = _cur.getNext();
        }
      } else {
        int pos = DsParameters.this._size - 1;
        for (_cur = DsParameters.this._tail; pos > position; pos--) {
          _cur = _cur.getPrevious();
        }
      }
    }

    /**
     * Insert <tt>linkable</tt> at the current position of the iterator. Calling next() after add()
     * will return the added object.
     *
     * @param linkable an object of type TLinkable
     */
    public void add(Object linkable) {
      DsParameters.this.insert(_pos, linkable);
    }

    /**
     * True if a call to next() will return an object.
     *
     * @return a <code>boolean</code> value
     */
    public boolean hasNext() {
      return _pos < DsParameters.this._size;
    }

    /**
     * True if a call to previous() will return a value.
     *
     * @return a <code>boolean</code> value
     */
    public boolean hasPrevious() {
      return _pos > 0;
    }

    /**
     * Returns the value at the Iterator's index and advances the iterator.
     *
     * @return an <code>Object</code> value
     * @throws NoSuchElementException if there is no next element
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
     * @return an <code>Object</code> value
     * @throws NoSuchElementException if there is no previous element.
     */
    public Object previous() {
      // System.out.println("Previous Called, Pos-[" + _pos + ", Last-[" +
      // ((_last==null)?"null":_last.toString())
      //                    + "], Previous-[" +((_cur==null)?"null":_cur.toString()) + "]");
      if (_cur == null) {
        _last = null;
        _cur = DsParameters.this._tail;
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
     * @return an <code>int</code> value
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
      _last = null;
    }

    /**
     * Replaces the current element in the list with <tt>linkable</tt>.
     *
     * @param linkable an object of type TLinkable
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
    put(
        new DsByteString(buffer, nameOffset, nameCount),
        new DsByteString(buffer, valueOffset, valueCount));
  }

  /*
      public static void main(String [] args)
      {
          try
          {
              byte[] bytes = DsSipHeader.read();
              DsParameters parameters = new DsParameters(bytes);
              parameters.startWithDelimiter(false);
              System.out.println();
              System.out.println("<<<<<<<<<<<<<<<<< PARAMETERS >>>>>>>>>>>>>>>>>>>>");
              System.out.println();
              parameters.write(System.out);
              System.out.println();
              System.out.println();
              System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
              System.out.println();
              DsParameters clone = (DsParameters) parameters.clone();
              clone.write(System.out);
              System.out.println();
              System.out.println();
              System.out.println("<<<<<<<<<<<<<<<<< (PARAMETERS == CLONE) = "
                                                      + parameters.equals(clone)
                                                      +" >>>>>>>>>>>>>>>>>>>>");
              System.out.println();
              System.out.println();
              System.out.println("<<<<<<<<<<<<<<<<< (CLONE == PARAMETERS) = "
                                                      + clone.equals(parameters)
                                                      +" >>>>>>>>>>>>>>>>>>>>");
              System.out.println();
          }
          catch(Exception e)
          {
              e.printStackTrace();
          }
      }// Ends main()
  */

} // Ends class DsParameters
