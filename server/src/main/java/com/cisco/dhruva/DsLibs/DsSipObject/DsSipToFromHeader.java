// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/** This class is a base class for To and From headers. */
public abstract class DsSipToFromHeader extends DsSipNameAddressHeader {
  private static final DsByteString STR_TAG = new DsByteString(";tag=");

  /** Holds the tag parameter value for this header. */
  protected DsByteString m_strTag;

  /** Default constructor. */
  protected DsSipToFromHeader() {
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
  protected DsSipToFromHeader(byte[] value)
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
  protected DsSipToFromHeader(byte[] value, int offset, int count)
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
  protected DsSipToFromHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this To/From header with the specified <code>nameAddress</code> and the specified
   * <code>parameters</code>. The name address value is first parsed into a valid DsSipNameAddress.
   *
   * @param nameAddress the name address for this To/From header.
   * @param parameters the list of parameters for this header.
   * @throws DsSipParserException if there is an error while parsing the nameAddress value
   */
  public DsSipToFromHeader(DsByteString nameAddress, DsParameters parameters)
      throws DsSipParserException {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this To/From header with the specified <code>nameAddress</code> and the specified
   * <code>parameters</code>.
   *
   * @param nameAddress the name address for this To/From header.
   * @param parameters the list of parameters for this header.
   */
  protected DsSipToFromHeader(DsSipNameAddress nameAddress, DsParameters parameters) {
    super(nameAddress, parameters);
  }

  /**
   * Constructs this To/From header with the specified <code>nameAddress</code>.
   *
   * @param nameAddress the name address for this To/From header.
   */
  protected DsSipToFromHeader(DsSipNameAddress nameAddress) {
    this(nameAddress, null);
  }

  /**
   * Constructs this To/From header with the specified <code>uri</code> and the specified <code>
   * parameters</code>.
   *
   * @param uri the uri for this To/From header.
   * @param parameters the list of parameters for this header.
   */
  protected DsSipToFromHeader(DsURI uri, DsParameters parameters) {
    super(uri, parameters);
  }

  /**
   * Constructs this To/From header with the specified <code>uri</code>.
   *
   * @param uri the uri for this To/From header.
   */
  protected DsSipToFromHeader(DsURI uri) {
    super(uri);
  }

  /**
   * Retrieves the tag info.
   *
   * @return the tag information.
   */
  public DsByteString getTag() {
    return m_strTag;
  }

  /** Removes the tag parameter from this header. */
  public void removeTag() {
    m_strTag = null;
  }

  /**
   * Set the tag information for this header.
   *
   * @param tag the tag information to be set.
   */
  public void setTag(DsByteString tag) {
    m_strTag = tag;
  }

  /**
   * Check if the tag is present in the header.
   *
   * @return <code>true</code> if present, <code>false</code> otherwise.
   */
  public boolean isTagPresent() {
    return (m_strTag != null) && (m_strTag.length() > 0);
  }

  // ensure that the generic param methods handle the tag parameter
  public DsByteString getParameter(DsByteString name) {
    if (BS_TAG.equalsIgnoreCase(name)) {
      return m_strTag;
    }

    return super.getParameter(name);
  }

  public void setParameter(DsByteString name, DsByteString value) {
    if (BS_TAG.equalsIgnoreCase(name)) {
      m_strTag = value;
    } else {
      super.setParameter(name, value);
    }
  }

  public void removeParameter(DsByteString name) {
    if (BS_TAG.equalsIgnoreCase(name)) {
      m_strTag = null;
    } else {
      super.removeParameter(name);
    }
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipToFromHeader source = (DsSipToFromHeader) header;
    m_strTag = source.m_strTag;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    DsSipToFromHeader header = null;
    try {
      header = (DsSipToFromHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if ((m_strTag == null || m_strTag.length() == 0)
        && (header.m_strTag == null || header.m_strTag.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strTag == null || !m_strTag.equals(header.m_strTag)) {
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
    if (m_nameAddress != null) {
      // To, From, Record, Record-Route headers should always have <> brackets.
      // In case of Contact, <> are must if the URI contains ',' | ';' | '?'.
      // That can be checked in the setURI() method of DsSipNameAddress itself.
      // We might have already set it, but it could be possible that user has
      // manipulated this nameaddress after retrieving by getNameAddress().
      // So setting it anyway. :)
      m_nameAddress.setBrackets(true);
      m_nameAddress.write(out);
    }

    if (m_strTag != null && m_strTag.length() > 0) {
      STR_TAG.write(out);
      m_strTag.write(out);
    }

    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (BS_TAG.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_strTag = new DsByteString(buffer, valueOffset, valueCount);
    } else {
      super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    }
  }
} // Ends class DsSipToFromHeader
