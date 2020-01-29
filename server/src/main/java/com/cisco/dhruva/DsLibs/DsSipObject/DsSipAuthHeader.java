// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * Base class for WWWAuthenticate and ProxyAuthenticate classes. It provides some common methods and
 * declares some methods which should be implemented.
 */
public abstract class DsSipAuthHeader extends DsSipParametricHeader
    implements Serializable, Cloneable {
  /** Represents the type of authentication (BASIC or DIGEST). */
  protected DsByteString m_strType;
  /** Represents the value after the authentication scheme in the header value. */
  protected DsByteString m_strData;

  /** Default constructor. */
  protected DsSipAuthHeader() {
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
  protected DsSipAuthHeader(byte[] value)
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
  protected DsSipAuthHeader(byte[] value, int offset, int count)
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
  protected DsSipAuthHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header from the specified authentication type and the specified authentication
   * data (that can be credentials info or the challenge info). It concatenates the authentication
   * type and the authentication data parts into in single byte array and then tries to parse the
   * various components into this header.
   *
   * @param type the authentication type for this header. This can be either BASIC or DIGEST.
   * @param data the authentication data (that can be credentials info or the challenge info)
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  protected DsSipAuthHeader(DsByteString type, DsByteString data)
      throws DsSipParserException, DsSipParserListenerException {
    super();
    m_strType = type;
    DsSipMsgParser.parseParameters(this, getHeaderID(), data.data(), data.offset(), data.length());
  }

  /**
   * Retrieves the authentication scheme (Basic or Digest).
   *
   * @return the authentication scheme (Basic or Digest).
   */
  public final DsByteString getType() {
    return m_strType;
  }

  /**
   * Sets the authentication scheme (Basic or Digest).
   *
   * @param type the authentication scheme that needs to be set. The supported schemes are Basic and
   *     Digest.
   */
  public final void setType(DsByteString type) {
    m_strType = type;
  }

  /**
   * Returns either the credentials info value or the challenge info value depending upon the header
   * type. In case of, authentication headers (WWW-Authenticate and Proxy-Authentication) the
   * challenge info value is returned and in case of authorization headers (Authorization and
   * Proxy-Authorization) the credentials info value is returned
   *
   * @return either the credentials info value or the challenge info value depending upon the header
   *     type.
   */
  public DsByteString getData() {
    DsByteString data = m_strData;
    if (m_paramTable != null) {
      m_paramTable.setDelimiter(B_COMMA);
      m_paramTable.startWithDelimiter(false);
      data = m_strData = m_paramTable.getValue();
    }
    return data;
  }

  /**
   * Sets either the credentials info value or the challenge info value depending upon the header
   * type. In case of, authentication headers (WWW-Authenticate and Proxy-Authentication) the
   * challenge info value is set and in case of authorization headers (Authorization and
   * Proxy-Authorization) the credentials info value is set. <br>
   * Note: If the authentication scheme is Basic, then the specified value is set as the basic
   * cookie. If the authentication scheme is Digest, then the specified value is cached and is not
   * parsed into individual parameters. In fact, if there are existing parameters, they are set to
   * null.
   *
   * @param data either the credentials info value or the challenge info value depending upon the
   *     header type.
   */
  public void setData(DsByteString data) {
    m_strData = data;
    if (m_paramTable != null) {
      m_paramTable = null;
    }
  }

  /**
   * Returns <code>true</code> if the specified object is equal to this object, <code>false</code>
   * otherwise.
   *
   * @param obj the object to compare.
   * @return <code>true</code> if the specified object is equal to this object, <code>false</code>
   *     otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    DsSipAuthHeader header = null;
    try {
      header = (DsSipAuthHeader) obj;
    } catch (ClassCastException cce) {
      return false;
    }
    if (m_strType != null && !m_strType.equalsIgnoreCase(header.m_strType)) {
      return false;
    }
    /*
            if (m_strData != null && !m_strData.equalsIgnoreCase(header.m_strData))
            {
                return false;
            }
    */
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
    if (m_strType != null) {
      m_strType.write(out);
    }
    out.write(B_SPACE);
    if (m_strData != null) {
      m_strData.write(out);
    } else if (m_paramTable != null) {
      m_paramTable.setDelimiter(B_COMMA);
      m_paramTable.startWithDelimiter(false);
      m_paramTable.write(out);
    }
  }

  // todo comma separated
  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(m_strType).write(out);

    // todo null check here...  Proxy-Authorization header has this as null.  Is something wrong
    // here?
    if (m_strData != null) {
      md.getEncoding(m_strData).write(out);
    }
    writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed object like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strType = null;
    m_strData = null;
  }

  //  unused method
  //    private static byte[] concat(DsByteString type, DsByteString data)
  //    {
  //        int size = type.length();
  //        size += data.length() + 1; // 1 for space
  //        byte[] bytes = new byte[size];
  //        System.arraycopy(type.data(), type.offset(), bytes, 0, type.length());
  //        bytes[type.length()] = B_SPACE;
  //        System.arraycopy(data.data(), data.offset(), bytes, type.length() + 1, data.length());
  //        return bytes;
  //    }

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
    switch (elementId) {
      case SINGLE_VALUE:
        m_strType = new DsByteString(buffer, offset, count);
        break;
      case BASIC_COOKIE:
        m_strData = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipAuthHeader
