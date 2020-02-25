// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This class represents a telephone URL which conforms to URI syntax. In a SIP header, a tel URL is
 * identified by the scheme of "tel". <b>Changes since v5.3</b><br>
 * The telephone subscriber information should be retrieved through the new method
 * getTelephoneSubscriber() and it deprecates the corresponding methods that were used to retrieve
 * and manipulate the telephone subscriber information. This method returns the parsed
 * DsSipTelephoneSubscriber object reference. This telephone subscriber information gets parsed and
 * constructed during the construction of the TEL URL itself. So no parsing happens at this point
 * and the previously parsed object is returned. This object reference can be used to manipulate the
 * telephone subscriber information and it directly manipulates the TEL URL. User need not to any
 * extra operation to reflect the changes in the URL itself as in case of SIP URL, where user has to
 * invoke DsSipURL.setUser(String) to set the manipulated telephone subscriber information.<br>
 */
public class DsTelURL extends DsURI implements Serializable {
  /** The tel subscriber. */
  private DsSipTelephoneSubscriber m_telSubscriber;

  /** Constructs TEL URL with the scheme "tel". */
  public DsTelURL() {
    super();
    m_strName = BS_TEL;
    m_telSubscriber = new DsSipTelephoneSubscriber();
  }

  /**
   * Constructs TEL URL with the scheme "tel" and the specified value.
   *
   * @param value the tel URL to parse, this will work with or without the "tel:", but the preferred
   *     way is to pass it in without the "tel:".
   * @throws DsSipParserException if the parser encounters an error.
   */
  public DsTelURL(DsByteString value) throws DsSipParserException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructs TEL URL with the scheme "tel" and the specified value.
   *
   * @param value the tel URL to parse, this will work with or without the "tel:", but the preferred
   *     way is to pass it in without the "tel:".
   * @throws DsSipParserException if the parser encounters an error.
   */
  public DsTelURL(byte[] value) throws DsSipParserException {
    this(value, 0, value.length);
  }

  /**
   * Constructs TEL URL with the scheme "tel" and the specified value.
   *
   * @param value the tel URL to parse, this will work with or without the "tel:", but the preferred
   *     way is to pass it in without the "tel:".
   * @param offset the start of the tel URL to parse.
   * @param count the number of bytes to parse.
   * @throws DsSipParserException if the parser encounters an error.
   */
  public DsTelURL(byte[] value, int offset, int count) throws DsSipParserException {
    super();
    m_strName = BS_TEL;
    m_telSubscriber = DsSipTelephoneSubscriber.parse(value, offset, count);
  }

  /**
   * Returns the escaped byte string representation of this TEL URL value.
   *
   * @return the escaped byte string representation of this TEL URL value.
   */
  public DsByteString getValue() {
    return getValue(true);
  }

  /**
   * Returns the byte string representation (escaped - if <code>escape</code> is true, unescaped -
   * if <code>escape</code> is false) of this TEL URL value. Returns escaped representation, if the
   * specified parameter <code>escape</code> is <code>true</code>, otherwise returns unescaped byte
   * string representation of this TEL URL's value.
   *
   * @param escape if true then escape the value.
   * @return the byte string representation(escaped - if <code>escape</code> is true, unescaped - if
   *     <code>escape</code> is false) of this TEL URL's value.
   */
  public DsByteString getValue(boolean escape) {
    return m_telSubscriber.toByteString(escape);
  }

  /**
   * Returns the byte string representation(escaped) for this TEL URL.
   *
   * @return the byte string representation(escaped) for this TEL URL.
   */
  public DsByteString toByteString() {
    return toByteString(true);
  }

  /**
   * Returns the byte string representation(escaped or unescaped) for this telephone subscriber
   * information. Returns escaped representation, if the specified parameter <code>escape</code> is
   * <code>true</code>, otherwise returns unescaped string representation of this TEL URL.
   *
   * @param escape if true then escape the value.
   * @return the byte string representation(escaped - if <code>escape</code> is true, unescaped - if
   *     <code>escape</code> is false) for this TEL URL.
   */
  public DsByteString toByteString(boolean escape) {
    ByteBuffer buffer = ByteBuffer.newInstance();
    try {
      write(buffer, escape);
    } catch (IOException ioe) {
    }
    return buffer.getByteString();
  }

  /**
   * Returns the string representation(escaped) for this TEL URL.
   *
   * @return the string representation(escaped) for this TEL URL.
   */
  public String toString() {
    return toString(true);
  }

  /**
   * Returns the string representation(escaped or unescaped) for this TEL URL Returns escaped
   * representation, if the specified parameter <code>escape</code> is <code>true</code>, otherwise
   * returns unescaped string representation of this telephone subscriber information.
   *
   * @param escape if true then escape the value.
   * @return the string representation(escaped - if <code>escape</code> is true, unescaped - if
   *     <code>escape</code> is false) for this TEL URL
   */
  public String toString(boolean escape) {
    DsByteString bs = toByteString(escape);
    return (bs != null) ? bs.toString() : null;
  }

  /**
   * Serializes this TEL URL value (escaped - if <code>escape</code> is true, unescaped - if <code>
   * escape</code> is false) to the specified <code>out</code> output stream.
   *
   * @param out the output stream to which this Telephone Subscriber information will be serialized.
   * @param escape whether the information should be escaped before serializing.
   * @throws IOException if there is an I/O error while writing to the stream
   */
  public void write(OutputStream out, boolean escape) throws IOException {
    DsByteString bs = null;
    bs = getName();
    if (bs != null) {
      bs.write(out);
    }
    out.write((int) B_COLON);
    writeValue(out, escape);
  }

  /**
   * Serializes this TEL URL's value in its escaped form to the specified <code>out</code> output
   * stream.
   *
   * @param out the output stream to which this TEL URL will be serialized.
   * @throws IOException if there is an I/O error while writing to the stream
   */
  public void writeValue(OutputStream out) throws IOException {
    writeValue(out, true);
  }

  /**
   * Serializes this TEL URL value (escaped - if <code>escape</code> is true, unescaped - if <code>
   * escape</code> is false) to the specified <code>out</code> output stream.
   *
   * @param out the output stream to which this Telephone Subscriber information will be serialized.
   * @param escape whether the information should be escaped before serializing.
   * @throws IOException if there is an I/O error while writing to the stream
   */
  public void writeValue(OutputStream out, boolean escape) throws IOException {
    if (m_telSubscriber != null) {
      m_telSubscriber.write(out, escape);
    }
  }

  public void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    m_telSubscriber.writeEncodedParameters(out, md);
  }

  /**
   * Checks if the tel url has parameters.
   *
   * @return <code>true</code> if the URL has parameters, <code>false</code> otherwise
   */
  public boolean hasParameters() {
    return (m_telSubscriber != null) ? m_telSubscriber.hasParameters() : false;
  }

  /**
   * Method used to check for equality of tel URLs.
   *
   * @param obj the tel url to check.
   * @return <code>true</code> if the tel urls are equal, <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj) return true;
    DsTelURL url = null;
    try {
      url = (DsTelURL) obj;
    } catch (ClassCastException cce) {
      return false;
    }
    if (m_telSubscriber == null && url.m_telSubscriber == null) return true;
    if (m_telSubscriber != null && url.m_telSubscriber != null) {
      return m_telSubscriber.equals(url.m_telSubscriber);
    }
    return false;
  }

  /**
   * Clone a tel URL object.
   *
   * @return the cloned tel URL object.
   */
  public Object clone() {
    DsTelURL clone = (DsTelURL) super.clone();
    if (m_telSubscriber != null) {
      clone.m_telSubscriber = (DsSipTelephoneSubscriber) m_telSubscriber.clone();
    }
    return clone;
  }

  /**
   * Method does nothing. Overridden from parent. Scheme must not change for a TEL URL, always
   * "tel".
   *
   * @param scheme the URL scheme.
   */
  public void setName(DsByteString scheme) {
    return;
  }

  /**
   * Returns the telephone subscriber information as a DsSipTelephoneSubscriber object.
   *
   * @return the DsSipTelephoneSubscriber object reference that can be used to manipulate the
   *     telephone subscriber information present in this TEL URL.
   */
  public DsSipTelephoneSubscriber getTelephoneSubscriber() {
    return m_telSubscriber;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    return m_telSubscriber.elementBegin(contextId, elementId);
  }

  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    m_telSubscriber.elementFound(contextId, elementId, buffer, offset, count, valid);
  }

  /*
   * javadoc inherited.
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    m_telSubscriber.parameterFound(
        contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
  }

  /*
   * public static void main(String args[])
   * {
   * try
   * {
   * DsTelURL url = new DsTelURL("<tel: +358-555-1234567;postd=pp22>");
   * DsTelURL url1 = new DsTelURL("tel: +358-555-1234567;phone-context=+3585551234;postd=pp22\n");
   * //DsTelURL url1 = new DsTelURL("tel: 0w003585551234567;postd=pp22\n");
   * //DsTelURL url = new DsTelURL("tel:732-326-1142");
   * // DsTelURL url = new DsTelURL("tel:+358-555-1234567");
   * System.out.println("URL is " + url.getAsString());
   * System.out.println(url1.equals(url));
   * }
   * catch(Exception e)
   * {
   * }
   * }
   */
  /*--
     public static void main(String args[])
      {
          DsTelURL url;
          String urlStr = "tel:+(123)456-7890;isub=01234;postd=567p8w9;phone-context=%2b1234;"
                          + "phone-context=%2b5678;phone-context=%2b9012;"
                          + "tsp=1234;tsp=9012;"
                          + "vnd.company.option=foo";
          try
          {
              if (args.length > 0)
              {
                  urlStr = args[0];
              }
              url = new DsTelURL(urlStr);
              System.out.println("Before parsing :\n" + urlStr);
              System.out.println("After  parsing Escaped:\n"+ url.getAsString());
              System.out.println("After  parsing UnEscaped:\n"+ url.toString(false));
              url.setPhoneParam("phone-context", "123;245;789");
              url.setPhoneParam("isub", "5678");
              url.setPhoneParam("postd", "1234p5w");
              url.setPhoneParam("ext1;ext2;ext3", "val1;val2;val3");
              url.setPhoneParam("tsp", "987;654;321");
              System.out.println("\nGlobal = "+ url.isGlobal());
              System.out.println("\nPhone No = "+ url.getPhoneNumber());
              System.out.println("\nPhone Digits = "+ url.getPhoneDigits());
              System.out.println("\nPost Dial = "+ url.getPostDial());
              System.out.println("\nIsdn Subaddress = "+ url.getIsdnSubaddress());
              System.out.println("\nIsdn Subaddress [param] = "+ url.getPhoneParam("isub"));
              System.out.println("\nPost Dial [param] = "+ url.getPhoneParam("postd"));
              System.out.println("\nService Provider [param] = "+ url.getPhoneParam("tsp"));
              System.out.println("\nArea Specifier [param] = "+ url.getPhoneParam("phone-context"));
              System.out.println("\nFuture Ext [param] = "+ url.getPhoneParam("vnd.company.option"));
              System.out.println("\nFuture Ext [param] = "+ url.getPhoneParam("ext1"));
              System.out.println("\nFuture Ext [param] = "+ url.getPhoneParam("ext2"));
              System.out.println("\nFuture Ext [param] = "+ url.getPhoneParam("ext3"));
          }
        catch(Exception e)
        {
          e.printStackTrace();
        }

        }
  --*/
}
