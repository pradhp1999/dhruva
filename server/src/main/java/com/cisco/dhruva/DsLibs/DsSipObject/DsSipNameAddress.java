// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/** This class represents the display name and URI as they are used in SIP headers. */
public class DsSipNameAddress
    implements Cloneable, DsSipElementListener, Serializable, DsSipConstants {
  /** The display name. */
  private DsByteString m_strName;
  /** The URI. */
  private DsURI m_URI;
  /** <code>true</code> to force the use of angle brackets. */
  private boolean m_bBrackets;

  /** Default constructor. */
  public DsSipNameAddress() {}

  /**
   * Constructs and parses this name address from the specified byte string <code>value</code>.
   *
   * @param value the byte string value that need to be parsed into this name address.
   * @throws DsSipParserException if there is an error while parsing the specified value.
   */
  public DsSipNameAddress(DsByteString value) throws DsSipParserException {
    parse(value);
  }

  /**
   * Constructs this name address with the specified display name value <code>name</code> and the
   * specified URI value <code>uri</code>.
   *
   * @param name the display name for this name address.
   * @param uri the URI for this name address
   */
  public DsSipNameAddress(DsByteString name, DsURI uri) {
    m_strName = name;
    m_URI = uri;
    if (name.length() != 0 || m_URI.hasParameters() == true || m_URI.hasHeaders()) {
      m_bBrackets = true;
    }
  }

  /**
   * Parses the specified byte string <code>value</code> into this name address.
   *
   * @param value the byte string value that need to be parsed into this name address.
   * @throws DsSipParserException if there is an error while parsing the specified value.
   */
  public void parse(DsByteString value) throws DsSipParserException {
    try {
      DsSipMsgParser.parseNameAddr(this, value.data(), value.offset(), value.length());
    } catch (DsSipParserListenerException e) {
      throw new DsSipParserException(e);
    }
  }

  /**
   * Returns the Display name value in this name address.
   *
   * @return the display name value in this name address.
   */
  public DsByteString getDisplayName() {
    return (m_strName);
  }

  /**
   * Tells whether the display name is present in this name address.
   *
   * @return <code>true</code> if there is a Display name, <code>false</code> otherwise
   */
  public boolean hasDisplayName() {
    return (m_strName != null);
  }

  /** Removes the Display name from this name address. */
  public void removeDisplayName() {
    if (m_strName != null) {
      m_strName = null;
      if (!m_URI.hasParameters() && !m_URI.hasHeaders()) {
        m_bBrackets = false;
      }
    }
  }

  /**
   * Sets the Display name for this name address.
   *
   * @param displayName the display name that need to be set in this name address.
   */
  public void setDisplayName(DsByteString displayName) {
    if (displayName != null) // && pDisplayName.length() > 0)
    {
      m_strName = displayName;
      if (!m_bBrackets) {
        m_bBrackets = true;
      }
    }
  }

  /**
   * Sets the bracket presence indicator in the name address.
   *
   * @param brackets <code>true</code> if brackets are present <code>false</code> otherwise
   */
  protected void setBrackets(boolean brackets) {
    if (m_bBrackets != brackets) {
      m_bBrackets = brackets;
    }
  }

  /**
   * Returns the URI in this name address.
   *
   * @return the URI in this name address
   */
  public DsURI getURI() {
    return (m_URI);
  }

  /**
   * Sets the URI in this name address.
   *
   * @param uri the new URI in this name address
   */
  public void setURI(DsURI uri) {
    if (uri == null) {
      throw new IllegalArgumentException("The URI cann't be null");
    }
    m_URI = uri;
    if (m_URI.hasParameters() || m_URI.hasHeaders()) {
      m_bBrackets = true;
    }
  }

  /**
   * Returns the URI value in this name address as a byte string.
   *
   * @return the URI value in this name address as a byte string.
   */
  public DsByteString getURIString() {
    if (m_URI != null) {
      return (m_URI.toByteString());
    } else {
      return null;
    }
  }

  /**
   * Returns the byte string representation of this name address.
   *
   * @return the byte string representation of this name address.
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
   * Returns the string representation of this name address.
   *
   * @return the string representation of this name address.
   */
  public String toString() {
    return toByteString().toString();
  }

  /**
   * Returns the byte string representation of this name address.
   *
   * @return the byte string representation of this name address.
   */
  public DsByteString getValue() {
    return toByteString();
  }

  /**
   * Serializes the value of this name address to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this name address's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void write(OutputStream out) throws IOException {
    boolean brackets = false;
    if (m_strName != null) {
      if (m_strName.length() > 0) {
        m_strName.write(out);
        out.write(B_SPACE);
      }
      m_bBrackets = true; // This should have been true, if we are here.
    }
    if (m_URI != null) {
      // Its okey to add angle brackets every time. No its not!
      if (m_bBrackets || m_URI.hasHeaders() || m_URI.hasParameters()) {
        brackets = true;
      }
      if (brackets) out.write(B_LABRACE);
      m_URI.write(out);
      if (brackets) out.write(B_RABRACE);
    }
  }

  /**
   * Returns a copy of the object.
   *
   * @return the cloned object
   */
  public Object clone() {
    if (DsPerf.ON) DsPerf.start(DsPerf.CLONE_NAME_ADDR);
    DsSipNameAddress address = null;

    try {
      address = (DsSipNameAddress) super.clone();
    } catch (CloneNotSupportedException e) {
    }

    if (m_URI != null) {
      address.m_URI = (DsURI) m_URI.clone();
    }

    if (DsPerf.ON) DsPerf.stop(DsPerf.CLONE_NAME_ADDR);
    return address;
  }

  /**
   * Checks for equality of NameAddresses.
   *
   * @param obj the object to check
   * @return <code>true</code> if the nameaddresses are equal <code>false</code> otherwise
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }
    DsSipNameAddress na = null;
    try {
      na = (DsSipNameAddress) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_URI != null) {
      if (na.m_URI == null) {
        return false;
      }
      if (!m_URI.equals(na.m_URI)) {
        return false;
      }
    } else {
      if (na.m_URI != null) {
        return false;
      }
    }

    if ((m_strName != null) && (m_strName.length() != 0)) {
      if ((na.m_strName == null) || (na.m_strName.length() == 0)) {
        return false;
      }
      if (!m_strName.equals(na.m_strName)) {
        return false;
      }
    } else if ((na.m_strName != null) && (na.m_strName.length() != 0)) {
      return false;
    }
    return true;
  }

  /**
   * Clears all the member data and made this object reusable. The various components (sub-elements)
   * of this object can be set again.
   */
  public void reInit() {
    m_bBrackets = false;
    m_strName = null;
    if (m_URI != null) {
      m_URI.reInit();
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener elementBegin(int contextId, int elementId)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println(
          "elementBegin - contextId = ["
              + contextId
              + "]["
              + DsSipMsgParser.HEADER_NAMES[contextId]
              + "]");
      System.out.println(
          "elementBegin - elementId = ["
              + elementId
              + "]["
              + DsSipMsgParser.ELEMENT_NAMES[elementId]
              + "]");
      System.out.println();
    }

    switch (elementId) {
      case DISPLAY_NAME:
      case URI:
        return this;
      default:
        return null;
    }
  }

  /*
   * javadoc inherited.
   */
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
      case DISPLAY_NAME:
        DsByteString bs = DsByteString.BS_EMPTY_STRING;
        if (count > 0) {
          bs = new DsByteString(buffer, offset, count);
        }
        setDisplayName(bs);
        break;
      case URI:
        try {
          m_URI = DsURI.constructFrom(buffer, offset, count);
        } catch (DsSipParserException e) {
          DsLog4j.messageCat.warn("Exception parsing URI in name address", e);
        }
        break;
      default:
        DsLog4j.messageCat.warn("Unknown element ID (" + elementId + ") in name address");
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

  /*
   * public static void main(String args[])
   * {
   * try
   * {
   * DsSipURL url = new DsSipURL("snayak", "maclaren");
   * url.setMAddrParam("test_MADDr");
   * url.setMethodParam("INVITE");
   * url.setTransportParam(DsSipTransportType.UDP);
   * url.setTTL(100);
   * url.setUser("SAndeep");
   * url.setHeader("head1","test1");
   * url.setHeader("head2","test2");
   *
   * //     DsSipNameAddress nameAdd = new DsSipNameAddress("Edgar <sip:sandeep@user:3000;param1=one;param2=two?header1=hone&header2=htwo>");
   * //     DsSipNameAddress nameAdd = new DsSipNameAddress("<sip:" + InetAddress.getLocalHost().getHostName() + ":"+ 9000 + ">");
   * //     DsSipNameAddress nameAdd = new DsSipNameAddress("Hizir <sip:sandeep@user:3000;param1=one;param2=two?header1=hone&header2=htwo>");
   * DsSipNameAddress nameAdd = new DsSipNameAddress("Sandeep<sip:ak@ericy.com>");
   * //     System.out.println("Username is " + url.getUser());
   * //     System.out.println("Username is " + url.getURLString());
   * System.out.println("nameaddress is " + nameAdd.toString());
   * }
   * catch(Exception e)
   * {
   * }
   * }
   */

  /*
        public static void main(String args[])
        {
        try
        {
        DsSipURL url = new DsSipURL(new DsByteString("snayak"), new DsByteString("maclaren"));
        url.setMAddrParam(new DsByteString("test_MADDr"));
        url.setMethodParam(BS_INVITE);
        url.setTransportParam(DsSipTransportType.UDP);
        url.setTTL(100);
        url.setUser(new DsByteString("SAndeep"));
        url.setHeader(BS_ACK,BS_INVITE);
        url.setHeader(BS_BYE,BS_CANCEL);

        DsSipNameAddress nameAdd1 = new DsSipNameAddress(new DsByteString("Edgar <sip:sandeep@user:3000;param1=one;param2=two?header1=hone&header2=htwo>"));
        DsSipNameAddress nameAdd2 = new DsSipNameAddress(new DsByteString("<sip:" + InetAddress.getLocalHost().getHostName() + ":"+ 9000 + ">"));
        DsSipNameAddress nameAdd3 = new DsSipNameAddress(new DsByteString("Hizir <sip:sandeep@user:3000;param1=one;param2=two?header1=hone&header2=htwo>"));
        DsSipNameAddress nameAdd4 = new DsSipNameAddress(new DsByteString("Sandeep<sip:ak@ericy.com>"));
        //     System.out.println("Username is " + url.getUser());
        //     System.out.println("Username is " + url.getURLString());
        System.out.println("NA1 " + nameAdd1.toString());
        System.out.println("NA2 " + nameAdd2.toString());
        System.out.println("NA3 " + nameAdd3.toString());
        System.out.println("NA4 " + nameAdd4.toString());
        System.out.println("URL is " + url.toString());
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
        }
  */
} // Ends calss DsSipNameAddress
