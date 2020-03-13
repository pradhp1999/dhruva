// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsHexEncoding;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.transport.Transport;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import gnu.trove.TLinkedList;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ListIterator;
import org.apache.logging.log4j.Level;

/**
 * This class represents the SIP URL as specified in RFC 3261. For a URL in a header, if it has URL
 * parameters, the whole URL must be enclosed in angle brackets. Otherwise, the parameters are
 * treated as header parameters. For a standalone(external) URL, no angle brackets are needed when
 * it has URL parameters.<br>
 * <b>Changes since v5.3</b><br>
 * 1) The return value of function setUserParam(int) is insignificant now and always returns true.
 * As part of initial implementation this method used to parse the user part as the telephone
 * subscriber information if the user param is set to "phone". Now this method never tries to parse
 * the user part irrespective of the user param value. Its is recommended that this function should
 * not be used as rvalue and we may consider this function to return void instead of boolean.
 *
 * <p>2) The parse(String) method used to parse the user part of the SIP URL as telephone subscriber
 * information if the user param value is "phone". Now the parse method never tries to parse the
 * user part as telephone subscriber information. The user part of the SIP URL can be retrieved as
 * telephone subscriber information even if the user param is not "phone" and the recommended way of
 * retrieving the telephone subscriber information is through getTelephoneSubscriber() method. This
 * method tries to parse the user part of the SIP URL and returns a reference to
 * DsSipTelephoneSubscriber object if successful, otherwise DsSipParserException is thrown depicting
 * the reason of failure. User the getTelephoneSubscriberNull() method to avoid the exception and
 * just get null returned instead. The telephone subscriber object that is returned from these
 * methods is now a reference to an internally held object. Changes made to this object will be
 * refelected in the serialized form of this object. Calling setUser() will null out any telephone
 * subscriber object that we previously set.
 */
@SuppressFBWarnings
public class DsSipURL extends DsURI implements Serializable, Cloneable, DsSipHeaderListener {
  // from rfc 3261
  // unreserved =        alphanum / mark
  // mark =              "-" / "_" / "." / "!" / "~" / "*" / "'" / "(" / ")"

  // reserved =          ";" / "/" / "?" / ":" / "@" / "&" / "=" / "+" / "$" / ","
  // user-unreserved =   "&" / "=" / "+" / "$" / "," / ";" / "?" / "/"
  // passwd-unreserved = "&" / "=" / "+" / "$" / ","
  // param-unreserved =  "[" / "]" / "/" / ":" / "&" / "+" / "$"
  // hnv-unreserved =    "[" / "]" / "/" / "?" / ":" / "+" / "$"

  // ASCII chars besides A-Z, a-z, 0-9 including SP
  //
  // all-marks = SP ! " # $ % & ' ( ) * + , - . / : ; < = > ? @ [ \ ] ^ _ ` { | } ~

  // now we subtract 'mark' from all-marks
  // res-marks = SP " # $ % & + , / : ; < = > ? @ [ \ ] ^ ` { | }

  /** The list of chars that must be escaped in the user name of a SIP URL. */
  public static final char[] USER_ESCAPE_CHARS = {
    ' ', '"', '#', '%', ':', '<', '>', '@', '[', '\\', ']', '^', '`', '{', '|', '}'
  };
  /** The list of chars that must be escaped in the password of a SIP URL. */
  public static final char[] PASSWD_ESCAPE_CHARS = {
    ' ', '"', '#', '%', ':', '<', '>', '@', '[', '\\', ']', '^', '`', '{', '|', '}', ';', '?', '/'
  };

  /** The list of chars that must be escaped in a parameter of a SIP URL. */
  public static final char[] PARAM_ESCAPE_CHARS = {
    ' ', '"', '#', '%', ',', ';', '<', '=', '>', '?', '@', '\\', '^', '`', '{', '|', '}'
  };

  /** The list of chars that must be escaped in a header name/value of a SIP URL. */
  public static final char[] HNV_ESCAPE_CHARS = {
    ' ', '"', '#', '%', ',', ';', '<', '=', '>', '&', '@', '\\', '^', '`', '{', '|', '}'
  };

  /** The list of bytes that must be escaped in the user name of a SIP URL. */
  public static final byte[] USER_ESCAPE_BYTES;
  /** The list of bytes that must be escaped in the password of a SIP URL. */
  public static final byte[] PASSWD_ESCAPE_BYTES;
  /** The list of bytes that must be escaped in a parameter of a SIP URL. */
  public static final byte[] PARAM_ESCAPE_BYTES;
  /** The list of bytes that must be escaped in a header name/value of a SIP URL. */
  public static final byte[] HNV_ESCAPE_BYTES;

  /** The list of chars that need to be escaped in at least one part of a SIP URL. */
  private static char[] RESERVED_SUPERSET_CHARS =
      new char[] {
        // old list - but logic was reversed
        // '|', '/', '?', ':', '@', '+', '$', '-', '_', '.', '!', '~', '*', '\'', '(', ')'
        ' ',
        '"',
        '#',
        '$',
        '%',
        '&',
        ',',
        '/',
        ':',
        ';',
        '<',
        '=',
        '>',
        '?',
        '@',
        '[',
        '\\',
        ']',
        '^',
        '`',
        '{',
        '|',
        '}',
      };

  private static byte[] RESERVED_SUPERSET_BYTES;

  /**
   * Tells whether the "lr" parameter in the SIP URL should be just a flag parameter or it should
   * have the value of "true" (like lr=true), when present. The value of "false" for this property
   * means that this parameter should appear like "lr=true", otherwise just a flag parameter in the
   * SIP URL.
   */
  private static boolean EMPTY_LR;

  static {
    RESERVED_SUPERSET_BYTES = new byte[RESERVED_SUPERSET_CHARS.length];
    USER_ESCAPE_BYTES = new byte[USER_ESCAPE_CHARS.length];
    PASSWD_ESCAPE_BYTES = new byte[PASSWD_ESCAPE_CHARS.length];
    PARAM_ESCAPE_BYTES = new byte[PARAM_ESCAPE_CHARS.length];
    HNV_ESCAPE_BYTES = new byte[HNV_ESCAPE_CHARS.length];

    for (int i = 0; i < RESERVED_SUPERSET_BYTES.length; i++) {
      RESERVED_SUPERSET_BYTES[i] = (byte) RESERVED_SUPERSET_CHARS[i];
    }

    for (int i = 0; i < RESERVED_SUPERSET_BYTES.length; i++) {
      RESERVED_SUPERSET_BYTES[i] = (byte) RESERVED_SUPERSET_CHARS[i];
    }

    for (int i = 0; i < USER_ESCAPE_BYTES.length; i++) {
      USER_ESCAPE_BYTES[i] = (byte) USER_ESCAPE_CHARS[i];
    }

    for (int i = 0; i < PASSWD_ESCAPE_BYTES.length; i++) {
      PASSWD_ESCAPE_BYTES[i] = (byte) PASSWD_ESCAPE_CHARS[i];
    }

    for (int i = 0; i < HNV_ESCAPE_BYTES.length; i++) {
      HNV_ESCAPE_BYTES[i] = (byte) HNV_ESCAPE_CHARS[i];
    }

    // Get the lr param property value.
    EMPTY_LR =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_EMPTY_LR, DsConfigManager.PROP_EMPTY_LR_DEFAULT);
  }

  private static ThreadLocal tlWriter = new EscWriterInitializer();

  private DsByteString m_strUser;
  private DsByteString m_strPassword;
  private DsByteString m_strHost;
  private int m_port; // = DsSipURL.DEFAULT_PORT;
  private boolean m_hasPort;
  private DsParameters m_Parameters;
  private DsHeaderParameters m_Headers;
  boolean m_isExternalURL;

  private DsSipTelephoneSubscriber m_telSub;

  // should these be final? -jsm
  /** Ennumerated value for phone type. */
  public static int USER_PHONE = 0;
  /** Ennumerated value for iP type. */
  public static int USER_IP = 1;

  private static final DsByteString BS_TTL = new DsByteString("ttl");
  private static final DsByteString BS_TRANSPORT = new DsByteString("transport");

  /** The default SIP port. */
  public static int DEFAULT_PORT = 5060;

  /**
   * Parses and constructs a new DsSipURL object.
   *
   * @param bytes the byte array from which SIP URL needs to be constructed.
   * @param offset the starting index to parse from.
   * @param count the number of bytes in the URL to parse.
   * @return a DsSipURL object.
   * @throws DsSipParserException if there is an error while parsing
   */
  public static DsSipURL parse(byte[] bytes, int offset, int count) throws DsSipParserException {
    DsSipURL url = new DsSipURL();
    try {
      int skip = skipScheme(bytes, offset, count);
      if (skip == 0) {
        url.m_strName = BS_LSIP;
        DsSipMsgParser.parseSipUrlData(url, url, bytes, offset, count);
      } else {
        DsSipMsgParser.parseSipUrl(url, url, bytes, offset, count);
      }
    } catch (DsSipParserListenerException e) {
      throw new DsSipParserException(e);
    }
    return url;
  }

  /**
   * Parses and constructs a new DsSipURL object.
   *
   * @param bytes the byte array from which SIP URL needs to be constructed.
   * @return a DsSipURL object.
   * @throws DsSipParserException if there is an error while parsing
   */
  public static DsSipURL parse(byte[] bytes) throws DsSipParserException {
    return parse(bytes, 0, bytes.length);
  }

  /** Default constructor. */
  public DsSipURL() {
    m_strName = BS_LSIP;
  }

  /**
   * Constructs the SIP URL object based on whether its secure or not. If the <code>isSecure</code>
   * is true, then SIPS URL will be created, otherwise SIP URL.
   *
   * @param isSecure tells whether the constructed SIP URL is secure or not.
   */
  public DsSipURL(boolean isSecure) {
    m_strName = (isSecure) ? BS_SIPS : BS_LSIP;
  }

  /**
   * Constructor which accepts a username and a hostname.
   *
   * @param username a username
   * @param hostname a hostname
   */
  public DsSipURL(DsByteString username, DsByteString hostname) {
    this();
    m_strUser = username;
    m_strHost = hostname;
  }

  /**
   * Constructor used to set the string to parse.
   *
   * @param pString the string to create a Sip URL from
   * @throws DsSipParserException if there is an error while parsing
   */
  public DsSipURL(DsByteString pString) throws DsSipParserException {
    m_strName = BS_LSIP;
    parse(pString);
  }

  /**
   * Constructs an instance of DsSipURL with the value specified by the <code>bytes</code> byte
   * array.
   *
   * @param bytes the byte array from which SIP URL needs to be constructed.
   * @param offset the starting index to parse from.
   * @param count the number of bytes in the URL to parse.
   * @throws DsSipParserException if there is an error while parsing
   */
  public DsSipURL(byte[] bytes, int offset, int count) throws DsSipParserException {
    m_strName = BS_LSIP;
    try {
      int skip = skipScheme(bytes, offset, count);
      if (skip == 0) {
        DsSipMsgParser.parseSipUrlData(this, this, bytes, offset, count);
      } else {
        DsSipMsgParser.parseSipUrl(this, this, bytes, offset, count);
      }
    } catch (DsSipParserListenerException e) {
      throw new DsSipParserException(e);
    }
  }

  /**
   * Constructs an instance of DsSipURL with the value specified by the <code>bytes</code> byte
   * array.
   *
   * @param bytes the byte array from which SIP URL needs to be constructed.
   * @throws DsSipParserException if there is an error while parsing
   */
  public DsSipURL(byte[] bytes) throws DsSipParserException {
    this(bytes, 0, bytes.length);
  }

  /**
   * Constructs an instance of DsSipURL with the value specified by the <code>url</code> string.
   *
   * @param url the string value that needs to be parsed into a SIP URL.
   * @throws DsSipParserException if there is an error while parsing
   */
  public DsSipURL(String url) throws DsSipParserException {
    this(DsByteString.getBytes(url));
  }

  /**
   * Parses the various components of SIP URL into this object.
   *
   * @param url the byte string from which SIP URL needs to be parsed.
   * @throws DsSipParserException if there is an error while parsing
   */
  public void parse(DsByteString url) throws DsSipParserException {
    try {
      int skip = skipScheme(url.data(), url.offset(), url.length());
      if (skip == 0) {
        DsSipMsgParser.parseSipUrlData(this, this, url.data(), url.offset(), url.length());
      } else {
        DsSipMsgParser.parseSipUrl(this, this, url.data(), url.offset(), url.length());
      }
    } catch (DsSipParserListenerException e) {
      throw new DsSipParserException(e);
    }
  }

  /** Removes the port. */
  public void removePort() {
    m_hasPort = false;
  }

  /**
   * Retrieves the entire SipURL as a string.
   *
   * @return the SipURL string
   */
  public DsByteString getValue() {
    ByteBuffer buffer = ByteBuffer.newInstance();
    try {
      writeValue(buffer);
    } catch (IOException e) {
      // e.printStackTrace();
    }
    return buffer.getByteString();
  }

  /**
   * Serializes the value of this SIP URL to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this SIP URL's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    // User Password part
    if (hasUser()) {
      if (m_telSub != null) {
        m_telSub.write(out);
      } else {
        m_strUser.write(out);
      }

      if (m_strPassword != null) {
        out.write(B_COLON);
        m_strPassword.write(out);
      }

      out.write(B_AT);
    }

    // Host part
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

    // Port
    if (m_hasPort) {
      out.write(B_COLON);
      out.write(DsIntStrCache.intToBytes(getPort()));
    }

    // Parameters
    if (m_Parameters != null) {
      m_Parameters.write(out);
    }

    // Headers
    if (m_Headers != null) {
      out.write(B_QUESTION);
      m_Headers.write(out);
    }
  }

  /**
   * Checks if the URL is a SIP URL.
   *
   * @return <code>true</code> if a sip URL <code>false</code> otherwise
   */
  public final boolean isSipURL() {
    return true;
  }

  /**
   * Tells if there are any parameters in this SIP URL.
   *
   * @return <code>true</code> if there are SIP URL parameters, <code>false</code> otherwise.
   */
  public boolean hasParameters() {
    if (m_Parameters == null) {
      return false;
    }
    return (!m_Parameters.isEmpty());
  }

  /**
   * Returns the parameters that are present in this SIP URL.
   *
   * @return the parameters that are present in this SIP URL.
   */
  public DsParameters getParameters() {
    return m_Parameters;
  }

  /** Removes all the parameters present in this SIP URL. */
  public void removeParameters() {
    if (null != m_Parameters) {
      m_Parameters.clear();
      m_Parameters = null;
    }
  }

  /**
   * Tells whether this SIP URL contains a parameter with the specified. parameter <code>name</code>
   * .
   *
   * @param name the name of the parameter that needs to be checked
   * @return <code>true</code> if a parameter with the specified name is present, <code>false</code>
   *     otherwise.
   */
  public boolean hasParameter(DsByteString name) {
    return (null == m_Parameters) ? false : m_Parameters.isPresent(name);
  }

  /**
   * Returns the parameter value for the parameter with the specified <code>name</code>, if present,
   * otherwise returns <code>null</code>.
   *
   * @param name the name of the parameter that needs to be retrieved
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns <code>null</code>.
   */
  public DsByteString getParameter(DsByteString name) {
    if (m_Parameters != null) {
      return (DsByteString) m_Parameters.get(name);
    }
    return null;
  }

  /**
   * Returns the parameter value for the parameter with the specified <code>name</code>, if present,
   * otherwise returns <code>null</code>. Use this get method only if you do not already have a
   * DsByteString key and only have a String key.
   *
   * @param name the name of the parameter that needs to be retrieved
   * @return the parameter value for the parameter with the specified <code>name</code>, if present,
   *     otherwise returns <code>null</code>.
   */
  public DsByteString getParameter(String name) {
    if (m_Parameters != null) {
      return (DsByteString) m_Parameters.get(name);
    }
    return null;
  }

  /**
   * Sets the specified name-value parameter in this object.
   *
   * @param name the name of the parameter
   * @param value the value of the parameter
   */
  public void setParameter(DsByteString name, DsByteString value) {
    if (m_Parameters == null) {
      m_Parameters = new DsParameters();
    }
    m_Parameters.put(name, value);
  }

  /**
   * Removes the parameter with the specified <code>name</code>, if present.
   *
   * @param name the name of the parameter that needs to be removed
   */
  public void removeParameter(DsByteString name) {
    if (m_Parameters != null) {
      m_Parameters.remove(name);
    }
  }

  /**
   * Tells if there are any headers in this SIP URL.
   *
   * @return <code>true</code> if there are SIP URL headers, <code>false</code> otherwise.
   */
  public boolean hasHeaders() {
    if (m_Headers == null) {
      return false;
    }
    return (!m_Headers.isEmpty());
  }

  /**
   * Returns the headers that are present in this SIP URL.
   *
   * @return the headers that are present in this SIP URL.
   */
  public DsHeaderParameters getHeaders() {
    return m_Headers;
  }

  /**
   * Sets the entire set of headers for this SIP URL. Note that this replaces all headers, including
   * the header container itself, with this new set of header parameters.
   *
   * @param headers the new set of header parameters for this SIP URL.
   */
  public void setHeaders(DsHeaderParameters headers) {
    m_Headers = headers;
  }

  /** Removes all the headers present in this SIP URL. */
  public void removeHeaders() {
    if (m_Headers != null) {
      m_Headers.clear();
      m_Headers = null;
    }
  }

  /**
   * Tells whether this SIP URL contains an header with the specified header <code>name</code>.
   *
   * @param name the name of the header that needs to be checked
   * @return <code>true</code> if a header with the specified name is present, <code>false</code>
   *     otherwise.
   */
  public boolean hasHeader(DsByteString name) {
    return (getHeader(name) != null);
  }

  /**
   * Returns the header value for the header with the specified. <code>name</code>, if present,
   * otherwise returns null.
   *
   * @param name the name of the header that needs to be retrieved
   * @return the header value for the header with the specified <code>name</code>, if present,
   *     otherwise returns null.
   */
  public DsByteString getHeader(DsByteString name) {
    if (m_Headers != null) {
      return m_Headers.get(name);
    }
    return null;
  }

  /**
   * Sets the specified name-value header in this object.
   *
   * @param name the name of the header
   * @param value the value of the header
   */
  public void setHeader(DsByteString name, DsByteString value) {
    if (m_Headers == null) {
      m_Headers = new DsHeaderParameters();
      m_Headers.startWithDelimiter(false);
    }
    m_Headers.put(name, value);
  }

  /**
   * Removes the header with the specified <code>name</code>, if present.
   *
   * @param name the name of the header that needs to be removed
   */
  public void removeHeader(DsByteString name) {
    if (null != m_Headers) {
      m_Headers.remove(name);
    }
  }

  /**
   * Escapes all of the values for all of the headers in this URL. This should be called when the
   * application is done with the message that this URL is part of and is passing it back down to
   * the JUA where the message will be serialized.
   *
   * <p>All characters in <code>HNV_ESCAPE_BYTES</code> will be replaced with their escaped
   * equivalents.
   *
   * <p>NOTE: If this SIP URL has headers, then this header container will be replaced, so if the
   * application maintains a reference to this object, it will no longer be the object that is used
   * to hold headers in this SIP URL.
   */
  public void escapeHeaders() {
    if (!hasHeaders()) {
      return;
    }

    DsHeaderParameters escParams = new DsHeaderParameters();
    escParams.startWithDelimiter(false);

    DsHeaderParameter hp = (DsHeaderParameter) m_Headers.getFirst();
    while (hp != null) {
      TLinkedList valList = hp.getValues();
      DsSipHeaderString origVal = (DsSipHeaderString) valList.getFirst();
      while (origVal != null) {
        DsByteString escVal = getEscapedString(origVal, DsSipURL.HNV_ESCAPE_BYTES);
        escParams.put(hp.getKey(), escVal);

        origVal = (DsSipHeaderString) origVal.getNext();
      }
      hp = (DsHeaderParameter) hp.getNext();
    }

    setHeaders(escParams);
  }

  /**
   * Checks if there is a username. This will also return <code>true</code> if there is a telephone
   * subscriber that has been set.
   *
   * @return <code>true</code> if there is non-empty username or a telephone subscriber is present,
   *     <code>false</code> otherwise.
   */
  public boolean hasUser() {
    if (m_telSub != null || (m_strUser != null && m_strUser.length() > 0)) {
      return true;
    }
    return false;
  }

  /**
   * Returns the non-empty user name, if present in this SIP URL, otherwise returns <code>null
   * </code>. If there is a telelphone subscriber that has been set, then the serialized version of
   * this is returned.
   *
   * @return the non-empty user name, if present in this SIP URL, otherwise returns <code>null
   *     </code>; If there is a elphone subscriber that has been set, then the serialized version of
   *     this object is returned.
   */
  public DsByteString getUser() {
    if (m_telSub != null) {
      return m_telSub.toByteString();
    }

    if (m_strUser != null && m_strUser.length() > 0) {
      return m_strUser;
    }

    return null;
  }

  /**
   * Sets the user name in the SIP URL. When this method is called, the telephone subscriber object
   * is overridden, if one exists.
   *
   * @param user the user name
   */
  public void setUser(DsByteString user) {
    m_strUser = user;
    m_telSub = null;
  }

  /** Removes the user name from the SIP URL. */
  public void removeUser() {
    setUser(null);
  }

  /**
   * Checks if there is a user password.
   *
   * @return <code>true</code> if there is user password, <code>false</code> otherwise
   */
  public boolean hasUserPassword() {
    return (null == m_strPassword) ? false : true;
  }

  /**
   * Gets the user password.
   *
   * @return the user password.
   */
  public DsByteString getUserPassword() {
    return m_strPassword;
  }

  /**
   * Sets the password.
   *
   * @param password the user password.
   */
  public void setUserPassword(DsByteString password) {
    m_strPassword = password;
  }

  /** Removes the user password from the SIP URL. */
  public void removeUserPassword() {
    setUserPassword(null);
  }

  /**
   * Gets the host name.
   *
   * @return the host name.
   */
  public DsByteString getHost() {
    return m_strHost;
  }

  /**
   * Sets the host name in the URL.
   *
   * @param host the host name.
   */
  public void setHost(DsByteString host) {
    m_strHost = host;
  }

  /**
   * Gets the port number.
   *
   * @return the port number. 5060 is the default.
   */
  public int getPort() {
    if (!m_hasPort) {
      return 5060;
    } else {
      return m_port;
    }
  }

  /**
   * Sets the port number.
   *
   * @param portnumber the port number
   */
  public void setPort(int portnumber) {
    m_port = portnumber;
    m_hasPort = true;
  }

  /**
   * Checks if the port is present.
   *
   * @return <code>true</code> if the port is present, <code>false</code> otherwise.
   */
  public boolean hasPort() {
    return m_hasPort;
  }

  /**
   * Returns the binding info value.
   *
   * @return the binding info value.
   * @throws DsSipParserException if the parser encounters an error.
   * @throws UnknownHostException if the host in unknown.
   */
  public DsBindingInfo getBindingInfo() throws DsSipParserException, UnknownHostException {
    String host = DsByteString.toString(getMAddrParam());
    if (null == host) {
      host = DsByteString.toString(getHost());
    }
    // GOGONG - 07.13.05 - Return default outgoing transport if the transport is not specified
    Transport transportParam = getTransportParam();
    return new DsBindingInfo(
        host,
        getPort(),
        isSecure()
            ? Transport.TLS
            : ((transportParam == Transport.NONE)
                ? DsConfigManager.getDefaultOutgoingTransport()
                : transportParam));
  }

  /**
   * Checks if the "ttl" parameter is present in this SIP URL.
   *
   * @return true, if the "ttl" parameter is present, false otherwise
   */
  public boolean hasTTL() {
    return (null == m_Parameters) ? false : m_Parameters.isPresent(BS_TTL);
  }

  /**
   * Returns the 'ttl' parameters value.
   *
   * @return the 'ttl' parameters value.
   */
  public int getTTL() {
    int ttl = 1;

    if (m_Parameters != null) {
      DsByteString value = m_Parameters.get(BS_TTL);

      if (value != null) {
        ttl = value.parseInt();
      }
    }

    return (ttl);
  }

  /**
   * Sets the 'ttl' parameters value.
   *
   * @param TTLValue the 'ttl' parameters value.
   */
  public void setTTL(int TTLValue) {
    if (m_Parameters == null) {
      m_Parameters = new DsParameters();
    }

    m_Parameters.put(BS_TTL, new DsByteString(DsIntStrCache.intToBytes(TTLValue)));
  }

  /** Removes the TTL parameter. */
  public void removeTTL() {
    if (m_Parameters != null) {
      m_Parameters.remove(BS_TTL);
    }
  }

  /**
   * Checks if the transport parameter is present.
   *
   * @return <code>true</code> if the port is present, <code>false</code> otherwise.
   */
  public boolean hasTransport() {
    return (m_Parameters != null && m_Parameters.isPresent(BS_TRANSPORT));
  }

  /**
   * Returns the transport parameter.
   *
   * @return the transport parameter. The method returns NONE if the transport param is not present.
   */
  public Transport getTransportParam() {
    if (DsLog4j.headerCat.isEnabled(Level.DEBUG))
      DsLog4j.headerCat.log(Level.DEBUG, "Retrieving the Transport Type ");

    // GOGONG - 07.13.05 - set the tranport value to NONE if no transport type is specified
    Transport transportValue = Transport.NONE;

    DsByteString value = null;

    if (m_Parameters != null && m_Parameters.isPresent(BS_TRANSPORT)) {
      value = m_Parameters.get(BS_TRANSPORT);
    }

    if (value != null) {
      transportValue = Transport.getTypeAsInt(value);
    }
    if (DsLog4j.headerCat.isEnabled(Level.DEBUG)) {
      DsLog4j.headerCat.log(Level.DEBUG, "Transport type is  " + value);
    }

    return transportValue;
  }

  /**
   * Sets the Transport parameter.
   *
   * @param transport the transport value, as an int
   */
  public void setTransportParam(Transport transport) {
    setParameter(BS_TRANSPORT, Transport.getTypeAsByteString(transport));
  }

  /**
   * Sets the Transport parameter.
   *
   * @param transport the transport value, as a String.
   */
  public void setTransportParam(DsByteString transport) {
    setParameter(BS_TRANSPORT, transport);
  }

  /** Removes the transport parameter. */
  public void removeTransportParam() {
    removeParameter(BS_TRANSPORT);
  }

  /**
   * Checks if the "user" parameter is present in this SIP URL.
   *
   * @return true, if the "user" parameter is present, false otherwise
   */
  public boolean hasUserParam() {
    return hasParameter(BS_USER);
  }

  /**
   * Gets user parameter.
   *
   * @return the user parameter.
   */
  public int getUserParam() {
    int user = USER_IP;
    DsByteString value = getParameter(BS_USER);
    if (value != null && value.equalsIgnoreCase(BS_PHONE)) {
      user = USER_PHONE;
    }
    return user;
  }

  /**
   * Sets the user parameter.
   *
   * @param userParam the user parameter
   */
  public void setUserParam(int userParam) {
    switch (userParam) {
      case 0:
        setParameter(BS_USER, BS_PHONE);
        break;
      default:
        setParameter(BS_USER, BS_IP);
        break;
    }
  }

  /** Removes the user parameter. */
  public void removeUserParam() {
    removeParameter(BS_USER);
  }

  /**
   * Checks if the "method" parameter is present in this SIP URL.
   *
   * @return true, if the "method" parameter is present, false otherwise
   */
  public boolean hasMethodParam() {
    return hasParameter(BS_METHOD);
  }

  /**
   * Gets the method parameters.
   *
   * @return the method parameter.
   * @throws DsSipParserException if there is an error while parsing.
   */
  public DsByteString getMethodParam() throws DsSipParserException {
    return getParameter(BS_METHOD);
  }

  /**
   * Sets the method parameter.
   *
   * @param method the methodParam to be set.
   */
  public void setMethodParam(DsByteString method) {
    setParameter(BS_METHOD, method);
  }

  /** Removes the method parameter. */
  public void removeMethodParam() {
    removeParameter(BS_METHOD);
  }

  /**
   * Checks if the "lr" parameter is present in this SIP URL.
   *
   * @return true, if the "lr" parameter is present, false otherwise.
   */
  public boolean hasLRParam() {
    return hasParameter(BS_LR);
  }

  /** Sets the lr Parameter. */
  public void setLRParam() {
    setParameter(BS_LR, (EMPTY_LR) ? DsByteString.BS_EMPTY_STRING : new DsByteString(BS_TRUE));

    //  An emtpy lr param should work but using "true" here instead since
    //   in testing w/ MS IM, it was found that an empty parameter value was
    //   causing that product to send a 400   -dg
    //
    //   Leave it to the app code to be MS IM compatible if they want to
    // setParameter(BS_LR, new DsByteString("true"));
  }

  /** Removes the lr Parameter. */
  public void removeLRParam() {
    removeParameter(BS_LR);
  }

  /**
   * Checks if the "maddr" parameter is present in this SIP URL.
   *
   * @return true, if the "maddr" parameter is present, false otherwise
   */
  public boolean hasMAddrParam() {
    return hasParameter(BS_MADDR);
  }

  /**
   * Method used to get the Maddr parameters.
   *
   * @return the Maddr parameters.
   */
  public DsByteString getMAddrParam() {
    return getParameter(BS_MADDR);
  }

  /**
   * Sets the MAddr Parameter.
   *
   * @param maddr the maddr parameter.
   */
  public void setMAddrParam(DsByteString maddr) {
    setParameter(BS_MADDR, maddr);
  }

  /** Method used to remove the MAddr parameter. */
  public void removeMAddrParam() {
    removeParameter(BS_MADDR);
  }

  /**
   * Checks if the "comp" parameter is present in this SIP URL.
   *
   * @return true, if the "comp" parameter is present, false otherwise.
   */
  public boolean hasCompParam() {
    return hasParameter(BS_COMP);
  }

  /**
   * Method used to get the Comp parameters.
   *
   * @return the Comp parameters.
   */
  public DsByteString getCompParam() {
    return getParameter(BS_COMP);
  }

  /**
   * Sets the Comp Parameter.
   *
   * @param comp the comp parameter.
   */
  public void setCompParam(DsByteString comp) {
    setParameter(BS_COMP, comp);
  }

  /** Method used to remove the Comp parameter. */
  public void removeCompParam() {
    removeParameter(BS_COMP);
  }

  /**
   * Remove parameters used for routing the message (maddr, transport, port). Used after popping a
   * route into the request URI.
   */
  public void removeRouteParameters() {
    removeMAddrParam();
    removeTransportParam();
    removePort();
  }

  /**
   * Tells whether this SIP URL is semantically equal to the specified object.
   *
   * @param obj the object that needs to be compared with this SIP URL.
   * @return <code>true</code> if the URIs are equal, <code>false</code> otherwise.
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    DsSipURL url = null;
    try {
      url = (DsSipURL) obj;
    } catch (ClassCastException cce) {
      return false;
    }

    // Check for the secure scheme equivalancy
    if (isSecure() != url.isSecure()) {
      return false;
    }

    if (!equalsUnescaped(m_strUser, url.m_strUser)) {
      return false;
    }

    if (!equalsUnescaped(m_strPassword, url.m_strPassword)) {
      return false;
    }

    if (!equalsIgnoreCaseUnescaped(m_strHost, url.m_strHost)) {
      return false;
    }

    if (m_port != url.m_port) {
      return false;
    }

    if ((m_telSub != null && url.m_telSub == null) || (m_telSub == null && url.m_telSub != null)) {
      return false;
    } else if (m_telSub != null && url.m_telSub != null && !m_telSub.equals(url.m_telSub)) {
      return false;
    }

    if (hasParameters() || url.hasParameters()) {
      // compare 'transport' parameter values
      DsByteString param1 = getParameter(DsSipConstants.BS_TRANSPORT);
      DsByteString param2 = url.getParameter(DsSipConstants.BS_TRANSPORT);
      if (!equalsIgnoreCaseUnescaped(param1, param2)) {
        return false;
      }

      // compare 'maddr' parameter values
      param1 = getParameter(BS_MADDR);
      param2 = url.getParameter(BS_MADDR);
      if (!equalsIgnoreCaseUnescaped(param1, param2)) {
        return false;
      }

      // compare 'comp' parameter values
      param1 = getParameter(BS_COMP);
      param2 = url.getParameter(BS_COMP);
      if (!equalsIgnoreCaseUnescaped(param1, param2)) {
        return false;
      }

      // compare 'ttl' parameter values
      param1 = getParameter(DsSipConstants.BS_TTL);
      param2 = url.getParameter(DsSipConstants.BS_TTL);
      if (!equalsIgnoreCaseUnescaped(param1, param2)) {
        return false;
      }

      // compare 'method' parameter values
      param1 = getParameter(BS_METHOD);
      param2 = url.getParameter(BS_METHOD);
      if (!equalsIgnoreCaseUnescaped(param1, param2)) {
        return false;
      }

      // compare for the rest of parameters
      if (m_Parameters != null) {
        DsParameter param = (DsParameter) m_Parameters.getFirst();
        DsByteString key = null;
        DsByteString value = null;
        while (null != param) {
          key = param.getKey();
          // check if we already compared these parameters
          if (BS_MADDR.equalsIgnoreCase(key)
              || BS_COMP.equalsIgnoreCase(key)
              || DsSipConstants.BS_TRANSPORT.equalsIgnoreCase(key)
              || DsSipConstants.BS_TTL.equalsIgnoreCase(key)
              || BS_METHOD.equalsIgnoreCase(key)) {
            param = (DsParameter) param.getNext();
            continue;
          }
          value = url.getParameter(key);
          if (null != value) {
            if (!equalsIgnoreCaseUnescaped(value, param.getValue())) {
              return false;
            }
          }
          param = (DsParameter) param.getNext();
        }
      }
    }

    // Now we have proper URL header parameter comparisons
    if (m_Headers == null) {
      // We do not have headers
      if (url.m_Headers == null || !url.hasHeaders()) {
        // They do not have headers either
        return true;
      } else {
        // They do have headers - can not be equal
        return false;
      }
    }

    if (url.m_Headers == null) {
      if (m_Headers == null || !hasHeaders()) {
        // We do not have headers either
        return true;
      } else {
        // We do have headers - can not be equal
        return false;
      }
    }

    // At this point we know that both m_Headers and url.m_Headers are not null
    if (!hasHeaders() && !url.hasHeaders()) {
      // No headers in either URL
      return true;
    } else if ((!hasHeaders() && url.hasHeaders()) || (hasHeaders() && !url.hasHeaders())) {
      // One has headers and one does not, can not be equal
      return false;
    }

    // Now we know that both URLs have headers.  We must compare each header using
    // the headers .equals() method.

    if (!m_Headers.equals(url.m_Headers)) {
      return false;
    }

    return true;
  }

  /**
   * Canonicalize the URL. The headers in addition to the parameters are removed. The spec describes
   * canonicalized for as:
   *
   * <p>"all URI parameters MUST be removed (including the user-param), and any escaped characters
   * MUST be converted to their unescaped form".
   *
   * @throws DsException if there is an exception unescaping escaped characters
   */
  public void canonicalize() throws DsException {
    if (m_telSub != null) {
      setUser(m_telSub.toByteString());
    }
    m_strUser = getUnescapedString(m_strUser);
    m_strPassword = getUnescapedString(m_strPassword);
    m_Parameters = null;
    // this might not be right, but I am not sure if we should leave
    //   headers if we remove parameters -dg
    m_Headers = null;
  }

  /**
   * Returns the canonicalized form of the URL. The headers in addition to the parameters are
   * removed. The spec describes canonicalized for as:
   *
   * <p>"all URI parameters MUST be removed (including the user-param), and any escaped characters
   * MUST be converted to their unescaped form".
   *
   * <p>the canonicalized form is not cached - it is created each time this method is called.
   *
   * @return the canonicalized form of the URL.
   * @throws DsException if there is an exception unescaping escaped characters
   */
  public DsSipURL getCanonical() throws DsException {
    DsSipURL canonical = (DsSipURL) clone();
    canonical.canonicalize();
    return canonical;
  }

  /**
   * Copy the header parameters of this SIP URL to the request. The following headers are ignored if
   * <code>ignoreRedirectHeaders</code> is set to <code>true</code>:
   *
   * <blockquote>
   *
   * From Call-ID CSeq Via Record-Route Route Accept Accept-Encoding Accept-Language Allow Contact
   * Organization Supported User-Agent
   *
   * </blockquote>
   *
   * @param request the request to which this URI parameters should be copied. If request is <code>
   *     null</code> this method does nothing.
   * @param ignoreRedirectHeaders ignore headers that are not relevant to a redirect
   * @param removeHeaders remove the headers from this URI after they are copied
   */
  public void copyHeadersToRequest(
      DsSipRequest request, boolean ignoreRedirectHeaders, boolean removeHeaders) {
    if (request == null) return;

    DsHeaderParameters headers = getHeaders();
    if (headers != null) {
      headers.copyHeadersToRequest(request, ignoreRedirectHeaders);

      if (removeHeaders) {
        removeHeaders();
      }
    }
  }

  /**
   * Returns a copy of the object.
   *
   * @return the cloned object.
   */
  public Object clone() {
    DsSipURL clone = (DsSipURL) super.clone();

    if (m_Parameters != null) {
      clone.m_Parameters = (DsParameters) m_Parameters.clone();
    }

    if (m_Headers != null) {
      clone.m_Headers = (DsHeaderParameters) m_Headers.clone();
    }

    if (m_telSub != null) {
      clone.m_telSub = (DsSipTelephoneSubscriber) m_telSub.clone();
    }

    return clone;
  }

  /**
   * Method does nothing. Overridden from parent. Name(scheme) must not change for a SIP URL, always
   * "sip".
   *
   * @param name the URL name(scheme)
   */
  public void setName(DsByteString name) {
    return;
  }

  /**
   * Clears all the member data and made this object reusable. The various components (sub-elements)
   * of this object can be set again.
   */
  public void reInit() {
    super.reInit();
    m_strName = BS_LSIP;
    m_strUser = null;
    m_strPassword = null;
    m_strHost = null;

    if (m_Parameters != null) {
      m_Parameters.reInit();
    }

    if (m_Headers != null) {
      m_Headers.reInit();
    }

    m_telSub = null;
  }

  /**
   * Returns the telephone subscriber information, if present, as a DsSipTelephoneSubscriber object.
   * This method tries to parse the user part of this SIP URL as telephone subscriber information.
   * Returns the DsSipTelephoneSubscriber object if successful, throws exception otherwise. The
   * returned DsSipTelephoneSubscriber object reference can be used to manipulate the telephone
   * subscriber information present in this SIP URL.
   *
   * <p>The now returns a reference to a telephone subscriber object that is kept as member data in
   * this object. Changes to this telephone subscriber will be reflected in the serialized version
   * of this object.
   *
   * <p>Note that this is new behavior as of 6.5.0.6. This mirror the behviour that is found in
   * DsTelURL. Also not that calling setUser() will null out the telephone subscriber object. This
   * provide backward compatibility with the old version of this method.
   *
   * @return the DsSipTelephoneSubscriber object reference that can be used to manipulate the
   *     telephone subscriber information present in this SIP URL.
   * @throws DsSipParserException if the user part couldn't be parsed as the valid telephone
   *     subscriber information in case of SIP URL.
   * @see #getTelephoneSubscriberNull
   */
  public DsSipTelephoneSubscriber getTelephoneSubscriber() throws DsSipParserException {
    if (m_telSub != null) {
      return m_telSub;
    }

    if (m_strUser == null || m_strUser.length() == 0) {
      throw new DsSipParserException("There is no user part in this SIP URL");
    }

    DsByteString bs = m_strUser;
    DsSipTelephoneSubscriber ts =
        DsSipTelephoneSubscriber.parse(bs.data(), bs.offset(), bs.length());

    if (!DsSipTelephoneSubscriber.isPhoneNumber(ts.getPhoneNumber())) {
      throw new DsSipParserException(ts.getPhoneNumber() + " is not a valid phone number.");
    }

    m_telSub = ts;
    return m_telSub;
  }

  /**
   * Returns the telephone subscriber information, if present, as a DsSipTelephoneSubscriber object.
   * This method tries to parse the user part of this SIP URL as telephone subscriber information.
   * Returns the DsSipTelephoneSubscriber object if successful, otherwise returns <code>null</code>.
   * The returned DsSipTelephoneSubscriber object reference can be used to manipulate the telephone
   * subscriber information present in this SIP URL.
   *
   * <p>The now returns a reference to a telephone subscriber object that is kept as member data in
   * this object. Changes to this telephone subscriber will be reflected in the serialized version
   * of this object.
   *
   * <p>Note that this is new behavior as of 6.5.0.6. This mirror the behviour that is found in
   * DsTelURL. Also not that calling setUser() will null out the telephone subscriber object. This
   * provide backward compatibility with the old version of this method.
   *
   * @return the DsSipTelephoneSubscriber object reference that can be used to manipulate the
   *     telephone subscriber information present in this SIP URL, or returns <code>null</code> if
   *     there was not a telephon number as the user name.
   * @throws DsSipParserException only if there was truly an exceptional parsing condition
   * @see #getTelephoneSubscriber
   */
  public DsSipTelephoneSubscriber getTelephoneSubscriberNull() throws DsSipParserException {
    if (m_telSub != null) {
      return m_telSub;
    }

    if (m_strUser == null || m_strUser.length() == 0) {
      return null;
    }

    DsByteString bs = m_strUser;
    DsSipTelephoneSubscriber ts =
        DsSipTelephoneSubscriber.parse(bs.data(), bs.offset(), bs.length());

    if (!DsSipTelephoneSubscriber.isPhoneNumber(ts.getPhoneNumber())) {
      return null;
    }

    m_telSub = ts;
    return m_telSub;
  }

  /**
   * Sets the telephone subscriber for this URL. Note that this will null out any previously set
   * user name.
   *
   * @param telSub the new telephone subscriber for this URL
   */
  public void setTelephoneSubscriber(DsSipTelephoneSubscriber telSub) {
    m_telSub = telSub;
    m_strUser = null;
  }

  /**
   * Sets this URI scheme for this SIP URL. If <code>flag == true</code>, then the uri scheme =
   * <code>sips</code>, otherwise it equals <code>sip</code>, which is the default.
   *
   * @param flag the new value for secure.
   */
  public void setSecure(boolean flag) {
    if (flag) {
      m_strName = BS_SIPS;
    } else {
      m_strName = BS_LSIP;
    }
  }

  /**
   * Sets this URI scheme for this SIP URL. If <code>flag == true</code>, then the uri scheme =
   * <code>sips</code>, otherwise it equals <code>sip</code>, which is
   *
   * @return <code>true</code> if the URI scheme is <code>sips</code>, else <code>false</code>
   */
  public boolean isSecure() {
    // == OK since m_strName is only ever set internally - jsm
    return (m_strName == BS_SIPS);
  }

  //    public final static void main(String args[])
  //    {
  //        try
  //        {
  //            DsSipURL url =
  // DsSipURL.parse("sip:aStringWith%5ii%26@dynamicsoft.com:5055;transport=tcp".getBytes());
  //            System.out.println(url);
  //            System.out.println(url.getCanonical().getValue());
  //        }
  //        catch (Exception exc)
  //        {
  //            exc.printStackTrace();
  //        }
  //    }

  /*
   * public static void main(String args[])
   * {
   * try
   * {
   * // DsSipURL url = new DsSipURL("sip:igors@dynamicsoft.com:5055;transport=tcp");
   * //DsSipURL url = new DsSipURL("sip:igors@dynamicsoft.com;transport=tcp");
   * // DsSipURL url = new DsSipURL("sip:contact:password@renault:5080;maddr=sip:contact@renault:5080;transport=udp");
   * // DsSipURL url = new DsSipURL("sip:contact@%65xample:5080;transport=UDP?head1=a&head2=b");
   * //DsSipURL url = new DsSipURL("<sip:user@host.com:5060;transport=udp; maddr=192.168.2.86?loser=true&accept=yes>");
   * DsSipURL urlc = new DsSipURL("sip:user:password@renault:8080?head1=a");
   * //DsSipURL url = new DsSipURL("sip:renault;transport=UDP");
   * DsSipURL url = new DsSipURL("sip:user:password@renault:8080;transport=udp?head1=a&head2=b");
   * //DsSipURL urlc  = new DsSipURL("sip:Contact:password@Renault:5080;maddr=sip:contact@renault:5080;transport=UDP");;
   * if (url.equals(urlc))
   * System.out.println("Equal");
   * System.out.println(url.getHost());
   * System.out.println(url.hasPort());
   * System.out.println(url.getUserPassword());
   * System.out.println(url.getUser());
   * System.out.println(url.getHeader("head1"));
   * System.out.println(url.getParameter("transport"));
   * System.out.println(url.getMAddrParam());
   * //url.removePort();
   * System.out.println(url.getPort());
   * System.out.println(url.getTransportParam());
   * System.out.println(url.getAsString());
   *
   * DsSipURL url1 = new DsSipURL("sip:sandeep@ericy.com:9000;tag=291511;transport=TCP");
   * System.out.println(url1.getHost());
   * url1.removePort();
   * System.out.println(url1.getPort());
   * System.out.println(url1.getTransportParam());
   * System.out.println(url1.getAsString());
   *
   * DsSipURL url2 = new DsSipURL("sip:ericy.com:9000;tag=291511;transport=TCP");
   * System.out.println(url2.getUser());
   * System.out.println(url2.getHost());
   * System.out.println(url2.getPort());
   * System.out.println(url2.getTransportParam());
   *
   * DsSipURL url4 = new DsSipURL("sip:ak@ericy.com;tag=291511");
   * if (url2.equals(url1))
   * {
   * System.out.println(url1.getHost());
   * System.out.println(url1.getPort());
   * System.out.println("URL is " + url1.getAsString());
   * }
   * }
   * catch(Exception e)
   * {
   * }
   *
   * String aStr="aStringWith^&*()and#";
   * String escaped = getEscapedString(aStr);
   * System.out.println("escaped string is <"+escaped+">");
   * String unescaped = getUnescapedString(escaped);
   * System.out.println("unescaped string is <"+unescaped+">");
   * String bStr = "aStringWith%5e%26*()and%23and%56";
   * String unescaped2 = getUnescapedString(bStr);
   * System.out.println("unescaped string is <"+unescaped2+">");
   * }
   */

  /*
      public static void main(String args[])
      {
          DsSipURL url, clone;
          String urlStr = "sip:+1234567890;isub=01234;postd=567p8w9;phone-context=%2b1234;"
                          + "vnd.company.option=foo@renault:8080;"
                          + "transport=udp;user=phone?head1=a&head2=b";
          try
          {

              if (args.length > 0)
                  urlStr = args[0];
              url = new DsSipURL(urlStr);
              System.out.println("Before parsing :\n" + urlStr);
              System.out.println("After  parsing :\n"+ url.getAsString());
              System.out.println("\nUser = "+ url.getUser());
              System.out.println("\nHost = "+ url.getHost());
              System.out.println("\nPort = "+ url.getPort());
              System.out.println("\nTransport = "+ url.getTransportParam());
              System.out.println("\nUser Param = "+ url.getUserParam());
              System.out.println("\nPhone No = "+ url.getPhoneNumber());
              System.out.println("\nPost Dial = "+ url.getPostDial());
              System.out.println("\nIsdn Subaddress = "+ url.getIsdnSubaddress());
              System.out.println("\nGlobal = "+ url.isGlobal());

              clone = (DsSipURL)url.clone();
              System.out.println("EQUALS = " + clone.equals(url));
              System.out.println("EQUALS = " + url.equals(clone));
  //            url.addAreaSpecifier("13579");
              url.removeIsdnSubaddress();
              url.removePostDial();

              System.out.println("EQUALS = " + clone.equals(url));
              System.out.println("EQUALS = " + url.equals(clone));

              System.out.println("\nURL Again :\n" + url.getAsString());
              System.out.println("\nUser = "+ url.getUser());
              System.out.println("\nHost = "+ url.getHost());
              System.out.println("\nPort = "+ url.getPort());
              System.out.println("\nTransport = "+ url.getTransportParam());
              System.out.println("\nUser Param = "+ url.getUserParam());
              System.out.println("\nPhone No = "+ url.getPhoneNumber());
              System.out.println("\nPost Dial = "+ url.getPostDial());
              System.out.println("\nIsdn Subaddress = "+ url.getIsdnSubaddress());
              System.out.println("\nGlobal = "+ url.isGlobal());

              System.out.println("Clone :\n"+ clone.getAsString());
              System.out.println("\nUser = "+ clone.getUser());
              System.out.println("\nHost = "+ clone.getHost());
              System.out.println("\nPort = "+ clone.getPort());

              System.out.println("\nTransport = "+ clone.getTransportParam());
              System.out.println("\nUser Param = "+ clone.getUserParam());
              System.out.println("\nPhone No = "+ clone.getPhoneNumber());
              System.out.println("\nPost Dial = "+ clone.getPostDial());
              System.out.println("\nIsdn Subaddress = "+ clone.getIsdnSubaddress());
              System.out.println("\nGlobal = "+ clone.isGlobal());

          }
        catch(Exception e)
        {
          e.printStackTrace();
        }

        }
  */

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

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
        if (count == 4) // only sip and sips every get here
        {
          m_strName = BS_SIPS;
        }
        // else sip is already set by the constructor or re-init
        break;
      case USERNAME:
        m_strUser = new DsByteString(buffer, offset, count);
        break;
      case PASSWORD:
        m_strPassword = new DsByteString(buffer, offset, count);
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
    }
  }

  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {
    if (BS_TRANSPORT.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setParameter(BS_TRANSPORT, DsByteString.newInstance(buffer, valueOffset, valueCount));
    } else if (BS_MADDR.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setParameter(BS_MADDR, new DsByteString(buffer, valueOffset, valueCount));
    } else if (BS_COMP.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      setParameter(BS_COMP, new DsByteString(buffer, valueOffset, valueCount));
    } else {
      setParameter(
          new DsByteString(buffer, nameOffset, nameCount),
          new DsByteString(buffer, valueOffset, valueCount));
    }
  }

  /**
   * Determines if a char should be escaped based on the escape characters specified in the <code>
   * escapes</code>.
   *
   * @param aChar the char to check.
   * @param escapes a char array containing characters to be escaped.
   * @return <code>true</code> if it needs to be escaped, <code>false</code> otherwise.
   */
  private static boolean needEscape(char aChar, char[] escapes) {
    if (aChar >= 'a' && aChar <= 'z') {
      return false;
    }
    if (aChar >= 'A' && aChar <= 'Z') {
      return false;
    }
    if (aChar >= '0' && aChar <= '9') {
      return false;
    }
    for (int i = 0; i < escapes.length; i++) {
      if (aChar == escapes[i]) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines if a char should be escaped based on the escape characters specified in the <code>
   * escapes</code>.
   *
   * @param aChar the char to check
   * @param escapes a char array containing characters to be escaped.
   * @return <code>true</code> if it needs to be escaped, <code>false</code> otherwise.
   */
  private static boolean needEscape(byte aChar, byte[] escapes) {
    if (aChar >= 'a' && aChar <= 'z') {
      return false;
    }
    if (aChar >= 'A' && aChar <= 'Z') {
      return false;
    }
    if (aChar >= '0' && aChar <= '9') {
      return false;
    }
    for (int i = 0; i < escapes.length; i++) {
      if (aChar == escapes[i]) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets a new string with certain chars escaped when escape is required for them.
   *
   * @param aString the string to be escaped.
   * @return the escaped string.
   */
  public static String getEscapedString(String aString) {
    return getEscapedString(aString, RESERVED_SUPERSET_CHARS);
  }

  /**
   * Gets a new string with certain chars escaped when escape is required for them.
   *
   * @param aString the string to be escaped.
   * @param escapes a char array containing characters to be escaped.
   * @return the escaped string.
   */
  public static String getEscapedString(String aString, char[] escapes) {
    if (aString == null) return null;

    CharArrayWriter writer = writerFactory();

    boolean changed = false;
    final int length = aString.length();

    char ch;

    for (int i = 0; i < length; i++) {
      ch = aString.charAt(i);
      if (needEscape(ch, escapes)) {
        changed = true;

        writer.write('%');

        String hexStr = DsHexEncoding.toHex(ch);

        writer.write(hexStr.charAt(0));
        writer.write(hexStr.charAt(1));
      } else {
        writer.write(ch);
      }
    }

    if (changed) {
      return writer.toString();
    } else {
      return aString;
    }
  }

  /**
   * Gets a new string with escaped chars unescaped.
   *
   * @param aString the string to be unescaped.
   * @return the unescaped string.
   * @throws DsException not thrown.
   */
  public static String getUnescapedString(String aString) throws DsException {
    if (aString == null) return null;

    CharArrayWriter writer = writerFactory();

    boolean changed = false;
    final int length = aString.length();

    char ch;

    for (int i = 0; i < length; i++) {
      ch = aString.charAt(i);

      if (ch == '%') {
        changed = true;

        char b[] = new char[2];
        b[0] = aString.charAt(++i);
        b[1] = aString.charAt(++i);

        writer.write(DsHexEncoding.fromHex(b));
      } else {
        writer.write(ch);
      }
    }

    if (changed) {
      return writer.toString();
    } else {
      return aString;
    }
  }

  /**
   * Gets a new byte string with certain chars escaped when escape is required for them.
   *
   * @param aString the byte string to be escaped.
   * @return the escaped byte string.
   */
  public static DsByteString getEscapedString(DsByteString aString) {
    return getEscapedString(aString, RESERVED_SUPERSET_BYTES);
  }

  /**
   * Gets a new byte string with certain chars escaped when escape is required for them.
   *
   * @param aString the byte string to be escaped.
   * @param escapes a char array containing characters to be escaped.
   * @return the escaped byte string.
   */
  public static DsByteString getEscapedString(DsByteString aString, byte[] escapes) {
    if (aString == null) return null;
    DsByteString byteString = null;
    try (ByteBuffer buffer = new ByteBuffer(128)) {

      final int len = aString.length();
      final int off = aString.offset();
      byte[] charArr1 = aString.data();
      byte ch;

      boolean changed = false;

      for (int i = 0; i < len; i++) {
        ch = charArr1[i + off];

        if (needEscape(ch, escapes)) {
          changed = true;

          buffer.write((byte) '%');
          String hexStr = DsHexEncoding.toHex((char) ch);
          buffer.write((byte) hexStr.charAt(0));
          buffer.write((byte) hexStr.charAt(1));
        } else {
          buffer.write(ch);
        }
      }

      if (changed) {
        return buffer.toByteString();
      } else {
        return aString;
      }
    } catch (IOException ie) {

    }
    return byteString;
  }

  /**
   * Gets a new byte string with escaped chars unescaped.
   *
   * @param aString the byte string to be unescaped.
   * @return the unescaped byte string.
   * @throws DsException not thrown.
   */
  public static DsByteString getUnescapedString(DsByteString aString) throws DsException {
    if (aString == null) return null;
    try (ByteBuffer buffer = new ByteBuffer(128)) {

      final int len = aString.length();
      final int off = aString.offset();
      byte[] charArr1 = aString.data();
      byte ch;

      boolean changed = false;

      for (int i = 0; i < len; i++) {
        ch = charArr1[i + off];

        if (ch == '%') {
          changed = true;

          char b[] = new char[2];
          b[0] = (char) charArr1[++i + off];
          b[1] = (char) charArr1[++i + off];

          buffer.write((byte) DsHexEncoding.fromHex(b));
        } else {
          buffer.write(charArr1[i + off]);
        }
      }

      if (changed) {
        return buffer.toByteString();
      } else {
        return aString;
      }
    } catch (IOException ie) {

    }
    return null;
  }

  /**
   * Compares the specified two strings by taking into consideration that there may be unescaped
   * bytes present in any or both of the strings and a value should be treated equal to its
   * unescaped (% HEX HEX) equivalent.
   *
   * @param first the first unescaped string that needs to be compared
   * @param second the second unescaped string that needs to be compared
   * @return <code>true</code> if both the strings are equal.
   */
  public static boolean equalsUnescaped(DsByteString first, DsByteString second) {
    if (first == second) // it will check the case where both are null
    {
      return true;
    }

    if (first != null && second != null) {
      int off0 = first.offset();
      int len0 = off0 + first.length();
      byte[] b0 = first.data();
      int off1 = second.offset();
      int len1 = off1 + second.length();
      byte[] b1 = second.data();
      byte b00, b11;
      for (; off0 < len0 && off1 < len1; off0++, off1++) {
        b00 = b0[off0];
        b11 = b1[off1];
        if (b00 == '%') {
          if (b11 != '%' && (off0 + 2) < len0) {
            try {
              b00 = DsHexEncoding.fromHex(b0, off0 + 1);
              off0 += 2;
            } catch (Exception exc) {
            }
          }
        } else if (b11 == '%') {
          if ((off1 + 2) < len1) {
            try {
              b11 = DsHexEncoding.fromHex(b1, off1 + 1);
              off1 += 2;
            } catch (Exception exc) {
            }
          }
        }
        if (b00 != b11) return false;
      } // end _for
      return (off1 == len1 && off0 == len0); // both are semantically equal.
    }
    return false; // either of the two is null.
  }

  /**
   * Compares the specified two strings by taking into consideration that there may be unescaped
   * bytes present in any or both of the strings and a value should be treated equal to its
   * unescaped (% HEX HEX) equivalent. Also the comparison is case-insensitive.
   *
   * @param first the first unescaped string that needs to be compared
   * @param second the second unescaped string that needs to be compared
   * @return <code>true</code> if both the strings are equal.
   */
  public static boolean equalsIgnoreCaseUnescaped(DsByteString first, DsByteString second) {
    if (first == second) // it will check the case where both are null
    {
      return true;
    }
    if (first != null && second != null) {
      int off0 = first.offset();
      int len0 = off0 + first.length();
      byte[] b0 = first.data();
      int off1 = second.offset();
      int len1 = off1 + second.length();
      byte[] b1 = second.data();
      byte b00, b11;
      for (; off0 < len0 && off1 < len1; off0++, off1++) {
        b00 = b0[off0];
        b11 = b1[off1];
        if (b00 == '%') {
          if (b11 != '%' && (off0 + 2) < len0) {
            try {
              b00 = DsHexEncoding.fromHex(b0, off0 + 1);
              off0 += 2;
            } catch (Exception exc) {
            }
          }
        } else if (b11 == '%') {
          if ((off1 + 2) < len1) {
            try {
              b11 = DsHexEncoding.fromHex(b1, off1 + 1);
              off1 += 2;
            } catch (Exception exc) {
            }
          }
        }
        if (b00 != b11 && (b00 - 32) != b11 && (b00 + 32) != b11) {
          return false;
        }
      }
      return (off1 == len1 && off0 == len0); // both are semantically equal.
    }
    return false; // either of the two is null.
  }

  // -- for debugging
  //    private static boolean compareUrls(String first, String second)
  //        throws Exception
  //    {
  //        DsSipURL url1 = DsSipURL.parse(DsByteString.getBytes(first));
  //        DsSipURL url2 = DsSipURL.parse(DsByteString.getBytes(second));
  //        return url1.equals(url2);
  //    }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipHeaderListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////

  /*
   * javadoc inherited.
   */
  public DsSipElementListener headerBegin(int headerId) throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("headerBegin = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
    }
    return null;
  }

  /*
   * javadoc inherited.
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    if (DsSipMessage.DEBUG) {
      System.out.println("headerFound - type = [" + DsSipMsgParser.HEADER_NAMES[headerId] + "]");
      System.out.println(
          "headerFound - value = [" + DsByteString.newString(buffer, offset, count) + "]");
    }
    setHeader(DsSipMsgParser.getHeader(headerId), new DsByteString(buffer, offset, count));
  }

  /*
   * javadoc inherited.
   */
  public void unknownFound(
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount,
      boolean isValid)
      throws DsSipParserListenerException {
    setHeader(
        new DsByteString(buffer, nameOffset, nameCount),
        new DsByteString(buffer, valueOffset, valueCount));
  }
  /////////////////////////////////////////////////////////////////////////////////
  // Ends Header Listener handlers
  /////////////////////////////////////////////////////////////////////////////////

  private static CharArrayWriter writerFactory() {
    CharArrayWriter writer = (CharArrayWriter) tlWriter.get();
    writer.reset();
    return writer;
  }

  public void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (getParameters() == null) {
      // spec says to write the # of params, even if it's zero!
      out.write(0);
      return;
    }
    ListIterator params = getParameters().listIterator();
    out.write(getParameters().size());
    while (params.hasNext()) {
      DsParameter param = (DsParameter) params.next();
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAM_MIN);
      md.getEncoding(param.getKey()).write(out);
      if ((param.getValue() != null) && (param.getValue().length() > 0)) {
        md.getEncoding(param.getValue()).write(out);
      } else {
        out.write(DsTokenSipConstants.TOKEN_SIP_NULL);
      }
    }
  }

  private static int skipScheme(byte[] data, int offset, int count) {
    if (count > 4
        && (data[offset] == 's' || data[offset] == 'S')
        && (data[offset + 1] == 'i' || data[offset + 1] == 'I')
        && (data[offset + 2] == 'p' || data[offset + 2] == 'P')
        && (data[offset + 3] == ':')) {
      return 4;
    } else if (count > 5
        && (data[offset] == 's' || data[offset] == 'S')
        && (data[offset + 1] == 'i' || data[offset + 1] == 'I')
        && (data[offset + 2] == 'p' || data[offset + 2] == 'P')
        && (data[offset + 3] == 's' || data[offset + 3] == 'S')
        && (data[offset + 4] == ':')) {
      return 5;
    }

    return 0;
  }
} // Ends class DsSipURL

/** Thread local writer initializer. */
class EscWriterInitializer extends ThreadLocal {
  protected Object initialValue() {
    return new CharArrayWriter(128);
  }
}
