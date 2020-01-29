// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * An abstract base class representing name address type of header. The headers that can contain a
 * name address (like To, From header) can extend this class.
 */
public abstract class DsSipNameAddressHeader extends DsSipParametricHeader {
  /** The name address value for this header. */
  protected DsSipNameAddress m_nameAddress;

  /** Default constructor. */
  protected DsSipNameAddressHeader() {
    super();
  }

  /*
   CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
       The origianl super() calling will eventually call down to the child and set child's private date member.

  protected DsSipNameAddressHeader(byte[] value)
      throws DsSipParserException, DsSipParserListenerException

  protected DsSipNameAddressHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException

  protected DsSipNameAddressHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException
   */

  /**
   * Constructs this header with the specified <code>nameAddress</code> and the specified <code>
   * parameters</code>. The name address value is first parsed into a valid DsSipNameAddress.
   *
   * @param nameAddress the name address for this header.
   * @param parameters the list of parameters for this header.
   * @throws DsSipParserException if there is an error while parsing the nameAddress value
   */
  protected DsSipNameAddressHeader(DsByteString nameAddress, DsParameters parameters)
      throws DsSipParserException {
    super();
    m_nameAddress = new DsSipNameAddress(nameAddress);
    m_paramTable = parameters;
  }

  /**
   * Constructs this header with the specified <code>nameAddress</code> and the specified <code>
   * parameters</code>.
   *
   * @param nameAddress the name address for this header.
   * @param parameters the list of parameters for this header.
   */
  protected DsSipNameAddressHeader(DsSipNameAddress nameAddress, DsParameters parameters) {
    super();
    m_nameAddress = nameAddress;
    m_paramTable = parameters;
  }

  /**
   * Constructs this header with the specified <code>nameAddress</code>.
   *
   * @param nameAddress the name address for this header.
   */
  protected DsSipNameAddressHeader(DsSipNameAddress nameAddress) {
    this(nameAddress, null);
  }

  /**
   * Constructs this header with the specified <code>uri</code> and the specified <code>parameters
   * </code>.
   *
   * @param uri the uri for this header.
   * @param parameters the list of parameters for this header.
   */
  protected DsSipNameAddressHeader(DsURI uri, DsParameters parameters) {
    super();
    m_paramTable = parameters;
    setURI(uri);
  }

  /**
   * Constructs this header with the specified <code>uri</code>.
   *
   * @param uri the uri for this header.
   */
  protected DsSipNameAddressHeader(DsURI uri) {
    this(uri, null);
  }

  /**
   * Returns the name address.
   *
   * @return the name address.
   */
  public DsSipNameAddress getNameAddress() {
    return (m_nameAddress);
  }

  /**
   * Retrieves the URL as a string.
   *
   * @return the URL value as a string.
   */
  public DsByteString getURIAsString() {
    DsURI uri = null;
    return (m_nameAddress != null && (uri = m_nameAddress.getURI()) != null)
        ? uri.getValue()
        : null;
  }

  /**
   * Retrieves the URI information.
   *
   * @return the URI object.
   */
  public DsURI getURI() {
    if (m_nameAddress != null) {
      return (m_nameAddress.getURI());
    }
    return null;
  }

  /**
   * Method used to set the URL value in a name address.
   *
   * @param aURI the URI value passed.
   */
  public void setURI(DsURI aURI) {
    if (m_nameAddress == null) {
      m_nameAddress = new DsSipNameAddress();
    }
    m_nameAddress.setURI(aURI);
    // To, From, Record, Record-Route headers should always have <> brackets.
    // In case of Contact, <> are must if the URI contains ',' | ';' | '?'.
    // That can be checked in the setURI() method of DsSipNameAddress itself.
    // May be we should check for brackets before serialization only.
    // --        if (getHeaderID() != CONTACT) m_nameAddress.setBrackets(true);
  }

  /**
   * sets the name address.
   *
   * @param nameAdd the name address value.
   */
  public void setNameAddress(DsSipNameAddress nameAdd) {
    if (nameAdd == null) {
      throw new IllegalArgumentException("The name address may not be null");
    } else {
      m_nameAddress = nameAdd;
    }
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipNameAddressHeader source = (DsSipNameAddressHeader) header;
    m_nameAddress = source.m_nameAddress;
  }

  /** This method makes a copy of the header. */
  public Object clone() {
    DsSipNameAddressHeader clonedHeader = (DsSipNameAddressHeader) super.clone();
    if (m_nameAddress != null)
      clonedHeader.m_nameAddress = (DsSipNameAddress) m_nameAddress.clone();
    return clonedHeader;
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
    DsSipNameAddressHeader header = null;
    try {
      header = (DsSipNameAddressHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    if (getHeaderID() != header.getHeaderID()) {
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
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_nameAddress != null) {
      // To, From, Record, Record-Route headers should always have <> brackets.
      // In case of Contact, <> are must if the URI contains ',' | ';' | '?'.
      // That can be checked in the setURI() method of DsSipNameAddress itself.
      // We might have already set it, but it could be possible that user has
      // manipulated this nameaddress after retrieving by getNameAddress().
      // So setting it anyway. :)
      if (getHeaderID() != CONTACT) m_nameAddress.setBrackets(true);
      m_nameAddress.write(out);
    }
    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    DsTokenSipNameAddressEncoder nameAddrEncoder;

    DsByteString tagString = null;
    // get the proper encoding type
    if (DsTokenSipHeaderDictionary.isFixedFormatUriHeader(getHeaderID())) {
      nameAddrEncoder = new DsTokenSipNameAddressFixedFormatEncoder(getNameAddress());

      if ((getHeaderID() == TO) || (getHeaderID() == FROM)) {
        tagString = ((DsSipToFromHeader) this).getTag();
        // todo don't use setTagPresent
        if (tagString != null) {
          ((DsTokenSipNameAddressFixedFormatEncoder) nameAddrEncoder).setTagPresent();
        }
      } else {
        if ((tagString = this.getParameter(BS_TAG)) != null) {
          ((DsTokenSipNameAddressFixedFormatEncoder) nameAddrEncoder).setTagPresent();
        }
      }
    } else {
      nameAddrEncoder = new DsTokenSipNameAddressEncoder(getNameAddress());
    }

    nameAddrEncoder.writeEncoded(out, md);

    if (tagString != null) {
      md.getEncoding(tagString).write(out);
    }

    this.writeEncodedParameters(out, md);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    if (m_nameAddress != null) {
      m_nameAddress.reInit();
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
    DsURI uri = null;
    switch (elementId) {
        /*
        // Not expected
                    case NAME_ADDR_ID:
                        if (m_nameAddress == null)
                        {
                            m_nameAddress = new DsSipNameAddress();
                        }
                        return m_nameAddress;
        */
      case SIP_URL:
        uri = new DsSipURL();
        break;
      case SIPS_URL:
        uri = new DsSipURL(true);
        break;
      case TEL_URL:
        uri = new DsTelURL();
        break;
      case HTTP_URL:
      case UNKNOWN_URL:
        uri = new DsURI();
        break;
    }
    if (null != uri) setURI(uri);
    return uri;
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
      case SIP_URL:
      case TEL_URL:
      case HTTP_URL:
      case UNKNOWN_URL:
        // In this case, we should know whether this header is valid or not.
        //                setValid(valid);
        break;
      case DISPLAY_NAME:
        if (m_nameAddress == null) {
          m_nameAddress = new DsSipNameAddress();
        }
        m_nameAddress.setDisplayName(new DsByteString(buffer, offset, count));
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
} // Ends class DsSipNameAddressHeader
