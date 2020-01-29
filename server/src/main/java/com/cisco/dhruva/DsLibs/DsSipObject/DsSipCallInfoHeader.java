// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the Call-Info header as specified in RFC 3261. It provides methods to
 * build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Call-Info   =  "Call-Info" ":" # ( "&LT" URI "&GT" *( ";" info-param )
 * info-param  =  "purpose" "=" ( "icon" | "info" | "card" | token ) | generic-param
 * </pre> </code>
 */
public final class DsSipCallInfoHeader extends DsSipUrlHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_CALL_INFO;
  /** Header ID. */
  public static final byte sID = CALL_INFO;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_CALL_INFO;
  /** The purpose parameter name. */
  private static final DsByteString STR_PURPOSE = new DsByteString(";purpose=");

  private DsByteString m_strPurpose;

  /** Default constructor. */
  public DsSipCallInfoHeader() {
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
  public DsSipCallInfoHeader(byte[] value)
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
  public DsSipCallInfoHeader(byte[] value, int offset, int count)
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
  public DsSipCallInfoHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructor used to set the URI.
   *
   * @param aURI the URI for this header.
   */
  public DsSipCallInfoHeader(DsURI aURI) {
    super(aURI);
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
    return BS_CALL_INFO_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CALL_INFO;
  }

  /**
   * Retrieves the value of the purpose parameter.
   *
   * @return the value of the purpose parameter
   */
  public DsByteString getPurpose() {
    return m_strPurpose;
  }

  /**
   * Sets the value of the purpose parameter.
   *
   * @param aPurpose the value of the purpose parameter
   */
  public void setPurpose(DsByteString aPurpose) {
    m_strPurpose = aPurpose;
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
    DsSipCallInfoHeader header = null;
    try {
      header = (DsSipCallInfoHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if ((m_strPurpose == null || m_strPurpose.length() == 0)
        && (header.m_strPurpose == null || header.m_strPurpose.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strPurpose == null || !m_strPurpose.equals(header.m_strPurpose)) {
      return false;
    }
    return true;
  }

  public int getParamCount() {
    return super.getParamCount() + ((m_strPurpose == null) ? 0 : 1);
  }

  protected void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {

    if (m_strPurpose != null) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(BS_PURPOSE).write(out);
      if (m_strPurpose.charAt(0) == '"') {
        md.getEncoding(m_strPurpose.substring(1, m_strPurpose.length() - 1)).write(out);
      } else {
        md.getEncoding(m_strPurpose).write(out);
      }
    }

    super.writeEncodedParameters(out, md);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_URI != null) {
      out.write(B_LABRACE);
      m_URI.write(out);
      out.write(B_RABRACE);
    }
    if (m_strPurpose != null) {
      STR_PURPOSE.write(out);
      m_strPurpose.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (BS_PURPOSE.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      // could use newLower if icon/info/card make it to DsByteString
      m_strPurpose = new DsByteString(buffer, valueOffset, valueCount);
      return;
    }
    super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            byte[] bytes = read();
  //            DsSipCallInfoHeader header = new DsSipCallInfoHeader(bytes);
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< THIS >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //            header.write(System.out);
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //                    DsSipCallInfoHeader clone = (DsSipCallInfoHeader) header.clone();
  //                    clone.write(System.out);
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< (THIS == CLONE) = "
  //                                        + header.equals(clone)
  //                                        +" >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< (CLONE == THIS) = "
  //                                        + clone.equals(header)
  //                                        +" >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()

} // Ends class DsSipCallInfoHeader
