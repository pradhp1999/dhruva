// Copyright (c) 2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the X-cisco-rai header as specified in EDCS-760329. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * x-cisco-rai              = ("X-cisco-rai") HCOLON resource-param *(COMMA resource-param)
 * resource-param           = resource-name-param SEMI resource-params *(SEMI resource-params)
 * resource-name-param      = "SYSTEM" / "CPU" / "MEM" / "DS0" / "DSP" / token
 * resource-params          = resource-status-param / resource-total-param /
 *                            resource-available-param / resource-used-param /
 *                            resource-extension
 * resource-status-param    = "almost-out-of-resource" EQUAL ("true" / "false")
 * resource-total-param     = "total" EQUAL 1*(DIGIT) ["%" / "MB" / token]
 * resource-available-param = "available" EQUAL 1*(DIGIT) ["%" / "MB" / token]
 * resource-used-param      = "used" EQUAL 1*3DIGIT "%"
 * resource-extension       = generic-param
 * </pre> </code>
 */
public final class DsSipXCiscoRaiHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_X_CISCO_RAI;
  /** Header ID. */
  public static final byte sID = X_CISCO_RAI;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_X_CISCO_RAI;

  /** The resource name. */
  private DsByteString m_resourceName;

  /** Default constructor. */
  public DsSipXCiscoRaiHeader() {
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
  public DsSipXCiscoRaiHeader(byte[] value)
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
  public DsSipXCiscoRaiHeader(byte[] value, int offset, int count)
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
  public DsSipXCiscoRaiHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name
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
    return BS_X_CISCO_RAI_TOKEN;
  }

  /**
   * Retrieves the resource name.
   *
   * @return the resource name.
   */
  public DsByteString getResourceName() {
    return m_resourceName;
  }

  /**
   * Sets the resource name.
   *
   * @param name the resource name.
   */
  public void setResourceName(DsByteString name) {
    m_resourceName = name;
  }

  /**
   * Sets the almost-out-of-resource parameter.
   *
   * @param flag value for the almost-out-of-resource parameter.
   */
  public void setAlmostOutOfResource(boolean flag) {
    if (flag) {
      setParameter(BS_ALMOST_OUT_OF_RESOURCE, BS_TRUE);
    } else {
      setParameter(BS_ALMOST_OUT_OF_RESOURCE, BS_FALSE);
    }
  }

  /** Method used to remove the almost-out-of-resource parameter. */
  public void removeAlmostOutOfResource() {
    removeParameter(BS_ALMOST_OUT_OF_RESOURCE);
  }

  /**
   * Check if the almost-out-of-resource parameter is present in the header.
   *
   * @return <code>true</code> if present, <code>false</code> otherwise.
   */
  public boolean hasAlmostOutOfResource() {
    return hasParameter(BS_ALMOST_OUT_OF_RESOURCE);
  }

  /**
   * Gets the value of the almost-out-of-resource parameter. Returns <code>false</code> if this
   * paramter is not present.
   *
   * @return the value of the almost-out-of-resource parameter or <code>false</code> if not present.
   */
  public boolean getAlmostOutOfResource() {
    DsByteString val = getParameter(BS_ALMOST_OUT_OF_RESOURCE);

    if (val == null || val.length() == 0) {
      return false;
    }

    if (val.equalsIgnoreCase(BS_TRUE)) {
      return true;
    }

    return false;
  }

  /**
   * Sets the total parameter.
   *
   * @param value the value for the total parameter.
   */
  public void setTotal(DsByteString value) {
    setParameter(BS_TOTAL, value);
  }

  /** Method used to remove the total parameter. */
  public void removeTotal() {
    removeParameter(BS_TOTAL);
  }

  /**
   * Check if the total parameter is present in the header.
   *
   * @return <code>true</code> if present, <code>false</code> otherwise.
   */
  public boolean hasTotal() {
    return hasParameter(BS_TOTAL);
  }

  /**
   * Gets the value of the total parameter. Returns <code>null</code> if this paramter is not
   * present.
   *
   * @return the value of the total parameter or <code>null</code> if not present.
   */
  public DsByteString getTotal() {
    return getParameter(BS_TOTAL);
  }

  /**
   * Sets the available parameter.
   *
   * @param value the value for the available parameter.
   */
  public void setAvailable(DsByteString value) {
    setParameter(BS_AVAILABLE, value);
  }

  /** Method used to remove the available parameter. */
  public void removeAvailable() {
    removeParameter(BS_AVAILABLE);
  }

  /**
   * Check if the available parameter is present in the header.
   *
   * @return <code>true</code> if present, <code>false</code> otherwise.
   */
  public boolean hasAvailable() {
    return hasParameter(BS_AVAILABLE);
  }

  /**
   * Gets the value of the available parameter. Returns <code>null</code> if this paramter is not
   * present.
   *
   * @return the value of the available parameter or <code>null</code> if not present.
   */
  public DsByteString getAvailable() {
    return getParameter(BS_AVAILABLE);
  }

  /**
   * Sets the used parameter.
   *
   * @param value the value for the used parameter.
   */
  public void setUsed(DsByteString value) {
    setParameter(BS_USED, value);
  }

  /** Method used to remove the used parameter. */
  public void removeUsed() {
    removeParameter(BS_USED);
  }

  /**
   * Check if the used parameter is present in the header.
   *
   * @return <code>true</code> if present, <code>false</code> otherwise.
   */
  public boolean hasUsed() {
    return hasParameter(BS_USED);
  }

  /**
   * Gets the value of the used parameter. Returns <code>null</code> if this paramter is not
   * present.
   *
   * @return the value of the used parameter or <code>null</code> if not present.
   */
  public DsByteString getUsed() {
    return getParameter(BS_USED);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_resourceName != null) {
      m_resourceName.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipXCiscoRaiHeader source = (DsSipXCiscoRaiHeader) header;
    m_resourceName = source.m_resourceName;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return X_CISCO_RAI;
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
    DsSipXCiscoRaiHeader header = null;
    try {
      header = (DsSipXCiscoRaiHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_resourceName != null && !m_resourceName.equals(header.m_resourceName)) {
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

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(m_resourceName).write(out);
    writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed object like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_resourceName = null;
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
        m_resourceName = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
}
