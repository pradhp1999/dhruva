// Copyright (c) 2004-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsSipMime.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.text.*;
import org.apache.logging.log4j.Level;

/**
 * The DsSipFragment message parser provides methods to parse body of a "message/sipfrag" content
 * type.
 */
public class DsSipFragmentParser extends DsMsgParserBase implements DsMimeBodyParser {

  /** Single instance of the parser. */
  private static DsSipFragmentParser instance = new DsSipFragmentParser();

  ////////////////////////////////////////////////////////////////////////////////
  // DATA
  ////////////////////////////////////////////////////////////////////////////////

  /** Error message format. */
  private static final String WRONG_CONTENT_TYPE =
      "The sipfrag parser cannot parse content type: {0}";
  /** Define for literal constant 100 */
  private static final int STATUS_CODE_FACTOR = 100;

  ////////////////////////////////////////////////////////////////////////////////
  // Constructor
  ////////////////////////////////////////////////////////////////////////////////

  /** Disallow instance creation from application code. */
  private DsSipFragmentParser() {}

  ////////////////////////////////////////////////////////////////////////////////
  // parse() implementing DsMimeBodyParser
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Parses the body of the entity. The body of the entity should be in parsed form after this
   * method call. This is the implementation of DsMimeBodyParser interface
   *
   * @param entity MIME entity whose body is unparsed
   * @throws DsSipParserException if there is parsing error.
   */
  public void parse(DsMimeEntity entity) throws DsSipParserException {
    DsMimeBody mimeBody = null;

    if (entity == null) return;
    mimeBody = entity.getMimeBody();
    if (mimeBody == null) return;
    if (mimeBody.isParsed()) return;

    // Make sure we deal with "message/sipfrag" only.
    DsByteString bodyType = entity.getBodyType();
    if (!DsMimeContentManager.MIME_MT_MESSAGE_SIPFRAG.equalsIgnoreCase(bodyType)) {
      String es = MessageFormat.format(WRONG_CONTENT_TYPE, new Object[] {bodyType});
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn(es);
        DsLog4j.messageCat.warn(mimeBody);
      }
      throw new DsSipParserException(es);
    }

    DsSipFragment sipfrag = new DsSipFragment();

    // unparsed string
    DsByteString unparsed = ((DsMimeUnparsedBody) mimeBody).getBytes();
    // body byte array
    byte[] body = unparsed.data();
    // body offset
    int offset = unparsed.offset();
    int count = unparsed.length();

    try {
      parse(sipfrag, body, offset, count);
    } catch (DsSipParserListenerException ple) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn("DsSipFragmentParser.parse(): DsSipParserListenerException", ple);
      }
    } catch (DsSipParserException pe) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn("DsSipFragmentParser.parse(): DsSipParserException", pe);
      }
    }
    entity.setMimeBody(sipfrag);
  }

  ////////////////////////////////////////////////////////////////////////////////
  // More convenient parse() methods
  ////////////////////////////////////////////////////////////////////////////////

  /**
   * Parses a byte array and create a sipfrag.
   *
   * @param msg the byte array that contains the sipfrag message to parse.
   * @return the created sipfrag.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static DsSipFragment parse(byte msg[])
      throws DsSipParserListenerException, DsSipParserException {
    return parse(msg, 0, msg.length);
  }

  /**
   * Parses a byte array and create a sipfrag.
   *
   * @param msg the byte array that contains the sipfrag to parse.
   * @param offset the start of the sipfrag in the array.
   * @param count the number of bytes in the sipfrag.
   * @return the created sipfrag.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static DsSipFragment parse(byte msg[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsSipFragment sipfrag = new DsSipFragment();
    parse(sipfrag, msg, offset, count);
    return sipfrag;
  }

  /**
   * Parses a byte array and create a SIPFRAG message.
   *
   * @param sipfrag the SIPFRAG message to fill the startLine, headers, and body in.
   * @param msg the byte array that contains the sipfrag message to parse.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parse(DsSipFragment sipfrag, byte msg[])
      throws DsSipParserListenerException, DsSipParserException {
    parse(sipfrag, msg, 0, msg.length);
  }

  /**
   * Parses a byte array and create a SIPFRAG message.
   *
   * @param sipfrag the SIPFRAG message to fill the headers in.
   * @param msg the byte array that contains the sipfrag message to parse.
   * @param offset the start of the sipfrag message in the array.
   * @param count the number of bytes in the sipfrag message.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static void parse(DsSipFragment sipfrag, byte msg[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    boolean hasBodyOnly = false;

    // strip leading white space
    while (msg[offset] <= ' ' && msg[offset] != '\r' && msg[offset] != '\n') {
      ++offset;
      --count;
    }

    if (msg[offset] == '\r' || msg[offset] == '\n') {
      ++offset;
      --count;
      if (offset < msg.length && (msg[offset] == '\r' && msg[offset + 1] == '\n')) {
        ++offset;
        --count;
      }
      hasBodyOnly = true; // We reached the \r\n (RFC rule), \r, or \n" (relaxed rule)
    }

    MsgBytes mb = new MsgBytes(msg, offset, count);

    try {
      if (hasBodyOnly == false) {
        // the sipfrag will be filled in as the parse proceeds.
        parseSipfragStartLine(sipfrag, mb);

        if (DsPerf.ON) DsPerf.start(PARSE_HEADERS);
        // keep parsing until the body is found
        while (true) {
          try {
            if (!parseHeader(sipfrag, mb)) {
              // the body was found - exit the loop and parse the body
              break;
            }
          } catch (Exception e) {
            throw generateDsSipParserException(sipfrag, e, mb.msg, offset, count);
          }
        } // End of "while (true)"
        if (DsPerf.ON) DsPerf.stop(PARSE_HEADERS);
      } // End of "if (hasBodyOnly == false)"

      parseBody(sipfrag, mb);

      if (DsPerf.ON) DsPerf.stop(ENTIRE_PARSE);
    } catch (DsSipParserException pe) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn("DsSipFragmentParser.parse(): DsSipParserException", pe);
      }
      throw generateDsSipParserException(sipfrag, pe, mb.msg, offset, count);
    } catch (DsSipParserListenerException ple) {
      if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
        DsLog4j.messageCat.warn("DsSipFragmentParser.parse(): DsSipParserListenerException", ple);
      }
      throw generateDsSipParserException(sipfrag, ple, mb.msg, offset, count);
    }

    return;
  }

  /**
   * Parses the first line of a SIP message. Creates the appropriate listener based on the first
   * line (no request or response will be created).
   *
   * @param mb the message bytes that contains the SIPFRAG message to parse.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  private static void parseSipfragStartLine(DsSipFragment sipfrag, MsgBytes mb)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_START_LINE);

    try {
      int start = mb.i; // beginning of method name (request) or
      // SIP version (response) or neither (special
      // case for sipfrag
      byte[] message = mb.msg;
      int offset = mb.offset;
      int count = 0;

      byte ch;

      int slashIndex = -1;
      int dotIndex = -1;
      ch = mb.msg[mb.i];
      while (ch > ' ') {
        if (ch == '/') // looking for "SIP/2.0 "
        {
          slashIndex = mb.i;
        } else if (ch == '.') {
          dotIndex = mb.i;
        } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '-') {
          // Looking for either method name or header name
          count++; // Count the number of characaters for the name
        }
        ch = mb.msg[++mb.i];
      }

      if (slashIndex != -1) // Response, e.g. "SIP/2.0 180 Ringing"
      {
        count = 0; // reuse the counter
        // index of the space after the protocol/version
        int protocolEnd = mb.i;
        int protocolStart = start;

        // Found the startLine for the response sipfrag
        sipfrag.setSipfragType(DsSipFragment.RESPONSEFRAG);

        // move to the first char after the space (start of version)
        ++mb.i;

        // always a 3 digit status code
        int statusCode =
            (((mb.msg[mb.i++] - '0') * 100)
                + ((mb.msg[mb.i++] - '0') * 10)
                + (mb.msg[mb.i++] - '0'));

        // valid the response code
        if (!DsSipResponseCode.isRespCodeInRange(statusCode)) {
          throw new DsSipParserException(
              "DsSipFragmentParser: Unknown/Unsupported response code found : " + statusCode);
        }
        sipfrag.setStatusCode(statusCode);

        // move to the first char of the reason phrase and set the start pointer
        start = ++mb.i;

        ch = mb.msg[mb.i++];
        while (ch != '\r' && ch != '\n') {
          // maivu - 12.12.05 - CSCsc71447 - If missing the crlf, let's move on
          if (mb.i >= mb.msg.length) {
            count++;
            break;
          }
          ch = mb.msg[mb.i++];
          count++;
        }
        sipfrag.setReasonPhrase(new DsByteString(mb.msg, start, count));

        if (ch != '\n') {
          // maivu - 12.12.05 - CSCsc71447 - If missing the crlf, let's move on
          while ((mb.i < mb.msg.length) && (mb.msg[mb.i++] != '\n')) {
            // just find the EOL
          }
        } else {
          // just move to the start of the next line
          ++mb.i;
        }

        if (dotIndex != -1) {
          // report the protocol and versions for validation
          sipfrag.protocolFound(
              mb.msg,
              protocolStart,
              slashIndex - protocolStart, // protocol
              slashIndex + 1,
              dotIndex - slashIndex - 1, // major version
              dotIndex + 1,
              protocolEnd - dotIndex - 1, // minor version
              true); // isValid
        } else {
          // report the protocol and versions for validation
          // maybe better just log the invalid version, because
          // protocolFound(..., false) simply returns  - JRY
          sipfrag.protocolFound(
              mb.msg,
              protocolStart,
              slashIndex - protocolStart, // protocol
              0,
              0, // major version
              0,
              0, // minor version
              false); // isValid
        }
      } else {
        int methodID = getMethod(message, offset, count);
        if (methodID != UNKNOWN) { // Request
          // already found the method name

          sipfrag.setSipfragType(DsSipFragment.REQUESTFRAG);
          sipfrag.setMethod(methodID);

          // start of the Request-URI
          start = ++mb.i;

          boolean isRequestUriValid = true;
          // find the scheme then the data
          ch = mb.msg[mb.i++];
          while (ch != ':') {
            if (ch <= ' ') // no WS allowed in URLs
            {
              isRequestUriValid = false;
              if (ch == '\n') // do not go past the EOL
              {
                break;
              }
            }
            ch = mb.msg[mb.i++];
          }

          if (isRequestUriValid == false) {
            // "The start line, if present, must be complete and valid per [1]."
            if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
              DsLog4j.messageCat.warn("Invalid URI found in the request's startLine");
            }
            throw new DsSipParserException("Invalid URI found in the request's startLine");
          }

          DsSipElementListener uri = sipfrag.requestURIBegin(mb.msg, start, mb.i - start - 1);

          // now find the end of the Request-URI
          while (ch > ' ') {
            ch = mb.msg[mb.i++];
          }

          try {
            if (uri != null) {
              // deep parse this uri
              // pick the right parser and store results in uri
              switch (getUrlType(mb.msg, start, mb.i - start - 1)) {
                case SIP_URL: /* falls through */
                case SIPS_URL:
                  // deep parse the SIP URL
                  parseSipUrl(sipfrag.getHeaderListener(), uri, mb.msg, start, mb.i - start - 1);
                  break;
                case TEL_URL:
                  parseTelUrl(uri, mb.msg, start, mb.i - start - 1);
                  // no parser yet
                  break;
                default: // includes OTHER_URL
                  // no parser
                  break;
              }
            }
          } catch (Exception e) {
            isRequestUriValid = false;
            // "The start line, if present, must be complete and valid per [1]."
            if (DsLog4j.messageCat.isEnabled(Level.WARN)) {
              DsLog4j.messageCat.warn("Invalid URI found in the request's startLine");
              DsLog4j.messageCat.warn(e);
            }
            throw new DsSipParserException(
                "Invalid URI found when parsing the request's startLine");
          }

          sipfrag.requestURIFound(mb.msg, start, mb.i - start - 1, isRequestUriValid);

          int protocolStart = mb.i;
          ch = mb.msg[mb.i];
          while (ch > ' ') {
            if (ch == '/') {
              slashIndex = mb.i;
            } else if (ch == '.') {
              dotIndex = mb.i;
            }
            ch = mb.msg[++mb.i];
          }

          if (slashIndex != -1 && dotIndex != -1) {
            // report the protocol and versions for validation
            sipfrag.protocolFound(
                mb.msg,
                protocolStart,
                slashIndex - protocolStart, // protocol
                slashIndex + 1,
                dotIndex - slashIndex - 1, // major version
                dotIndex + 1,
                mb.i - dotIndex - 1, // minor version
                true); // isValid
          } else {
            // invalid - all data goes in protocol
            // maybe better just log the invalid version, because
            // protocolFound(..., false) simply returns  - JRY
            sipfrag.protocolFound(
                mb.msg,
                protocolStart,
                mb.i - protocolStart, // protocol
                0,
                0, // major version
                0,
                0, // minor version
                false); // isValid
          }

          if (ch != '\n') {
            while (mb.msg[mb.i++] != '\n') {
              // just find the EOL
            }
          } else {
            // just move to the start of the next line
            ++mb.i;
          }
        } else // Neither request, nor response, no startLine
        {
          sipfrag.setSipfragType(DsSipFragment.NOSTARTLINEFRAG);
          // need to backup the index, and continue the parse
          // for headers and body, if any.
          mb.i = start;
          return;
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw generateDsSipParserException(sipfrag, e, mb.msg, mb.offset, mb.count);
    }
  }

  /** Returns the only instance of this parser. */
  public static DsSipFragmentParser getInstance() {
    return instance;
  }
}
