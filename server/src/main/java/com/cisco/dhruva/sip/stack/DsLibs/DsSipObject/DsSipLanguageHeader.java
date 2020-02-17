// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

/** Base class for language based SIP headers. */
public abstract class DsSipLanguageHeader extends DsSipHeader {
  /** Contains the value of the language in this header. */
  protected DsByteString m_strLanguage;
  /** Contains the list of the language sub-tag values present in this header. */
  protected LinkedList m_subTags;

  /** Default constructor. */
  protected DsSipLanguageHeader() {
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
  protected DsSipLanguageHeader(byte[] value)
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
  protected DsSipLanguageHeader(byte[] value, int offset, int count)
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
  protected DsSipLanguageHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the language part of this header.
   *
   * @return the language part of this header.
   */
  public DsByteString getLanguage() {
    return m_strLanguage;
  }

  /**
   * Sets the language part of this header.
   *
   * @param language the language part of this header.
   */
  public void setLanguage(DsByteString language) {
    m_strLanguage = language;
  }

  /**
   * Retrieves the language range for this header.
   *
   * @return the language range for this header.
   */
  public DsByteString getLanguageRange() {
    ByteBuffer out = ByteBuffer.newInstance(20);
    try {
      if (m_strLanguage != null) {
        m_strLanguage.write(out);
      }
      if ((m_subTags != null) && (m_subTags.size() > 0)) {
        Iterator iter = m_subTags.listIterator(0);
        DsByteString bs = null;
        while (iter.hasNext()) {
          bs = (DsByteString) iter.next();
          if (bs != null) {
            out.write(B_HIPHEN);
            bs.write(out);
          }
        }
      }
    } catch (IOException ioe) {
    }
    return out.getByteString();
  }

  /**
   * Appends the specified language sub tag <code>buffer</code> to the language range value in this
   * header.
   *
   * @param buffer the byte array containing the language sub tag value
   * @param off the offset in the specified byte array <code>buffer</code> where from the language
   *     sub tag value starts.
   * @param count the number of bytes in the specified byte array <code>buffer</code> that comprise
   *     the language sub tag value starting from <code>off</code>.
   */
  public void appendTag(byte[] buffer, int off, int count) {
    if (m_subTags == null) {
      m_subTags = new LinkedList();
    }
    m_subTags.addLast(new DsByteString(buffer, off, count));
  }

  /**
   * Returns the LinkedList that contains the list of all the language sub tag values in the
   * language range value of this header.
   *
   * @return the list of language sub tag values.
   */
  public LinkedList getSubTags() {
    return m_subTags;
  }

  /**
   * Sets the LinkedList that contains the list of all the language sub tag values in the language
   * range value for this header to the specified value <code>tags</code>.
   *
   * @param tags the list of language sub tag values that need to be set to this header.
   */
  public void setSubTags(LinkedList tags) {
    m_subTags = tags;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipLanguageHeader source = (DsSipLanguageHeader) header;
    m_strLanguage = source.m_strLanguage;
    m_subTags = source.m_subTags;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) {
      return false;
    }
    DsSipLanguageHeader header = null;
    try {
      header = (DsSipLanguageHeader) obj;
    } catch (ClassCastException cce) {
      return false;
    }
    if ((m_strLanguage == null || m_strLanguage.length() == 0)
        && (header.m_strLanguage == null || header.m_strLanguage.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strLanguage == null || !m_strLanguage.equals(header.m_strLanguage)) {
      return false;
    }
    if ((m_subTags != null) && (m_subTags.size() > 0)) {
      if ((header.m_subTags == null) || (header.m_subTags.size() != m_subTags.size())) {
        return false;
      }
      Iterator iter1 = m_subTags.listIterator(0);
      Iterator iter2 = header.m_subTags.listIterator(0);
      DsByteString str1 = null;
      DsByteString str2 = null;
      while (iter1.hasNext()) {
        str1 = (DsByteString) iter1.next();
        str2 = (DsByteString) iter2.next();
        if (!str1.equals(str2)) return false;
      }
    } else if ((header.m_subTags != null) && (header.m_subTags.size() > 0)) {
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
    if (m_strLanguage != null) {
      m_strLanguage.write(out);
    }
    if ((m_subTags != null) && (m_subTags.size() > 0)) {
      Iterator iter = m_subTags.listIterator(0);
      DsByteString bs = null;
      while (iter.hasNext()) {
        bs = (DsByteString) iter.next();
        if (bs != null) {
          out.write(B_HIPHEN);
          bs.write(out);
        }
      }
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    // todo look for instance problems in byte strings
    DsByteString encodedString = m_strLanguage.copy();

    if ((m_subTags != null) && (m_subTags.size() > 0)) {
      Iterator iter = m_subTags.listIterator(0);
      DsByteString bs = null;
      while (iter.hasNext()) {
        bs = (DsByteString) iter.next();
        if (bs != null) {
          encodedString.append(DsTokenSipConstants.TOKEN_SIP_LANGUAGE_SEPERATOR);
          encodedString.append(bs);
        }
      }
    }
    md.getEncoding(encodedString).write(out);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strLanguage = null;
    if (m_subTags != null) {
      m_subTags.clear();
    }
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
    if (elementId == SINGLE_VALUE) {
      return this;
    }

    return null;
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case SINGLE_VALUE:
        // We should not have been setting this value as the value of
        // the whole header.
        //                setValue(buffer, offset, count);
        break;
      case LANGUAGE_TAG:
        m_strLanguage = new DsByteString(buffer, offset, count);
        break;
      case LANGUAGE_SUBTAG:
        appendTag(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipLanguageHeader
