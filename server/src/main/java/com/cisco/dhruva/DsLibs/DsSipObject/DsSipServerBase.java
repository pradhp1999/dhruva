// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.util.*;

/**
 * This class represents the abstract base class for the Server and User-Agent headers as specified
 * in RFC 3261. It provides methods to build, access, modify, serialize and clone the header. <br>
 */
public abstract class DsSipServerBase extends DsSipHeader {
  /** List of DsSipServerObjs. */
  protected LinkedList m_list;

  private DsSipServerObj data;

  /** Default constructor. */
  protected DsSipServerBase() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon ) of this
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
  protected DsSipServerBase(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.<br>
   * The byte array <code>value</code> should be the value part (data after the colon ) of this
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
  protected DsSipServerBase(byte[] value, int offset, int count)
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
   * The specified byte string <code>value</code> should be the value part (data after the colon )
   * of this header.<br>
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
  protected DsSipServerBase(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructor used to set the product name, product version and the comment.
   *
   * @param productName the product name.
   * @param productVersion the product version, null OK.
   * @param comment the comment, null OK.
   */
  protected DsSipServerBase(
      DsByteString productName, DsByteString productVersion, DsByteString comment) {
    super();
    DsSipServerObj data = new DsSipServerObj(productName, productVersion, comment);
    m_list = new LinkedList();
    m_list.add(data);
  }

  /**
   * Retrieves the list of DsSipServerObj's for this header. Returns a reference to the internal
   * list, not a copy.
   *
   * @return a LinkedList that contains only DsSipServerObj's, never null.
   */
  public LinkedList getList() {
    if (m_list == null) {
      m_list = new LinkedList();
    }
    // assume change since the list is out of our direct control
    return m_list;
  }

  /**
   * Retrieves a list of elements in this header. This is a copy of the data, and returns LinkedList
   * of DsByteString's only. There are no null or zero length DsByteStrings.
   *
   * @return a LinkedList that contains only DsByteStrings.
   */
  public LinkedList getListOfStrings() {
    LinkedList list = new LinkedList();

    for (ListIterator i = m_list.listIterator(); i.hasNext(); ) {
      DsSipServerObj data = (DsSipServerObj) i.next();

      if (data.productName != null && data.productName.length() > 0) {
        list.add(data.productName);
      }

      if (data.productVersion != null && data.productVersion.length() > 0) {
        list.add(data.productVersion);
      }

      if (data.comment != null && data.comment.length() > 0) {
        list.add(data.comment);
      }
    }

    return list;
  }

  /**
   * Retrieves the product name from the first element in the list.
   *
   * @return the product name.
   */
  public DsByteString getProductName() {
    if (m_list != null && m_list.size() > 0) {
      DsSipServerObj data = (DsSipServerObj) m_list.getFirst();
      return data.productName;
    } else {
      return null;
    }
  }

  /**
   * Retrieves the product version from the first element in the list.
   *
   * @return the product version.
   */
  public DsByteString getProductVersion() {
    if (m_list != null && m_list.size() > 0) {
      DsSipServerObj data = (DsSipServerObj) m_list.getFirst();
      return data.productVersion;
    } else {
      return null;
    }
  }

  /**
   * Retrieves the comment from the first element in the list.
   *
   * @return the comment.
   */
  public DsByteString getComment() {
    if (m_list != null && m_list.size() > 0) {
      DsSipServerObj data = (DsSipServerObj) m_list.getFirst();
      return data.comment;
    } else {
      return null;
    }
  }

  /**
   * Adds an element to the end of the list of elements for this header.
   *
   * @param element the element to add to the end of the list of elements.
   */
  public void addElement(DsSipServerObj element) {
    if (m_list == null) {
      m_list = new LinkedList();
    }
    m_list.add(element);
  }

  /**
   * Sets the list of DsSipServerObj's for this header.
   *
   * @param list the list of only DsSipServerObj's for this header.
   */
  public void setList(LinkedList list) {
    m_list = list;
  }

  /**
   * Sets the product name, for the first element in the list.
   *
   * @param productName the product name.
   */
  public void setProductName(DsByteString productName) {
    if (m_list != null && m_list.size() > 0) {
      DsSipServerObj data = (DsSipServerObj) m_list.getFirst();
      data.productName = productName;
    } else {
      DsSipServerObj data = new DsSipServerObj(productName, null, null);
      m_list = new LinkedList();
      m_list.add(data);
    }
  }

  /**
   * Sets the product version, for the first element in the list.
   *
   * @param productVersion the product version.
   */
  public void setProductVersion(DsByteString productVersion) {
    if (m_list != null && m_list.size() > 0) {
      DsSipServerObj data = (DsSipServerObj) m_list.getFirst();
      data.productVersion = productVersion;
    } else {
      DsSipServerObj data = new DsSipServerObj(null, productVersion, null);
      m_list = new LinkedList();
      m_list.add(data);
    }
  }

  /**
   * Sets the comment, for the first element in the list.
   *
   * @param comment the comment to set.
   */
  public void setComment(DsByteString comment) {
    if (m_list != null && m_list.size() > 0) {
      DsSipServerObj data = (DsSipServerObj) m_list.getFirst();
      data.comment = comment;
    } else {
      DsSipServerObj data = new DsSipServerObj(null, null, comment);
      m_list = new LinkedList();
      m_list.add(data);
    }
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void writeValue(OutputStream out) throws IOException {
    DsByteString data = null;
    if (m_list != null && m_list.size() > 0) {
      boolean isFirst = true;
      ListIterator iter = m_list.listIterator();
      DsSipServerObj obj = null;
      while (iter.hasNext()) {
        if (!isFirst) {
          out.write(B_SPACE);
        }
        obj = (DsSipServerObj) iter.next();
        data = obj.productName;
        if (data != null && data.length() > 0) {
          data.write(out);
        }
        data = obj.productVersion;
        if (data != null && data.length() > 0) {
          out.write(B_SLASH);
          data.write(out);
        }
        data = obj.comment;
        if (data != null && data.length() > 0) {
          if (data.charAt(0) == '(') {
            out.write(B_SPACE);
            data.write(out);
          } else {
            out.write(B_SPACE);
            out.write(B_LBRACE);
            data.write(out);
            out.write(B_RBRACE);
          }
        }
        if (isFirst) {
          isFirst = false;
        }
      } // _while
    } // _if
  }
  /*
      protected void updateValue()
      {
          Vector dataChunks = new Vector();
          DsByteString data =  null;
          int size = 0;
          boolean isFirst = true;
          ListIterator iter = m_list.listIterator();
          DsSipServerObj obj = null;
          while (iter.hasNext())
          {
              obj = (DsSipServerObj)iter.next();
              data = obj.productName;
              if (!isFirst)
              {
                  size += BS_SPACE.appendTo(dataChunks);
              }
              if (data != null && data.length() > 0)
              {
                  size += data.appendTo(dataChunks);
              }
              data = obj.productVersion;
              if (data != null && data.length() > 0)
              {
                  size += BS_SLASH.appendTo(dataChunks);
                  size += data.appendTo(dataChunks);
              }
              data = obj.comment;
              if (data != null && data.length() > 0)
              {
                  if (data.charAt(0) == '(')
                  {
                      size += BS_SPACE.appendTo(dataChunks);
                      size += data.appendTo(dataChunks);
                  }
                  else
                  {
                      size += BS_SPACE.appendTo(dataChunks);
                      size += BS_LBRACE.appendTo(dataChunks);
                      size += data.appendTo(dataChunks);
                      size += BS_RBRACE.appendTo(dataChunks);
                  }
              }
              if (isFirst)
              {
                  isFirst = false;
              }
          }
          byte[] bytes = new byte[size];
          size = 0;
          for (int i = 0; i < dataChunks.size(); i++)
          {
              data = (DsByteString)dataChunks.elementAt(i);
              data.appendTo(bytes,size); // Here size actually serves as offset index
              size += data.length();
          }
          dataChunks.clear();
          dataChunks = null;
          setValue(bytes);
      }
  */
  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipServerBase source = (DsSipServerBase) header;
    m_list = source.m_list;
  }

  /** This method makes a copy of the header. */
  public Object clone() {
    DsSipServerBase clone = (DsSipServerBase) super.clone();
    if (m_list != null) {
      clone.m_list = (LinkedList) m_list.clone();
    }
    return clone;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check.
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    DsSipServerBase header = null;
    try {
      header = (DsSipServerBase) obj;
    } catch (ClassCastException e) {
      return false;
    }
    // Compare lists
    if ((m_list != null) && (m_list.size() > 0)) {
      if ((header.m_list == null) || (header.m_list.size() != m_list.size())) {
        return false;
      }
      Iterator iter1 = m_list.listIterator(0);
      Iterator iter2 = header.m_list.listIterator(0);
      DsSipServerObj so1 = null;
      DsSipServerObj so2 = null;
      while (iter1.hasNext()) {
        so1 = (DsSipServerObj) iter1.next();
        so2 = (DsSipServerObj) iter2.next();

        if (!so1.equals(so2)) {
          return false;
        }
      }
    } else if ((header.m_list != null) && (header.m_list.size() > 0)) {
      return false;
    }
    return true;
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    writeValue(bos);
    md.getEncoding(DsByteString.newInstance(bos.toByteArray())).write(out);
  }

  /**
   * Clears all the member data and made this object reusable. The various components (sub-elements)
   * of this object can be set again.
   */
  public void reInit() {
    super.reInit();
    if (m_list != null) {
      m_list.clear();
    }
    if (data != null) {
      data.reInit();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case TYPE:
        data = new DsSipServerObj(new DsByteString(buffer, offset, count), null, null);
        addElement(data);
        break;
      case SUB_TYPE:
        if (data == null) {
          data = new DsSipServerObj();
          addElement(data);
        }
        data.productVersion = new DsByteString(buffer, offset, count);
        break;
      case COMMENT:
        if (data == null) {
          data = new DsSipServerObj();
          addElement(data);
        }
        data.comment = new DsByteString(buffer, offset, count);
        data = null;
        break;
    }
  }
} // Ends class DsSipServerBase
