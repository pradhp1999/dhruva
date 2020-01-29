// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * An abstract base class that defines common constructs and functionality for the Event and
 * Allow-Events headers.
 */
public abstract class DsSipEventHeaderBase extends DsSipParametricHeader {
  /** Represents the package component of the header. */
  protected DsByteString m_strPackage;
  /** Represents the list of all the sub-package components of this header. */
  protected LinkedList m_subPackages;

  /** Default constructor. */
  protected DsSipEventHeaderBase() {
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
  protected DsSipEventHeaderBase(byte[] value)
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
  protected DsSipEventHeaderBase(byte[] value, int offset, int count)
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
  protected DsSipEventHeaderBase(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified package and the specified sub-package. It assumes
   * only one sub-package for this header (no '.' present in the specified <code>subPackage</code>).
   *
   * @param eventPackage the event package
   * @param subPackage the event sub-package
   */
  protected DsSipEventHeaderBase(DsByteString eventPackage, DsByteString subPackage) {
    m_strPackage = eventPackage;
    appendSubPackage(subPackage);
  }

  /**
   * Appends the specified event sub-package to this header.
   *
   * @param buffer the sub-package byte array.
   * @param off the offset on the specified byte array where from the event sub-package starts.
   * @param count the number of bytes for the event sub-package.
   */
  public void appendSubPackage(byte[] buffer, int off, int count) {
    appendSubPackage(new DsByteString(buffer, off, count));
  }

  /**
   * Appends the specified event sub-package to this header.
   *
   * @param subPackage the sub-package byte string.
   */
  // GOGONG 09/09/05 null value should not be added to the m_SubPackages
  public void appendSubPackage(DsByteString subPackage) {
    if (subPackage != null) {
      if (m_subPackages == null) {
        m_subPackages = new LinkedList();
      }
      m_subPackages.addLast(subPackage);
    }
  }

  /**
   * Returns the package component of this header.
   *
   * @return the package component of this header.
   */
  public DsByteString getPackage() {
    return m_strPackage;
  }

  /**
   * Sets the package component of this header to the specified <code>pkg</code> value.
   *
   * @param pkg the new package component of this header.
   */
  public void setPackage(DsByteString pkg) {
    m_strPackage = pkg;
  }

  /**
   * Returns the list of all the sub-package components of this header.
   *
   * @return the list of all the sub-package components of this header.
   */
  public LinkedList getSubPackages() {
    return m_subPackages;
  }

  /**
   * Returns the full package name, with subpackages appended with dots. For example, if getPackge()
   * returns "package" and getSubPackages() returns a list of 2 strings, "sub1" and "sub2", then
   * this method will return "package.sub1.sub2". This string may be re-generated each time this
   * method is called.
   *
   * @return the full package name, with subpackages appended with dots.
   */
  public DsByteString getFullPackageName() {
    if (m_subPackages == null || m_subPackages.size() == 0) {
      return m_strPackage;
    }

    DsByteString fullName = m_strPackage.copy();
    Iterator iter = m_subPackages.listIterator(0);
    // GOGONG 09/08/05 should not append "." to the pacakge name if sub-package is null
    DsByteString nextSubPkg;
    while (iter.hasNext()) {
      nextSubPkg = (DsByteString) iter.next();
      if (nextSubPkg != null) {
        fullName.append(BS_PERIOD);
        fullName.append(nextSubPkg);
      }
    }

    return fullName;
  }

  /**
   * Sets the list of all the sub-package components of this header to the specified <code>
   * subPackages</code> value.
   *
   * @param subPackages the new list of all the sub-package component of this header.
   */
  public void setSubPackages(LinkedList subPackages) {
    m_subPackages = subPackages;
  }

  /** Copy another header's members to me. */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipEventHeaderBase source = (DsSipEventHeaderBase) header;
    m_strPackage = source.m_strPackage;
    m_subPackages = source.m_subPackages;
  }

  /**
   * Returns a clone of this header.
   *
   * @return a clone of this header.
   */
  public Object clone() {
    DsSipEventHeaderBase clone = (DsSipEventHeaderBase) super.clone();
    //        clone.m_strPackage = m_strPackage;
    if (m_subPackages != null) {
      clone.m_subPackages = (LinkedList) m_subPackages.clone();
    }

    return clone;
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
    DsSipEventHeaderBase header = null;
    try {
      header = (DsSipEventHeaderBase) obj;
    } catch (ClassCastException cce) {
      return false;
    }
    if ((m_strPackage != null) && (m_strPackage.length() > 0)) {
      if (!m_strPackage.equals(header.m_strPackage)) {
        return false;
      }
    } else if ((header.m_strPackage != null) && (header.m_strPackage.length() > 0)) {
      return false;
    }
    if ((m_subPackages != null) && (m_subPackages.size() > 0)) {
      if ((header.m_subPackages == null) || (header.m_subPackages.size() != m_subPackages.size())) {
        return false;
      }
      Iterator iter1 = m_subPackages.listIterator(0);
      Iterator iter2 = header.m_subPackages.listIterator(0);
      DsByteString str1 = null;
      DsByteString str2 = null;
      while (iter1.hasNext()) {
        str1 = (DsByteString) iter1.next();
        str2 = (DsByteString) iter2.next();
        if (!str1.equals(str2)) return false;
      }
    } else if ((header.m_subPackages != null) && (header.m_subPackages.size() > 0)) {
      return false;
    }

    if (m_paramTable != null && header.m_paramTable != null) {
      if (!m_paramTable.equals(header.m_paramTable)) {
        return false;
      }
    } else if (m_paramTable == null && header.m_paramTable != null) {
      if (!header.m_paramTable.isEmpty()) {
        return false;
      }
    } else if (header.m_paramTable == null && m_paramTable != null) {
      if (!m_paramTable.isEmpty()) {
        return false;
      }
    }
    // else both null - ok

    return true;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_strPackage != null) {
      m_strPackage.write(out);
    }
    if ((m_subPackages != null) && (m_subPackages.size() > 0)) {
      Iterator iter = m_subPackages.listIterator(0);
      DsByteString bs = null;
      while (iter.hasNext()) {
        bs = (DsByteString) iter.next();
        if (bs != null) {
          out.write(B_PERIOD);
          bs.write(out);
        }
      }
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    DsByteString eventString = m_strPackage.copy();
    if ((m_subPackages != null) && (m_subPackages.size() > 0)) {
      Iterator iter = m_subPackages.listIterator(0);
      DsByteString bs = null;
      while (iter.hasNext()) {
        bs = (DsByteString) iter.next();
        if (bs != null) {
          eventString.append(DsTokenSipConstants.TOKEN_SIP_EVENT_SEPERATOR);
          eventString.append(bs);
        }
      }
    }
    md.getEncoding(eventString).write(out);
    writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strPackage = null;
    if (m_subPackages != null) {
      m_subPackages.clear();
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

    if (elementId == SINGLE_VALUE) {
      return this;
    } else {
      return null;
    }
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

    switch (elementId) {
      case SINGLE_VALUE:
        break;
      case EVENT_PACKAGE:
        m_strPackage = new DsByteString(buffer, offset, count);
        break;
      case EVENT_SUB_PACKAGE:
        appendSubPackage(new DsByteString(buffer, offset, count));
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipEventHeaderBase
