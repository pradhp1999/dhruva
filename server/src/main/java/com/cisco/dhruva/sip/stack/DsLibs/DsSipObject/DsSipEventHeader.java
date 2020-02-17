// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import java.util.*;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the Event header as specified in RFC 3265. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Event             =  ("Event" | "o") ":" event-type *((";" parameter-name
 *                        ["=" (token | quoted-string) ])
 * event-type        =  event-package *("." event-subpackage)
 * event-package     =  token-nodot
 * event-subpackage  =  token-nodot
 * token-nodot       =  1*(alphanum | "-"  | "!" | "%" | "*"
 *                                  | "_" | "+" | "`" | "'" | "~")
 * </pre> </code>
 */
public final class DsSipEventHeader extends DsSipEventHeaderBase {
  /** Header token. */
  public static final DsByteString sToken = BS_EVENT;
  /** Header ID. */
  public static final byte sID = EVENT;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_EVENT_C;

  // CAFFEINE 2.0 DEVELOPMENT
  /** Logger for the DsSipEvents package. */
  private static Logger m_cat = DsLog4j.eventsCat;

  /** Default constructor. */
  public DsSipEventHeader() {
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
  public DsSipEventHeader(byte[] value) throws DsSipParserException, DsSipParserListenerException {
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
  public DsSipEventHeader(byte[] value, int offset, int count)
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
  public DsSipEventHeader(DsByteString value)
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
  public DsSipEventHeader(DsByteString eventPackage, DsByteString subPackage) {
    super(eventPackage, subPackage);
  }

  /**
   * Returns the token which is the name of the header.
   *
   * @return the token value.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return the compact token name.
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
    return (isCompact()) ? BS_EVENT_C_TOKEN : BS_EVENT_TOKEN;
  }

  /**
   * Sets the id parameter.
   *
   * @param id the value of the id parameter
   */
  public void setID(DsByteString id) {
    setParameter(BS_ID, id);
  }

  // CAFFEINE 2.0 DEVELOPMENT
  /**
   * Sets the id parameter.
   *
   * @param id the value of the id parameter
   */
  public void setID(long id) {
    setParameter(BS_ID, DsByteString.valueOf(id));
  }

  /** Removes the id parameter. */
  public void removeID() {
    removeParameter(BS_ID);
  }

  /**
   * Checks if id parameter is present.
   *
   * @return <code>true</code> if the id parameter is present, <code>false</code> otherwise.
   */
  public boolean hasID() {
    return (getParameter(BS_ID) != null);
  }

  /**
   * Retrieves the id parameter.
   *
   * @return the id parameter.
   */
  public DsByteString getID() {
    return getParameter(BS_ID);
  }

  /**
   * Checks for equality of headers, ignoring all parameters except the "id" parameter.
   *
   * @param header the Event header to compare against
   * @return <code>true</code> if the headers are equal, ignoring all parameters except "id", <code>
   *     false</code> otherwise
   */
  public boolean equalsIdOnly(DsSipEventHeader header) {
    if (this == header) return true;
    if (header == null) {
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
        if (!str1.equals(str2)) {
          // Thread.currentThread().dumpStack();
          return false;
        }
      }
    } else if ((header.m_subPackages != null) && (header.m_subPackages.size() > 0)) {
      return false;
    }

    if (m_paramTable != null && header.m_paramTable != null) {
      DsByteString id1 = getID();
      DsByteString id2 = header.getID();
      if (id1 == null) id1 = DsByteString.BS_EMPTY_STRING;
      if (id2 == null) id2 = DsByteString.BS_EMPTY_STRING;

      if (!id1.equals(id2)) {
        return false;
      }
    } else if (m_paramTable == null && header.m_paramTable != null) {
      DsByteString id = header.getID();
      if (id == null) id = DsByteString.BS_EMPTY_STRING;

      if (id.length() > 0) {
        return false;
      }
    } else if (header.m_paramTable == null && m_paramTable != null) {
      DsByteString id = getID();
      if (id == null) id = DsByteString.BS_EMPTY_STRING;

      if (id.length() > 0) {
        return false;
      }
    }
    // else both null - ok

    return true;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return EVENT;
  }

  // CAFFEINE 2.0 DEVELOPMENT
  /**
   * Method to generate an event header with event type of "refer". The new header contains no event
   * id.
   *
   * @return the new event header.
   */
  public static DsSipEventHeader generateReferEventHeader() {
    DsSipEventHeader eventHdr = null;
    try {
      // REFACTOR package name changed from dynamicsoft to dhruva
      eventHdr =
          new DsSipEventHeader(
              new DsByteString(
                  com.cisco.dhruva.sip.stack.DsLibs.DsSipEvents.DsSipEventPackage.REFER_PKG_NAME));
    } catch (DsException e) {
      m_cat.warn("fail to generate an Event header of \"refer\" type:" + e.toString());
    }
    return eventHdr;
  }

  /**
   * Test if the package is "refer" in this event header.
   *
   * @return <code>true</code> if the package is "refer", <code>false</code> otherwise.
   */
  public boolean isRefer() {
    // REFACTOR
    return getPackage()
        .equals(
            com.cisco.dhruva.sip.stack.DsLibs.DsSipEvents.DsSipEventPackage.REFER_PKG.getName());
  }
  //    public static void main(String[] args)
  //    {
  //        DsByteString pkg = new DsByteString("package");
  //        DsByteString sub = new DsByteString("sub");
  //
  //        DsSipEventHeader h1 = new DsSipEventHeader(pkg, sub);
  //        DsSipEventHeader h2 = new DsSipEventHeader(pkg, sub);
  //        DsSipEventHeader h3 = new DsSipEventHeader(pkg, sub);
  //        DsSipEventHeader h4 = new DsSipEventHeader(pkg, sub);
  //        DsSipEventHeader h5 = new DsSipEventHeader(pkg, sub);
  //
  //        h1.setID(new DsByteString("id1"));
  //        h2.setParameter(new DsByteString("name"), new DsByteString("value"));
  //        h4.setID(new DsByteString("id1"));
  //        h5.setID(new DsByteString("id5"));
  //
  //        System.out.println("false h1 = h2 -> " + h1.equalsIdOnly(h2));
  //        System.out.println("false h2 = h1 -> " + h2.equalsIdOnly(h1));
  //        System.out.println("false h3 = h1 -> " + h3.equalsIdOnly(h1));
  //        System.out.println("false h1 = h3 -> " + h1.equalsIdOnly(h3));
  //        System.out.println("true h3 = h2 -> " + h3.equalsIdOnly(h2));
  //        System.out.println("true h2 = h3 -> " + h2.equalsIdOnly(h3));
  //        System.out.println("true h1 = h4 -> " + h1.equalsIdOnly(h4));
  //        System.out.println("true h4 = h1 -> " + h4.equalsIdOnly(h1));
  //        System.out.println("false h1 = h5 -> " + h1.equalsIdOnly(h5));
  //        System.out.println("false h5 = h1 -> " + h5.equalsIdOnly(h1));
  //    }
}
