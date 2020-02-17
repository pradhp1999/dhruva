// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSecurity.DsCert.SubjectAltName;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipMime.DsMimeEntity;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ReadOnly.DsSipReadOnlyElement;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.ReadOnly.DsSipURIElements;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipFrameStream;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSDPMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipHeaderDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipInteger;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMethodDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsHexEncoding;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface.SipMsgNormalizationState;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsNetwork;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsPerf;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsSSLBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsString;
import com.cisco.dhruva.util.cac.SIPSession;
import com.cisco.dhruva.util.cac.SIPSessionID;
import com.cisco.dhruva.util.cac.SIPSessions;
import com.cisco.dhruva.util.cac.SessionStateType;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/** javadoc inherited. */
// CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
public abstract class DsSipMessage extends DsSipMessageBase {
  static final String REGEX_CRYPT_LINE = "(a=crypto.*inline:).*";
  static final String REGEX_CRYPT_MASK = "$1********MASKED***************";

  /* constants used for wrapping message output */
  private static final String NEW_LINE_REGEX_PATTERN = "[\\t\\n\\r]+";
  private static final String NEW_LINE_REPLACEMENT_DELIMITER = " ";
  private static final String BEGIN_SIP_MESSAGE = "BEGIN SIP MESSAGE:";
  private static final String END_SIP_MESSAGE = "END SIP MESSAGE:";
  private static final String SDP_SPLIT_STRING = "v=";

  // Set the logging category
  protected static Logger Log = DsLog4j.messageCat;

  public static final String CN = "cn";

  public static final String EQUAL = "=";

  public static final String SAN = "san";

  public static final String LEFT_ANGLE_BRACKET = "<";

  public static final String RIGHT_ANGLE_BRACKET = ">";

  public static final String X_CISCO_CERT_HEADER = "X-Cisco-Peer-Cert-Info";

  public static final String SPACE = " ";

  public static final String COMMA = ",";

  // Max size for individual cert header
  private static final int CERT_HEADER_SIZE_MAX = 256;

  // Max size of all the cert headers combined
  private static final int CERT_HEADER_TOTAL_SIZE_MAX = 4096;

  private static final int COMMA_SPACE_SIZE = 2;

  ////////////////////////////////////////////////////////////////////////////////
  // static functions
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Sets the option to use VIA branch parameter as the transaction key as per the SIP RFC 3261. If
   * set to true then, VIA branch parameter value will be used as the transaction key as per SIP RFC
   * 3261, otherwise the classic style of the key, as specified in the 2543-bis-04 SIP draft, will
   * be used.
   *
   * @param policy the message key policy option that need to be set.
   * @see #isNewKeyPolicy()
   */
  public static void setNewKeyPolicy(boolean policy) {
    NEW_KEY = policy;
  }

  /**
   * Tells whether the stack uses VIA branch parameter as the transaction key as per the 2543-bis-07
   * SIP draft. If set to true then, VIA branch parameter value will be used as the transaction key
   * as per 2543-bis-07 SIP draft, otherwise the classic style of the key, as specified in the
   * 2543-bis-04 SIP draft, will be used.
   *
   * @return the message key policy option that is being used.
   * @see #setNewKeyPolicy(boolean)
   */
  public static boolean isNewKeyPolicy() {
    return NEW_KEY;
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public static void initDeepHeaders(int[] ids)
  */

  /**
   * Sets the default header priority level. This priority level means that all the headers, that
   * are having priority less than and equal to this priority level, are having direct access for
   * fast retrieval.<br>
   * It is recommended that the frequently accessed headers should fall under this priority level
   * for fast access.
   *
   * @param level the default header priority level that need to be set for all the messages
   *     globally.
   */
  public static void setHeaderPriority(int level) {
    // If the priority level <= 0, it means no direct access array is required.
    HEADER_PRIORITY_LEVEL = (level < 0) ? 0 : (level + 1);
  }

  /**
   * Tells the default header priority level. This priority level means that all the headers, that
   * are having priority less than and equal to this priority level, are having direct access for
   * fast retrieval.<br>
   * It is recommended that the frequently accessed headers should fall under this priority level
   * for fast access.
   *
   * @return the default header priority level that has been set for all the messages globally.
   */
  public static int getHeaderPriority() {
    return HEADER_PRIORITY_LEVEL - 1;
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public static void setCommaSeparated(boolean option)
      public static boolean isCommaSeparated()
      public static void setParseAllHeaders(boolean enable)
      public static boolean isParseAllHeaders()
  */

  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  public static DsSipMessage createMessage(byte[] bytes)
      throws DsSipParserListenerException, DsSipParserException {
    return createMessage(bytes, 0, bytes.length);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array. If the
   * specified flag <code>createKey</code> is <code>true</code> then the message key will also be
   * parsed while parsing the message. Similarly if the specified flag <code>validate</code> is
   * <code>true</code> then the parsed message will also be validated.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @param createKey if <code>true</code>, the message key will also be generated while parsing the
   *     message.
   * @param validate if <code>true</code> then the message will also be validated after parsing.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  public static DsSipMessage createMessage(byte[] bytes, boolean createKey, boolean validate)
      throws DsSipParserListenerException, DsSipParserException {
    return createMessage(bytes, 0, bytes.length, createKey, validate);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @param offset the offset where from the message bytes start in the specified <code>bytes</code>
   *     byte array.
   * @param count the number of bytes beginning from the offset that needs to be considered while
   *     parsing the message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  public static DsSipMessage createMessage(byte[] bytes, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    return DsSipDefaultMessageFactory.getInstance().createMessage(bytes, offset, count);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>in</code> input stream.
   *
   * @param in the input stream where from bytes need to be read for parsing SIP message.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   * @throws IOException if there is an error while reading from the specified input stream.
   */
  public static DsSipMessage createMessage(DsSipFrameStream in)
      throws DsSipParserListenerException, DsSipParserException, IOException {
    return DsSipDefaultMessageFactory.getInstance().createMessage(in);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>bytes</code> byte array. If the
   * specified flag <code>createKey</code> is <code>true</code> then the message key will also be
   * parsed while parsing the message. Similarly if the specified flag <code>validate</code> is
   * <code>true</code> then the parsed message will also be validated.
   *
   * @param bytes the byte array that needs to be parsed as a SIP message.
   * @param offset the offset where from the message bytes start in the specified <code>bytes</code>
   *     byte array.
   * @param count the number of bytes beginning from the offset that needs to be considered while
   *     parsing the message.
   * @param createKey if <code>true</code>, the message key will also be generated while parsing the
   *     message.
   * @param validate if <code>true</code> then the message will also be validated after parsing.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   */
  public static DsSipMessage createMessage(
      byte[] bytes, int offset, int count, boolean createKey, boolean validate)
      throws DsSipParserListenerException, DsSipParserException {
    return DsSipDefaultMessageFactory.getInstance()
        .createMessage(bytes, offset, count, createKey, validate);
  }

  /**
   * Parses and Creates a DsSipMessage from the specified <code>in</code> input stream. If the
   * specified flag <code>createKey</code> is <code>true</code> then the message key will also be
   * parsed while parsing the message. Similarly if the specified flag <code>validate</code> is
   * <code>true</code> then the parsed message will also be validated.
   *
   * @param in the input stream where from bytes need to be read for parsing SIP message.
   * @param createKey if <code>true</code>, the message key will also be generated while parsing the
   *     message.
   * @param validate if <code>true</code> then the message will also be validated after parsing.
   * @return the parsed SIP Message.
   * @throws DsSipParserException if there is an error while parsing the specified byte array as a
   *     SIP Message.
   * @throws DsSipParserListenerException if there is an error condition detected by the SIP Message
   *     listener, while parsing.
   * @throws IOException if there is an error while reading from the specified input stream.
   */
  public static DsSipMessage createMessage(DsSipFrameStream in, boolean createKey, boolean validate)
      throws DsSipParserListenerException, DsSipParserException, IOException {
    return DsSipDefaultMessageFactory.getInstance().createMessage(in, createKey, validate);
  }

  /** Default constructor. */
  protected DsSipMessage() {
    super();
  }

  protected DsSipMessage(boolean encoded) {
    if (Log.isDebugEnabled()) Log.debug("In encoded SIP message constructor");
    if (encoded == true) {
      m_isEncoded = true;
      headerType = new DsSipHeaderInterface[DsSipConstants.MAX_KNOWN_HEADER + 1];
    } else {
      headerType = new DsSipHeaderInterface[HEADER_PRIORITY_LEVEL + 1];
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // member functions
  ////////////////////////////////////////////////////////////////////////////////
  /**
   * Method used to retrieve the Authorization header or the Authentication header based on the
   * message whether being a request or a response.
   *
   * @return DsSipHeader the header retrieved
   */
  public abstract DsSipHeader getAuthenticationHeader();

  public abstract DsTokenSipDictionary shouldEncode();

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public abstract int getMethodID();
      public abstract void writeStartLine(OutputStream out) throws IOException;
  */
  /**
   * Serializes the binary encoded start line of this message to the specified <code>out</code>
   * output stream. This method must be implemented by the subclass.
   *
   * @param out the output stream where the start line needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public abstract void writeEncodedStartLine(OutputStream out) throws IOException;

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public abstract boolean equalsStartLine(DsSipMessage message);
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public final void addHeader(DsSipHeaderInterface header)
      public final void addHeader(DsSipHeaderInterface header, boolean start)
      public final void addHeader(DsSipHeaderInterface header, boolean start, boolean clone)
      public final void addHeaders(DsSipHeaderList headers)
      public final void addHeaders(DsSipHeaderList headers, boolean start)
      public final void addHeaders(DsSipHeaderList headers, boolean start, boolean clone)
      public final DsSipHeaderInterface removeHeader(DsByteString header)
      public final DsSipHeaderInterface removeHeader(DsByteString header, boolean start)
      public final DsSipHeaderInterface removeHeader(int id)
      public DsSipHeaderInterface removeHeader(int id, boolean start)
      public final DsSipHeaderInterface removeHeader(DsSipHeaderInterface header)
      public final DsSipHeaderInterface removeHeaders(int id)
      public final DsSipHeaderInterface removeHeaders(DsByteString name)
      public final DsSipHeaderInterface updateHeader(DsSipHeaderInterface header)
      public final DsSipHeaderInterface updateHeader(DsSipHeaderInterface header, boolean clone)
      public final DsSipHeaderInterface updateHeaders(DsSipHeaderList headers)
      public final DsSipHeaderInterface updateHeaders(DsSipHeaderList headers, boolean clone)
      public final boolean hasHeaders(DsByteString name)
      public final boolean hasHeaders(int id)
      public final DsSipHeaderInterface getHeader(int id)
      public final DsSipHeaderInterface getHeader(int id, boolean start)
      public final DsSipHeaderInterface getHeader(DsByteString name)
      public final DsSipHeaderInterface getHeader(DsByteString name, boolean start)
      public final DsSipHeader getHeaderValidate(int id)
      public final DsSipHeader getHeaderValidate(int id, boolean start)
      public final DsSipHeader getHeaderValidate(DsByteString name)
      public final DsSipHeader getHeaderValidate(DsByteString name, boolean start)
      public DsSipHeader getHeaderValidate(int id, DsByteString name, boolean start)
      public final DsSipHeaderList getHeaders(int id)
      public DsSipHeaderList getHeaders(DsByteString name)
      public DsSipHeaderList getHeadersValidate(int id, int num, boolean start)
      public DsSipHeaderList getHeadersValidate(DsByteString name, int num, boolean start)
      public DsSipHeaderList getHeadersValidate(int id)
      public DsSipHeaderList getHeadersValidate(DsByteString name)
      public DsSipHeaderList getHeaders()
      public boolean hasHeaders()
      public Map getHeadersMap()
  */

  //////////////////////////////////////////////////////////////////////////////////
  //  The following set of methods are convenience functions that can be used
  //  in place of the more general getHeader() and getHeaders() method.
  //////////////////////////////////////////////////////////////////////////////////

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public DsSipHeaderInterface getContactHeader()
      public DsSipContactHeader getContactHeaderValidate()
      public DsSipHeaderList getContactHeaders()
      public DsSipHeaderInterface getContentTypeHeader()
      public DsSipContentTypeHeader getContentTypeHeaderValidate()
      public DsSipHeaderInterface getFromHeader()
      public DsSipFromHeader getFromHeaderValidate()
      public DsSipHeaderInterface getToHeader()
      public DsSipToHeader getToHeaderValidate()
      public DsSipHeaderInterface getViaHeader()
      public DsSipViaHeader getViaHeaderValidate()
      public DsSipHeaderList getViaHeaders()
  */

  /**
   * Method to get the unique method ID for the method in the CSeq header.
   *
   * @return the method ID
   */
  public final int getCSeqType() {
    return (null != m_strCSeq) ? DsSipMsgParser.getMethod(m_strCSeq) : -1;
  }

  /**
   * Returns the method name in the CSeq header in this message.
   *
   * @return the method name in the CSeq header in this message.
   */
  public final DsByteString getCSeqMethod() {
    return m_strCSeq;
  }

  /**
   * Returns the CSeq number in this message.
   *
   * @return the CSeq number in this message.
   */
  public final long getCSeqNumber() {
    return m_lCSeq;
  }

  /**
   * Sets the method name in the CSeq header in this message.
   *
   * @param cseq the new method name in the CSeq header in this message.
   */
  public final void setCSeqMethod(DsByteString cseq) {
    m_strCSeq = cseq;
  }

  /**
   * Sets the CSeq number in this message.
   *
   * @param cseq new CSeq number in this message.
   */
  public final void setCSeqNumber(long cseq) {
    if (cseq >= 0) {
      m_lCSeq = cseq;
    }
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public final int getContentLength()
      public DsByteString getBody()
      public void setBody(DsByteString body, DsByteString type)
      public void setBody (byte[] body, DsByteString type )
      public DsByteString getBodyType()
      public int getBodyLength()
      public boolean hasBody()
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public int getVersionHigh()
      public int getVersionLow()
      public void setVersion(int high, int low)
  */

  /**
   * For an outgoing message, retrieves the destination address. For an incoming message retrieves
   * the source address.
   *
   * @return the InetAddress
   */
  public final InetAddress getConnectionAddress() {
    return m_bindingInfo.getRemoteAddress();
  }

  /**
   * Set the destination address of an outgoing message.
   *
   * @param address the destination address.
   */
  public final void setConnectionAddress(InetAddress address) {
    m_bindingInfo.setRemoteAddress(address);
  }

  /**
   * Set the destination address of an outgoing message.
   *
   * @param address the destination address.
   * @throws UnknownHostException if the specified <code>address</code> could not be resolved to a
   *     valid host name.
   */
  public final void setConnectionAddress(String address) throws UnknownHostException {
    // don't resove this here - it is need as a String for
    //       SRV rules to work correctly -dg
    // m_bindingInfo.setRemoteAddress(InetAddress.getByName(address));
    m_bindingInfo.setRemoteAddress(address);
  }

  /**
   * For an outgoing message, retrieves the destination port. For an incoming message retrieves the
   * source port.
   *
   * @return int the port number
   */
  public final int getConnectionPort() {
    return m_bindingInfo.getRemotePort();
  }

  /**
   * Set the destination port of the message.
   *
   * @param port int the port number
   */
  public final void setConnectionPort(int port) {
    m_bindingInfo.setRemotePort(port);
  }

  /**
   * Set local InetAddress for a connection.
   *
   * @param laddr Local address to bind
   */
  public final void setLocalBindingAddress(InetAddress laddr) {
    m_bindingInfo.setLocalAddress(laddr);
  }

  /**
   * Set local InetAddress for a connection.
   *
   * @param address Local address to bind.
   * @throws UnknownHostException if the specified <code>address</code> could not be resolved to a
   *     valid host name.
   */
  public final void setLocalBindingAddress(String address) throws UnknownHostException {
    m_bindingInfo.setLocalAddress(InetAddress.getByName(address));
  }

  /**
   * Get local InetAddress for a connection.
   *
   * @return Local address to bind
   */
  public final InetAddress getLocalBindingAddress() {
    return m_bindingInfo.getLocalAddress();
  }

  /**
   * Set local port for a connection.
   *
   * @param lport Local port to bind to
   */
  public final void setLocalBindingPort(int lport) {
    m_bindingInfo.setLocalPort(lport);
  }

  /**
   * Get local port for a connection.
   *
   * @return Local port to bind
   */
  public final int getLocalBindingPort() {
    return m_bindingInfo.getLocalPort();
  }

  /**
   * Method used to retrieve the transport type.
   *
   * @return int the type of protocol used
   */
  public final int getConnectionTransport() {
    return m_bindingInfo.getTransport();
  }

  /**
   * Method used to set the transport type.
   *
   * @param aTransportType the type of protocol used
   */
  public final void setConnectionTransport(int aTransportType) {
    m_bindingInfo.setTransport(aTransportType);
  }

  /**
   * Returns true if compression is forced.
   *
   * @return true if compression is forced
   */
  public final boolean compress() {
    return m_bindingInfo.compress();
  }

  /**
   * Force compression on an outgoing message.
   *
   * @param compress used to indicate that a message should be compressed.
   */
  public final void compress(boolean compress) {
    m_bindingInfo.compress(compress);
  }

  /**
   * Used by the transport layer to determine whether or not a message should be compressed.
   *
   * @return false if the message has already been compressed (based on it's serialized form). After
   *     the serialized form has been checked, this method will check the compress flag on the
   *     message. If that flag is set, this method will return true; If that flag is not set,
   *     comp=sigcomp is checked on the Via header for responses and the rURI for requests. This
   *     method returns true if that parameter is set.
   */
  public abstract boolean shouldCompress();

  /**
   * Sets the serialized form of the message to this compressed data. Note that if the native
   * sigcomp library is configured to not copy the compressed data back to the Java side, then this
   * will always return false.
   *
   * @param compressedData set the serialized form of the message to this compressed data. For stack
   *     use only.
   */
  public final void setCompressedData(byte[] compressedData) {
    if (m_strValue == null) {
      m_strValue = new DsByteString(compressedData);
    } else {
      m_strValue.setData(compressedData);
    }
  }

  /**
   * Returns true if based on the serialized form of the message, the message has been sigcomp
   * compressed. Use for outgoing messages. This method will return false unless the message has
   * been finalised (using {@link #setFinalised(boolean)}).
   *
   * @return true if based on the serialized form of the message, the message has been sigcomp
   *     compressed. Use for outgoing messages. This method will return false unless the message has
   *     been finalised (using {@link #setFinalised(boolean)}).
   */
  public final boolean isCompressed() {
    boolean ret = false;
    if (m_bFinalized) {
      ret = isCompressed(toByteArray());
    }
    return ret;
  }

  /**
   * Returns true if the specified byte array <code>data</code> is sigcomp compressed.
   *
   * @return true if this byte[] is sigcomp compressed.
   * @param data the data to check
   */
  public static final boolean isCompressed(byte[] data) {
    boolean ret = false;
    if (data != null && data.length > 0) {
      if ((data[0] & 0xf8) == 0xf8) {
        ret = true;
      }
    }
    return ret;
  }

  /**
   * Sets the network type for this SIP message binding info to the specified <code>network</code>
   * type.
   *
   * @param network the new network type for this message binding info.
   */
  public final void setNetwork(int network) {
    m_bindingInfo.setNetwork(network);
  }

  /**
   * Sets the network type for this SIP message binding info to the network type of the specified
   * <code>network</code>.
   *
   * @param network the new network type for this message binding info.
   */
  public final void setNetwork(DsNetwork network) {
    m_bindingInfo.setNetwork(network);
  }

  /**
   * Returns the network property associated with the binding info of this SIP message.
   *
   * @return the network property associated with the binding info of this SIP message.
   */
  public final DsNetwork getNetwork() {
    return m_bindingInfo.getNetwork();
  }

  /**
   * Returns the network associated with this message's binding info or the default system network
   * if there is no network associated with it.
   *
   * @return the network associated with this message's binding info or the default system network
   *     if there is no network associated with it.
   */
  public final DsNetwork getNetworkReliably() {
    return m_bindingInfo.getNetworkReliably();
  }

  /**
   * Set the binding information for this message. Important Note: If the passed in paramete
   * binding_info is null, this api will NOT set this object's binding_info. The original binding
   * info will remain. Note that
   *
   * @param binding_info the binding information for this message
   */
  public final void setBindingInfo(DsBindingInfo binding_info) {
    if (binding_info != null) m_bindingInfo = binding_info;
  }

  /**
   * Get the binding information for this message.
   *
   * @return the binding information for this message
   */
  public final DsBindingInfo getBindingInfo() {
    return m_bindingInfo;
  }

  /**
   * Updates the binding information for this message.
   *
   * @param binding_info the updated binding information for this message
   */
  public final void updateBinding(DsBindingInfo binding_info) {
    if (binding_info == null) return;

    if (null != m_bindingInfo) {
      m_bindingInfo.update(binding_info);
    } else {
      m_bindingInfo = (DsBindingInfo) binding_info.clone();
    }
  }

  /** Set this message's key bytes. Called just after the message is parsed. */
  void setKey(DsSipTransactionKey key) {
    m_key = key;
  }

  /**
   * Returns the key for this message.
   *
   * @return the key for this message.
   */
  public final DsSipTransactionKey getKey() {
    return m_key;
  }

  /**
   * Sets the specified <code>context</code> to the transaction key of this SIP message. A context
   * defines what all components of the SIP message should be considered while composing the
   * transaction key.
   *
   * @param context the context that needs to be set for this key.
   * @see DsSipTransactionKey#setKeyContext(int).
   */
  public final void setKeyContext(int context) {
    if (m_key != null) {
      m_key.setKeyContext(context);
    } else {
      // for incoming messages, there should ALWAYS be key
      //  bytes
    }
  }

  /**
   * Checks if the message is a request.
   *
   * @return boolean <b>True</b> if a request <b>False</b> otherwise
   */
  public boolean isRequest() {
    return false;
  }

  /**
   * Checks if the message is a response.
   *
   * @return boolean <b>True</b> if a response <b>False</b> otherwise
   */
  public boolean isResponse() {
    return (!isRequest());
  }

  public void writeEncodedBody(OutputStream out) throws IOException {
    try {
      DsSipContentTypeHeader contentType = this.getContentTypeHeaderValidate();

      if (contentType != null) {
        if (contentType.hasParameters()) {
          contentType.writeEncoded(out, m_messageDictionary);
        }

        if (contentType.getSubType().compareTo(DsSipConstants.BS_SDP) != 0) {
          out.write(DsTokenSDPMsgParser.TOKEN_SIP_CONTENT_GENERIC);
          if (contentType.hasParameters() == false) {
            m_messageDictionary.getEncoding(contentType.getMediaType()).write(out);
          }

          int contentIndex = 0;
          int bodyLength = getBodyLength();

          while (contentIndex < bodyLength) {
            out.write(DsTokenSipConstants.TOKEN_SIP_MESSAGE_BODY_CONTENT);

            int bytesRemaining = bodyLength - contentIndex;
            int bytesToWrite =
                (bytesRemaining > DsTokenSipConstants.GENERIC_CONTENT_MAX_SIZE)
                    ? DsTokenSipConstants.GENERIC_CONTENT_MAX_SIZE
                    : bytesRemaining;

            if (Log.isDebugEnabled())
              Log.debug("Writing raw message body, number of bytes " + bytesToWrite);
            out.write(bytesToWrite);
            out.write(getBody().toByteArray(contentIndex, bytesToWrite));
            contentIndex += bytesToWrite;
          }
        } else {
          out.write(DsTokenSDPMsgParser.TOKEN_SIP_CONTENT_SDP_DEPRECATED);
          // REFACTOR
          //          try {
          //            DsSdpMsg messageBody = new DsSdpMsg(getBody().toByteArray());
          //
          //            DsTokenSipSDPEncoder sdpEncoder = new DsTokenSipSDPEncoder(messageBody);
          //            sdpEncoder.write(out, m_messageDictionary);
          //
          //          } catch (DsSdpMsgException e) {
          //            Log.error("Exception while writing SDP Content", e);
          //            throw new IOException(e.getMessage());
          //          }
        }
      }
    } catch (DsSipParserException e) {
    } catch (DsSipParserListenerException e) {
    } catch (IOException e) {
    }
  }

  /**
   * Writes the message to the output stream, and then calls flush().
   *
   * @param out the output stream
   * @throws IOException if there is an exception in writing to the stream
   */
  public void write(OutputStream out) throws IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.MSG_WRITE);
    if (m_bFinalized) {
      m_strValue.write(out);
    } else {
      ensureMaxForwards();

      writeStartLine(out);
      writeHeadersAndBody(out);
    }
    out.flush();
    if (DsPerf.ON) DsPerf.stop(DsPerf.MSG_WRITE);
  }

  /**
   * Writes the message to the output stream, and then calls flush().
   *
   * @param out the output stream
   * @throws IOException if there is an exception in writing to the stream
   */
  public void writeEncoded(DsTokenSipDictionary dictionary, OutputStream out) throws IOException {
    if (DsPerf.ON) DsPerf.start(DsPerf.MSG_WRITE);
    if (m_bFinalized) {
      m_strValue.write(out);
    } else {
      ensureMaxForwards();
      m_messageDictionary = new DsTokenSipMessageDictionary(dictionary);
      writeEncodedStartLine(out);
      writeEncodedHeaders(out);

      if (getBodyLength() != 0) {
        if (Log.isDebugEnabled())
          Log.debug(
              "found a message body.  It's length is "
                  + getBody().length()
                  + " and the value is <"
                  + getBody()
                  + ">");
        writeEncodedBody(out);
      }
    }
    if (DsPerf.ON) DsPerf.stop(DsPerf.MSG_WRITE);
  }

  /**
   * Create a deep clone of this object.
   *
   * @return a deep clone of this object
   */
  public Object clone() {
    if (DsPerf.ON) DsPerf.start(DsPerf.MSG_CLONE);
    DsSipMessage clone = null;
    try {
      clone = (DsSipMessage) super.clone();
    } catch (CloneNotSupportedException cne) {
      // We know the clone is supported :)
    }

    clone.m_bFinalized = false;
    clone.m_strValue = null;

    //  transaction key is null
    clone.m_key = null;

    // clone the headers, one set at a time
    clone.headerType = new DsSipHeaderInterface[headerType.length];

    int len = headerType.length - 1;
    DsSipHeaderInterface headers = null;
    // iterate through the known headers and copy them
    for (int i = 0; i < len; i++) {
      headers = headerType[i];
      if (headers != null) {
        clone.headerType[i] = (DsSipHeaderInterface) headers.clone();
      }
    }
    DsSipHeaderList list = null;
    Object cloneList = null;
    DsSipHeaderList subList = null;
    list = (DsSipHeaderList) headerType[len];
    if (null != list) {
      subList = new DsSipHeaderList();
      headers = (DsSipHeaderInterface) list.getFirst();
      while (null != headers) {
        cloneList = headers.clone();
        if (cloneList != null) {
          subList.addLast(cloneList);
        }
        headers = (DsSipHeaderInterface) headers.getNext();
      } // _while
      if (!subList.isEmpty()) {
        clone.headerType[len] = subList;
      }
    } // _if
    if (DsPerf.ON) DsPerf.stop(DsPerf.MSG_CLONE);
    return clone;
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      private boolean equalsHeaders(DsSipMessage message)
      private void validateHeaders()
  */

  /**
   * Checks for the semantic equality of this message against the specified <code>comparator</code>
   * message object.
   *
   * @param comparator the message whose semantics needs to be compared for equality against the
   *     semantics of this message object.
   * @return <code>true</code> if this message is semantically equal to the the specified <code>
   *     comparator</code> message object, <code>false</code> otherwise.
   */
  public boolean equals(Object comparator) {
    try {
      return equals((DsSipMessage) comparator);
    } catch (ClassCastException cce) {
    }
    return false;
  }

  /**
   * Checks for the semantic equality of this message against the specified <code>message</code>
   * object.
   *
   * @param message the message whose semantics needs to be compared for equality against the
   *     semantics of this message object.
   * @return <code>true</code> if this message is semantically equal to the the specified <code>
   *     message</code>, <code>false</code> otherwise.
   */
  public boolean equals(DsSipMessage message) {
    return super.equals(message);
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public byte[] toByteArray()
      public DsByteString toByteString()
  */

  /**
   * Returns the whole message as a DsByteString. A new DsByteString object is created every time
   * unless this message is finalized. If finalised, then we should have the whole message as
   * DsByteString already. In that case, that DsByteString object will be returned. To finalise the
   * message, call {@link #setFinalised(boolean)} method.
   *
   * @return this message as a byte string representation.
   */
  public DsByteString toEncodedByteString(DsTokenSipDictionary dictionary) {
    if (Log.isDebugEnabled()) Log.debug("Entering toEncodedByteString() NOW");
    // If we have finalized all the individual items (headers and body)
    // Then we should have the whole message in m_strValue object
    if (m_bFinalized) {
      return m_strValue;
    }
    ByteBuffer buffer = ByteBuffer.newInstance(1024);
    ByteBuffer dictionaryBuffer = null;
    byte[] messageBytes;
    try {
      debugIndex = 0;
      dictionaryBuffer = ByteBuffer.newInstance(1024);
      writeEncoded(dictionary, buffer);
      // m_messageDictionary.dump(System.out);
      // m_messageDictionary.dumpBytes(System.out);

      messageBytes = buffer.toByteArray();

      m_messageDictionary.write(dictionaryBuffer);
      dictionaryBuffer.write(messageBytes);
    } catch (IOException e) {
      // We may never get this exception as we are writing to the
      // byte buffer. Even if we enter here, it may be possible that
      // some of the bytes are already written to this buffer, so
      // just return those bytes only.
    }

    return dictionaryBuffer.getByteString();
  }

  /**
   * Returns a String representation of this message.
   *
   * @return a String representation of this message.
   */
  public String toString() {
    if (isEncoded() && isFinalised()) {
      return m_strValue.toString();
    }

    DsByteString bs = toByteString();

    if (isCompressed(bs.data())) {
      return DsString.toSnifferDisplay(bs.data());
    } else {
      return bs.toString();
    }
  }

  /**
   * Returns a String representation of this message.
   *
   * @return a String representation of this message.
   */
  public String toCryptoInfoMaskedString() {
    if (isEncoded() && isFinalised()) {
      return m_strValue.toString();
    }

    DsByteString bs = toByteString();

    if (isCompressed(bs.data())) {
      return DsString.toSnifferDisplay(bs.data());
    } else {
      return toCryptoInfoMaskedString(bs.toString());
    }
  }

  /**
   * Returns a String representation of this message after masking any crypto info if present.
   *
   * @return a String representation of this message.
   */
  public static String toCryptoInfoMaskedString(String msg) {
    return msg.replaceAll(REGEX_CRYPT_LINE, REGEX_CRYPT_MASK);
  }

  public static String maskAndWrapSIPMessageToSingleLineOutput(String msg) {
    msg = toCryptoInfoMaskedString(msg);
    msg = removeSDP(msg);
    return formWrappedMessage(msg);
  }

  public String maskAndWrapSIPMessageToSingleLineOutput() {
    String msg = toCryptoInfoMaskedString();
    msg = removeSDP(msg);
    return formWrappedMessage(msg);
  }

  private static String formWrappedMessage(String msg) {
    msg = msg.replaceAll(NEW_LINE_REGEX_PATTERN, NEW_LINE_REPLACEMENT_DELIMITER);
    msg = BEGIN_SIP_MESSAGE + msg + END_SIP_MESSAGE;
    return msg;
  }

  private static String removeSDP(String msg) {
    return msg.split(SDP_SPLIT_STRING)[0];
  }
  /**
   * Constructs and returns the transaction key for this message. The constructed key can be RFC
   * 3261 style transaction key or RFC 2543 style (for backwards compatibility) transaction key
   * based on the key policy defined for the stack. The key policy can be set by using {@link
   * #setNewKeyPolicy(boolean)} method.
   *
   * <p>If the policy is set to construct RFC 3261 style transaction key and there is an exception
   * while constructing the key, then RFC 2543 style key is constructed. After construction, the key
   * is set to this message and the same reference is returned.
   *
   * @return the newly constructed transaction key for this message.
   */
  public final DsSipTransactionKey createKey() {
    // we probably want 2 key timers here
    if (DsPerf.ON) DsPerf.start(DsPerf.TRANS_KEY);

    boolean created = false;
    DsSipViaHeader via = null;

    if (NEW_KEY) {
      try {
        if (Log.isDebugEnabled()) Log.debug("Creating new transaction key");

        via = getViaHeaderValidate();
        DsByteString bs = via.getBranch();
        if (null != bs && BS_MAGIC_COOKIE.equals(bs.data(), bs.offset(), MAGIC_COOKIE_COUNT)) {
          DsSipDefaultTransactionKey key = new DsSipDefaultTransactionKey();
          key.setViaBranch(bs.toByteArray());
          key.setCSeqMethod(m_strCSeq);
          m_key = key;
          created = true;
          if (DsPerf.ON) DsPerf.stop(DsPerf.TRANS_KEY);
        }
      } catch (Exception exc) {
        if (DsLog4j.messageCat.isEnabled(Level.ERROR)) {
          DsLog4j.messageCat.error("Exception creating branch id based key", exc);
        }
      }
    }

    if (!created) {
      // warn if we tried to create a branch id based key and couldn't
      if (DsLog4j.messageCat.isEnabled(Level.WARN) && NEW_KEY) {
        DsLog4j.messageCat.warn(
            "Couldn't create Message Key from Via header: " + via + " Trying Classic Key.");
      }

      DsByteString strValue = toByteString(); // potentially (see toByteString) serialize
      try {
        DsSipClassicTransactionKey classic = new DsSipClassicTransactionKey();
        DsSipMsgParser.parse(classic, strValue.data(), strValue.offset(), strValue.length());
        m_key = classic;
        m_key.setKeyContext(DsSipTransactionKey.USE_VIA);
      } catch (DsSipMessageValidationException mve) {
        // if we have a parser exception we have real
        //   trouble here
        if (DsLog4j.messageCat.isEnabled(Level.ERROR)) {
          DsLog4j.messageCat.error(
              "Message Validation Exception while "
                  + " creating Classic Key. Message-["
                  + strValue.toString()
                  + "]\n",
              mve);
        }
      } catch (DsSipKeyValidationException kve) {
        if (DsLog4j.messageCat.isEnabled(Level.ERROR)) {
          DsLog4j.messageCat.error(
              "Key Validation Exception while "
                  + " creating Classic Key. Message-["
                  + strValue.toString()
                  + "]\n",
              kve);
        }
      } catch (Exception exc) {
        // if we have a parser exception we have real
        //   trouble here
        if (DsLog4j.messageCat.isEnabled(Level.ERROR)) {
          DsLog4j.messageCat.error(
              "Exception while creating Classic Key. Message-[" + strValue.toString() + "]\n", exc);
        }
      }
      if (DsPerf.ON) DsPerf.stop(DsPerf.TRANS_KEY);
    }

    return m_key;
  }

  /**
   * Sets the transport value for the Via header. It invokes the {@link #getViaHeaderValidate()}
   * method to retrieve the top Via header and set its transport value.
   *
   * @param transport the new transport value for the top Via header present in this message.
   * @throws DsSipParserException {@link #getViaHeaderValidate()}
   * @throws DsSipParserListenerException {@link #getViaHeaderValidate()}
   */
  public void setViaTransport(int transport)
      throws DsSipParserException, DsSipParserListenerException {
    DsSipViaHeader via = getViaHeaderValidate();
    if (via != null) via.setTransport(transport);
  }

  /**
   * Checks whether the size of this message exceeds the size of the MTU.
   *
   * @return <code>true</code> if the size of this message is more than the MTU size, <code>false
   *     </code> otherwise.
   */
  public boolean sizeExceedsMTU() {
    int mtu = getNetworkReliably().getMTU();

    if (mtu == Integer.MAX_VALUE) return false;

    DsByteString strValue;

    if (isEncoded()) {
      strValue = toEncodedByteString(shouldEncode());
    } else {
      strValue = toByteString();
    }

    if ((mtu - strValue.length()) < 200) {
      return true;
    }

    return false;
  }

  /**
   * Given a DsSipHeaderInterface get the URI. If it's not parsed, don't parse it: construct a URI
   * from the string value. If it is parsed, get the URI out.
   *
   * @param header - the header from which to build the URI. It must be a single header.
   * @param clone If the specified <code>header</code> is of DsSipHeaderInterface.HEADER type, then
   *     if this parameter is <code>true</code> then clone of the URI in the header will be
   *     returned.
   * @return the URI that is available in the specified in the passed in header.
   * @throws DsSipParserException If the specified <code>header</code> is of
   *     DsSipHeaderInterface.STRING type, then this method try to parse the string value to extract
   *     the URI. If there is an error while parsing the URI from the string header, this exception
   *     is thrown.
   * @throws DsSipParserListenerException If the specified <code>header</code> is of
   *     DsSipHeaderInterface.STRING type, then this method try to parse the string value to extract
   *     the URI. if there is an error condition detected by the URI parser listener, this exception
   *     is thrown.
   */
  protected final DsURI createURI(DsSipHeaderInterface header, boolean clone)
      throws DsSipParserListenerException, DsSipParserException {
    if (header == null) return null;
    DsURI ret = null;
    switch (header.getForm()) {
      case DsSipHeaderInterface.STRING:
        DsSipReadOnlyElement uri =
            DsSipReadOnlyElement.createElement(header.getHeaderID(), URI, header.getValue());
        ret = DsURI.constructFrom(uri.getElement(DsSipURIElements.VALUE));
        break;
      case DsSipHeaderInterface.HEADER:
        ret = ((DsSipNameAddressHeader) header).getNameAddress().getURI();
        ret = clone ? (DsURI) ret.clone() : ret;
        break;
      case DsSipHeaderInterface.LIST:
        break;
    }
    return ret;
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public boolean isFinalised()
      public void setFinalised(boolean finalised)
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public abstract DsSipElementListener requestURIBegin(byte[] buffer, int offset, int count)
      public abstract void requestURIFound(byte[] buffer, int offset, int count, boolean valid)
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public DsSipHeaderListener getHeaderListener()
      public void messageFound(byte[] buffer, int offset, int count, boolean messageValid)
  */

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

    // As we decided to parse these header in this message itself
    switch (headerId) {
      case CSEQ:
      case CALL_ID:
        return this;
      case CONTENT_LENGTH:
        if (!m_jainCompatability) {
          return this;
        }
    }
    return createElementListener(headerId);
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

    // As we decided to parse these header in this message itself
    switch (headerId) {
      case CSEQ:
      case CALL_ID:
        return;
      case CONTENT_LENGTH:
        if (!m_jainCompatability) {
          return;
        }
    }

    if (header != null) {
      addHeader(header, false, false);
      header = null;
    } else {
      addHeader(
          new DsSipHeaderString(
              headerId, DsSipMsgParser.getHeader(headerId), buffer, offset, count),
          false,
          false);
    }
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public void unknownFound(byte[] buffer, int nameOffset, int nameCount,
      public DsSipElementListener elementBegin(int contextId, int elementId)
  */

  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    super.elementFound(contextId, elementId, buffer, offset, count, valid);
    switch (contextId) {
      case CSEQ:
        switch (elementId) {
          case CSEQ_METHOD:
            m_strCSeq = DsByteString.newInstance(buffer, offset, count);
            break;
          case CSEQ_NUMBER:
          case SINGLE_VALUE:
            m_lCSeq = DsSipMsgParser.parseLong(buffer, offset, count);
            break;
        } // _switch
        break;
      case CALL_ID:
        switch (elementId) {
          case SINGLE_VALUE:
            m_strCallId = new DsByteString(buffer, offset, count);
            break;
        } // _switch
        break;
    } // _switch
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public void parameterFound(int contextId, byte[] buffer, int nameOffset,
  */

  ////////////////////////////////////////////////////////////////////////////////
  // private member functions
  ////////////////////////////////////////////////////////////////////////////////

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      private boolean initHeader(int headerId)
  */

  private boolean initHeaderForEncoding(int headerId) {
    header = DsSipHeader.newInstance(headerId);
    return true;
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      private void writeHeaders(OutputStream out) throws IOException
  */

  /**
   * Serializes the headers in this message to the specified <code>out</code> output stream.
   *
   * @param out the output stream where the headers need to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  private void writeEncodedHeaders(OutputStream out) throws IOException {
    int len = headerType.length - 1;
    // int len = DsSipConstants.MAX_KNOWN_HEADER;
    // TJR -  DsSipHeaderInterface l = null;

    DsSipHeaderInterface l = null;

    for (int type = 0; type < len; type++) {
      switch (type) {
        case CSEQ:
        case CALL_ID:
        case CONTENT_LENGTH:
          break;
        default:
          if (DsTokenSipHeaderDictionary.isBlockedHeader(type) == false) {
            writeEncodedHeaderType(type, out);
          } else {
            if (Log.isDebugEnabled()) Log.debug("Header " + type + " is blocked");
          }
          break;
      }
    } // _for
    // Write in-built headers

    writeEncodedCallId(out);
    writeEncodedCSeq(out);
    writeEncodedContentLength(out);

    if (Log.isDebugEnabled()) Log.debug("At the last header block");

    DsSipHeaderList list = (DsSipHeaderList) headerType[len];
    if (null != list) {
      l = (DsSipHeaderInterface) list.getFirst();

      if (Log.isDebugEnabled())
        Log.debug(list.size() + " number of headers present in the last header block");

      while (l != null) {
        writeEncodedHeaderType(l, out);
        l = (DsSipHeaderInterface) l.getNext();
      }
    }
    // headers are finished - add the empty header to separate body
  }

  private void writeEncodedHeaderType(int type, OutputStream out) throws IOException {
    try {
      if (isSingular(type)) {
        DsSipHeader header = getHeaderValidate(type);

        if (null != header) {
          if (DsTokenSipHeaderDictionary.isBlockedHeader(header.getHeaderID()) == false) {
            writeEncodedSingleHeader(header, out);
          }
        } // _if
      } else // list of headers
      {
        DsSipHeaderList list = getHeadersValidate(type);
        if (list == null) {
          return;
        }

        DsSipHeader header;
        Iterator i = list.iterator();

        while (i.hasNext()) {
          header = (DsSipHeader) i.next();
          if (DsTokenSipHeaderDictionary.isBlockedHeader(header.getHeaderID()) == false) {
            writeEncodedSingleHeader(header, out);
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void writeEncodedHeaderType(DsSipHeaderInterface l, OutputStream out) {
    try {
      switch (l.getForm()) {
        case DsSipHeaderInterface.HEADER:
          if (DsTokenSipHeaderDictionary.isBlockedHeader(((DsSipHeader) l).getHeaderID())
              == false) {
            writeEncodedSingleHeader((DsSipHeader) l, out);
          }
          break;
        case DsSipHeaderInterface.STRING:
          DsSipHeaderString str = (DsSipHeaderString) l;
          DsSipHeader myHeader = DsSipHeader.newInstance(l.getHeaderID(), l.getToken());
          myHeader.parse(str.data(), str.offset(), str.length());
          if (DsTokenSipHeaderDictionary.isBlockedHeader(myHeader.getHeaderID()) == false) {
            writeEncodedSingleHeader(myHeader, out);
            ((DsSipHeaderList) headerType[headerType.length - 1]).replace(l, myHeader);
          }
          break;
        case DsSipHeaderInterface.LIST:
          ((DsSipHeaderList) l).validate();

          DsSipHeaderList headerList = (DsSipHeaderList) l;
          DsSipHeader listHeader = (DsSipHeader) headerList.getFirst();

          while (listHeader != null) {
            if (DsTokenSipHeaderDictionary.isBlockedHeader(listHeader.getHeaderID()) == false) {
              writeEncodedSingleHeader(listHeader, out);
            }
            listHeader = (DsSipHeader) listHeader.getNext();
          }
          break;
        default:
          throw new DsSipParserException(
              "Invalid header interface type returned in writeEncodedHeaderType");
      }
    } catch (Exception e) {
      Log.error("Error writing non-preferred headers", e);
    }
  }

  private final void writeEncodedSingleHeader(DsSipHeader header, OutputStream out)
      throws IOException {
    if (header.getHeaderID() != UNKNOWN_HEADER) {
      header.writeEncoded(out, m_messageDictionary);
    } else {
      out.write(DsTokenSipConstants.TOKEN_SIP_UNKNOWN_HEADER);
      m_messageDictionary.getEncoding(header.getToken()).write(out);
      m_messageDictionary.getEncoding(header.getValue()).write(out);
    }
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public DsByteString getToTag()
      public DsByteString getFromTag()
  */

  /**
   * It queries the transaction key for the Via Branch value.
   *
   * @return value from transaction key or null if no key exists
   */
  public DsByteString getViaBranch() {
    return m_key == null ? null : m_key.getViaBranch();
  }

  /**
   * Returns the value of CallID header, if present, in this message.
   *
   * @return the value of CallID header, if present, in this message, otherwise returns null.
   */
  public DsByteString getCallId() {
    return m_strCallId;
  }

  /**
   * Sets the CallID header value to the specified <code>callid</code> value.
   *
   * @param callid The new value for the CallID header in this message.
   */
  public void setCallId(DsByteString callid) {
    m_strCallId = callid;
  }

  /**
   * Serializes the CallID header to the specified <code>out</code> output stream.
   *
   * @param out the output stream where the CallID header needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void writeCallId(OutputStream out) throws IOException {
    if (null != m_strCallId) {
      if (DsSipHeader.isCompact()) {
        BS_CALL_ID_C_TOKEN.write(out);
      } else {
        BS_CALL_ID_TOKEN.write(out);
      }
      m_strCallId.write(out);
      BS_EOH.write(out);
    }
  }

  public void writeEncodedCallId(OutputStream out) throws IOException {
    int seperator = this.m_strCallId.indexOf('@');

    if (seperator > 0) {
      // pick client or server style

      try {
        if (seperator == 8) {
          byte[] clientStyle =
              DsHexEncoding.fromHex(m_strCallId.substring(0, seperator).toString());
          out.write(DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_CALLID1_HEADER);
          out.write(clientStyle);
          m_messageDictionary.getEncoding((this.m_strCallId.substring(seperator + 1))).write(out);
          return;
        }
      } catch (DsException e) {
        // wasn't a valid hex string
      }

      out.write(DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_CALLID2_HEADER);
      m_messageDictionary.getEncoding((this.m_strCallId.substring(0, seperator))).write(out);
      m_messageDictionary.getEncoding((this.m_strCallId.substring(seperator + 1))).write(out);
    } else {
      out.write(DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_CALLID2_HEADER);
      m_messageDictionary.getEncoding(this.m_strCallId).write(out);
    }
  }

  /**
   * Serializes the CSeq header to the specified <code>out</code> output stream.
   *
   * @param out the output stream where the CSeq header needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public void writeCSeq(OutputStream out) throws IOException {

    BS_CSEQ_TOKEN.write(out);
    out.write(DsIntStrCache.intToBytes(m_lCSeq));
    if (m_strCSeq != null) {
      out.write(B_SPACE);
      m_strCSeq.write(out);
    }
    BS_EOH.write(out);
  }

  public void writeEncodedCSeq(OutputStream out) throws IOException {
    out.write(DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_CSEQ_HEADER);
    DsTokenSipInteger.write32Bit(out, this.m_lCSeq);

    // todo Is it necessary we look at the CSeq method?   Can we check the transaction type???
    int methodId = DsTokenSipMethodDictionary.getEncoding(getMethodID());

    if (methodId != DsTokenSipMethodDictionary.UNKNOWN) {
      out.write(methodId);
    } else {
      // todo why can't I get the method name from response objects???
      if ((getMethodID() == DsSipConstants.UNKNOWN) && (isRequest())) {

        methodId = DsTokenSipMethodDictionary.getEncoding(((DsSipRequest) this).getMethod());
        out.write(methodId);

        if (methodId == DsTokenSipMethodDictionary.UNKNOWN) {
          m_messageDictionary.getEncoding(((DsSipRequest) this).getMethod()).write(out);
        }
      } else {
        out.write(DsTokenSipMethodDictionary.UNKNOWN);
        m_messageDictionary.getEncoding(this.m_strCSeq).write(out);
      }
    }
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      public void writeContentLength(OutputStream out) throws IOException
  */

  public static void writeEncodedContentLength(OutputStream out) throws IOException {
    // do nothing
    // todo do something???
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public static void writeContentLength(OutputStream out, int len) throws IOException
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public static boolean isSingular(int id)
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Removed for CSCec16593 fix
     private void validate(int id)
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsMimeEntity:
      private DsSipHeaderInterface getHeader(int id, DsByteString name, boolean start)
      private DsSipHeaderList retrieveList(int kind, DsByteString name, DsSipHeaderList newList)
      private final DsSipHeaderInterface find(int kind, DsByteString name)
      private DsSipHeaderInterface removeHeader(int id, DsByteString name, boolean start)
      private final DsSipHeaderInterface removeHeaders(int kind, DsByteString name)
      private DsSipHeaderInterface update(DsSipHeaderInterface header, boolean clone)
      private DsSipHeader getSingularValidate(int kind, DsByteString name)
      private DsSipHeader getValidate(int kind, DsByteString name, boolean start)
      private DsSipHeaderInterface removeSingular(DsSipHeaderInterface header, int kind, DsByteString name)
      private DsSipHeaderInterface remove(DsSipHeaderInterface header)
  */

  /**
   * Ensures that the Max-Forwards header is in the message. If one does not exist, the default one
   * is added.
   */
  private void ensureMaxForwards() {
    if (!NO_MAX_FORWARDS && isRequest() && !hasHeaders(MAX_FORWARDS)) {
      addHeader(DEFAULT_MAX_FORWARDS_HEADER, true, false);
    }
  }

  /**
   * Returns the default value of the Max-Forwards header.
   *
   * @return the default value of the Max-Forwards header.
   */
  public static int getDefaultMaxForwards() {
    return MAX_FORWARDS_DEFAULT;
  }
  /**
   * Sets the default value of the Max-Forwards header.
   *
   * @param maxForwards The new default value of the Max-Forwards header.
   */
  public static void setDefaultMaxForwards(int maxForwards) {
    MAX_FORWARDS_DEFAULT = maxForwards;
    DEFAULT_MAX_FORWARDS_HEADER =
        new DsSipHeaderString(MAX_FORWARDS, BS_MAX_FORWARDS, DsByteString.valueOf(maxForwards));
  }

  /*Returns the local and remote sessionid
   */
  public String[] getSessionId() {
    String[] sessionIdValue = new String[2];
    sessionIdValue[0] = sessionIdValue[1] = null;
    DsSipHeaderInterface sessionHeader = getHeader(DsSipConstants.BS_SESSION_ID);
    if (null != sessionHeader) {
      String[] sessionID = sessionHeader.getValue().toString().split(DsSipMessage.SEMICOLON);
      sessionIdValue[0] = sessionID[0];
      if (sessionID.length == 2
          && sessionID[1].toLowerCase().startsWith(DsSipMessage.REMOTESTRING)) {
        String[] remoteID = sessionID[1].split("=");
        if (remoteID.length == 2) {
          sessionIdValue[1] = remoteID[1];
        }
      }
    }
    return sessionIdValue;
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following methods were moved to DsSipMessageBase:
      public void protocolFound(byte[] buffer, int protocolOffset, int protocolCount,
  */

  /**
   * Returns an integer array of all the known header IDs.
   *
   * @return an integer array of all the known header IDs.
   */
  private static int[] allDeepHeaders() {
    int[] ids = new int[DsSipConstants.MAX_KNOWN_HEADER + 1];
    for (int i = 0; i < ids.length; i++) {
      ids[i] = i;
    }
    return ids;
  }

  public final void setEncoded() {
    // m_isEncoded = shouldEncode();

    if (Log.isDebugEnabled()) Log.debug("In setEncoded.  Value is now " + m_isEncoded);
  }

  public final boolean isEncoded() {
    return m_isEncoded;
  }

  public final void setEncodable(boolean encodable) {
    m_canEncode = encodable;
  }

  public final boolean canEncode() {
    return m_canEncode;
  }

  protected final DsTokenSipMessageDictionary getEncodedMessageDictionary() {
    return m_messageDictionary;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Data
  ////////////////////////////////////////////////////////////////////////////////

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following class (static) variables were moved to DsMimeEntity:
      public static final boolean DEBUG = false;
      private static boolean[] deepHeaders;
  */
  /** The default Max-Forwards header that is added to a message when one does not exists. */
  private static DsSipHeaderString DEFAULT_MAX_FORWARDS_HEADER;

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following instance variables were moved to DsSipMessageBase:
      byte versionHigh = 2;
      byte versionLow = 0;
  */

  /** Holds the hash code value of this message object. */
  protected int m_hashCode;

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following instance variable was moved to DsMimeEntity and changed to DsMimeBody m_body
      protected DsByteString dsBody;
  */

  /** Holds the reference to the binding info of this message. */
  protected DsBindingInfo m_bindingInfo = new DsBindingInfo();

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following instance variables were moved to DsMimeEntity:
      private DsByteString m_strValue;
      private boolean m_bFinalized;
      private DsSipHeaderInterface headerType[];
  */
  /* CAFFEINE 2.0 DEVELOPMENT
     Removed it between version DsSipMessage.java@@/main/cisco_main/caffeine2/3
     and DsSipMessage.java@@/main/cisco_main/caffeine2/4
      private boolean m_doProcessRoute = true;
  */

  /** Holds the CSeq number value of the CSeq header. */
  protected long m_lCSeq;
  /** Holds the value of CSeq header. */
  protected DsByteString m_strCSeq;
  /** Holds the value of CallID header. */
  protected DsByteString m_strCallId;

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following instance variables were moved to DsMimeEntity:
      private int m_clHdrVal = -1;
  */

  /** Holds the reference of the transaction key. */
  protected DsSipTransactionKey m_key;

  private boolean m_canEncode = false;
  private boolean m_isEncoded = false;
  private DsTokenSipMessageDictionary m_messageDictionary = null;
  int debugIndex = 0;
  protected Calendar m_timestamp;

  /**
   * indicates message normalized status.<br>
   * after execution of normalization(pre, post) this is updated
   */
  protected SipMsgNormalizationState normalizationState = SipMsgNormalizationState.UNMODIFIED;

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following class (static) variables were moved to DsSipMessageBase:
       private static int HEADER_PRIORITY_LEVEL = CONTENT_LENGTH;
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add MIME body and Sipfrag support
     The following class (static) and instance variables were moved to DsMimeEntity:
      private static boolean s_bCommaSeparated = false;
      private static boolean s_bAllHeaders;
      private DsSipHeader header;
  */

  /** Represents an array of header ids and denotes the preferred setting for deep parsing level. */
  public static final int[] PREFERRED_DEEP_HEADERS = {VIA};
  /**
   * Represents an empty array that can be used so that no deep parsing of any header should happen
   * while parsing the SIP message.
   */
  public static final int[] NO_DEEP_HEADERS = {};
  /**
   * Represents an array of default header ids that will be deeply parsed while parsing of SIP
   * message.
   */
  public static final int[] DEFAULT_DEEP_HEADERS = {TO, FROM, CSEQ, CALL_ID, VIA};
  /**
   * Represents an array of all the header ids. This value can be used if all the headers should be
   * deeply parsed while parsing SIP message.
   */
  public static final int[] ALL_HEADERS = allDeepHeaders();

  /** Flag that tells whether RFC 3261 style transaction key should be used. */
  static boolean NEW_KEY = false;
  /**
   * Represents the Max Transmission Unit size for UDP.
   *
   * @deprecated get the MTU per network
   */
  public static int MTU;

  /**
   * Represents the default value of Max Transmission Unit size for UDP.
   *
   * @deprecated get the default MTU from the Network Properties
   */
  public static final int DEFAULT_MTU = 1500;

  /**
   * The default value for the number of Max-Forwards to insert into SIP requests w/o a Max-Forwards
   * header.
   */
  private static int MAX_FORWARDS_DEFAULT = 70;

  /**
   * The static flag that tells whether Max-Forwards should not be added explicitly by stack into
   * SIP requests. This is off by default and can be set by specifying the Java System Property
   * "com.dynamicsoft.DsLibs.DsSipObject.noMaxForwards" to 'true'.
   */
  private static boolean NO_MAX_FORWARDS;

  /** <code>true</code> if we are running as a JAIN stack. */
  private static final boolean m_jainCompatability;

  public static final String SEMICOLON = ";";

  private static final String NULLSESSIONID = "00000000000000000000000000000000";

  private static final String UUIDREGEX = "[0-9a-fA-F]{32}";

  private static final int SESSIONPROGRESSRESPONSECODE = 180;

  private static final int REDIRECTMAXRESPONSECODE = 399;

  public static final String REMOTESTRING = "remote=";

  private static final String SESSIONIDDELIMITER = ";remote=";

  private static final String SESSIONIDREGEX = UUIDREGEX + SESSIONIDDELIMITER + UUIDREGEX;

  // Reason for message generation, one of the reason defined in DsMessageLoggingInterface
  private int applicationReason;

  public int getApplicationReason() {
    return applicationReason;
  }

  public void setApplicationReason(int reason) {
    this.applicationReason = reason;
  }

  // Initializes the deep headers bit set, that tells which headers should be
  // deeply parsed.
  static {
    // Check for the header priority level property
    String val = DsConfigManager.getProperty(DsConfigManager.PROP_HEADER_PRI);
    if (val != null) {
      try {
        int level = Integer.parseInt(val);
        setHeaderPriority(level);
      } catch (NumberFormatException nfe) {
        if (DsLog4j.messageCat.isEnabled(Level.ERROR)) {
          DsLog4j.messageCat.error(
              "Invalid value for the \""
                  + DsConfigManager.PROP_HEADER_PRI
                  + "\" property : ["
                  + val
                  + "]. The default value will be used.");
        }
      }
    }

    // initDeepHeaders(DEFAULT_DEEP_HEADERS);
    initDeepHeaders(PREFERRED_DEEP_HEADERS);
    // Check for the key construction style
    NEW_KEY =
        DsConfigManager.getProperty(DsConfigManager.PROP_KEY, DsConfigManager.PROP_KEY_DEFAULT);

    MTU = DsConfigManager.getProperty(DsConfigManager.PROP_MTU, DsConfigManager.PROP_MTU_DEFAULT);

    setDefaultMaxForwards(MAX_FORWARDS_DEFAULT);

    m_jainCompatability =
        DsConfigManager.getProperty(DsConfigManager.PROP_JAIN, DsConfigManager.PROP_JAIN_DEFAULT);

    // JAIN uses the classic key
    if (m_jainCompatability) {
      NEW_KEY = false;
    }
    // Whether the Max-Forwards header should not be addeded explicitly by stack.
    NO_MAX_FORWARDS =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_NO_MAX_FORWARDS, DsConfigManager.PROP_NO_MAX_FORWARDS_DEFAULT);
  }

  /**
   * Compares two request objects to see if they are logically equivalent.
   *
   * @deprecated
   * @param message The message to compare to
   * @return integer 0 if the messages are equal; != 0 if they are not
   * @throws Exception
   */
  public final int debugCompareTo(DsSipMessage message) throws Exception {
    if (isRequest()) {
      if (message.isResponse()) {
        return -1;
      }
    } else {
      if (message.isRequest()) {
        return -2;
      }
    }

    // startline
    if (isResponse()) {
      DsSipResponse originalResponse = (DsSipResponse) this;
      DsSipResponse finalResponse = (DsSipResponse) message;

      if (originalResponse.getStatusCode() != finalResponse.getStatusCode()) {
        return -3;
      }
      if (originalResponse.getResponseClass() != finalResponse.getResponseClass()) {
        return -4;
      }
      if (originalResponse.getReasonPhrase().compareTo(finalResponse.getReasonPhrase()) != 0) {
        return -5;
      }
    } else {
      DsSipRequest originalRequest = (DsSipRequest) this;
      DsSipRequest finalRequest = (DsSipRequest) message;

      if (originalRequest.getMethod().compareTo(finalRequest.getMethod()) != 0) {
        return -6;
      }
      if (originalRequest.getURI().equals(finalRequest.getURI()) == false) {
        return -7;
      }
    }

    // now in common DsSipMessage stuff
    if (getMethodID() != message.getMethodID()) {
      return -8;
    }

    if (getCallId().compareTo(message.getCallId()) != 0) {
      return -9;
    }

    if ((getBodyType() == null) && (message.getBodyType() == null)) {
      // do nothing
    } else if ((getBodyType() == null) || (message.getBodyType() == null)) {
      return -100;
    } else {
      if (getBodyType().compareTo(message.getBodyType()) != 0) {
        return -10;
      }
    }

    if ((getContentTypeHeader() != null)
        && (getContentTypeHeaderValidate().getSubType().equals(DsSipConstants.BS_SDP))) {
      // compare the SDP
      // SDP has no comparison functions, and the endlines can differ depending on encoder/decoder-
      //  \n vs \r\n

      String firstBody = getBody().toString();
      String lastBody = message.getBody().toString();

      StringTokenizer firstST = new StringTokenizer(firstBody);
      StringTokenizer lastST = new StringTokenizer(lastBody);

      if (firstST.countTokens() != lastST.countTokens()) {
        return -1;
      }

      while (firstST.hasMoreTokens()) {
        int result = firstST.nextToken().compareTo(lastST.nextToken());
        if (result != 0) {
          return result;
        }
      }
      // firstMessageBody.equals(lastMessageBody);
    } else {
      // compare the raw data
      if (getBodyLength() != message.getBodyLength()) {
        return -1;
      }

      if (getBodyLength() > 0) {
        if (getBody().compareTo(message.getBody()) != 0) {
          return -1;
        }
      }

      if (getContentLength() != message.getContentLength()) {
        return -1;
      }
    }

    if (getCSeqMethod().compareTo(message.getCSeqMethod()) != 0) {
      return -11;
    }

    if (getCSeqNumber() != message.getCSeqNumber()) {
      return -12;
    }

    if (getCSeqType() != message.getCSeqType()) {
      return -13;
    }

    // now the headers

    DsSipHeaderInterface headerList[][] = new DsSipHeaderInterface[2][];

    headerList[0] = (DsSipHeaderInterface[]) getDebugHeadersList();
    headerList[1] = (DsSipHeaderInterface[]) message.getDebugHeadersList();

    for (int type = 0; type < headerList[0].length; type++) {
      if (Log.isDebugEnabled()) Log.debug("Trying header " + type);

      if (DsTokenSipHeaderDictionary.isBlockedHeader(type) == false) {
        if (((headerList[0][type] == null) && (headerList[1][type] != null))
            || ((headerList[0][type] != null) && (headerList[1][type] == null))) {
          return -27;
        } else if ((headerList[0][type] == null) && (headerList[1][type] == null)) {
          continue;
        } else {
          switch (headerList[0][type].getForm()) {
            case DsSipHeaderInterface.HEADER:
              if (DsTokenSipHeaderDictionary.isBlockedHeader(
                      ((DsSipHeader) headerList[0][type]).getHeaderID())
                  == false) {
                if (Log.isDebugEnabled()) Log.debug("Comparing individual header");
                if (debugCompareHeaders(
                        (DsSipHeader) headerList[0][type], (DsSipHeader) headerList[1][type])
                    == false) {
                  return -28;
                }
              }
              break;
            case DsSipHeaderInterface.STRING:
              DsSipHeaderString originalstr = (DsSipHeaderString) headerList[0][type];
              DsSipHeaderString finalstr = (DsSipHeaderString) headerList[1][type];
              DsSipHeader myOriginalHeader =
                  DsSipHeader.newInstance(
                      headerList[0][type].getHeaderID(), headerList[0][type].getToken());
              DsSipHeader myFinalHeader =
                  DsSipHeader.newInstance(
                      headerList[1][type].getHeaderID(), headerList[1][type].getToken());
              myOriginalHeader.parse(
                  originalstr.data(), originalstr.offset(), originalstr.length());
              myFinalHeader.parse(finalstr.data(), finalstr.offset(), finalstr.length());
              if (DsTokenSipHeaderDictionary.isBlockedHeader(myOriginalHeader.getHeaderID())
                  == false) {
                if (Log.isDebugEnabled()) Log.debug("Comparing string header");
                if (debugCompareHeaders(myOriginalHeader, myFinalHeader) == false) {
                  return -29;
                }
              }
              break;
            case DsSipHeaderInterface.LIST:
              ((DsSipHeaderList) headerList[0][type]).validate();
              ((DsSipHeaderList) headerList[1][type]).validate();

              DsSipHeaderList originalHeaderList = (DsSipHeaderList) headerList[0][type];
              DsSipHeaderList finalHeaderList = (DsSipHeaderList) headerList[1][type];
              DsSipHeader originalListHeader = (DsSipHeader) originalHeaderList.getFirst();
              DsSipHeader finalListHeader = (DsSipHeader) finalHeaderList.getFirst();

              if (((originalListHeader == null) && (finalListHeader != null))
                  || ((originalListHeader != null) && (finalListHeader == null))) {
                return -30;
              } else if ((originalListHeader == null) && (finalListHeader == null)) {
                return 0;
              }

              while (originalListHeader != null) {
                if (DsTokenSipHeaderDictionary.isBlockedHeader(originalListHeader.getHeaderID())
                    == false) {

                  if (debugCompareHeaders(originalListHeader, finalListHeader) == false) {
                    System.out.println(
                        "Error Comparing member of list header - " + originalListHeader.getToken());
                    System.out.println("<" + originalListHeader + ">");
                    System.out.println("<" + finalListHeader + ">");

                    return -31;
                  }
                }
                originalListHeader = (DsSipHeader) originalListHeader.getNext();
                finalListHeader = (DsSipHeader) finalListHeader.getNext();
              }
              if ((originalListHeader == null) && (finalListHeader != null)) {
                return -32;
              }
              break;
            default:
              throw new DsSipParserException(
                  "Invalid header interface type returned in writeEncodedHeaderType");
          }
        }
      }
    } // _for
    // Write in-built headers

    // now test the wellknown header interfaces
    if (((getAuthenticationHeader() == null) && (message.getAuthenticationHeader() != null))
        || ((getAuthenticationHeader() != null) && (message.getAuthenticationHeader() == null))) {
      return -14;
    } else if ((getAuthenticationHeader() != null) && (message.getAuthenticationHeader() != null)) {
      if (getAuthenticationHeader().equals(message.getAuthenticationHeader()) == false) {
        return -15;
      }
    }

    if (((getContactHeaders() == null) && (message.getContactHeaders() != null))
        || ((getContactHeaders() != null) && (message.getContactHeaders() == null))) {
      return -16;
    } else if ((getContactHeaders() != null) && (message.getContactHeaders() != null)) {
      if (getContactHeaders().equals(message.getContactHeaders()) == false) {
        return -17;
      }
    }

    try {
      if (((getContentTypeHeaderValidate() == null)
              && (message.getContentTypeHeaderValidate() != null))
          || ((getContentTypeHeaderValidate() != null)
              && (message.getContentTypeHeaderValidate() == null))) {
        return -18;
      } else if ((getContentTypeHeaderValidate() != null)
          && (message.getContentTypeHeaderValidate() != null)) {
        if (getContentTypeHeaderValidate().equals(message.getContentTypeHeaderValidate())
            == false) {
          return -19;
        }
      }

      if (((getFromHeaderValidate() == null) && (message.getFromHeaderValidate() != null))
          || ((getFromHeaderValidate() != null) && (message.getFromHeaderValidate() == null))) {
        return -20;
      } else if ((getFromHeaderValidate() != null) && (message.getFromHeaderValidate() != null)) {
        if (getFromHeaderValidate().equals(message.getFromHeaderValidate()) == false) {
          return -21;
        }
      }

      if (((getToHeaderValidate() == null) && (message.getToHeaderValidate() != null))
          || ((getToHeaderValidate() != null) && (message.getToHeaderValidate() == null))) {
        return -22;
      } else if ((getToHeaderValidate() != null) && (message.getToHeaderValidate() != null)) {
        if (getToHeaderValidate().equals(message.getToHeaderValidate()) == false) {
          return -23;
        }
      }

      if (((getViaHeaders() == null) && (message.getViaHeaders() != null))
          || ((getViaHeaders() != null) && (message.getViaHeaders() == null))) {
        return -24;
      } else if ((getViaHeaders() != null) && (message.getViaHeaders() != null)) {
        if (getViaHeaders().equals(message.getViaHeaders()) == false) {
          return -25;
        }
      }
    } catch (Exception e) {
      return -26;
    }

    return 0;
  }

  /**
   * Private utility for debugCompareTo.
   *
   * @deprecated
   * @param firstHeader
   * @param lastHeader
   * @return
   */
  private final boolean debugCompareHeaders(DsSipHeader firstHeader, DsSipHeader lastHeader) {
    // hack for UA parameter comparison
    debugStripParamQuotes(firstHeader);
    debugStripParamQuotes(lastHeader);

    // hack for ACK requests w/Proxy-Authorization ... we don't want differing URI parameter vals to
    // mark messages different
    if (getMethodID() == ACK) {
      if (firstHeader.getHeaderID() == DsSipProxyAuthorizationHeader.sID) {
        ((DsSipProxyAuthorizationHeader) firstHeader).removeParameter(DsSipConstants.BS_URI);
        ((DsSipProxyAuthorizationHeader) lastHeader).removeParameter(DsSipConstants.BS_URI);
      }
    }

    return (firstHeader.equals(lastHeader));
  }

  /**
   * Private utility for debugCompareTo.
   *
   * @deprecated
   * @param myHeader
   */
  private static final void debugStripParamQuotes(DsSipHeader myHeader) {
    if (myHeader instanceof DsSipParametricHeader) {
      DsParameters params = ((DsSipParametricHeader) myHeader).getParameters();
      if (params != null) {
        Iterator i = params.iterator();
        while (i.hasNext()) {
          DsParameter param = (DsParameter) i.next();
          param.getValue().unquote();
        }
      }
    }
  }

  /**
   * Private utility for debugCompareTo.
   *
   * @deprecated
   * @return
   * @throws Exception
   */
  private final DsSipHeaderInterface[] getDebugHeadersList() throws Exception {
    DsSipHeaderInterface headerList[] = new DsSipHeaderInterface[UNKNOWN_HEADER + 1];
    int len = headerType.length - 1;

    for (int type = 0; type < len; type++) {
      if (Log.isDebugEnabled()) Log.debug("Trying header " + type);
      switch (type) {
        case CSEQ:
        case CALL_ID:
        case CONTENT_LENGTH:
          break;
        default:
          if (DsTokenSipHeaderDictionary.isBlockedHeader(type) == false) {
            if (isSingular(type)) {
              headerList[type] = getHeaderValidate(type);
            } else {
              // list
              headerList[type] = getHeadersValidate(type);
            }
          } else {
            if (Log.isDebugEnabled()) Log.debug("Header " + type + " is blocked");
          }
          break;
      }
    } // _for

    DsSipHeaderList finalList = (DsSipHeaderList) headerType[len];

    if (finalList != null) {
      DsSipHeaderInterface l = (DsSipHeaderInterface) finalList.getFirst();

      if (Log.isDebugEnabled())
        Log.debug(finalList.size() + " number of headers present in the last header block");

      while (l != null) {
        headerList[l.getHeaderID()] = l;
        l = (DsSipHeaderInterface) l.getNext();
      }
    }

    return headerList;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // All the code below this line is unique to the Caffeine Stack.
  //
  // This code can be either merged in logically with the code above, or grouped below as a whole.
  // In either case, the purpose of the code should be documented and noted as appropriate
  //

  /**
   * Checks the equality of in-built headers.
   *
   * @param entity MIME entity
   * @return true if in-built headers are equal.
   */
  protected boolean equalsInBuiltHeaders(DsMimeEntity entity) {
    if (!(entity instanceof DsSipMessage)) return false;
    DsSipMessage message = (DsSipMessage) entity;
    // Call-ID
    if (!DsByteString.equals(m_strCallId, message.m_strCallId)) return false;
    // CSeq
    if (m_lCSeq != message.m_lCSeq || !DsByteString.equals(m_strCSeq, message.m_strCSeq))
      return false;
    return true;
  }

  /**
   * Writes in built headers to output stream.
   *
   * @param out output stream
   * @param len content length
   */
  protected void writeInBuiltHeaders(OutputStream out, int len) throws IOException {
    writeCallId(out);
    writeCSeq(out);

    // In JAIN, this will be an actual header, in the native stack, this is just the body length
    if (!m_jainCompatability) {
      writeContentLength(out, len);
    }
  }

  protected boolean isInBuilt(int id) {
    switch (id) {
      case CSEQ: /* falls through */
      case CALL_ID:
        return true;
      case CONTENT_LENGTH:
        if (!m_jainCompatability) {
          return true;
        }
    }
    return false;
  }

  protected boolean hasInBuiltHeader(int id) {
    switch (id) {
      case CSEQ:
        if (m_lCSeq > 0 && m_strCSeq != null) {
          return true;
        }
        return false;
      case CALL_ID:
        if (m_strCallId != null) {
          return true;
        }
        return false;
      case CONTENT_LENGTH:
        if (!m_jainCompatability) {
          return true;
        }
        return false;
      default:
        return false;
    }
  }

  protected DsSipHeaderInterface getInBuiltHeader(int id) {
    switch (id) {
      case CSEQ:
        if (m_lCSeq > 0 && m_strCSeq != null) {
          return new DsSipCSeqHeader(m_lCSeq, m_strCSeq);
        }
        return null;
      case CALL_ID:
        if (m_strCallId != null) {
          return new DsSipCallIdHeader(m_strCallId);
        }
        return null;
      case CONTENT_LENGTH:
        if (!m_jainCompatability) {
          return new DsSipContentLengthHeader(getContentLength());
        }
        return null;
      default:
        return null;
    }
  }

  protected DsSipHeaderInterface updateInBuiltHeader(int id, DsSipHeaderInterface header) {
    if (header == null) {
      return removeInBuiltHeader(id);
    }
    DsSipHeaderInterface hdr = null;
    switch (id) {
      case CSEQ:
        hdr = getInBuiltHeader(CSEQ);
        DsSipCSeqHeader cseqHdr = (DsSipCSeqHeader) header;
        m_lCSeq = cseqHdr.getNumber();
        m_strCSeq = cseqHdr.getMethod();
        break;
      case CALL_ID:
        hdr = getInBuiltHeader(CALL_ID);
        DsSipCallIdHeader callIdHdr = (DsSipCallIdHeader) header;
        m_strCallId = callIdHdr.getCallId();
        break;
      case CONTENT_LENGTH:
        // do not allow application to update content length
        return null;
    }
    return hdr;
  }

  protected DsSipHeaderInterface removeInBuiltHeader(int id) {
    DsSipHeaderInterface header = null;
    switch (id) {
      case CSEQ:
        header = getInBuiltHeader(CSEQ);
        m_lCSeq = 0;
        m_strCSeq = null;
        break;
      case CALL_ID:
        header = getInBuiltHeader(CALL_ID);
        m_strCallId = null;
        break;
      case CONTENT_LENGTH:
        // do not allow application to update content length
        return null;
    }
    return header;
  }

  /**
   * Adds/updates the Session-Id header to the message in following cases If SessionId header
   * doesn't exist SessionId header is not valid Update required in session Id header
   */
  public void addSessionIDHeader() {
    // Check if Session-ID header exists
    try {
      Log.debug("Inside addSessionIDHeader()");
      DsSipHeaderInterface sessionIDHeader = getHeader(new DsByteString("Session-ID"));
      if (sessionIDHeader == null) Log.debug("Session-Id header not found Adding one");

      if (getCallId() != null) {
        if (sessionIDHeader == null || isSessionIdHeaderUpdateNeeded()) {
          SIPSession session = SIPSessions.getActiveSession(getCallId().toString());
          if (session != null) {

            DsByteString sessionIdValue = null;

            if (isMessageFromUAS(session)) {
              if (session.sessionAttrib.isUacStandardSessionIDImplementation()) {
                sessionIdValue =
                    new DsByteString(
                        session.sessionAttrib.getRemoteUuid()
                            + SESSIONIDDELIMITER
                            + session.sessionAttrib.getLocalUuid());
              } else {
                sessionIdValue = new DsByteString(session.sessionAttrib.getLocalUuid());
              }
            } else {

              // For response the remote uuid is the local and
              // vice versa
              if (session.sessionAttrib.isUasStandardSessionIDImplementation()) {
                sessionIdValue =
                    new DsByteString(
                        session.sessionAttrib.getLocalUuid()
                            + SESSIONIDDELIMITER
                            + session.sessionAttrib.getRemoteUuid());
              } else {
                String remoteUuid = session.sessionAttrib.getRemoteUuid();
                if (remoteUuid == null || remoteUuid == NULLSESSIONID) {
                  remoteUuid = session.sessionAttrib.getLocalUuid();
                }
                sessionIdValue = new DsByteString(session.sessionAttrib.getRemoteUuid());
              }
            }

            if (sessionIdValue != null) {
              DsSipHeaderString sessionID =
                  new DsSipHeaderString(
                      DsSipMsgParser.getHeader(DsSipConstants.BS_SESSION_ID),
                      DsSipConstants.BS_SESSION_ID,
                      sessionIdValue);
              if (sessionIDHeader == null) {
                addHeader(sessionID);
                Log.debug("Added Session-Id header " + sessionID);
              } else {
                removeHeader(new DsByteString("Session-ID"));
                addHeader(sessionID);
                Log.debug("Updated Session-Id header to " + sessionID);
              }
            }
          } else // check if this is options ping
          {
            if (isRequest() && getMethodID() == DsSipConstants.OPTIONS) {

              String sessionIdValue = SIPSessionID.generateUuid(this);
              sessionIdValue =
                  sessionIdValue + SESSIONIDDELIMITER + SIPSessionID.getNillsessionid();
              DsSipHeaderString sessionID =
                  new DsSipHeaderString(
                      DsSipMsgParser.getHeader(DsSipConstants.BS_SESSION_ID),
                      DsSipConstants.BS_SESSION_ID,
                      new DsByteString(sessionIdValue));
              if (sessionIDHeader == null) {
                addHeader(sessionID);
              } else {
                removeHeader(new DsByteString("Session-ID"));
                addHeader(sessionID);
              }

              Log.debug("Added Session-Id Header " + sessionID + " to OPTIONS request");
            }
          }
        }
      }
    } catch (Exception e) {
      Log.error("Exception in addSessionIDHeader ", e);
    }
  }

  /**
   * Compares the binding info host/port/transport with the lastDestination tried in the session
   * object
   *
   * @param sipSession
   * @return boolean indicating if the message is from UAS
   */
  protected boolean isMessageFromUAS(SIPSession sipSession) {
    Log.debug("Checking if the message is from UAS");
    boolean isMessageFromUAS = false;
    DsBindingInfo bindingInfo = (DsBindingInfo) getBindingInfo().clone();
    if (bindingInfo != null) {
      if (sipSession != null
          && sipSession.sessionAttrib.getUacNetwork() == bindingInfo.getNetwork()) {
        Log.debug("Message is not from UAS");
      } else {
        Log.debug("Message is from UAS");
        isMessageFromUAS = true;
      }
    } else // This shouldn't happen
    {
      Log.error("Binding info is null for message");
    }
    return isMessageFromUAS;
  }

  /**
   * Updates the localUuid and remoteUuid in the session object for the messages
   *
   * @param forAck if the update session is called for a ACK, if it is ACK we have to update session
   *     only if the ACK is for a success response
   */
  private void updateSession(boolean forAck) {

    try {
      Log.debug("Inside updateSession()");
      String callId = getCallId().toString();
      SIPSession sipSession = SIPSessions.getActiveSession(callId);

      if (sipSession != null) {
        // If ACK is for the failure response we should not update the
        // session id
        if (forAck) {
          if (sipSession.getSessionState() == SessionStateType.FAILED) return;
        }

        DsSipHeaderInterface sessionIDHeader = getHeader(new DsByteString("Session-ID"));

        if (sessionIDHeader != null) {

          String sessionIdValue = sessionIDHeader.getValue().toString().trim();

          if (!isSessionIdHeaderValid()) return;

          boolean standardSessionIdImplementation = false;
          if (sessionIdValue.contains(REMOTESTRING)) standardSessionIdImplementation = true;

          String localUuid = null;
          String remoteUuid = null;

          if (isMessageFromUAS(sipSession)) {
            // localUuid =
            // sessionIdValue.split(SEMICOLON)[1].split("remote=")[1];
            if (standardSessionIdImplementation) {
              remoteUuid = sessionIdValue.split(SEMICOLON)[0];
              if (remoteUuid.equals(NULLSESSIONID)) {
                remoteUuid = SIPSessionID.generateUuid(this);
                Log.info("Generated Remote Uuid for the remote peer Uuid= " + remoteUuid);
              }
              sipSession.sessionAttrib.setUasStandardSessionIDImplementation(true);
            } else {
              remoteUuid = sessionIdValue;
              sipSession.sessionAttrib.setUasStandardSessionIDImplementation(false);
            }

          } else {
            if (standardSessionIdImplementation) {
              localUuid = sessionIdValue.split(SEMICOLON)[0];
              remoteUuid = sessionIdValue.split(SEMICOLON)[1].split(REMOTESTRING)[1];
              sipSession.sessionAttrib.setUacStandardSessionIDImplementation(true);
            } else {
              localUuid = sessionIdValue;
              sipSession.sessionAttrib.setUacStandardSessionIDImplementation(false);
            }
          }
          if (localUuid != null) {
            if (!localUuid.equals(sipSession.sessionAttrib.getLocalUuid())) {
              sipSession.sessionAttrib.setLocalUuid(localUuid);
              Log.info("Updated Local uuid to " + localUuid);
            }
          }

          if (remoteUuid != null) {
            if (!remoteUuid.equals(sipSession.sessionAttrib.getRemoteUuid())) {
              sipSession.sessionAttrib.setRemoteUuid(remoteUuid);
              Log.info("Updated Remote uuid to " + remoteUuid);
            }
          }
        } else {
          // If it is a request uuid gets generated in
          if (isMessageFromUAS(sipSession)) {
            if (sipSession.sessionAttrib.getRemoteUuid().equals(NULLSESSIONID)) {
              sipSession.sessionAttrib.setRemoteUuid(SIPSessionID.generateUuid(this));
              Log.info("Generated Remote uuid " + sipSession.sessionAttrib.getRemoteUuid());
            }
          }
        }
      }
    } catch (Exception e) {
      Log.error("Exception in updateSession() ", e);
    }
  }

  /**
   * Checks for the correctness of the SessionId header based on the RegEx pattern
   *
   * @return true is SessionId header is syntactically valid
   */
  private boolean isSessionIdHeaderValid() {
    Log.debug("Inside isSessionIdHeaderValid()");
    DsSipHeaderInterface sessionIDHeader = getHeader(new DsByteString("Session-ID"));
    String callId = getCallId().toString();
    SIPSession session = SIPSessions.getActiveSession(callId);
    boolean isSessionIdHeaderValid = false;
    if (sessionIDHeader != null && session != null) {
      String sessionIdValue = sessionIDHeader.getValue().toString().trim();
      if (sessionIdValue.contains(REMOTESTRING)) // new standard sessionID
      // implementation
      {
        if (Pattern.matches(SESSIONIDREGEX, sessionIdValue)) {
          Log.debug(
              "Session ID ="
                  + sessionIdValue
                  + " matches the Regular Expression "
                  + SESSIONIDREGEX);
          isSessionIdHeaderValid = true;
        }

      } else if (Pattern.matches(UUIDREGEX, sessionIdValue)) {
        // pre standard implementation
        Log.debug("Pre standard Session Id matches Regular Expression");
        isSessionIdHeaderValid = true;
      }
    }
    return isSessionIdHeaderValid;
  }

  /**
   * Method to check whether the sessionId header update is needed. It checks for the syntax of the
   * session Id header of the message and also matches the uuid's with our stored uuid's and returns
   * true if update is needed
   *
   * @return true if the SessionId header should be updated in the message
   */
  private boolean isSessionIdHeaderUpdateNeeded() {
    boolean isSessionIdHeaderUpdateNeeded = false;
    try {
      Log.debug("Inside isSessionIdHeaderUpdateNeeded()");
      DsSipHeaderInterface sessionIDHeader = getHeader(new DsByteString("Session-ID"));
      SIPSession session = SIPSessions.getActiveSession(getCallId().toString());
      if (!isSessionIdHeaderValid()) {
        isSessionIdHeaderUpdateNeeded = true;
      } else if (sessionIDHeader != null && session != null) {
        String sessionIdValue = sessionIDHeader.getValue().toString().trim();
        boolean standardSessionIdImplementation = false;
        if (sessionIdValue.contains(REMOTESTRING)) standardSessionIdImplementation = true;

        String localUuid = null;
        String remoteUuid = null;

        if (isMessageFromUAS(session)) {
          if (standardSessionIdImplementation
              != session.sessionAttrib.isUacStandardSessionIDImplementation()) {
            isSessionIdHeaderUpdateNeeded = true; // session-id
            // modification
            // needed for
            // compatibility
          } else if (standardSessionIdImplementation) {
            localUuid = sessionIdValue.split(SEMICOLON)[1].split(REMOTESTRING)[1];
            remoteUuid = sessionIdValue.split(SEMICOLON)[0];
            if (!(localUuid.equals(session.sessionAttrib.getLocalUuid())
                && remoteUuid.equals(session.sessionAttrib.getRemoteUuid()))) {
              Log.debug("SessionId is not valid update needed");
              isSessionIdHeaderUpdateNeeded = true;
            }

          } else {
            remoteUuid = sessionIdValue.trim();
            if (!remoteUuid.equals(session.sessionAttrib.getRemoteUuid())) {
              Log.debug("localUuid is not upto date in response SessionId update needed");
              isSessionIdHeaderUpdateNeeded = true;
            }
          }

        } else {
          if (standardSessionIdImplementation
              != session.sessionAttrib.isUasStandardSessionIDImplementation()) {
            isSessionIdHeaderUpdateNeeded = true; // session-id
            // modification
            // needed for
            // compatibility
          } else if (standardSessionIdImplementation) {
            localUuid = sessionIdValue.split(SEMICOLON)[0];
            remoteUuid = sessionIdValue.split(SEMICOLON)[1].split(REMOTESTRING)[1];
            if (!(localUuid.equals(session.sessionAttrib.getLocalUuid())
                && remoteUuid.equals(session.sessionAttrib.getRemoteUuid()))) {
              Log.debug("SessionId not valid  update needed");
              isSessionIdHeaderUpdateNeeded = true;
            }
          } else {
            localUuid = sessionIdValue.trim();
            if (!localUuid.equals(session.sessionAttrib.getLocalUuid())) {
              Log.debug("localUuid not valid SessionId update  needed");
              isSessionIdHeaderUpdateNeeded = true;
            }
          }
        }
      } else {
        isSessionIdHeaderUpdateNeeded = true;
      }
    } catch (Exception e) {
      Log.error("Exception in isSessionIdHeaderUpdateNeeded ", e);
      isSessionIdHeaderUpdateNeeded = true; // If its not a valid
      // SessionId header we have
      // to
      // update this header
    }
    return isSessionIdHeaderUpdateNeeded;
  }

  /** This method updates the session object and also updates the Session-Id header if needed */
  public void addAndUpdateSessionIdHeader() {
    Log.debug("Inside addAndUpdateSessionIdHeader()");
    if (isRequest()) {

      int methodId = getMethodID();
      if (methodId == DsSipRequest.ACK) {
        updateSession(true);
      } else if (methodId != DsSipRequest.CANCEL) {
        updateSession(false);
      }

    } else if (isResponse()) {
      DsSipResponse response = (DsSipResponse) this;
      int methodId = response.getStatusCode();
      Log.debug("Method Id " + methodId + " ");
      if (methodId >= SESSIONPROGRESSRESPONSECODE && methodId <= REDIRECTMAXRESPONSECODE) {
        updateSession(false);
      }
    }

    DsSipHeaderInterface sessionIDHeader = this.getHeader(new DsByteString("Session-ID"));
    if (sessionIDHeader == null) addSessionIDHeader();
    else if (!isSessionIdHeaderValid()) addSessionIDHeader();
  }

  /**
   * Helper instance method to take advantage of polymorphism. Must be reimplemented in subclasses.
   */
  protected int getHeaderPriLevel() {
    return DsSipMessage.getHeaderPriority();
  }

  /**
   * Create element listener based on header id.
   *
   * @param headerId header id
   * @return element listener
   */
  protected DsSipElementListener createElementListener(int headerId) {
    if (m_isEncoded == true) {
      if (initHeaderForEncoding(headerId)) {
        return header;
      }
      return null;
    } else {
      return super.createElementListener(headerId);
    }
  }

  /**
   * Returns the timestamp in the message. This returns the reference, so mutable. The application
   * should not alter the timestamp through this reference. Instead, setTimestamp(Calendar) can be
   * called to set the Timestamp.
   *
   * @return the Timestamp associated with the message
   * @see #setTimestamp(Calendar)
   */
  public Calendar getTimestamp() {
    return m_timestamp;
  }

  /** Set to Timestamp to current time. */
  public void setTimestamp() {
    if (null == m_timestamp) {
      m_timestamp = new GregorianCalendar();
    }
  }

  /**
   * Set the timestamp to time specified.
   *
   * @param cal the new timestamp of the message.
   */
  public void setTimestamp(Calendar cal) {
    m_timestamp = cal;
  }

  /**
   * Inserts a X-Cisco-Peer-Cert-Info header to the initial Invite.
   *
   * <p>When a header size exceeds CERT_HEADER_SIZE_MAX a new header created
   *
   * @param request
   */
  public void addPeerCertInfoHeader(DsSSLBindingInfo bindingInfo) {
    try {
      ArrayList<String> ciscoCertInfoHeaderVal = buildCertInfoHeaderValue(bindingInfo);
      if (ciscoCertInfoHeaderVal != null && !ciscoCertInfoHeaderVal.isEmpty()) {
        ciscoCertInfoHeaderVal.forEach(
            headerValue -> {
              DsByteString headerValueByteString = new DsByteString(headerValue);
              DsByteString xCiscoCertHeaderName = new DsByteString(X_CISCO_CERT_HEADER);
              DsSipHeaderString ciscoCertInfoHeader =
                  new DsSipHeaderString(
                      DsSipMsgParser.getHeader(xCiscoCertHeaderName),
                      xCiscoCertHeaderName,
                      headerValueByteString);
              addHeader(ciscoCertInfoHeader);
              Log.info("Added " + ciscoCertInfoHeader + " to request");
            });
      }
    } catch (Exception e) {
      Log.error("Exception Adding " + X_CISCO_CERT_HEADER, e);
    }
  }

  /**
   * Builds X-Cisco-Peer-Cert-Info value Format = <cn=CommonNameInCert>, <sanSANType=SAN1Entry>,
   * <sanSANType=SAN2Entry>
   *
   * <p>And Adds the comma separated header values to ArrayList. Each entry in the ArrayList
   * represents a individual cert header.
   *
   * <p>When a header size exceeds CERT_HEADER_SIZE_MAX a new entry is added.
   *
   * <p>If the size exceeds CERT_HEADER_TOTAL_SIZE_MAX , Iteration stops and untreated values are
   * ignored.
   *
   * @param dsBindingInfo
   * @return X-Cisco-Peer-Cert-Info header value
   */
  ArrayList<String> buildCertInfoHeaderValue(DsSSLBindingInfo dsBindingInfo) {
    String cn = dsBindingInfo.getPeerCommonName();
    List<SubjectAltName> sanList = dsBindingInfo.getPeerSubjectAltName();
    ArrayList<String> certHeaderList;
    String headerVal = "";

    if (cn != null) {
      if (cn.length() <= CERT_HEADER_SIZE_MAX) {
        headerVal = formCertHeaderValue(CN, cn);
      } else {
        Log.warn("CN is exceeding " + CERT_HEADER_SIZE_MAX + " ,not added to header");
      }
    } else {
      Log.warn("CN is null for connection " + dsBindingInfo);
    }

    certHeaderList = buildCertHeaderSANValue(sanList, headerVal);

    return certHeaderList;
  }

  private ArrayList<String> buildCertHeaderSANValue(List<SubjectAltName> sanList, String cnValue) {
    String headerVal = cnValue;
    int totalHeaderValueSize = headerVal.length();

    ArrayList<String> certHeaderList = new ArrayList<>();
    if (sanList != null && !sanList.isEmpty()) {
      int sanListSize = sanList.size();
      for (int index = 0; index < sanListSize; index++) {
        String currentSan =
            formCertHeaderValue(
                SAN + sanList.get(index).getSanType().toString(), sanList.get(index).getSanName());

        if ((totalHeaderValueSize + currentSan.length()) > CERT_HEADER_TOTAL_SIZE_MAX) {
          break;
        }

        if ((currentSan.length() + headerVal.length() + COMMA_SPACE_SIZE) < CERT_HEADER_SIZE_MAX) {
          if (headerVal.isEmpty()) {
            headerVal = currentSan;
            totalHeaderValueSize += currentSan.length();
          } else {
            headerVal += COMMA + SPACE + currentSan;
            totalHeaderValueSize += currentSan.length() + COMMA_SPACE_SIZE;
          }
        } else {
          certHeaderList.add(headerVal);
          headerVal = currentSan;
          totalHeaderValueSize += currentSan.length();
        }
      }
    }
    if (!headerVal.isEmpty()) {
      certHeaderList.add(headerVal);
    }
    return certHeaderList;
  }

  private String formCertHeaderValue(String key, String value) {
    return LEFT_ANGLE_BRACKET + key + EQUAL + value + RIGHT_ANGLE_BRACKET;
  }

  /**
   * indicates message is in normalized state
   *
   * @return
   */
  public SipMsgNormalizationState getNormalizationState() {
    return normalizationState;
  }

  /** @param normalized - pass state after execution of normalization(pre | post) */
  public void setNormalizationState(SipMsgNormalizationState normalizationState) {
    this.normalizationState = normalizationState;
  }
} // Ends class DsSipMessage
