// /////////////////////////////////////////////////////////////////
// FILENAME:    DsSipMessageBase.java
//
// MODULE:      DsSipObject
//
// COPYRIGHT:
// ============== copyright 2004 Cisco Systems, Inc. =================
// ==================== all rights reserved =======================
// /////////////////////////////////////////////////////////////////
package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipMime.DsMimeEntity;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMessageListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import java.io.IOException;
import java.io.OutputStream;

/*
 * This class contains common pieces for DsSipMessage and DsSipFragment.
 *
 * The DsSipMessageBase and DsSipMessage together represent the SIP message as per
 * the RFC 3261 and provide methods to
 * build, access, modify, serialize and clone a message.
 * </p>
 * The following documentation applies to both DsSipMessageBase and DsSipMessage.
 */
/**
 * This message can contain headers in any of its two known forms, as defined in {@link
 * DsSipHeaderInterface} interface. In other words, the headers in this message can be of type
 * {@link DsSipHeader} or {@link DsSipHeaderString} type. <br>
 * User can decide what headers should be in DsSipHeader form and what should be in
 * DsSipHeaderString form. The DsSipHeaderString headers are light weight and provides for less
 * memory foot print and thus performance. <br>
 * To specify what headers should be deeply parsed (the deeply parsed headers will be in DsSipHeader
 * form), user can invoke {@link #initDeepHeaders(int[])} by passing an array of header integer IDs.
 * The header integer IDs are defined in {@link DsSipConstants}. There are some predefined constant
 * header ID arrays for convenience.<br>
 *
 * <pre>
 * PREFERRED_DEEP_HEADERS  - in this case only VIA header will be deeply
 *                              parsed.
 * DEFAULT_DEEP_HEADERS    - in this case only TO, FROM and VIA headers will
 *                              be deeply parsed.
 * NO_DEEP_HEADERS         - in this case no header will be deeply parsed.
 * ALL_HEADERS             - in this case all the headers will be deeply parsed.
 * </pre>
 *
 * <p>There is an exception for CSeq, Call-ID and Content-Length headers. These headers are not
 * present in this message either in DsSipHeader form or in DsSipHeaderString. These headers'
 * elements are directly embedded in this message for performance and efficiency reasons. To access
 * these headers' elements, there are corresponding methods defined. Although these in-built headers
 * can still be obtained or updated in DsSipHeader form by calling getHeaderValidate() or
 * updateHeader(), the operations will be very expensive as a new header will be created each time.
 * For example, <br>
 *
 * <pre>
 * {@link #getCSeqMethod()}             - To get the CSeq method name
 * {@link #getCSeqNumber()}             - To get the CSeq number
 *  - To set the CSeq method name
 *          - To set the CSeq number
 * {@link #getContentLength()}          - To get the content length
 * {@link #getCallId()}                 - To get the Call Id
 *      - To set the Call Id
 * </pre>
 *
 * <p>Note that no one header element can be part of two header lists at the same time. For example,
 * if <code>a</code> is an element of header list <code>A</code>, then to add this header <code>a
 * </code> to another header list <code>B</code>, the header <code>a</code> should be removed from
 * header list <code>A</code> first. Also any header that needs to be added in an header list, the
 * next and previous pointers to that element should be <code>null</code>. Refer , and {@link
 * DsSipHeaderList}.
 */
public abstract class DsSipMessageBase extends DsMimeEntity implements DsSipMessageListener {
  ////////////////////////////////////////////////////////////////////////////////
  // member functions
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Method to get the unique method ID for a message, this is the same value as sID and matches the
   * values in DsSipConstants.
   *
   * @return the method ID.
   */
  public abstract int getMethodID();

  /**
   * Serializes the start line of this message to the specified <code>out</code> output stream. This
   * method must be implemented by the subclass.
   *
   * @param out the output stream where the start line needs to be serialized.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public abstract void writeStartLine(OutputStream out) throws IOException;

  /**
   * Checks for the equality of the start line semantics of this message object against the start
   * line of the specified <code>message</code>.
   *
   * @param message the message whose start line semantics needs to be compared for equality against
   *     the start line semantics of this message object.
   * @return <code>true</code> if the start line of this message is semantically equal to the start
   *     line of the specified <code>message</code>, <code>false</code> otherwise.
   */
  public abstract boolean equalsStartLine(DsSipMessageBase message);

  //////////////////////////////////////////////////////////////////////////////////
  //  The following set of methods are convenience functions that can be used
  //  in place of the more general getHeader() and getHeaders() method.
  //////////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the Contact header object if present in this message, otherwise return null. It returns
   * the first Contact header in the header list, if more than one Contact headers are present.
   *
   * @return the Contact header object if present in this message, otherwise return null.
   */
  public DsSipHeaderInterface getContactHeader() {
    return getHeader(CONTACT);
  }

  /**
   * Returns the Contact header object if present in this message, otherwise return null. It returns
   * the first Contact header in the header list, if more than one Contact headers are present. If
   * the contact header is present in the string form, then it would parsed and returned as a
   * DsSipContactHeader instance.
   *
   * @return the Contact header object if present in this message, otherwise return null.
   * @throws DsSipParserException if there is an error while parsing the contact header.
   * @throws DsSipParserListenerException if there is an error condition detected by the Header
   *     listener, while parsing.
   */
  public DsSipContactHeader getContactHeaderValidate()
      throws DsSipParserException, DsSipParserListenerException {
    return (DsSipContactHeader) getHeaderValidate(CONTACT);
  }

  /**
   * Returns the list of Contact header objects, if any, present in this message, otherwise return
   * null.
   *
   * @return the list of Contact header objects if any present in this message, otherwise return
   *     null.
   */
  public DsSipHeaderList getContactHeaders() {
    return getHeaders(CONTACT);
  }

  /**
   * Returns the Content-Type header object if present in this message, otherwise return null. It
   * returns the first Content-Type header in the header list, if more than one Content-Type headers
   * are present.
   *
   * @return the Content-Type header object if present in this message, otherwise return null.
   */
  public DsSipHeaderInterface getContentTypeHeader() {
    return getHeader(CONTENT_TYPE);
  }

  /**
   * Returns the Content-Type header object if present in this message, otherwise return null. It
   * returns the first Content-Type header in the header list, if more than one Content-Type headers
   * are present. If the Content-Type header is present in the string form, then it would parsed and
   * returned as a DsSipContentTypeHeader instance.
   *
   * @return the Content-Type header object if present in this message, otherwise return null.
   * @throws DsSipParserException if there is an error while parsing the Content-type header.
   * @throws DsSipParserListenerException if there is an error condition detected by the Header
   *     listener, while parsing.
   */
  public DsSipContentTypeHeader getContentTypeHeaderValidate()
      throws DsSipParserException, DsSipParserListenerException {
    return (DsSipContentTypeHeader) getHeaderValidate(CONTENT_TYPE);
  }

  /**
   * Returns the From header object if present in this message, otherwise return null. It returns
   * the first From header in the header list, if more than one From headers are present.
   *
   * @return the From header object if present in this message, otherwise return null.
   */
  public DsSipHeaderInterface getFromHeader() {
    return getHeader(FROM);
  }

  /**
   * Returns the From header object if present in this message, otherwise return null. It returns
   * the first From header in the header list, if more than one From headers are present. If the
   * From header is present in the string form, then it would parsed and returned as a
   * DsSipFromHeader instance.
   *
   * @return the From header object if present in this message, otherwise return null.
   * @throws DsSipParserException if there is an error while parsing the From header.
   * @throws DsSipParserListenerException if there is an error condition detected by the Header
   *     listener, while parsing.
   */
  public DsSipFromHeader getFromHeaderValidate()
      throws DsSipParserException, DsSipParserListenerException {
    return (DsSipFromHeader) getHeaderValidate(FROM);
  }

  /**
   * Returns the To header object if present in this message, otherwise return null. It returns the
   * first To header in the header list, if more than one To headers are present.
   *
   * @return the To header object if present in this message, otherwise return null.
   */
  public DsSipHeaderInterface getToHeader() {
    return getHeader(TO);
  }

  /**
   * Returns the To header object if present in this message, otherwise return null. It returns the
   * first To header in the header list, if more than one To headers are present. If the To header
   * is present in the string form, then it would parsed and returned as a DsSipToHeader instance.
   *
   * @return the To header object if present in this message, otherwise return null.
   * @throws DsSipParserException if there is an error while parsing the To header.
   * @throws DsSipParserListenerException if there is an error condition detected by the Header
   *     listener, while parsing.
   */
  public DsSipToHeader getToHeaderValidate()
      throws DsSipParserException, DsSipParserListenerException {
    return (DsSipToHeader) getHeaderValidate(TO);
  }

  /**
   * Returns the Via header object if present in this message, otherwise return null. It returns the
   * first Via header in the header list, if more than one Via headers are present.
   *
   * @return the Via header object if present in this message, otherwise return null.
   */
  public DsSipHeaderInterface getViaHeader() {
    return getHeader(VIA);
  }

  /**
   * Returns the Via header object if present in this message, otherwise return null. It returns the
   * first Via header in the header list, if more than one Via headers are present. If the Via
   * header is present in the string form, then it would parsed and returned as a DsSipViaHeader
   * instance.
   *
   * @return the Via header object if present in this message, otherwise return null.
   * @throws DsSipParserException if there is an error while parsing the Via header.
   * @throws DsSipParserListenerException if there is an error condition detected by the Header
   *     listener, while parsing.
   */
  public DsSipViaHeader getViaHeaderValidate()
      throws DsSipParserException, DsSipParserListenerException {
    return (DsSipViaHeader) getHeaderValidate(VIA);
  }

  /**
   * Returns the list of Via header objects, if any, present in this message, otherwise return null.
   *
   * @return the list of Via header objects if any present in this message, otherwise return null.
   */
  public DsSipHeaderList getViaHeaders() {
    return getHeaders(VIA);
  }

  /**
   * Method to get the unique method ID for the method in the CSeq header.
   *
   * @return the method ID
   */
  public int getCSeqType() {
    // Implement utilizing the general header getter
    try {
      DsSipCSeqHeader cseqHeader = (DsSipCSeqHeader) getHeaderValidate(CSEQ);
      if (cseqHeader == null) return -1;
      return cseqHeader.getMethodID();
    } catch (DsSipParserException pe) {
      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getCSeqType(): Exception getHeaderValidate(CSEQ)", pe);

    } catch (DsSipParserListenerException ple) {

      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getCSeqType(): Exception getHeaderValidate(CSEQ)", ple);
    }
    return -1;
  }

  /**
   * Returns the method name in the CSeq header in this message.
   *
   * @return the method name in the CSeq header in this message.
   */
  public DsByteString getCSeqMethod() {
    try {
      DsSipCSeqHeader cseqHeader = (DsSipCSeqHeader) getHeaderValidate(CSEQ);
      if (cseqHeader == null) return null;
      return cseqHeader.getMethod();
    } catch (DsSipParserException pe) {

      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getCSeqMethod(): Exception getHeaderValidate(CSEQ)", pe);

    } catch (DsSipParserListenerException ple) {
      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getCSeqMethod(): Exception getHeaderValidate(CSEQ)", ple);
    }
    return null;
  }

  /**
   * Returns the CSeq number in this message.
   *
   * @return the CSeq number in this message.
   */
  public long getCSeqNumber() {
    try {
      DsSipCSeqHeader cseqHeader = (DsSipCSeqHeader) getHeaderValidate(CSEQ);
      if (cseqHeader == null) return -1;
      return cseqHeader.getNumber();
    } catch (DsSipParserException pe) {

      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getCSeqNumber(): Exception getHeaderValidate(CSEQ)", pe);

    } catch (DsSipParserListenerException ple) {

      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getCSeqNumber(): Exception getHeaderValidate(CSEQ)", ple);
    }
    return -1;
  }

  /**
   * Returns the High version value of SIP for this message.
   *
   * @return the High version value of SIP for this message.
   */
  public int getVersionHigh() {
    return (versionHigh);
  }

  /**
   * Returns the Low version value of SIP for this message.
   *
   * @return the Low version value of SIP for this message.
   */
  public int getVersionLow() {
    return (versionLow);
  }

  /**
   * Sets the High version and Low version values of SIP for this message to the specified <code>
   * high</code> and <code>low</code> values, respectively. The version values should not be less
   * that 0, otherwise the version values will not be set.
   *
   * @param high the high version of SIP for this message
   * @param low the low version of SIP for this message
   */
  public void setVersion(int high, int low) {
    if (high < 0 || low < 0) {
      return;
    }
    versionHigh = (byte) high;
    versionLow = (byte) low;
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
  @Override
  public boolean equals(Object message) {
    if (null == message) return false;

    // check for SIP version.
    DsSipMessageBase other = (DsSipMessageBase) message;
    if (versionHigh != other.versionHigh || versionLow != other.versionLow) return false;

    return super.equals(other);
  }

  /*
   * javadoc inherited
   */
  public void bodyFoundInRequestURI(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {
    setBody(new DsByteString(buffer, offset, count), null);
  }

  ////////////////////////////////////////////////////////////////////////////////
  // DsSipMessageListener Interface implementation
  ////////////////////////////////////////////////////////////////////////////////
  /*
   * javadoc inherited.
   */
  public abstract DsSipElementListener requestURIBegin(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException;

  /*
   * javadoc inherited.
   */
  public abstract void requestURIFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException;

  /**
   * Gets the tag parameter from the To header.
   *
   * @return the tag parameter from the To header
   */
  public DsByteString getToTag() {
    try {
      return ((DsSipToHeader) getHeaderValidate(TO)).getTag();
    } catch (Exception e) {

      DsLog4j.messageCat.warn("DsSipMessageBase.getToTag(): Exception getHeaderValidate(TO)", e);

      // parse or null pointer exception or any exception returns null
      return null;
    }
  }

  /**
   * Gets the tag parameter from the From header.
   *
   * @return the tag parameter from the From header
   */
  public DsByteString getFromTag() {
    try {
      return ((DsSipFromHeader) getHeaderValidate(FROM)).getTag();
    } catch (Exception e) {

      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getFromTag(): Exception getHeaderValidate(FROM)", e);

      // parse or null pointer exception or any exception returns null
      return null;
    }
  }

  /**
   * Returns the value of CallID header, if present, in this message.
   *
   * @return the value of CallID header, if present, in this message, otherwise returns null.
   */
  public DsByteString getCallId() {
    try {
      DsSipCallIdHeader callidHeader = (DsSipCallIdHeader) getHeaderValidate(CALL_ID);
      if (callidHeader == null) return null;
      return callidHeader.getCallId();
    } catch (DsSipParserException pe) {

      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getCallId(): Exception getHeaderValidate(CSEQ)", pe);

    } catch (DsSipParserListenerException ple) {

      DsLog4j.messageCat.warn(
          "DsSipMessageBase.getCallId(): Exception getHeaderValidate(CSEQ)", ple);
    }
    return null;
  }

  // CSCsm39865: content length added to multipart mime sections for SDP
  // I moved this method here so that Content-Length does not appear in MIME parts
  /*
   * javadoc inherited.
   */
  protected void writeInBuiltHeaders(OutputStream out, int len) throws IOException {
    writeContentLength(out, len);
  }

  /**
   * Serializes the Content-Length header to the specified <code>out</code> output stream.
   *
   * @param out the output stream where the Content-Length header needs to be serialized.
   * @param len the value of the content length.
   * @throws IOException if there is an error while writing to the output stream.
   */
  public static void writeContentLength(OutputStream out, int len) throws IOException {
    if (out == null) return; // to prevent NullPointerException
    if (DsSipHeader.isCompact()) {
      BS_CONTENT_LENGTH_C_TOKEN.write(out);
      if (len == 0) {
        out.write('0');
      } else {
        out.write(DsIntStrCache.intToBytes(len));
      }
      BS_EOH.write(out);
    } else {
      DsMimeEntity.writeContentLength(out, len);
    }
  }

  public void protocolFound(
      byte[] buffer,
      int protocolOffset,
      int protocolCount,
      int majorOffset,
      int majorCount,
      int minorOffset,
      int minorCount,
      boolean valid)
      throws DsSipParserListenerException {
    if (!valid) {
      return;
    }

    if (buffer == null) return; // to prevent NullPointerException

    // defaults to 2, so we only need to parse and set if it is different
    if (majorCount > 1
        || (majorOffset > 0 && majorOffset < buffer.length && buffer[majorOffset] != '2')) {
      versionHigh = (byte) DsSipMsgParser.parseInt(buffer, majorOffset, majorCount);
    }

    // defaults to 0, so we only need to parse and set if it is different
    if (minorCount > 1
        || (minorOffset > 0 && minorOffset < buffer.length && buffer[minorOffset] != '0')) {
      versionLow = (byte) DsSipMsgParser.parseInt(buffer, majorOffset, majorCount);
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  // Data
  ////////////////////////////////////////////////////////////////////////////////

  /** Represents the version high value. */
  byte versionHigh = 2;
  /** Represents the version low value. */
  byte versionLow = 0;

  /**
   * Tells the default priority level of the headers. This specifies the max length of the array of
   * directly accessible headers. This can be specified through the Java property
   * "com.dynamicsoft.DsLibs.DsSipObject.HeaderPriLevel".
   */
  protected static int HEADER_PRIORITY_LEVEL = CONTENT_LENGTH;

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // All the code below this line is unique to the Caffeine Stack.
  //
  // This code can be either merged in logically with the code above, or grouped below as a whole.
  // In either case, the purpose of the code should be documented and noted as appropriate
  //

  ////////////////////////////////////////////////////////////////////////////////
  // Constructors
  ////////////////////////////////////////////////////////////////////////////////

  /** Default constructor. */
  protected DsSipMessageBase() {
    super(HEADER_PRIORITY_LEVEL - 1);
  }

  //////////////////////////////////////////////////////////////////////////////////
  //  The following set of methods are convenience functions that can be used
  //  in place of the more general getHeader() and getHeaders() method.
  //////////////////////////////////////////////////////////////////////////////////

  // Put the common logic here, to be invoked by DsSipRequest and DsSipFragment
  protected static void finishURI(
      DsURI dsURI, byte[] buffer, int offset, int count, boolean valid) {
    // This request-uri may be invalid, so what we are going to do ?
    if (DsSipMessage.DEBUG)
      System.out.println(
          "requestURIFound = [" + DsByteString.newString(buffer, offset, count) + "]");
    if (valid == false) {
      return;
    }
    if (null != dsURI) {
      int colonIndex = offset;

      while (--count > 0 && buffer[colonIndex++] != ':') {}
      dsURI.setValue(new DsByteString(buffer, colonIndex, count));
    }
  }

  /*
   * Common logic shared by DsSipFragment and DsSipRequest
   */
  protected static DsURI initURI(byte[] buffer, int off, int count) {
    DsURI dsURI = null;
    if (count == 3) {
      int i = off;
      if ((buffer[i] == (byte) 's' || buffer[i] == (byte) 'S')
          && (buffer[i + 1] == (byte) 'i' || buffer[i + 1] == (byte) 'I')
          && (buffer[i + 2] == (byte) 'p' || buffer[i + 2] == (byte) 'P')) {
        dsURI = new DsSipURL();
      } else if ((buffer[i] == (byte) 't' || buffer[i] == (byte) 'T')
          && (buffer[i + 1] == (byte) 'e' || buffer[i + 1] == (byte) 'E')
          && (buffer[i + 2] == (byte) 'l' || buffer[i + 2] == (byte) 'L')) {
        dsURI = new DsTelURL();
      } else {
        dsURI = new DsURI();
        dsURI.setName(new DsByteString(buffer, off, count));
      }
    } else if (count == 4) {
      int i = off;
      if ((buffer[i] == (byte) 's' || buffer[i] == (byte) 'S')
          && (buffer[i + 1] == (byte) 'i' || buffer[i + 1] == (byte) 'I')
          && (buffer[i + 2] == (byte) 'p' || buffer[i + 2] == (byte) 'P')
          && (buffer[i + 3] == (byte) 's' || buffer[i + 3] == (byte) 'S')) {
        dsURI = new DsSipURL(true);
      } else {
        dsURI = new DsURI();
        dsURI.setName(new DsByteString(buffer, off, count));
      }
    } else {
      dsURI = new DsURI();
      dsURI.setName(new DsByteString(buffer, off, count));
    }

    return dsURI;
  }
} // Ends class DsSipMessageBase
