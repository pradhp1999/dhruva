// Copyright (c) 2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the P-Charging-Function-Addresses header as specified in RFC 3455, Private
 * Header (P-Header) Extensions to the Session Initiation Protocol (SIP) for the 3rd-Generation
 * Partnership Project (3GPP). It provides methods to build, access, modify, serialize and clone the
 * header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * P-Charging-Addr = "P-Charging-Function-Addresses" HCOLON
 *                        charge-addr-params
 *                        *(SEMI charge-addr-params)
 * charge-addr-params  = ccf / ecf / generic-param
 * ccf                 = "ccf" EQUAL gen-value
 * ecf                 = "ecf" EQUAL gen-value
 * </pre> </code>
 */
public final class DsSipPChargingFunctionAddressesHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_P_CHARGING_FUNCTION_ADDRESSES;
  /** Header ID. */
  public static final byte sID = P_CHARGING_FUNCTION_ADDRESSES;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_P_CHARGING_FUNCTION_ADDRESSES;

  /** Default constructor. */
  public DsSipPChargingFunctionAddressesHeader() {
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
  public DsSipPChargingFunctionAddressesHeader(byte[] value)
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
  public DsSipPChargingFunctionAddressesHeader(byte[] value, int offset, int count)
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
  public DsSipPChargingFunctionAddressesHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public final DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name
   */
  public final DsByteString getCompactToken() {
    return sCompactToken;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_P_CHARGING_FUNCTION_ADDRESSES_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return sID;
  }

  /**
   * Sets the ccf parameter.
   *
   * @param ccf the ccf parameter value
   */
  public void setCcf(DsByteString ccf) {
    setParameter(BS_CCF, ccf);
  }

  /**
   * Gets the ccf parameter.
   *
   * @return the ccf parameter value
   */
  public DsByteString getCcf() {
    return getParameter(BS_CCF);
  }

  /** Method used to remove the ccf parameter. */
  public void removeCcf() {
    removeParameter(BS_CCF);
  }

  /**
   * Sets the ecf parameter.
   *
   * @param ecf the ecf parameter value
   */
  public void setEcf(DsByteString ecf) {
    setParameter(BS_ECF, ecf);
  }

  /**
   * Gets the ecf parameter.
   *
   * @return the ecf parameter value
   */
  public DsByteString getEcf() {
    return getParameter(BS_ECF);
  }

  /** Method used to remove the ecf parameter. */
  public void removeEcf() {
    removeParameter(BS_ECF);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  /*
   * javadoc inherited.
   */
  public DsByteString getTextBeforeParam() {
    return DsByteString.BS_EMPTY_STRING;
  }

  /*
   * javadoc inherited.
   */
  protected boolean getStartsWithDelimeter() {
    return false;
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
    DsSipPChargingFunctionAddressesHeader header;
    try {
      header = (DsSipPChargingFunctionAddressesHeader) obj;
    } catch (ClassCastException e) {
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

  /*
   * javadoc inherited.
   */
  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    writeEncodedParameters(out, md);
  }
}
