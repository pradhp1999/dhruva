/*

 FILENAME:    DsMimeMultipartParser.java


 DESCRIPTION: A parser for commonly use multipart/mixed and multipart/alternative content types

 MODULE:      DsMimeMultipartParser

 AUTHOR:      Michael Zhou (xmzhou@cisco.com)

 COPYRIGHT:

 Copyright (c) 2004 by Cisco Systems, Inc.
 All rights reserved.

*/

package com.cisco.dhruva.DsLibs.DsSipMime;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsSipParser.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.text.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/** An implementation of DsMimeBodyParser for multipart content type. */
public class DsMimeMultipartParser implements DsMimeBodyParser {
  /** single instance of multipart parser */
  private static DsMimeMultipartParser instance = new DsMimeMultipartParser();
  /** Logger */
  private static final Logger logger = DsLog4j.mimeCat;
  /** error message format */
  private static final String WRONG_CONTENT_TYPE =
      "The multipart parser cannot parse content type: {0}";
  /** error message */
  private static final String NULL_OR_EMPTY_BOUNDARY =
      "Boundary string is null or empty for this multipart.";
  /** error message */
  private static final String NO_CLOSING_BOUNDARY = "No closing boundary found.";
  /** error message */
  private static final String INVALID_CLOSING_BOUNDARY =
      "Invalid closing boundary (invalid characters after ending --)";
  /** error message */
  private static final String WRONG_STARTING_OR_BAD_CLOSING_BOUNDARY =
      "Wrong starting boundary or bad closing boundary!";
  /** error message */
  private static final String INVALID_MULTIPART = "Invalid multipart.";

  ////////////////////
  //   CONSTRUCTORS
  ////////////////////

  /** Private constructor. */
  private DsMimeMultipartParser() {}

  /////////////////////////
  //       METHODS
  /////////////////////////

  /** Returns the single instance of the parser. */
  public static DsMimeMultipartParser getInstance() {
    return instance;
  }

  public void parse(DsMimeEntity entity) throws DsSipParserException {
    // Note: To handle other subtypes of multipart, we need some listeners.
    // Right now, we only handle mixed and alternative subtypes.

    DsMimeBody mimeBody = null;
    if (entity == null || (mimeBody = entity.getMimeBody()) == null || mimeBody.isParsed()) {
      return;
    }

    // For now, make sure we deal with mixed or alternative subtypes only.
    DsByteString bodyType = entity.getBodyType();
    if (!DsMimeContentManager.MIME_MT_MULTIPART_MIXED.equalsIgnoreCase(bodyType)
        && !DsMimeContentManager.MIME_MT_MULTIPART_ALTERNATIVE.equalsIgnoreCase(bodyType)) {
      // Parsing algorithm should be the same for all multipart types, but the stack
      // doesn't know how to create instances of types other than mixed or alternative.
      String es = MessageFormat.format(WRONG_CONTENT_TYPE, new Object[] {bodyType});
      if (logger.isEnabled(Level.WARN)) {
        logger.log(Level.WARN, es);
        logger.log(Level.WARN, mimeBody);
      }
      throw new DsSipParserException(es);
    }

    // boundary
    DsByteString delimiter = entity.getContentTypeParameter(DsSipConstants.BS_BOUNDARY);
    if (delimiter == null) {
      if (logger.isEnabled(Level.WARN)) {
        logger.log(Level.WARN, NULL_OR_EMPTY_BOUNDARY);
        logger.log(Level.WARN, mimeBody);
      }
      throw new DsSipParserException(NULL_OR_EMPTY_BOUNDARY);
    }
    byte[] boundary = null;
    byte[] delData = delimiter.data();
    int delOffset = delimiter.offset();
    int delCount = delimiter.length();
    if (delCount > 1
        && delData[delOffset] == DsSipConstants.B_QUOTE
        && delData[delOffset + delCount - 1] == DsSipConstants.B_QUOTE) {
      delOffset++;
      delCount -= 2;
    }
    if (delCount <= 0) {
      if (logger.isEnabled(Level.WARN)) {
        logger.log(Level.WARN, NULL_OR_EMPTY_BOUNDARY);
        logger.log(Level.WARN, mimeBody);
      }
      throw new DsSipParserException(NULL_OR_EMPTY_BOUNDARY);
    }
    boundary = new byte[delCount + 2];
    System.arraycopy(delData, delOffset, boundary, 2, delCount);
    boundary[0] = boundary[1] = DsSipConstants.B_HIPHEN;

    // unparsed string
    DsByteString unparsed = ((DsMimeUnparsedBody) mimeBody).getBytes();
    // body byte array
    byte[] body = unparsed.data();
    // body offset
    int bodyOffset = unparsed.offset();
    // end index of the body == body string length + body offset
    int bodyEnd = unparsed.length() + bodyOffset;
    // index in body
    int bodyIndex = bodyOffset;
    // boundary string length
    int boundLen = boundary.length;
    // index in boundary
    int boundIndex = 0;
    // start of current boundary
    int boundStart = bodyIndex;
    // found starting boundary line
    boolean foundStartBound = false;
    // start of body part
    int bodyPartStart = bodyIndex;

    DsMimeMultipartMixed parsedBody = null;
    if (bodyType.equalsIgnoreCase(DsMimeContentManager.MIME_MT_MULTIPART_MIXED)) {
      parsedBody = new DsMimeMultipartMixed();
    } else {
      parsedBody = new DsMimeMultipartAlternative();
    }

    while (true) {
      // match boundary with source
      boundIndex = 0;
      boundStart = bodyIndex;
      while (bodyIndex < bodyEnd
          && boundIndex < boundLen
          && body[bodyIndex++] == boundary[boundIndex++]) ;
      if (boundIndex == boundLen) // the line starts with boundary string
      {
        // eat trailing white spaces
        while ((bodyIndex < bodyEnd - 1) && (body[bodyIndex] == ' ' || body[bodyIndex] == '\t'))
          bodyIndex++;
        if (bodyIndex >= bodyEnd - 1) {
          // Running out of characters without finding the trailing --
          if (logger.isEnabled(Level.WARN)) {
            logger.log(Level.WARN, NO_CLOSING_BOUNDARY);
            logger.log(Level.WARN, mimeBody);
          }
          throw new DsSipParserException(NO_CLOSING_BOUNDARY);
        }
        // RFC: MUST be CRLF, ie, 0x0D 0x0A
        //      We allow CRLF, or CR, or LF
        if (body[bodyIndex] == '\r' || body[bodyIndex] == '\n') {
          if (foundStartBound) {
            // found a body part
            try {
              parsedBody.addPart(
                  new DsMimeEntity(body, bodyPartStart, boundStart - bodyPartStart - 1));
            } catch (DsSipParserListenerException ex) {
              if (logger.isEnabled(Level.WARN)) {
                logger.log(Level.WARN, ex);
                logger.log(Level.WARN, mimeBody);
              }
              throw new DsSipParserException(ex);
            }
          } else {
            // have not found a starting boundary yet
            foundStartBound = true;
            parsedBody.setPreamble(
                (boundStart == 0)
                    ? null
                    : new DsByteString(body, bodyOffset, boundStart - bodyOffset));
          }
          if (body[bodyIndex] == '\r' && body[bodyIndex + 1] == '\n') {
            bodyIndex += 2;
          } else {
            bodyIndex++;
          }
          bodyPartStart = bodyIndex;
        } else {
          if (foundStartBound && body[bodyIndex] == '-' && body[bodyIndex + 1] == '-') {
            // check if there are trailing characters other than white spaces
            bodyIndex += 2;
            while (bodyIndex < bodyEnd && (body[bodyIndex] == ' ' || body[bodyIndex] == '\t'))
              bodyIndex++;
            if (bodyIndex < bodyEnd) {
              // must also be CR or LF.
              if (body[bodyIndex] == '\n' || body[bodyIndex] == '\r') {
                try {
                  parsedBody.addPart(
                      new DsMimeEntity(body, bodyPartStart, boundStart - bodyPartStart - 1));
                } catch (DsSipParserListenerException ex) {
                  if (logger.isEnabled(Level.WARN)) {
                    logger.log(Level.WARN, ex);
                    logger.log(Level.WARN, mimeBody);
                  }
                  throw new DsSipParserException(ex);
                }

                // Read at most two chars
                int crlfCount = 1;
                while (crlfCount < 3
                    && bodyIndex < bodyEnd
                    && (body[bodyIndex] == '\n' || body[bodyIndex] == '\r')) {
                  crlfCount++;
                  bodyIndex++;
                }
                if (bodyIndex < bodyEnd) {
                  parsedBody.setEpilog(new DsByteString(body, bodyIndex, bodyEnd - bodyIndex));
                }
                entity.setMimeBody(parsedBody);
                parsedBody.setBoundary(delimiter);
                return;
              } else {
                if (logger.isEnabled(Level.WARN)) {
                  logger.log(Level.WARN, INVALID_CLOSING_BOUNDARY);
                  logger.log(Level.WARN, mimeBody);
                }
                throw new DsSipParserException(INVALID_CLOSING_BOUNDARY);
              }
            } else {
              entity.setMimeBody(parsedBody);
              parsedBody.setBoundary(delimiter);
              return;
            }
          } else {
            if (logger.isEnabled(Level.WARN)) {
              logger.log(Level.WARN, WRONG_STARTING_OR_BAD_CLOSING_BOUNDARY);
              logger.log(Level.WARN, mimeBody);
            }
            throw new DsSipParserException(WRONG_STARTING_OR_BAD_CLOSING_BOUNDARY);
          }
        }
      } else {
        if (foundStartBound) {
          while (bodyIndex < bodyEnd && body[bodyIndex++] != '\n') ;
          if (bodyIndex >= bodyEnd) {
            if (logger.isEnabled(Level.WARN)) {
              logger.log(Level.WARN, NO_CLOSING_BOUNDARY);
              logger.log(Level.WARN, mimeBody);
            }
            throw new DsSipParserException(NO_CLOSING_BOUNDARY);
          }
        } else {
          // still in preamble. move source string pointer to next line
          while (bodyIndex < bodyEnd && (body[bodyIndex] != '\n' && body[bodyIndex] != '\r'))
            bodyIndex++;
          while (bodyIndex < bodyEnd && (body[bodyIndex] == '\n' || body[bodyIndex] == '\r'))
            bodyIndex++;
          if (bodyIndex >= bodyEnd) {
            if (logger.isEnabled(Level.WARN)) {
              logger.log(Level.WARN, INVALID_MULTIPART);
              logger.log(Level.WARN, mimeBody);
            }
            throw new DsSipParserException(INVALID_MULTIPART);
          }
        }
      }
      if (bodyIndex >= bodyEnd) {
        if (logger.isEnabled(Level.WARN)) {
          logger.log(Level.WARN, INVALID_MULTIPART);
          logger.log(Level.WARN, mimeBody);
        }
        throw new DsSipParserException(INVALID_MULTIPART);
      }
    }
  }
} // End of public class DsMimeMultipartParser
