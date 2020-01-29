// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipParser;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;

/**
 * Optimized class that directly accesses the BufferedInputStream data members to frame SIP messages
 * as they arrive on a stream.
 */
public class DsSipFrameStream extends BufferedInputStream {
  /**
   * This sets the maximum size of a message that can be read. Larger messages than this will cause
   * and IOException to be thrown and the stack will close the socket on this exception.
   */
  private static final int MARK_LIMIT =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_MAX_TCP_MSG_SIZE, DsConfigManager.PROP_MAX_TCP_MSG_SIZE_DEFAULT);

  /**
   * Constructor that takes an input stream.
   *
   * @param is the underlying input stream.
   */
  public DsSipFrameStream(InputStream is) {
    super(is);
    marklimit = MARK_LIMIT;
  }

  /**
   * Constructor that takes an input stream and a buffer size.
   *
   * @param is the underlying input stream.
   * @param size the buffer size.
   */
  public DsSipFrameStream(InputStream is, int size) {
    super(is, size);
    marklimit = MARK_LIMIT;
  }

  /*
      public void mark(int readlimit)
      {
          // mark not supported
      }
  */

  public boolean markSupported() {
    return false;
  }

  // public void reset() throws IOException
  // {
  // mark not supported
  // }

  public long skip(long n) throws IOException {
    // mark not supported
    throw new IOException("You may not use this method");
  }

  public int available() throws IOException {
    return 0;
  }

  public int read() throws IOException {
    throw new IOException("You may not use this method - use readMsg()");
  }

  public int read(byte[] b) throws IOException {
    throw new IOException("You may not use this method - use readMsg()");
  }

  public int read(byte[] b, int off, int len) throws IOException {
    throw new IOException("You may not use this method - use readMsg()");
  }

  /**
   * Frames a single SIP message from the underlying input stream.
   *
   * @return a byte[] that represents exactly 1 SIP message.
   * @throws IOException when there is an exception with the underlying input stream.
   */
  public byte[] readMsg() throws IOException {
    // set the mark at the first char, since we want to keep the entire msg buffered until we find
    // the end
    // also, should pos always be 0 at this point?
    markpos = pos;

    int contentLength = -1;

    byte ch;

    boolean getContentLength = false;
    boolean done = false;
    boolean trimmedLeadingWS = false;

    // CAFFEINE 2.0 DEVELOPMENT - CSCec65691
    // flag true if readByte() has not been called in this method
    boolean firstRead = true;

    // handled is Msg Parser // used to trim leading white space from the beginning of messages
    // handled is Msg Parser boolean isLeadingWhiteSpace = true;

    // We have to do a few things to frame this message:
    // Look for "\nContent-Length:" or "\nl:" (don't forget about LWS before the ':') (case
    // insensitve)
    // At the same time look for "\n\n" or "\n\r\n" - empty header to separate body
    // If content length exists, continue to look for the empty header
    //     then just read the body and we have a framed message.
    // else we found the empty header but no content length - read until EOF and we have a framed
    // message
    while (!done) {
      // CAFFEINE 2.0 DEVELOPMENT - CSCec65691
      ch = readByte(firstRead);
      firstRead = false;
      if (!trimmedLeadingWS && ch <= ' ') {
        markpos = pos;
        continue;
      }
      trimmedLeadingWS = true;

      // handled is Msg Parser if (isLeadingWhiteSpace)
      // handled is Msg Parser {
      // handled is Msg Parser if (ch <= ' ')
      // handled is Msg Parser {
      // handled is Msg Parser // the char after this WS is now the start of the msg
      // handled is Msg Parser markpos = pos;

      // handled is Msg Parser // and keep stripping WS until we see the first non-WS char
      // handled is Msg Parser continue;
      // handled is Msg Parser }
      // handled is Msg Parser isLeadingWhiteSpace = false;
      // handled is Msg Parser }

      // EOL start of all our tests
      if (ch == '\n') {
        try {
          // CAFFEINE 2.0 DEVELOPMENT - CSCec65691
          ch = readByte(firstRead);
        } catch (EOFException e) {
          // It might be that this mb.msg is just missing the final empty header
          // We will try to parse it anyway
          // No need to throw EOFExcetion here, it will be thrown on first read next time

          // hack - fix the message before passing to next parser - jsm
          byte copiedBytes[] = new byte[pos - markpos + 2];
          copiedBytes[copiedBytes.length - 2] = (byte) '\r';
          copiedBytes[copiedBytes.length - 1] = (byte) '\n';
          System.arraycopy(buf, markpos, copiedBytes, 0, pos - markpos);

          return copiedBytes;
        }

        // short form content length if followed by white space or a ':'
        if (ch == 'l' || ch == 'L') {
          ch = readByte(firstRead);
          if (ch == ':') {
            getContentLength = true;
          } else if (ch == ' ' || ch == '\t' || ch == '\r') // end of header name
          {
            getContentLength = true;
            ch = readByte(firstRead);
            while (ch != ':') {
              try {
                ch = readByte(firstRead);
              } catch (EOFException e) {
                done = true;
                break;
              }
            }
          } else if (ch == '\n') // end of header name
          {
            getContentLength = true;
            // white space must follow or the mb.msg is illegal
            // but, don't worry about that now, it will be caught later
            // just asumme white space and find the ':'
            ch = readByte(firstRead);
            while (ch != ':') {
              try {
                ch = readByte(firstRead);
              } catch (EOFException e) {
                done = true;
                break;
              }
            }
          }
        } else if (ch == 'C' || ch == 'c') {
          ch = readByte(firstRead);
          if (ch == 'o' || ch == 'O') {
            ch = readByte(firstRead);
            if (ch == 'n' || ch == 'N') {
              ch = readByte(firstRead);
              if (ch == 't' || ch == 'T') {
                ch = readByte(firstRead);
                if (ch == 'e' || ch == 'E') {
                  ch = readByte(firstRead);
                  if (ch == 'n' || ch == 'N') {
                    ch = readByte(firstRead);
                    if (ch == 't' || ch == 'T') {
                      ch = readByte(firstRead);
                      if (ch == '-') {
                        ch = readByte(firstRead);
                        if (ch == 'L' || ch == 'l') {
                          ch = readByte(firstRead);
                          if (ch == 'e' || ch == 'E') {
                            ch = readByte(firstRead);
                            if (ch == 'n' || ch == 'N') {
                              ch = readByte(firstRead);
                              if (ch == 'g' || ch == 'G') {
                                ch = readByte(firstRead);
                                if (ch == 't' || ch == 'T') {
                                  ch = readByte(firstRead);
                                  if (ch == 'h' || ch == 'H') {
                                    // found Content-Length - now find white space or ':'
                                    ch = readByte(firstRead);
                                    if (ch == ':') {
                                      getContentLength = true;
                                    } else if (ch == ' '
                                        || ch == '\t'
                                        || ch == '\r') // end of header name
                                    {
                                      getContentLength = true;
                                      ch = readByte(firstRead);
                                      while (ch != ':') {
                                        try {
                                          ch = readByte(firstRead);
                                        } catch (EOFException e) {
                                          done = true;
                                          break;
                                        }
                                      }
                                    } else if (ch == '\n') // end of header name
                                    {
                                      getContentLength = true;
                                      // white space must follow or the mb.msg is illegal
                                      // but, don't worry about that now, it will be caught later
                                      // just asumme white space and find the ':'
                                      ch = readByte(firstRead);
                                      while (ch != ':') {
                                        try {
                                          ch = readByte(firstRead);
                                        } catch (EOFException e) {
                                          done = true;
                                        }
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
          ch = readByte(firstRead);
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

          ch = readByte(firstRead);

          while (ch == ' ' || ch == '\r' || ch == '\n') // remove leading ws
          {
            ch = readByte(firstRead);
          }

          while (ch >= '0' && ch <= '9') // get digits
          {
            sb.append((char) ch);
            ch = readByte(firstRead);
          }

          contentLength = Integer.parseInt(sb.toString());

          // Here we go, another strange border case.    Let's say that the content length is the
          // last header in the list and there is no CRLF, just LF.    In this case we have just
          // read
          // the LF that clues us into the fact that the last empty header is next.
          // We may also need to EOF handling from above here as well. - jsm
          if (ch == '\n') // already read the EOL for content length - need to see if empty header
          // follows
          {
            ch = readByte(firstRead);
            if (ch == '\n') {
              break; // found empty header
            } else if (ch == '\r') {
              ch = readByte(firstRead);
              if (ch == '\n') {
                break; // found empty header
              }
            }
          }
        }
      } // if ch = \n
    } // while !done

    // need to make sure that the markpos does not go to -1 during this search
    if (contentLength == 0) // no body - nothing to do
    {
    } else if (contentLength == -1) // no content length - read until EOF
    {
      throw new IOException("Malformed message, No Content-Length Header found");

    } else if ((contentLength + (pos - markpos))
        > marklimit) // content length set but msg too large
    {
      throw new IOException(
          "SIP message too large to frame: "
              + (contentLength + (pos - markpos))
              + " is greater than the max size of: "
              + marklimit);
    } else // positive content length - read that data
    {
      int headerCount =
          pos - markpos; // number of chars in the start line and headers, with separator EOL
      int totalCount = headerCount + contentLength; // total number of chars in the entire msg

      // keep reading until we get enough bytes or until EOF
      while (true) {
        if ((markpos + totalCount) <= count) {
          // the rest of the msg is in the buffer already, just move to the end of the body
          pos = markpos + totalCount;
          break; // found entire msg
        } else {
          // there is more to the msg, force a read
          pos = count;
          int retval = super.read();
          if (retval == -1) {
            throw new EOFException("Unexpected EOF reached while parsing/framing.");
          }
        }
      }
    }

    // if (DsLog4j.messageCat.isEnabledFor(Level.DEBUG))
    // DsLog4j.messageCat.log(Level.DEBUG, "Framed the following message:\n" +
    // DsByteString.newString(mb.msg, 0, mb.i) + "** end of message **");

    // copy the bytes are return the correct sized array
    byte copiedBytes[] = new byte[pos - markpos];
    System.arraycopy(buf, markpos, copiedBytes, 0, copiedBytes.length);

    return copiedBytes;
  }

  /**
   * Method to get one bytes from the stream and put it in the class' array. The array will grow if
   * needed. The array in mb must be the scratch array, because it will get set back to the thread
   * local when it grows. Also, assumes 0 offset in mb and count = msg.length
   *
   * @param firstRead first time this method is called
   * @throws IOException when there is an exception with the underlying input stream.
   */
  // CAFFEINE 2.0 DEVELOPMENT - CSCec65691
  public byte readByte(boolean firstRead) throws IOException {
    int chi;

    // is the buffer full?
    if (pos >= count) {
      // no data available in the buffer, read from the stream
      // this will cause fill() to be called in super
      chi = super.read();
      if (chi == -1) {
        // CAFFEINE 2.0 DEVELOPMENT - CSCec65691
        if (firstRead) {
          throw new DsEOFException(DsEOFException.EC_FIRST_READ);
        } else {
          throw new EOFException("Unexpected EOF reached while parsing/framing.");
        }
      }

      // found data - return it
      return (byte) chi;
    }

    // otherwise, we can safely return the next byte from the buffer

    return buf[pos++];
  }

  //    public static void main(String[] args)
  //    {
  //        try
  //        {
  //            int readInt;
  //            int length = 0;
  //            byte msgBytes[] = new byte[128*1024];
  //
  //            readInt = System.in.read();
  //            while (readInt != -1)
  //            {
  //                msgBytes[length++] = (byte)readInt;
  //                readInt = System.in.read();
  //            }
  //
  //            ByteArrayInputStream byteStream = new ByteArrayInputStream(msgBytes, 0, length);
  //
  //            DsSipFrameStream msgStream = new DsSipFrameStream(byteStream);
  //
  //            DsSipMessageListenerFactory impl = DsSipDebugImpl.getInstance();
  //
  //            try
  //            {
  //                while (true)
  //                {
  //                    DsByteString str = new DsByteString(msgStream.readMsg());
  //
  //                    System.out.println("length = " + str.length());
  //                    System.out.println("[");
  //                    System.out.print(str);
  //                    System.out.println("]");
  //                    System.out.println();
  //                }
  //            }
  //            catch (EOFException e)
  //            {
  //                System.out.println();
  //            }
  //
  /// *
  //            int count = 150000;
  //            int warmup = 50000;
  //            long start = 0;
  //            long end = 0;
  //
  //            for (int i = 0; i < warmup; i++)
  //            {
  //                DsSipMsgParser.parse(impl, msgStream.readMsg());
  //                msgStream.reset();
  //            }
  //
  //            start = System.currentTimeMillis();
  //            for (int i = 0; i < count; i++)
  //            {
  //                DsSipMsgParser.parse(impl, msgStream.readMsg());
  //                msgStream.reset();
  //            }
  //            end = System.currentTimeMillis();
  //
  //            System.out.println("Avg time / parse = " + (((double)(end - start) / count) *
  // 1000.0) + " microseconds");
  //            System.out.println();
  // */
  //        }
  //        catch (Exception e)
  //        {
  //            System.out.println("Exception: " + e);
  //            e.printStackTrace();
  //        }
  //    }
}
