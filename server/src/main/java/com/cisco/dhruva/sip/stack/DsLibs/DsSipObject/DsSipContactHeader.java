// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;
import java.io.*;
import java.util.*;
import org.slf4j.event.Level;

/**
 * This class represents the Contact header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the Contact header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Contact           =  ( "Contact" | "m") ":" ("*" | (1# (( name-addr | addr-spec )
 *                        *( ";" contact-params ))))
 * name-addr         =  [ display-name ] "&LT" addr-spec "&GT"
 * addr-spec         =  SIP-URL | URI
 * display-name      =  *token | quoted-string
 * contact-params    =  "q" "=" qvalue
 *                        | "action" "=" ("proxy"| "redirect")
 *                        | "expires" "=" (delta-seconds | &LT"&GT SIP-date &LT"&GT)
 *                        | contact-extension
 * contact-extension =  generic-param
 * </pre> </code>
 */
public final class DsSipContactHeader extends DsSipNameAddressHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_CONTACT;
  /** Header ID. */
  public static final byte sID = CONTACT;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_CONTACT_C;

  private static final long sMaxDeltaSeconds = 4294967295L;
  private static final DsByteString STR_DELTA_SECONDS = new DsByteString("4294967295");
  /** The expires parameter name. */
  private static final DsByteString STR_EXPIRES = new DsByteString(";expires=");
  /** The action parameter name. */
  private static final DsByteString STR_ACTION = new DsByteString(";action=");

  private boolean m_bWildCard;
  private DsByteString m_strComments;
  private float m_fQValue = -1.0f;
  private DsByteString m_strAction;
  private DsByteString m_strExpires;

  public static final byte sFixedFormatHeaderId =
      DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_CONTACT_HEADER;
  public static final byte[] sHeaderBytes = DsTokenSipHeaderDictionary.getEncoding(sID);

  /** Default constructor. */
  public DsSipContactHeader() {
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
  public DsSipContactHeader(byte[] value)
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
  public DsSipContactHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    super();
    if (value.length == 1 && value[0] == B_WILDCARD) {
      m_bWildCard = true;
    } else {
      parse(value, offset, count);
    }
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
  public DsSipContactHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs this Contact header with the specified <code>nameAddress</code>.
   *
   * @param nameAddress the name address for this Contact header.
   */
  public DsSipContactHeader(DsSipNameAddress nameAddress) {
    super();
    this.m_nameAddress = nameAddress;
  }

  /**
   * Constructs this Contact header with the specified <code>uri</code>.
   *
   * @param uri the uri for this Contact header.
   * @throws DsException does not throw.
   */
  public DsSipContactHeader(DsURI uri) throws DsException {
    super();
    if (m_nameAddress == null) {
      m_nameAddress = new DsSipNameAddress();
    }
    m_nameAddress.setURI(uri);
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
    return (isCompact()) ? BS_CONTACT_C_TOKEN : BS_CONTACT_TOKEN;
  }

  /**
   * check for wildcard presence.
   *
   * @return <code>true</code> if wildcard is present, <code>false</code> otherwise.
   */
  public boolean isWildCard() {
    return (m_bWildCard);
  }

  /** Set the name address of this contact header to "*". */
  public void setWildCard() {
    m_bWildCard = true;
  }

  /**
   * Checks if Q value is present.
   *
   * @return <code>true</code> if Q value is present, <code>false</code> otherwise
   */
  public boolean hasQvalue() {
    return (m_fQValue >= 0);
  }

  /**
   * gets the Q value for the contact header.
   *
   * @return the Q value NOTE:- The default value currently is -1 but should throw an Exception.
   */
  public float getQvalue() {
    return m_fQValue;
  }

  /** Removes the Q value. */
  public void removeQValue() {
    m_fQValue = -1.0f;
  }

  /**
   * The method sets the Q value in parameter set for the contact header.
   *
   * @param qValue the Q value.
   */
  public void setQvalue(float qValue) {
    if (qValue >= 0 && qValue <= 1) {
      m_fQValue = qValue;
    }
  }

  /**
   * The method is used to set the value of the action parameter.
   *
   * @param action The value of the action parameter.
   */
  public void setAction(DsByteString action) {
    if (action != null) {
      m_strAction = action;
    }
  }

  /** Method used to remove the action parameter. */
  public void removeAction() {
    if (m_strAction != null) {
      m_strAction = null;
    }
  }

  /**
   * Checks if action parameter is present.
   *
   * @return <code>true</code> if action parameter is present, <code>false</code> otherwise.
   */
  public boolean hasAction() {
    return (m_strAction != null);
  }

  /**
   * Retrieves the action parameter.
   *
   * @return the value of action parameter.
   */
  public DsByteString getAction() {
    return m_strAction;
  }

  /**
   * Sets the value of the expires parameter.
   *
   * @param expiresVal the byte string value of expires parameter.
   */
  public void setExpires(DsByteString expiresVal) {
    int length = expiresVal.length();
    if (length > 10) // too big
    {
      m_strExpires = STR_DELTA_SECONDS;
    } else if (length == 10) // may be too big, check it
    {
      long val = expiresVal.parseLong();
      if (val > sMaxDeltaSeconds) {
        m_strExpires = STR_DELTA_SECONDS;
      }
    } else {
      m_strExpires = expiresVal;
    }
  }

  /**
   * Sets the value of the expires parameter.
   *
   * @param expiresVal the value of expires parameter.
   */
  public void setExpires(long expiresVal) {
    if (expiresVal > sMaxDeltaSeconds) {
      m_strExpires = STR_DELTA_SECONDS;
      return;
    }
    if (expiresVal < 0) {
      expiresVal = 0;
    }
    m_strExpires = DsByteString.valueOf(expiresVal);
  }

  /** Method used to remove the expires parameter. */
  public void removeExpires() {
    if (m_strExpires != null) {
      m_strExpires = null;
    }
  }

  /**
   * Checks if expires parameter is present.
   *
   * @return <code>true</code> if expires parameter is present, <code>false</code> otherwise.
   */
  public boolean hasExpires() {
    return (m_strExpires != null);
  }

  /**
   * Method used to retrieve the expires parameter as a DsByteString.
   *
   * @return the expires value as a DsByteString, null if not present.
   */
  public DsByteString getExpiresAsString() {
    return m_strExpires;
  }

  // ensures the getParameter("expires") works
  public DsByteString getParameter(DsByteString name) {
    if (BS_EXPIRES_VALUE.equalsIgnoreCase(name)) {
      return getExpiresAsString();
    }

    return super.getParameter(name);
  }

  // ensures the getParameter("expires") works
  public DsByteString getParameter(String name) {
    if (BS_EXPIRES_VALUE.equalsIgnoreCase(name)) {
      return getExpiresAsString();
    }

    return super.getParameter(name);
  }

  /**
   * Method used to retrieve the expires parameter.
   *
   * @return the expires value in seconds, -1 for no value.
   */
  public long getExpires() {
    DsByteString bs = m_strExpires;
    if (bs != null) {
      String expires = bs.toString();
      if (expires != null && expires.length() > 0) {
        long aLong;

        // remove double quotes which enclose the string
        if (expires.charAt(0) == '"' && expires.charAt(expires.length() - 1) == '"') {
          expires = expires.substring(1, expires.length() - 1);

          try {
            DsDate expireDate = new DsDate();

            expireDate.constructDsDate(expires);

            aLong = expireDate.getDate().getTime() - System.currentTimeMillis();
            aLong /= 1000; // convert from milliseconds into seconds
          } catch (Exception e) {
            if (DsLog4j.headerCat.isEnabled(Level.ERROR)) {
              DsLog4j.headerCat.error("expires string wrong format: in Contact header", e);
            }

            return -1L;
          }
        } else {
          try {
            aLong = Long.parseLong(expires);
          } catch (NumberFormatException e) {
            if (DsLog4j.headerCat.isEnabled(Level.ERROR)) {
              DsLog4j.headerCat.error("expires string wrong format: in Contact header ", e);
            }

            return -1L;
          }
        }

        if (aLong > sMaxDeltaSeconds) {
          aLong = sMaxDeltaSeconds;
        }

        return aLong;
      }
    }

    return -1L;
  }

  // CAFFEINE 2.0 DEVELOPMENT - Adding caller preferences support

  /**
   * Add callerpref feature, default feature value to true
   *
   * @param name feature name
   */
  public void setFeature(DsByteString name) {
    setParameter(name, BS_BLANK);
  }

  /**
   * Add callerpref feature
   *
   * @param name feature name
   * @param value feature value
   */
  public void setFeature(DsByteString name, DsByteString value) {
    setParameter(name, value);
  }

  /** Remove all callerpref features */
  public void removeFeatures() {

    if (!hasParameters()) {
      return;
    }

    Iterator itr = getParameters().iterator();
    while (itr.hasNext()) {
      DsParameter param = (DsParameter) itr.next();
      if (DsSipCallerPrefs.isFeature(param.getKey())) {
        itr.remove();
        // bug in itr.remove(), so have to do the following
        itr = getParameters().iterator();
      }
    }
    return;
  }

  /**
   * Remove specified callerpref features
   *
   * @param name feature name
   */
  public void removeFeature(DsByteString name) {
    removeParameter(name);
  }

  /**
   * Returns true if this contact has callerpref features
   *
   * @return true if this contact has callerpref features
   */
  public boolean hasFeatures() {
    if (!hasParameters()) {
      return false;
    }

    Iterator itr = getParameters().iterator();
    while (itr.hasNext()) {
      DsParameter param = (DsParameter) itr.next();
      if (DsSipCallerPrefs.isFeature(param.getKey())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns true if this contact has specified feature
   *
   * @param name feature name
   * @return true if this contact has specified feature
   */
  public boolean hasFeature(DsByteString name) {
    if (!DsSipCallerPrefs.isFeature(name)) {
      return false;
    }
    return hasParameter(name);
  }

  /** Return collection of valid callerpref features */
  public DsParameters getFeatures() {
    DsParameters features = new DsParameters();

    Iterator itr = m_paramTable.iterator();
    while (itr.hasNext()) {
      DsParameter param = (DsParameter) itr.next();
      if (DsSipCallerPrefs.isFeature(param.getKey())) {
        features.add(param);
      }
    }

    return features;
  }

  /**
   * Reture value of specified feature
   *
   * @param name feature name
   */
  public DsByteString getFeature(DsByteString name) {
    DsByteString result = getParameter(name);
    if (result == null && name.charAt(0) == '+') {
      result = getParameter(name.substring(1));
    }
    if (result == null) {
      return null;
    }

    if (result.equals("")) {
      return BS_TRUE;
    }

    return result;
  }

  /**
   * Returns true if this contact has specified feature set or implied to be true
   *
   * @param name feature name
   * @return true if this contact has specified feature as true or implied to be true
   */
  public boolean matchFeature(DsByteString name) {
    DsByteString contactFeatureValue = getFeature(name);
    if (contactFeatureValue == null) {
      return false;
    }

    return DsSipCallerPrefs.matchFeatureValue(BS_TRUE, contactFeatureValue);
  }

  /**
   * Returns true if this contact has specified feature with specifed value or implied to be true
   *
   * @param name feature name
   * @param value feature value
   * @return true if this contact has specified feature with specifed value
   */
  public boolean matchFeature(DsByteString name, DsByteString value) {
    DsByteString contactFeatureValue = getFeature(name);
    if (contactFeatureValue == null) {
      return false;
    }

    if (value.equals("")) {
      value = BS_TRUE;
    }

    return DsSipCallerPrefs.matchFeatureValue(value, contactFeatureValue);
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipContactHeader source = (DsSipContactHeader) header;
    m_bWildCard = source.m_bWildCard;
    m_strComments = source.m_strComments;
    m_strExpires = source.m_strExpires;
    m_strAction = source.m_strAction;
    m_fQValue = source.m_fQValue;
  }

  /*
   * This method makes a copy of the header.
   */
  /*
      public Object clone()
      {
          DsSipContactHeader clone = (DsSipContactHeader)super.clone();
          clone.m_bWildCard = m_bWildCard;
          if (m_strComments != null)
          {
              clone.m_strComments = (DsByteString)m_strComments.clone();
          }
          return clone;
      }
  */
  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CONTACT;
  }

  /**
   * Checks for equality of headers.
   *
   * @param obj the object to check.
   * @return <code>true</code> if the headers are equal <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipContactHeader header = null;
    try {
      header = (DsSipContactHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_bWildCard != header.m_bWildCard) {
      return false;
    }

    if (m_nameAddress != null) {
      if (header.m_nameAddress == null) {
        return false;
      }
      if (!m_nameAddress.equals(header.m_nameAddress)) {
        return false;
      }
    } else {
      if (header.m_nameAddress != null) {
        return false;
      }
    }
    // Compare QValue
    if (Float.compare(m_fQValue, header.m_fQValue) != 0) {
      return false;
    }
    // Compare Action
    if ((m_strAction == null || m_strAction.length() == 0)
        && (header.m_strAction == null || header.m_strAction.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strAction == null || !m_strAction.equals(header.m_strAction)) {
      return false;
    }
    // Compare Expires
    if ((m_strExpires == null || m_strExpires.length() == 0)
        && (header.m_strExpires == null || header.m_strExpires.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strExpires == null || !m_strExpires.equals(header.m_strExpires)) {
      return false;
    }
    // Compare Comments
    if ((m_strComments == null || m_strComments.length() == 0)
        && (header.m_strComments == null || header.m_strComments.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strComments == null || !m_strComments.equals(header.m_strComments)) {
      return false;
    }
    // Compare ext-parameters
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
   * Retuns the value of this header. There will be no end of line character or carriage return
   * character at the end of this returned value. <br>
   * Note: Calling this method may construct a new DsByteString object every time.
   *
   * @return the DsByteString representation of the value of this header.
   */
  public DsByteString getValue() {
    if (m_bWildCard) {
      return BS_WILDCARD;
    }
    return super.getValue();
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_bWildCard) {
      out.write(B_WILDCARD);
      return;
    }
    if (m_nameAddress != null) {
      m_nameAddress.write(out);
    }
    if (m_fQValue >= 0) {
      BS_QVALUE.write(out);
      DsByteString.valueOf(m_fQValue).write(out);
    }
    if (m_strAction != null) {
      STR_ACTION.write(out);
      m_strAction.write(out);
    }
    if (m_strExpires != null) {
      STR_EXPIRES.write(out);
      m_strExpires.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
    if (m_strComments != null) {
      out.write(B_SPACE);
      out.write(B_LBRACE);
      m_strComments.write(out);
      out.write(B_RBRACE);
    }
  }

  public void writeEncodedHeaderName(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (isWildCard()) {
      out.write(sHeaderBytes);
    } else {
      out.write(sFixedFormatHeaderId);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (isWildCard()) {
      out.write('*');
    } else {
      super.writeEncodedValue(out, md);
    }
  }

  public int getParamCount() {
    int size = super.getParamCount();
    if (m_fQValue >= 0) {
      size++;
    }
    if (m_strAction != null) {
      size++;
    }
    if (m_strExpires != null) {
      size++;
    }
    return size;
  }

  protected void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {

    if (m_fQValue >= 0) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(BS_Q).write(out);
      md.getEncoding(Float.toString(m_fQValue)).write(out);
    }
    if (m_strAction != null) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(BS_ACTION).write(out);

      if (m_strAction.charAt(0) == '"') {
        md.getEncoding(m_strAction.substring(1, m_strAction.length() - 1)).write(out);
      } else {
        md.getEncoding(m_strAction).write(out);
      }
    }
    if (m_strExpires != null) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(BSL_EXPIRES).write(out);
      if (m_strExpires.charAt(0) == '"') {
        md.getEncoding(m_strExpires.substring(1, m_strExpires.length() - 1)).write(out);
      } else {
        md.getEncoding(m_strExpires).write(out);
      }
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
    m_strComments = null;
  }

  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case WILDCARD:
        m_bWildCard = true;
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
    } else if (BS_ACTION.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_strAction = DsByteString.newLower(buffer, valueOffset, valueCount);
    } else if (BS_EXPIRES_VALUE.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setExpires(new DsByteString(buffer, valueOffset, valueCount));
    } else {
      super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    }
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            byte[] bytes = read();
  //            DsSipContactHeader header = new DsSipContactHeader(bytes);
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            header.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            DsSipContactHeader clone = (DsSipContactHeader) header.clone();
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
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }// Ends main()
}
