// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser.TokenSip;

import Acme.Crypto.Crc16Hash;
import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserException;
import com.cisco.dhruva.DsLibs.DsSipParser.DsSipParserListenerException;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import com.cisco.dhruva.util.log.Trace;
import java.io.*;
import java.util.*;
import org.apache.logging.log4j.Level;

/*
Todo:
does not support-
    Any transport except UDP.
    headers embedded in SIP URLs.
    Unknown headers with encoded URIs


*/
public class DsTokenSipMsgParser implements DsTokenSipConstants {

  private static final int SIP_DATE_LENGTH = 29;

  /** DsPerf constant. */
  private static final int ENTIRE_PARSE;
  /** DsPerf constant. */
  private static final int PARSE_START_LINE;
  /** DsPerf constant. */
  private static final int PARSE_HEADERS;
  /** DsPerf constant. */
  private static final int PARSE_VIA;
  /** DsPerf constant. */
  private static final int PARSE_BODY;

  /** DsPerf constant. */
  private static final int PARSE_INT;
  /** DsPerf constant. */
  private static final int PARSE_LONG;
  /** DsPerf constant. */
  private static final int PARSE_FLOAT;

  /** DsPerf constant. */
  private static final int PARSE_SIP_URL_DATA;
  /** DsPerf constant. */
  private static final int PARSE_TEL_URL_DATA;

  /** DsPerf constant. */
  private static final int FIRE_ELEMENT;
  /** DsPerf constant. */
  private static final int FIRE_PARAMETER;
  /** DsPerf constant. */
  private static final int PARSE_URL_HEADER;
  /** DsPerf constant. */
  private static final int PARSE_NAME_ADDR;
  /** DsPerf constant. */
  private static final int DEEP_PARSE_HEADERS;

  static {

    // DsPerf register
    ENTIRE_PARSE = DsPerf.addType("\nEntire Msg Parse               ");
    PARSE_START_LINE = DsPerf.addType("  Parse Start Line             ");
    PARSE_HEADERS = DsPerf.addType("  Parse Headers                ");
    DEEP_PARSE_HEADERS = DsPerf.addType("    Deep Parse Headers         ");
    FIRE_ELEMENT = DsPerf.addType("    Fire Element               ");
    FIRE_PARAMETER = DsPerf.addType("    Fire Parameter             ");
    PARSE_VIA = DsPerf.addType("    Parse Via                  ");
    PARSE_BODY = DsPerf.addType("  Parse Body                   ");
    PARSE_INT = DsPerf.addType("Parse Int                      ");
    PARSE_LONG = DsPerf.addType("Parse Long                     ");
    PARSE_FLOAT = DsPerf.addType("Parse Float                    ");
    PARSE_SIP_URL_DATA = DsPerf.addType("Parse SIP URL Data             ");
    PARSE_TEL_URL_DATA = DsPerf.addType("Parse TEL URL Data             ");
    PARSE_URL_HEADER = DsPerf.addType("Parse URL Header               ");
    PARSE_NAME_ADDR = DsPerf.addType("Parse Name Addr                ");
  }

  // Set the logging category
  protected static Trace Log = Trace.getTrace(DsTokenSipMsgParser.class.getName());

  /** Private default constructor. Disallow instance construction. */
  // private DsTokenSipMsgParser()
  // {
  // }

  /**
   * Parses a byte array as a binary Tokenized msg and passes the events to the created listener.
   *
   * @param msgFactory the way to create the listener.
   * @param msg the byte array that contains the SIP message to parse.
   * @return the created listener.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static DsSipMessageListener parse(DsSipMessageListenerFactory msgFactory, byte msg[])
      throws DsSipParserListenerException, DsSipParserException {
    return parse(msgFactory, msg, 0, msg.length);
  }

  /**
   * Parses a byte array and passes the events to the created listener.
   *
   * @param msgFactory the way to create the listener.
   * @param msg the byte array that contains the SIP message to parse.
   * @param offset the start of the SIP message in the array.
   * @param count the number of bytes in the SIP message.
   * @return the created listener.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static DsSipMessageListener parse(
      DsSipMessageListenerFactory msgFactory, byte msg[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(ENTIRE_PARSE);

    if (Log.isDebugEnabled()) Log.debug("\n" + DsString.toSnifferDisplay(msg));

    MsgBytes mb = new MsgBytes(msg, offset, count);

    Crc16Hash hash = new Crc16Hash();
    hash.add("1/com.sprintpcs/1".getBytes());
    int dictionarySignature = DsTokenSipInteger.read16Bit(hash.get());
    if (mb.msg[mb.i] == TOKEN_SIP_PREFIX1) {
      mb.i++;
      if (Log.isDebugEnabled()) Log.debug("Found the 0x80 encoded SIP message prefix");
    } else if (mb.msg[mb.i] == TOKEN_SIP_PREFIX2) {
      mb.i++;
      if (Log.isDebugEnabled()) Log.debug("Found the 0x81 encoded SIP message prefix");
      dictionarySignature = DsTokenSipInteger.read16Bit(mb);
      /*int messageId =*/ DsTokenSipInteger.read16Bit(mb);
    }

    DsTokenSipDictionary dictionary;
    try {
      dictionary = DsTokenSipMasterDictionary.getDictionary(dictionarySignature);
    } catch (DsTokenSipInvalidDictionaryException e) {
      throw new DsSipParserException(e);
    }

    DsTokenSipMessageDictionary messageDictionary;

    try {
      messageDictionary = new DsTokenSipMessageDictionary(dictionary, mb);
    } catch (DsTokenSipInvalidDictionaryException e) {
      throw new DsSipParserException(e);
    }

    DsSipMessageListener messageListener =
        parseStartLine(dictionary, msgFactory, mb, messageDictionary);

    if (messageListener == null) {
      throw new DsSipParserException(
          "Null listener: Cannot lazy parse a binary SIP encoded message");
    }

    // int exceptionCount = 0;

    if (Log.isDebugEnabled()) Log.debug("First header byte is " + mb.msg[mb.i]);

    try {
      DsSipHeaderListener headerListener = messageListener.getHeaderListener();

      if (DsPerf.ON) DsPerf.start(PARSE_HEADERS);
      HeaderEncodingTypeHolder headerId;
      int numHeaders = 0;

      boolean foundContentLength = false;

      // keep parsing until the body is found
      while (true) {
        try {
          if ((headerId = getHeaderId(dictionary, mb, messageDictionary)) == null) {
            break;
          }

          if (headerId.getType() == CONTENT_TYPE) {
            foundContentLength = true;
          }

          if (Log.isDebugEnabled()) {
            Log.debug(
                "Found new header- "
                    + ((headerId.getType() != UNKNOWN_HEADER)
                        ? (new DsByteString(
                            DsTokenSipHeaderDictionary.getHeaderName(headerId.getType())))
                        : BS_UNKNOWN_HEADER));
          }

          parseHeader(dictionary, headerListener, headerId, mb, messageDictionary);
          numHeaders++;
        } catch (Exception e) {
          // ignore the first few exceptions and continue to parse as best as we can
          // just make sure that there is not an infinite loop trying to parse
          // todo if (++exceptionCount > MAX_EXCEPTIONS)

          Log.warn("Exception caught while parsing headers", e);

          // if (++exceptionCount > 5)
          // {
          throw new Exception("Maximum number of parser exceptions exeeded.");
          // }
        }
      }
      if (DsPerf.ON) DsPerf.stop(PARSE_HEADERS);
      if (Log.isDebugEnabled()) Log.debug("Number of headers parsed - " + numHeaders);
      if (Log.isDebugEnabled()) Log.debug("Index into the message is now " + mb.i);

      if ((mb.i < mb.msg.length - 1) && (mb.msg[mb.i] != TOKEN_SIP_CRLF_CRLF)) {
        parseBody(
            dictionary, messageListener, headerListener, mb, messageDictionary, foundContentLength);
      } else {
        messageListener.messageFound(DsByteString.BS_EMPTY_STRING.data(), 0, 0, true);
      }

      // todo find out how malformed requests are rejected normally.

      if (DsPerf.ON) DsPerf.stop(ENTIRE_PARSE);

      if (Log.isDebugEnabled()) Log.debug("\nDONE PARSING\n");

      return messageListener;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (DsTokenSipInvalidDictionaryException e) {
      throw new DsSipParserListenerException(e);
    } catch (Exception e) {
      // this will return a bogus body - jsm
      throw generateDsSipParserException(messageListener, e, msg, offset, count);
    }
  }

  /**
   * Parses the first line of a SIP message. Creates the appropriate listener based on the first
   * line.
   *
   * @param msgFactory the way to create the listener.
   * @param mb the message bytes that contains the SIP message to parse.
   * @return the created listener.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  private static DsSipMessageListener parseStartLine(
      DsTokenSipDictionary dictionary,
      DsSipMessageListenerFactory msgFactory,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_START_LINE);

    DsSipMessageListener sipMsg;
    try {
      // int start = mb.i; // beginning of method name (request) or SIP version (response)
      byte ch = mb.msg[mb.i++];

      switch (ch) {
          // look for highly optimized start lines first
        case TOKEN_SIP_FIXED_FORMAT_INVITE_START_LINE:
          byte flags = mb.msg[mb.i++];
          DsTokenSipNameAddressFixedFormatEncoder nameAddrEncoding =
              new DsTokenSipNameAddressFixedFormatEncoder(flags);

          sipMsg =
              msgFactory.requestBegin(
                  DS_TOKEN_SIP_INVITE_STARTLINE, 0, DS_TOKEN_SIP_INVITE_STARTLINE.length, true);
          DsSipElementListener uri;

          switch (nameAddrEncoding.getScheme()) {
            case SIP_URL_ID:
              uri =
                  sipMsg.requestURIBegin(
                      DS_TOKEN_SIP_SIP_SCHEME, 0, DS_TOKEN_SIP_SIP_SCHEME.length);
              parseSipUrl(dictionary, uri, mb, nameAddrEncoding, messageDictionary);
              sipMsg.requestURIFound(DsByteString.EMPTY_BYTES, 0, 0, true);
              break;
            case TEL_URL_ID:
              uri =
                  sipMsg.requestURIBegin(
                      DS_TOKEN_SIP_TEL_SCHEME, 0, DS_TOKEN_SIP_TEL_SCHEME.length);
              parseTelUrl(dictionary, uri, mb, nameAddrEncoding, messageDictionary);
              sipMsg.requestURIFound(DsByteString.EMPTY_BYTES, 0, 0, true);
              break;
            default:
              // UNKNOWN_URL_ID
              DsByteString scheme = messageDictionary.get(mb);
              uri = sipMsg.requestURIBegin(scheme.data(), scheme.offset(), scheme.length());
              DsByteString val = messageDictionary.get(mb);
              byte[] data = new byte[scheme.length() + val.length() + 1];
              System.arraycopy(scheme.data(), scheme.offset(), data, 0, scheme.length());
              data[scheme.length()] = (byte) ':';
              System.arraycopy(val.data(), val.offset(), data, scheme.length() + 1, val.length());
              sipMsg.requestURIFound(data, 0, data.length, true);
          }

          sipMsg.protocolFound(
              s_ProtocolBytes,
              s_ProtocolStart,
              s_ProtocolLength,
              s_MajorVersionStart,
              s_MajorVersionLength,
              s_MinorVersionStart,
              s_MinorVersionLength,
              true);
          return sipMsg;

        case TOKEN_SIP_FIXED_FORMAT_RESPONSE_START_LINE:
          // get the status code
          // int statusCode = (ch << 8) + (mb.msg[mb.i++]);
          int statusCode = DsTokenSipInteger.read16Bit(mb);
          DsByteString responseString = messageDictionary.get(mb);

          sipMsg =
              msgFactory.responseBegin(
                  responseString.data(),
                  statusCode,
                  responseString.offset(),
                  responseString.length(),
                  true);

          if (sipMsg == null) {
            // nothing left to do, the caller does not want this message parsed yet
            if (DsPerf.ON) DsPerf.stop(PARSE_START_LINE);
            return null;
          }

          sipMsg.protocolFound(
              s_ProtocolBytes,
              s_ProtocolStart,
              s_ProtocolLength,
              s_MajorVersionStart,
              s_MajorVersionLength,
              s_MinorVersionStart,
              s_MinorVersionLength,
              true);
          return sipMsg;

          // start line is not highly optimized.  It's a non-INVITE request
        case TOKEN_SIP_FIXED_FORMAT_REQUEST_START_LINE:
          // create the request for this method type
          DsByteString method = messageDictionary.getMethodShortcut(mb);
          sipMsg = msgFactory.requestBegin(method.data(), method.offset(), method.length(), true);

          // todo farm this part out and make it common

          flags = mb.msg[mb.i++];
          nameAddrEncoding = new DsTokenSipNameAddressFixedFormatEncoder(flags);

          switch (nameAddrEncoding.getScheme()) {
            case SIP_URL_ID:
              uri =
                  sipMsg.requestURIBegin(
                      DS_TOKEN_SIP_SIP_SCHEME, 0, DS_TOKEN_SIP_SIP_SCHEME.length);
              parseSipUrl(dictionary, uri, mb, nameAddrEncoding, messageDictionary);
              sipMsg.requestURIFound(DsByteString.EMPTY_BYTES, 0, 0, true);

              break;
            case TEL_URL_ID:
              uri =
                  sipMsg.requestURIBegin(
                      DS_TOKEN_SIP_TEL_SCHEME, 0, DS_TOKEN_SIP_TEL_SCHEME.length);
              parseTelUrl(dictionary, uri, mb, nameAddrEncoding, messageDictionary);
              sipMsg.requestURIFound(DsByteString.EMPTY_BYTES, 0, 0, true);

              break;
            default:
              // UNKNOWN_URL_ID
              DsByteString scheme = messageDictionary.get(mb);
              uri = sipMsg.requestURIBegin(scheme.data(), scheme.offset(), scheme.length());
              DsByteString val = messageDictionary.get(mb);
              byte[] data = new byte[scheme.length() + val.length() + 1];
              System.arraycopy(scheme.data(), scheme.offset(), data, 0, scheme.length());
              data[scheme.length()] = (byte) ':';
              System.arraycopy(val.data(), val.offset(), data, scheme.length() + 1, val.length());
              sipMsg.requestURIFound(data, 0, data.length, true);
          }

          // todo make a ProtocolFound(sipMsg) method
          sipMsg.protocolFound(
              s_ProtocolBytes,
              s_ProtocolStart,
              s_ProtocolLength,
              s_MajorVersionStart,
              s_MajorVersionLength,
              s_MinorVersionStart,
              s_MinorVersionLength,
              true);

          return sipMsg;
        default:
          generateDsSipParserException(
              null,
              new DsSipParserException("Startline expected.  Other data found"),
              mb.msg,
              0,
              mb.msg.length);
          return null;
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      // this will return a bogus body - jsm
      throw generateDsSipParserException(null, e, mb.msg, mb.offset, mb.count);
    }
  }

  //    private static final boolean isValidURL(byte token)
  //    {
  //        switch(token)
  //        {
  //           case TOKEN_SIP_OPEN_URI:
  //           case TOKEN_SIP_BRACKETED_URI:
  //           case TOKEN_SIP_OPEN_URI_WITH_PORT:
  //           case TOKEN_SIP_BRACKETED_URI_WITH_PORT:
  //                return true;
  //           default:
  //                return false;
  //        }
  //    }

  private static HeaderEncodingTypeHolder getHeaderId(
      DsTokenSipDictionary dictionary, MsgBytes mb, DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException, DsTokenSipInvalidContextException {
    int type = UNKNOWN_HEADER;

    try {
      // todo make sure that the case statements are in ascending order
      switch (mb.msg[mb.i]) {
          // end of all headers
        case TOKEN_SIP_END_OF_HEADERS:
        case DsTokenSDPMsgParser.TOKEN_SIP_CONTENT_GENERIC:
        case DsTokenSDPMsgParser.TOKEN_SIP_CONTENT_SDP_DEPRECATED:
          return null; // no more headers to parse

          // fixed format encoded headers
        case TOKEN_SIP_FIXED_FORMAT_AUTHENTICATION_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(PROXY_AUTHENTICATE, true);
        case TOKEN_SIP_FIXED_FORMAT_CALLID1_HEADER:
        case TOKEN_SIP_FIXED_FORMAT_CALLID2_HEADER:
          HeaderEncodingTypeHolder holder =
              new HeaderEncodingTypeHolder(CALL_ID, true, mb.msg[mb.i]);
          mb.i++;
          return holder;
        case TOKEN_SIP_FIXED_FORMAT_CONTACT_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(CONTACT, true);
        case TOKEN_SIP_FIXED_FORMAT_CSEQ_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(CSEQ, true);
        case TOKEN_SIP_FIXED_FORMAT_AUTHORIZATION_HEADER:
        case TOKEN_SIP_FIXED_FORMAT_AUTHORIZATION_VARIANT_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(PROXY_AUTHORIZATION, true, mb.msg[mb.i - 1]);
        case TOKEN_SIP_FIXED_FORMAT_FROM_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(FROM, true);
        case TOKEN_SIP_FIXED_FORMAT_RECORD_ROUTE_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(RECORD_ROUTE, true);
        case TOKEN_SIP_FIXED_FORMAT_ROUTE_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(ROUTE, true);
        case TOKEN_SIP_FIXED_FORMAT_TO_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(TO, true);
        case TOKEN_SIP_FIXED_FORMAT_VIA_HEADER:
          mb.i++;
          return new HeaderEncodingTypeHolder(VIA, true);
        case TOKEN_SIP_KNOWN_HEADER:
          mb.i++;
          type = DsTokenSipHeaderDictionary.getHeaderId(mb.msg[mb.i]);
          if (type == DsTokenSipHeaderDictionary.UNDEFINED) {
            throw new DsTokenSipInvalidContextException(
                "Known header token found followed by undefined value");
          } else {
            mb.i++;
            return new HeaderEncodingTypeHolder(type, false);
          }
        case TOKEN_SIP_UNKNOWN_HEADER:
          mb.i++;
          DsByteString headerName = messageDictionary.get(mb);
          if (headerName.length() > 0) {
            return new HeaderEncodingTypeHolder(headerName);
          } else {
            return null;
          }

          // some other token.  check the dictionary
        default:
          type = DsTokenSipHeaderDictionary.getShortcutHeaderId(mb.msg[mb.i]);
          if (type == DsSipConstants.UNKNOWN_HEADER) {
            return null;

          } else {
            mb.i++;
            return new HeaderEncodingTypeHolder(type, false);
          }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      return null; // no more headers to parse
    }
  }

  /**
   * Parses a SIP header value. Note that the "name:" portion of the header must not exist.
   *
   * @param headerListener where to report the results of parsed header.
   * @param headerType the header ID that the data is from.
   * @param data the byte array that contains the SIP message to parse.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseHeader(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      HeaderEncodingTypeHolder headerType,
      MsgBytes data,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(DEEP_PARSE_HEADERS);

    if (Log.isEnabled(Level.DEBUG)) {
      // Log.debug("Starting parseHeader headerType =
      // "+DsTokenSipHeaderDictionary.getHeader(headerType));
    }

    // the order of this switch statement must remain in sync with the order od DsSipConstants - jsm
    switch (headerType.getType()) {
      case VIA:
        parseVia(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case MAX_FORWARDS:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case ROUTE:
      case RECORD_ROUTE:
      case TO:
      case FROM:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case CSEQ:
        parseCSeq(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case CALL_ID:
        parseCallIdHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case CONTENT_LENGTH:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case CONTACT:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case EXPIRES:
        parseDateOrLongHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseDateOrLong (headerListener, headerType, data, messageDictionary);
        break;
      case PROXY_REQUIRE:
      case REQUIRE:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case SERVICE_AGENT_PHASE:
      case SERVICE_AGENT_CONTEXT:
      case SERVICE_AGENT_APPLICATION:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case SERVICE_AGENT_ROUTE:
      case REMOTE_PARTY_ID:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case EVENT:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case DIVERSION:
      case P_ASSOCIATED_URI:
      case P_CALLED_PARTY_ID:
      case SERVICE_ROUTE:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case P_ACCESS_NETWORK_INFO:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
        // case PRIVACY:
        // break;
      case CONTENT_TYPE:
        parseMediaType(dictionary, headerListener, headerType.getType(), data, messageDictionary);
        break;
      case X_APPLICATION:
      case X_APPLICATION_CONTEXT:
      case X_FROM_OUTSIDE:
      case AS_PATH:
      case AS_RECORD_PATH:
      case AE_COOKIE:
        parseStringHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        break;
      case SUBSCRIPTION_EXPIRES:
        parseDateOrLongHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseDateOrLong (headerListener, headerType, data, offset, count);
        break;
      case ALLOW_EVENTS:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case PROXY_AUTHENTICATE:
        parseProxyAuthenticateHeader(dictionary, headerListener, data, messageDictionary);
        break;
      case PROXY_AUTHORIZATION:
        parseProxyAuthorizationHeader(
            dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case AUTHORIZATION:
      case AUTHENTICATION_INFO:
      case WWW_AUTHENTICATE:
        parseStringHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseAuth(headerListener, headerType, data, offset, count);
        break;
      case ACCEPT:
        parseMediaType(dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseMediaTypeWithParams(headerListener, headerType, data, offset, count);
        break;
      case ACCEPT_ENCODING:
      case ACCEPT_LANGUAGE:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case ALERT_INFO:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case ALLOW:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case CALL_INFO:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case CONTENT_DISPOSITION:
      case CONTENT_ENCODING:
      case CONTENT_LANGUAGE:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case DATE:
        parseDateOrLongHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseDate(headerListener, headerType, data, offset, count);
        break;
      case ERROR_INFO:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case IN_REPLY_TO:
      case MIME_VERSION:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case ORGANIZATION:
      case PRIORITY:
      case RACK:
        parseStringHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        break;
      case RETRY_AFTER:
        parseDateOrLongHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseDateOrLong (headerListener, headerType, data, offset, count);
        break;
      case RSEQ:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case SERVER:
        // parseStringHeader(headerListener, headerType, data, messageDictionary);
        parseServerUserAgent(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        break;
      case SUBJECT:
        parseStringHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        break;
      case SUPPORTED:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case TIMESTAMP:
        parseStringHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseTimestamp(headerListener, headerType, data, offset, count);
        break;
      case UNSUPPORTED:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case USER_AGENT:
        // parseStringHeader(headerListener, headerType, data, messageDictionary);
        parseServerUserAgent(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        break;
      case WARNING:
        parseStringHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseWarning(headerListener, headerType, data, offset, count);
        break;
      case SESSION_EXPIRES:
        parseDateOrLongHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        // parseDateOrLong (headerListener, headerType, data, offset, count);
        break;
      case TRANSLATE:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case CONTENT_VERSION:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case REPLY_TO:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case SUBSCRIPTION_STATE:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;

        // todo : why do these 2 header references not compile?
        // I put the calls to the right methods in and these should work now - jsm
      case REFER_TO:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case MIN_EXPIRES:
        parseDateOrLongHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        break;

      case PATH:
      case P_ASSERTED_IDENTITY:
      case P_PREFERRED_IDENTITY:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case REPLACES:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;

        // per Edgar, connection info header is no longer used by AE
      case X_CONNECTION_INFO:
        parseStringHeader(
            dictionary, headerListener, headerType.getType(), data, messageDictionary);
        break;
      case P_CHARGING_FUNCTION_ADDRESSES:
      case P_CHARGING_VECTOR:
      case P_VISITED_NETWORK_ID:
        parseTokenListWithParams(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case REFERRED_BY:
        parseUrlHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      case UNKNOWN_HEADER:
        // todo - is this the right thing to do>???
        parseUnknownHeader(dictionary, headerListener, headerType, data, messageDictionary);
        break;
      default:
        break;
    }
    if (DsPerf.ON) DsPerf.stop(DEEP_PARSE_HEADERS);
  }

  /**
   * Reports the body of a SIP message to the listener.
   *
   * @param sipMsg where to report the results.
   * @param mb the message bytes that contains the SIP message to parse.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  private static void parseBody(
      DsTokenSipDictionary dictionary,
      DsSipMessageListener sipMsg,
      DsSipHeaderListener headerListener,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary,
      boolean hasContentLength)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_BODY);
    if (Log.isDebugEnabled()) Log.debug("parsing some kind of body");

    switch (mb.msg[mb.i]) {
      case DsTokenSDPMsgParser.TOKEN_SIP_CONTENT_SDP_DEPRECATED:
      case DsTokenSDPMsgParser.TOKEN_SIP_CONTENT_SDP:
        if (hasContentLength == false) {
          DsSipElementListener header = headerListener.headerBegin(CONTENT_TYPE);
          if (header == null) {
            throw new DsSipParserException(
                "Null listener: Cannot lazy parse a binary SIP encoded message");
          }

          fireElement(
              header, CONTENT_TYPE, TYPE, BS_APPLICATION.data(), 0, BS_APPLICATION.length());
          fireElement(header, CONTENT_TYPE, SUB_TYPE, BS_SDP.data(), 0, BS_SDP.length());
          headerListener.headerFound(CONTENT_TYPE, mb.msg, 0, 0, true);
        }
        DsTokenSDPMsgParser.parse(sipMsg, mb, messageDictionary);
        break;

      case DsTokenSDPMsgParser.TOKEN_SIP_CONTENT_GENERIC:
        mb.i++;
        if (Log.isDebugEnabled()) Log.debug("Starting parse of  fixed format generic");

        if (hasContentLength == false) {
          DsSipElementListener header = headerListener.headerBegin(CONTENT_TYPE);
          if (header == null) {
            throw new DsSipParserException(
                "Null listener: Cannot lazy parse a binary SIP encoded message");
          }

          header = headerListener.headerBegin(CONTENT_TYPE);
          if (header == null) {
            throw generateDsSipParserException(
                headerListener,
                "Cannot lazy parse an encoded message body",
                CONTENT_TYPE,
                mb.msg,
                0,
                mb.msg.length);
          }

          DsByteString content = messageDictionary.get(mb);

          int typeSep = findByte(content.data(), content.offset(), content.length(), B_SLASH);
          if (typeSep > 0) {
            fireElement(header, CONTENT_TYPE, TYPE, content.data(), content.offset(), typeSep);
            fireElement(
                header,
                CONTENT_TYPE,
                SUB_TYPE,
                content.data(),
                content.offset() + typeSep + 1,
                content.length() - typeSep - 1);
          } else {
            fireElement(
                header, CONTENT_TYPE, TYPE, content.data(), content.offset(), content.length());
          }
          headerListener.headerFound(
              CONTENT_TYPE, content.data(), content.offset(), content.length(), true);
        }

        // get the data
        if ((mb.msg[mb.i] >= TOKEN_SIP_ASCII_CHAR_LOW)
            && (mb.msg[mb.i] <= TOKEN_SIP_ASCII_CHAR_HIGH)) {
          // it's raw data
          sipMsg.messageFound(mb.msg, mb.i, mb.msg.length - mb.i, true);
        } else {
          if (Log.isDebugEnabled()) Log.debug("Starting parse of raw body");

          byte[] tempBody = decodeRawHeaderData(dictionary, mb, messageDictionary);

          if (Log.isDebugEnabled()) Log.debug("body length is " + tempBody.length);
          if (tempBody != null) {
            sipMsg.messageFound(tempBody, 0, tempBody.length, true);
          }
        }

        break;

      default:

        // todo other media
        mb.i++;
        if (Log.isDebugEnabled()) Log.debug("OTHER media found");

        sipMsg.messageFound(DsByteString.BS_EMPTY_STRING.data(), 0, 0, true);
        break;
    }
    if (DsPerf.ON) DsPerf.stop(PARSE_BODY);
  }

  /**
   * Helper method to notify a listener than an element has been found. The listener may be asked
   * for sub-element listeners and then deeper parsing can take place.
   *
   * @param element the element listener to notify
   * @param contextId the context to fire this event in
   * @param buffer the buffer where the element can be found
   * @param offset the index that the element starts at
   * @param count the number of bytes in the found element
   * @throws DsSipParserListenerException if it is thrown by one of the listeners
   */
  protected static void fireElement(
      DsSipElementListener element,
      int contextId,
      int elementId,
      byte[] buffer,
      int offset,
      int count)
      throws DsSipParserListenerException {
    if (DsPerf.ON) DsPerf.start(FIRE_ELEMENT);

    if (element != null) {
      DsSipElementListener subElement = null;

      // first check and see if this may need to be parsed deeper
      switch (elementId) {
        case SIP_URL:
        case SIPS_URL:
        case TEL_URL:
        case HTTP_URL:
        case UNKNOWN_URL:
        case SIP_DATE:
          // we do not parse Expires SIP_DATEs, since they are just 3600 anyway. - jsm
          if (contextId != EXPIRES) {
            subElement = element.elementBegin(contextId, elementId);
          }
          break;
        case SINGLE_VALUE:
          if (contextId == CONTENT_LANGUAGE
              || contextId == ACCEPT_LANGUAGE
              || contextId == SIP_DATE
              || contextId == EVENT
              || contextId == ALLOW_EVENTS) {
            subElement = element.elementBegin(contextId, elementId);
          }
        default:
          break;
      }

      boolean isElementValid = true;

      try {
        // maybe move this below the call to elementFound? - jsm
        if (subElement != null) // needs to be parsed as well
        {
          // find the right parser for this type and parse it
          switch (elementId) {
            case SIP_URL:
            case SIPS_URL:
              Log.error("Oops.  I didn't think I'd ever get here");
              // parseSipUrl(subElement, mb, )
              break;
            case TEL_URL:
              Log.error("Oops.  I didn't think I'd ever get here 1");
              // parseTelUrl(subElement, buffer, offset, count);
              break;
            case HTTP_URL:
            case UNKNOWN_URL:
              Log.error("Oops.  I didn't think I'd ever get here 3");
              // parseUnknownUrl(subElement, buffer, offset, count);
              break;
            default:
              break;
          }

          // make this a switch statement
          if ((contextId == CONTENT_LANGUAGE || contextId == ACCEPT_LANGUAGE)
              && elementId == SINGLE_VALUE) {
            // need to parse the language
            parseLanguage(subElement, contextId, buffer, offset, count);
          }

          if (((contextId == DATE) && elementId == SINGLE_VALUE) || (elementId == SIP_DATE)) {
            // need to parse the SIP-Date
            parseSipDate(subElement, contextId, buffer, offset, count);
          }

          if ((contextId == EVENT || contextId == ALLOW_EVENTS) && elementId == SINGLE_VALUE) {
            // need to parse the event package and sub package
            parseEventPackage(subElement, contextId, buffer, offset, count);
          }
        }
      } catch (DsSipParserListenerException e) {
        throw e;
      } catch (Exception e) {
        isElementValid = false;
      }

      element.elementFound(contextId, elementId, buffer, offset, count, isElementValid);
    }

    if (DsPerf.ON) DsPerf.stop(FIRE_ELEMENT);
  }

  private static void fireParameter(
      DsSipElementListener element,
      int contextId,
      byte[] buffer,
      int nameOffset,
      int nameCount,
      int valueOffset,
      int valueCount)
      throws DsSipParserListenerException {
    if (DsPerf.ON) DsPerf.start(FIRE_PARAMETER);

    // if (Log.isEnabledFor(Priority.DEBUG))
    // {
    //    Log.debug("In fireParameter.  The parameter string is "+new String(buffer));
    // }

    // fix any missed flag parameter value
    if (valueOffset < 0 || valueCount < 0) {
      valueOffset = 0;
      valueCount = 0;
    }

    element.parameterFound(contextId, buffer, nameOffset, nameCount, valueOffset, valueCount);

    if (DsPerf.ON) DsPerf.stop(FIRE_PARAMETER);
  }

  // To
  // From
  // Contact
  // Route
  // Record-Route
  // Alert-Info (allow 0 or more)
  // Call-Info (allow 0 or more)
  // Error-Info (allow 0 or more)
  // more?
  static void parseUrlHeader(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      HeaderEncodingTypeHolder headerTypeHolder,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_URL_HEADER);
    if (Log.isDebugEnabled()) {
      Log.debug(
          "Starting urlHeader parse for "
              + headerTypeHolder.getName()
              + "/"
              + headerTypeHolder.getType());
    }

    try {
      int headerType = headerTypeHolder.getType();
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        throw generateDsSipParserException(
            headerListener,
            "Cannot do a lazy parse of encoded messages",
            headerType,
            mb.msg,
            0,
            mb.msg.length);
      }

      if (headerTypeHolder.isFixedFormat()) {
        if (Log.isDebugEnabled()) Log.debug("Starting urlHeader parse fixed format");

        byte uriFlags = mb.msg[mb.i++];

        DsTokenSipNameAddressEncoder nameAddrEncoding =
            new DsTokenSipNameAddressFixedFormatEncoder(uriFlags);

        parseNameAddr(dictionary, header, headerType, mb, nameAddrEncoding, messageDictionary);

        if (nameAddrEncoding.isTagParam()) {
          DsByteString tagValue = messageDictionary.get(mb);
          byte[] tagString = new byte[s_URLTagStartBytes.length + tagValue.length()];
          System.arraycopy(s_URLTagStartBytes, 0, tagString, 0, s_URLTagStartBytes.length);
          System.arraycopy(
              tagValue.data(),
              tagValue.offset(),
              tagString,
              s_URLTagStartBytes.length,
              tagValue.length());

          fireParameter(
              header,
              headerType,
              tagString,
              0,
              s_URLTagStartBytes.length - 1,
              s_URLTagStartBytes.length,
              tagValue.length());
        }

        parseParameters(dictionary, header, mb, messageDictionary, headerType);

        headerListener.headerFound(headerType, DsByteString.EMPTY_BYTES, 0, 0, true);
      } // done fixed format parsing
      else {

        if (mb.msg[mb.i] == '*') // star for Contact only
        {
          // check for contact header type? - jsm
          mb.i++;
          fireElement(header, headerType, WILDCARD, s_ContactWildcardBytes, 0, 1);
          headerListener.headerFound(headerType, mb.msg, 0, 0, true);

        } else {
          // maybe it's strings
          int encodingType = getNextTokenType(mb.msg[mb.i]);
          if (encodingType == DATA_TOKEN) {
            // byte[] headerVal = lazyParseUrlHeader(headerTypeHolder, mb, messageDictionary);

            // todo reuse the getRawdata method
            // header is string encoded
            DsByteString data = new DsByteString(getNextToken(dictionary, mb, messageDictionary));
            while (getNextTokenType(mb.msg[mb.i]) == DATA_TOKEN) {
              data.append(getNextToken(dictionary, mb, messageDictionary));
            }

            // skip URIs

            if (getContextChangeType(mb.msg[mb.i]) == PARAMETER_CONTEXT) {
              constructParameters(dictionary, data, mb, messageDictionary);
            }

            if (getContextChangeType(mb.msg[mb.i]) == NEW_DATA_FIELD_CONTEXT) {
              DsSipMsgParser.parseHeader(
                  headerListener, headerTypeHolder.getType(), data.toByteArray(), 0, data.length());
            }
          } else {
            // context encoding.  There must be a URI encoded here
            if (getContextChangeType(mb.msg[mb.i]) != NAME_ADDR_CONTEXT) {
              throw new DsSipParserException(
                  "Unexpected token found in generic URI decoding.  Didn't find strings OR URI");
            }

            DsTokenSipNameAddressEncoder nameAddrEncoding =
                new DsTokenSipNameAddressEncoder(mb.msg[mb.i++]);
            parseNameAddr(dictionary, header, headerType, mb, nameAddrEncoding, messageDictionary);
            parseParameters(
                dictionary, header, mb, messageDictionary, nameAddrEncoding.getScheme());
            headerListener.headerFound(headerType, DsByteString.EMPTY_BYTES, 0, 0, true);
          }
        }
      }
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(
          headerListener, e, headerTypeHolder.getType(), mb.msg, 0, mb.msg.length);
    }
  }

  // todo implement this!!!
  //    private static byte[] buildUrlHeaderString(HeaderEncodingTypeHolder headerTypeHolder,
  // MsgBytes mb, DsTokenSipMessageDictionary md)
  //    {
  //        byte uriFlags = mb.msg[mb.i++];
  //
  //        DsTokenSipNameAddressEncoder nameAddrEncoding = new
  // DsTokenSipNameAddressFixedFormatEncoder(uriFlags);
  //
  //
  //        return null;
  //    }

  static void parseStringHeader(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      int headerType,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      byte[] headerData = decodeRawHeaderData(dictionary, mb, messageDictionary);

      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        throw generateDsSipParserException(
            headerListener,
            "Cannot do a lazy parse of encoded messages",
            headerType,
            mb.msg,
            0,
            mb.msg.length);
      }

      if ((headerData == null) || (headerData.length == 0)) {
        fireElement(header, headerType, SINGLE_VALUE, headerData, 0, 0);
        headerListener.headerFound(headerType, headerData, 0, 0, true);
      } else {
        fireElement(header, headerType, SINGLE_VALUE, headerData, 0, headerData.length);
        headerListener.headerFound(headerType, headerData, 0, headerData.length, true);
      }
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, mb.msg, 0, mb.msg.length);
    }
  }

  static void parseDateOrLongHeader(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      int headerType,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      byte[] headerData = decodeRawHeaderData(dictionary, mb, messageDictionary);

      // todo Just call the DsSipMsgParser parser for this type
      DsSipMsgParser.parseDateOrLong(headerListener, headerType, headerData, 0, headerData.length);
      /*
      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null)
      {
          throw generateDsSipParserException(headerListener, "Cannot do a lazy parse of encoded messages", headerType, mb.msg, 0, mb.msg.length);
      }

      if ((headerData == null)||(headerData.length == 0))
      {
          fireElement(header, headerType, SINGLE_VALUE, headerData, 0, 0);
          headerListener.headerFound(headerType, headerData, 0, 0, true);
      }
      else
      {
          if ((headerData[0] >= '0' && headerData[0] <= '9'))
          {
              fireElement(header, headerType, DELTA_SECONDS, headerData, 0, headerData.length);
          }
          else
          {
              fireElement(header, headerType, SIP_DATE, headerData, 0, headerData.length);
          }
          headerListener.headerFound(headerType, headerData, 0, headerData.length, true);
      }
      */
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, mb.msg, 0, mb.msg.length);
    }
  }

  // todo do degenerate case
  static void parseCallIdHeader(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      HeaderEncodingTypeHolder headerType,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(CALL_ID);
      if (header == null) {
        throw generateDsSipParserException(
            headerListener,
            "Cannot do a lazy parse of encoded messages",
            headerType.getType(),
            mb.msg,
            0,
            mb.msg.length);
      }

      if ((headerType.getFixedFormatType() == TOKEN_SIP_FIXED_FORMAT_CALLID1_HEADER)
          || (headerType.getFixedFormatType() == TOKEN_SIP_FIXED_FORMAT_CALLID2_HEADER)) {
        if (Log.isDebugEnabled()) Log.debug("Parse Call-ID fixed format");
        // fixed format call-id parsing
        DsByteString callIdStart = null;

        if (headerType.getFixedFormatType() == TOKEN_SIP_FIXED_FORMAT_CALLID1_HEADER) {
          // 4bytes of hex
          if (Log.isDebugEnabled()) Log.debug("Parse Call-ID fixed format type 1");
          // callIdStart = DsHexEncoding.toHex(DsByteString.createCopy(mb.msg, mb.i,
          // 4).toByteArray()).getBytes();
          callIdStart = getHexData(mb.msg, mb.i, 4);
          mb.i += 4;
        } else {

          // string data
          if (Log.isDebugEnabled()) Log.debug("Parse Call-ID fixed format type 2");
          callIdStart = messageDictionary.get(mb);
        }

        if (callIdStart == null) {
          if (Log.isDebugEnabled()) Log.debug("Parse Call-ID fixed format is null");
          throw generateDsSipParserException(
              headerListener,
              "Cannot do a lazy parse of encoded messages",
              CALL_ID,
              mb.msg,
              0,
              mb.msg.length);
        }

        DsByteString callIdEnd = messageDictionary.get(mb);

        byte[] callIdBytes;

        if (callIdEnd != null) {
          callIdBytes = new byte[callIdEnd.length() + callIdStart.length() + 1];
          System.arraycopy(
              callIdStart.data(), callIdStart.offset(), callIdBytes, 0, callIdStart.length());
          callIdBytes[callIdStart.length()] = '@';
          System.arraycopy(
              callIdEnd.data(),
              callIdEnd.offset(),
              callIdBytes,
              callIdStart.length() + 1,
              callIdEnd.length());

          fireElement(header, CALL_ID, SINGLE_VALUE, callIdBytes, 0, callIdBytes.length);
          headerListener.headerFound(CALL_ID, callIdBytes, 0, callIdBytes.length, true);

        } else {
          fireElement(
              header,
              CALL_ID,
              SINGLE_VALUE,
              callIdStart.data(),
              callIdStart.offset(),
              callIdStart.length());
          headerListener.headerFound(
              CALL_ID, callIdStart.data(), callIdStart.offset(), callIdStart.length(), true);
        }

      } else {
        // arbitrary encoding
        // todo I am assuming that in the arbitrary case, I'm getting text ONLY!!!
        byte[] data = decodeRawHeaderData(dictionary, mb, messageDictionary);

        if (data != null) {
          DsSipMsgParser.parseHeader(headerListener, headerType.getType(), data, 0, data.length);
        } else {
          headerListener.headerFound(headerType.getType(), DsByteString.EMPTY_BYTES, 0, 0, true);
        }
      }
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, CALL_ID, mb.msg, 0, mb.msg.length);
    }
  }

  // todo talk to jsm about how to fire events for cseq
  static void parseCSeq(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      HeaderEncodingTypeHolder headerTypeHolder,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    try {

      int headerType = headerTypeHolder.getType();

      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        throw generateDsSipParserException(
            headerListener,
            "Cannot do a lazy parse of encoded messages",
            headerType,
            mb.msg,
            0,
            mb.msg.length);
      }

      if (headerTypeHolder.isFixedFormat()) {
        // cseq number
        long cSeqNumber = DsTokenSipInteger.read32Bit(mb);
        byte[] cSeqNumberBytes = DsIntStrCache.intToBytes(cSeqNumber);
        fireElement(header, headerType, CSEQ_NUMBER, cSeqNumberBytes, 0, cSeqNumberBytes.length);

        if (Log.isDebugEnabled()) Log.debug("CSeq number is " + cSeqNumber);

        // cseq method
        DsByteString cseqMethod = messageDictionary.getMethodShortcut(mb);
        fireElement(
            header,
            headerType,
            CSEQ_METHOD,
            cseqMethod.data(),
            cseqMethod.offset(),
            cseqMethod.length());
        if (Log.isDebugEnabled()) Log.debug("CSeq method is " + (cseqMethod));

        headerListener.headerFound(headerType, DsByteString.EMPTY_BYTES, 0, 0, true);
      } else {
        // todo non-fixed cseq parsing
        byte[] cseqData = decodeRawHeaderData(dictionary, mb, messageDictionary);

        if (cseqData != null) {
          DsSipMsgParser.parseHeader(headerListener, cseqData, 0, cseqData.length);
        } else {
          headerListener.headerFound(headerType, DsByteString.EMPTY_BYTES, 0, 0, true);
        }
      }
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(
          headerListener, e, headerTypeHolder.getType(), mb.msg, 0, mb.msg.length);
    }
  }

  private static final byte[] decodeRawHeaderData(
      DsTokenSipDictionary dictionary, MsgBytes mb, DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException {
    if (mb.i >= mb.msg.length) {
      return TOKEN_SIP_EMPTY_TOKEN;
    }
    int encodingType = getNextTokenType(mb.msg[mb.i]);
    if (encodingType != DATA_TOKEN) {
      return null;
      // throw new DsSipParserListenerException("Generic call-id must be string encoded");
    }
    // header is string encoded
    // DsByteString data = new DsByteString(getNextToken(mb, messageDictionary));
    ByteBuffer data = ByteBuffer.newInstance();

    while ((mb.i < mb.msg.length) && (getNextTokenType(mb.msg[mb.i]) == DATA_TOKEN)) {
      writeNextToken(dictionary, data, mb, messageDictionary);
    }

    return data.toByteArray();
  }

  /*
      static void parseWarning(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
          throws DsSipParserListenerException, DsSipParserException
      {
          try
          {
              int index = offset;
              int end = offset + count;

              while (true)
              {
                  int startHeader = index;
                  DsSipElementListener header = headerListener.headerBegin(headerType);
                  if (header == null)
                  {
                      headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
                      return; // done - lazy parse only
                  }

                  boolean foundComma = false;
                  int commaIndex = -1;

                  // move to first non-WS char
                  index = lws(data, index, end);

                  // handle empty case
                  if (index == end)
                  {
                      headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
                      return;
                  }

                  // handle empty element - ",,"
                  if (data[index] == ',')
                  {
                      //ignore this empty element
                      index++;
                      continue;
                  }

                  // mark the start of the data
                  int start = index;

                  byte ch = data[index++];
                  while (index < end && ch != ' ')
                  {
                      // just find the SP

                      ch = data[index++];
                  }

                  fireElement(header, headerType, WARN_CODE, data, start, index - start - 1);

                  start = index;
                  ch = data[index];
                  boolean foundHost = false;
                  while (index < end && ch != ' ')
                  {
                      if (ch == ':') // host with a port
                      {
                          foundHost = true;
                          fireElement(header, headerType, HOST, data, start, index - start - 1);
                          start = index;
                          while (index < end && data[index++] != ' ')
                          {
                          }

                          fireElement(header, headerType, PORT, data, start, index - start - 1);
                          break;
                      }

                      ch = data[index++];
                  }

                  if (!foundHost)
                  {
                      fireElement(header, headerType, HOST, data, start, index - start - 1);
                  }

                  start = index;
                  ch = data[index++];

                  if (ch != '"') // exception
                  {
                      // throw exception
                  }

                  index = readToCloseQuote(data, index, end);

                  fireElement(header, headerType, WARN_TEXT, data, start, index - start);

                  index = lws(data, index, end);

                  if (index == end)
                  {
                      headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
                      return; // done parsing
                  }

                  headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);

                  if (data[index] == ',')
                  {
                      index++;
                  }
                  else
                  {
                      return;
                  }
              }
          }
          //catch (DsSipParserException e)
          //{
              //throw e;
          //}
          catch (DsSipParserListenerException e)
          {
              throw e;
          }
          catch (Exception e)
          {
              throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
          }
      }
  */

  static void parseVia(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      HeaderEncodingTypeHolder headerTypeHolder,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_VIA);
    if (Log.isDebugEnabled()) Log.debug("Starting Via parse");
    int start = mb.i;

    try {
      int headerType = headerTypeHolder.getType();
      if (headerTypeHolder.isFixedFormat()) {
        if (Log.isDebugEnabled()) Log.debug("Starting Via parse fixed format");

        // highly optimized parse
        DsSipElementListener header = headerListener.headerBegin(headerType);
        if (header == null) {
          throw generateDsSipParserException(
              headerListener,
              "Cannot do a lazy parse of encoded messages",
              headerType,
              mb.msg,
              0,
              mb.msg.length);
        }

        // got the protocol stuff
        fireElement(
            header, headerType, PROTOCOL_NAME, s_ProtocolBytes, s_ProtocolStart, s_ProtocolLength);
        fireElement(
            header, headerType, PROTOCOL_VERSION, s_ProtocolBytes, s_VersionStart, s_VersionLength);
        fireElement(header, headerType, TRANSPORT, s_UDB_BYTES, s_UDP_START, s_UDP_LENGTH);

        // Now comes the host and port
        DsByteString hostAndPortBytes = messageDictionary.get(mb);

        if (hostAndPortBytes == null) {
          throw generateDsSipParserException(
              headerListener,
              "Cannot do a lazy parse of encoded messages",
              headerType,
              mb.msg,
              0,
              mb.msg.length);
        }

        // todo better to avoid creating this DsByteString

        int startOfPort =
            findByte(
                hostAndPortBytes.data(),
                hostAndPortBytes.offset(),
                hostAndPortBytes.length(),
                (byte) ':');
        if (startOfPort < 1) {
          // just a host
          fireElement(
              header,
              headerType,
              HOST,
              hostAndPortBytes.data(),
              hostAndPortBytes.offset(),
              hostAndPortBytes.length());
          if (Log.isDebugEnabled()) Log.debug("Via parse host is " + hostAndPortBytes);
        } else {
          // host and port
          fireElement(
              header,
              headerType,
              HOST,
              hostAndPortBytes.data(),
              hostAndPortBytes.offset(),
              startOfPort - hostAndPortBytes.offset());
          fireElement(
              header,
              headerType,
              PORT,
              hostAndPortBytes.data(),
              startOfPort + 1,
              hostAndPortBytes.length() + hostAndPortBytes.offset() - startOfPort - 1);
          if (Log.isDebugEnabled()) Log.debug("Via parse host and port is " + hostAndPortBytes);
        }

        // now we have params.  The following are not supported -
        //     ',' - end of header, start new one
        //     '(' - comment

        // branch param first
        if (mb.msg[mb.i] != TOKEN_SIP_NULL) {

          DsByteString branchData = messageDictionary.get(mb);
          byte[] branchParam = new byte[s_ViaBranchNameStartBytes.length + branchData.length()];
          System.arraycopy(
              s_ViaBranchNameStartBytes, 0, branchParam, 0, s_ViaBranchNameStartBytes.length);
          System.arraycopy(
              branchData.data(),
              branchData.offset(),
              branchParam,
              s_ViaBranchNameStartBytes.length,
              branchData.length());
          fireParameter(
              header,
              headerType,
              branchParam,
              0,
              s_ViaBranchParamNameLength,
              s_ViaBranchParamNameLength + 1,
              s_ViaBranchValueStartBytes.length + branchData.length());
          if (Log.isDebugEnabled()) Log.debug("Via parse branch param is " + branchData);
        } else {
          mb.i++;
        }

        // tok param next
        if (mb.msg[mb.i] != TOKEN_SIP_NULL) {
          DsByteString tokenData = messageDictionary.get(mb);
          byte[] tokenParam = new byte[s_ViaTokenNameStartBytes.length + tokenData.length()];
          System.arraycopy(
              s_ViaTokenNameStartBytes, 0, tokenParam, 0, s_ViaTokenNameStartBytes.length);
          System.arraycopy(
              tokenData.data(),
              tokenData.offset(),
              tokenParam,
              s_ViaTokenNameStartBytes.length,
              tokenData.length());
          fireParameter(
              header,
              headerType,
              tokenParam,
              0,
              s_ViaTokenNameStartBytes.length - 1,
              s_ViaTokenNameStartBytes.length,
              tokenData.length());
        } else {
          mb.i++;
        }

        // todo - is this last param correct???
        parseParameters(dictionary, header, mb, messageDictionary, SIP_URL_ID);

        // todo what to do with the hostAndPortBytes and offsets in this call???
        headerListener.headerFound(headerType, DsByteString.EMPTY_BYTES, 0, 0, true);
        if (Log.isDebugEnabled())
          Log.debug("Leaving Via parse.  With token after " + (mb.i - start));

        if (DsPerf.ON) DsPerf.stop(PARSE_VIA);
      } // done fixed format
      else {
        // generic (AKA slow) via parsing

        // maybe it's strings
        int encodingType = getNextTokenType(mb.msg[mb.i]);
        if (encodingType == DATA_TOKEN) {
          // header is string encoded
          DsByteString data = new DsByteString(getNextToken(dictionary, mb, messageDictionary));
          while (getNextTokenType(mb.msg[mb.i]) == DATA_TOKEN) {
            data.append(getNextToken(dictionary, mb, messageDictionary));
          }

          // skip URIs

          if (getContextChangeType(mb.msg[mb.i]) == PARAMETER_CONTEXT) {
            constructParameters(dictionary, data, mb, messageDictionary);
          }

          if (getContextChangeType(mb.msg[mb.i]) == NEW_DATA_FIELD_CONTEXT) {
            DsSipMsgParser.parseHeader(headerListener, VIA, data.toByteArray(), 0, data.length());
          } else {
            // todo I don't know what to do here.
            throw new DsSipParserException(
                "Unsupported token combinations in generic via decoding");
          }
        }
      }
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(
          headerListener, e, headerTypeHolder.getType(), mb.msg, 0, mb.msg.length);
    }
  }

  // todo This header parser is broken right now.  Use string header instead.
  static void parseUnknownHeader(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      HeaderEncodingTypeHolder headerType,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {

    DsByteString header = headerType.getName();
    DsByteString val = messageDictionary.get(mb);
    byte[] bytes = new byte[header.length() + 1 + val.length()];
    System.arraycopy(header.data(), header.offset(), bytes, 0, header.length());
    bytes[header.length()] = ':';
    System.arraycopy(val.data(), val.offset(), bytes, header.length() + 1, val.length());

    headerListener.unknownFound(bytes, 0, header.length(), header.length() + 1, val.length(), true);
  }

  // Authorization
  // Proxy-Authorization
  // Proxy-Authenticate
  // WWW-Authenticate
  // Authentication-Info?
  // todo Only Digest is supported
  static void parseProxyAuthorizationHeader(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      HeaderEncodingTypeHolder headerType,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(PROXY_AUTHORIZATION);
      if (header == null) {
        throw generateDsSipParserException(
            headerListener,
            "Cannot do a lazy parse of encoded messages",
            PROXY_AUTHORIZATION,
            mb.msg,
            0,
            mb.msg.length);
      }

      DsByteString userNamePart1 = messageDictionary.get(mb);
      DsByteString userNamePart2 = messageDictionary.get(mb);

      // should be "Digest"
      fireElement(
          header,
          PROXY_AUTHORIZATION,
          SINGLE_VALUE,
          s_DigestAuthenticationBytes,
          0,
          s_DigestAuthenticationBytes.length);

      if (userNamePart2 == null) {
        throw generateDsSipParserException(
            headerListener,
            "Malformed encoding.  Username null",
            PROXY_AUTHORIZATION,
            mb.msg,
            0,
            mb.msg.length);
      }

      ByteBuffer paramBuffer = ByteBuffer.newInstance();
      paramBuffer.write(s_DigestAuthUsernameParam);
      paramBuffer.write('"');

      if (userNamePart1 != null) {
        paramBuffer.write(userNamePart1);
        paramBuffer.write(s_AtSeparator);
      }
      paramBuffer.write(userNamePart2);
      paramBuffer.write('"');

      byte[] userName = paramBuffer.toByteArray();

      fireParameter(
          header,
          PROXY_AUTHORIZATION,
          userName,
          0,
          s_DigestAuthUsernameLength,
          s_DigestAuthUsernameLength + 1,
          userName.length - s_DigestAuthUsernameLength - 1);

      paramBuffer = ByteBuffer.newInstance();
      paramBuffer.write(s_DigestAuthRealmParam);
      paramBuffer.write('"');

      DsByteString realm = messageDictionary.get(mb);
      if (realm == null) {
        throw generateDsSipParserException(
            headerListener,
            "Malformed encoding.  Realm is null.",
            PROXY_AUTHORIZATION,
            mb.msg,
            0,
            mb.msg.length);
      }

      paramBuffer.write(realm);
      paramBuffer.write('"');

      byte[] realmValue = paramBuffer.toByteArray();

      fireParameter(
          header,
          PROXY_AUTHORIZATION,
          realmValue,
          0,
          s_DigestAuthRealmLength,
          s_DigestAuthRealmLength + 1,
          realm.length() + 2);

      paramBuffer = ByteBuffer.newInstance();
      paramBuffer.write(s_DigestAuthNonceParam);
      paramBuffer.write('"');

      DsByteString nonce = messageDictionary.get(mb);
      if (nonce == null) {
        throw generateDsSipParserException(
            headerListener,
            "Malformed encoding.  Nonce is null.",
            PROXY_AUTHORIZATION,
            mb.msg,
            0,
            mb.msg.length);
      }

      paramBuffer.write(nonce);
      paramBuffer.write('"');

      byte[] nonceValue = paramBuffer.toByteArray();

      fireParameter(
          header,
          PROXY_AUTHORIZATION,
          nonceValue,
          0,
          s_DigestAuthNonceLength,
          s_DigestAuthNonceLength + 1,
          nonce.length() + 2);

      // is this the URI Variant version???
      if (headerType.getFixedFormatType() == TOKEN_SIP_FIXED_FORMAT_AUTHORIZATION_VARIANT_HEADER) {
        paramBuffer = ByteBuffer.newInstance();
        paramBuffer.write(s_DigestAuthURIParam);
        paramBuffer.write('"');

        DsByteString uri = messageDictionary.get(mb);
        if (uri == null) {
          throw generateDsSipParserException(
              headerListener,
              "Malformed encoding.  URI is null.",
              PROXY_AUTHORIZATION,
              mb.msg,
              0,
              mb.msg.length);
        }

        paramBuffer.write(uri);
        paramBuffer.write('"');

        byte[] uriValue = paramBuffer.toByteArray();

        fireParameter(
            header,
            PROXY_AUTHORIZATION,
            uriValue,
            0,
            s_DigestAuthURILength,
            s_DigestAuthURILength + 1,
            uri.length() + 2);
      }

      DsByteString modifiedResponse = getHexData(mb.msg, mb.i, s_DigestAuthResponseLength);

      paramBuffer = ByteBuffer.newInstance();
      paramBuffer.write(s_DigestAuthResponseParam);
      paramBuffer.write('"');
      paramBuffer.write(modifiedResponse);
      paramBuffer.write('"');

      byte[] response = paramBuffer.toByteArray();

      fireParameter(
          header,
          PROXY_AUTHORIZATION,
          response,
          0,
          s_DigestAuthResponsePrefixLength - 1,
          s_DigestAuthResponsePrefixLength,
          modifiedResponse.length() + 2);

      headerListener.headerFound(PROXY_AUTHORIZATION, mb.msg, mb.i, mb.msg.length, true);
      mb.i += s_DigestAuthResponseLength;

    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(
          headerListener, e, PROXY_AUTHORIZATION, mb.msg, mb.i, mb.msg.length - mb.i);
    }
  }

  static void parseProxyAuthenticateHeader(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      DsSipElementListener header = headerListener.headerBegin(PROXY_AUTHENTICATE);

      if (header == null) {
        throw generateDsSipParserException(
            headerListener,
            "Cannot do a lazy parse of encoded messages",
            PROXY_AUTHENTICATE,
            mb.msg,
            0,
            mb.msg.length);
      }

      // should be "Digest"
      fireElement(
          header,
          PROXY_AUTHENTICATE,
          SINGLE_VALUE,
          s_DigestAuthenticationBytes,
          0,
          s_DigestAuthenticationBytes.length);

      DsByteString realm = messageDictionary.get(mb);
      if (realm == null) {
        throw generateDsSipParserException(
            headerListener,
            "Malformed encoding.  Realm is null.",
            PROXY_AUTHENTICATE,
            mb.msg,
            0,
            mb.msg.length);
      } else {
        ByteBuffer paramBuffer = ByteBuffer.newInstance();
        paramBuffer.write(s_DigestAuthRealmParam);
        paramBuffer.write('"');
        paramBuffer.write(realm);
        paramBuffer.write('"');
        byte[] paramValue = paramBuffer.toByteArray();

        fireParameter(
            header,
            PROXY_AUTHENTICATE,
            paramValue,
            0,
            s_DigestAuthRealmLength,
            s_DigestAuthRealmLength + 1,
            realm.length() + 2);
      }

      DsByteString domain = messageDictionary.get(mb);
      if (domain == null) {
        throw generateDsSipParserException(
            headerListener,
            "Malformed encoding.  Domain is null.",
            PROXY_AUTHENTICATE,
            mb.msg,
            0,
            mb.msg.length);
      } else {
        ByteBuffer paramBuffer = ByteBuffer.newInstance();
        paramBuffer.write(s_DigestAuthDomainParam);
        paramBuffer.write('"');
        paramBuffer.write(domain);
        paramBuffer.write('"');
        byte[] paramValue = paramBuffer.toByteArray();

        fireParameter(
            header,
            PROXY_AUTHENTICATE,
            paramValue,
            0,
            s_DigestAuthDomainLength,
            s_DigestAuthDomainLength + 1,
            domain.length() + 2);
      }

      DsByteString nonce = messageDictionary.get(mb);
      if (nonce == null) {
        throw generateDsSipParserException(
            headerListener,
            "Malformed encoding.  Nonce is null.",
            PROXY_AUTHENTICATE,
            mb.msg,
            0,
            mb.msg.length);
      } else {
        ByteBuffer paramBuffer = ByteBuffer.newInstance();
        paramBuffer.write(s_DigestAuthNonceParam);
        paramBuffer.write('"');
        paramBuffer.write(nonce);
        paramBuffer.write('"');
        byte[] paramValue = paramBuffer.toByteArray();

        fireParameter(
            header,
            PROXY_AUTHENTICATE,
            paramValue,
            0,
            s_DigestAuthNonceLength,
            s_DigestAuthNonceLength + 1,
            nonce.length() + 2);
      }

      headerListener.headerFound(PROXY_AUTHENTICATE, mb.msg, mb.i, mb.msg.length, true);
    } catch (DsSipParserException e) {
      throw e;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(
          headerListener, e, PROXY_AUTHENTICATE, mb.msg, mb.i, mb.msg.length - mb.i);
    }
  }

  /*
     static void parseAuth(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
             throws DsSipParserListenerException, DsSipParserException
         {
             try
             {
                 int index = offset;
                 int end = offset + count;

                 while (true)
                 {
                     int startHeader = index;
                     DsSipElementListener header = headerListener.headerBegin(headerType);
                     if (header == null)
                     {
                         headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
                         return; // done - lazy parse only
                     }

                     index = lws(data, index, end);

                     // handle empty case
                     if (index == end)
                     {
                         headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
                         return;
                     }

                     // handle empty element - ",,"
                     if (data[index] == ',')
                     {
                         //ignore this empty element
                         index++;
                         continue;
                     }

                     int start = index;

                     int commaIndex = - 1;
                     byte ch;

                     // find the WS separator for the token
                     while (index < end && data[index++] > ' ')
                     {
                     }

                     // index == end?

                     // should be "Digest"
                     fireElement(header, headerType, SINGLE_VALUE, data, start, index - start - 1);

                     index = lws(data, index, end);

                     // check to see if this is "Basic"
                     if (index - start >= 5 &&
                         data[start    ] == 'B' &&
                         data[start + 1] == 'a' &&
                         data[start + 2] == 's' &&
                         data[start + 3] == 'i' &&
                         data[start + 4] == 'c')
                     {
                         start = index;

                         while (true)
                         {
                             ch = data[index++];
                             if (ch <= ' ')
                             {
                                 fireElement(header, headerType, BASIC_COOKIE, data, start, index - start - 1);

                                 // check for more headers
                                 index = lws(data, index, end);
                                 if (index >= end)
                                 {
                                     // no more headers
                                     headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
                                     return;
                                 }
                                 else if (data[index] == ',')
                                 {
                                     // there is another header
                                     headerListener.headerFound(headerType, data, startHeader, index - startHeader - 1, true);
                                     index++;
                                     break;
                                 }
                                 else
                                 {
                                     throw generateDsSipParserException(headerListener, "Illegal: Extra characters found after Basic Cookie", headerType, data, offset, count);
                                 }
                             }
                             else if (ch == ',')
                             {
                                 fireElement(header, headerType, BASIC_COOKIE, data, start, index - start - 1);

                                 // there is another header
                                 headerListener.headerFound(headerType, data, startHeader, index - startHeader - 1, true);
                                 break;
                             }
                             else if (index == end)
                             {
                                 fireElement(header, headerType, BASIC_COOKIE, data, start, index - start);

                                 // no more headers
                                 headerListener.headerFound(headerType, data, startHeader, end - startHeader, true);
                                 return;
                             }
                         }

                         continue;
                     }

                     // if we get here, it is Digest or other, but not Basic.

                     // Ready to look for params
                     // Params are different here, they start with WS and then are separated by ','
                     boolean inName = true;

                     boolean foundName = false;
                     boolean foundValue = false;
                     int nameCount = -1;
                     int valueCount = -1;

                     int startName = index;
                     int startValue = -1;

                     boolean moreHeaders = false;
                     while (index < end && !moreHeaders)  // still stuff to check for?
                     {
                         switch (data[index++])
                         {
                             case ',':

                                 commaIndex = index - 1;

                                 // begin new param
                                 // end name or value
                                 if (inName)
                                 {
                                     if (!foundName) // name not set yet
                                     {
                                         foundName = true;
                                         nameCount = index - startName - 1;
                                     }
                                 }
                                 else
                                 {
                                     inName = true;
                                     if (!foundValue) // value not set yet
                                     {
                                         foundValue = true;
                                         valueCount = index - startValue - 1;
                                     }
                                 }
                                 // inName is always true at this point

                                 fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);

                                 index = lws(data, index, end);

                                 startName = index;
                                 nameCount = -1;
                                 valueCount = -1;
                                 foundName = false;
                                 foundValue = false;


                                 // since the grammar allows for comma separates list of comma separated lists,
                                 // we need to look ahead every time we find a ',' to see if there are more parameters
                                 // for this header or if there is a new header

                                 // first char of next token
                                 int lookAheadIndex = lws(data, index, end);
                                 ch = data[lookAheadIndex];

                                 // find out if there is WS
                                 // if there is then there is more work to do to make a determination
                                 // if an '=' or a ',' is found then we know there are more parameters
                                 while (lookAheadIndex < end &&
                                        ch > ' ' &&
                                        ch != '=' &&
                                        ch != ','
                                       )
                                 {
                                     ch = data[lookAheadIndex++];
                                 }

                                 if (ch != '=' && ch != ',') // check next non-WS char
                                 {
                                     lookAheadIndex = lws(data, lookAheadIndex, end);

                                     ch = data[lookAheadIndex];

                                     if (ch != '=' && ch != ',') // if this is true, then we have a new header
                                     {
                                          moreHeaders = true;
                                     }
                                 }

                                 break;
                             case '=':
                                 // end name
                                 // begin value
                                 if (!foundName) // name not set yet
                                 {
                                     foundName = true;

                                     nameCount = index - startName - 1;
                                 }
                                 inName = false; // switch to value

                                 index = lws(data, index, end);

                                 startValue = index;
                                 break;
                             case '"':
                                 // quotes allowed in values only
                                 if (!inName)
                                 {
                                     index = readToCloseQuote(data, index, end);

                                     // if we are at the end, the value will get created below - don't create it twice
                                     if (index != end)
                                     {
                                         foundValue = true;
                                         valueCount = index - startValue;
                                     }
                                 }
                                 else
                                 {
                                     throw generateDsSipParserException(headerListener, "Illegal: '\"' found in parameter name", headerType, data, offset, count);
                                 }
                                 break;
                             case ' ':
                             case '\t':
                             case '\r':
                             case '\n':
                                 // end name or value
                                 if (inName && !foundName)
                                 {
                                     foundName = true;

                                     nameCount = index - startName - 1;
                                 }
                                 else if (!inName && !foundValue)
                                 {
                                     foundValue = true;

                                     valueCount = index - startValue - 1;
                                 }

                                 // or just skip if already ended

                                 break;
                         }

                         // The last case that the switch statement cannot handle
                         // We must get the last string (name or value here)
                         // Too hard to do outside of loop, since we may or maynot get into the loop
                         if (index == end)
                         {
                             if (inName)
                             {
                                 if (!foundName)
                                 {
                                     foundName = true;

                                     nameCount = index - startName;
                                 }

                                 fireParameter(header, headerType, data, startName, nameCount, 0, 0);
                             }
                             else
                             {
                                 if (!foundValue)
                                 {
                                     foundValue = true;

                                     valueCount = index - startValue;
                                 }

                                 fireParameter(header, headerType, data, startName, nameCount, startValue, valueCount);
                             }
                         }
                     }

                     if (moreHeaders)
                     {
                         headerListener.headerFound(headerType, data, startHeader, commaIndex - startHeader, true);
                     }
                     else
                     {
                         headerListener.headerFound(headerType, data, startHeader, index - startHeader, true);
                         return;
                     }
                 }
             }
             catch (DsSipParserException e)
             {
                 throw e;
             }
             catch (DsSipParserListenerException e)
             {
                 throw e;
             }
             catch (Exception e)
             {
                 throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
             }
         }
  */

  // Accept-Encoding
  // Accept-Language - language can be deeply parsed later
  // Content-Disposition
  // Content-Language - language can be deeply parsed later
  // Service-Agent-Phase
  // Service-Agent-Context
  // Service-Agent-Application
  // Event
  // more
  // also - token headers w/o params
  static void parseTokenListWithParams(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      HeaderEncodingTypeHolder headerTypeHolder,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    int headerType = headerTypeHolder.getType();

    try {
      byte[] headerVal = decodeRawHeaderData(dictionary, mb, messageDictionary);

      DsSipElementListener header = headerListener.headerBegin(headerType);

      if (header == null) {
        throw generateDsSipParserException(
            headerListener,
            "Cannot do a lazy parse of encoded messages",
            headerType,
            mb.msg,
            0,
            mb.msg.length);
      } else {
        int valLength;
        if (headerVal == null) {
          headerVal = DsByteString.EMPTY_BYTES;
          valLength = 0;
        } else {
          valLength = headerVal.length;
        }
        fireElement(header, headerType, SINGLE_VALUE, headerVal, 0, valLength);
        parseParameters(dictionary, header, mb, messageDictionary, headerType);
        headerListener.headerFound(headerType, headerVal, 0, valLength, true);
      }

    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, mb.msg, 0, mb.msg.length);
    }
  }

  // Timestamp
  /*
      static void parseTimestamp(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
          throws DsSipParserListenerException, DsSipParserException
      {
          try
          {
              DsSipElementListener header = headerListener.headerBegin(headerType);
              if (header == null)
              {
                  headerListener.headerFound(headerType, data, offset, count, true);
                  return; // done - lazy parse only
              }

              // end points 1 past the last slot, treat it like .length
              int end = count + offset;
              int index = lws(data, offset, end);
              int start = index;

              // skip any leading white space
              while (index < end && data[index++] > ' ')
              {
              }

              if (index == end && data[index - 1] > ' ')
              {
                  fireElement(header, headerType, SINGLE_VALUE, data, start, index - start);
              }
              else
              {
                  fireElement(header, headerType, SINGLE_VALUE, data, start, index - start - 1);
              }

              start = index = lws(data, index, end);

              if (index < end)
              {
                  while (index < end && data[index++] > ' ')
                  {
                  }

                  if (index == end && data[index - 1] > ' ')
                  {
                      fireElement(header, headerType, DELAY, data, start, index - start);
                  }
                  else
                  {
                      fireElement(header, headerType, DELAY, data, start, index - start - 1);
                  }
              }

              headerListener.headerFound(headerType, data, offset, count, true);
          }
          //catch (DsSipParserException e)
          //{
              //throw e;
          //}
          catch (DsSipParserListenerException e)
          {
              throw e;
          }
          catch (Exception e)
          {
              throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
          }
      }
  */
  // Server
  // User-Agent

  static void parseServerUserAgent(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      int headerType,
      MsgBytes mb,
      DsTokenSipMessageDictionary md)
      throws DsSipParserListenerException, DsSipParserException {

    byte[] data = decodeRawHeaderData(dictionary, mb, md);

    if (data != null) {
      DsSipMsgParser.parseHeader(headerListener, headerType, data, 0, data.length);
    } else {
      DsSipMsgParser.parseHeader(headerListener, headerType, data, 0, 0);
    }
  }
  // Accept
  // Content-Type

  static void parseMediaType(
      DsTokenSipDictionary dictionary,
      DsSipHeaderListener headerListener,
      int headerType,
      MsgBytes mb,
      DsTokenSipMessageDictionary md)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      byte[] contentBytes = decodeRawHeaderData(dictionary, mb, md);

      DsSipElementListener header = headerListener.headerBegin(headerType);
      if (header == null) {
        throw generateDsSipParserException(
            headerListener,
            "Cannot do a lazy parse of encoded messages",
            headerType,
            mb.msg,
            0,
            mb.msg.length);
      }

      // todo optimization for finding the / in the byte[] rather than in DsByteString
      int typeSep = findByte(contentBytes, 0, contentBytes.length, B_SLASH);
      if (typeSep > 0) {
        fireElement(header, headerType, TYPE, contentBytes, 0, typeSep);
        fireElement(
            header,
            headerType,
            SUB_TYPE,
            contentBytes,
            typeSep + 1,
            contentBytes.length - typeSep - 1);
      } else {
        fireElement(header, headerType, TYPE, contentBytes, 0, contentBytes.length);
      }

      parseParameters(dictionary, header, mb, md, headerType);

      headerListener.headerFound(headerType, contentBytes, 0, contentBytes.length, true);

    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(headerListener, e, headerType, mb.msg, 0, mb.msg.length);
    }
  }

  /*    // Date
      static void parseDate(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
          throws DsSipParserListenerException, DsSipParserException
      {
          try
          {
              DsSipElementListener header = headerListener.headerBegin(headerType);
              if (header == null)
              {
                  headerListener.headerFound(headerType, data, offset, count, true);
                  return; // done - lazy parse only
              }

              int end = count + offset - 1;
              int index = lws(data, offset, end);
              int start = index;

              if (start + SIP_DATE_LENGTH > end)
              {
                  // throw exception
              }

              fireElement(header, headerType, SINGLE_VALUE, data, start, SIP_DATE_LENGTH);

              headerListener.headerFound(headerType, data, offset, count, true);
          }
          //catch (DsSipParserException e)
          //{
              //throw e;
          //}
          catch (DsSipParserListenerException e)
          {
              throw e;
          }
          catch (Exception e)
          {
              throw generateDsSipParserException(headerListener, e, headerType, data, offset, count);
          }
      }
  */

  private static void parseEventPackage(
      DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      int end = offset + count;
      int index = offset; // lws(data, offset, end);
      int start = index;

      int elementType = EVENT_PACKAGE;

      if (element == null) {
        throw generateDsSipParserException(
            element,
            "Cannot do a lazy parse of encoded messages",
            headerType,
            elementType,
            data,
            0,
            data.length);
      }

      while (index <= end) {
        while (index < end && data[index++] != '.') {}

        if (index < end) {
          fireElement(element, headerType, elementType, data, start, index - start - 1);
        } else {
          fireElement(element, headerType, elementType, data, start, index - start);
          return;
        }

        start = index;

        // ch = data[index++];
        elementType = EVENT_SUB_PACKAGE;
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, headerType, SINGLE_VALUE, data, offset, count);
    }
  }

  // called from Date, Retry-After, Expires and expires parameter

  // Sat, 13 Nov 2001 23:29:00 GMT
  // 01234567890123456789012345678
  //          1         2
  private static void parseSipDate(
      DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      // check count? - jsm

      int start = offset;

      fireElement(element, headerType, DAY_OF_WEEK, data, start, 3);

      // next comes ", " - ignore

      // next comes 2 digit day of month
      fireElement(element, headerType, DAY_OF_MONTH, data, start + 5, 2);

      // the next three chars are the month
      fireElement(element, headerType, MONTH, data, start + 8, 3);

      // index 11 is a space and 12-15 are the 4 digit year
      fireElement(element, headerType, YEAR, data, start + 12, 4);

      // index 16 is a space and 17-18 are the 2 digit hour
      fireElement(element, headerType, HOUR, data, start + 17, 2);

      // index 19 is a ':' and 20-21 are the 2 digit minute
      fireElement(element, headerType, MINUTE, data, start + 20, 2);

      // index 22 is a ':' and 23-24 are the 2 digit second
      fireElement(element, headerType, SECOND, data, start + 23, 2);

      // 26-28 must be "GMT"
      if (data[start + 26] == 'G' && data[start + 27] == 'M' && data[start + 28] == 'T') {
        // assume this as default? - jsm
        fireElement(element, headerType, TIME_ZONE, data, start + 26, 3);
      } else {
        // throw exception...
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, headerType, SIP_DATE, data, offset, count);
    }
  }

  // called from Accept-Language and Content-Language.
  private static void parseLanguage(
      DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    try {
      if (element == null) {
        return; // done - lazy parse only
      }

      int end = offset + count;
      int index = offset;
      int start;

      byte ch;
      int elementId = LANGUAGE_TAG;

      while (true) {
        start = index;
        ch = data[index++];
        // check if WS is allowed or not - jsm
        while (index < end && ch > ' ' && ch != '-') {
          ch = data[index++];
        }

        if (index == end) {
          fireElement(element, headerType, elementId, data, start, index - start);
          return;
        } else {
          fireElement(element, headerType, elementId, data, start, index - start - 1);

          // the rest are subtags
          elementId = LANGUAGE_SUBTAG;
        }

        if (ch != '-') {
          return; // only continue if there is another subtag
        }
      }
    }
    // catch (DsSipParserException e)
    // {
    // throw e;
    // }
    catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(element, e, headerType, SINGLE_VALUE, data, offset, count);
    }
  }

  /**
   * Parses a Name Address. Reports the results to the listener. This parser fires events for the
   * following element IDs:
   *
   * <blockquote>
   *
   * DISPLAY_NAME<br>
   * URI<br>
   *
   * </blockquote>
   *
   * @param element where to report the results.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parseNameAddr(
      DsTokenSipDictionary dictionary,
      DsSipElementListener element,
      int headerType,
      MsgBytes mb,
      DsTokenSipNameAddressEncoder nameAddrFlags,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_NAME_ADDR);

    try {
      if (element == null) {
        if (DsPerf.ON) DsPerf.stop(PARSE_NAME_ADDR);
        throw generateDsSipParserException(
            null, "Cannot do a lazy parse of encoded messages", 0, mb.msg, 0, mb.msg.length);
      }

      if (nameAddrFlags.isDisplayNameSet()) {
        // todo this is WRONG WRONG WRONG
        DsByteString displayName = messageDictionary.get(mb);
        fireElement(
            element,
            NAME_ADDR_ID,
            DISPLAY_NAME,
            displayName.data(),
            displayName.offset(),
            displayName.length());
      }

      // todo I can either fireElement or parseURI.
      // fireElement(element, NAME_ADDR_ID, URI, mb, start, index - start + 1);

      parseURI(dictionary, element, mb, headerType, nameAddrFlags, messageDictionary);

      if (DsPerf.ON) DsPerf.stop(PARSE_NAME_ADDR);
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(
          element, e, NAME_ADDR_ID, NAME_ADDR, mb.msg, 0, mb.msg.length);
    }
  }

  public static void parseURI(
      DsTokenSipDictionary dictionary,
      DsSipElementListener element,
      MsgBytes mb,
      int headerType,
      DsTokenSipNameAddressEncoder uriFlags,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipElementListener subElement;

    switch (uriFlags.getScheme()) {
      case SIP_URL_ID:
        // sip url
        subElement = element.elementBegin(headerType, SIP_URL);
        parseSipUrl(dictionary, subElement, mb, uriFlags, messageDictionary);
        element.elementFound(headerType, SIP_URL, mb.msg, 0, mb.msg.length, true);

        break;
      case TEL_URL_ID:
        // tel uri
        subElement = element.elementBegin(headerType, TEL_URL);
        parseTelUrl(dictionary, subElement, mb, uriFlags, messageDictionary);
        element.elementFound(headerType, TEL_URL, mb.msg, 0, mb.msg.length, true);
        break;
      default:
        // some unhandled uri type
        subElement = element.elementBegin(headerType, UNKNOWN_URL);
        DsByteString scheme = messageDictionary.get(mb);
        DsByteString val = messageDictionary.get(mb);
        fireElement(
            subElement,
            UNKNOWN_URL_ID,
            URI_SCHEME,
            scheme.data(),
            scheme.offset(),
            scheme.length());
        fireElement(subElement, UNKNOWN_URL_ID, URI_DATA, val.data(), val.offset(), val.length());

        element.elementFound(headerType, UNKNOWN_URL, mb.msg, 0, mb.msg.length, true);
    }
  }

  public static void parseTelUrl(
      DsTokenSipDictionary dictionary,
      DsSipElementListener element,
      MsgBytes mb,
      DsTokenSipNameAddressEncoder uriFlags,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    // todo  Implement Tel URL parse

    fireElement(
        element,
        TEL_URL_ID,
        URI_SCHEME,
        DS_TOKEN_SIP_TEL_SCHEME,
        0,
        DS_TOKEN_SIP_TEL_SCHEME.length);
    DsByteString telNumber = messageDictionary.get(mb);
    fireElement(
        element,
        TEL_URL_ID,
        TEL_URL_NUMBER,
        telNumber.data(),
        telNumber.offset(),
        telNumber.length());
    if (uriFlags.isNonTagParams()) {
      parseURIParameters(dictionary, element, mb, messageDictionary, TEL_URL_ID);
    }
  }

  public static void parseSipUrl(
      DsTokenSipDictionary dictionary,
      DsSipElementListener element,
      MsgBytes mb,
      DsTokenSipNameAddressEncoder uriFlags,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {

    if (Log.isDebugEnabled()) Log.debug("Starting SIP URL Parse");

    DsByteString user = messageDictionary.get(mb);

    if (uriFlags.isTwoUserParts() == false) {
      if (user != null) {
        if (Log.isDebugEnabled()) Log.debug("SIP URL Parse user is - " + user);
        fireElement(element, SIP_URL_ID, USERNAME, user.data(), user.offset(), user.length());
      }
    } else {
      DsByteString secondUserPart = messageDictionary.get(mb);
      if (user != null) {
        byte[] twoPartUser =
            new byte
                [user.length()
                    + TOKEN_SIP_TWO_PART_USER_SEPERATOR.length()
                    + secondUserPart.length()];
        System.arraycopy(user.data(), user.offset(), twoPartUser, 0, user.length());
        System.arraycopy(
            TOKEN_SIP_TWO_PART_USER_SEPERATOR.data(),
            0,
            twoPartUser,
            user.length(),
            TOKEN_SIP_TWO_PART_USER_SEPERATOR.length());
        System.arraycopy(
            secondUserPart.data(),
            secondUserPart.offset(),
            twoPartUser,
            user.length() + TOKEN_SIP_TWO_PART_USER_SEPERATOR.length(),
            secondUserPart.length());

        if (Log.isDebugEnabled()) {
          Log.debug("SIP URL Parse two part user is - " + new DsByteString(twoPartUser));
        }

        fireElement(element, SIP_URL_ID, USERNAME, twoPartUser, 0, twoPartUser.length);
      } else {
        if (secondUserPart != null) {
          if (Log.isDebugEnabled()) {
            Log.debug("SIP URL Parse two part user is - " + new DsByteString(secondUserPart));
          }

          fireElement(
              element,
              SIP_URL_ID,
              USERNAME,
              secondUserPart.data(),
              secondUserPart.offset(),
              secondUserPart.length());
        } else {
          if (Log.isDebugEnabled()) {
            Log.debug("SIP URL Parser - user is empty.");
          }
        }
      }
    }

    DsByteString host = messageDictionary.get(mb);
    if (Log.isDebugEnabled()) Log.debug("SIP URL Parse host is - " + host);
    fireElement(element, SIP_URL_ID, HOST, host.data(), host.offset(), host.length());

    if (uriFlags.isPortSpecified()) {
      DsByteString portStr = DsByteString.valueOf(DsTokenSipInteger.read16Bit(mb));
      if (Log.isDebugEnabled()) Log.debug("SIP URL Parse port is - " + portStr);
      fireElement(element, SIP_URL_ID, PORT, portStr.data(), portStr.offset(), portStr.length());
    }

    if (uriFlags.isNonTagParams()) {
      if (Log.isDebugEnabled()) Log.debug("SIP URL Parse getting params");
      parseURIParameters(dictionary, element, mb, messageDictionary, SIP_URL);
    }
  }

  public static boolean isParameter(byte indicator) {
    if ((indicator >= TOKEN_SIP_PARAM_MIN) && (indicator <= TOKEN_SIP_PARAM_MAX)) {
      return true;
    }
    return false;
  }

  public static void parseParameters(
      DsTokenSipDictionary dictionary,
      DsSipElementListener element,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary,
      int urlId)
      throws DsSipParserListenerException, DsSipParserException {
    if (element == null) {
      // todo what to do here?
    }

    while ((mb.i < mb.msg.length) && (isParameter(mb.msg[mb.i]) == true)) {
      // DsTokenSipParamEncoder paramEncoder = new DsTokenSipParamEncoder(mb.msg[mb.i]);
      mb.i++;

      DsByteString paramName = messageDictionary.get(mb);
      DsByteString paramValue = messageDictionary.get(mb);

      // todo quotes in param value is broken here too

      if (paramValue == null) {
        fireParameter(
            element, urlId, paramName.data(), paramName.offset(), paramName.length(), 0, 0);
      } else {
        byte[] paramString = new byte[paramName.length() + paramValue.length()];
        System.arraycopy(paramName.data(), paramName.offset(), paramString, 0, paramName.length());
        System.arraycopy(
            paramValue.data(),
            paramValue.offset(),
            paramString,
            paramName.length(),
            paramValue.length());
        fireParameter(
            element,
            urlId,
            paramString,
            0,
            paramName.length(),
            paramName.length(),
            paramValue.length());
      }
    }
  }

  public static void constructParameters(
      DsTokenSipDictionary dictionary,
      DsByteString data,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary)
      throws DsSipParserListenerException, DsSipParserException {
    while (isParameter(mb.msg[mb.i]) == true) {
      mb.i++;

      DsByteString paramName = messageDictionary.get(mb);
      DsByteString paramValue = messageDictionary.get(mb);
      byte[] paramString;
      if (paramValue == null) {
        data.append(BS_SEMI);
        data.append(paramName);
      } else {
        paramString = new byte[paramName.length() + paramValue.length() + 1];
        System.arraycopy(paramName.data(), paramName.offset(), paramString, 0, paramName.length());
        paramString[paramName.length()] = (byte) '=';
        System.arraycopy(
            paramValue.data(),
            paramValue.offset(),
            paramString,
            paramName.length() + 1,
            paramValue.length());

        data.append(BS_SEMI);
        data.append(paramString);
      }
    }
  }

  public static void parseURIParameters(
      DsTokenSipDictionary dictionary,
      DsSipElementListener element,
      MsgBytes mb,
      DsTokenSipMessageDictionary messageDictionary,
      int urlId)
      throws DsSipParserListenerException, DsSipParserException {
    int numParams = mb.msg[mb.i++];
    for (int x = 0; x < numParams; x++) {
      if (isParameter(mb.msg[mb.i]) == false) {
        throw generateDsSipParserException(
            element,
            "Parameter count found, but no parameters present",
            urlId,
            urlId,
            mb.msg,
            0,
            mb.msg.length);
      }

      // todo check to see if this is a quoted value...  It is necessary
      mb.i++;

      DsByteString paramName = messageDictionary.get(mb);
      DsByteString paramValue = messageDictionary.get(mb);
      if (paramValue == null) {
        fireParameter(
            element, urlId, paramName.data(), paramName.offset(), paramName.length(), 0, 0);
      } else {
        byte[] paramString;
        paramString = new byte[paramName.length() + paramValue.length()];
        System.arraycopy(paramName.data(), paramName.offset(), paramString, 0, paramName.length());
        System.arraycopy(
            paramValue.data(),
            paramValue.offset(),
            paramString,
            paramName.length(),
            paramValue.length());
        fireParameter(
            element,
            urlId,
            paramString,
            0,
            paramName.length(),
            paramName.length(),
            paramValue.length());
      }
    }
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param element the listener to pass the elementFound() event to
   * @param msg the message to include in the exception
   * @param contextId the context (header, URI, etc) that was being parsed
   * @param elementId the element that was being parsed
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipElementListener element,
      String msg,
      int contextId,
      int elementId,
      byte[] data,
      int offset,
      int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(msg, contextId, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(msg, contextId, DsByteString.BS_EMPTY_STRING);
    }

    notifyElementListener(element, contextId, elementId, data, offset, count, validData);

    return newE;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param element the listener to pass the elementFound() event to
   * @param e the exception that caused this exception to be generated
   * @param contextId the context (header, URI, etc) that was being parsed
   * @param elementId the element that was being parsed
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipElementListener element,
      Exception e,
      int contextId,
      int elementId,
      byte[] data,
      int offset,
      int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(e, contextId, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(e, contextId, DsByteString.BS_EMPTY_STRING);
    }

    notifyElementListener(element, contextId, elementId, data, offset, count, validData);

    return newE;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param headerListener the listener to pass the headerFound() event to
   * @param msg the message to include in the exception
   * @param contextId the context (header, URI, etc) that was being parsed
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipHeaderListener headerListener,
      String msg,
      int contextId,
      byte[] data,
      int offset,
      int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(msg, contextId, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(msg, contextId, DsByteString.BS_EMPTY_STRING);
    }

    notifyHeaderListener(headerListener, contextId, data, offset, count, validData);

    return newE;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param headerListener the listener to pass the headerFound() event to
   * @param e the exception that caused this exception to be generated
   * @param contextId the context (header, URI, etc) that was being parsed
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipHeaderListener headerListener,
      Exception e,
      int contextId,
      byte[] data,
      int offset,
      int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(e, contextId, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(e, contextId, DsByteString.BS_EMPTY_STRING);
    }

    notifyHeaderListener(headerListener, contextId, data, offset, count, validData);

    return newE;
  }

  /**
   * Generate a DsSipParserException and add the header to header list.
   *
   * @param messageListener the listener to pass the headerFound() event to
   * @param e the exception that caused this exception to be generated
   * @param data the data being parsed
   * @param offset the start of the data in the array
   * @param count the number of chars in the array that belong to this value
   * @return the DsSipParserException generated by this method
   */
  public static DsSipParserException generateDsSipParserException(
      DsSipMessageListener messageListener, Exception e, byte[] data, int offset, int count) {
    boolean validData = isDataValid(data, offset, count);

    DsSipParserException newE;

    if (validData) {
      newE = new DsSipParserException(e, ENTIRE_MSG_ID, new DsByteString(data, offset, count));
    } else // data is null or offset/count goes out of bounds, just use ""
    {
      newE = new DsSipParserException(e, ENTIRE_MSG_ID, DsByteString.BS_EMPTY_STRING);
    }

    notifyMessageListener(messageListener, data, offset, count, validData);

    return newE;
  }

  private static boolean isDataValid(byte[] data, int offset, int count) {
    return (data != null
        && offset >= 0
        && offset < data.length
        && count >= 0
        && (offset + count) <= data.length);
  }

  private static void notifyMessageListener(
      DsSipMessageListener messageListener, byte[] data, int offset, int count, boolean validData) {
    try {
      if (messageListener != null) {
        if (validData) {
          messageListener.messageFound(data, offset, count, false);
        } else if (data != null) {
          messageListener.messageFound(data, 0, data.length, false);
        } else {
          messageListener.messageFound(new byte[0], 0, 0, false);
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

  private static void notifyHeaderListener(
      DsSipHeaderListener headerListener,
      int contextId,
      byte[] data,
      int offset,
      int count,
      boolean validData) {
    try {
      if (headerListener != null) {
        if (validData) {
          headerListener.headerFound(contextId, data, offset, count, false);
        } else if (data != null) {
          headerListener.headerFound(contextId, data, 0, data.length, false);
        } else {
          headerListener.headerFound(contextId, new byte[0], 0, 0, false);
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

  private static void notifyElementListener(
      DsSipElementListener element,
      int contextId,
      int elementId,
      byte[] data,
      int offset,
      int count,
      boolean validData) {
    try {
      if (element != null) {
        if (validData) {
          element.elementFound(contextId, elementId, data, offset, count, false);
        } else if (data != null) {
          element.elementFound(contextId, elementId, data, 0, data.length, false);
        } else {
          element.elementFound(contextId, elementId, new byte[0], 0, 0, false);
        }
      }
    } catch (Exception ex) {
      // ignore
    }
  }

  // data solely for the purpose of getNextTokenType(int i);

  public static final int DATA_TOKEN = 0;
  public static final int CONTEXT_TOKEN = 1;
  public static final int UNDEFINED_TOKEN = 2;

  private static final int[] m_tokenType = new int[256];

  static {
    m_tokenType[0x00] = UNDEFINED_TOKEN; // null
    m_tokenType[0x0b] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x50] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x51] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x52] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x53] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x54] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x55] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x56] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x57] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x58] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x59] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x5a] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x5b] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x5c] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x5d] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x5e] = UNDEFINED_TOKEN; // reserved
    m_tokenType[0x5f] = UNDEFINED_TOKEN; // reserved

    m_tokenType[0x08] = CONTEXT_TOKEN; // known header ptr
    m_tokenType[0x09] = CONTEXT_TOKEN; // unknown header ptr
    m_tokenType[0x0a] = CONTEXT_TOKEN; // comment
    m_tokenType[0x0c] = CONTEXT_TOKEN; // CRLF    end
    m_tokenType[0x0d] = CONTEXT_TOKEN; // CRLFCRLF   end

    m_tokenType[0x10] = CONTEXT_TOKEN; // name-addr/uri encoding
    m_tokenType[0x11] = CONTEXT_TOKEN;
    m_tokenType[0x12] = CONTEXT_TOKEN;
    m_tokenType[0x13] = CONTEXT_TOKEN;
    m_tokenType[0x14] = CONTEXT_TOKEN;
    m_tokenType[0x15] = CONTEXT_TOKEN;
    m_tokenType[0x16] = CONTEXT_TOKEN;
    m_tokenType[0x17] = CONTEXT_TOKEN;
    m_tokenType[0x18] = CONTEXT_TOKEN;
    m_tokenType[0x19] = CONTEXT_TOKEN;
    m_tokenType[0x1a] = CONTEXT_TOKEN;
    m_tokenType[0x1b] = CONTEXT_TOKEN;
    m_tokenType[0x1c] = CONTEXT_TOKEN;
    m_tokenType[0x1d] = CONTEXT_TOKEN;
    m_tokenType[0x1e] = CONTEXT_TOKEN;
    m_tokenType[0x1f] = CONTEXT_TOKEN;

    m_tokenType[0x40] = CONTEXT_TOKEN; // param encoding
    m_tokenType[0x41] = CONTEXT_TOKEN;
    m_tokenType[0x42] = CONTEXT_TOKEN;
    m_tokenType[0x43] = CONTEXT_TOKEN;
    m_tokenType[0x44] = CONTEXT_TOKEN;
    m_tokenType[0x45] = CONTEXT_TOKEN;
    m_tokenType[0x46] = CONTEXT_TOKEN;
    m_tokenType[0x47] = CONTEXT_TOKEN;
    m_tokenType[0x48] = CONTEXT_TOKEN;
    m_tokenType[0x49] = CONTEXT_TOKEN;
    m_tokenType[0x4a] = CONTEXT_TOKEN;
    m_tokenType[0x4b] = CONTEXT_TOKEN;
    m_tokenType[0x4c] = CONTEXT_TOKEN;
    m_tokenType[0x4d] = CONTEXT_TOKEN;
    m_tokenType[0x4e] = CONTEXT_TOKEN;
    m_tokenType[0x4f] = CONTEXT_TOKEN;

    m_tokenType[0x60] = CONTEXT_TOKEN; // fixed format encoding
    m_tokenType[0x61] = CONTEXT_TOKEN;
    m_tokenType[0x62] = CONTEXT_TOKEN;
    m_tokenType[0x63] = CONTEXT_TOKEN;
    m_tokenType[0x64] = CONTEXT_TOKEN;
    m_tokenType[0x65] = CONTEXT_TOKEN;
    m_tokenType[0x66] = CONTEXT_TOKEN;
    m_tokenType[0x67] = CONTEXT_TOKEN;
    m_tokenType[0x68] = CONTEXT_TOKEN;
    m_tokenType[0x69] = CONTEXT_TOKEN;
    m_tokenType[0x6a] = CONTEXT_TOKEN;
    m_tokenType[0x6b] = CONTEXT_TOKEN;
    m_tokenType[0x6c] = CONTEXT_TOKEN;
    m_tokenType[0x6d] = CONTEXT_TOKEN;
    m_tokenType[0x6e] = CONTEXT_TOKEN;
    m_tokenType[0x6f] = CONTEXT_TOKEN;
    m_tokenType[0x70] = CONTEXT_TOKEN;
    m_tokenType[0x71] = CONTEXT_TOKEN;
    m_tokenType[0x72] = CONTEXT_TOKEN;
    m_tokenType[0x73] = CONTEXT_TOKEN;
    m_tokenType[0x74] = CONTEXT_TOKEN;
    m_tokenType[0x75] = CONTEXT_TOKEN;
    m_tokenType[0x76] = CONTEXT_TOKEN;
    m_tokenType[0x77] = CONTEXT_TOKEN;
    m_tokenType[0x78] = CONTEXT_TOKEN;
    m_tokenType[0x79] = CONTEXT_TOKEN;
    m_tokenType[0x7a] = CONTEXT_TOKEN;
    m_tokenType[0x7b] = CONTEXT_TOKEN;
    m_tokenType[0x7c] = CONTEXT_TOKEN;
    m_tokenType[0x7d] = CONTEXT_TOKEN;
    m_tokenType[0x7e] = CONTEXT_TOKEN;
    m_tokenType[0x7f] = CONTEXT_TOKEN;

    m_tokenType[0xe0] = CONTEXT_TOKEN; // generic header starts
    m_tokenType[0xe1] = CONTEXT_TOKEN;
    m_tokenType[0xe2] = CONTEXT_TOKEN;
    m_tokenType[0xe3] = CONTEXT_TOKEN;
    m_tokenType[0xe4] = CONTEXT_TOKEN;
    m_tokenType[0xe5] = CONTEXT_TOKEN;
    m_tokenType[0xe6] = CONTEXT_TOKEN;
    m_tokenType[0xe7] = CONTEXT_TOKEN;
    m_tokenType[0xe8] = CONTEXT_TOKEN;
    m_tokenType[0xe9] = CONTEXT_TOKEN;
    m_tokenType[0xea] = CONTEXT_TOKEN;
    m_tokenType[0xeb] = CONTEXT_TOKEN;
    m_tokenType[0xec] = CONTEXT_TOKEN;
    m_tokenType[0xed] = CONTEXT_TOKEN;
    m_tokenType[0xee] = CONTEXT_TOKEN;
    m_tokenType[0xef] = CONTEXT_TOKEN;
    m_tokenType[0xf0] = CONTEXT_TOKEN;
    m_tokenType[0xf1] = CONTEXT_TOKEN;
    m_tokenType[0xf2] = CONTEXT_TOKEN;
    m_tokenType[0xf3] = CONTEXT_TOKEN;
    m_tokenType[0xf4] = CONTEXT_TOKEN;
    m_tokenType[0xf5] = CONTEXT_TOKEN;
    m_tokenType[0xf6] = CONTEXT_TOKEN;
    m_tokenType[0xf7] = CONTEXT_TOKEN;
    m_tokenType[0xf8] = CONTEXT_TOKEN;
    m_tokenType[0xf9] = CONTEXT_TOKEN;
    m_tokenType[0xfa] = CONTEXT_TOKEN;
    m_tokenType[0xfb] = CONTEXT_TOKEN;
    m_tokenType[0xfc] = CONTEXT_TOKEN;
    m_tokenType[0xfd] = CONTEXT_TOKEN;
    m_tokenType[0xfe] = CONTEXT_TOKEN;
    m_tokenType[0xff] = CONTEXT_TOKEN;
  }

  public static final int getNextTokenType(int i) {
    int index = (i < 0) ? 256 + i : i;
    return m_tokenType[index];
  }

  // data solely for the purpose of getContextChangeType(int i);

  public static final int CONTEXT_NO_CHANGE = 0;
  public static final int PARAMETER_CONTEXT = 1;
  public static final int NAME_ADDR_CONTEXT = 2;
  public static final int NEW_DATA_FIELD_CONTEXT = 3; // new header, body, or startline

  private static final int[] m_contextChangeType = new int[256];

  static {
    m_contextChangeType[0x08] = NEW_DATA_FIELD_CONTEXT; // known header ptr
    m_contextChangeType[0x09] = NEW_DATA_FIELD_CONTEXT; // unknown header ptr

    m_contextChangeType[0x10] = NAME_ADDR_CONTEXT; // name-addr/uri encoding
    m_contextChangeType[0x11] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x12] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x13] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x14] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x15] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x16] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x17] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x18] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x19] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x1a] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x1b] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x1c] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x1d] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x1e] = NAME_ADDR_CONTEXT;
    m_contextChangeType[0x1f] = NAME_ADDR_CONTEXT;

    m_contextChangeType[0x40] = PARAMETER_CONTEXT; // param encoding
    m_contextChangeType[0x41] = PARAMETER_CONTEXT;
    m_contextChangeType[0x42] = PARAMETER_CONTEXT;
    m_contextChangeType[0x43] = PARAMETER_CONTEXT;
    m_contextChangeType[0x44] = PARAMETER_CONTEXT;
    m_contextChangeType[0x45] = PARAMETER_CONTEXT;
    m_contextChangeType[0x46] = PARAMETER_CONTEXT;
    m_contextChangeType[0x47] = PARAMETER_CONTEXT;
    m_contextChangeType[0x48] = PARAMETER_CONTEXT;
    m_contextChangeType[0x49] = PARAMETER_CONTEXT;
    m_contextChangeType[0x4a] = PARAMETER_CONTEXT;
    m_contextChangeType[0x4b] = PARAMETER_CONTEXT;
    m_contextChangeType[0x4c] = PARAMETER_CONTEXT;
    m_contextChangeType[0x4d] = PARAMETER_CONTEXT;
    m_contextChangeType[0x4e] = PARAMETER_CONTEXT;
    m_contextChangeType[0x4f] = PARAMETER_CONTEXT;

    for (int x = 96; x < 128; x++) {
      m_contextChangeType[x] = NEW_DATA_FIELD_CONTEXT;
    }

    for (int x = 224; x < 256; x++) {
      m_contextChangeType[x] = NEW_DATA_FIELD_CONTEXT;
    }
  }

  public static final int getContextChangeType(int i) {
    int index = (i < 0) ? 256 + i : i;
    return m_contextChangeType[index];
  }

  // data solely for fetching the token type
  public static final int NO_DATA = 0;
  public static final int INT_DATA = 1;
  public static final int LONG_DATA = 2;
  public static final int BYTE_DATA = 3;
  public static final int BYTE_ARRAY_DATA = 4;
  public static final int DICTIONARY_DATA = 5;

  private static final int[] m_dataType = new int[256];

  static {
    m_dataType[1] = DICTIONARY_DATA;
    m_dataType[2] = DICTIONARY_DATA;
    m_dataType[3] = DICTIONARY_DATA;
    m_dataType[4] = DICTIONARY_DATA;

    m_dataType[5] = INT_DATA;
    m_dataType[6] = LONG_DATA;

    m_dataType[7] = BYTE_ARRAY_DATA;

    m_dataType[15] = BYTE_ARRAY_DATA;

    for (int x = 0; x <= 32; x++) {
      m_dataType[x + 32] = BYTE_DATA;
    }

    for (int x = 0; x <= 127; x++) {
      m_dataType[x + 128] = DICTIONARY_DATA;
    }
  }

  public static final int getNextTokenDataType(int i) {
    int index = (i < 0) ? 256 + i : i;

    return m_dataType[index];
  }

  // todo do this with a byteArrayOutputStream
  public static final byte[] getNextToken(
      DsTokenSipDictionary dictionary, MsgBytes mb, DsTokenSipMessageDictionary md) {
    // todo this is where an array of interfaces would be useful.         Try to get to this

    byte[] tokenData;

    if (mb.i == mb.msg.length) {
      return null;
    }

    switch (getNextTokenDataType(mb.msg[mb.i])) {
      case INT_DATA:
        mb.i++;
        tokenData = DsIntStrCache.intToBytes(DsTokenSipInteger.read16Bit(mb));
        // return DsByteString.valueOf(DsTokenSipInteger.read(mb)).toByteArray();
        break;
      case LONG_DATA:
        mb.i++;
        tokenData = DsIntStrCache.intToBytes(DsTokenSipInteger.read32Bit(mb));
        // return DsByteString.valueOf(DsTokenSipLong.read(mb)).toByteArray();
        break;
      case BYTE_DATA:
        tokenData = new byte[] {mb.msg[mb.i++]};
        break;

      case BYTE_ARRAY_DATA:
        mb.i++;
        int length = mb.msg[mb.i++];
        if (length < 0) {
          length = 256 + length;
        }

        byte[] rawBytes = new byte[length];
        System.arraycopy(mb.msg, mb.i, rawBytes, 0, length);
        mb.i += length;

        tokenData = rawBytes;
        break;

      case DICTIONARY_DATA:
        tokenData = md.get(mb).toByteArray();
        break;

      default:
        // bogus data
        tokenData = null;
        break;
    }

    if (tokenData == null) {
      return TOKEN_SIP_EMPTY_TOKEN;
    } else {
      return tokenData;
    }
  }

  // todo do this with a byteArrayOutputStream
  public static final void writeNextToken(
      DsTokenSipDictionary dictionary,
      OutputStream out,
      MsgBytes mb,
      DsTokenSipMessageDictionary md) {

    try {
      if (mb.i == mb.msg.length) {
        return;
      }

      switch (getNextTokenDataType(mb.msg[mb.i])) {
        case INT_DATA:
          mb.i++;
          out.write(DsIntStrCache.intToBytes(DsTokenSipInteger.read16Bit(mb)));
          // return DsByteString.valueOf(DsTokenSipInteger.read(mb)).toByteArray();
          break;
        case LONG_DATA:
          mb.i++;
          out.write(DsIntStrCache.intToBytes(DsTokenSipInteger.read32Bit(mb)));
          // return DsByteString.valueOf(DsTokenSipLong.read(mb)).toByteArray();
          break;
        case BYTE_DATA:
          out.write(mb.msg[mb.i++]);
          break;

        case BYTE_ARRAY_DATA:
          mb.i++;
          int length = mb.msg[mb.i++];
          if (length < 0) {
            length = 256 + length;
          }

          out.write(mb.msg, mb.i, length);
          mb.i += length;

          break;

        case DICTIONARY_DATA:
          md.get(mb).write(out);
          break;

        default:
          break;
      }
    } catch (IOException e) {
      Log.warn("Error getting next token in rawDataDecode", e);
    }
  }

  private static final char[] digits = {
    '0', '1', '2', '3', '4', '5',
    '6', '7', '8', '9', 'A', 'B',
    'C', 'D', 'E', 'F'
  };

  private static DsByteString getHexData(byte[] message, int offset, int length) {
    byte[] hexString = new byte[length * 2];

    for (int x = 0; x < length; x++) {
      hexString[2 * x] = (byte) digits[(message[offset + x] >>> 4) & 0x0000000f];
      hexString[2 * x + 1] = (byte) digits[message[offset + x] & 0x0000000f];
    }
    return new DsByteString(hexString);
  }

  public static final int findByte(byte[] data, int offset, int length, byte token) {
    if (data == null) {
      return -1;
    }

    int endOfSearch = Math.min(length, (data.length - offset));

    for (int x = offset; x < endOfSearch + offset; x++) {
      if (token == data[x]) {
        return x;
      }
    }

    return -1;
  }

  /*
  public static final byte[] getNextToken(MsgBytes mb, DsTokenSipMessageDictionary md)
  {
      //todo Steve M. suggestion to have an array of interfaces here to do this in order 1...

      switch (mb.msg[mb.i])
      {
          case TOKEN_SIP_UNSIGNED_SHORT:
              return DsByteString.valueOf(DsTokenSipInteger.read(mb)).toByteArray();
              break;
          case TOKEN_SIP_UNSIGNED_LONG:

              break;
      }

  }
  */

  /**
   * Test driver.
   *
   * <p><b>USAGE:</b>
   *
   * <pre> <code>
   * DsTokenSipMsgParser &LTimpl&GT &LTtype&GT &LTsource&GT
   *                impl   = debug | default | flat
   *                type   = speed | one
   *                source = array | stream
   * </code> </pre>
   *
   * @param args the command line arguments.
   */
  public static void main(String[] args) throws Exception {

    // Category.getRoot().setPriority(Priority.DEBUG);
    // Category.getRoot().addAppender(new FileAppender(new SimpleLayout(), System.out));

    // DsSipMessage.initDeepHeaders(DsSipMessage.ALL_HEADERS);

    // DsSipMessage.setHeaderPriority(DsSipConstants.MAX_KNOWN_HEADER);

    if (args.length == 0) {
      Error();
    }

    if (args[0].compareTo("sipfile") == 0) {

      long numTries = 0;
      long startTime = System.currentTimeMillis();
      if (args.length > 1) {
        numTries = Long.parseLong(args[2]);
      } else {
        numTries = 1;
      }

      FileInputStream fis = new FileInputStream(args[1]);
      byte[] initialBytes = new byte[fis.available()];
      fis.read(initialBytes);

      for (int x = 0; x < numTries; x++) {
        byte[] messageBytes = initialBytes;
        DsSipMessage msg = DsSipMessage.createMessage(messageBytes);

        DsByteString origMsg = msg.toByteString();
        if (Log.isDebugEnabled()) {
          System.out.println("Here's the message");
          System.out.println(origMsg);
        }

        // System.out.println("Here's the encoded message");

        byte[] encodedMsg = msg.toEncodedByteString(msg.shouldEncode()).toByteArray();
        // System.out.println(DsHexEncoding.toHex(encodedMsg));
        // System.out.println("Original message length- "+ origMsg.data().length);
        // System.out.println("Encoded message length- "+ encodedMsg.length);

        // String encodedFile = args[1]+".encoded";
        // FileOutputStream fos = new FileOutputStream(encodedFile);
        // fos.write(encodedMsg);

        fis.close();
        // fos.close();

        // DsSipFrameStream msgStream = new DsSipFrameStream(new FileInputStream(args[1]));
        // FileInputStream fis2 = new FileInputStream(encodedFile);

        // byte[] msgBytes = new byte[fis2.available()];
        // fis2.read(msgBytes);

        byte[] msgBytes = encodedMsg;

        DsSipMessageListenerFactory impl = null;
        impl = new DsSipDefaultMessageListenerFactory();

        DsSipMsgParser.parse(impl, msgBytes, 0, msgBytes.length);

        DsSipMessage message = ((DsSipDefaultMessageListenerFactory) impl).getMessage();

        if ((x % 500) == 0) {
          System.out.println(x);
          long endTime = System.currentTimeMillis();
          System.out.println("Messages/second- " + (x / ((endTime - startTime) / 1000)));
        }

        if (Log.isDebugEnabled()) {
          Log.debug("Done parsing the encoded Message.");

          Log.debug(message.toByteString());
        }
      }

      try {
      } finally {
        fis.close();
      }
    } else if (args[0].compareTo("encodedfile") == 0) {

      FileInputStream fis2 = new FileInputStream(args[1]);

      byte[] msgBytes = new byte[fis2.available()];
      fis2.read(msgBytes);

      DsSipMessageListenerFactory impl = null;
      impl = new DsSipDefaultMessageListenerFactory();

      DsTokenSipMsgParser.parse(impl, msgBytes, 0, msgBytes.length);

      DsSipMessage message = ((DsSipDefaultMessageListenerFactory) impl).getMessage();

      Log.debug("Done parsing the encoded Message.");
      Log.debug(message.toByteString());

      try {
      } finally {
        fis2.close();
      }
    } else if (args[0].equals("snifferfile")) {

      int maxBytesPerLine = 16;

      FileInputStream fis = new FileInputStream(args[1]);
      byte[] data = new byte[fis.available()];
      fis.read(data);
      String dataString = new String(data);
      System.out.println("Working with this- " + dataString);

      StringTokenizer st = new StringTokenizer(dataString);
      StringBuffer sb = new StringBuffer();

      while (st.hasMoreElements()) {
        String element = (String) st.nextElement();
        // System.out.println("Data- <"+element+">");

        // new line
        if (element.startsWith("0x")) {
          int lineCount = 0;

          while ((st.hasMoreElements()) && (lineCount < maxBytesPerLine)) {
            element = (String) st.nextElement();
            if (st.hasMoreElements()) {
              sb.append(element);
            }
            lineCount++;
          }
        } else {
          // System.out.println("Throwing away "+element);
        }
      }

      System.out.println("Done parsing file");
      byte[] msgBytes = DsHexEncoding.fromHex(sb.toString());

      FileOutputStream fos = new FileOutputStream(args[1] + ".encoded");
      fos.write(msgBytes);
      fos.close();

      DsSipMessageListenerFactory impl = null;
      impl = new DsSipDefaultMessageListenerFactory();

      DsSipMsgParser.parse(impl, msgBytes, 0, msgBytes.length);

      DsSipMessage message = ((DsSipDefaultMessageListenerFactory) impl).getMessage();

      Log.debug("Done parsing the encoded Message.");
      Log.debug(message.toByteString());

      // temporary
      byte[] encodedMsg = message.toEncodedByteString(message.shouldEncode()).toByteArray();

      Log.debug("Encoded the message ");
      Log.debug(DsString.toSnifferDisplay(encodedMsg));

      impl = new DsSipDefaultMessageListenerFactory();

      DsSipMsgParser.parse(impl, encodedMsg, 0, encodedMsg.length);
      message = ((DsSipDefaultMessageListenerFactory) impl).getMessage();
      Log.debug("Done parsing the encoded Message.");
      Log.debug(message.toByteString());

      try {
      } finally {
        fos.close();
        fis.close();
      }
    } else if (args[0].equals("snoopfile")) {

      int maxBytesPerLine = 8;

      FileInputStream fis = new FileInputStream(args[1]);
      byte[] data = new byte[fis.available()];
      fis.read(data);
      String dataString = new String(data);
      System.out.println("Working with this- " + dataString);

      StringTokenizer st = new StringTokenizer(dataString);
      StringBuffer sb = new StringBuffer();

      while (st.hasMoreElements()) {
        String element = (String) st.nextElement();
        // System.out.println("Data- <"+element+">");

        // new line
        if (element.endsWith(":")) {
          int lineCount = 0;

          while ((st.hasMoreElements()) && (lineCount < maxBytesPerLine)) {
            element = (String) st.nextElement();

            if (element.length() > 4) {
              break;
            }

            if (st.hasMoreElements()) {
              sb.append(element);
            }
            lineCount++;
          }
        } else {
          // System.out.println("Throwing away "+element);
        }
      }

      System.out.println("Done parsing file");
      System.out.println(sb.toString());

      int pos = 0;
      String snoopString = sb.toString();

      while (true) {
        int byte80 = snoopString.indexOf("80", pos);

        if (byte80 < 0) {
          System.out.println("Token SIP message not found in the message.");
          System.exit(1);
        }

        if ((byte80 % 2) == 0) {
          snoopString = snoopString.substring(byte80);
          break;
        }
        pos = byte80 + 1;
      }

      System.out.println("Got message substring");
      System.out.println(sb.toString());

      byte[] msgBytes = DsHexEncoding.fromHex(snoopString);

      FileOutputStream fos = new FileOutputStream(args[1] + ".encoded");
      fos.write(msgBytes);

      DsSipMessageListenerFactory impl = null;
      impl = new DsSipDefaultMessageListenerFactory();

      DsSipMsgParser.parse(impl, msgBytes, 0, msgBytes.length);

      DsSipMessage message = ((DsSipDefaultMessageListenerFactory) impl).getMessage();

      Log.debug("Done parsing the encoded Message.");
      Log.debug(message.toByteString());

      // temporary
      byte[] encodedMsg = message.toEncodedByteString(message.shouldEncode()).toByteArray();

      Log.debug("Encoded the message ");
      Log.debug(DsString.toSnifferDisplay(encodedMsg));

      impl = new DsSipDefaultMessageListenerFactory();

      DsSipMsgParser.parse(impl, encodedMsg, 0, encodedMsg.length);
      message = ((DsSipDefaultMessageListenerFactory) impl).getMessage();
      Log.debug("Done parsing the encoded Message.");
      Log.debug(message.toByteString());

      try {
      } finally {
        fos.close();
        fis.close();
      }
    } else {
      Error();
    }
  }

  private static void Error() {
    System.out.println("Startup Options:");
    System.out.println("sipfile <file path>");
    System.out.println("encodedfile <file path>");
    System.out.println("snifferfile <file path>");
    System.exit(1);
  }
}
