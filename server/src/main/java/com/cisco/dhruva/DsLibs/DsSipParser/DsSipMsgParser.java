// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

import com.cisco.dhruva.DsLibs.DsSipMime.DsMimeMultipartMixed;
import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipConstants;
import com.cisco.dhruva.DsLibs.DsSipParser.TokenSip.DsTokenSipMsgParser;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * Used to parse SIP messages. This is where header parsers are registered as well as new element
 * IDs.
 */
public class DsSipMsgParser extends DsMsgParserBase implements DsSipConstants {
  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     The following static/class variables were moved to DsMsgParserBase:
      private static final int MAX_EXCEPTIONS = 10;
      private static final int SIP_DATE_LENGTH = 29;
      private static DsParseTreeNode parseTree[] = new DsParseTreeNode[128];
  */

  //////////////////////////////////////////////////////////////////////////////
  // Method, Header and Element Names
  //////////////////////////////////////////////////////////////////////////////

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     The following static/class variables were moved to DsMsgParserBase:
      public static DsByteString[] METHOD_NAMES;
      public static DsByteString[] HEADER_NAMES;
      public static DsByteString[] COMPACT_HEADER_NAMES;
      public static DsByteString[] ELEMENT_NAMES;
      private static final HashMap headersMap = new HashMap(((MAX_KNOWN_HEADER + COMPACT_HEADER_NAMES_SIZE) * 2) + 1);
      private static final HashMap methodsMap = new HashMap((METHOD_NAMES_SIZE * 2) + 1);
      private static final TIntObjectHashMap parserMap = new TIntObjectHashMap();
  */

  // Constants for DsPerf

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     The following static/class variables were moved to DsMsgParserBase:
      private static final int ENTIRE_PARSE;
      private static final int PARSE_START_LINE;
      private static final int PARSE_HEADERS;
      private static final int PARSE_VIA;
      private static final int PARSE_BODY;
      private static final int PARSE_INT;
      private static final int PARSE_LONG;
      private static final int PARSE_FLOAT;
      private static final int PARSE_SIP_URL_DATA;
      private static final int PARSE_TEL_URL_DATA;
      private static final int FIRE_ELEMENT;
      private static final int FIRE_PARAMETER;
      private static final int PARSE_URL_HEADER;
      private static final int PARSE_NAME_ADDR;
      private static final int DEEP_PARSE_HEADERS;
  */

  //////////////////////////////////////////////////////////////////////////////
  // Static block initializing Method, Header and Element Names
  //////////////////////////////////////////////////////////////////////////////

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     The static initialization block was moved to DsMsgParserBase:
      static
      {
          // Method Names initialization
          // Header Names initialization
          // Compact Header Names initialization
          // Element Names initialization
          // Initialize the (String to Integer) methods map
          // Initialize the (String to Integer) headers map
          // compact form - normalized
          // compact form - canonical
          // DsPerf register
      }
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     The following methods were moved to DsMsgParserBase:
      public static DsByteString getMethod(int id)
      public static int getMethod(DsByteString name)
      public static DsByteString toMethodBS(byte[] name, int offset, int count)
      public static int getMethod(byte[] data, int offset, int count)
      public static DsByteString getHeader(int id)
      public static int getHeader(DsByteString name)
      public static DsByteString getHeaderCompact(int id)
  */

  //////////////////////////////////////////////////////////////////////////////

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     The static initialization block was moved to DsMsgParserBase:
      static
      {
          // build the parse tree for header names
          // need to change from .length to last header
      }
  */

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     The following methods were moved to DsMsgParserBase:
      public static int registerElement(DsByteString name)
      public static int registerHeader(DsByteString longName, DsByteString compactName,
      private static void addParserMapping(int id, DsSipHeaderParserInterface parser)
      public static int registerMethod(DsByteString name)
  */

  /** Private default constructor. Disallow instance construction. */
  private DsSipMsgParser() {}

  /**
   * Parses a byte array and passes the events to the created listener.
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
    int origOffset = offset;
    int origCount = count;
    try {
      // strip leading white space
      while (msg[offset] <= ' ') {
        if ((msg[offset] == DsTokenSipConstants.TOKEN_SIP_PREFIX1)
            || (msg[offset] == DsTokenSipConstants.TOKEN_SIP_PREFIX2)) {
          // found a tokenized SIP message
          return DsTokenSipMsgParser.parse(msgFactory, msg, offset, count);
        }
        ++offset;
        --count;
      }
      return parseCanonical(msgFactory, msg, offset, count);
    } catch (ArrayIndexOutOfBoundsException e) {
      // this will return a bogus body - jsm
      throw generateDsSipParserException(null, e, msg, origOffset, origCount);
    }
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
  public static DsSipMessageListener parseCanonical(
      DsSipMessageListenerFactory msgFactory, byte msg[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(ENTIRE_PARSE);

    MsgBytes mb = new MsgBytes(msg, offset, count);

    DsSipMessageListener messageListener = parseStartLine(msgFactory, mb);

    if (messageListener == null) {
      // nothing left to do, the caller does not want this message parsed yet
      return null;
    }

    int exceptionCount = 0;

    try {
      DsSipHeaderListener headerListener = messageListener.getHeaderListener();

      if (DsPerf.ON) DsPerf.start(PARSE_HEADERS);
      // keep parsing until the body is found
      while (true) {
        try {
          if (!parseHeader(headerListener, mb)) {
            // the body was found - exit the loop and parse the body
            break;
          }
        } catch (Exception e) {
          // ignore the first few exceptions and continue to parse as best as we can
          // just make sure that there is not an infinite loop trying to parse
          if (++exceptionCount > MAX_EXCEPTIONS) {
            throw new Exception("Maximum number of parser exceptions exeeded.");
          }
        }
      }
      if (DsPerf.ON) DsPerf.stop(PARSE_HEADERS);

      parseBody(messageListener, mb);

      if (DsPerf.ON) DsPerf.stop(ENTIRE_PARSE);

      return messageListener;
    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      // this will return a bogus body - jsm
      throw generateDsSipParserException(messageListener, e, msg, offset, count);
    }
  }

  /**
   * Pre-parses a byte array and returns the results.
   *
   * @param msg the byte array that contains the SIP message to pre-parse.
   * @return the information gathered from the pre-parse.
   * @throws DsSipParserException if there is an exception while parsing.
   */
  public static DsPreParseData preParse(byte msg[]) throws DsSipParserException {
    return preParse(msg, 0, msg.length);
  }

  /**
   * Pre-parses a byte array and returns the results.
   *
   * @param msg the byte array that contains the SIP message to pre-parse.
   * @param offset the start of the SIP message in the array.
   * @param count the number of bytes in the SIP message.
   * @return the information gathered from the pre-parse.
   * @throws DsSipParserException if there is an exception while parsing.
   */
  public static DsPreParseData preParse(byte msg[], int offset, int count)
      throws DsSipParserException {
    int origOffset = offset;
    int origCount = count;
    DsPreParseData preParseData = new DsPreParseData();

    try {
      int end = offset + count;

      // strip leading white space
      while (msg[offset] <= ' ') {
        ++offset;
        --count;
      }

      int index = offset;
      boolean eol = false;
      byte ch;
      boolean isResponse = false;

      // find the method name or if it is a response
      while ((ch = msg[index++]) != ' ') {
        if (ch == '/') {
          isResponse = true;
        }
      }

      if (isResponse) {
        preParseData.setResponseCode(msg, index, 3);
      } else {
        preParseData.setMethod(msg, offset, index - offset - 1);
      }

      // skip the start line
      while (msg[index++] != '\n') {
        // just find the EOL
      }

      boolean findBranch;
      // look for this first Via header
      while (true) {
        ch = msg[index++];
        findBranch = false;
        eol = false;
        if (ch == 'V' || ch == 'v') {
          ch = msg[index++];
          if (ch == 'i' || ch == 'I') {
            ch = msg[index++];
            if (ch == 'a' || ch == 'A') {
              ch = msg[index++];
              if (ch <= ' ' || ch == ':') {
                findBranch = true;
              }
            }
          } else if (ch <= ' ' || ch == ':') {
            findBranch = true;
          }
        }

        if (!findBranch) // not a via header - skip this line
        {
          while (!eol) {
            while (msg[index++] != '\n') {}
            if (msg[index] == ' ' || msg[index] == '\t') {
              eol = false;
            } else {
              eol = true;
              if ((msg[index] == '\r' && msg[index + 1] == '\n') || msg[index] == '\n') {
                // double eol - end of msg, branch not found
                return preParseData;
              }
            }
          }
        } else // look for the branch
        {
          while (!eol) // only search in this Via header
          {
            while ((ch = msg[index++]) != '\n') {
              if (ch == '"') // ignore quoted strings
              {
                index = readToCloseQuote(msg, index, end);
                ch = msg[index++];
              }

              if (ch == ';') // start of a parameter
              {
                index = lws(msg, index, end);

                if (msg[index] == 'b'
                    || msg[index] == 'B' && msg[index + 1] == 'r'
                    || msg[index + 1] == 'R' && msg[index + 2] == 'a'
                    || msg[index + 2] == 'A' && msg[index + 3] == 'n'
                    || msg[index + 3] == 'N' && msg[index + 4] == 'c'
                    || msg[index + 4] == 'C' && msg[index + 5] == 'h'
                    || msg[index + 5] == 'H' && msg[index + 6] == '='
                    || msg[index + 6] <= ' ') {
                  // found the branch parameter
                  index += 6;
                  index = lws(msg, index, end);
                  if (msg[index++] != '=') // malformed
                  {
                    // exception
                  }
                  index = lws(msg, index, end);

                  // index now points to the first char of the branch parameter
                  if (msg[index] == 'z'
                      && msg[index + 1] == '9'
                      && msg[index + 2] == 'h'
                      && msg[index + 3] == 'G'
                      && msg[index + 4] == '4'
                      && msg[index + 5] == 'b'
                      && msg[index + 6] == 'K') {
                    // found magic cookiee
                    int start = index;
                    index += 7;

                    // find the end of the key
                    ch = msg[index++];
                    while (ch > ' ' && ch != ';' && ch != '(') {
                      // just find the first separator
                      ch = msg[index++];
                    }

                    preParseData.setKey(msg, start, index - start - 1);

                    return preParseData;
                  }
                }
              }
            }
            if (msg[index] == ' ' || msg[index] == '\t') {
              eol = false;
            } else {
              eol = true;
              if ((msg[index] == '\r' && msg[index + 1] == '\n') || msg[index] == '\n') {
                // double eol - end of msg, branch not found
                return preParseData;
              }
            }
          }

          // passed the first Via header and did not find a branch - we are done
          return preParseData;
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw generateDsSipParserException(null, e, msg, origOffset, origCount);
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
      DsSipMessageListenerFactory msgFactory, MsgBytes mb)
      throws DsSipParserListenerException, DsSipParserException {
    if (DsPerf.ON) DsPerf.start(PARSE_START_LINE);

    DsSipMessageListener sipMsg = null;
    try {
      int start = mb.i; // beginning of method name (request) or SIP version (response)
      byte ch;

      int slashIndex = -1;
      int dotIndex = -1;
      ch = mb.msg[mb.i];
      while (ch > ' ') {
        if (ch == '/') {
          slashIndex = mb.i;
        } else if (ch == '.') {
          dotIndex = mb.i;
        }
        ch = mb.msg[++mb.i];
      }

      if (slashIndex != -1) // Response
      {
        // index of the space after the protocol/version
        int protocolEnd = mb.i;
        int protocolStart = start;

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
              "DsSipMsgParser: Unknown/Unsupported response code found : " + statusCode);
        }
        // move to the first char of the reason phrase and set the start pointer
        start = ++mb.i;

        ch = mb.msg[mb.i++];
        int backup = 1;
        while (ch != '\r' && ch != '\n') {
          ch = mb.msg[mb.i++];
        }

        // if we found a '\r' then move ahead past the '\n' to the end of the start line
        if (ch == '\r') {
          mb.i++;
          backup = 2;
        }

        // create the response
        sipMsg = msgFactory.responseBegin(mb.msg, statusCode, start, mb.i - start - backup, false);

        if (sipMsg == null) {
          // nothing left to do, the caller does not want this message parsed yet
          if (DsPerf.ON) DsPerf.stop(PARSE_START_LINE);
          return null;
        }

        if (dotIndex != -1) {
          // report the protocol and versions for validation
          sipMsg.protocolFound(
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
          sipMsg.protocolFound(
              mb.msg,
              protocolStart,
              slashIndex - protocolStart, // protocol
              0,
              0, // major version
              0,
              0, // minor version
              false); // isValid
        }
      } else // Request
      {
        // already found the method name

        // create the request for this method type
        sipMsg = msgFactory.requestBegin(mb.msg, start, mb.i - start, false);

        if (sipMsg == null) {
          // nothing left to do, the caller does not want this message parsed yet
          if (DsPerf.ON) DsPerf.stop(PARSE_START_LINE);
          return null;
        }

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

        DsSipElementListener uri = sipMsg.requestURIBegin(mb.msg, start, mb.i - start - 1);

        // now find the end of the Request-URI
        while (ch > ' ') {
          ch = mb.msg[mb.i++];
        }

        try {
          if (uri != null) {
            // deep parse this uri
            // pick the right parser and store results in uri

            // CAFFEINE 2.0 DEVELOPMENT
            // kaiw: this part w/o : just parsed in DsSipRequest.requestURIBegin
            //       should set a constant to avoid double parsing
            //       the parseXXXUrl reparse the url type the 3rd time!,
            //       but becareful about other places uses parseXXXUrl, only a 3 or 4 more ==, ok
            switch (getUrlType(mb.msg, start, mb.i - start - 1)) {
              case SIP_URL:
              case SIPS_URL:
                // deep parse the SIP URL
                parseSipUrl(sipMsg.getHeaderListener(), uri, mb.msg, start, mb.i - start - 1);
                break;
              case TEL_URL:
                parseTelUrl(uri, mb.msg, start, mb.i - start - 1);
                // no parser yet
                break;
                //                        case HTTP_URL:
                //                            // no parser yet
                //                            break;
              default: // includes OTHER_URL
                parseUnknownUrl(uri, mb.msg, start, mb.i - start - 1);
                // no parser
                // constructFrom?
                break;
            }
          }
        } catch (Exception e) {
          // hack - need to do more here - jsm
          isRequestUriValid = false;
        }

        sipMsg.requestURIFound(mb.msg, start, mb.i - start - 1, isRequestUriValid);

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
          sipMsg.protocolFound(
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
          sipMsg.protocolFound(
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
      }

      if (DsPerf.ON) DsPerf.stop(PARSE_START_LINE);
      return sipMsg;
    } catch (ArrayIndexOutOfBoundsException e) {
      // this will return a bogus body - jsm
      throw generateDsSipParserException(sipMsg, e, mb.msg, mb.offset, mb.count);
    }
  }

  /* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     The following methods were moved to DsMsgParserBase:
      private static boolean parseHeader(DsSipHeaderListener headerListener, MsgBytes mb)
      public static void parseHeader(DsSipHeaderListener headerListener, byte data[], int offset, int count)
      public static void parseHeader(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      private static void parseBody(DsSipMessageListener sipMsg, MsgBytes mb) throws DsSipParserListenerException
      private static void lws(MsgBytes mb)
      private static int lws(byte[] data, int index, int end)
      private static void addHeaderType(DsByteString name, DsByteString compactName, int code)
      private static void addBranch(DsByteString name, int code)
      private static void fireElement(DsSipElementListener element, int contextId, int elementId, byte[] buffer, int offset, int count)
      private static void fireParameter(DsSipElementListener element,
      static void parseUrlHeader(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      public static void parseParameters(DsSipElementListener element, int headerType, byte data[], int offset, int count)
      static void parseStringHeader(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseRAck(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseCSeq(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseWarning(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseVia(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseUnknownHeader(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseAuthParams(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseAuth(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseTokenListWithParams(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseTimestamp(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseServerUserAgent(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseMediaTypeWithParams(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      static void parseDate(DsSipHeaderListener headerListener, int headerType, byte data[], int offset, int count)
      private static void parseEventPackage(DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      private static void parseSipDate(DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      public static void parseDateOrLong(DsSipHeaderListener headerListener, int headerType, byte[] data, int offset, int count)
      private static void parseLanguage(DsSipElementListener element, int headerType, byte[] data, int offset, int count)
      private static int getUrlType(byte[] data, int offset, int count)
      private static int readToCloseParen(byte[] data, int index, int end)
      public static void parseUnknownUrl(DsSipElementListener element, byte data[], int offset, int count)
      public static void parseTelUrl(DsSipElementListener element, byte data[], int offset, int count)
      public static void parseTelUrlData(DsSipElementListener element, byte data[], int offset, int count)
      public static void parseNameAddr(DsSipElementListener element, byte data[], int offset, int count)
      public static void parseSipUrl(DsSipElementListener element, byte data[], int offset, int count)
      public static void parseSipUrl(DsSipHeaderListener hdrListener, DsSipElementListener element, byte data[], int offset, int count)
      public static void parseSipUrlData(DsSipElementListener element, byte data[], int offset, int count)
      public static void parseSipUrlData(DsSipHeaderListener hdrListener, DsSipElementListener element, byte data[], int offset, int count)
      private static void parseHeaderFromUrl(DsSipHeaderListener hdrListener, byte[] data, int startName, int nameCount, int startValue, int valueCount)
      private static byte hexVal(byte b)
      public static int parseInt(DsByteString val)
      public static long parseLong(DsByteString val)
      public static float parseFloat(DsByteString val)
      public static float parseFloat(byte data[], int offset, int count)
      public static long parseLong(byte data[], int offset, int count)
      public static int parseInt(byte data[], int offset, int count)
      private static int readToCloseQuote(byte[] data, int index, int end)
      public static DsSipParserException generateDsSipParserException(DsSipElementListener element,
      public static DsSipParserException generateDsSipParserException(DsSipElementListener element,
      public static DsSipParserException generateDsSipParserException(DsSipHeaderListener headerListener,
      public static DsSipParserException generateDsSipParserException(DsSipHeaderListener headerListener,
      public static DsSipParserException generateDsSipParserException(DsSipMessageListener messageListener,
      private static boolean isDataValid(byte[] data, int offset, int count)
      private static void notifyMessageListener(DsSipMessageListener messageListener,
      private static void notifyHeaderListener(DsSipHeaderListener headerListener,
      private static void notifyElementListener(DsSipElementListener element,
  */

  /**
   * Test driver.
   *
   * <p><b>USAGE:</b>
   *
   * <pre> <code>
   * DsSipMsgParser &LTimpl&GT &LTtype&GT &LTsource&GT
   *                impl   = debug | default | flat
   *                type   = speed | one
   *                source = array | stream
   * </code> </pre>
   *
   * @param args the command line arguments.
   */
  public static void main(String[] args) {
    new TestThread(args).start();

    /*
            try
            {
                //byte[] nameaddr = "name another name <sip:user@host>".getBytes();
                //parseNameAddr(DsSipDebugImpl.getInstance(), nameaddr, 0, nameaddr.length);

                //byte[] params = " name = value , name2 = value2 ".getBytes();
                //parseParameters(DsSipDebugImpl.getInstance(), AUTHORIZATION, params, 0, params.length);

                //byte[] header = " To : sip:user@host ".getBytes();
                //parseHeader(DsSipDebugImpl.getInstance(), header, 0, header.length);

                //byte[] header = "Expires: Thu, 21 Feb 2002 13:02:03 GMT".getBytes();
                byte[] header = "Expires: 2500".getBytes();
                //byte[] header = "Expires: test".getBytes();
                DsSipExpiresHeader expires = new DsSipExpiresHeader();
                parseHeader(expires, header, 0, header.length);
                //System.out.println(expires.getDeltaSeconds());
                System.out.println(expires.toString());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
    */
  }

  /*
      private static void printParseTree()
      {
          for (int i = 0; i < parseTree.length; i++)
          {
              DsParseTreeNode node = parseTree[i];
              if (node == null)
              {
                  continue;
              }

              node.print((byte)i, "");
          }
      }
  */
}

/** A thread used to test some of the functionality of the parser. For testing the parser only. */
class TestThread extends DsThread {
  /** The arguments from the command line. */
  String[] args;

  public TestThread(String[] args) {
    this.args = args;
  }

  /** Prints the usage information. */
  private static void usage() {
    System.out.println();
    System.out.println("USAGE: DsSipMsgParser <impl> <type> <source>");
    System.out.println("       impl   = debug | default | flat");
    System.out.println("       type   = speed | one");
    System.out.println("       source = array | stream");
    System.out.println();

    System.exit(1);
  }

  public void run() {
    try {
      // CAFFEINE 3.1 DEVELOPMENT - Used for Codenomicon test cases
      // This makes it so that the SDP parser will be invoked
      // REFACTOR
      //      DsSdpMsg.registerType();
      DsMimeMultipartMixed.registerType();

      // Leave other types here in case we ever want to test with them
      // DsMimeMultipartMixed.registerType();
      // DsSipFragment.registerType();
      // DsMimeMultipartAlternative.registerType();

      if (args.length != 3) {
        usage();
        System.exit(1);
      }
      // DsSipMsgParser.printParseTree();
      // System.exit(1);

      int readInt;
      int length = 0;
      byte tmpBytes[] = new byte[4096];

      readInt = System.in.read();
      while (readInt != -1) {
        tmpBytes[length++] = (byte) readInt;
        readInt = System.in.read();
      }

      byte msgBytes[] = new byte[length];
      System.arraycopy(tmpBytes, 0, msgBytes, 0, length);

      ByteArrayInputStream byteStream = new ByteArrayInputStream(msgBytes, 0, length);
      DsSipFrameStream msgStream = new DsSipFrameStream(byteStream);

      DsSipMessageListenerFactory impl = null;
      if (args[0].equalsIgnoreCase("default")) {
        DsSipMessage.setParseAllHeaders(true);
        impl = new DsSipDefaultMessageListenerFactory();
      } else if (args[0].equalsIgnoreCase("debug")) {
        impl = DsSipDebugImpl.getInstance();
      }
      // else if (args[0].equalsIgnoreCase("flat"))
      // {
      // impl = new DsSipMessageBytesImpl();
      // impl = new DsSipKeyRequestBytes();
      // }
      else {
        usage();
        System.exit(1);
      }

      // DsSipDebugImpl impl = DsSipDebugImpl.getInstance();
      // DsSipDefaultMessageListenerFactory impl = new DsSipDefaultMessageListenerFactory();

      if (args[1].equalsIgnoreCase("one")) {
        if (args[2].equalsIgnoreCase("array")) {
          DsSipMsgParser.parse(impl, msgBytes, 0, length);
        } else if (args[2].equalsIgnoreCase("stream")) {
          DsSipMsgParser.parse(impl, msgStream.readMsg());
        } else {
          usage();
          System.exit(1);
        }
      } else if (args[1].equalsIgnoreCase("speed")) {
        int count = 125000;
        int warmup = 75000;
        long start = 0;
        long end = 0;

        if (args[2].equalsIgnoreCase("array")) {
          if (args[0].equalsIgnoreCase(
              "default")) // time with default deep headers, all deep headers and no deep headers
          {
            System.out.println("With default deep headers:");
            for (int i = 0; i < warmup; i++) {
              DsSipMsgParser.parse(impl, msgBytes, 0, length);
            }

            DsPerf.reset();

            start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
              DsSipMsgParser.parse(impl, msgBytes, 0, length);
            }
            end = System.currentTimeMillis();

            System.out.println();
            if (DsPerf.ON) System.out.println(DsPerf.getResults());
            System.out.println(
                "Avg time / parse = "
                    + (((double) (end - start) / count) * 1000.0)
                    + " microseconds");
            System.out.println();

            System.out.println("With no deep headers:");
            DsSipMessage.initDeepHeaders(DsSipMessage.NO_DEEP_HEADERS);
            for (int i = 0; i < warmup; i++) {
              DsSipMsgParser.parse(impl, msgBytes, 0, length);
            }

            DsPerf.reset();

            start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
              DsSipMsgParser.parse(impl, msgBytes, 0, length);
            }
            end = System.currentTimeMillis();

            System.out.println();
            if (DsPerf.ON) System.out.println(DsPerf.getResults());
            System.out.println(
                "Avg time / parse = "
                    + (((double) (end - start) / count) * 1000.0)
                    + " microseconds");
            System.out.println();

            System.out.println("With all deep headers (first in each list only):");
            DsSipMessage.initDeepHeaders(DsSipMessage.ALL_HEADERS);
            for (int i = 0; i < warmup; i++) {
              DsSipMsgParser.parse(impl, msgBytes, 0, length);
            }

            DsPerf.reset();

            start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
              DsSipMsgParser.parse(impl, msgBytes, 0, length);
            }
            end = System.currentTimeMillis();
          } else // just do it once
          {
            for (int i = 0; i < warmup; i++) {
              DsSipMsgParser.parse(impl, msgBytes, 0, length);
            }

            DsPerf.reset();

            start = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
              DsSipMsgParser.parse(impl, msgBytes, 0, length);
            }
            end = System.currentTimeMillis();
          }
        } else if (args[2].equalsIgnoreCase("stream")) {
          for (int i = 0; i < warmup; i++) {
            DsSipMsgParser.parse(impl, msgStream.readMsg());
            msgStream.reset();
          }

          DsPerf.reset();

          start = System.currentTimeMillis();
          for (int i = 0; i < count; i++) {
            DsSipMsgParser.parse(impl, msgStream.readMsg());
            msgStream.reset();
          }
          end = System.currentTimeMillis();
        } else {
          usage();
          System.exit(1);
        }

        System.out.println();
        if (DsPerf.ON) System.out.println(DsPerf.getResults());
        System.out.println(
            "Avg time / parse = " + (((double) (end - start) / count) * 1000.0) + " microseconds");
        System.out.println();
      } else {
        usage();
        System.exit(1);
      }

      if (args[0].equalsIgnoreCase("default")) {
        DsSipMessage message = ((DsSipDefaultMessageListenerFactory) impl).getMessage();
        try {
          if (args[1].equalsIgnoreCase("one")) {
            // CAFFEINE 3.1 DEVELOPMENT - Codenomicon test, parse SDP body
            System.out.println();
            System.out.println("BEGIN getMimeBodyValidate()");
            System.out.println("message..getMimeBody() = [" + message.getMimeBody() + "]");
            System.out.println(
                "message..getMimeBodyValidate() = [" + message.getMimeBodyValidate() + "]");
            // message.getMimeBodyValidate();
            System.out.println("END getMimeBodyValidate()");
            System.out.println();

            // DsSipMsgParser.parse(impl, msgStream.readMsg());
            DsSipMsgParser.parse(impl, msgStream.readMsg());
            message = ((DsSipDefaultMessageListenerFactory) impl).getMessage();
            message.write(System.out);

            // Original test of clone
            System.out.println();
            System.out.println("<<<<<<<<<<<<<<<<< MESSAGE >>>>>>>>>>>>>>>>>>>>");
            System.out.println();
            message.write(System.out);
            System.out.println();
            System.out.println("<<<<<<<<<<<<<<<<< CLONE >>>>>>>>>>>>>>>>>>>>");
            System.out.println();
            DsSipMessage clone = (DsSipMessage) message.clone();
            clone.write(System.out);
            System.out.println();
            System.out.println(
                "<<<<<<<<<<<<<<<<< (MESSAGE == CLONE) = "
                    + message.equals(clone)
                    + " >>>>>>>>>>>>>>>>>>>>");
            System.out.println();
            System.out.println();
            System.out.println(
                "<<<<<<<<<<<<<<<<< (CLONE == MESSAGE)  = "
                    + clone.equals(message)
                    + " >>>>>>>>>>>>>>>>>>>>");
            System.out.println();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (Exception e) {
      System.out.println("Caught Exception: " + e);
      e.printStackTrace();
    }
  }
}

/* CAFFEINE 2.0 DEVELOPMENT - Changed class hierarchy to add support for MIME body and Sipfrag parsing.
     This internal class was moved to a new class MsgBytes.java
final class MsgBytes
*/
