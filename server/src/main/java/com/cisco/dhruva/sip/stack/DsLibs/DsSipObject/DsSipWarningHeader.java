// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * This class represents the Warning header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Warning           =  "Warning" ":" 1#warning-value
 * warning-value     =  warn-code SP warn-agent SP warn-text
 * warn-code         =  3DIGIT
 * warn-agent        =  ( host [ ":" port ] ) | pseudonym
 *                      ; the name or pseudonym of the server adding
 *                      ; the Warning header, for use in debugging
 * warn-text         =  quoted-string
 * pseudonym         =  token
 * </pre> </code>
 */
public final class DsSipWarningHeader extends DsSipHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_WARNING;
  /** Header ID. */
  public static final byte sID = WARNING;
  /** Compact header token. */
  public static final DsByteString sCompactToken = sToken;

  /** Maps codes to their matching text strings. */
  private static HashMap codeMap = initCodeMap();

  private DsByteString m_strCode;
  private DsByteString m_strAgent;
  private DsByteString m_strText;
  private short m_sPort;

  /** Default constructor. */
  public DsSipWarningHeader() {
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
  public DsSipWarningHeader(byte[] value)
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
  public DsSipWarningHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
     * CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
     * The origianl super() calling will eventually call down to the child and set child's private date member.
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
  public DsSipWarningHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructor used to set the code, agent and the text information.
   *
   * @param aCode the code.
   * @param aAgent the agent.
   * @param aText the text.
   */
  public DsSipWarningHeader(DsByteString aCode, DsByteString aAgent, DsByteString aText) {
    super();
    m_strCode = aCode;
    m_strAgent = aAgent;
    m_strText = aText;
  }

  /**
   * Constructor used to set the code, agent. The text is set automatically.
   *
   * @param aCode the code.
   * @param aAgent the agent.
   */
  public DsSipWarningHeader(DsByteString aCode, DsByteString aAgent) {
    super();
    m_strCode = aCode;
    m_strAgent = aAgent;
    m_strText = (DsByteString) codeMap.get(aCode);
    if (m_strText == null) {
      m_strText = BS_UNKNOWN;
    }
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name.
   */
  public DsByteString getToken() {
    return sToken;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name.
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
    return BS_WARNING_TOKEN;
  }

  /**
   * Retrieves the code.
   *
   * @return the code.
   */
  public DsByteString getCode() {
    return m_strCode;
  }

  /**
   * Retrieves the agent.
   *
   * @return the agent.
   */
  public DsByteString getAgent() {
    return m_strAgent;
  }

  /**
   * Retrieves the text.
   *
   * @return the text.
   */
  public DsByteString getText() {
    return m_strText;
  }

  /**
   * Retrieves the port.
   *
   * @return the port.
   */
  public int getPort() {
    return (int) m_sPort;
  }

  /**
   * Determines if a port has been set.
   *
   * @return true if a port has been set, otherwise false, if it is the default port and was not
   *     specified
   */
  public boolean hasPort() {
    return (m_sPort > 0);
  }

  /**
   * Sets the code.
   *
   * @param aCode the code.
   */
  public void setCode(DsByteString aCode) {
    m_strCode = aCode;
    DsByteString autoText = (DsByteString) codeMap.get(aCode);
    if (autoText != null) {
      m_strText = autoText;
    }
  }

  /**
   * Sets the agent.
   *
   * @param aAgent the agent.
   */
  public void setAgent(DsByteString aAgent) {
    m_strAgent = aAgent;
  }

  /**
   * Sets the text.
   *
   * @param aText the text.
   */
  public void setText(DsByteString aText) {
    m_strText = aText;
  }

  /**
   * Sets the port.
   *
   * @param port the port.
   */
  public void setPort(int port) {
    m_sPort = (short) port;
  }

  /** Removes the port. */
  public void removePort() {
    m_sPort = 0;
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_strCode != null) {
      m_strCode.write(out);
    }
    if (m_strAgent != null) {
      out.write(B_SPACE);
      if (m_strAgent.indexOf(':') == -1) // no colon means not IPv6
      {
        m_strAgent.write(out);
      } else {
        // IPv6 needs []'s
        out.write(B_OPEN_BRACKET);
        m_strAgent.write(out);
        out.write(B_CLOSE_BRACKET);
      }

      if (m_sPort > 0) {
        out.write(B_COLON);
        out.write(DsIntStrCache.intToBytes(m_sPort));
      }
    }
    if (m_strText != null) {
      out.write(B_SPACE);
      m_strText.write(out);
    }
  }

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipWarningHeader source = (DsSipWarningHeader) header;

    m_strCode = source.m_strCode;
    m_strAgent = source.m_strAgent;
    m_strText = source.m_strText;
    m_sPort = source.m_sPort;
  }

  /** This method makes a copy of the header. */
  /*    public Object clone()
  {
      DsSipWarningHeader clone = (DsSipWarningHeader)super.clone();
      if (m_strCode != null)
      {
          clone.m_strCode = (DsByteString)m_strCode.clone();
      }
      if (m_strAgent != null)
      {
          clone.m_strAgent = (DsByteString)m_strAgent.clone();
      }
      if (m_strText != null)
      {
          clone.m_strText = (DsByteString)m_strText.clone();
      }
      clone.m_sPort = m_sPort;
      return clone;
  }
  */

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return WARNING;
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
    DsSipWarningHeader header = null;
    try {
      header = (DsSipWarningHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_sPort != header.m_sPort) {
      return false;
    }
    if (!DsByteString.equals(m_strCode, header.m_strCode)) {
      return false;
    }
    if (!DsByteString.equals(m_strAgent, header.m_strAgent)) {
      return false;
    }
    if (!DsByteString.equals(m_strText, header.m_strText)) {
      return false;
    }
    return true;
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    md.getEncoding(m_strCode).write(out);
    md.getEncoding(m_strAgent).write(out);
    md.getEncoding(m_strText).write(out);
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    m_strCode = null;
    m_strAgent = null;
    m_strText = null;
  }

  private static HashMap initCodeMap() {
    HashMap map = new HashMap();

    map.put(
        new DsByteString("300"),
        new DsByteString(
            "Incompatible network protocol: One or more network protocols contained in the session description are not available."));
    map.put(
        new DsByteString("301"),
        new DsByteString(
            "Incompatible network address formats: One or more network address formats contained in the session description are not available."));
    map.put(
        new DsByteString("302"),
        new DsByteString(
            "Incompatible transport protocol: One or more transport protocols described in the session description are not available."));
    map.put(
        new DsByteString("303"),
        new DsByteString(
            "Incompatible bandwidth units: One or more bandwidth measurement units contained in the session description were not understood."));
    map.put(
        new DsByteString("304"),
        new DsByteString(
            "Media type not available: One or more media types contained in the session description are not available."));
    map.put(
        new DsByteString("305"),
        new DsByteString(
            "Incompatible media format: One or more media formats contained in the session description are not available."));
    map.put(
        new DsByteString("306"),
        new DsByteString(
            "Attribute not understood: One or more of the media attributes in the session description are not supported."));
    map.put(
        new DsByteString("307"),
        new DsByteString(
            "Session description parameter not understood: A parameter other than those listed above was not understood."));
    map.put(
        new DsByteString("330"),
        new DsByteString(
            "Multicast not available: The site where the user is located does not support multicast."));
    map.put(
        new DsByteString("331"),
        new DsByteString(
            "Unicast not available: The site where the user is located does not support unicast communication (usually due to the presence of a firewall)."));
    map.put(
        new DsByteString("370"),
        new DsByteString(
            "Insufficient bandwidth: The bandwidth specified in the session description or defined by the media exceeds that known to be available."));
    map.put(new DsByteString("399"), new DsByteString("Miscellaneous warning:"));

    return map;
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
      case WARN_CODE:
        m_strCode = new DsByteString(buffer, offset, count);
        break;
      case HOST:
        m_strAgent = new DsByteString(buffer, offset, count);
        break;
      case WARN_TEXT:
        m_strText = new DsByteString(buffer, offset, count);
        break;
      case PORT:
        try {
          m_sPort = (short) DsSipMsgParser.parseInt(buffer, offset, count);
        } catch (NumberFormatException nfe) {
          throw new DsSipParserListenerException(
              "Exception while constructing the numerical value: ", nfe);
        }
        break;
    }
  }
} // Ends class DsSipWarningHeader
