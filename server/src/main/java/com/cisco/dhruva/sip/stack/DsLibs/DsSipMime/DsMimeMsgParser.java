package com.cisco.dhruva.sip.stack.DsLibs.DsSipMime;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.*;

/**
 * The MIME message parser.
 *
 * @author Michael Zhou (xmzhou@cisco.com)
 */
public class DsMimeMsgParser extends DsMsgParserBase {
  /** Disallow instance creation from application code. */
  private DsMimeMsgParser() {}

  /**
   * Parses a byte array and create a MIME entity.
   *
   * @param msg the byte array that contains the MIME message to parse.
   * @return the created MIME entity.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static DsMimeEntity parse(byte msg[])
      throws DsSipParserListenerException, DsSipParserException {
    return parse(msg, 0, msg.length);
  }

  /**
   * Parses a byte array and create a MIME entity.
   *
   * @param msg the byte array that contains the MIME message to parse.
   * @param offset the start of the MIME message in the array.
   * @param count the number of bytes in the MIME message.
   * @return the created MIME entity.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  public static DsMimeEntity parse(byte msg[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {
    DsMimeEntity entity = new DsMimeEntity();
    parse(entity, msg, offset, count);
    return entity;
  }

  /**
   * Parses a byte array and create a MIME entity.
   *
   * @param mimeEntity the MIME entity to fill the headers in.
   * @param msg the byte array that contains the MIME message to parse.
   * @param offset the start of the MIME message in the array.
   * @param count the number of bytes in the MIME message.
   * @throws DsSipParserException if there is an exception while parsing.
   * @throws DsSipParserListenerException if the listener throws this exception.
   */
  static void parse(DsMimeEntity mimeEntity, byte msg[], int offset, int count)
      throws DsSipParserListenerException, DsSipParserException {

    int origOffset = offset;
    int origCount = count;
    boolean hasHeaders = true;
    try {
      // strip leading white space
      while (msg[offset] <= ' ' && msg[offset] != '\r') {
        ++offset;
        --count;
      }
      if (msg[offset] == '\r') {
        hasHeaders = false;
        ++offset;
        --count;
        if (offset < msg.length && msg[offset] == '\n') {
          ++offset;
          --count;
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw generateDsSipParserException(null, e, msg, origOffset, origCount);
    }

    MsgBytes mb = new MsgBytes(msg, offset, count);

    int exceptionCount = 0;

    try {
      if (hasHeaders) {
        DsSipHeaderListener headerListener = mimeEntity.getHeaderListener();

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
      }

      parseBody(mimeEntity, mb);

    } catch (DsSipParserListenerException e) {
      throw e;
    } catch (Exception e) {
      throw generateDsSipParserException(mimeEntity, e, msg, offset, count);
    }
  }
}
