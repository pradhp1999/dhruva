// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the Content-Disposition header as specified in RFC 3261. It provides
 * methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Content-Disposition =  "Content-Disposition" ":" disposition-type *(";" disposition-param)
 * disposition-type    =  "render" | "session" | token
 * disposition-param   =  handling-param | generic-param
 * handling-param      =  "handling" "=" ( "optional" | "required" | other-handling )
 * other-handling      =  token
 * generic-param       =  token [ "=" ( token | quoted-string ) ]
 * </pre> </code>
 */
public final class DsSipContentDispositionHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_CONTENT_DISPOSITION;
  /** Header ID. */
  public static final byte sID = CONTENT_DISPOSITION;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_CONTENT_DISPOSITION;

  /** The content disposition type. */
  private DsByteString m_strType;

  /** Default constructor. */
  public DsSipContentDispositionHeader() {
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
  public DsSipContentDispositionHeader(byte[] value)
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
  public DsSipContentDispositionHeader(byte[] value, int offset, int count)
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
  public DsSipContentDispositionHeader(DsByteString value)
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
    return BS_CONTENT_DISPOSITION_TOKEN;
  }

  /**
   * Retrieves the disposition-type information.
   *
   * @return the disposition type.
   */
  public DsByteString getType() {
    return m_strType;
  }

  /**
   * Sets the disposition-type information.
   *
   * @param type the disposition type.
   */
  public void setType(DsByteString type) {
    m_strType = type;
  }

  /**
   * Sets the disposition handling information.
   *
   * @param handling the disposition type.
   */
  public void setHandling(DsByteString handling) {
    setParameter(BS_HANDLING, handling);
  }

  /** Method used to remove the handling parameter. */
  public void removeHandling() {
    removeParameter(BS_HANDLING);
  }

  /**
   * Check if the handling parameter is present in the header.
   *
   * @return <code>true</code> if present, <code>false</code> otherwise.
   */
  public boolean hasHandling() {
    return hasParameter(BS_HANDLING);
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
    DsSipContentDispositionHeader source = (DsSipContentDispositionHeader) header;
    m_strType = source.m_strType;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CONTENT_DISPOSITION;
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
    DsSipContentDispositionHeader header = null;
    try {
      header = (DsSipContentDispositionHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (m_strType != null && !m_strType.equals(header.m_strType)) {
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
    md.getEncoding(m_strType).write(out);
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
        m_strType = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            byte[] bytes = read();
  //            DsSipContentDispositionHeader header = new DsSipContentDispositionHeader(bytes);
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //            header.write(System.out);
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //                    DsSipContentDispositionHeader clone = (DsSipContentDispositionHeader)
  // header.clone();
  //                    clone.write(System.out);
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< (HEADER == CLONE) = "
  //                                        + header.equals(clone)
  //                                        +" >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  // System.out.println();
  // System.out.println("<<<<<<<<<<<<<<<<< (CLONE == HEADER) = "
  //                                        + clone.equals(header)
  //                                        +" >>>>>>>>>>>>>>>>>>>>");
  // System.out.println();
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()
} // Ends class DsSipContentDisposition
