// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the SIP Translate header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * translate-header =  "Translate" ":" SIP-URL [ ";" "nat" "=" nat-types ]
 * nat-types        =  "sym" | "cone"
 * </pre> </code>
 */
public final class DsSipTranslateHeader extends DsSipUrlHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_TRANSLATE;
  /** Header ID. */
  public static final byte sID = TRANSLATE;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** The start of the nat parameter. */
  private static final DsByteString STR_NAT = new DsByteString(";nat=");

  /** Default constructor. */
  public DsSipTranslateHeader() {
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
  public DsSipTranslateHeader(byte[] value)
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
  public DsSipTranslateHeader(byte[] value, int offset, int count)
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
  public DsSipTranslateHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs the DsSipTranslateHeader with the specified SIP URL and the specified nat type.
   *
   * @param uri The SIP URL that will be the part of this Translate header.
   * @param natType the specified nat type value for this Translate header.
   */
  public DsSipTranslateHeader(DsSipURL uri, DsByteString natType) {
    super(uri);
    m_strNatType = natType;
  }

  /**
   * Constructs this header with the specified URI value.
   *
   * @param uri the URI for this header.
   */
  public DsSipTranslateHeader(DsURI uri) {
    super(uri);
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
    return BS_TRANSLATE_TOKEN;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return TRANSLATE;
  }

  /**
   * Returns the nat type value for this Translate header.
   *
   * @return the nat type value for this Translate header.
   */
  public DsByteString getNatType() {
    return m_strNatType;
  }

  /**
   * Sets the nat type value for this Translate header.
   *
   * @param natType the nat type value for this Translate header.
   */
  public void setNatType(DsByteString natType) {
    m_strNatType = natType;
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipTranslateHeader source = (DsSipTranslateHeader) header;
    m_strNatType = source.m_strNatType;
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
    DsSipTranslateHeader header = null;
    try {
      header = (DsSipTranslateHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_URI != null) {
      if (header.m_URI == null) {
        return false;
      }
      if (!m_URI.equals(header.m_URI)) {
        return false;
      }
    } else {
      if (header.m_URI != null) {
        return false;
      }
    }
    if (!DsByteString.equals(m_strNatType, header.m_strNatType)) {
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
    if (m_URI != null) {
      out.write(B_LABRACE);
      m_URI.write(out);
      out.write(B_RABRACE);
    }
    if (m_strNatType != null) {
      STR_NAT.write(out);
      m_strNatType.write(out);
    }
  }

  public int getParamCount() {
    return super.getParamCount() + ((m_strNatType == null) ? 0 : 1);
  }

  protected void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {

    if (m_strNatType != null) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(BS_NAT).write(out);

      if (m_strNatType.charAt(0) == '"') {
        md.getEncoding(m_strNatType.substring(1, m_strNatType.length() - 1)).write(out);
      } else {
        md.getEncoding(m_strNatType).write(out);
      }
    }

    super.writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strNatType = null;
  }

  public boolean hasParameter(DsByteString key) {
    if (key.equalsIgnoreCase(BS_NAT)) {
      return (m_strNatType != null);
    }
    return super.hasParameter(key);
  }

  public DsByteString getParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_NAT)) {
      return m_strNatType;
    }
    return super.getParameter(name);
  }

  public void setParameter(DsByteString name, DsByteString value) {
    if (name.equalsIgnoreCase(BS_NAT)) {
      m_strNatType = value;
    } else {
      super.setParameter(name, value);
    }
  }

  public void removeParameter(DsByteString name) {
    if (name.equalsIgnoreCase(BS_NAT) && m_strNatType != null) {
      m_strNatType = null;
    } else {
      super.removeParameter(name);
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
    if (BS_NAT.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      // can use new lower if sym and cone ever make it to DsByteString
      m_strNatType = new DsByteString(buffer, valueOffset, valueCount);
    } else {
      super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    }
  }

  ///////////////////////////////////////////////////////////
  // Data members
  ///////////////////////////////////////////////////////////

  private DsByteString m_strNatType;

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            byte[] bytes = read();
  //            DsSipTranslateHeader header = new DsSipTranslateHeader(bytes);
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            header.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //                    DsSipTranslateHeader clone = (DsSipTranslateHeader) header.clone();
  //                    clone.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< (HEADER == CLONE) = "
  //                                                    + header.equals(clone)
  //                                                    +" >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< (CLONE == HEADER) = "
  //                                                    + clone.equals(header)
  //                                                    +" >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            System.out.println("Nat (param) = " + header.getParameter(BS_NAT));
  //            System.out.println("Nat (local) = " + header.getNatType());
  //            System.out.println();
  //            header.setNatType(new DsByteString("cone"));
  //            System.out.println("Setting with local");
  //            System.out.println("Nat (param) = " + header.getParameter(BS_NAT));
  //            System.out.println("Nat (local) = " + header.getNatType());
  //            System.out.println();
  //            header.setParameter(BS_NAT, new DsByteString("sym"));
  //            System.out.println("Setting with param");
  //            System.out.println("Nat (param) = " + header.getParameter(BS_NAT));
  //            System.out.println("Nat (local) = " + header.getNatType());
  //            System.out.println();
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()

} // Ends class DsSipTranslateHeader
