// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import java.io.UnsupportedEncodingException;
import java.net.*;

/** A class for String manipulation. Should use DsSipObject.DsByteString instead. */
public final class DsString {
  // data for getSniffDisplay

  private static final String SPACE = " ";
  private static final String SPACE_2 = "  ";
  private static final String SPACE_3 = "   ";
  private static final String SPACE_4 = "    ";
  private static final String SPACE_5 = "     ";
  private static final String LFCR = "\r\n";
  private static final String CRLF = "\n\r";
  private static final String HEXPREFIX = "0x";
  private static final String ZERO = "0";
  private static final String ZERO_2 = "00";
  private static final String ZERO_3 = "000";

  private static final int LINE_LEN = 16;
  private static final int PERIOD = '.';
  private static final int CR = '\n';
  private static final int LF = '\r';
  private static final int DASH = '-';

  /** The private constructor. Disallow construction, static methods only. */
  private DsString() {}

  /**
   * Get the bytes from a String. Tries to cast directly from chars to bytes. If there is UTF-8,
   * then it returns String.getBytes("UTF-8"). Otherwise, the casting is fine, ASCII only.
   *
   * @param str the String to get the bytes from
   * @return the same thing that String.getBytes() would return
   */
  public static byte[] getBytes(String str) {
    int strLen = str.length();
    byte strBytes[] = new byte[strLen];

    char ch;

    for (int i = 0; i < strLen; i++) {
      ch = str.charAt(i);
      if (ch > 127) {
        // Since there is a non-ASCII char in this string,
        // let the Java String class do this conversion
        try {
          return str.getBytes(DsSipConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
          return str.getBytes();
        }
      }

      strBytes[i] = (byte) ch;
    }

    return strBytes;
  }

  /**
   * Return a sniffer-like display of this data.
   *
   * <p>
   *
   * <pre>
   *     format: HHHH NN NN NN NN NN NN NN NN-NN NN NN NN NN NN NN NN  1234567890123456
   * </pre>
   *
   * @param buffer the message to transform
   * @return a sniffer-like display (hex, aside char representation) of the data. This code was
   *     taken from the uactrl tool., laddr);
   */
  public static String toSnifferDisplay(byte[] buffer) {
    return toSnifferDisplay(buffer, 0, buffer.length);
  }

  /**
   * Return a sniffer-like display of this data.
   *
   * <p>
   *
   * <pre>
   *     format: HHHH NN NN NN NN NN NN NN NN-NN NN NN NN NN NN NN NN  1234567890123456
   * </pre>
   *
   * @param buffer the message to transform
   * @param offset the start of the data to transform
   * @param count the number of bytes to transform
   * @return a sniffer-like display (hex, aside char representation) of the data. This code was
   *     taken from the uactrl tool., laddr);
   */
  public static String toSnifferDisplay(byte[] buffer, int offset, int count) {
    StringBuffer sb = new StringBuffer();
    for (int buffCtr = offset; buffCtr < count; buffCtr += LINE_LEN) {
      // format the address
      String hexStr = Integer.toHexString(buffCtr);
      sb.append(HEXPREFIX);
      switch (hexStr.length()) {
        case 1:
          sb.append(ZERO_3);
          break;
        case 2:
          sb.append(ZERO_2);
          break;
        case 3:
          sb.append(ZERO);
          break;
      }
      sb.append(hexStr);
      // sb.append(buffCtr); // to display address in decimal
      sb.append(SPACE);

      int lineSize = Math.min(LINE_LEN, count - buffCtr);
      int lineLast = lineSize + buffCtr;
      for (int lineCtr = buffCtr; lineCtr < lineLast; lineCtr++) {
        int hex = buffer[lineCtr] & 0xFF;
        if (hex < 16) sb.append(ZERO);
        sb.append(Integer.toHexString(hex));
        sb.append(SPACE);
      }
      // need to handle padding
      for (int padCtr = 16 - lineSize; padCtr > 0; padCtr--) sb.append(SPACE_3);

      // output the chars
      sb.append(SPACE_2);
      for (int lineCtr = buffCtr; lineCtr < lineLast; lineCtr++) {
        int hex = buffer[lineCtr] & 0xFF;
        if (hex < 32 || hex > 127) sb.append((char) PERIOD);
        else sb.append((char) hex);
      }
      sb.append(LFCR);
    }
    return sb.toString();
  }

  /**
   * Create a String from the supplied bytes. Tries to cast directly from bytes to chars. If there
   * is UTF-8, then it returns new String.(byte[]). Otherwise, the casting is fine, ASCII only.
   *
   * @param bytes the bytes to turn into a String
   * @return the same thing that new String(byte[]) would return
   */
  public static String newString(byte[] bytes) {
    return newString(bytes, 0, bytes.length);
  }

  /**
   * Get aString from the bytes. Tries to cast directly from bytes to chars. If there is UTF-8, then
   * it returns new String.(byte[]). Otherwise, the casting is fine, ASCII only.
   *
   * @param bytes the bytes to turn into a String
   * @param offset the starting index
   * @param count the number of the bytes array to put into the string
   * @return the same thing that new String(byte[], int, int) would return
   */
  public static String newString(byte[] bytes, int offset, int count) {
    // using the deprecated method is twice as fast, since the
    // char array constructor copies the chars, we can skip one
    // array allocation and one arraycopy
    int end = offset + count;
    for (int i = offset; i < end; i++) {
      if (bytes[i] < 0) {
        // let Java do its thing
        return new String(bytes);
      }
    }

    return new String(bytes, 0, offset, count);
    /*
            char strChars[] = new char[count];
            int j = 0;
            int end = offset + count;

            for (int i = offset; i < end; i++)
            {
                if (bytes[i] < 0)
                {
                    // let Java do its thing
                    return new String(bytes);
                }

                strChars[j++] = (char)bytes[i];
            }

            return new String(strChars);
    */
  }

  /**
   * Use to be optimized, now it is just a wrapper for addr.getHostAddress().
   *
   * @param addr the address to use, must not be null
   * @return addr.getHostAddress()
   * @deprecated just use addr.getHostAddress() directly
   */
  public static String getHostAddress(InetAddress addr) {
    return addr.getHostAddress();
    /*
            byte bytes[] = addr.getAddress();

            StringBuffer sb = new StringBuffer(15); // max length = 111.111.111.111 - 15

            // make sure that the ints are positive representations of the bytes
            sb.append(DsIntStrCache.intToStr((int)bytes[0] & 0x000000ff));
            sb.append('.');
            sb.append(DsIntStrCache.intToStr((int)bytes[1] & 0x000000ff));
            sb.append('.');
            sb.append(DsIntStrCache.intToStr((int)bytes[2] & 0x000000ff));
            sb.append('.');
            sb.append(DsIntStrCache.intToStr((int)bytes[3] & 0x000000ff));

            return sb.toString();
    */
  }

  /*--
      public static String getHostAddress(InetAddress addr)
      {
          return DsByteString.newString(getHostBytes(addr));
      }
  --*/

  /**
   * Just a wrapper for new DsByteString(addr.getHostAddress()). No longer optimized.
   *
   * @param addr the address to get the host address of, must not be null
   * @return new DsByteString(addr.getHostAddress())
   */
  public static DsByteString getHostByteString(InetAddress addr) {
    return new DsByteString(addr.getHostAddress());
    // return new DsByteString( getHostBytes(addr) );
  }

  /**
   * Just a wrapper for DsByteString.getBytes(addr.getHostAddress()). No longer optimized.
   *
   * @param addr the address to get the host address of, must not be null
   * @return DsByteString.getBytes(addr.getHostAddress())
   */
  public static byte getHostBytes(InetAddress addr)[] {
    return DsByteString.getBytes(addr.getHostAddress());
    /*
            byte bytes[] = addr.getAddress();

            ByteBuffer sb = ByteBuffer.newInstance(15); // max length = 111.111.111.111 - 15

            // make sure that the ints are positive representations of the bytes
            sb.write(DsIntStrCache.intToBytes((int)bytes[0] & 0x000000ff));
            sb.write('.');
            sb.write(DsIntStrCache.intToBytes((int)bytes[1] & 0x000000ff));
            sb.write('.');
            sb.write(DsIntStrCache.intToBytes((int)bytes[2] & 0x000000ff));
            sb.write('.');
            sb.write(DsIntStrCache.intToBytes((int)bytes[3] & 0x000000ff));

            return sb.toByteArray();
    */
  }

  public static String toStunDebugString(byte[] data) {
    boolean unknown = false;
    StringBuffer sb = new StringBuffer(256);

    if (data[0] == 0 && data[1] == 1) {
      sb.append("Binding Request\n");
    } else if (data[0] == 1 && data[1] == 1) {
      sb.append("Binding Response\n");
    } else {
      unknown = true;
      sb.append("Unknown Type\n");
    }

    for (int i = 0; i < data.length; i++) {
      if (data[i] >= 0 && data[i] <= 0xf) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(data[i] & 0x000000ff));
      sb.append(' ');

      if ((i + 1) % 8 == 0) {
        sb.append('\n');
      }
    }
    sb.append('\n');

    if (unknown) {
      return sb.toString();
    }

    int msgLen = (data[2] << 8) | (data[3] & 0x000000ff);
    sb.append("Message Length = ");
    sb.append(msgLen);
    sb.append('\n');

    int index = 20; // STUN_HEADER_LENGTH;

    while (index < data.length) {
      sb.append("Type = ");
      switch (data[index + 1]) {
        case 1:
          sb.append("MAPPED-ADDRESS\n");
          break;
        case 2:
          sb.append("RESPONSE-ADDRESS\n");
          break;
        case 3:
          sb.append("CHANGE-REQUEST\n");
          break;
        case 4:
          sb.append("SOURCE-ADDRESS\n");
          break;
        case 5:
          sb.append("CHANGED-ADDRESS\n");
          break;
        case 6:
          sb.append("USERNAME\n");
          break;
        case 7:
          sb.append("PASSWORD\n");
          break;
        case 8:
          sb.append("MESSAGE-INTEGRITY\n");
          break;
        case 9:
          sb.append("ERROR-CODE\n");
          break;
        case 10:
          sb.append("UNKNOWN-ATTRIBUTES\n");
          break;
        case 11:
          sb.append("REFLECTED-FROM\n");
          break;
        default:
          sb.append("UNKNOWN " + (int) data[index + 1] + "\n");
      }

      int len = (data[index + 2] << 8) | (data[index + 3] & 0x000000ff);
      sb.append("Length = ");
      sb.append(len);
      sb.append('\n');

      // known types for responses
      // RESPONSE-ADDRESS / SOURCE-ADDRESS / CHANGED-ADDRESS
      if (data[index + 1] == 1 || data[index + 1] == 4 || data[index + 1] == 5) {
        index += 4; // 2 for type and 2 for length
        sb.append("Family = ");
        if (data[index + 1] == 1 /*IPv4*/) {
          sb.append("IPv4");
        } else {
          sb.append("UNKNOWN");
        }
        sb.append('\n');

        int port = (data[index + 2] << 8) | (data[index + 3] & 0x000000ff);
        sb.append("Port = ");
        sb.append(port);
        sb.append('\n');

        sb.append("Address = ");
        for (int i = index + 4; i < index + 8; i++) {
          sb.append(data[i] & 0x000000ff);
          if (i != index + 7) {
            sb.append('.');
          }
        }
        sb.append('\n');

        index -= 4;
      }
      index += len + 4;
    }

    return sb.toString();
  }
}
