// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import org.slf4j.event.Level;

/**
 * This class represents a Call-ID header as specified in RFC 3261. It provides methods to build,
 * access, modify, serialize and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Call-ID =  ( "Call-ID" | "i" ) ":" callid
 * callid  =  token [ "@" token ]
 * </pre> </code>
 */
public final class DsSipCallIdHeader extends DsSipStringHeader {
  /** Header token. */
  public static final DsByteString sToken = BS_CALL_ID;
  /** Header ID. */
  public static final byte sID = CALL_ID;
  /** Compact header token. */
  public static final DsByteString sCompactToken = BS_CALL_ID_C;

  /** Seed value for random numbers */
  private static int seed = 0;

  public static final byte sFixedFormatHeaderId =
      DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_CALLID2_HEADER;

  /** Default constructor. */
  public DsSipCallIdHeader() {
    super();
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipCallIdHeader(byte[] value) {
    super(value);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @param offset the offset in the specified byte array, where from the value part, that needs to
   *     be parsed, starts.
   * @param count the total number of bytes, starting from the specified offset, that constitute the
   *     value part.
   */
  public DsSipCallIdHeader(byte[] value, int offset, int count) {
    super(value, offset, count);
  }

  /**
   * Constructs this header with the specified value.<br>
   * The specified byte string <code>value</code> should be the value part (data after the colon) of
   * this header.<br>
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   */
  public DsSipCallIdHeader(DsByteString value) {
    super(value);
  }

  /**
   * Constructor used to accept the hostname as a string. A random ID will be added.
   *
   * @param host the hostname.
   */
  public DsSipCallIdHeader(String host) {
    super();
    setValue(reGenerate(host));
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
    return (isCompact()) ? BS_CALL_ID_C_TOKEN : BS_CALL_ID_TOKEN;
  }

  /**
   * Sets the call ID.
   *
   * @param callId The call ID string.
   */
  public void setCallId(DsByteString callId) {
    setValue(callId);
  }

  /**
   * Returns the call ID.
   *
   * @return the call ID.
   */
  public DsByteString getCallId() {
    return getValue();
  }

  /**
   * Method to get the unique header ID.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return CALL_ID;
  }

  /**
   * Regenerates the Call ID.
   *
   * @return the call ID.
   */
  public static String reGenerate() {
    return reGenerate(getLocalHost());
  }

  /**
   * Regenerates the Call ID.
   *
   * @param host the hostname to be used.
   * @return the call ID.
   */
  public static String reGenerate(String host) {
    if (host == null) {
      host = getLocalHost();
    }

    if (DsLog4j.headerCat.isEnabled(Level.DEBUG)) {
      DsLog4j.headerCat.log(Level.DEBUG, "Generating the call ID using the host name " + host);
    }

    long curr_time = System.currentTimeMillis();

    StringBuffer buff =
        new StringBuffer(64).append(curr_time).append(getNextSeedValue()).append('@').append(host);

    String str = buff.toString();

    if (DsLog4j.headerCat.isEnabled(Level.DEBUG)) {
      DsLog4j.headerCat.log(Level.DEBUG, "Call ID is  " + str);
    }

    return str;
  }

  private static String getLocalHost() {
    String host = null;
    try {
      host = InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      host = "";
    }
    return host;
  }

  private static synchronized int getNextSeedValue() {
    seed++;
    if (seed == 100000) // I don't want it to be too big
    {
      seed = 0;
    }
    return seed;
  }

  public void writeEncodedHeaderName(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    out.write(sFixedFormatHeaderId);
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    int seperator = this.m_strValue.indexOf('@');

    if (seperator > 0) {
      md.getEncoding((this.m_strValue.substring(0, seperator - 1))).write(out);
      md.getEncoding((this.m_strValue.substring(seperator + 1))).write(out);
    } else {
      md.getEncoding(this.m_strValue).write(out);
    }
  }
} // Ends class DsSipCallIdHeader
