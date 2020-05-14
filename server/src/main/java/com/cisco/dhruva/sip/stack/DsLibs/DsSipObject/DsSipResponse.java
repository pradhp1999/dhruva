// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.cac.SIPSession;
import com.cisco.dhruva.sip.cac.SIPSessions;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipDictionary;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.TokenSip.DsTokenSipInteger;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsIntStrCache;
import java.io.IOException;
import java.io.OutputStream;

/** This class represents the response message as specified in RFC 3261. */
public class DsSipResponse extends DsSipMessage {
  /**
   * Constant representing the integer ID for the Response. It is the same value as defined in
   * DsSipConstants.
   *
   * @deprecated use {@link DsSipConstants#RESPONSE DsSipConstants.RESPONSE}
   */
  public static int sID = RESPONSE;

  /** The status code for this response. */
  private int m_StatusCode; // = 0;
  /** The reason phrase for this response. */
  private DsByteString m_strPhrase; // = null;

  public static boolean receiveAlways = false;

  static {
    receiveAlways =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_ALLOW_RECEIVED_PARAM,
            DsConfigManager.PROP_ALLOW_RECEIVED_PARAM_DEFAULT);
  }
  /** Default constructor. */
  public DsSipResponse() {
    super();
  }

  /**
   * Constructs this response object with the specified response status <code>code</code> and the
   * specified reason phrase <code>buffer</code>.
   *
   * @param buffer the byte array containing the reason phrase for this response.
   * @param code the response's status code.
   * @param offset the offset in the <code>buffer</code> byte array where from the response reason
   *     phrase starts.
   * @param count the number of bytes in the <code>buffer</code> byte array that comprise the
   *     response reason phrase.
   */
  public DsSipResponse(byte[] buffer, int code, int offset, int count) {
    super();
    m_StatusCode = code;
    m_strPhrase = DsByteString.newInstance(buffer, offset, count);
  }

  protected DsSipResponse(byte[] buffer, int code, int offset, int count, boolean encoded) {
    super(encoded);
    m_StatusCode = code;
    m_strPhrase = DsByteString.newInstance(buffer, offset, count);
  }

  /**
   * Constructs this response object with the specified response status <code>code</code>, the SIP
   * request <code>sipRequest</code>, the response <code>body</code> and the response body's content
   * type <code>contentType</code>.
   *
   * @param code the response's status code (1xx, 2xx, 3xx, 4xx, 5xx, 6xx).
   * @param sipRequest the incoming SIP request for which the response needs to be generated.
   * @param body the body that should be part of the constructed Response
   * @param contentType the content type for the specified body.
   */
  public DsSipResponse(int code, DsSipRequest sipRequest, byte[] body, DsByteString contentType) {
    this(code, sipRequest, body, contentType, true);
  }

  /**
   * Constructs this response object with the specified response status <code>code</code>, the SIP
   * request <code>sipRequest</code>, the response <code>body</code> and the response body's content
   * type <code>contentType</code>.
   *
   * @param code the response's status code (1xx, 2xx, 3xx, 4xx, 5xx, 6xx).
   * @param sipRequest the incoming SIP request for which the response needs to be generated.
   * @param body the body that should be part of the constructed Response
   * @param contentType the content type for the specified body.
   * @param cloneHeaders tells whether the required headers in the request should be cloned first
   *     and then inserted in this response or no cloning should be done.
   */
  public DsSipResponse(
      int code,
      DsSipRequest sipRequest,
      byte[] body,
      DsByteString contentType,
      boolean cloneHeaders) {
    this(code, sipRequest, body, contentType, cloneHeaders, false);
  }

  /**
   * Constructs this response object with the specified response status <code>code</code>, the SIP
   * request <code>sipRequest</code>, the response <code>body</code> and the response body's content
   * type <code>contentType</code>.
   *
   * @param code the response's status code (1xx, 2xx, 3xx, 4xx, 5xx, 6xx).
   * @param sipRequest the incoming SIP request for which the response needs to be generated.
   * @param body the body that should be part of the constructed Response
   * @param contentType the content type for the specified body.
   * @param cloneHeaders tells whether the required headers in the request should be cloned first
   *     and then inserted in this response or no cloning should be done.
   * @param copyRecordRoute copy the Record-Route headers from the request to the response. If set
   *     to <code>true</code>, the Record-Route headers will be copied from the request to the
   *     response. If set to <code>false</code>, the Record-Route headers will be copied if and only
   *     if there is no To tag and the response is a 2xx response.
   */
  public DsSipResponse(
      int code,
      DsSipRequest sipRequest,
      byte[] body,
      DsByteString contentType,
      boolean cloneHeaders,
      boolean copyRecordRoute) {
    super();
    m_StatusCode = code;
    m_strPhrase = DsSipResponseCode.getBSReasonPhrase(code);
    DsBindingInfo bi = sipRequest.getBindingInfo();
    m_bindingInfo.setRemoteAddress(bi.getRemoteAddress());
    m_bindingInfo.setRemotePort(bi.getRemotePort());
    m_bindingInfo.setTransport(bi.getTransport());
    m_bindingInfo.setNetwork(bi.getNetwork());

    DsSipHeaderList vias = sipRequest.getHeaders(VIA);
    if (vias != null && !vias.isEmpty()) {
      updateHeaders(vias, true);
    }
    DsSipHeaderInterface link = (DsSipHeaderInterface) sipRequest.getFromHeader();
    if (link != null) {
      addHeader(link, false, cloneHeaders);
    }

    link = (DsSipHeaderInterface) sipRequest.getToHeader();
    if (link != null) {
      addHeader(link, false, cloneHeaders);
    }

    // Call-ID Header
    setCallId(sipRequest.getCallId());

    // CSeq header
    m_lCSeq = sipRequest.getCSeqNumber();
    setCSeqMethod(sipRequest.getCSeqMethod());

    // addSession-ID header
    addSessionIDHeader();

    if (code == 100) {
      // copy the timestamp header into 100 responses, as per bis-09
      DsSipHeaderInterface ts = (DsSipHeaderInterface) sipRequest.getHeader(TIMESTAMP);
      if (ts != null) {
        addHeader(ts, false, false);
      }
    }

    // Do not copy Record-Route headers if the to tag exists
    // this means that it is already part of a dialog and the route set cannot change
    // so it is wasteful to copy.

    // I do not see any reference to the use of Record-Route in the 484, 401 and 484 responses
    //   in 3261  -dg
    // if (copyRecordRoute || (sipRequest.getToTag() == null && ((code / 100) == 2 || code == 401 ||
    // code == 407 || code == 484)))

    if (copyRecordRoute || (sipRequest.getToTag() == null && (code / 100) == 2)) {
      DsSipHeaderList routes = sipRequest.getHeaders(RECORD_ROUTE);
      if (routes != null && !routes.isEmpty()) {
        updateHeaders(routes, true);
      }
    }

    // CAFFEINE 2.0 DEVELOPMENT -  CSCeg69327 GUID support in stack.
    // add Cisco-Guid header if needed
    if (DsConfigManager.isUseCiscoGuidHeader()) {
      DsSipHeaderInterface guidHdr = sipRequest.getHeaders(CISCO_GUID);
      if (guidHdr != null) {
        updateHeader(guidHdr, true);
      }
    }

    setBody(body, contentType);
  }

  /**
   * Used to generate a response, but as serialized bytes instead of as an object. This is useful if
   * all you are doing with this response is sending it on the wire. By using the method you get the
   * same results as the constructor with the same arguments except that the received parameter is
   * removed from the topmost via header.
   *
   * @param code the response's status code (1xx, 2xx, 3xx, 4xx, 5xx, 6xx).
   * @param sipRequest the incoming SIP request for which the response needs to be generated.
   * @param body the body that should be part of the constructed Response
   * @param contentType the content type for the specified body.
   * @return a byte array representing the created response, precisely sized.
   */
  public static byte[] createResponseBytes(
      int code, DsSipRequest sipRequest, byte[] body, DsByteString contentType) {
    return createResponseByteBuffer(code, sipRequest, body, contentType);
  }

  /**
   * Used to generate a response, but as serialized bytes instead of as an object. This is useful if
   * all you are doing with this response is sending it on the wire. By using the method you get the
   * same results as the constructor with the same arguments except that the received parameter is
   * removed from the topmost via header.
   *
   * @param code the response's status code (1xx, 2xx, 3xx, 4xx, 5xx, 6xx).
   * @param sipRequest the incoming SIP request for which the response needs to be generated.
   * @param body the body that should be part of the constructed Response
   * @param contentType the content type for the specified body.
   * @return a ThreadLocal ByteBuffer representing the created response - MUST NOT be passed to
   *     another thread.
   */
  public static byte[] createResponseByteBuffer(
      int code, DsSipRequest sipRequest, byte[] body, DsByteString contentType) {
    byte[] buffer2Array = null;

    try (ByteBuffer buffer = ByteBuffer.newInstance(2048)) {

      try {
        // write start line
        // optimize for 100 response
        if (code == 100) {
          BS_100_TRYING.write(buffer);
        } else {
          DsByteString reasonPhrase = DsSipResponseCode.getBSReasonPhrase(code);
          // Write "SIP/2.0 "
          BS_SIP_VERSION.write(buffer);
          buffer.write(B_SPACE);
          buffer.write(DsIntStrCache.intToBytes(code));
          buffer.write(B_SPACE);
          reasonPhrase.write(buffer);
          BS_EOH.write(buffer); // Write "\r\n"
        }

        // we must remove the received parameter from the via before sending
        // don't forget to put it back, since this is the real msg
        DsSipViaHeader via = null;
        try {
          // Get the top via header, and make sure its parsed
          via = (DsSipViaHeader) sipRequest.getHeaderValidate(VIA, true);
          DsSipHeaderInterface link = null;
          if (via != null) {
            if (!receiveAlways) {
              if (Log.isDebugEnabled())
                Log.debug("In createResponseBytes, removing the received= tag from the via");
              DsByteString received = via.getReceived();
              via.removeReceived();
              via.write(buffer);
              if (null != received) {
                via.setReceived(received);
              }
            } else {
              via.write(buffer);
            }
            link = (DsSipHeaderInterface) via.getNext();
          }
          while (link != null) {
            link.write(buffer);
            link = (DsSipHeaderInterface) link.getNext();
          }

        } catch (DsSipParserException pe) {
          // log?
        } catch (DsSipParserListenerException ple) {
          // log?
        }
        // Write To header
        DsSipHeaderInterface header = (DsSipHeaderInterface) sipRequest.getHeader(TO, true);
        if (null != header) header.write(buffer);

        // Write From header
        header = (DsSipHeaderInterface) sipRequest.getHeader(FROM, true);
        if (null != header) header.write(buffer);

        // Write Call-ID header
        sipRequest.writeCallId(buffer);
        // Write CSeq header
        sipRequest.writeCSeq(buffer);

        // check need to check for session id header before adding one
        // Add Session-ID header
        writeSessionId(buffer, sipRequest);

        // CAFFEINE 2.0 DEVELOPMENT -  CSCeg69327 GUID support in stack.
        // add Cisco-Guid header if needed
        if (DsConfigManager.isUseCiscoGuidHeader()) {
          DsSipHeaderInterface guidHdr = sipRequest.getHeaders(CISCO_GUID);
          if (guidHdr != null) guidHdr.write(buffer);
        }

        if (code == 100) {
          // copy the timestamp header into 100 responses, as per bis-09
          DsSipHeaderInterface ts = (DsSipHeaderInterface) sipRequest.getHeader(TIMESTAMP);
          if (ts != null) {
            ts.write(buffer);
          }
        }

        if ((code / 100) == 2 || code == 401 || code == 407 || code == 484) {
          // write the record route headers
          header = (DsSipHeaderInterface) sipRequest.getHeader(RECORD_ROUTE, true);
          while (header != null) {
            header.write(buffer);
            header = (DsSipHeaderInterface) header.getNext();
          }
        }
        // Check and write body type
        if (contentType != null && contentType.length() > 0) // body type exists
        {
          BS_CONTENT_TYPE_TOKEN.write(buffer);
          contentType.write(buffer);
          BS_EOH.write(buffer);
        }
        writeContentLength(buffer, ((null == body) ? 0 : body.length));

        // separatory of headers and body
        BS_EOH.write(buffer);
        if (body != null) {
          buffer.write(body);
        }
      } catch (IOException e) {
        // retval will be null
      }

      buffer2Array = buffer.toByteArray();
    } catch (IOException ie) {

    }
    return buffer2Array;
  }

  private static void writeSessionId(ByteBuffer buffer, DsSipRequest sipRequest) {
    try {
      SIPSession session = SIPSessions.getActiveSession(sipRequest.getCallId().toString());
      if (session != null) {
        DsByteString sessionIdValue;
        if (session.sessionAttrib.isUacStandardSessionIDImplementation()) {
          sessionIdValue =
              new DsByteString(
                  session.sessionAttrib.getRemoteUuid()
                      + ";remote="
                      + session.sessionAttrib.getLocalUuid());
        } else {

          sessionIdValue = new DsByteString(session.sessionAttrib.getLocalUuid());
        }
        DsSipHeaderString sessionIDHeader =
            new DsSipHeaderString(
                DsSipMsgParser.getHeader(DsSipConstants.BS_SESSION_ID),
                DsSipConstants.BS_SESSION_ID,
                sessionIdValue);
        sessionIDHeader.write(buffer);
        Log.debug("Added SessionID header inside createResponseByteBuffer");
      }

    } catch (Exception e) {
      Log.error("Exception in writeSessionId", e);
    }
  }

  /**
   * Retrieves the Authentication header or the ProxyAuthentication header.
   *
   * @return the Authentication header or the ProxyAuthentication header.
   */
  public DsSipHeader getAuthenticationHeader() {
    DsSipHeader aHeader = null;
    try {
      aHeader = getHeaderValidate(DsSipConstants.WWW_AUTHENTICATE);
      if (null == aHeader) {
        aHeader = getHeaderValidate(DsSipConstants.PROXY_AUTHENTICATE);
      }
    } catch (DsSipParserException pe) {
      // pe.printStackTrace();
      // log?
    } catch (DsSipParserListenerException ple) {
      // ple.printStackTrace();
      // log?
    }
    return aHeader;
  }

  /**
   * Retrieves the response class code.
   *
   * @return the response class code. For a 180 response, getResponseClass() returns 1. For a 200
   *     response, it returns 2, etc.
   */
  public final int getResponseClass() {
    return (m_StatusCode / 100);
  }

  /**
   * Retrieves the status code.
   *
   * @return the status code.
   */
  public final int getStatusCode() {
    return m_StatusCode;
  }

  /**
   * Retrieves the reason phrase.
   *
   * @return the reason phrase.
   */
  public DsByteString getReasonPhrase() {
    return m_strPhrase;
  }

  /**
   * Sets the status code.
   *
   * @param aStatusCode the status code.
   */
  public void setStatusCode(int aStatusCode) {
    m_StatusCode = aStatusCode;
  }

  /**
   * Sets the reason phrase.
   *
   * @param pReasonPhrase the reason phrase.
   */
  public void setReasonPhrase(DsByteString pReasonPhrase) {
    m_strPhrase = pReasonPhrase;
  }

  /**
   * Checks if the message is a request or not.
   *
   * @return <b>false</b>.
   */
  public final boolean isRequest() {
    return false;
  }

  /**
   * Serializes the response line.
   *
   * @param out the output stream.
   * @throws IOException thrown when there is an I/O error.
   */
  public void writeStartLine(OutputStream out) throws IOException {
    // optimized for most used case
    if (versionHigh == 2 && versionLow == 0) {
      BS_SIP_VERSION.write(out);
    } else {
      BS_SIP.write(out);
      out.write(B_SLASH);
      out.write(DsIntStrCache.intToBytes(getVersionHigh()));
      out.write(B_PERIOD);
      out.write(DsIntStrCache.intToBytes(getVersionLow()));
    }
    out.write(B_SPACE);
    out.write(DsIntStrCache.intToBytes(m_StatusCode));
    out.write(B_SPACE);
    if (m_strPhrase != null) {
      m_strPhrase.write(out);
    }
    BS_EOH.write(out);
  }

  /**
   * Serializes this request's start line to the specified <code>out</code> output stream.
   *
   * @param out the output stream where this request's start line needs to be serialized.
   * @throws IOException thrown when there is an I/O error
   */
  public void writeEncodedStartLine(OutputStream out) throws IOException {
    // write startline prefix
    out.write(DsTokenSipConstants.TOKEN_SIP_FIXED_FORMAT_RESPONSE_START_LINE);

    // write status code
    DsTokenSipInteger.write16Bit(out, getStatusCode());

    // write reason phrase
    (getEncodedMessageDictionary().getEncoding(getReasonPhrase())).write(out);
  }

  /**
   * Checks for the equality of the start line semantics of this response object against the start
   * line of the specified response <code>message</code>.
   *
   * @param message the message whose start line semantics needs to be compared for equality against
   *     the start line semantics of this response object.
   * @return <code>true</code> if the start line of this response is semantically equal to the start
   *     line of the specified response <code>message</code>, <code>false</code> otherwise.
   */
  // CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to DsSipMessageBase
  public boolean equalsStartLine(DsSipMessageBase message) {
    DsSipResponse response = null;
    try {
      response = (DsSipResponse) message;
    } catch (ClassCastException e) {
      return false;
    }

    // check for status code.
    if (m_StatusCode != response.m_StatusCode) return false;
    // check for reason phrase.
    if (!DsByteString.equals(m_strPhrase, response.m_strPhrase)) return false;

    return true;
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
  public final boolean shouldCompress() {
    if (isCompressed()) return false;
    boolean shouldCompress = compress();

    if (!shouldCompress) {
      DsByteString compval = null;
      try {
        DsSipViaHeader topvia = getViaHeaderValidate();
        compval = topvia.getComp();
      } catch (Exception exc) {
      }
      if (compval != null) {
        if (DsSipConstants.BS_SIGCOMP.equals(compval)) {
          shouldCompress = true;
        }
      }
    }
    return shouldCompress;
  }

  public final DsTokenSipDictionary shouldEncode() {

    return null;
  }
  /**
   * Returns the method ID as per the method in the CSeq header.
   *
   * @return the method ID.
   */
  public final int getMethodID() {
    return getCSeqType();
  }

  /*
   * javadoc inherited.
   */
  public DsSipElementListener requestURIBegin(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {
    return null;
  }

  /*
   * javadoc inherited.
   */
  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {}
} // Ends class DsSipResponse
