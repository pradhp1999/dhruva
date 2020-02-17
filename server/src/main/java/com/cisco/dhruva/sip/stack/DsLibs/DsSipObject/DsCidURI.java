// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * Representation of CID URL, RFC2111.
 *
 * <p>content-id = url-addr-spec url-addr-spec = addr-spec ; URL encoding of <a
 * href="http://www.faqs.org/rfcs/rfc822.html">RFC 822</a> addr-spec cid-url = "cid" ":" content-id
 *
 * @author Michael Zhou (xmzhou@cisco.com)
 * @author Jianren Yang (jryang@cisco.com)
 */
public class DsCidURI extends DsURI {

  /** Constructs CID URL with the scheme "cid". */
  public DsCidURI() {
    super();
    m_strName = BS_CID;
  }

  /**
   * Constructs CID URL with the scheme "cid" and the specified value.
   *
   * @param value the cid URL to parse.
   * @throws DsSipParserException if the parser encounters an error.
   */
  public DsCidURI(DsByteString value) throws DsSipParserException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs CID URL with the scheme "cid" and the specified value.
   *
   * @param value the cid URL to parse.
   * @throws DsSipParserException if the parser encounters an error.
   */
  public DsCidURI(byte[] value) throws DsSipParserException {
    this(value, 0, value.length);
  }

  /**
   * Constructs CID URL with the scheme "cid" and the specified value.
   *
   * @param value the cid URL to parse.
   * @param offset the start of the cid URL to parse.
   * @param count the number of bytes to parse.
   * @throws DsSipParserException if the parser encounters an error.
   */
  public DsCidURI(byte[] value, int offset, int count) throws DsSipParserException {
    super();
    if (count <= 4) throw new DsSipParserException("Not a CID URL");
    if ((value[offset] == 'c' || value[offset] == 'C')
        && (value[offset + 1] == 'i' || value[offset + 1] == 'I')
        && (value[offset + 2] == 'd' || value[offset + 2] == 'D')
        && (value[offset + 3] == ':')) {
      m_strName = BS_CID;
      // Skip the name part, "cid:"
      m_strValue = new DsByteString(value, offset + 4, count - 4);
      m_strValue.trim(); // remove white spaces from the start and end of this string.
    } else {
      throw new DsSipParserException("Not a CID URL");
    }
  }

  /**
   * Sets the name(scheme) of this URI.
   *
   * @param name the new name(scheme) for this URI.
   */
  public void setName(DsByteString name) {
    // Do nothing.
  }

  /**
   * Gets the CID value.
   *
   * @return cid value
   */
  public DsByteString getCid() {
    return getValue();
  }

  /**
   * Set CID value.
   *
   * @param cid CID value
   */
  public void setCid(DsByteString cid) {
    setValue(cid);
  }

  /**
   * Tells whether this URI is semantically equal to the specified object.
   *
   * @param obj the object that needs to be compared with this URI
   * @return <code>true</code> if the URIs are equal, <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof DsCidURI)) return false;
    DsCidURI cidUri = (DsCidURI) obj;
    return (m_strValue == null
        ? (DsByteString.nullOrEmpty(cidUri.m_strValue))
        : (DsByteString.equals(m_strValue, cidUri.m_strValue)));
  }
}
