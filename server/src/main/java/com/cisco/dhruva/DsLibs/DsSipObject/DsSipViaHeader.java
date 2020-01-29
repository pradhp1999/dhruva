// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import org.apache.logging.log4j.Level;

/**
 * This class represents the Via header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Via              =  ( "Via" | "v" ) ":" 1#( sent-protocol sent-by *( ";" via-params ) [ comment ] )
 * via-params       =  via-hidden | via-ttl | via-maddr | via-received | via-branch | via-extension
 * via-hidden       =  "hidden"
 * via-ttl          =   "ttl" "=" ttl
 * via-maddr        =  "maddr" "=" host
 * via-received     =  "received" "=" host [ ":" port ]
 * via-branch       =  "branch" "=" token
 * via-extension    =  token [ "=" ( token | quoted-string ) ]
 * sent-protocol    =  protocol-name "/" protocol-version "/" transport
 * protocol-name    =  "SIP" | token
 * protocol-version =  token
 * transport        =  "UDP" | "TCP" | token
 * sent-by          =  ( host [ ":" port ] ) | ( concealed-host )
 * concealed-host   =  token
 * ttl              =  1*3DIGIT ; 0 to 255
 *
 * hostport         =  host [ ":" port ]
 * host             =  hostname | IPv4address
 * hostname         =  *(domainlabel "." ) toplabel [ "." ]
 * domainlabel      =  alphanum | alphanum *( alphanum | "-" ) alphanum
 * toplabel         =  alpha | alpha *( alphanum | "-" ) alphanum
 * IPv4address      =  1*digit "." 1*digit "." 1*digit "." 1*digit
 * port             =  * digit                                            ; empty port field is allowed
 *
 * comment          =  "(" *(ctext | quoted-pair | comment) ")"
 * ctext            =  &LT any TEXT-UTF8 excluding "(" and ")"&GT
 * quoted-string    =  ( &LT"&GT *(qdtext | quoted-pair ) &LT"&GT )
 * qdtext           =  LWS | %x21 | %x23-5b | %x5d-7e | UTF8-NONASCII
 * quoted-pair      =  "\" CHAR
 * </pre> </code>
 */
public class DsSipViaHeader extends DsSipParametricHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_VIA;
  /** Header ID. */
  public static final byte sID = VIA;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_VIA_C;

  private static final DsByteString DEFAULT_NAME = BS_SIP;
  private static final DsByteString DEFAULT_VERSION = BS_VERSION;
  private static final DsByteString DEFAULT_TRANSPORT = DsSipTransportType.UC_BS_UDP;
  private static final DsByteString DEFAULT_START_CHARS = new DsByteString("SIP/2.0/");
  private static final DsByteString STR_HIDDEN = new DsByteString(";hidden");
  private static final DsByteString STR_TTL = new DsByteString(";ttl=");
  private static final DsByteString STR_MADDR = new DsByteString(";maddr=");
  private static final DsByteString STR_COMP = new DsByteString(";comp=");
  private static final DsByteString STR_RECEIVED = new DsByteString(";received=");
  private static final DsByteString STR_BRANCH = new DsByteString(";branch=");
  private static final DsByteString STR_RPORT = new DsByteString(";rport=");
  private static final DsByteString STR_RPORT_NOVALUE = new DsByteString(";rport");

  private DsByteString m_strProtoName;
  private DsByteString m_strProtoVersion;
  private DsByteString m_strTransport;
  private DsByteString m_strHost;
  private DsByteString m_strComment;
  private DsByteString m_strConcealedHost;
  private DsByteString m_strBranch;
  private int m_Port;
  private boolean isConcealedHost;
  private boolean hasPort;
  private int rPort;

  private static int RPORT_NOT_PRESENT = -1;
  private static int RPORT_NOVALUE = -2;

  public static final byte sFixedFormatHeaderId =
      DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_VIA_HEADER;

  /** Default constructor. */
  public DsSipViaHeader() {
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
  public DsSipViaHeader(byte[] value) throws DsSipParserException, DsSipParserListenerException {
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
  public DsSipViaHeader(byte[] value, int offset, int count)
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
  public DsSipViaHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Constructor added for the Via header hiding.
   *
   * @param concealed_Host the concealed host name.
   * @param transport the m_strTransport type.
   */
  public DsSipViaHeader(DsByteString concealed_Host, int transport) {
    this();
    if (concealed_Host != null) {
      m_strConcealedHost = concealed_Host;
      isConcealedHost = true;
    }
    m_strTransport = DsSipTransportType.getTypeAsUCByteString(transport);
  }

  /**
   * Constructor which accepts the host name, port number and a Transport type.
   *
   * @param host the host name.
   * @param port the port number.
   * @param transport the transport type.
   */
  public DsSipViaHeader(DsByteString host, int port, int transport) {
    this();
    m_strTransport = DsSipTransportType.getTypeAsUCByteString(transport);
    m_Port = port;
    hasPort = true;
    m_strHost = host;
  }

  /**
   * Constructor which accepts the host name, port number and a Transport type.
   *
   * @param host the host name.
   * @param port the port number.
   * @param transport the transport type.
   */
  public DsSipViaHeader(DsByteString host, int port, DsSipTransportType transport) {
    this();
    m_strTransport = DsSipTransportType.getTypeAsUCByteString(transport.getAsInt());
    m_Port = port;
    hasPort = true;
    m_strHost = host;
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return DsByteString The complete token name.
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
    return (isCompact()) ? BS_VIA_C_TOKEN : BS_VIA_TOKEN;
  }

  /**
   * Checks if the hidden parameter is present.
   *
   * @return boolean <b>true</b> if hidden parameter is present or <b>false</b> otherwise.
   */
  public boolean isHiddenPresent() {
    return (getParameter(BS_HIDDEN) != null);
  }

  /**
   * Checks if the port value is set.
   *
   * @return boolean <b>true</b> if port is set else <b>false</b>.
   */
  public boolean isPortPresent() {
    return hasPort;
  }

  /**
   * Retrieves the protocol name.
   *
   * @return The protocol name.
   */
  public DsByteString getProtocolName() {
    return (m_strProtoName);
  }

  /**
   * Retrieves the protocol version.
   *
   * @return The protocol version.
   */
  public DsByteString getProtocolVersion() {
    return m_strProtoVersion;
  }

  /**
   * Gets the transport protocol type.
   *
   * @return the transport protocol.
   */
  public int getTransport() {
    return DsSipTransportType.getTypeAsInt(m_strTransport);
  }

  /**
   * Gets the transport protocol type.
   *
   * @return The transport protocol, as a DsByteString.
   */
  public DsByteString getTransportAsString() {
    return m_strTransport;
  }

  /**
   * Gets the host name.
   *
   * @return the host name.
   */
  public DsByteString getHost() {
    if (isConcealedHost) {
      return m_strConcealedHost;
    }
    return (m_strHost);
  }

  /**
   * Retrieve the host port.
   *
   * @return the port number.
   */
  public int getPort() {
    return (m_Port);
  }

  /**
   * Checks if comment is present.
   *
   * @return <code>true</code> if comment is present, <code>false</code> otherwise.
   */
  public boolean hasComment() {
    return (m_strComment != null);
  }

  /**
   * Retrieves the comment.
   *
   * @return the comment.
   */
  public DsByteString getComment() {
    return m_strComment;
  }

  /**
   * Checks if the hidden parameter is set.
   *
   * @param hidden <code>true</code> if hidden parameter needs to be set or <code>false</code>
   *     otherwise.
   */
  public void setHidden(boolean hidden) {
    if (hidden) {
      setParameter(BS_HIDDEN, DsByteString.BS_EMPTY_STRING);
    } else {
      removeParameter(BS_HIDDEN);
    }
  }

  /**
   * Sets the TTL type.
   *
   * @param ttl the ttl value to be set.
   */
  public void setTTL(DsByteString ttl) {
    setParameter(BS_TTL, ttl);
  }

  /** Removes the ttl parameter. */
  public void removeTTL() {
    removeParameter(BS_TTL);
  }

  /**
   * Sets the Maddress.
   *
   * @param pMaddr the Maddress value to be set.
   */
  public void setMaddr(DsByteString pMaddr) {
    setParameter(BS_MADDR, pMaddr);
  }

  /** Removes the maddr parameter. */
  public void removeMaddr() {
    removeParameter(BS_MADDR);
  }

  /**
   * Sets the Comp value.
   *
   * @param comp the Maddress value to be set.
   */
  public void setComp(DsByteString comp) {
    setParameter(BS_COMP, comp);
  }

  /** Removes the maddr parameter. */
  public void removeComp() {
    removeParameter(BS_COMP);
  }

  /**
   * Sets the received parameter.
   *
   * @param pReceived the received value to be set.
   */
  public void setReceived(DsByteString pReceived) {
    setParameter(BS_RECEIVED, pReceived);
  }

  /** Removes the received parameter. */
  public void removeReceived() {
    removeParameter(BS_RECEIVED);
  }

  /**
   * Sets the concealed host.
   *
   * @param name the concealed host name.
   */
  public void setConcealedHost(DsByteString name) {
    m_strConcealedHost = name;
    isConcealedHost = true;
  }

  /**
   * Sets the Branch parameter.
   *
   * @param pBranch the Branch value to be set.
   */
  public void setBranch(DsByteString pBranch) {
    m_strBranch = pBranch;
  }

  /** Removes the branch parameter. */
  public void removeBranch() {
    if (m_strBranch != null) {
      m_strBranch = null;
    }
  }

  /**
   * Sets the Protocol name.
   *
   * @param pProtocolName the name of the protocol.
   */
  public void setProtocolName(DsByteString pProtocolName) {
    m_strProtoName = pProtocolName;
  }

  /**
   * Sets the protocol version.
   *
   * @param pProtocolVersion the protocol version.
   */
  public void setProtocolVersion(DsByteString pProtocolVersion) {
    m_strProtoVersion = pProtocolVersion;
  }

  /**
   * Sets the transport protocol type.
   *
   * @param pTransport the transport type from DsSipTransportType.
   */
  public void setTransport(DsByteString pTransport) {
    m_strTransport = pTransport;
  }

  /**
   * Sets the transport protocol type.
   *
   * @param type the transport type from DsSipTransportType.
   */
  public void setTransport(int type) {
    m_strTransport = DsSipTransportType.getTypeAsUCByteString(type);
  }

  /**
   * Sets the Host name.
   *
   * @param pHost the Host name.
   */
  public void setHost(DsByteString pHost) {
    m_strHost = pHost;
    isConcealedHost = false;
  }

  /**
   * Sets the port number.
   *
   * @param portno the port number.
   */
  public void setPort(int portno) {
    m_Port = portno;
    hasPort = true;
    isConcealedHost = false;
  }

  /** Removes the port. */
  public void removePort() {
    if (hasPort) {
      hasPort = false;
      m_Port = DsSipURL.DEFAULT_PORT;
    }
  }

  /**
   * Sets the comment.
   *
   * @param acomment The comment.
   */
  public void setComment(DsByteString acomment) {
    m_strComment = acomment;
  }

  /** Removes the comment. */
  public void removeComment() {
    if (m_strComment != null) {
      m_strComment = null;
    }
  }

  /**
   * Checks if TTL is present.
   *
   * @return <code>true</code> if TTL is present, <code>false</code> otherwise.
   */
  public boolean hasTTL() {
    return (getParameter(BS_TTL) != null);
  }

  /**
   * Retrieves the TTL type.
   *
   * @return the TTL type.
   */
  public int getTTL() {
    int ttl = -1;
    DsByteString str = getParameter(BS_TTL);
    if (str != null) {
      ttl = str.parseInt();
    }
    return ttl;
  }

  /**
   * Retrieves the TTL type.
   *
   * @return the TTL type.
   */
  public DsByteString getTTLAsString() {
    return getParameter(BS_TTL);
  }

  /**
   * Checks if Maddr is present.
   *
   * @return <code>true</code> if Maddr is present, <code>false</code> otherwise.
   */
  public boolean hasMaddr() {
    return (getParameter(BS_MADDR) != null);
  }

  /**
   * Retrieves the Maddr value.
   *
   * @return the Maddr value.
   */
  public DsByteString getMaddr() {
    return getParameter(BS_MADDR);
  }

  /**
   * Checks if Comp is present.
   *
   * @return <code>true</code> if Comp is present, <code>false</code> otherwise.
   */
  public boolean hasComp() {
    return (getParameter(BS_COMP) != null);
  }

  /**
   * Retrieves the Comp value.
   *
   * @return the Comp value.
   */
  public DsByteString getComp() {
    return getParameter(BS_COMP);
  }

  /**
   * Checks if received parameter is present.
   *
   * @return <code>true</code> if received parameter is present, <code>false</code> otherwise
   */
  public boolean hasReceived() {
    return (getParameter(BS_RECEIVED) != null);
  }

  /**
   * Retrieves the Received parameter.
   *
   * @return the received parameter.
   */
  public DsByteString getReceived() {
    return getParameter(BS_RECEIVED);
  }

  /**
   * Checks if branch is present.
   *
   * @return <code>true</code> if branch is present, <code>false</code> otherwise.
   */
  public boolean hasBranch() {
    return (m_strBranch != null);
  }

  /**
   * Gets the Branch parameter value.
   *
   * @return the branch parameter value.
   */
  public DsByteString getBranch() {
    return m_strBranch;
  }

  /**
   * Sets the response port 'rport' parameter to the specified value, in this via header.
   *
   * @param port The response port value.
   */
  public void setRPort(int port) {
    rPort = port;
  }

  /** Set an empty rport. */
  public void setRPort() {
    rPort = RPORT_NOVALUE;
  }

  /**
   * Returns the response port 'rport' parameter value if present in this via header.
   *
   * @return the value of the rPort parameter or RPORT_NOVALUE if the parameter has no value or
   *     RPORT_NOT_PRESENT if the rport parameter is not present
   */
  public int getRPort() {
    return rPort;
  }

  /**
   * Tells whether the response 'rport' is present in this via header.
   *
   * @return true if 'rport' is present in this via header, false otherwise
   */
  public boolean isRPortPresent() {
    return (rPort != RPORT_NOT_PRESENT);
  }

  /**
   * Tells whether the response port 'rport' is present in this via header and the port value is not
   * present.
   *
   * @return true if 'rport' is present in this via header but does not contain the port value
   *     itself; false otherwise
   */
  public boolean isRPortNoValue() {
    return (rPort == RPORT_NOVALUE);
  }

  /**
   * Returns true if rPort &GT 0.
   *
   * @return true if rPort &GT 0
   */
  public boolean isRPortSet() {
    return rPort > 0;
  }

  /** Removes the response port 'rport' parameter from this via header, if present. */
  public void removeRPort() {
    rPort = RPORT_NOT_PRESENT;
  }

  /**
   * Sets the extension parameters for this header.
   *
   * @param name The name of the extension parameter to set
   * @param value The value of the extension parameter to set
   */
  public void setExtension(DsByteString name, DsByteString value) {
    setParameter(name, value);
  }

  /**
   * Removes the parameter from the parameter list.
   *
   * @param key the key whose value is to be removed
   */
  public void removeExtension(DsByteString key) {
    removeParameter(key);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (DEFAULT_NAME.equals(m_strProtoName) && DEFAULT_VERSION.equals(m_strProtoVersion)) {
      DEFAULT_START_CHARS.write(out);
    } else {
      if (m_strProtoName != null) {
        m_strProtoName.write(out);
        out.write(B_SLASH);
      }
      if (m_strProtoVersion != null) {
        m_strProtoVersion.write(out);
        out.write(B_SLASH);
      }
    }
    if (m_strTransport != null) {
      m_strTransport.write(out);
      out.write(B_SPACE);
    }
    if (isConcealedHost) {
      m_strConcealedHost.write(out);
    } else {
      if (m_strHost != null) {
        if (m_strHost.indexOf(':') == -1) // no colon means not IPv6
        {
          m_strHost.write(out);
        } else {
          // IPv6 needs []'s
          out.write(B_OPEN_BRACKET);
          m_strHost.write(out);
          out.write(B_CLOSE_BRACKET);
        }
      }
      if (hasPort) {
        out.write(B_COLON);
        out.write(DsIntStrCache.intToBytes(m_Port));
      }
    }
    if (m_strBranch != null) {
      STR_BRANCH.write(out);
      m_strBranch.write(out);
    }

    if (rPort == RPORT_NOVALUE) {
      STR_RPORT_NOVALUE.write(out);
    } else if (rPort > 0) {
      STR_RPORT.write(out);
      DsByteString.valueOf(rPort).write(out);
    }

    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
    if (m_strComment != null && m_strComment.length() > 0) {
      if (m_strComment.charAt(0) == '(') {
        out.write(B_SPACE);
        m_strComment.write(out);
      } else {
        out.write(B_SPACE);
        out.write(B_LBRACE);
        m_strComment.write(out);
        out.write(B_RBRACE);
      }
    }
  }

  //    /**
  //     * Get a string representation of this and all subsequent Via headers, separating each via
  // with a comma and
  //     * terminating the line with "\r\n".
  //     * @return a string representation of this and all subsequent Via headers, separating each
  // via with a comma and
  //     * terminating the line with "\r\n".
  //     */
  /*
      public DsByteString headersToString()
      {
  //        if (DsLog4j.headerCat.isEnabledFor(Level.DEBUG))
  //            sLogger.dsLog(Level.DEBUG, "Entered headersToString()");

          StringBuffer sb = new StringBuffer(64);
          sb.append(DsSipViaHeader.sToken);
          sb.append(':');
          DsSipViaHeader tmp = this;

          // Now serialize me and all of my successors (if any)
          if (listIBelongTo != null)
           {
               for (ListIterator i = listIBelongTo.listIterator (listIBelongTo.indexOf (this)); i.hasNext ();)
               {
                   tmp = (DsSipViaHeader)i.next ();

                   if (!tmp.getIsValidFlag())
                   {
                       sb.append(tmp.getHeaderString());
                   }
                   else
                   {
                       if (tmp.m_strProtoName != null)
                       {
                           sb.append(tmp.m_strProtoName);
                           sb.append('/');
                       }

                       if (tmp.m_strProtoVersion != null)
                       {
                           sb.append(tmp.m_strProtoVersion);
                           sb.append('/');
                       }

                       if (tmp.m_strTransport != null)
                       {
                           sb.append(tmp.m_strTransport);
                           sb.append(' ');
                       }

                       if (tmp.isConcealedHost)
                       {
                           sb.append(tmp.m_strConcealedHost);
                       }
                       else
                       {
                           if (tmp.m_strHost != null)
                           {
                               sb.append(tmp.m_strHost);
                           }
                           if (tmp.hasPort)
                           {
                               sb.append(':');
                               sb.append(tmp.m_Port);
                           }
                       }

                       if (tmp.m_Parameters != null && !tmp.m_Parameters.empty())
                       {
                           sb.append(tmp.m_Parameters.getAsString());
                       }

                       if (tmp.m_strComment != null)
                       {
                           sb.append('(');
                           sb.append(tmp.m_strComment);
                           sb.append(')');
                       }
                   }

                   if (i.hasNext ())
                       sb.append(',');
               }
           }

          // append the promised end of line
          sb.append(HDR_EOL);

  //        if (DsLog4j.headerCat.isEnabledFor(Level.DEBUG))
  //            sLogger.dsLog(Level.DEBUG, "exit headersToString()");

          return new String(sb);
      }
  */

  /**
   * Copy another header's members to me.
   *
   * @param header the header to copy.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipViaHeader source = (DsSipViaHeader) header;
    m_strProtoName = source.m_strProtoName;
    m_strProtoVersion = source.m_strProtoVersion;
    m_strTransport = source.m_strTransport;
    m_strHost = source.m_strHost;
    m_strComment = source.m_strComment;
    m_strBranch = source.m_strBranch;
    m_Port = source.m_Port;
    m_strConcealedHost = source.m_strConcealedHost;
    isConcealedHost = source.isConcealedHost;
    hasPort = source.hasPort;
    rPort = source.rPort;
  }

  /*
   * Makes a copy of the header.
   */
  /*
      public Object clone()
      {
          DsSipViaHeader clone = (DsSipViaHeader)super.clone();
          clone.m_strProtoName = m_strProtoName;
          clone.m_strProtoVersion = m_strProtoVersion;
          clone.m_strTransport = m_strTransport;
          clone.m_strHost = m_strHost;
          clone.m_strComment = m_strComment;
          clone.m_Port = m_Port;
          clone.m_strConcealedHost = m_strConcealedHost;
          clone.isConcealedHost = isConcealedHost;
          clone.hasPort = hasPort;
          clone.rPort = rPort;
          return clone;
      }
  */
  /**
   * Gets the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return VIA;
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
    DsSipViaHeader header = null;
    try {
      header = (DsSipViaHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }
    // for TLS we need to check for the 5061 default port
    if (hasPort && header.hasPort) {
      if (m_Port != header.m_Port) {
        return false;
      }
    } else if (hasPort && !header.hasPort) {
      if (m_Port != 5060) {
        return false;
      }
    } else if (!hasPort && header.hasPort) {
      if (header.m_Port != 5060) {
        return false;
      }
    }

    if (isConcealedHost != header.isConcealedHost) {
      return false;
    }

    if (isConcealedHost) {
      if ((m_strConcealedHost == null || m_strConcealedHost.length() == 0)
          && (header.m_strConcealedHost == null || header.m_strConcealedHost.length() == 0)) {
        // null == "" - this is ok
      } else if (m_strConcealedHost == null
          || !m_strConcealedHost.equals(header.m_strConcealedHost)) {
        return false;
      }
    } else {
      if ((m_strHost == null || m_strHost.length() == 0)
          && (header.m_strHost == null || header.m_strHost.length() == 0)) {
        // null == "" - this is ok
      } else if (m_strHost == null || !m_strHost.equals(header.m_strHost)) {
        return false;
      }
    }

    if ((m_strProtoName == null || m_strProtoName.length() == 0)
        && (header.m_strProtoName == null || header.m_strProtoName.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strProtoName == null || !m_strProtoName.equals(header.m_strProtoName)) {
      return false;
    }

    if ((m_strProtoVersion == null || m_strProtoVersion.length() == 0)
        && (header.m_strProtoVersion == null || header.m_strProtoVersion.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strProtoVersion == null || !m_strProtoVersion.equals(header.m_strProtoVersion)) {
      return false;
    }

    if ((m_strTransport == null || m_strTransport.length() == 0)
        && (header.m_strTransport == null || header.m_strTransport.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strTransport == null || !m_strTransport.equals(header.m_strTransport)) {
      return false;
    }

    if ((m_strComment == null || m_strComment.length() == 0)
        && (header.m_strComment == null || header.m_strComment.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strComment == null || !m_strComment.equals(header.m_strComment)) {
      return false;
    }

    if ((m_strBranch == null || m_strBranch.length() == 0)
        && (header.m_strBranch == null || header.m_strBranch.length() == 0)) {
      // null == "" - this is ok
    } else if (m_strBranch == null || !m_strBranch.equals(header.m_strBranch)) {
      return false;
    }

    if (rPort != header.rPort) {
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
    // else both null - ok

    return true;
  }

  public void writeEncodedHeaderName(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    out.write(sFixedFormatHeaderId);
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    // write the host and port
    if (this.getPort() != DsSipURL.DEFAULT_PORT) {
      md.getEncoding(getHost().copy().append(BS_COLON).append(DsByteString.valueOf(this.getPort())))
          .write(out);
    } else {
      md.getEncoding(this.getHost()).write(out);
    }

    writeEncodedParameters(out, md);
  }

  protected void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    // branch ID
    if (this.getBranch() != null) {
      md.getEncoding(
              this.getBranch().substring(DsTokenSipConstants.s_ViaBranchValueStartBytes.length))
          .write(out);
    } else {
      out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
    }

    // token param
    DsByteString tok = this.getParameter(DsTokenSipConstants.s_TokParamName);
    if (tok != null) {
      if (tok.charAt(0) == '"') {
        md.getEncoding(tok.substring(1, tok.length() - 1)).write(out);
      } else {
        md.getEncoding(tok).write(out);
      }
    } else {
      out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
    }

    if (rPort == RPORT_NOVALUE) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(BS_RPORT).write(out);
      out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
    } else if (rPort > 0) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(BS_RPORT).write(out);
      md.getEncoding(DsByteString.valueOf(rPort)).write(out);
    }

    writeEncodedParameters(out, md, true);
  }

  public int getParamCount() {
    int size = super.getParamCount();
    if (rPort == RPORT_NOVALUE) {
      size++;
    } else if (rPort > 0) {
      size++;
    }
    return size;
  }

  /**
   * Clears all the member data and made this header reusable. The various components (sub-elements)
   * of this headers can be set again. In this case, we reuse already constructed objects like (URI,
   * Name Address, Parameter Tables, Dates, etc), to avoid expensive object creation.
   */
  public void reInit() {
    super.reInit();
    init();
    m_strHost = null;
    m_strConcealedHost = null;
    m_strComment = null;
    isConcealedHost = false;
    hasPort = false;
    m_strBranch = null;
  }

  /** Initialize this instance. */
  // CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
  //  changed to private to allow local access only
  private void init() {
    m_strProtoName = DEFAULT_NAME;
    m_strProtoVersion = DEFAULT_VERSION;
    m_strTransport = DEFAULT_TRANSPORT;
    m_Port = DsSipURL.DEFAULT_PORT;
    rPort = RPORT_NOT_PRESENT;
  }

  /*
      public static void main(String args[])
      {
          try
              {
                  // DsByteArrayInputStream str = new DsByteArrayInputStream("Via: dynamic\r\n".getBytes());
                  // DsCharArrayInputStream str1 = new DsCharArrayInputStream("Via: dynamic:5055\r\n".toCharArray());
                  // DsSipViaHeader header = (DsSipViaHeader) DsSipHeader.constructFrom(str1);
                  DsMessageOutputStream o = new DsMessageOutputStream(System.out);
                  // header.writeExternal (o);
                  // o.flush ();

                  DsSipViaHeader header1 = new DsSipViaHeader("one", 5060, 2);
                  DsSipViaHeader header2 = new DsSipViaHeader("two", 5060, 2);
                  DsSipViaHeader header3 = new DsSipViaHeader("three", 5060, 2);
                  DsSipViaHeader header4 = new DsSipViaHeader("four", 5060, 2);

                  DsSipInviteMessage message = new DsSipInviteMessage ();
                  message.addHeader (header1, false);
                  message.addHeader (header2, false);
                  message.addHeader (header3, false);
                  message.removeHeader (sID);

  //                 System.out.println (message.toString ());
  //                 DsSipMessage clonedMessage = (DsSipMessage)message.clone ();
  //                 System.out.println (clonedMessage);
  //                 message.addHeader (header3, false);
                  System.out.println (">>> Message with extra header: " + message);
  //                 System.out.println (">>> Clone of original message: " + clonedMessage);
              }
          catch(Exception e)
              {
                  System.out.println ("OOPS!");
              }
      }
  */

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
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
      case COMMENT:
        m_strComment = new DsByteString(buffer, offset, count);
        break;
      case HOST:
        m_strHost = new DsByteString(buffer, offset, count);
        break;
      case PORT:
        try {
          setPort(DsSipMsgParser.parseInt(buffer, offset, count));
        } catch (NumberFormatException nfe) {
          throw new DsSipParserListenerException(
              "Exception while constructing the numerical value: ", nfe);
        }
        break;
      case PROTOCOL_NAME:
        m_strProtoName = DsByteString.newInstance(buffer, offset, count);
        break;
      case PROTOCOL_VERSION:
        m_strProtoVersion = DsByteString.newInstance(buffer, offset, count);
        break;
      case TRANSPORT:
        m_strTransport = DsByteString.newInstance(buffer, offset, count);
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
    if (BS_BRANCH.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      m_strBranch = new DsByteString(buffer, valueOffset, valueCount);
    } else if (BS_HIDDEN.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setParameter(BS_HIDDEN, DsByteString.BS_EMPTY_STRING);
    } else if (BS_TTL.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setParameter(BS_TTL, new DsByteString(buffer, valueOffset, valueCount));
    } else if (BS_RECEIVED.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setParameter(BS_RECEIVED, new DsByteString(buffer, valueOffset, valueCount));
    } else if (BS_MADDR.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setParameter(BS_MADDR, new DsByteString(buffer, valueOffset, valueCount));
    } else if (BS_RPORT.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      if (valueCount == 0) {
        rPort = RPORT_NOVALUE;
      } else {
        try {
          rPort = DsSipMsgParser.parseInt(buffer, valueOffset, valueCount);
        } catch (Exception exc) {
          if (DsLog4j.headerCat.isEnabled(Level.WARN))
            DsLog4j.headerCat.log(Level.WARN, "error parsing rport value", exc);
        }
      }
    } else if (BS_COMP.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setParameter(BS_COMP, new DsByteString(buffer, valueOffset, valueCount));
    } else {
      setParameter(
          new DsByteString(buffer, nameOffset, nameCount),
          new DsByteString(buffer, valueOffset, valueCount));
    }
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  ////            byte[] bytes = read();
  //            byte[] bytes = "SIP/2.0/UDP
  // 63.113.46.93:7300;branch=fslflskfsofwefrkweiskdfjsk".getBytes();
  //            DsSipViaHeader header = new DsSipViaHeader(bytes);
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< HEADER >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            header.write(System.out);
  //            System.out.println();
  //            System.out.println();
  //            System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
  //            System.out.println();
  //            DsSipViaHeader clone = (DsSipViaHeader) header.clone();
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
  //            System.out.println();
  //            System.out.println("Parameters = " + header.getParameters());
  //            System.out.println();
  //            System.out.println("Branch Parameter = " + header.getParameter(new
  // DsByteString("branch")));
  //        }
  //        catch(Exception e)
  //        {
  //            e.printStackTrace();
  //        }
  /// *
  //        DsByteString bs = null;
  //        byte[] buffer = { (byte) 'U', (byte) 'D', (byte) 'P'};
  //        byte[] buffer1 = { (byte) '2', (byte) '.', (byte) '0'};
  //        long start = System.currentTimeMillis();
  //        int interval = 500000;
  //        for (int i = 0; i < interval; i++)
  //        {
  //            bs = (DEFAULT_NAME.equalsIgnoreCase(buffer, 0, 3))
  //                                ? DEFAULT_NAME
  //                                : new DsByteString(buffer, 0, 3);
  //        }
  //        long end = System.currentTimeMillis();
  //        long diff1 = end - start;
  //
  //        start = System.currentTimeMillis();
  //        for (int j = 0; j < interval; j++)
  //        {
  //            bs = new DsByteString(buffer, 0, 3);
  //        }
  //        end = System.currentTimeMillis();
  //        long diff3 = end - start;
  //
  //        start = System.currentTimeMillis();
  //        for (int k = 0; k < interval; k++)
  //        {
  //            bs = (DEFAULT_VERSION.equalsIgnoreCase(buffer1, 0, 3))
  //                                ? DEFAULT_VERSION
  //                                : new DsByteString(buffer1, 0, 3);
  //        }
  //        end = System.currentTimeMillis();
  //        long diff2 = end - start;
  //
  //        start = System.currentTimeMillis();
  //        for (int l = 0; l < interval; l++)
  //        {
  //            bs = new DsByteString(buffer1, 0, 3);
  //        }
  //        end = System.currentTimeMillis();
  //        long diff4 = end - start;
  //
  //        System.out.println("Diff1 = " + diff1);
  //        System.out.println("Diff2 = " + diff2);
  //        System.out.println("Diff3 = " + diff3);
  //        System.out.println("Diff4 = " + diff4);
  //        System.out.println("Loss Name = " + (diff3 - diff1));
  //        System.out.println("Loss Version = " + (diff4 - diff2));
  // */
  //    }
} // Ends class DsSipViaHeader
