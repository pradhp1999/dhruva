// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipElementListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipHeaderListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMessageListener;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMessageListenerFactory;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Validates SIP messages as they are being parsed. This class should be used as a ThreadLocal. */
public final class DsSipMessageValidator
    implements DsSipConstants,
        DsSipMessageListener,
        DsSipMessageListenerFactory,
        DsSipHeaderListener,
        DsSipElementListener {
  private static final long MASK = 0xFFFFFFFF00000000L;

  byte m_viaCount;
  byte m_toCount;
  byte m_fromCount;
  byte m_callIdCount;
  byte m_cSeqCount;
  DsByteString m_cseqMethod;
  long m_cseqNum;
  DsByteString m_startLineMethod;
  boolean m_body;
  boolean m_bodyValid;
  boolean m_request;
  boolean m_contentType;
  boolean m_contentLengthPresent;
  int m_contentLength = -1;
  int m_actualLength;
  boolean m_majorVersionValid;
  boolean m_minorVersionValid;
  boolean m_requestUriValid;
  boolean m_protocolValid;
  boolean m_subscriptionState;
  int m_eventCount;

  // This class should be used as ThreadLocal
  ByteArrayOutputStream m_reasonStream = new ByteArrayOutputStream(150);

  private static boolean m_validateExtensions =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_VALIDATE_EXTENSIONS,
          DsConfigManager.PROP_VALIDATE_EXTENSIONS_DEFAULT);

  /** Reinitilize this object. */
  public void reInit() {
    m_contentLengthPresent = false;
    m_contentLength = -1;
    m_actualLength = 0;
    m_reasonStream.reset();
    m_viaCount = 0;
    m_toCount = 0;
    m_fromCount = 0;
    m_callIdCount = 0;
    m_cSeqCount = 0;
    m_cseqNum = 0;
    m_cseqMethod = null;
    m_startLineMethod = null;
    m_body = false;
    m_bodyValid = false;
    m_request = false;
    m_contentType = false;
    m_majorVersionValid = false;
    m_minorVersionValid = false;
    m_requestUriValid = false;
    m_protocolValid = false;

    m_subscriptionState = false;
    m_eventCount = 0;
  }

  /*
   * javadoc inherited
   */
  public void headerFound(int headerId, byte[] buffer, int offset, int count, boolean isValid) {
    // Here we don't use the isValid information, since we didn't
    // ask for a deep parse ourselves

    // optimize for the proper switch statement op code. Content-Type required entering too many
    // headers,
    // so I just put a check at the end.  -jsm 06/17/2002
    switch (headerId) {
      case VIA:
        ++m_viaCount;
        return;
      case MAX_FORWARDS:
      case ROUTE:
      case RECORD_ROUTE:
        // skip these headers
        // here for the switch statement op code only
        return;
      case TO:
        ++m_toCount;
        return;
      case FROM:
        ++m_fromCount;
        return;
      case CSEQ:
        ++m_cSeqCount;
        return;
      case CALL_ID:
        ++m_callIdCount;
        return;
    }

    if (headerId == CONTENT_TYPE) {
      m_contentType = true;
    }

    if (headerId == SUBSCRIPTION_STATE) {
      m_subscriptionState = true;
    }

    if (headerId == EVENT) {
      m_eventCount++;
    }
  }

  /*
   * javadoc inherited
   */
  public void messageFound(byte[] buffer, int offset, int count, boolean isValid)
      throws DsSipParserListenerException {
    // Don't even bother: there is a parser exn and
    // it will be thrown
    if (!isValid) {
      reInit();
    } else {
      if (m_contentLengthPresent) {
        m_body = ((count > 0) && (m_contentLength > 0));
      } else {
        m_body = (count > 0);
      }

      m_actualLength = count;

      // extra bytes are OK, we just ignore them
      // too few bytes is a problem.
      m_bodyValid = m_contentLengthPresent ? m_actualLength >= m_contentLength : true;

      validate(); // this throws the DsSipParserListenerException
      // and resets
    }
  }

  /*
   * javadoc inherited
   */
  public void bodyFoundInRequestURI(byte[] buffer, int offset, int count)
      throws DsSipParserListenerException {}

  /*
   * javadoc inherited
   */
  public DsSipMessageListener requestBegin(
      byte[] buffer, int methodOffset, int methodCount, boolean isEncoded) {
    reInit();
    m_request = true;
    m_startLineMethod = DsByteString.newInstance(buffer, methodOffset, methodCount);

    // The Subscription-State header is only mandatory in NOTIFY requests
    if (!m_startLineMethod.equals(BS_NOTIFY)) {
      // For any other request, this test passes.
      m_subscriptionState = true;
    }
    return this;
  }

  /*
   * javadoc inherited
   */
  public DsSipMessageListener responseBegin(
      byte[] buffer, int code, int reasonOffset, int reasonCount, boolean isEncoded) {
    reInit();
    m_request = false;
    m_startLineMethod = DsByteString.BS_EMPTY_STRING;

    // Only mandatory in NOTIFY requests, not responses
    m_subscriptionState = true;

    return this;
  }

  /*
   * javadoc inherited
   */
  public DsSipElementListener headerBegin(int headerId) {
    if (headerId == CSEQ || headerId == CONTENT_LENGTH) {
      return this;
    }
    return null;
  }

  /*
   * javadoc inherited
   */
  public void elementFound(
      int contextId, int elementId, byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    if (contextId == CSEQ) {
      if (elementId == CSEQ_METHOD) {
        m_cseqMethod = DsByteString.newInstance(buffer, offset, count);
      } else if (elementId == CSEQ_NUMBER || elementId == SINGLE_VALUE) {
        m_cseqNum = DsSipMsgParser.parseLong(buffer, offset, count);
      }
    } else if (contextId == CONTENT_LENGTH && elementId == SINGLE_VALUE) {
      try {
        m_contentLength = DsSipMsgParser.parseInt(buffer, offset, count);
        m_contentLengthPresent = true;
      } catch (Exception exc) {
        m_contentLengthPresent = false;
      }
    }
  }

  /*
   * javadoc inherited
   */
  public void unknownFound(
      byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount, boolean valid)
      throws DsSipParserListenerException {
    // We do not we care about unknown headers in the validator. - jsm
  }

  void validate() throws DsSipMessageValidationException {
    boolean valid = true;
    boolean versionValid = true;

    try {
      // At least 1 Via header must be present
      if (m_viaCount < 1) {
        valid = false;
        DsSipInvalidReasons.BS_MISSING_VIA_HEADER.write(m_reasonStream);
        BS_EOH.write(m_reasonStream);
      }

      // One and only 1 To header should present
      if (m_toCount != 1) {
        if (m_toCount < 1) {
          valid = false;
          DsSipInvalidReasons.BS_MISSING_TO_HEADER.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        } else {
          valid = false;
          DsSipInvalidReasons.BS_MULTIPLE_TO_HEADERS.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
      }

      // One and only 1 From header should present
      if (m_fromCount != 1) {
        if (m_fromCount < 1) {
          valid = false;
          DsSipInvalidReasons.BS_MISSING_FROM_HEADER.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        } else {
          valid = false;
          DsSipInvalidReasons.BS_MULTIPLE_FROM_HEADERS.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
      }

      // One and only 1 CallId header should present
      if (m_callIdCount != 1) {
        if (m_callIdCount < 1) {
          valid = false;
          DsSipInvalidReasons.BS_MISSING_CALL_ID_HEADER.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        } else {
          valid = false;
          DsSipInvalidReasons.BS_MULTIPLE_CALL_ID_HEADERS.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
      }

      // One and only 1 CSeq header should present
      if (m_cSeqCount != 1) {
        if (m_cSeqCount < 1) {
          valid = false;
          DsSipInvalidReasons.BS_MISSING_CSEQ_HEADER.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        } else {
          valid = false;
          DsSipInvalidReasons.BS_MULTIPLE_CSEQ_HEADERS.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
      }

      if (m_request) {
        if (m_startLineMethod != null) {
          if (m_startLineMethod.length() == 0) {
            // no method in request!
            valid = false;
            DsSipInvalidReasons.BS_GENERIC.write(m_reasonStream);
            BS_EOH.write(m_reasonStream);
          }
        } else // start line method is somehow null!
        {
          valid = false;
          DsSipInvalidReasons.BS_GENERIC.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
      }
      if (m_cseqMethod == null) {
        valid = false;
        DsSipInvalidReasons.BS_MISSING_CSEQ_METHOD.write(m_reasonStream);
        BS_EOH.write(m_reasonStream);
      } else {
        if (m_cseqMethod.length() == 0) {
          valid = false;
          DsSipInvalidReasons.BS_MISSING_CSEQ_METHOD.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        } else {
          if (m_request) {
            if (m_startLineMethod != null) {
              // cseq method must match method start line method
              if ((m_cSeqCount == 1) && !m_startLineMethod.equals(m_cseqMethod)) {
                valid = false;
                DsSipInvalidReasons.BS_CSEQ_MISMATCH.write(m_reasonStream);
                BS_EOH.write(m_reasonStream);
              }
            }
          }
        }
        if ((m_cseqNum & MASK) != 0) {
          valid = false;
          DsSipInvalidReasons.BS_INVALID_CSEQ_NUMBER.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
      }

      // if there is a body and there is no content-type header, this is invalid
      // check for the body with the content length header
      if (m_body) {
        if (!m_contentType) {
          valid = false;
          DsSipInvalidReasons.BS_MISSING_CONTENT_TYPE.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
        if (!m_bodyValid) {
          valid = false;
          DsSipInvalidReasons.BS_BODY_INVALID.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
      }

      if (m_contentLengthPresent && m_contentLength < 0) {
        // Negative Content-Length
        valid = false;
        DsSipInvalidReasons.BS_INVALID_CONTENT_LENGTH.write(m_reasonStream);
        BS_EOH.write(m_reasonStream);
      }

      // if this is a request, make sure that the Request-URI is well formed
      if (m_request && !m_requestUriValid) {
        valid = false;
        DsSipInvalidReasons.BS_MALFORMED_REQUEST_URI.write(m_reasonStream);
        BS_EOH.write(m_reasonStream);
      }

      // make sure that the major verion is 2, or else
      // we need to reject the request
      // or this response is bogus
      if (!m_majorVersionValid) {
        versionValid = false;
        DsSipInvalidReasons.BS_INVALID_MAJOR_VERSION.write(m_reasonStream);
        BS_EOH.write(m_reasonStream);
      }

      // Also check the minor version.  Accept only "2.0"
      if (!m_minorVersionValid) {
        versionValid = false;
        DsSipInvalidReasons.BS_INVALID_MINOR_VERSION.write(m_reasonStream);
        BS_EOH.write(m_reasonStream);
      }

      // make sure that the protocol is SIP
      if (!m_protocolValid) {
        valid = false;
        DsSipInvalidReasons.BS_PROTOCOL_NOT_SIP.write(m_reasonStream);
        BS_EOH.write(m_reasonStream);
      }

      if (m_validateExtensions) {
        // make sure that there is a Susbcription-State header in NOTIFY requests
        if (!m_subscriptionState) {
          valid = false;
          DsSipInvalidReasons.BS_NOTIFY_MISSING_SUB_STATE.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }

        // NOTIFY and SUBSCRIBE messages must contain exactly 1 Event header
        if (m_eventCount != 1
            && (m_startLineMethod.equals(BS_SUBSCRIBE) || m_startLineMethod.equals(BS_NOTIFY))) {
          valid = false;
          DsSipInvalidReasons.BS_NOTIFY_SUBSCRIBE_EVENT.write(m_reasonStream);
          BS_EOH.write(m_reasonStream);
        }
      }
    } catch (IOException exc) {
      // there should never be one with a ByteArrayOutputStream
    }

    if (!versionValid) {
      String invalidStr = m_reasonStream.toString();
      reInit();
      throw new DsSipVersionValidationException(invalidStr);
    }

    if (!valid) {
      String invalidStr = m_reasonStream.toString();
      reInit();
      throw new DsSipMessageValidationException(invalidStr);
    }

    reInit();
  }

  /**
   * Tells whether the messages specific to SIP extensions (non RFC 3261 specific) should be
   * validated.
   *
   * <p>For Example, <br>
   * Whether the SUBSCRIBE and NOTIFY requests should be checked for the presence of one and only
   * one Event Header. If this flag is set to true and the SUBSCRIBE/NOTIFY request doesn't contain
   * exactly one Event Header, then "400 Bad Request" response would be sent by the stack
   * automatically. Same is true if there is no "Subscription-State" Header present in the Notify
   * Request. This flag is set by default and can be unset either by specifying the Java System
   * Property "com.dynamicsoft.DsLibs.DsSipObject.validateExtensions" to <code>false</code> or
   * through {@link #setValidateExtensions(boolean) setValidateExtensions(false)}.
   *
   * @return <code>true</code> if the SUBSCRIBE/NOTIFY requests should be checked for the presence
   *     of exactly one Event Header and the NOTIFY requests for the presence of
   *     "Subscription-State" Header, <code>false</code> otherwise.
   */
  public static boolean isValidateExtensions() {
    return m_validateExtensions;
  }

  /**
   * Sets the flag that tells whether the messages specific to SIP extensions (non RFC 3261
   * specific) should be validated.
   *
   * <p>For Example:<br>
   * Whether the SUBSCRIBE and NOTIFY requests should be checked for the presence of one and only
   * one Event Header. If this flag is set to true and the SUBSCRIBE/NOTIFY request doesn't contain
   * exactly one Event Header, then "400 Bad Request" response would be sent by the stack
   * automatically. Same is true if there is no "Subscription-State" Header present in the Notify
   * Request.
   *
   * <p>This flag is set by default.
   *
   * @param check if <code>true</code> then the SUBSCRIBE/NOTIFY requests should be checked for the
   *     presence of exactly one Event Header and the NOTIFY requests for the presence of
   *     "Subscription-State" Header, otherwise no such check should be done.
   */
  public static void setValidateExtensions(boolean check) {
    m_validateExtensions = check;
  }

  // unit test for validator
  //    public final static void main (String[] main)
  //    {
  //        DsSipMessageValidator validator = new DsSipMessageValidator();
  //
  //        final int MISMATCH = 0;
  //
  //
  //        byte[][] invalids = {
  //
  //            ("INVITE sip:UserB@there.com SIP/2.0\r\n" +
  //             "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
  //             "Via: SIP/2.0/UDP here.com:5060\r\n" +
  //             "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //             "From: BigGuy <sip:UserA@here.com>\r\n" +
  //             "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //             "Call-ID: 12345601@here.com\r\n" +
  //             "CSeq: 1 CANCEL\r\n" +
  //             "Contact: <sip:UserA@100.101.102.103>\r\n" +
  //             "Content-Type: application/sdp\r\n" +
  //             "Content-Length: 147\r\n" +
  //             "\r\n" +
  //             "v=0\r\n" +
  //             "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //             "s=Session SDP\r\n" +
  //             "c=IN IP4 100.101.102.103\r\n" +
  //             "t=0 0\r\n" +
  //             "m=audio 49172 RTP/AVP 0\r\n" +
  //             "a=rtpmap:0 PCMU/8000\r\n").getBytes(),
  //
  //            ("INVITE sip:UserB@there.com SIP/2.0\r\n" +
  //             "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
  //             "Via: SIP/2.0/UDP here.com:5060\r\n" +
  //             "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //             "From: BigGuy <sip:UserA@here.com>\r\n" +
  //             "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //             "Call-ID: 12345601@here.com\r\n" +
  //             "CSeq: 1\r\n" +
  //             "Contact: <sip:UserA@100.101.102.103>\r\n" +
  //             "Content-Type: application/sdp\r\n" +
  //             "Content-Length: 147\r\n" +
  //             "\r\n" +
  //             "v=0\r\n" +
  //             "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //             "s=Session SDP\r\n" +
  //             "c=IN IP4 100.101.102.103\r\n" +
  //             "t=0 0\r\n" +
  //             "m=audio 49172 RTP/AVP 0\r\n" +
  //             "a=rtpmap:0 PCMU/8000\r\n").getBytes(),
  //
  //            (" sip:UserB@there.com SIP/2.0\r\n" +
  //             "Via: SIP/2.0/UDP ss1.wcom.com:5060;branch=2d4790.1\r\n" +
  //             "Via: SIP/2.0/UDP here.com:5060\r\n" +
  //             "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //             "From: BigGuy <sip:UserA@here.com>\r\n" +
  //             "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //             "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //             "Call-ID: 12345601@here.com\r\n" +
  //             "CSeq: 1 FOO\r\n" +
  //             "Contact: <sip:UserA@100.101.102.103>\r\n" +
  //             "Content-Type: application/sdp\r\n" +
  //             "Content-Length: 147\r\n" +
  //             "\r\n" +
  //             "v=0\r\n" +
  //             "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //             "s=Session SDP\r\n" +
  //             "c=IN IP4 100.101.102.103\r\n" +
  //             "t=0 0\r\n" +
  //             "m=audio 49172 RTP/AVP 0\r\n" +
  //             "a=rtpmap:0 PCMU/8000\r\n").getBytes(),
  //
  //            ("BAR sip:UserB@there.com SIP/2.0\r\n" +
  //             "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //             "From: BigGuy <sip:UserA@here.com>\r\n" +
  //             "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //             "Call-ID: 12345601@here.com\r\n" +
  //             "CSeq: 1 BAR\r\n" +
  //             "Contact: <sip:UserA@100.101.102.103>\r\n" +
  //             "Content-Type: application/sdp\r\n" +
  //             "Content-Length: 47\r\n" +
  //             "\r\n" +
  //             "v=0\r\n" +
  //             "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //             "s=Session SDP\r\n" +
  //             "c=IN IP4 100.101.102.103\r\n" +
  //             "t=0 0\r\n" +
  //             "m=audio 49172 RTP/AVP 0\r\n" +
  //             "a=rtpmap:0 PCMU/8000\r\n").getBytes(),
  //
  //            ("BAR sip:UserB@there.com SIP/2.0\r\n" +
  //             "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //             "From: BigGuy <sip:UserA@here.com>\r\n" +
  //             "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //             "Call-ID: 12345601@here.com\r\n" +
  //             "CSeq: 1 BAR\r\n" +
  //             "Contact: <sip:UserA@100.101.102.103>\r\n" +
  //             "Content-Type: application/sdp\r\n" +
  //             "Content-Length: 47888\r\n" +
  //             "\r\n" +
  //             "v=0\r\n" +
  //             "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //             "s=Session SDP\r\n" +
  //             "c=IN IP4 100.101.102.103\r\n" +
  //             "t=0 0\r\n" +
  //             "m=audio 49172 RTP/AVP 0\r\n" +
  //             "a=rtpmap:0 PCMU/8000\r\n").getBytes(),
  //
  //            ("BAR sip:UserB@there.com SIP/2.0\r\n" +
  //             "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //             "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //             "Call-ID:       \r\n" +
  //             "CSeq: 1 BAR\r\n" +
  //             "Contact: <sip:UserA@100.101.102.103>\r\n" +
  //             "Content-Length: 8\r\n" +
  //             "\r\n" +
  //             "v=0\r\n" +
  //             "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //             "s=Session SDP\r\n" +
  //             "c=IN IP4 100.101.102.103\r\n" +
  //             "t=0 0\r\n" +
  //             "m=audio 49172 RTP/AVP 0\r\n" +
  //             "a=rtpmap:0 PCMU/8000\r\n").getBytes(),
  //
  //            ("BAR sip:UserB@there.com SIP/2.0\r\n" +
  //             "Record-Route: <sip:UserB@there.com;maddr=ss1.wcom.com>\r\n" +
  //             "To: LittleGuy <sip:UserB@there.com>\r\n" +
  //             "Call-ID:XXX    \r\n" +
  //             "Call-ID:       \r\n" +
  //             "CSeq: 1 BAR\r\n" +
  //             "Contact: <sip:UserA@100.101.102.103>\r\n" +
  //             "Content-Length: 8\r\n" +
  //             "\r\n" +
  //             "v=0\r\n" +
  //             "o=UserA 2890844526 2890844526 IN IP4 here.com\r\n" +
  //             "s=Session SDP\r\n" +
  //             "c=IN IP4 100.101.102.103\r\n" +
  //             "t=0 0\r\n" +
  //             "m=audio 49172 RTP/AVP 0\r\n" +
  //             "a=rtpmap:0 PCMU/8000\r\n").getBytes(),
  //        };
  //
  //        for (int i = 0; i < invalids.length; ++i)
  //        {
  //            try
  //            {
  //                DsSipMsgParser.parse(validator, invalids[i]);
  //            }
  //            catch (DsSipMessageValidationException exc)
  //            {
  //                System.out.println("======= INVALID " + i + " =========================");
  //                System.out.println(DsByteString.newString(invalids[i]));
  //                System.out.println("Reason(s)::: " + exc.getMessage());
  //                System.out.println("========================================");
  //            }
  //            catch (DsSipParserListenerException ple)
  //            {
  //                ple.printStackTrace();
  //            }
  //            catch (DsSipParserException pe)
  //            {
  //                pe.printStackTrace();
  //            }
  //        }
  //
  //    }

  /*
   * javadoc inherited
   */
  public DsSipElementListener elementBegin(int contextId, int elementId) {
    return null;
  }

  /*
   * javadoc inherited
   */
  public void parameterFound(
      int contextId, byte[] buffer, int nameOffset, int nameCount, int valueOffset, int valueCount)
      throws DsSipParserListenerException {}

  /*
   * javadoc inherited
   */
  public DsSipElementListener requestURIBegin(byte[] buffer, int schemeOffset, int schemeCount)
      throws DsSipParserListenerException {
    return null;
  }

  /*
   * javadoc inherited
   */
  public void requestURIFound(byte[] buffer, int offset, int count, boolean valid)
      throws DsSipParserListenerException {
    // if the format of the Reqeust-URI was incorrect, then valid is false, and we cannot process
    // this msg.
    m_requestUriValid = valid;
  }

  /*
   * javadoc inherited
   */
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
      m_protocolValid = false;
      return;
    }

    // the protocol must be SIP or the msg is invalid, true for requests and responses
    m_protocolValid =
        (protocolCount == 3
            && (buffer[protocolOffset] == 'S' || buffer[protocolOffset] == 's')
            && (buffer[protocolOffset + 1] == 'I' || buffer[protocolOffset + 1] == 'i')
            && (buffer[protocolOffset + 2] == 'P' || buffer[protocolOffset + 2] == 'p'));

    // defaults to 2, so we only need to parse and set if it is different
    if (majorCount != 1 || buffer[majorOffset] != '2') {
      m_majorVersionValid = false;
    } else {
      m_majorVersionValid = true;
    }

    // Check the minor version, must be 0
    if (minorCount != 1 || buffer[minorOffset] != '0') {
      m_minorVersionValid = false;
    } else {
      m_minorVersionValid = true;
    }
  }

  /*
   * javadoc inherited
   */
  public DsSipHeaderListener getHeaderListener() {
    return this;
  }
}
