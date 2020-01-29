// Copyright (c) 2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents the P-DCS-LAES header as specified in PacketCable 1.5 Specifications, CMS
 * to CMS Signaling, PKT-SP-CMSS1.5-I01-050128. This is located at
 * http://www.packetcable.com/downloads/specs/PKT-SP-CMSS1.5-I01-050128.pdf<br>
 * It provides methods to build, access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * P-DCS-LAES   = "P-DCS-LAES" HCOLON Laes-sig *(SEMI Laes-param)
 * Laes-sig     = hostport
 * Laes-param   = Laes-content / Laes-key / Laes-cccid / Laes-bcid / generic-param
 * Laes-content = "content" EQUAL hostport
 * Laes-key     = "key" EQUAL token
 * Laes-bcid    = "bcid" EQUAL 1*48(HEXDIG)
 * Laes-cccid   = "cccid" EQUAL 1*8(HEXDIG)
 * </pre> </code>
 */
public final class DsSipPDCSLAESHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_P_DCS_LAES;
  /** Header ID. */
  public static final byte sID = P_DCS_LAES;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_P_DCS_LAES;

  /** The host. */
  private DsByteString m_host = null;

  /** The port. -1 means port not present. */
  private int m_port = -1;

  /** Default constructor. */
  public DsSipPDCSLAESHeader() {
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
  public DsSipPDCSLAESHeader(byte[] value)
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
  public DsSipPDCSLAESHeader(byte[] value, int offset, int count)
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
  public DsSipPDCSLAESHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Construct this object without parsing.
   *
   * @param host the host
   * @param port the port
   */
  public DsSipPDCSLAESHeader(DsByteString host, int port) {
    m_host = host;
    m_port = port;
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
    return BS_P_DCS_LAES_TOKEN;
  }

  /**
   * Retrieves the host information.
   *
   * @return the host
   */
  public DsByteString getHost() {
    return m_host;
  }

  /**
   * Sets the host information.
   *
   * @param host the new host
   */
  public void setHost(DsByteString host) {
    m_host = host;
  }

  /**
   * Retrieves the port information.
   *
   * @return the port
   */
  public int getPort() {
    return m_port;
  }

  /**
   * Sets the port information.
   *
   * @param port the new port
   */
  public void setPort(int port) {
    m_port = port;
  }

  /**
   * Determines if a port is present in the hostport.
   *
   * @return <code>true</code> if the port is present in the hostport, else <code>false</code>
   */
  public boolean hasPort() {
    return (m_port >= 0);
  }

  /** Removes the port. */
  public void removePort() {
    m_port = -1;
  }

  /**
   * Sets the content parameter.
   *
   * @param content the content parameter value
   */
  public void setContent(DsByteString content) {
    setParameter(BS_CONTENT, content);
  }

  /**
   * Gets the content parameter.
   *
   * @return the content parameter value
   */
  public DsByteString getContent() {
    return getParameter(BS_CONTENT);
  }

  /** Method used to remove the content parameter. */
  public void removeContent() {
    removeParameter(BS_CONTENT);
  }

  /**
   * Sets the key parameter.
   *
   * @param key the key parameter value
   */
  public void setKey(DsByteString key) {
    setParameter(BS_KEY, key);
  }

  /**
   * Gets the key parameter.
   *
   * @return the key parameter value
   */
  public DsByteString getKey() {
    return getParameter(BS_KEY);
  }

  /** Method used to remove the key parameter. */
  public void removeKey() {
    removeParameter(BS_KEY);
  }

  /**
   * Sets the bcid parameter.
   *
   * @param bcid the bcid parameter value
   */
  public void setBcid(DsByteString bcid) {
    setParameter(BS_BCID, bcid);
  }

  /**
   * Gets the bcid parameter.
   *
   * @return the bcid parameter value
   */
  public DsByteString getBcid() {
    return getParameter(BS_BCID);
  }

  /** Method used to remove the bcid parameter. */
  public void removeBcid() {
    removeParameter(BS_BCID);
  }

  /**
   * Sets the cccid parameter.
   *
   * @param cccid the cccid parameter value
   */
  public void setCccid(DsByteString cccid) {
    setParameter(BS_CCCID, cccid);
  }

  /**
   * Gets the cccid parameter.
   *
   * @return the cccid parameter value
   */
  public DsByteString getCccid() {
    return getParameter(BS_CCCID);
  }

  /** Method used to remove the cccid parameter. */
  public void removeCccid() {
    removeParameter(BS_CCCID);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    writeTextBeforeParam(out);

    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  private OutputStream writeTextBeforeParam(OutputStream out) throws IOException {
    if (m_host != null) {
      m_host.write(out);
    }

    if (m_port >= 0) {
      out.write(B_COLON);
      out.write(DsIntStrCache.intToBytes(m_port));
    }

    return out;
  }

  /*
   * javadoc inherited.
   */
  public DsByteString getTextBeforeParam() {
    ByteBuffer buffer = ByteBuffer.newInstance();

    try {
      writeTextBeforeParam(buffer);
    } catch (IOException ioe) {
    }

    return buffer.getByteString();
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipPDCSLAESHeader source = (DsSipPDCSLAESHeader) header;
    m_host = source.m_host;
    m_port = source.m_port;
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return P_DCS_LAES;
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
    DsSipPDCSLAESHeader header;
    try {
      header = (DsSipPDCSLAESHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_host != null && !m_host.equalsIgnoreCase(header.m_host)) {
      return false;
    }

    if (m_port != header.m_port) {
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
    md.getEncoding(getValue()).write(out);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed object like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_host = null;
    m_port = -1;
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
        int last = offset + count;
        int colonIndex = -1;
        for (int i = offset; i < last; i++) {
          if (buffer[i] == ':') {
            colonIndex = i;
            break;
          }
        }

        if (colonIndex == -1) {
          // no port, the whole string is just the host
          m_host = new DsByteString(buffer, offset, count);
        } else {
          // port exists after :
          m_host = new DsByteString(buffer, offset, (colonIndex - offset));
          DsByteString portStr = new DsByteString(buffer, colonIndex + 1, last - (colonIndex + 1));
          m_port = portStr.parseInt();
        }
        break;
      default:
        super.elementFound(contextId, elementId, buffer, offset, count, valid);
    }
  }
}
