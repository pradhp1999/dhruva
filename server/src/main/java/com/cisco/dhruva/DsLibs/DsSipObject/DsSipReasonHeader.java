// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMessageDictionary;
import com.cisco.dhruva.DsLibs.DsUtil.DsLog4j;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.Level;

/**
 * <b>DsSipReasonHeader</b>
 *
 * <p>This class represents the Reason header as specified in RFC 3326. It provides methods to
 * build, access, modify, serialize, and clone the header.
 *
 * <p><b>Header ABNF:</b> <code> <pre>
 * Reason            =  "Reason" ":" reason-value *( "," reason-value)
 * reason-value      =  protocol *( ";" reason-params )
 * protocol          =  "SIP" / "Q.850" / token
 * reason-params     =  protocol-cause / reason-text / reason-extension
 *
 * protocol-cause    =  "cause" "=" cause
 * cause             =  1*DIGIT
 * reason-text       =  "text" "=" quoted-string
 * reason-extension  =  generic-param
 *
 * Examples of Reason headers include:
 *
 *     Reason: SIP ;cause=200 ;text="Call completed elsewhere"
 *     Reason: Q.850 ;cause=16 ;text="Terminated"
 *     Reason: SIP ;cause=600 ;text="Busy everywhere"
 *     Reason: SIP ;cause=580 ;text="Precondition Failure"
 *
 * For completeness, examples showing proposed extensions
 * for this header (draft-koshiko-sipping-reason-indicating-locations-00.txt):
 *
 *     Reason: SIP ; location="uac"
 *     Reason: SIP ; location="uas" ;domain="pc22.biloxi.example"
 *     Reason: SIP ; cause=503 ;text="Service Unavailable" ;location="proxy" ;domain="biloxi.example"
 *     Reason: SIP ; cause=408 ;text="Request Timeout" ;location="non-ip" ;domain="gw.atlanta.example"
 * </pre> </code>
 */
public final class DsSipReasonHeader extends DsSipParametricHeader {
  /** Header ID. */
  public static final byte sID = REASON_HEADER;

  /**
   * Value signifying that the protocol-cause ('cause') parameter is invalid and/or is not present
   * in the header
   */
  public static final int INVALID_CAUSE_VALUE = -1;

  /** Used in writing out the header string */
  private static final DsByteString STR_CAUSE = new DsByteString("cause=");

  private static final DsByteString CAUSE = new DsByteString("cause");

  /** The protocol associated with this reason header. */
  private DsByteString m_protocol = null;

  /** The cause associated with this reason header. */
  private int m_cause = INVALID_CAUSE_VALUE;
  // All other parameters are handled by super's set/getParameters

  /** Default constructor. */
  public DsSipReasonHeader() {
    super();
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.
   *
   * <p>The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.
   *
   * <p>If there is an exception during parsing phase, it will set the invalid flag of this header
   * and retain the various components that it already parsed. One should check the valid flag
   * before retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipReasonHeader(byte[] value) throws DsSipParserException, DsSipParserListenerException {
    this(value, 0, value.length);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.
   *
   * <p>The byte array <code>value</code> should be the value part (data after the colon) of this
   * header.
   *
   * <p>If there is an exception during parsing phase, it will set the invalid flag of this header
   * and retain the various components that it already parsed. One should check the valid flag
   * before retrieving the various components of this header.
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
  public DsSipReasonHeader(byte[] value, int offset, int count)
      throws DsSipParserException, DsSipParserListenerException {
    /*
      CAFFEINE 2.0 bug fix - CSCef03455 It is the initialization sequence problem.
      The origianl super() calling will eventually call down to the child and set child's private date member.
    */
    parse(value, offset, count);
  }

  /**
   * Parses the specified value to extract the various components as per the grammar of this header
   * and constructs this header.
   *
   * <p>The specified byte string <code>value</code> should be the value part (data after the colon)
   * of this header.
   *
   * <p>If there is an exception during parsing phase, it will set the invalid flag of this header
   * and retain the various components that it already parsed. One should check the valid flag
   * before retrieving the various components of this header.
   *
   * @param value the value part of the header that needs to be parsed into the various components
   *     of this header.
   * @throws DsSipParserException if there is an error while parsing the specified value into this
   *     header.
   * @throws DsSipParserListenerException if there is an error condition detected by this header as
   *     a Parser Listener, while parsing.
   */
  public DsSipReasonHeader(DsByteString value)
      throws DsSipParserException, DsSipParserListenerException {
    this(value.data(), value.offset(), value.length());
  }

  /**
   * Returns the token which is the complete name of the header.
   *
   * @return The complete token name
   */
  public DsByteString getToken() {
    return BS_REASON_HEADER;
  }

  /**
   * Returns the token which is the compact name of the header.
   *
   * @return The compact token name
   */
  public DsByteString getCompactToken() {
    return BS_REASON_HEADER;
  }

  /**
   * Returns the header name plus ": " as a single byte string token. The header name will be in the
   * compact form if this header is set to be in compact form.
   *
   * @return the header name plus ": " as a single byte string token.
   */
  public final DsByteString getTokenC() {
    return BS_REASON_HEADER_TOKEN;
  }

  /**
   * Returns the 'protocol' value of the Reason header.
   *
   * @return the string <code>(DsByteString)</code> value of the protocol of the ReasonHeader.
   */
  public DsByteString getProtocol() {
    return m_protocol;
  }

  /**
   * Sets the 'protocol' value of the Reason header; e.g., 'SIP' or 'Q.850'.
   *
   * @param protocol the new string <code>(DsByteString)</code> value for the protocol of the Reason
   *     header.
   */
  public void setProtocol(DsByteString protocol) {
    m_protocol = protocol;
  }

  /**
   * Returns the protocol-cause ('cause') parameter value of the Reason header.
   *
   * <p>A return value of <code>INVALID_CAUSE_VALUE</code> (-1) indicates that the cause parameter
   * value was not present in the header (or was, in fact, invalid).
   *
   * @return the integer value for the protocol-cause ('cause') parameter of the Reason header
   * @see #INVALID_CAUSE_VALUE
   */
  public int getCause() {
    return m_cause;
  }

  /**
   * Sets the protocol-cause ('cause') parameter value of the Reason header.
   *
   * <p>Any SIP status code MAY appear in the Reason header field of a request, assuming the
   * protocol field of the Reason header is SIP.
   *
   * <p>To remove the protocol-cause ('cause') parameter from the header supply a value of <code>
   * INVALID_CAUSE_VALUE</code> (-1) for the cause argument
   *
   * @param cause the new integer value for the protocol-cause ('cause') parameter of the Reason
   *     header
   * @see #INVALID_CAUSE_VALUE
   */
  public void setCause(int cause) {
    if (cause > -1) {
      m_cause = cause;
    } else {
      // we are not treating negative values as illegal arguments; rather, they are a way to specify
      // that the cause parameter should not exist or should be removed from the header
      m_cause = INVALID_CAUSE_VALUE;
    }
  }

  /**
   * Returns the reason-text ('text') parameter value of the Reason header.
   *
   * @return the string <code>(DsByteString)</code> value for the reason-text ('text') parameter of
   *     the ReasonHeader
   */
  public DsByteString getText() {
    return getParameter(BS_TEXT);
  }

  /**
   * Sets the reason-text ('text') parameter value of the Reason header.
   *
   * @param text the new string <code>(DsByteString)</code> value for the reason-text ('text')
   *     parameter of the Reason header
   */
  public void setText(DsByteString text) {
    setParameter(BS_TEXT, text);
  }

  /** Removes the reason-text ('text') parameter of the Reason header. */
  public void removeText() {
    removeParameter(BS_TEXT);
  }

  /**
   * Serializes the value of this header to the specified <code>out</code> byte output stream.
   *
   * @param out the byte output stream where this header's value need to be serialized.
   * @throws IOException if there is an error while writing to the output stream
   */
  public void writeValue(OutputStream out) throws IOException {
    if (m_protocol != null) {
      m_protocol.write(out);
    }

    if (m_cause > -1) {
      BS_SEMI.write(out);
      STR_CAUSE.write(out);
      byte[] bytes = DsByteString.getBytes(Integer.toString(m_cause));
      out.write(bytes);
    }

    if (m_paramTable != null) {
      m_paramTable.write(out);
    }
  }

  public void writeEncodedValue(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (m_protocol != null) {
      md.getEncoding(m_protocol).write(out);
    }

    writeEncodedParameters(out, md);
  }

  protected void writeEncodedParameters(OutputStream out, DsTokenSipMessageDictionary md)
      throws IOException {
    if (m_cause > -1) {
      out.write(DsTokenSipConstants.TOKEN_SIP_PARAMETER_LOW);
      md.getEncoding(CAUSE).write(out);
      byte[] bytes = DsByteString.getBytes(Integer.toString(m_cause));
      out.write(bytes);
    }
    super.writeEncodedParameters(out, md);
  }

  /**
   * Copy another header's values to this header.
   *
   * @param header the header whose values should be copied.
   */
  protected void copy(DsSipHeader header) {
    super.copy(header);
    DsSipReasonHeader source = (DsSipReasonHeader) header;

    m_protocol = source.m_protocol;
    m_cause = source.m_cause;
  }

  /**
   * Returns the unique header ID for this header.
   *
   * @return the header ID.
   */
  public final int getHeaderID() {
    return REASON_HEADER;
  }

  /**
   * Returns an indication of the equality of another header to this header.
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

    DsSipReasonHeader header = null;
    try {
      header = (DsSipReasonHeader) obj;
    } catch (ClassCastException e) {
      return false;
    }

    if (m_cause != header.m_cause) {
      return false;
    }

    if (m_protocol != null && !m_protocol.equals(header.m_protocol)) {
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

  /**
   * Clears all the member data and makes this header reusable. That is, the various components
   * (sub-elements) of this headers can be set again.
   *
   * <p>Already constructed objects (e.g., Parameter Tables, etc.) are reused to avoid expensive
   * object creation.
   */
  public void reInit() {
    super.reInit();

    m_protocol = null;
    m_cause = INVALID_CAUSE_VALUE;
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipElementListener Interface implementation
  //

  /*
   * javadoc inherited.
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    switch (elementId) {
      case SINGLE_VALUE:
        m_protocol = new DsByteString(buffer, offset, count);
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
    if (BS_CAUSE.equalsIgnoreCase(buffer, nameOffset, nameCount)) {
      try {
        this.setCause(DsSipMsgParser.parseInt(buffer, valueOffset, valueCount));
      }
      // Don't really care what exception we get (although it is most likely a
      // NumberFormatException) from the act of parsing, any exception represents
      // failure, and we need to deal with it the best we can
      catch (Exception e) {
        // Option A: don't include the cause value in the header and log the issue (more tolerant,
        // but loses data)
        this.setCause(INVALID_CAUSE_VALUE);
        if (DsLog4j.headerCat.isEnabled(Level.WARN)) {
          DsLog4j.headerCat.log(
              Level.WARN, "Exception while parsing the Reason header 'cause' value:", e);
        }

        // Option B: bubble up the exception (less tolerant, but expected based on method signature)
        // throw new DsSipParserListenerException("Exception while parsing the Reason header 'cause'
        // value: "+ e);
      }
    } else {
      super.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);
    }
  }

  /*
  // Quick Testing/Debugging only
  public static void main(String[] args)
  {
      try
      {
          System.out.println();
          System.out.println("<<<<<<<<<< REASON HEADER: QUICK TEST >>>>>>>>>>>>>");
          System.out.println();
          System.out.println("Input a sample reason header, followed by a newline and then EOF (e.g., CTRL-D)");
          System.out.print("    : ");
          byte[] bytes = read();

          DsSipHeader header = null;
          header = new DsSipReasonHeader(bytes);            // Q.850 ;cause = 16 ; text   ="Terminated"
          //header = new DsSipContentDispositionHeader(bytes);  // attachment ; filename=smime.p7s ; handling=required
          //header = new DsSipAppInfoHeader(bytes);           // "Call Timer" <http://mediasvr.provider.net/calltimer.vxml>; id=app4323!sub4+svr56.provider.net

          System.out.println();
          System.out.print(" INPUT: " + new DsByteString(bytes));
          System.out.println("-------");
          System.out.print("HEADER: ");
          header.write(System.out);
          System.out.print(" CLONE: ");
          DsSipHeader clone = (DsSipHeader) header.clone();
          clone.write(System.out);
          System.out.println();
          System.out.println("HEADER == CLONE: " + header.equals(clone));
          System.out.println("CLONE == HEADER: " + clone.equals(header));
          System.out.println();
      }
      catch(Exception e)
      {
          e.printStackTrace();
      }
  }
   */
}
