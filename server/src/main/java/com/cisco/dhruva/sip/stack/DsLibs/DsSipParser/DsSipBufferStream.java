// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipParser;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/** A framing stream that frames SIP messages from a ByteBuffer. */
public class DsSipBufferStream {
  /**
   * Frames the SIP message from the specified buffer and returns the SIP message as a byte array.
   * The callee needs to keep calling this method until null is returned. Every invocation returns a
   * framed SIP message, and if a SIP message can not be framed then a BufferUnderflowException is
   * thrown and the specified buffer can be further used for reading more bytes.
   *
   * @return a byte[] that represents exactly 1 SIP message or null if the buffer passed in is
   *     positioned at its limit.
   * @param buffer the message buffer
   * @throws BufferUnderflowException when no more SIP message can be framed from the available
   *     bytes in the buffer.
   */
  public static byte[] readMsg(ByteBuffer buffer) {
    // set the mark at the first char, since we want to keep the entire msg
    // buffered until we find the end also, should pos always be 0 at this point?
    buffer.mark();

    int contentLength = -1;

    byte ch;

    boolean getContentLength = false;
    boolean done = false;
    boolean trimmedLeadingWS = false;

    // The first thing we want to do is check that the user hasn't asked us to read from
    // a buffer that has actually been read to its end
    if (buffer.position() == buffer.limit()) return null;

    // We have to do a few things to frame this message:
    // Look for "\nContent-Length:" or "\nl:" (don't forget about LWS before
    // the ':') (case insensitve). At the same time look for "\n\n" or "\n\r\n"
    // - empty header to separate body.
    // If content length exists, continue to look for the empty header
    //     then just read the body and we have a framed message.
    // else we found the empty header but no content length - read until EOF and
    //     we have a framed message

    while (!done) {
      ch = buffer.get();
      if (!trimmedLeadingWS && ch <= ' ') {
        buffer.mark();
        continue;
      }
      trimmedLeadingWS = true;

      // EOL start of all our tests
      if (ch == '\n') {
        ch = buffer.get();
        // short form content length if followed by white space or a ':'
        if (ch == 'l' || ch == 'L') {
          ch = buffer.get();
          if (ch == ':') {
            getContentLength = true;
          } else if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') { // end of header name
            getContentLength = true;
            ch = buffer.get();
            while (ch != ':') {
              ch = buffer.get();
            }
          }
        } else if (ch == 'C' || ch == 'c') {
          ch = buffer.get();
          if (ch == 'o' || ch == 'O') {
            ch = buffer.get();
            if (ch == 'n' || ch == 'N') {
              ch = buffer.get();
              if (ch == 't' || ch == 'T') {
                ch = buffer.get();
                if (ch == 'e' || ch == 'E') {
                  ch = buffer.get();
                  if (ch == 'n' || ch == 'N') {
                    ch = buffer.get();
                    if (ch == 't' || ch == 'T') {
                      ch = buffer.get();
                      if (ch == '-') {
                        ch = buffer.get();
                        if (ch == 'L' || ch == 'l') {
                          ch = buffer.get();
                          if (ch == 'e' || ch == 'E') {
                            ch = buffer.get();
                            if (ch == 'n' || ch == 'N') {
                              ch = buffer.get();
                              if (ch == 'g' || ch == 'G') {
                                ch = buffer.get();
                                if (ch == 't' || ch == 'T') {
                                  ch = buffer.get();
                                  if (ch == 'h' || ch == 'H') {
                                    // found Content-Length - now find white space or ':'
                                    ch = buffer.get();
                                    if (ch == ':') {
                                      getContentLength = true;
                                    } else if (ch == ' '
                                        || ch == '\t'
                                        || ch == '\r'
                                        || ch == '\n') // end of header name
                                    {
                                      getContentLength = true;
                                      ch = buffer.get();
                                      while (ch != ':') {
                                        ch = buffer.get();
                                      }
                                    }
                                  } // h
                                } // t
                              } // g
                            } // n
                          } // e
                        } // L
                      } // -
                    } // t
                  } // n
                } // e
              } // t
            } // n
          } // o
        } // C

        // second EOL
        else if (ch == '\r') {
          ch = buffer.get();
          if (ch == '\n') {
            break; // found empty header
          }
        } else if (ch == '\n') {
          break; // found empty header
        }

        // found the name part and ':' for content length - now get the value
        if (getContentLength) {
          getContentLength = false;

          StringBuffer sb = new StringBuffer();

          ch = buffer.get();

          while (ch == ' ' || ch == '\r' || ch == '\n') // remove leading ws
          {
            ch = buffer.get();
          }

          while (ch >= '0' && ch <= '9') // get digits
          {
            sb.append((char) ch);
            ch = buffer.get();
          }

          contentLength = Integer.parseInt(sb.toString());

          // Here we go, another strange border case.  Let's say that the content
          // length is the last header in the list and there is no CRLF, just LF.
          // In this case we have just read the LF that clues us into the fact
          // that the last empty header is next. We may also need to EOF handling
          // from above here as well. - jsm
          if (ch == '\n') { // already read the EOL for content length - need to see if empty
            // header follows
            ch = buffer.get();
            if (ch == '\n') {
              break; // found empty header
            } else if (ch == '\r') {
              ch = buffer.get();
              if (ch == '\n') {
                break; // found empty header
              }
            }
          }
        }
      } // if ch = \n
    } // while !done

    // Go to the end of the message as denoted by the Content-Length header
    if (buffer.position() + contentLength <= buffer.limit()) {
      buffer.position(buffer.position() + contentLength);
    } else {
      // The entire message is not here, move to the end
      buffer.position(buffer.limit());

      // This will throw the necessary BufferUnderflowException
      buffer.get();
    }

    // save the current limit which is the end of the buffer as passed in by the user
    int savedLimit = buffer.limit();
    // save the current position which at this point is the end of the message
    // as described by the content length
    int savedPos = buffer.position();

    // Here we are going to save the bytes that make up the message in a byte array
    // to pass back to the user

    // set the position back to the beginning of the actual message, without ws
    buffer.reset();
    // set the limit of the buffer to the end of the message; the point
    // which the saved position was holding
    buffer.limit(savedPos);
    // get enough bytes for the whole message, (limit - pos) bytes
    byte copiedBytes[] = new byte[buffer.remaining()];
    // since the position is now pointing to the beginning of the message
    // and the limit is at the end of the message, we can simply ask for the
    // what is in the buffer
    buffer.get(copiedBytes);
    // reset the limit to its passed in initial value
    buffer.limit(savedLimit);

    return copiedBytes;
  }
}
