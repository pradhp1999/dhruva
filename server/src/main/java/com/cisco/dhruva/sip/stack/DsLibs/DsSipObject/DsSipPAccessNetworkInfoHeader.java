// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.*;

/**
 * This class represents a Supported header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * P-Access-Network-Info  = "P-Access-Network-Info" HCOLON access-net-spec
 * access-net-spec        = access-type *(SEMI access-info)
 * access-type            = "IEEE-802.11a" / "IEEE-802.11b" /
 *                          "3GPP-GERAN" / "3GPP-UTRAN-FDD" /
 *                          "3GPP-UTRAN-TDD" / "3GPP-CDMA2000" / token
 * access-info            = cgi-3gpp / utran-cell-id-3gpp / extension-access-info
 * extension-access-info  = gen-value
 * cgi-3gpp               = "cgi-3gpp" EQUAL (token / quoted-string)
 * utran-cell-id-3gpp     = "utran-cell-id-3gpp" EQUAL (token / quoted-string)
 * </pre> </code>
 */
public final class DsSipPAccessNetworkInfoHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_P_ACCESS_NETWORK_INFO;
  /** Header ID. */
  public static final byte sID = P_ACCESS_NETWORK_INFO;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** The state. */
  private DsByteString m_accessType;

  /** Default constructor. */
  public DsSipPAccessNetworkInfoHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipPAccessNetworkInfoHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     kevmo: 02.24.2006 bug fix - CSCef03455 It is the initialization
     sequence problem.  The origianl super() calling will eventually
     call down to the child and set child's private date member.
     super(value);
    */
    this(value, 0, value.length);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
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
  public DsSipPAccessNetworkInfoHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     kevmo: 02.24.2006 bug fix - CSCef03455 It is the initialization
     sequence problem.  The origianl super() calling will eventually
     call down to the child and set child's private date member.
     super(value, offset, count);
    */
    this();
    parse(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipPAccessNetworkInfoHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     kevmo: 02.24.2006 bug fix - CSCef03455 It is the initialization
     sequence problem.  The origianl super() calling will eventually
     call down to the child and set child's private date member.
     super(value);
    */
    this(value.toByteArray(), 0, value.length());
  }

  /**
   * Retrieves the access type.
   *
   * @return the access type
   */
  public DsByteString getAccessType() {
    return m_accessType;
  }

  /**
   * Sets the access type.
   *
   * @param type the new access type
   */
  public void setAccessType(DsByteString type) {
    m_accessType = type;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    DsSipPAccessNetworkInfoHeader header = null;
    try {
      header = (DsSipPAccessNetworkInfoHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_accessType != null && !m_accessType.equalsIgnoreCase(header.m_accessType)) {
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
    return true;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_accessType != null) {
      m_accessType.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(m_accessType).write(out);
    writeEncodedParameters(out, md);
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipPAccessNetworkInfoHeader source = (DsSipPAccessNetworkInfoHeader) header;
    m_accessType = source.m_accessType;
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
    return BS_P_ACCESS_NETWORK_INFO_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return P_ACCESS_NETWORK_INFO;
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
    switch (elementId) {
      case SINGLE_VALUE:
        m_accessType = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
}
