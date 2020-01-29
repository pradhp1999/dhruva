// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.util.ListIterator;

/**
 * This class represents a Accept header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Accept           =  "Accept" ":" #( media-range [ accept-params ] )
 * media-range      =  ( "*\/*" | ( type "/" "*" ) | ( type "/" subtype ) ) *( ";" parameter )
 * parameter        =  attribute "=" value             ; is value optional?  We let it be.
 * attribute        =  token
 * value            =  token | quoted-string
 * accept-params    =  ";" "q" "=" qvalue *( accept-extension )
 * accept-extension =  ";" token [ "=" (token | quoted-string ) ]
 * type             =  token
 * subtype          =  token
 * attribute        =  token
 * value            =  token | quoted-string
 * </pre> </code> From the HTTP spec:<br>
 * <br>
 * Note: Use of the "q" parameter name to separate media type <br>
 * parameters from Accept extension parameters is due to historical <br>
 * practice. Although this prevents any media type parameter named <br>
 * "q" from being used with a media range, such an event is believed <br>
 * to be unlikely given the lack of any "q" parameters in the IANA <br>
 * media type registry and the rare usage of any media type <br>
 * parameters in Accept. Future media types are discouraged from <br>
 * registering any parameter named "q". <br>
 * <br>
 */
public final class DsSipAcceptHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_ACCEPT;
  /** Header ID. */
  public static final byte sID = ACCEPT;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  private boolean m_bWildCard;
  private DsByteString m_strType;
  private DsByteString m_strSubType;

  /** Parameters that apply to the media range */
  private DsParameters m_AcceptParams;

  private float m_fQValue;

  /** Default constructor. */
  public DsSipAcceptHeader() {
    super();
    // CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
    // The origianl super() calling will eventually call down to the child and set child's private
    // date member.
    init();
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
  public DsSipAcceptHeader(byte[] value) throws DsSipParserException, DsSipParserListenerException {
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
  public DsSipAcceptHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    this();
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
  public DsSipAcceptHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this header with the specified <code>type</code> media type and the specified <code>
   * subtype</code> medial sub type.
   *
   * @param type the type for the media range.
   * @param subtype the subtype for the media range.
   */
  public DsSipAcceptHeader(DsByteString type, DsByteString subtype) {
    this();
    setType(type);
    setSubType(subtype);
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
   * returns the token which is the compact name of the header.
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
    return BS_ACCEPT_TOKEN;
  }

  /**
   * Method is used to check if there is a wildcard '*'.
   *
   * @return <code>true</code> when there is a wild card is present, <code>false</code> otherwise
   */
  public boolean isWildCard() {
    return m_bWildCard;
  }

  /**
   * Retrieves the type.
   *
   * @return the type.
   * @see #setType
   */
  public DsByteString getType() {
    return m_strType;
  }

  /**
   * Retrieves the sub-type of the media range for this header.
   *
   * @return the sub-type of the media range of this header.
   * @see #setSubType(DsByteString)
   */
  public DsByteString getSubType() {
    return m_strSubType;
  }

  /**
   * Retrieves the media parameter with the specified name in this header.<br>
   * Note: This method is to retrieve the media parameters (the parameters that occur before the
   * qvalue parameter in this header). Also for retrieving the qvalue parameter, one should use
   * {@link #getQValue()} method. For retrieving all other parameters one should use {@link
   * #getParameter(DsByteString)}.
   *
   * @param name the name of the media parameter whose value is to be retrieved
   * @return the media parameter value
   * @see #setMediaParameter
   */
  public DsByteString getMediaParameter(DsByteString name) {
    DsByteString value = null;
    if (name != null && m_AcceptParams != null) {
      value = m_AcceptParams.get(name);
    }
    return value;
  }

  /**
   * Checks if any media range parameters exist.
   *
   * @return true, if any media range parameters exist, false otherwise
   * @see #getMediaParameter(DsByteString)
   */
  public boolean hasMediaParameters() {
    if (m_AcceptParams == null || m_AcceptParams.isEmpty()) {
      return false;
    }
    return true;
  }

  /**
   * Retrieves the DsParameters object, these parameters apply to the media range.
   *
   * @return the media range parameter list
   * @see #getMediaParameter(DsByteString)
   */
  public DsParameters getMediaParameters() {
    return m_AcceptParams;
  }

  /**
   * Retrieves the Media Range.
   *
   * @return the media range.
   */
  public DsByteString getMediaRange() {
    byte[] data = new byte[(m_strType.length() + m_strSubType.length() + 1)];
    int index = 0;
    System.arraycopy(m_strType.data(), m_strType.offset(), data, index, m_strType.length());
    index += m_strType.length();
    data[index++] = (byte) '/';
    System.arraycopy(
        m_strSubType.data(), m_strSubType.offset(), data, index, m_strSubType.length());
    return new DsByteString(data);
  }

  /**
   * Checks if the parameter is present, as a media range parameter.
   *
   * @param pParameter the parameter to check for.
   * @return <code>true</code> if the parameter is present, <code>false</code> otherwise
   * @see #getMediaParameter(DsByteString)
   */
  public boolean hasMediaParameter(DsByteString pParameter) {
    if (pParameter != null && m_AcceptParams != null) {
      return (m_AcceptParams.isPresent(pParameter));
    }
    return false;
  }

  /**
   * Sets the subtype.
   *
   * @param aSubType the sub-type.
   * @see #getSubType
   */
  public void setSubType(DsByteString aSubType) {
    m_strSubType = aSubType;
  }

  /**
   * Sets the type.
   *
   * @param aType the type.
   * @see #getType
   */
  public void setType(DsByteString aType) {
    m_strType = aType;
    if (aType != null) {
      if (aType.equals(BS_WILDCARD)) {
        m_bWildCard = true;
      } else {
        m_bWildCard = false;
      }
    } else {
      m_bWildCard = false;
    }
  }

  /**
   * Sets the media parameter with the specified <code>name</code> to the specified <code>value
   * </code> in this header.<br>
   * Note: This method is to set the media parameters (the parameters that occur before the qvalue
   * parameter in this header). Also for setting the qvalue parameter, one should use {@link
   * #setQValue(float)} method. For setting all other parameters one should use {@link
   * #setParameter(DsByteString, DsByteString)}.
   *
   * @param name the name of the media parameter whose value is to be set
   * @param value the value of the specified media parameter
   * @see #getMediaParameter(DsByteString)
   */
  public void setMediaParameter(DsByteString name, DsByteString value) {
    if (name == null || value == null) {
      return;
    }
    if (m_AcceptParams == null) {
      m_AcceptParams = new DsParameters();
    }
    m_AcceptParams.put(name, value);
  }

  /**
   * Sets the <code>qvalue</code> parameter value.
   *
   * @param value the <code>qvalue</code> parameter value.
   * @see #getQValue
   */
  public void setQValue(float value) {
    m_fQValue = value;
  }

  /**
   * Checks if <code>qvalue</code> exists.
   *
   * @return <code>true</code>, if <code>qvalue</code> exists, <code>false</code> otherwise.
   */
  public boolean hasQValue() {
    return (m_fQValue != -1.0);
  }

  /**
   * Returns the <code>qvalue</code> parameter value.
   *
   * @return the <code>qvalue</code> parameter value.
   * @see #setQValue(float)
   */
  public float getQValue() {
    return m_fQValue;
  }

  /** Removes all media range parameters. */
  public void removeMediaParameters() {
    if (m_AcceptParams != null) {
      m_AcceptParams.clear();
      m_AcceptParams = null;
    }
  }

  /**
   * Removes a media range parameter.
   *
   * @param pParameter the parameter.
   */
  public void removeMediaParameter(DsByteString pParameter) {
    if (pParameter == null) {
      return;
    }
    if (m_AcceptParams != null) {
      m_AcceptParams.remove(pParameter);
    }
  }

  /** Removes the <code>qvalue</code> parameter from this header. */
  public void removeQValue() {
    m_fQValue = (float) -1.0;
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
    if (m_strSubType != null) {
      out.write(B_SLASH);
      m_strSubType.write(out);
    }
    if (m_AcceptParams != null) {
      m_AcceptParams.write(out);
    }
    if (m_fQValue >= 0.0 && m_fQValue <= 1.0) {
      BS_QVALUE.write(out);
      out.write(DsByteString.getBytes(Float.toString(m_fQValue)));
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
    DsSipAcceptHeader source = (DsSipAcceptHeader) header;

    m_bWildCard = source.m_bWildCard;
    m_strType = source.m_strType;
    m_strSubType = source.m_strSubType;
    m_AcceptParams = source.m_AcceptParams;
    m_fQValue = source.m_fQValue;
  }

  /** This method makes a copy of the header. */
  public Object clone() {
    DsSipAcceptHeader clone = (DsSipAcceptHeader) super.clone();
    //        clone.m_strType      = (DsByteString)m_strType.clone();
    //        clone.m_strSubType   = (DsByteString)m_strSubType.clone();

    if (m_AcceptParams != null) {
      clone.m_AcceptParams = (DsParameters) m_AcceptParams.clone();
    }
    return clone;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID
   */
  public final int getHeaderID() {
    return ACCEPT;
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
    DsSipAcceptHeader header = null;
    try {
      header = (DsSipAcceptHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_bWildCard != header.m_bWildCard) {
      return false;
    }

    if (m_fQValue != header.m_fQValue) {
      return false;
    }
    if (!DsByteString.equals(m_strType, header.m_strType)) {
      return false;
    }
    if (!DsByteString.equals(m_strSubType, header.m_strSubType)) {
      return false;
    }
    if (m_AcceptParams != null && header.m_AcceptParams != null) {
      if (!m_AcceptParams.equals(header.m_AcceptParams)) {
        return false;
      }
    } else if (m_AcceptParams == null && header.m_AcceptParams != null) {
      if (!header.m_AcceptParams.isEmpty()) {
        return false;
      }
    } else if (header.m_AcceptParams == null && m_AcceptParams != null) {
      if (!m_AcceptParams.isEmpty()) {
        return false;
      }
    }
    // else both null - ok
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

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(m_strType).write(out);
    out.write('/');
    md.getEncoding(m_strSubType).write(out);
    writeEncodedParameters(out, md);
  }

  public int getParamCount() {
    int size = (m_AcceptParams == null) ? 0 : m_AcceptParams.size();
    if (m_fQValue >= 0.0 && m_fQValue <= 1.0) {
      size++;
    }
    return size;
  }

  protected void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (m_AcceptParams != null) {
      ListIterator paramIterator = m_AcceptParams.listIterator();
      while (paramIterator.hasNext()) {
        out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
        DsParameter param = (DsParameter) paramIterator.next();
        md.getEncoding(param.getKey()).write(out);

        DsByteString val = param.getValue();
        if (val != null) {
          if (val.charAt(0) == '"') {
            md.getEncoding(val.substring(1, val.length() - 1)).write(out);
          } else {
            md.getEncoding(param.getValue()).write(out);
          }
        } else {
          out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
        }
      }
    }
    if (m_fQValue >= 0.0 && m_fQValue <= 1.0) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(BS_Q).write(out);
      md.getEncoding(Float.toString(m_fQValue)).write(out);
    }

    super.writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed object like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_bWildCard = false;
    m_fQValue = (float) -1.0;
    m_strType = null;
    m_strSubType = null;
    if (m_AcceptParams != null) {
      m_AcceptParams.reInit();
    }
  }

  /**
   * This method contains the default initialization for the members of this header. This method
   * would be called in the constructor of this header.
   */
  // CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
  //  changed to private to allow local access only
  private void init() {
    // CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
    //         The origianl super() calling will eventually call down to the child and set child's
    // private date member.
    //        super();
    m_fQValue = (float) -1.0;
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
      case TYPE:
        m_strType = new DsByteString(buffer, offset, count);
        break;
      case SUB_TYPE:
        m_strSubType = new DsByteString(buffer, offset, count);
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (BS_Q.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      try {
        m_fQValue = DsSipMsgParser.parseFloat(buffer, valueOffset, valueCount);
      } catch (NumberFormatException nfe) {
      }
      return;
    }
    DsByteString name = new DsByteString(buffer, nameOffset, nameCount);
    DsByteString value = new DsByteString(buffer, valueOffset, valueCount);
    if (hasQValue()) {
      setParameter(name, value);
    } else {
      setMediaParameter(name, value);
    }
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            byte[] bytes = read();
  //            DsSipAcceptHeader header = new DsSipAcceptHeader(bytes);
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            header.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            DsSipAcceptHeader clone = (DsSipAcceptHeader) header.clone();
  //            clone.write(System.out);
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
  //            System.out.println("Media Parameters = " + header.getMediaParameters().toString());
  //            System.out.println();
  //            System.out.println("Extension Parameters = " + header.getParameters());
  //            System.out.println();
  //            System.out.println("Media Parameter = " + header.getMediaParameter(new
  // DsByteString("mp")));
  //            System.out.println();
  //            System.out.println("Extension Parameter = " + header.getParameter(new
  // DsByteString("ep")));
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()
} // Ends class DsSipAcceptHeader
