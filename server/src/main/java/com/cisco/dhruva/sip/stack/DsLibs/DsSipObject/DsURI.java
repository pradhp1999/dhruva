// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipURIGenericEncoder;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsPerf;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This class represents a URI as specified in RFC 2396. It is the base class from which DsSipURL
 * and DsTelURL are derived.
 *
 * <p><b>Changes since v5.3</b>
 *
 * <p>The telephone subscriber information should be retrieved through the new method
 * getTelephoneSubscriber() and it deprecates the methods in this class that were used to retrieve
 * and manipulate the telephone subscriber information that may be present in the SIP and TEL URLs.
 *
 * <p>If this is a SIP URL, then this method tries to parse the user part of the SIP URL as
 * telephone subscriber information even if the user parameter is not equal to "phone" for this SIP
 * URL. If successful then returns the parsed DsSipTelephoneSubscriber object reference that can be
 * used to manipulate the telephone subscriber information. The manipulated information can be set
 * back as the user part of the SIP URL. If the user part couldn't be parsed as valid telephone
 * subscriber information then DsSipParserException is thrown.
 *
 * <p>If this is a TEL URL, then this method returns the parsed DsSipTelephoneSubscriber object
 * reference. This telephone subscriber information gets parsed and constructed during the
 * construction of the TEL URL itself. So no parsing happens at this point and the previously parsed
 * object is returned. This object reference can be used to manipulate the telephone subscriber
 * information and it directly manipulates the TEL URL. User need not to do any extra operation to
 * reflect the changes in the URL itself as in case of SIP URL, where user has to invoke
 * DsSipURL.setUser(String) to set the manipulated telephone subscriber information.
 *
 * <p>If the URL is neither SIP nor TEL, then this method returns <code>null</code>.
 */
public class DsURI implements Cloneable, Serializable, DsSipElementListener, DsSipConstants {
  /** Represents the value of this uri as a DsByteString. It's ths value after the ':'. */
  protected DsByteString m_strValue;
  /**
   * Represents the name of this uri as a DsByteString. It's ths name before the ':'. In other
   * words, its URI scheme.
   */
  protected DsByteString m_strName;

  // -- kevmo 11.03.05 CSCsc33810 remove BS_TEL static final values
  // -- those values are defined in the DsSipConstants. Remove those static final
  // -- values reduce ambiguity.

  /**
   * Constructs and returns the corresponding URI object from the specified <code>uriStr</code>. If
   * the URI name(scheme) is 'tel' then DsTelURL object will be returned, and if the URI
   * name(scheme) is 'sip' then DsSipURL will be returned, otherwise DsURI object is returned.
   *
   * @param uriStr the byte array containing the URI data.
   * @param offset the offset in the specified <code>uriStr</code> byte array where from this URI
   *     starts.
   * @param length the number of bytes that comprise the URI in the specified <code>uriStr</code>
   *     byte array.
   * @return a DsURI of the proper sublass.
   * @throws DsSipParserException if there is an exception while parsing
   */
  public static DsURI constructFrom(byte[] uriStr, int offset, int length)
      throws DsSipParserException {
    int colonIndex = -1;
    int end = offset + length;

    for (int i = offset; i < end; i++) {
      if (uriStr[i] == ':') {
        colonIndex = i;
        break;
      }
    }

    if (colonIndex == -1) {
      throw new DsSipParserException("No ':' in URI");
    }

    int schemeLen = colonIndex - offset;
    DsByteString scheme = new DsByteString(uriStr, offset, schemeLen);

    if (schemeLen == 3) {
      if ((uriStr[offset] == 's' || uriStr[offset] == 'S')
          && (uriStr[offset + 1] == 'i' || uriStr[offset + 1] == 'I')
          && (uriStr[offset + 2] == 'p' || uriStr[offset + 2] == 'P')) {
        DsSipURL url = new DsSipURL();
        try {
          DsSipMsgParser.parseSipUrl(url, url, uriStr, offset, length);
        } catch (DsSipParserListenerException exc) {
        }
        return url;
      } else if ((uriStr[offset] == 't' || uriStr[offset] == 'T')
          && (uriStr[offset + 1] == 'e' || uriStr[offset + 1] == 'E')
          && (uriStr[offset + 2] == 'l' || uriStr[offset + 2] == 'L')) {
        DsTelURL url = new DsTelURL();
        try {
          DsSipMsgParser.parseTelUrl(url, uriStr, offset, length);
        } catch (DsSipParserListenerException exc) {
        }
        return url;
      }
      // CAFFEINE 2.0 DEVELOPMENT - Content-ID Support
      else if ((uriStr[offset] == 'c' || uriStr[offset] == 'C')
          && (uriStr[offset + 1] == 'i' || uriStr[offset + 1] == 'I')
          && (uriStr[offset + 2] == 'd' || uriStr[offset + 2] == 'D')) {
        DsCidURI uri = new DsCidURI(uriStr, offset, length);
        return uri;
      }
    } else if (schemeLen == 4) {
      if ((uriStr[offset] == 's' || uriStr[offset] == 'S')
          && (uriStr[offset + 1] == 'i' || uriStr[offset + 1] == 'I')
          && (uriStr[offset + 2] == 'p' || uriStr[offset + 2] == 'P')
          && (uriStr[offset + 3] == 's' || uriStr[offset + 3] == 'S')) {
        DsSipURL url = new DsSipURL(true);
        try {
          DsSipMsgParser.parseSipUrl(url, url, uriStr, offset, length);
        } catch (DsSipParserListenerException exc) {
        }
        return url;
      }
    }
    // else // just a generic uri
    // {
    DsURI uri = new DsURI();

    uri.m_strName = scheme;
    uri.m_strValue = new DsByteString(uriStr, (colonIndex + 1), (length - colonIndex - 1));

    return uri;
    // }
  }

  /**
   * Constructs and returns the corresponding URI object from the specified <code>uriStr</code>. If
   * the URI name(scheme) is 'tel' then DsTelURL object will be returned, and if the URI
   * name(scheme) is 'sip' then DsSipURL will be returned, otherwise DsURI object is returned.
   *
   * @param uriStr the byte string containing the URI data.
   * @return a DsURI of the proper sublass.
   * @throws DsSipParserException if there is an exception while parsing
   */
  public static DsURI constructFrom(DsByteString uriStr) throws DsSipParserException {
    return constructFrom(uriStr.data(), uriStr.offset(), uriStr.length());
  }

  /**
   * Constructs and returns the corresponding URI object from the specified <code>uriStr</code>. If
   * the URI name(scheme) is 'tel' then DsTelURL object will be returned, and if the URI
   * name(scheme) is 'sip' then DsSipURL will be returned, otherwise DsURI object is returned.
   *
   * @param uriStr the string containing the URI data.
   * @return a DsURI of the proper sublass.
   * @throws DsSipParserException if there is an exception while parsing
   */
  public static DsURI constructFrom(String uriStr) throws DsSipParserException {
    return constructFrom(new DsByteString(uriStr));
  }

  /**
   * Tells if there are any parameters in this URI.
   *
   * @return <code>true</code> if there are URI parameters, <code>false</code> otherwise
   */
  public boolean hasParameters() {
    return false;
  }

  /**
   * Tells if there are any headers in this URI.
   *
   * @return <code>true</code> if there are URI headers, <code>false</code> otherwise
   */
  public boolean hasHeaders() {
    return false;
  }

  /**
   * Sets the name(scheme) of this URI.
   *
   * @param name the new name(scheme) for this URI.
   */
  public void setName(DsByteString name) {
    m_strName = name;
  }

  /**
   * Returns the name(scheme) of this URI.
   *
   * @return the name(scheme) of this URI.
   */
  public DsByteString getName() {
    return m_strName;
  }

  /**
   * Sets the value( after ':' part of the URI) of this URI to the specified <code>value</code>.
   *
   * @param value the new value (after ':' part of the URI) for this URI
   */
  public void setValue(DsByteString value) {
    m_strValue = value;
  }

  /**
   * Returns the value( after ':' part of the URI) of this URI.
   *
   * @return the value( after ':' part of the URI) of this URI.
   */
  public DsByteString getValue() {
    return m_strValue;
  }

  /**
   * Sets the scheme(name) of this URI.
   *
   * @param scheme the new scheme(name) for this URI.
   */
  public void setScheme(DsByteString scheme) {
    setName(scheme);
  }

  /**
   * Returns the scheme(name) of this URI.
   *
   * @return the scheme(name) of this URI.
   */
  public DsByteString getScheme() {
    return getName();
  }

  /**
   * Sets the value( after ':' part of the URI) of this URI to the specified <code>value</code>.
   *
   * @param value the new value (after ':' part of the URI) for this URI
   */
  public void setSchemeData(DsByteString value) {
    setValue(value);
  }

  /**
   * Returns the value( after ':' part of the URI) of this URI.
   *
   * @return the value( after ':' part of the URI) of this URI.
   */
  public DsByteString getSchemeData() {
    return getValue();
  }

  /**
   * Writes the value( after ':' part of the URI) of this URI to the specified <code>out</code>
   * output stream.
   *
   * @param out the byte array output stream where the value(after ':' part of the URI) of this URI
   *     needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void writeValue(OutputStream out) throws IOException {
    DsByteString bs = getValue();
    if (bs != null) {
      bs.write(out);
    }
  }

  /**
   * Writes this URI (in the format 'name:value')to the specified <code>out</code> output stream.
   *
   * @param out the byte array output stream where this URI needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void write(OutputStream out) throws IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.URI_WRITE);
    DsByteString bs = null;
    bs = getName();
    if (bs != null) {
      bs.write(out);
    }
    out.write(B_COLON);
    writeValue(out);
    if (DsPerf.ON) DsPerf.stop(DsPerf.URI_WRITE);
  }

  /**
   * Returns a DsByteString representation of this URI in the format 'name:value'.
   *
   * @return a DsByteString representation of this URI in the format 'name:value'.
   */
  public DsByteString toByteString() {
    ByteBuffer buffer = ByteBuffer.newInstance();
    try {
      write(buffer);
    } catch (IOException ioe) {
    }
    return buffer.getByteString();
  }

  /**
   * Returns a String representation of this URI in the format 'name:value'.
   *
   * @return a String representation of this URI in the format 'name:value'.
   */
  public String toString() {
    return toByteString().toString();
  }

  /**
   * Tells whether this URI is a SIP URI.
   *
   * @return <code>true</code> if its a SIP URL, <code>false</code> otherwise
   */
  public boolean isSipURL() {
    return false;
  }

  /**
   * Returns a clone of this URI object.
   *
   * @return a clone of this URI object.
   */
  public Object clone() {
    DsURI clone = null;
    try {
      clone = (DsURI) super.clone();
    } catch (CloneNotSupportedException e) {
      // Why this catch block is empty?
      // We know we never get to this exception.
    }
    return clone;
  }

  /**
   * Tells whether this URI is semantically equal to the specified object.
   *
   * @param obj the object that needs to be compared with this URI
   * @return <code>true</code> if the URIs are equal, <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    DsURI dsURI = null;
    try {
      dsURI = (DsURI) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_strName != null) {
      if (dsURI.m_strName == null) {
        return false;
      }
      if (!m_strName.equalsIgnoreCase(dsURI.m_strName)) {
        return false;
      }
    } else {
      if (dsURI.m_strName != null) {
        return false;
      }
    }

    if (m_strValue != null) {
      if (dsURI.m_strValue == null) {
        return false;
      }
      if (!m_strValue.equals(dsURI.m_strValue)) {
        return false;
      }
    } else {
      if (dsURI.m_strValue != null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the telephone subscriber information, if present, as a DsSipTelephoneSubscriber object.
   * If this is a SIP URL, then this method tries to parse the user part of the SIP URL as telephone
   * subscriber information even if the user parameter is not equal to "phone" for this SIP URL. If
   * successful then returns the parsed DsSipTelephoneSubscriber object reference that can be used
   * to manipulate the telephone subscriber information. The manipulated information can be set back
   * as the user part of the SIP URL. If the user part couldn't be parsed as valid telephone
   * subscriber information then DsSipParserException is thrown. If this is a TEL URL, then this
   * method returns the parsed DsSipTelephoneSubscriber object reference. This telephone subscriber
   * information gets parsed and constructed during the construction of the TEL URL itself. So no
   * parsing happens at this point and the previously parsed object is returned. This object
   * reference can be used to manipulate the telephone subscriber information and it directly
   * manipulates the TEL URL. If the URL is neither SIP nor TEL, then this method returns null.
   *
   * @return the DsSipTelephoneSubscriber object reference that contains the telephone subscriber
   *     information if present in this URL, returns null otherwise.
   * @throws DsSipParserException if the user part couldn't be parsed as the valid telephone
   *     subscriber information in case of SIP URL.
   */
  public DsSipTelephoneSubscriber getTelephoneSubscriber() throws DsSipParserException {
    return null;
  }

  //    public static void main(String args[])
  //    {
  //        try
  //        {
  //            DsURI url = DsURI.constructFrom(new DsByteString("http:user@host"));
  //
  //            System.out.println("Name is " + url.getName());
  //
  //           System.out.println("Value is " + url.getValue());
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  //    }

  /**
   * Writes this URI (in the format 'name:value')to the specified <code>out</code> output stream.
   *
   * @param out the byte array output stream where this URI needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  // todo finish this!!!
  public void writeEncoded(OutputStream out, DsTokenSipMessageDictionary md) throws IOException {

    if (DsPerf.ON) DsPerf.start(DsPerf.URI_WRITE);

    DsTokenSipURIGenericEncoder uriEncoding = new DsTokenSipURIGenericEncoder(this);
    out.write(uriEncoding.getFlags());

    DsByteString bs = null;
    bs = getName();
    if (bs != null) {
      bs.write(out);
    }
    out.write(B_COLON);
    writeValue(out);
    if (DsPerf.ON) DsPerf.stop(DsPerf.URI_WRITE);
  }

  public void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    // DsParameters params

  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    m_strName = null;
    m_strValue = null;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG)
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
    if (DsSipMessage.DEBUG)
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
    if (DsSipMessage.DEBUG) System.out.println();
    return null;
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementFound - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementFound - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println("elementFound - value [offset, count] = [" + offset + ", " + count + "]");
      System.out.println(
          "elementFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
      System.out.println();
    }

    switch (elementId) {
      case URI_SCHEME:
        m_strName = new DsByteString(buffer, offset, count);
        break;
      case URI_DATA:
        m_strValue = new DsByteString(buffer, offset, count);
        break;
    }
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("parameterFound - contextId = [" + contextId + "]");
      System.out.println(
          "parameterFound - name [offset, count] = [" + nameOffset + " ," + nameCount + "]");
      System.out.println(
          "parameterFound - name = ["
              + DsByteString.newString(buffer, nameOffset, nameCount)
              + "]");
      System.out.println(
          "parameterFound - value [offset, count] = [" + valueOffset + ", " + valueCount + "]");
      System.out.println(
          "parameterFound - value = ["
              + DsByteString.newString(buffer, valueOffset, valueCount)
              + "]");
      System.out.println();
    }
  }
}
