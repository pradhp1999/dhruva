// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipParser.DsSipMsgParser;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Vector;

/**
 * A class for byte[] based Strings. Includes utilities formerly in {@link
 * com.dynamicsoft.DsLibs.DsUtil.DsString}.
 *
 * <p>These objects should be treated as immutable. That is, the {@link #data()} method returns the
 * actual underlying byte[] that represents this string. That array must not be modified, as you do
 * not know who else is using it. Use {@link #toByteArray()} to get a copy of the data is a byte
 * array if you need to modify anything or {@link #copy()} before mutating.
 *
 * <p>We could have enfored this, as the java.lang.String class does, but that would have prevented
 * us from getting some of the performance that we needed out of this class.
 */
public class DsByteString implements Cloneable, Serializable {

  /** The string "null", used when objects are null. */
  public static final DsByteString BS_NULL = new DsByteString("null");
  /** The string "true". */
  public static final DsByteString BS_TRUE = new DsByteString("true");
  /** The string "false". */
  public static final DsByteString BS_FALSE = new DsByteString("false");
  /** The empty string "". */
  public static final DsByteString BS_EMPTY_STRING;

  /** All possible chars for representing a number as a string. */
  private static final byte[] m_digits = {
    (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
    (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
    (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g', (byte) 'h',
    (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
    (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't',
    (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z'
  };

  /** A static byte array of length 0. */
  public static final byte[] EMPTY_BYTES = new byte[0];

  static {
    BS_EMPTY_STRING = new DsByteString();
    BS_EMPTY_STRING.data = EMPTY_BYTES;
    BS_EMPTY_STRING.offset = 0;
    BS_EMPTY_STRING.count = 0;
  }

  /** The byte array that contains the data for this string. */
  protected byte[] data;
  /** The start of the bytes in this string in <code>data</code>. */
  protected int offset;
  /** The number of bytes that are part of this string. */
  protected int count;
  /** The hash code for this string. */
  private int hash;

  /** The private constructor */
  private DsByteString() {}

  /**
   * Factory constructor - returns static DsByteString for the follow:<br>
   * The Empty String <br>
   * Method Names <br>
   * Transport Types.<br>
   *
   * @param data the byte[] that contains the data
   * @param offset the start of the data
   * @param count the number of bytes in the data
   * @return same thing the new DsByteString(byte[], int, int) returns, except it may just be a
   *     static reference to an already created object - do not modify!
   */
  public static DsByteString newInstance(byte[] data, int offset, int count) {
    if (data == null || count == 0) {
      return BS_EMPTY_STRING;
    }

    switch (count) {
      case 1:
        if (data[offset] == '*') {
          return DsSipConstants.BS_WILDCARD;
        }
        break;
      case 2:
        if (data[offset] == 'O' && data[offset + 1] == 'k') {
          return DsSipResponseCode.DS_BS_RESPONSE_OK;
        }
        break;
      case 3:
        if (data[offset] == 'A' && data[offset + 1] == 'C' && data[offset + 2] == 'K') {
          return DsSipConstants.BS_ACK;
        } else if (data[offset] == 'B' && data[offset + 1] == 'Y' && data[offset + 2] == 'E') {
          return DsSipConstants.BS_BYE;
        } else if (data[offset] == 'U' && data[offset + 1] == 'D' && data[offset + 2] == 'P') {
          return DsSipTransportType.UC_BS_UDP;
        } else if (data[offset] == 'u' && data[offset + 1] == 'd' && data[offset + 2] == 'p') {
          return DsSipTransportType.BS_UDP;
        } else if (data[offset] == 'T' && data[offset + 1] == 'C' && data[offset + 2] == 'P') {
          return DsSipTransportType.UC_BS_TCP;
        } else if (data[offset] == 't' && data[offset + 1] == 'c' && data[offset + 2] == 'p') {
          return DsSipTransportType.BS_TCP;
        } else if (data[offset] == 'T' && data[offset + 1] == 'L' && data[offset + 2] == 'S') {
          return DsSipTransportType.UC_BS_TLS;
        } else if (data[offset] == 't' && data[offset + 1] == 'l' && data[offset + 2] == 's') {
          return DsSipTransportType.BS_TLS;
        } else if (data[offset] == 'S' && data[offset + 1] == 'I' && data[offset + 2] == 'P') {
          return DsSipConstants.BS_SIP;
        } else if (data[offset] == 's' && data[offset + 1] == 'i' && data[offset + 2] == 'p') {
          return DsSipConstants.BS_LSIP;
        } else if (data[offset] == '2' && data[offset + 1] == '.' && data[offset + 2] == '0') {
          return DsSipConstants.BS_VERSION;
        } else if (data[offset] == 's' && data[offset + 1] == 'd' && data[offset + 2] == 'p') {
          return DsSipConstants.BS_SDP;
        }
        break;
      case 4:
        if (data[offset] == 'I'
            && data[offset + 1] == 'N'
            && data[offset + 2] == 'F'
            && data[offset + 3] == 'O') {
          return DsSipConstants.BS_INFO;
        } else if (data[offset] == 'P'
            && data[offset + 1] == 'I'
            && data[offset + 2] == 'N'
            && data[offset + 3] == 'G') {
          return DsSipConstants.BS_PING;
        } else if (data[offset] == 'S'
            && data[offset + 1] == 'C'
            && data[offset + 2] == 'T'
            && data[offset + 3] == 'P') {
          return DsSipTransportType.UC_BS_SCTP;
        } else if (data[offset] == 's'
            && data[offset + 1] == 'c'
            && data[offset + 2] == 't'
            && data[offset + 3] == 'p') {
          return DsSipTransportType.BS_SCTP;
        } else if (data[offset] == 't'
            && data[offset + 1] == 'e'
            && data[offset + 2] == 'x'
            && data[offset + 3] == 't') {
          return DsSipConstants.BS_TEXT;
        } else if (data[offset] == 'h'
            && data[offset + 1] == 't'
            && data[offset + 2] == 'm'
            && data[offset + 3] == 'l') {
          return DsSipConstants.BS_HTML;
        }
        break;
      case 5:
        if (data[offset] == 'P'
            && data[offset + 1] == 'R'
            && data[offset + 2] == 'A'
            && data[offset + 3] == 'C'
            && data[offset + 4] == 'K') {
          return DsSipConstants.BS_PRACK;
        } else if (data[offset] == 'p'
            && data[offset + 1] == 'l'
            && data[offset + 2] == 'a'
            && data[offset + 3] == 'i'
            && data[offset + 4] == 'n') {
          return DsSipConstants.BS_PLAIN;
        } else if (data[offset] == 'R'
            && data[offset + 1] == 'E'
            && data[offset + 2] == 'F'
            && data[offset + 3] == 'E'
            && data[offset + 4] == 'R') {
          return DsSipConstants.BS_REFER;
        }
        break;
      case 6:
        if (data[offset] == 'I'
            && data[offset + 1] == 'N'
            && data[offset + 2] == 'V'
            && data[offset + 3] == 'I'
            && data[offset + 4] == 'T'
            && data[offset + 5] == 'E') {
          return DsSipConstants.BS_INVITE;
        } else if (data[offset] == 'N'
            && data[offset + 1] == 'O'
            && data[offset + 2] == 'T'
            && data[offset + 3] == 'I'
            && data[offset + 4] == 'F'
            && data[offset + 5] == 'Y') {
          return DsSipConstants.BS_NOTIFY;
        } else if (data[offset] == 'T'
            && data[offset + 1] == 'r'
            && data[offset + 2] == 'y'
            && data[offset + 3] == 'i'
            && data[offset + 4] == 'n'
            && data[offset + 5] == 'g') {
          return DsSipResponseCode.DS_BS_RESPONSE_TRYING;
        } else if (data[offset] == 'C'
            && data[offset + 1] == 'A'
            && data[offset + 2] == 'N'
            && data[offset + 3] == 'C'
            && data[offset + 4] == 'E'
            && data[offset + 5] == 'L') {
          return DsSipConstants.BS_CANCEL;
        } else if (data[offset] == 'U'
            && data[offset + 1] == 'P'
            && data[offset + 2] == 'D'
            && data[offset + 3] == 'A'
            && data[offset + 4] == 'T'
            && data[offset + 5] == 'E') {
          return DsSipConstants.BS_UPDATE;
        }
        break;
      case 7:
        if (data[offset] == 'M'
            && data[offset + 1] == 'E'
            && data[offset + 2] == 'S'
            && data[offset + 3] == 'S'
            && data[offset + 4] == 'A'
            && data[offset + 5] == 'G'
            && data[offset + 6] == 'E') {
          return DsSipConstants.BS_MESSAGE;
        } else if (data[offset] == 'O'
            && data[offset + 1] == 'P'
            && data[offset + 2] == 'T'
            && data[offset + 3] == 'I'
            && data[offset + 4] == 'O'
            && data[offset + 5] == 'N'
            && data[offset + 6] == 'S') {
          return DsSipConstants.BS_OPTIONS;
        } else if (data[offset] == 'P'
            && data[offset + 1] == 'U'
            && data[offset + 2] == 'B'
            && data[offset + 3] == 'L'
            && data[offset + 4] == 'I'
            && data[offset + 5] == 'S'
            && data[offset + 6] == 'H') {
          return DsSipConstants.BS_PUBLISH;
        } else if (data[offset] == 'R'
            && data[offset + 1] == 'i'
            && data[offset + 2] == 'n'
            && data[offset + 3] == 'g'
            && data[offset + 4] == 'i'
            && data[offset + 5] == 'n'
            && data[offset + 6] == 'g') {
          return DsSipResponseCode.DS_BS_RESPONSE_RINGING;
        }
        break;
      case 8:
        if (data[offset] == 'R'
            && data[offset + 1] == 'E'
            && data[offset + 2] == 'G'
            && data[offset + 3] == 'I'
            && data[offset + 4] == 'S'
            && data[offset + 5] == 'T'
            && data[offset + 6] == 'E'
            && data[offset + 7] == 'R') {
          return DsSipConstants.BS_REGISTER;
        }
        break;
      case 9:
        if (data[offset] == 'S'
            && data[offset + 1] == 'U'
            && data[offset + 2] == 'B'
            && data[offset + 3] == 'S'
            && data[offset + 4] == 'C'
            && data[offset + 5] == 'R'
            && data[offset + 6] == 'I'
            && data[offset + 7] == 'B'
            && data[offset + 8] == 'E') {
          return DsSipConstants.BS_SUBSCRIBE;
        }
        break;
      case 10:
        break;
      case 11:
        if (data[offset] == 'a'
            && data[offset + 1] == 'p'
            && data[offset + 2] == 'p'
            && data[offset + 3] == 'l'
            && data[offset + 4] == 'i'
            && data[offset + 5] == 'c'
            && data[offset + 6] == 'a'
            && data[offset + 7] == 't'
            && data[offset + 8] == 'i'
            && data[offset + 9] == 'o'
            && data[offset + 10] == 'n') {
          return DsSipConstants.BS_APPLICATION;
        }
        break;
    }
    DsByteString bs = new DsByteString();
    bs.data = data;
    bs.offset = offset;
    bs.count = count;

    return bs;
  }

  /**
   * Wrapper method for newInstance(byte[], int, int).
   *
   * @param data the String to represent as a DsByteString.
   * @return same thing the new DsByteString(byte[], int, int) returns, except it may just be a
   *     static reference to an already created object - do not modify!
   */
  public static DsByteString newInstance(String data) {
    byte[] bytes = getBytes(data);

    return newInstance(bytes, 0, bytes.length);
  }

  /**
   * Factory constructor - returns static lower case DsByteString for the follow:<br>
   * The Empty String <br>
   * Parameter Names (case insensitive).<br>
   *
   * @param data the byte[] that contains the data
   * @param offset the start of the data
   * @param count the number of bytes in the data
   * @return same thing the new DsByteString(byte[], int, int) returns, except it may just be a
   *     static reference to an already created object - do not modify!
   */
  public static DsByteString newLower(byte[] data, int offset, int count) {
    if (data == null || count == 0) {
      return BS_EMPTY_STRING;
    }

    switch (count) {
      case 1:
        if (data[offset] == 'q' || data[offset] == 'Q') {
          return DsSipConstants.BS_Q;
        }
        break;
      case 2:
        if ((data[offset] == 'n' || data[offset] == 'N')
            && (data[offset + 1] == 'c' || data[offset + 1] == 'C')) {
          return DsSipConstants.BS_NC;
        } else if ((data[offset] == 'l' || data[offset] == 'L')
            && (data[offset + 1] == 'r' || data[offset + 1] == 'R')) {
          return DsSipConstants.BS_LR;
        }
        break;
      case 3:
        if ((data[offset] == 't' || data[offset] == 'T')
            && (data[offset + 1] == 'a' || data[offset + 1] == 'A')
            && (data[offset + 2] == 'g' || data[offset + 2] == 'G')) {
          return DsSipConstants.BS_TAG;
        } else if ((data[offset] == 't' || data[offset] == 'T')
            && (data[offset + 1] == 't' || data[offset + 1] == 'T')
            && (data[offset + 2] == 'l' || data[offset + 2] == 'L')) {
          return DsSipConstants.BS_TTL;
        } else if ((data[offset] == 'u' || data[offset] == 'U')
            && (data[offset + 1] == 'r' || data[offset + 1] == 'R')
            && (data[offset + 2] == 'i' || data[offset + 2] == 'I')) {
          return DsSipConstants.BS_URI;
        } else if ((data[offset] == 'q' || data[offset] == 'Q')
            && (data[offset + 1] == 'o' || data[offset + 1] == 'O')
            && (data[offset + 2] == 'p' || data[offset + 2] == 'P')) {
          return DsSipConstants.BS_QOP;
        } else if ((data[offset] == 't' || data[offset] == 'T')
            && (data[offset + 1] == 's' || data[offset + 1] == 'S')
            && (data[offset + 2] == 'p' || data[offset + 2] == 'P')) {
          return DsSipConstants.BS_TSP;
        } else if ((data[offset] == 'n' || data[offset] == 'N')
            && (data[offset + 1] == 'a' || data[offset + 1] == 'A')
            && (data[offset + 2] == 't' || data[offset + 2] == 'T')) {
          return DsSipConstants.BS_NAT;
        }
        break;
      case 4:
        if ((data[offset] == 'u' || data[offset] == 'U')
            && (data[offset + 1] == 's' || data[offset + 1] == 'S')
            && (data[offset + 2] == 'e' || data[offset + 2] == 'E')
            && (data[offset + 3] == 'r' || data[offset + 3] == 'R')) {
          return DsSipConstants.BS_USER;
        } else if ((data[offset] == 'a' || data[offset] == 'A')
            && (data[offset + 1] == 'u' || data[offset + 1] == 'U')
            && (data[offset + 2] == 't' || data[offset + 2] == 'T')
            && (data[offset + 3] == 'h' || data[offset + 3] == 'H')) {
          return DsSipConstants.BS_AUTH;
        } else if ((data[offset] == 'i' || data[offset] == 'I')
            && (data[offset + 1] == 's' || data[offset + 1] == 'S')
            && (data[offset + 2] == 'u' || data[offset + 2] == 'U')
            && (data[offset + 3] == 'b' || data[offset + 3] == 'B')) {
          return DsSipConstants.BS_ISUB;
        } else if ((data[offset] == 'c' || data[offset] == 'C')
            && (data[offset + 1] == 'o' || data[offset + 1] == 'O')
            && (data[offset + 2] == 'm' || data[offset + 2] == 'M')
            && (data[offset + 3] == 'p' || data[offset + 3] == 'P')) {
          return DsSipConstants.BS_COMP;
        }

        break;
      case 5:
        if ((data[offset] == 'm' || data[offset] == 'M')
            && (data[offset + 1] == 'a' || data[offset + 1] == 'A')
            && (data[offset + 2] == 'd' || data[offset + 2] == 'D')
            && (data[offset + 3] == 'd' || data[offset + 3] == 'D')
            && (data[offset + 4] == 'r' || data[offset + 4] == 'R')) {
          return DsSipConstants.BS_MADDR;
        } else if ((data[offset] == 'r' || data[offset] == 'R')
            && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
            && (data[offset + 2] == 'a' || data[offset + 2] == 'A')
            && (data[offset + 3] == 'l' || data[offset + 3] == 'L')
            && (data[offset + 4] == 'm' || data[offset + 4] == 'M')) {
          return DsSipConstants.BS_REALM;
        } else if ((data[offset] == 'n' || data[offset] == 'N')
            && (data[offset + 1] == 'o' || data[offset + 1] == 'O')
            && (data[offset + 2] == 'n' || data[offset + 2] == 'N')
            && (data[offset + 3] == 'c' || data[offset + 3] == 'C')
            && (data[offset + 4] == 'e' || data[offset + 4] == 'E')) {
          return DsSipConstants.BS_NONCE;
        } else if ((data[offset] == 's' || data[offset] == 'S')
            && (data[offset + 1] == 't' || data[offset + 1] == 'T')
            && (data[offset + 2] == 'a' || data[offset + 2] == 'A')
            && (data[offset + 3] == 'l' || data[offset + 3] == 'L')
            && (data[offset + 4] == 'e' || data[offset + 4] == 'E')) {
          return DsSipConstants.BS_STALE;
        } else if ((data[offset] == 'p' || data[offset] == 'P')
            && (data[offset + 1] == 'a' || data[offset + 1] == 'A')
            && (data[offset + 2] == 'r' || data[offset + 2] == 'R')
            && (data[offset + 3] == 't' || data[offset + 3] == 'T')
            && (data[offset + 4] == 'y' || data[offset + 4] == 'Y')) {
          return DsSipConstants.BS_PARTY;
        } else if ((data[offset] == 'p' || data[offset] == 'P')
            && (data[offset + 1] == 'o' || data[offset + 1] == 'O')
            && (data[offset + 2] == 's' || data[offset + 2] == 'S')
            && (data[offset + 3] == 't' || data[offset + 3] == 'T')
            && (data[offset + 4] == 'd' || data[offset + 4] == 'D')) {
          return DsSipConstants.BS_POSTD;
        } else if ((data[offset] == 'p' || data[offset] == 'P')
            && (data[offset + 1] == 'r' || data[offset + 1] == 'R')
            && (data[offset + 2] == 'o' || data[offset + 2] == 'O')
            && (data[offset + 3] == 'x' || data[offset + 3] == 'X')
            && (data[offset + 4] == 'y' || data[offset + 4] == 'Y')) {
          return DsSipConstants.BS_ACTION_PROXY;
        }
        break;
      case 6:
        if ((data[offset] == 'b' || data[offset] == 'B')
            && (data[offset + 1] == 'r' || data[offset + 1] == 'R')
            && (data[offset + 2] == 'a' || data[offset + 2] == 'A')
            && (data[offset + 3] == 'n' || data[offset + 3] == 'N')
            && (data[offset + 4] == 'c' || data[offset + 4] == 'C')
            && (data[offset + 5] == 'h' || data[offset + 5] == 'H')) {
          return DsSipConstants.BS_BRANCH;
        } else if ((data[offset] == 'h' || data[offset] == 'H')
            && (data[offset + 1] == 'i' || data[offset + 1] == 'I')
            && (data[offset + 2] == 'd' || data[offset + 2] == 'D')
            && (data[offset + 3] == 'd' || data[offset + 3] == 'D')
            && (data[offset + 4] == 'e' || data[offset + 4] == 'E')
            && (data[offset + 5] == 'n' || data[offset + 5] == 'N')) {
          return DsSipConstants.BS_HIDDEN;
        } else if ((data[offset] == 'a' || data[offset] == 'A')
            && (data[offset + 1] == 'c' || data[offset + 1] == 'C')
            && (data[offset + 2] == 't' || data[offset + 2] == 'T')
            && (data[offset + 3] == 'i' || data[offset + 3] == 'I')
            && (data[offset + 4] == 'o' || data[offset + 4] == 'O')
            && (data[offset + 5] == 'n' || data[offset + 5] == 'N')) {
          return DsSipConstants.BS_ACTION;
        } else if ((data[offset] == 'm' || data[offset] == 'M')
            && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
            && (data[offset + 2] == 't' || data[offset + 2] == 'T')
            && (data[offset + 3] == 'h' || data[offset + 3] == 'H')
            && (data[offset + 4] == 'o' || data[offset + 4] == 'O')
            && (data[offset + 5] == 'd' || data[offset + 5] == 'D')) {
          return DsSipConstants.BS_METHOD;
        } else if ((data[offset] == 'c' || data[offset] == 'C')
            && (data[offset + 1] == 'n' || data[offset + 1] == 'N')
            && (data[offset + 2] == 'o' || data[offset + 2] == 'O')
            && (data[offset + 3] == 'n' || data[offset + 3] == 'N')
            && (data[offset + 4] == 'c' || data[offset + 4] == 'C')
            && (data[offset + 5] == 'e' || data[offset + 5] == 'E')) {
          return DsSipConstants.BS_CNONCE;
        } else if ((data[offset] == 'd' || data[offset] == 'D')
            && (data[offset + 1] == 'o' || data[offset + 1] == 'O')
            && (data[offset + 2] == 'm' || data[offset + 2] == 'M')
            && (data[offset + 3] == 'a' || data[offset + 3] == 'A')
            && (data[offset + 4] == 'i' || data[offset + 4] == 'I')
            && (data[offset + 5] == 'n' || data[offset + 5] == 'N')) {
          return DsSipConstants.BS_DOMAIN;
        } else if ((data[offset] == 's' || data[offset] == 'S')
            && (data[offset + 1] == 'c' || data[offset + 1] == 'C')
            && (data[offset + 2] == 'r' || data[offset + 2] == 'R')
            && (data[offset + 3] == 'e' || data[offset + 3] == 'E')
            && (data[offset + 4] == 'e' || data[offset + 4] == 'E')
            && (data[offset + 5] == 'n' || data[offset + 5] == 'N')) {
          return DsSipConstants.BS_SCREEN;
        } else if ((data[offset] == 'o' || data[offset] == 'O')
            && (data[offset + 1] == 'p' || data[offset + 1] == 'P')
            && (data[offset + 2] == 'a' || data[offset + 2] == 'A')
            && (data[offset + 3] == 'q' || data[offset + 3] == 'Q')
            && (data[offset + 4] == 'u' || data[offset + 4] == 'U')
            && (data[offset + 5] == 'e' || data[offset + 5] == 'E')) {
          return DsSipConstants.BS_OPAQUE;
        }
        break;
      case 7:
        if ((data[offset] == 'e' || data[offset] == 'E')
            && (data[offset + 1] == 'x' || data[offset + 1] == 'X')
            && (data[offset + 2] == 'p' || data[offset + 2] == 'P')
            && (data[offset + 3] == 'i' || data[offset + 3] == 'I')
            && (data[offset + 4] == 'r' || data[offset + 4] == 'R')
            && (data[offset + 5] == 'e' || data[offset + 5] == 'E')
            && (data[offset + 6] == 's' || data[offset + 6] == 'S')) {
          return DsSipConstants.BS_EXPIRES_VALUE;
        } else if ((data[offset] == 'p' || data[offset] == 'P')
            && (data[offset + 1] == 'u' || data[offset + 1] == 'U')
            && (data[offset + 2] == 'r' || data[offset + 2] == 'R')
            && (data[offset + 3] == 'p' || data[offset + 3] == 'P')
            && (data[offset + 4] == 'o' || data[offset + 4] == 'O')
            && (data[offset + 5] == 's' || data[offset + 5] == 'S')
            && (data[offset + 6] == 'e' || data[offset + 6] == 'E')) {
          return DsSipConstants.BS_PURPOSE;
        } else if ((data[offset] == 'i' || data[offset] == 'I')
            && (data[offset + 1] == 'd' || data[offset + 1] == 'D')
            && (data[offset + 2] == '-')
            && (data[offset + 3] == 't' || data[offset + 3] == 'T')
            && (data[offset + 4] == 'y' || data[offset + 4] == 'Y')
            && (data[offset + 5] == 'p' || data[offset + 5] == 'P')
            && (data[offset + 6] == 'e' || data[offset + 6] == 'E')) {
          return DsSipConstants.BS_ID_TYPE;
        } else if ((data[offset] == 'p' || data[offset] == 'P')
            && (data[offset + 1] == 'r' || data[offset + 1] == 'R')
            && (data[offset + 2] == 'i' || data[offset + 2] == 'I')
            && (data[offset + 3] == 'v' || data[offset + 3] == 'V')
            && (data[offset + 4] == 'a' || data[offset + 4] == 'A')
            && (data[offset + 5] == 'c' || data[offset + 5] == 'C')
            && (data[offset + 6] == 'y' || data[offset + 6] == 'Y')) {
          return DsSipConstants.BSL_PRIVACY;
        }
        break;
      case 8:
        if ((data[offset] == 'r' || data[offset] == 'R')
            && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
            && (data[offset + 2] == 'c' || data[offset + 2] == 'C')
            && (data[offset + 3] == 'e' || data[offset + 3] == 'E')
            && (data[offset + 4] == 'i' || data[offset + 4] == 'I')
            && (data[offset + 5] == 'v' || data[offset + 5] == 'V')
            && (data[offset + 6] == 'e' || data[offset + 6] == 'E')
            && (data[offset + 7] == 'd' || data[offset + 7] == 'D')) {
          return DsSipConstants.BS_RECEIVED;
        } else if ((data[offset] == 'h' || data[offset] == 'H')
            && (data[offset + 1] == 'a' || data[offset + 1] == 'A')
            && (data[offset + 2] == 'n' || data[offset + 2] == 'N')
            && (data[offset + 3] == 'd' || data[offset + 3] == 'D')
            && (data[offset + 4] == 'l' || data[offset + 4] == 'L')
            && (data[offset + 5] == 'i' || data[offset + 5] == 'I')
            && (data[offset + 6] == 'n' || data[offset + 6] == 'N')
            && (data[offset + 7] == 'g' || data[offset + 7] == 'G')) {
          return DsSipConstants.BS_HANDLING;
        } else if ((data[offset] == 'd' || data[offset] == 'D')
            && (data[offset + 1] == 'u' || data[offset + 1] == 'U')
            && (data[offset + 2] == 'r' || data[offset + 2] == 'R')
            && (data[offset + 3] == 'a' || data[offset + 3] == 'A')
            && (data[offset + 4] == 't' || data[offset + 4] == 'T')
            && (data[offset + 5] == 'i' || data[offset + 5] == 'I')
            && (data[offset + 6] == 'o' || data[offset + 6] == 'O')
            && (data[offset + 7] == 'n' || data[offset + 7] == 'N')) {
          return DsSipConstants.BS_DURATION;
        } else if ((data[offset] == 'u' || data[offset] == 'U')
            && (data[offset + 1] == 's' || data[offset + 1] == 'S')
            && (data[offset + 2] == 'e' || data[offset + 2] == 'E')
            && (data[offset + 3] == 'r' || data[offset + 3] == 'R')
            && (data[offset + 4] == 'n' || data[offset + 4] == 'N')
            && (data[offset + 5] == 'a' || data[offset + 5] == 'A')
            && (data[offset + 6] == 'm' || data[offset + 6] == 'M')
            && (data[offset + 7] == 'e' || data[offset + 7] == 'E')) {
          return DsSipConstants.BS_USERNAME;
        } else if ((data[offset] == 'r' || data[offset] == 'R')
            && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
            && (data[offset + 2] == 's' || data[offset + 2] == 'S')
            && (data[offset + 3] == 'p' || data[offset + 3] == 'P')
            && (data[offset + 4] == 'o' || data[offset + 4] == 'O')
            && (data[offset + 5] == 'n' || data[offset + 5] == 'N')
            && (data[offset + 6] == 's' || data[offset + 6] == 'S')
            && (data[offset + 7] == 'e' || data[offset + 7] == 'E')) {
          return DsSipConstants.BS_RESPONSE;
        } else if ((data[offset] == 'r' || data[offset] == 'R')
            && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
            && (data[offset + 2] == 'd' || data[offset + 2] == 'D')
            && (data[offset + 3] == 'i' || data[offset + 3] == 'I')
            && (data[offset + 4] == 'r' || data[offset + 4] == 'R')
            && (data[offset + 5] == 'e' || data[offset + 5] == 'E')
            && (data[offset + 6] == 'c' || data[offset + 6] == 'C')
            && (data[offset + 7] == 't' || data[offset + 7] == 'T')) {
          return DsSipConstants.BS_ACTION_REDIRECT;
        }
        break;
      case 9:
        if ((data[offset] == 't' || data[offset] == 'T')
            && (data[offset + 1] == 'r' || data[offset + 1] == 'R')
            && (data[offset + 2] == 'a' || data[offset + 2] == 'A')
            && (data[offset + 3] == 'n' || data[offset + 3] == 'N')
            && (data[offset + 4] == 's' || data[offset + 4] == 'S')
            && (data[offset + 5] == 'p' || data[offset + 5] == 'P')
            && (data[offset + 6] == 'o' || data[offset + 6] == 'O')
            && (data[offset + 7] == 'r' || data[offset + 7] == 'R')
            && (data[offset + 8] == 't' || data[offset + 8] == 'T')) {
          return DsSipConstants.BS_TRANSPORT;
        } else if ((data[offset] == 'n' || data[offset] == 'N')
            && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
            && (data[offset + 2] == 'x' || data[offset + 2] == 'X')
            && (data[offset + 3] == 't' || data[offset + 3] == 'T')
            && (data[offset + 4] == 'n' || data[offset + 4] == 'N')
            && (data[offset + 5] == 'o' || data[offset + 5] == 'O')
            && (data[offset + 6] == 'n' || data[offset + 6] == 'N')
            && (data[offset + 7] == 'c' || data[offset + 7] == 'C')
            && (data[offset + 8] == 'e' || data[offset + 8] == 'E')) {
          return DsSipConstants.BS_NEXTNONCE;
        } else if ((data[offset] == 'a' || data[offset] == 'A')
            && (data[offset + 1] == 'l' || data[offset + 1] == 'L')
            && (data[offset + 2] == 'g' || data[offset + 2] == 'G')
            && (data[offset + 3] == 'o' || data[offset + 3] == 'O')
            && (data[offset + 4] == 'r' || data[offset + 4] == 'R')
            && (data[offset + 5] == 'i' || data[offset + 5] == 'I')
            && (data[offset + 6] == 't' || data[offset + 6] == 'T')
            && (data[offset + 7] == 'h' || data[offset + 7] == 'H')
            && (data[offset + 8] == 'm' || data[offset + 8] == 'M')) {
          return DsSipConstants.BS_ALGORITHM;
        } else if ((data[offset] == 'r' || data[offset] == 'R')
            && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
            && (data[offset + 2] == 'f' || data[offset + 2] == 'F')
            && (data[offset + 3] == 'r' || data[offset + 3] == 'R')
            && (data[offset + 4] == 'e' || data[offset + 4] == 'E')
            && (data[offset + 5] == 's' || data[offset + 5] == 'S')
            && (data[offset + 6] == 'h' || data[offset + 6] == 'H')
            && (data[offset + 7] == 'e' || data[offset + 7] == 'E')
            && (data[offset + 8] == 'r' || data[offset + 8] == 'R')) {
          return DsSipConstants.BS_REFRESHER;
        } else if ((data[offset] == 'f' || data[offset] == 'F')
            && (data[offset + 1] == 'o' || data[offset + 1] == 'O')
            && (data[offset + 2] == 'r' || data[offset + 2] == 'R')
            && (data[offset + 3] == 'k' || data[offset + 3] == 'K')
            && (data[offset + 4] == 'i' || data[offset + 4] == 'I')
            && (data[offset + 5] == 'n' || data[offset + 5] == 'N')
            && (data[offset + 6] == 'g' || data[offset + 6] == 'G')
            && (data[offset + 7] == 'I' || data[offset + 7] == 'i')
            && (data[offset + 8] == 'D' || data[offset + 8] == 'd')) {
          return DsSipConstants.BS_FORKING_ID;
        }
        break;
      case 10:
        break;
      case 11:
        break;
      case 12:
        break;
      case 13:
        if ((data[offset] == 'p' || data[offset] == 'P')
            && (data[offset + 1] == 'h' || data[offset + 1] == 'H')
            && (data[offset + 2] == 'o' || data[offset + 2] == 'O')
            && (data[offset + 3] == 'n' || data[offset + 3] == 'N')
            && (data[offset + 4] == 'e' || data[offset + 4] == 'E')
            && (data[offset + 5] == '-')
            && (data[offset + 6] == 'c' || data[offset + 6] == 'C')
            && (data[offset + 7] == 'o' || data[offset + 7] == 'O')
            && (data[offset + 8] == 'n' || data[offset + 8] == 'N')
            && (data[offset + 9] == 't' || data[offset + 9] == 'T')
            && (data[offset + 10] == 'e' || data[offset + 10] == 'E')
            && (data[offset + 11] == 'x' || data[offset + 11] == 'X')
            && (data[offset + 12] == 't' || data[offset + 12] == 'T')) {
          return DsSipConstants.BS_PHONE_CONTEXT;
        } else if ((data[offset] == 'r' || data[offset] == 'R')
            && (data[offset + 1] == 'e' || data[offset + 1] == 'E')
            && (data[offset + 2] == 'c' || data[offset + 2] == 'C')
            && (data[offset + 3] == 'e' || data[offset + 3] == 'E')
            && (data[offset + 4] == 'i' || data[offset + 4] == 'I')
            && (data[offset + 5] == 'v' || data[offset + 5] == 'V')
            && (data[offset + 6] == 'e' || data[offset + 6] == 'E')
            && (data[offset + 7] == 'd' || data[offset + 7] == 'D')
            && (data[offset + 8] == '-')
            && (data[offset + 9] == 'p' || data[offset + 9] == 'P')
            && (data[offset + 10] == 'o' || data[offset + 10] == 'O')
            && (data[offset + 11] == 'r' || data[offset + 11] == 'R')
            && (data[offset + 12] == 't' || data[offset + 12] == 'T')) {
          return DsSipConstants.BS_RECEIVED_PORT;
        }
        break;
    }

    DsByteString bs = new DsByteString();
    bs.data = new byte[count];
    bs.offset = 0;
    bs.count = count;
    for (int i = 0; i < count; ++i) {
      byte ch = data[offset + i];
      if (ch >= 'A' && ch <= 'Z') {
        ch += 32;
      }
      bs.data[i] = ch;
    }

    return bs;
  }

  /**
   * Returns a new instance of DsByteString with the specified byte array <code>data</code>.
   *
   * @param data the byte[] that contains the data.
   * @return same thing the new DsByteString(byte[], int, int) returns, except it may just be a
   *     static reference to an already created object - do not modify!
   */
  public static DsByteString newInstance(byte[] data) {
    if (data == null) {
      return BS_EMPTY_STRING;
    }

    return newInstance(data, 0, data.length);
  }

  /**
   * Constructor that takes a byte array.
   *
   * @param data the byte[] that contains the data.
   */
  public DsByteString(byte[] data) {
    if (data != null) {
      this.data = data;
      this.offset = 0;
      this.count = data.length;
    } else // handle null pointer
    {
      this.data = EMPTY_BYTES;
      this.offset = 0;
      this.count = 0;
    }
  }

  /**
   * Constructor that takes a char array.
   *
   * @param cdata the char[] that contains the data.
   */
  public DsByteString(char[] cdata) {
    if (cdata != null) {
      byte[] data = getBytes(cdata);

      this.data = data;
      this.offset = 0;
      this.count = data.length;
    } else // handle null pointer
    {
      this.data = EMPTY_BYTES;
      this.offset = 0;
      this.count = 0;
    }
  }

  /**
   * Constructor that takes a byte array.
   *
   * @param data the byte[] that contains the data.
   * @param offset the start of the data.
   * @param count the number of bytes in the data.
   */
  public DsByteString(byte[] data, int offset, int count) {
    this.data = data;
    this.offset = offset;
    this.count = count;
  }

  /**
   * Constructor that takes a char array.
   *
   * @param cdata the char[] that contains the data.
   * @param offset the start of the data.
   * @param count the number of bytes in the data.
   */
  public DsByteString(char[] cdata, int offset, int count) {
    this.data = getBytes(cdata, offset, count);
    this.offset = 0;
    this.count = count;
  }

  /**
   * Constructor that takes a String.
   *
   * @param data the String to represent as a DsByteString.
   */
  public DsByteString(String data) {
    this(getBytes(data));
  }

  /**
   * Constructor that takes a StringBuffer.
   *
   * @param data the String to represent as a DsByteString.
   */
  public DsByteString(StringBuffer data) {
    this(getBytes(data));
  }

  /**
   * Constructor that takes a DsByteString.
   *
   * @param data the DsByteString. If data is null, DsByteString.BS_EMPTY_STRING is used.
   */
  public DsByteString(DsByteString data) {
    this(
        data == null ? BS_EMPTY_STRING.data : data.data,
        data == null ? BS_EMPTY_STRING.offset : data.offset,
        data == null ? BS_EMPTY_STRING.count : data.count);
  }

  /**
   * Factory method to create a DsByteString from a section of a byte array. After calling this
   * method, <code>data()</code> will return a byte array of length <code>length()</code>, with
   * <code>offset()</code> 0, for the returned DsByteString.
   *
   * @param data the char[] that contains the data.
   * @param offset the start of the data.
   * @param count the number of bytes in the data.
   * @return a new DsByteString with copied data.
   */
  public static DsByteString createCopy(byte[] data, int offset, int count) {
    byte[] copy = new byte[count];
    System.arraycopy(data, offset, copy, 0, count);

    return new DsByteString(copy, 0, count);
  }

  /**
   * Creates a new byte array of the proper size and copies the data into it. After calling this
   * method, <code>data()</code> will return a byte array of length <code>length()</code>, with
   * <code>offset()</code> 0.
   */
  public final void copyData() {
    byte[] copy = new byte[count];
    System.arraycopy(this.data, offset, copy, 0, count);
    data = copy;
    this.offset = 0;
  }

  /**
   * Creates a new byte array of the proper size and copies the data into it, then creates a new
   * DsByteString with that byte array. After calling this method, <code>data()</code> will return a
   * byte array of length <code>length()</code>, with <code>offset()</code> 0, for the returned
   * DsByteString.
   *
   * @return a new DsByteString, with a copy of the data.
   */
  public final DsByteString copy() {
    return createCopy(data, offset, count);
  }

  /**
   * Sets the member <code>data</code> to the passed in <code>data</code>. The <code>offset</code>
   * will be 0 and <code>length</code> with be the length of the byte array.
   *
   * @param data the byte[] that contains the data.
   */
  final void setData(byte[] data) {
    this.data = data;
    offset = 0;
    count = data.length;
  }

  /**
   * Gets the byte array that contains the data for this DsByteString. The bytes in this byte array
   * should be consider immutable and should not be changed. Editing the data in this byte array
   * produces undefined behavior. Also, this byte array should not be accessed outside of <code>
   * offset()</code> and <code>length()</code>.
   *
   * @return The byte array that contains the data for this string.
   */
  public final byte[] data() {
    return data;
  }

  /**
   * Gets the starting point of this string in <code>data()</code>.
   *
   * @return The start of the bytes in this string in <code>data</code>.
   */
  public final int offset() {
    return offset;
  }

  /**
   * Gets the number of bytes in this string.
   *
   * @return the number of bytes in the byte array.
   */
  public final int length() {
    return count;
  }

  /**
   * Gets the byte at the specified index. The caller is responsible for ensuring that this index is
   * valid.
   *
   * @param index the byte at this position in the string.
   * @return the byte at the specified index.
   */
  public final byte charAt(int index) {
    // out of bounds exception?
    return data[offset + index];
  }

  /**
   * Appends <code>data</code> to this string and returns this string. This causes a copy of the
   * data of both sets of data to be made.
   *
   * @param data the int to append to this string.
   * @return this string with the data appended to it.
   */
  public final DsByteString append(int data) {
    int len = count + 1;
    byte[] bytes = new byte[len];
    System.arraycopy(this.data, this.offset, bytes, 0, this.count);
    bytes[len - 1] = (byte) data;
    this.data = bytes;
    this.offset = 0;
    this.count = len;

    return this;
  }

  /**
   * Appends <code>data</code> to this string and returns this string. This causes a copy of the
   * data of both sets of data to be made.
   *
   * @param data the DsByteString to append to this string, may be <code>null</code>.
   * @return this string with the data appended to it.
   */
  public final DsByteString append(DsByteString data) {
    if (data != null && data.length() > 0) {
      int len = count + data.count;
      byte[] bytes = new byte[len];
      System.arraycopy(this.data, this.offset, bytes, 0, this.count);
      System.arraycopy(data.data, data.offset, bytes, this.count, data.count);
      this.data = bytes;
      this.offset = 0;
      this.count = len;
    }
    return this;
  }

  /**
   * Appends <code>count</code> bytes of <code>data</code>, starting at <code>index</code> to this
   * string and returns this string. This causes a copy of the data of both sets of data to be made.
   *
   * @param data the byte[] that contains the data, may be <code>null</code>.
   * @param offset the start of the data.
   * @param count the number of bytes in the data.
   * @return this string with the data appended to it.
   */
  public final DsByteString append(byte[] data, int offset, int count) {
    if (data != null && offset > -1 && count > 0) {
      int len = count + this.count;
      byte[] bytes = new byte[len];
      System.arraycopy(this.data, this.offset, bytes, 0, this.count);
      System.arraycopy(data, offset, bytes, this.count, count);
      this.data = bytes;
      this.offset = 0;
      this.count = len;
    }
    return this;
  }

  /**
   * Appends <code>data.length</code> bytes of <code>data</code>, starting at <code>0</code> to this
   * string and returns this string. This causes a copy of the data of both sets of data to be made.
   *
   * @param data the byte[] that contains the data, must not be <code>null</code>.
   * @return this string with the data appended to it.
   * @throws NullPointerException if data is <code>null</code>.
   */
  public final DsByteString append(byte[] data) {
    return append(data, 0, data.length);
  }

  /**
   * Copies the data from this DsByteString to the byte array starting from the specified offset.
   *
   * @param bytes the byte array to copy the data from this string into, must not be null, and must
   *     be properly sized.
   * @param offset the index into <code>bytes</code> to start appending this string.
   * @throws NullPointerException if <code>bytes</code> is <code>null</code>.
   * @throws ArrayIndexOutOfBounds if <code>offset</code> is negative or <code>offset</code> +
   *     <code>length()</code> is greater than or equal to <code>bytes.length</code>.
   */
  public final void appendTo(byte[] bytes, int offset) {
    System.arraycopy(data, this.offset, bytes, offset, count);
  }

  /**
   * Copies the data from this DsByteString to the byte array starting from 0.
   *
   * @param bytes the byte array to copy the data from this string into, must not be null, and must
   *     be properly sized.
   * @throws NullPointerException if <code>bytes</code> is <code>null</code>.
   * @throws ArrayIndexOutOfBounds if <code>length()</code> is greater than or equal to <code>
   *     bytes.length</code>.
   */
  public final void appendTo(byte[] bytes) {
    appendTo(bytes, 0);
  }

  /**
   * Adds this DsByteString to the specified Vector. This function helps in adding various
   * DsByteString objects into a Vector object138 and then concatenating all the DsByteString object
   * into a single byte array or DsByteString object.
   *
   * @param elements the Vector to add this string to.
   * @return the number of bytes in this string.
   */
  public final int appendTo(Vector elements) {
    elements.add(this);
    return this.length();
  }

  /**
   * Tests this DsByteString versus <code>str</code> for equality.
   *
   * @param str the data to compare against.
   * @return <code>true</code> if a case-sensitive comparsion shows that the strings are equal.
   */
  // assumes ASCII - jsm
  public final boolean equals(String str) {
    if (str == null || str.length() != count) {
      return false;
    }

    for (int i = 0; i < count; i++) {
      if (str.charAt(i) != (char) data[offset + i]) {
        return false;
      }
    }

    return true;
  }

  /**
   * Tests this DsByteString versus <code>str</code> for equality, while ignoring case.
   *
   * @param str the data to compare against.
   * @return <code>true</code> if a case-insensitive comparsion shows that the strings are equal.
   */
  // assumes ASCII - jsm
  public final boolean equalsIgnoreCase(String str) {
    if (str == null || str.length() != count) {
      return false;
    }

    char c1, c2;

    for (int i = 0; i < count; i++) {
      c1 = str.charAt(i);
      c2 = (char) data[offset + i];

      // convert to lower case
      if (c1 >= 'A' && c1 <= 'Z') {
        c1 += 32;
      }

      // convert to lower case
      if (c2 >= 'A' && c2 <= 'Z') {
        c2 += 32;
      }

      if (c1 != c2) {
        return false;
      }
    }

    return true;
  }

  /*
   * javadoc inherited
   */
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }

    DsByteString comparator = null;
    try {
      comparator = (DsByteString) obj;
    } catch (ClassCastException cce) {
      return false;
    }
    return equals(comparator.data, comparator.offset, comparator.count);
  }

  /**
   * Tests this DsByteString versus <code>comparator</code> for equality, while ignoring case.
   *
   * @param comparator the data to compare against.
   * @return <code>true</code> if a case-insensitive comparsion shows that the strings are equal.
   */
  public final boolean equalsIgnoreCase(DsByteString comparator) {
    if (this == comparator) {
      return true;
    }
    if (comparator == null) {
      return false;
    }
    return equalsIgnoreCase(comparator.data, comparator.offset, comparator.count);
  }

  /**
   * Tests this DsByteString versus <code>comparator</code> for equality.
   *
   * @param data the data to compare against.
   * @return <code>true</code> if a case-sensitive comparsion shows that the strings are equal.
   */
  public final boolean equals(byte[] data) {
    return equals(data, 0, data.length);
  }

  /**
   * Tests this DsByteString versus <code>comparator</code> for equality, using the subset of <code>
   * data</code> starting at <code>offset</code> and using <code>count</code> bytes.
   *
   * @param data the data to compare against.
   * @param offset the offset in <code>data</code> to start the comparison from.
   * @param count the number of bytes to compare from <code>data</code>.
   * @return <code>true</code> if a case-sensitive comparsion shows that the strings are equal.
   */
  public final boolean equals(byte[] data, int offset, int count) {
    if (this.count != count) {
      return false;
    }
    byte[] b1 = this.data;
    int i = this.offset;
    while (count-- != 0) {
      if (b1[i++] != data[offset++]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests this DsByteString versus <code>comparator</code> for equality while ignoring case.
   *
   * @param data the data to compare against.
   * @return <code>true</code> if a case-insensitive comparsion shows that the strings are equal.
   */
  public final boolean equalsIgnoreCase(byte[] data) {
    return equalsIgnoreCase(data, 0, data.length);
  }

  /**
   * Tests this DsByteString versus <code>comparator</code> for equality, using the subset of <code>
   * data</code> starting at <code>offset</code> and using <code>count</code> bytes, while ignoring
   * case.
   *
   * @param data the data to compare against.
   * @param offset the offset in <code>data</code> to start the comparison from.
   * @param count the number of bytes to compare from <code>data</code>.
   * @return <code>true</code> if a case-insensitive comparsion shows that the strings are equal.
   */
  public final boolean equalsIgnoreCase(byte[] data, int offset, int count) {
    if (this.count != count) {
      return false;
    }
    byte[] b1 = this.data;
    int i = this.offset;
    while (count-- != 0) {
      if (b1[i] != data[offset] && (b1[i] - 32) != data[offset] && (b1[i] + 32) != data[offset]) {
        return false;
      }
      i++;
      offset++;
    }
    return true;
  }

  /**
   * Write the data that this string represents to an output stream.
   *
   * @param out the output stream to write this data to.
   * @throws IOException if there is an exception writing to the stream.
   */
  public void write(OutputStream out) throws IOException {
    if (count > 0) {
      out.write(data, offset, count);
    }
  }

  /**
   * Turn the passed DsByteString into a String. It is safer to use this toString() that the member
   * method <code>toString()</code> since this one checks for <code>null</code>.
   *
   * @param bs the string to convert to a String.
   * @return a String representation of <code>bs</code> or <code>null</code> if <code>bs</code> is
   *     <code>null</code>.
   */
  public static String toString(DsByteString bs) {
    if (bs == null) {
      return null;
    } else {
      return bs.toString();
    }
  }

  /**
   * Turn this DsByteString into a String.
   *
   * @return a String representation of this string.
   */
  public String toString() {
    return newString(data, offset, count);
  }

  /**
   * Create a String representation of a substring of this string.
   *
   * @param offset the index to start the copy, assumes 0 is the first byte of the string.
   * @param count the number of bytes to copy.
   * @return a String representation of the substring specified.
   */
  public final String toString(int offset, int count) {
    offset += this.offset;
    return newString(data, offset, count);
  }

  public static DsByteString toHexString(long i) {
    byte[] buf = new byte[16];
    int charPos = 16;
    final long mask = 15;
    do {
      buf[--charPos] = m_digits[(int) (i & mask)];
      i >>>= 4;
    } while (i != 0);
    return new DsByteString(buf, charPos, (16 - charPos));
  }

  public static DsByteString toHexString(int i) {
    byte[] buf = new byte[8];
    int charPos = 8;
    final long mask = 15;
    do {
      buf[--charPos] = m_digits[(int) (i & mask)];
      i >>>= 4;
    } while (i != 0);
    return new DsByteString(buf, charPos, (8 - charPos));
  }

  public static DsByteString toHexString0Pad(long i, int length) {
    if (length < 0 || length > 16) {
      throw new IllegalArgumentException("Length must be between 0 and 16.");
    }

    DsByteString bs = toHexString(i);

    if (bs.length() < length) {
      Arrays.fill(bs.data, 16 - length, 16 - bs.count, (byte) '0');
      bs.offset = 16 - length;
      bs.count = length;
    }

    return bs;
  }

  /**
   * Copies the entire string into a new byte array of proper size.
   *
   * @return a copy of the entire string in its own byte array.
   */
  public final byte[] toByteArray() {
    return toByteArray(0, count);
  }

  /**
   * Copies the partial string into a new byte array of proper size.
   *
   * @param offset offset the index to start the copy, assumes 0 is the first byte of the string.
   * @param count the number of bytes to copy.
   * @return a copy of the partial string in its own byte array.
   */
  public final byte[] toByteArray(int offset, int count) {
    byte[] bytes = new byte[count];
    offset += this.offset;
    System.arraycopy(data, offset, bytes, 0, count);
    return bytes;
  }

  /**
   * Creates a new DsByteString representing a substring of the DsByteString without copying data
   * from the underlying byte array.
   *
   * @param begin where to start the substring, inclusive, assumes 0 is the first byte of the
   *     string.
   * @param end the ending index, exclusive, assumes 0 is the first byte of the string.
   * @return the substring created.
   */
  public final DsByteString substring(int begin, int end) {
    begin += this.offset;
    end += this.offset;
    return new DsByteString(data, begin, (end - begin));
  }

  /**
   * Returns a new DsByteString that is a substring of this string. The substring begins with the
   * byte at the specified index and extends to the end of this string. The data is not copied.
   *
   * @param begin where to start the substring, inclusive, assumes 0 is the first byte of the
   *     string.
   * @return the substring created.
   */
  public final DsByteString substring(int begin) {
    return substring(begin, count);
  }

  /**
   * Returns a hash code for this byte string. The hash value of the empty string is zero.
   *
   * @return a hash code value for this object.
   */
  // Its a copy from the Java String class.
  public final int hashCode() {
    int h = hash;
    if (h == 0) {
      int off = offset;
      byte val[] = data;
      int len = count;

      for (int i = 0; i < len; i++) {
        h = 31 * h + val[off++];
      }
      hash = h;
    }
    return h;
  }

  /**
   * Compares two strings lexicographically.
   *
   * @param comparator the string to compare to.
   * @return the value 0 if the argument string is equal to this string; a value less than 0 if this
   *     string is lexicographically less than the string argument; and a value greater than 0 if
   *     this string is lexicographically greater than the string argument.
   */
  public final int compareTo(DsByteString comparator) {
    int len1 = count;
    int len2 = comparator.count;
    int n = Math.min(len1, len2);
    byte v1[] = data;
    byte v2[] = comparator.data;
    int i = offset;
    int j = comparator.offset;

    while (n-- != 0) {
      byte c1 = v1[i++];
      byte c2 = v2[j++];
      if (c1 != c2) {
        return c1 - c2;
      }
    }
    return len1 - len2;
  }

  /**
   * Compares two strings lexicographically.
   *
   * @param o the string to compare to.
   * @return the value 0 if the argument string is equal to this string; a value less than 0 if this
   *     string is lexicographically less than the string argument; and a value greater than 0 if
   *     this string is lexicographically greater than the string argument.
   * @throws ClassCastException if <code>o</code> is not a DsByteString.
   */
  public final int compareTo(Object o) {
    return compareTo((DsByteString) o);
  }

  /**
   * Returns the index within this string of the first occurrence of the specified substring,
   * starting at index 0.
   *
   * @param bs the string to search for.
   * @return if the string argument occurs as a substring within this object, then the index of the
   *     first character of the first such substring is returned; if it does not occur as a
   *     substring, -1 is returned.
   * @throws NullPointerException if <code>bs</code> is <code>null</code>.
   */
  public final int indexOf(DsByteString bs) {
    return indexOf(bs, 0);
  }

  /**
   * Returns the index within this string of the first occurrence of the specified substring,
   * starting at index <code>fromIndex</code>.
   *
   * @param bs the string to search for.
   * @param fromIndex the index to start the search from.
   * @return if the string argument occurs as a substring within this object, then the index of the
   *     first character of the first such substring is returned; if it does not occur as a
   *     substring, -1 is returned.
   * @throws NullPointerException if <code>bs</code> is <code>null</code>.
   * @throws ArrayIndexOutOfBounds if <code>fromIndex</code> is greater than or equal to <code>
   *     bs.length</code> or less than <code>0</code>.
   */
  public final int indexOf(DsByteString bs, int fromIndex) {
    byte v1[] = data;
    byte v2[] = bs.data;
    int max = offset + (count - bs.count);

    if (fromIndex >= count) {
      if (count == 0 && fromIndex == 0 && bs.count == 0) {
        // always return found when searching for an empty string
        return 0;
      }

      return -1;
    }

    if (fromIndex < 0) {
      fromIndex = 0;
    }

    if (bs.count == 0) {
      // always return found when searching for an empty string
      return fromIndex;
    }

    int bsOffset = bs.offset;
    byte first = v2[bsOffset];
    int i = offset + fromIndex;

    int j;
    int end;
    int k;

    startSearchForFirstChar:
    while (true) {
      // Look for the first char
      while (i <= max && v1[i] != first) {
        i++;
      }
      if (i > max) {
        return -1;
      }

      // Found the first char, now look at the rest of v2
      j = i + 1;
      end = j + bs.count - 1;
      k = bsOffset + 1;

      while (j < end) {
        if (v1[j++] != v2[k++]) {
          i++;
          // start at the beginning again
          continue startSearchForFirstChar;
        }
      }
      return i - offset; // Found it
    }
  }

  /**
   * Returns the index within this string of the first occurrence of the specified character,
   * starting at index <code>fromIndex</code>.
   *
   * <p>
   *
   * <p>There is no restriction on the value of fromIndex. If it is negative, it has the same effect
   * as if it were zero: this entire string may be searched. If it is greater than the length of
   * this string, it has the same effect as if it were equal to the length of this string: -1 is
   * returned.
   *
   * @param b the character to search for.
   * @param fromIndex the index to begin the search from, assume string starts at index 0.
   * @return the index of the first occurrence of the character in the character sequence
   *     represented by this object, or -1 if the character does not occur.
   */
  // CAFFEINE 2.0 DEVELOPMENT
  // Note: Not throw any exception. The original comment below is wrong.
  // removed similar comments in indexOf(int) as well - kaiw 1/22/04
  // @throws ArrayIndexOutOfBounds if <code>fromIndex</code> is greater than or equal to
  public final int indexOf(int b, int fromIndex) {
    int max = offset + count;
    byte v[] = data;
    if (fromIndex < 0) {
      fromIndex = 0;
    } else if (fromIndex >= count) {
      return -1;
    }
    for (int i = offset + fromIndex; i < max; i++) {
      if (v[i] == b) {
        return i - offset;
      }
    }
    return -1;
  }

  /**
   * Returns the index within this string of the first occurrence of the specified character,
   * starting at index 0.
   *
   * @param b the character to search for.
   * @return the index of the first occurrence of the character in the character sequence
   *     represented by this object, or -1 if the character does not occur.
   */
  public final int indexOf(int b) {
    return indexOf(b, 0);
  }

  // CAFFEINE 2.0 DEVELOPMENT - some changes by Paul's Kyzivat comments in Caffeine-1.

  /**
   * Returns the index within this string of the last occurrence of the specified character. The
   * string is earched backwards starting from the last character.
   *
   * @param b the character to search for.
   * @return the index of the last occurrence of the character in the character sequence represented
   *     by this object, or -1 if the character does not occur.
   */
  public final int lastIndexOf(int b) {
    return lastIndexOf(b, count - 1);
  }

  /**
   * Returns the index within this string of the last occurrence of the specified character,
   * searching backward starting at index <code>fromIndex</code>.
   *
   * <p>
   *
   * <p>There is no restriction on the value of fromIndex. If it is greater than or equal to the
   * length of this string, it has the same effect as if it were equal to one less than the length
   * of this string: this entire string may be searched. If it is negative, it has the same effect
   * as if it were -1: -1 is returned.
   *
   * @param b the character to search for.
   * @param fromIndex the index to begin the search from, assume string starts at index 0.
   * @return the index of the last occurrence of the character in the character sequence represented
   *     by this object, or -1 if the character does not occur.
   */
  public final int lastIndexOf(int b, int fromIndex) {
    int max = offset + count;
    byte v[] = data;
    if (fromIndex < 0) {
      return -1;
    } else if (fromIndex >= count) {
      fromIndex = count - 1;
    }
    for (int i = offset + fromIndex; i >= offset; i--) {
      if (v[i] == b) {
        return i - offset;
      }
    }
    return -1;
  }

  /**
   * Creates a new DsByteString with all characters changed to upper case. Does not modify the
   * original byte array.
   *
   * @return the string, converted to upper case.
   */
  public final DsByteString toUpperCase() {
    // TODO: need to comeup with a better way of doing this.
    // For now just using, String.
    return new DsByteString(newString(data, offset, count).toUpperCase());
  }

  /**
   * Creates a new DsByteString with all characters changed to lower case. Does not modify the
   * original byte array.
   *
   * @return the string, converted to lower case.
   */
  public final DsByteString toLowerCase() {
    int end = offset + count;
    boolean upperExists = false;
    byte b;
    for (int i = offset; i < end; i++) {
      b = data[i];
      if (b < 0) {
        // Let the Java String class handle this, since a non-ascii char exists
        return new DsByteString(newString(data, offset, count).toLowerCase());
      } else if (!upperExists && b >= (byte) 'A' && b <= (byte) 'Z') {
        upperExists = true;
      }
    }

    if (!upperExists) {
      // already lower case
      return new DsByteString(data, offset, count);
    }

    byte[] bytes = new byte[count];
    for (int i = offset; i < end; i++) {
      b = data[i];
      if (b >= (byte) 'A' && b <= (byte) 'Z') {
        bytes[i - offset] = (byte) (b + 32);
      } else {
        bytes[i - offset] = b;
      }
    }

    return new DsByteString(bytes);
  }

  /**
   * Removes the '"' character from the beginning and the end of this byte string, if present at
   * both locations. Does not modify the original byte array.
   */
  public final void unquote() {
    if (count > 1
        && data[offset] == DsSipConstants.B_QUOTE
        && data[offset + count - 1] == DsSipConstants.B_QUOTE) {
      offset++;
      count -= 2;
    }
  }

  /**
   * Removes the '"' character from the beginning and the end of this byte string, if present at
   * both locations, and returns as a new DsByteString. Otherwise reference to this byte string is
   * returned.
   *
   * @return a DsByteString without quotes.
   */
  public final DsByteString unquoted() {
    if (count > 1
        && data[offset] == DsSipConstants.B_QUOTE
        && data[offset + count - 1] == DsSipConstants.B_QUOTE) {
      return new DsByteString(data, offset + 1, count - 2);
    }
    return this;
  }

  /**
   * Removes the '(' character from the beginning and the ')' character from the end of this byte
   * string, if both are present. Does not modify the original byte array.
   */
  public final void uncomment() {
    if (count > 1 && data[offset] == '(' && data[offset + count - 1] == ')') {
      offset++;
      count -= 2;
    }
  }

  /**
   * Removes the '(' character from the beginning and the ')' character from the end of this byte
   * string, if both are present, and returns as a new DsByteString. Otherwise reference to this
   * byte string is returned. Does not modify the original byte array.
   *
   * @return a DsByteString without parens.
   */
  public final DsByteString uncommented() {
    if (count > 1 && data[offset] == '(' && data[offset + count - 1] == ')') {
      return new DsByteString(data, offset + 1, count - 2);
    }
    return this;
  }

  /**
   * Concatenates the specified byte string to the end of this byte string.
   *
   * <p>If the length of the argument string is <code>0</code>, then this <code>DsByteString</code>
   * object is returned. Otherwise, a new <code>DsByteString</code> object is created, representing
   * a byte sequence that is the concatenation of the byte sequence represented by this <code>
   * DsByteString</code> object and the byte sequence represented by the argument byte string.
   *
   * <p>
   *
   * @param str the <code>DsByteString</code> that is concatenated to the end of this <code>
   *     DsByteString</code>.
   * @return a byte string that represents the concatenation of this object's bytes followed by the
   *     string argument's bytes.
   * @throws java.lang.NullPointerException if <code>str</code> is <code>null</code>.
   */
  public final DsByteString concat(DsByteString str) {
    if (str.count == 0) {
      return this;
    }
    byte buf[] = new byte[count + str.count];
    System.arraycopy(data, offset, buf, 0, count);
    System.arraycopy(str.data, str.offset, buf, count, str.count);
    return new DsByteString(buf, 0, buf.length);
  }

  public static String concatToStr(DsByteString bs, String s) {
    StringBuffer sb = new StringBuffer(bs.count + s.length());

    byte b;
    for (int i = 0; i < bs.count; i++) {
      b = bs.data[bs.offset + i];
      if (b < 0) {
        // non-ASCII, let the Java String class handle this correctly
        return (bs.toString() + s);
      }
      sb.append((char) b);
    }

    sb.append(s);

    return sb.toString();
  }

  public static String concatToStr(String s, DsByteString bs) {
    StringBuffer sb = new StringBuffer(s.length() + bs.count);

    sb.append(s);

    byte b;
    for (int i = 0; i < bs.count; i++) {
      b = bs.data[bs.offset + i];
      if (b < 0) {
        // non-ASCII, let the Java String class handle this correctly
        return (s + bs.toString());
      }
      sb.append((char) b);
    }

    return sb.toString();
  }

  /**
   * Tells whether this byte string starts with the bytes in the specified byte string <code>str
   * </code>.
   *
   * @param str the string to check if this string starts with.
   * @return <code>true</code> if this byte string starts with the specified byte string <code>str
   *     </code>, <code>false</code> otherwise.
   */
  public final boolean startsWith(DsByteString str) {
    if (count < str.count) {
      return false;
    }
    int i1 = offset + str.count - 1;
    int i2 = str.offset + str.count - 1;

    for (; i2 >= str.offset; i1--, i2--) {
      if (data[i1] != str.data[i2]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tells whether this byte string ends with the bytes in the specified byte string <code>str
   * </code>.
   *
   * @param str the string to check for.
   * @return <code>true</code> if this byte string ends with the specified byte string <code>str
   *     </code>, <code>false</code> otherwise.
   */
  public final boolean endsWith(DsByteString str) {
    if (count < str.count) {
      return false;
    }
    int i1 = offset + count - 1; // index of last entry in this
    int i2 = str.offset + str.count - 1; // index of last entry in s2

    for (; i2 >= str.offset; i1--, i2--) {
      if (data[i1] != str.data[i2]) {
        return false;
      }
    }
    return true;
  }

  // static methods from DsString

  /**
   * Get the bytes from a String. Tries to cast directly from chars to bytes. If there is UTF-8,
   * then it returns String.getBytes("UTF-8"). Otherwise, the casting is fine, ASCII only.
   *
   * @param str the String to get the bytes from.
   * @return the same thing that String.getBytes() would return.
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
   * Get the bytes from a StringBuffer. Tries to cast directly from chars to bytes. If there is
   * UTF-8, then it returns String.getBytes("UTF-8"). Otherwise, the casting is fine, ASCII only.
   *
   * @param str the String to get the bytes from.
   * @return the same thing that String.getBytes() would return.
   */
  public static byte[] getBytes(StringBuffer str) {
    int strLen = str.length();
    byte strBytes[] = new byte[strLen];

    char ch;

    for (int i = 0; i < strLen; i++) {
      ch = str.charAt(i);
      if (ch > 127) {
        // Since there is a non-ASCII char in this string,
        // let the Java String class do this conversion
        try {
          return str.toString().getBytes(DsSipConstants.UTF8);
        } catch (UnsupportedEncodingException e) {
          return str.toString().getBytes();
        }
      }

      strBytes[i] = (byte) ch;
    }

    return strBytes;
  }

  /**
   * Get the bytes from a char array. Tries to cast directly from chars to bytes. If there is UTF-8,
   * then it returns String.getBytes("UTF-8"). Otherwise, the casting is fine, ASCII only.
   *
   * @param data the char array to get the bytes from.
   * @return the same thing that String.getBytes() would return.
   */
  public static byte[] getBytes(char[] data) {
    return getBytes(data, 0, data.length);
  }

  /**
   * Get the bytes from a String. Tries to cast directly from chars to bytes. If there is UTF-8,
   * then it returns String.getBytes("UTF-8"). Otherwise, the casting is fine, ASCII only.
   *
   * @param data the char array to get the bytes from.
   * @param offset the start of the char array to being getting bytes from.
   * @param count the number of byte to retrieve from the char array.
   * @return the same thing that String.getBytes() would return.
   */
  public static byte[] getBytes(char[] data, int offset, int count) {
    byte strBytes[] = new byte[count];

    char ch;

    for (int i = 0; i < count; i++) {
      ch = data[offset + i];
      if (ch > 127) {
        // Since there is a non-ASCII char in this string,
        // let the Java String class do this conversion
        String str = new String(data, offset, count);
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
   * Get aString from the bytes. Tries to cast directly from bytes to chars. If there is UTF-8, then
   * it returns new String.(byte[]). Otherwise, the casting is fine, ASCII only.
   *
   * @param bytes the bytes to turn into a String.
   * @return the same thing that new String(byte[]) would return.
   */
  public static String newString(byte[] bytes) {
    return newString(bytes, 0, bytes.length);
  }

  /**
   * Get aString from the bytes. Tries to cast directly from bytes to chars. If there is UTF-8, then
   * it returns new String.(byte[]). Otherwise, the casting is fine, ASCII only.
   *
   * @param bytes the bytes to turn into a String.
   * @param offset the starting index.
   * @param count the number of the bytes array to put into the string.
   * @return the same thing that new String(byte[], int, int) would return.
   */
  public static String newString(byte[] bytes, int offset, int count) {
    // using the deprecated method is twice as fast, since the
    // char array constructor copies the chars, we can skip one
    // array allocation and one arraycopy
    int end = offset + count;
    for (int i = offset; i < end; i++) {
      if (bytes[i] < 0) {
        // Since there is a non-ASCII char in this string,
        // let the Java String class do this conversion
        return new String(bytes, offset, count);
      }
    }

    return new String(bytes, 0, offset, count);
  }

  /**
   * Adds the specified DsByteString to the specified Vector. This function helps in adding various
   * DsByteString objects into a Vector object and then concatenating all the DsByteString object
   * into a single byte array or DsByteString object.
   *
   * @param elements the Vector to append to.
   * @param str the string to append to the Vector.
   * @return the length of the string appended to the Vector.
   */
  public static int appendTo(Vector elements, DsByteString str) {
    elements.add(str);
    return str.length();
  }

  /*
      public static String getHostAddress(InetAddress addr)
      {
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
      }
  */

  /**
   * Returns a DsByteString representing the <code>Object</code> argument.
   *
   * @param obj an <code>Object</code>.
   * @return if the argument is <code>null</code>, then a BS_NULL is returned; otherwise, a new
   *     DsByteString with the value <code>obj.toString()</code> is returned.
   */
  public static DsByteString valueOf(Object obj) {
    return (obj == null) ? BS_NULL : new DsByteString(obj.toString());
  }

  /**
   * Returns a DsByteString representing the <code>char</code> array argument.
   *
   * @param data a <code>char</code> array.
   * @return a newly allocated DsByteString representing the same sequence of characters contained
   *     in the character array argument.
   */
  public static DsByteString valueOf(char data[]) {
    return new DsByteString(data, 0, data.length);
  }

  /**
   * Returns a DsByteString representing the specific subarray of the <code>char</code> array
   * argument.
   *
   * @param data the char array.
   * @param offset the initial offset into the value of the DsByteString.
   * @param count the length of the value of the DsByteString.
   * @return a newly allocated DsByteString representing the sequence of characters contained in the
   *     subarray of the char array argument.
   */
  public static DsByteString valueOf(char data[], int offset, int count) {
    return new DsByteString(data, offset, count);
  }

  /**
   * Returns a DsByteString representing the <code>boolean</code> argument.
   *
   * @param b a <code>boolean</code>.
   * @return if the argument is <code>true</code> BS_TRUE is returned; otherwise, BS_FALSE is
   *     returned.
   */
  public static DsByteString valueOf(boolean b) {
    return b ? BS_TRUE : BS_FALSE;
  }

  /**
   * Returns a DsByteString representing the <code>char</code> argument. The char will be cast to a
   * byte.
   *
   * @param c a <code>char</code>.
   * @return a newly allocated DsByteString of length <code>1</code> containing as its single
   *     character the argument <code>c</code>.
   */
  public static DsByteString valueOf(char c) {
    byte data[] = {(byte) c};
    return new DsByteString(data, 0, 1);
  }

  /**
   * Returns a DsByteString representing the <code>int</code> argument.
   *
   * @param i an <code>int</code>.
   * @return a newly allocated DsByteString representing the <code>int</code> argument.
   */
  public static DsByteString valueOf(int i) {
    // can be optimized - jsm
    return new DsByteString(Integer.toString(i, 10));
  }

  /**
   * Returns a DsByteString representing the <code>long</code> argument.
   *
   * @param l a <code>long</code>.
   * @return a newly allocated DsByteString representing of the <code>long</code> argument.
   */
  public static DsByteString valueOf(long l) {
    // can be optimized - jsm
    return new DsByteString(Long.toString(l, 10));
  }

  /**
   * Returns a DsByteString representing the <code>float</code> argument.
   *
   * @param f a <code>float</code>.
   * @return a newly allocated DsByteString representing the <code>float</code> argument.
   */
  public static DsByteString valueOf(float f) {
    /*
             * this will almost always evaluate to false due to floating point imprecision
             * if (f == 1.0f)
            {
                return BS_ONE_DEC;
            }
    */
    return new DsByteString(Float.toString(f));
  }

  /**
   * Returns a DsByteString representing the <code>double</code> argument.
   *
   * @param d a <code>double</code>.
   * @return a newly allocated DsByteString containing a representing of the <code>double</code>
   *     argument.
   */
  public static DsByteString valueOf(double d) {
    /*
     * this will almost always evaluate to false due to floating point imprecision
     * if (d == 1.0d)
    {
        return BS_ONE_DEC;
    }*/

    return new DsByteString(Double.toString(d));
  }

  /**
   * Tests the two portions of byte arrays for equality, using the subset of <code>data</code>
   * starting at <code>offset</code> and using <code>count</code> bytes, while ignoring case.
   *
   * @param data1 a byte array to compare.
   * @param offset1 the offset in <code>data1</code> to start the comparison from.
   * @param count1 the number of bytes to compare from <code>data1</code>.
   * @param data2 a byte array to compare.
   * @param offset2 the offset in <code>data2</code> to start the comparison from.
   * @param count2 the number of bytes to compare from <code>data2</code>.
   * @return <code>true</code> if a case-sensitive comparsion shows that the byte arrays are equal.
   */
  public static boolean equals(
      byte[] data1, int offset1, int count1, byte[] data2, int offset2, int count2) {
    if (count1 != count2) {
      return false;
    }
    while (count1-- != 0) {
      if (data1[offset1++] != data2[offset2++]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests the DsByteStrings for equality.
   *
   * @param str1 a string to compare.
   * @param str2 a string to compare.
   * @return <code>true</code> if a case-sensitive comparsion shows that the byte arrays are equal.
   */
  public static boolean equals(DsByteString str1, DsByteString str2) {
    if (str1 == str2) {
      return true;
    }
    if (str1 == null || str2 == null) {
      return false;
    }
    return equals(str1.data, str1.offset, str1.count, str2.data, str2.offset, str2.count);
  }

  /**
   * Get the value of this string as an int.
   *
   * @return an int representation of this string.
   */
  public final int parseInt() {
    return DsSipMsgParser.parseInt(data, offset, count);
  }

  /**
   * Get the value of this string as an long.
   *
   * @return an long representation of this string.
   */
  public final long parseLong() {
    return DsSipMsgParser.parseLong(data, offset, count);
  }

  /**
   * Get the value of this string as an float.
   *
   * @return an float representation of this string.
   */
  public final float parseFloat() {
    return DsSipMsgParser.parseFloat(data, offset, count);
  }

  /**
   * Compares two strings lexicographically.
   *
   * @param first a string to compare.
   * @param second a string to compare.
   * @return the value 0 if the first string is equal to the second string; a value less than 0 if
   *     the second string is lexicographically less than the first string; and a value greater than
   *     0 if the first string is lexicographically greater than the second string.
   */
  public static int compare(DsByteString first, DsByteString second) {
    return compare(
        first.data,
        (int) first.offset,
        (int) first.count,
        second.data,
        (int) second.offset,
        (int) second.count);
  }

  /**
   * Compares two byte arrays lexicographically.
   *
   * @param first a byte array to compare.
   * @param second a byte array to compare.
   * @return the value 0 if the first byte array is equal to the second byte array; a value less
   *     than 0 if the second byte array is lexicographically less than the first byte array; and a
   *     value greater than 0 if the first byte array is lexicographically greater than the second
   *     byte array.
   */
  public static int compare(byte[] first, byte[] second) {
    return compare(first, 0, first.length, second, 0, second.length);
  }

  /**
   * Compares two portions of byte arrays lexicographically.
   *
   * @param first a byte array to compare.
   * @param firstOff the index into the first byte array to begin comparison.
   * @param firstCount the number of bytes of first to use in the comparison.
   * @param second a byte array to compare.
   * @param secondOff the index into the second byte array to begin comparison.
   * @param secondCount the number of bytes of second to use in the comparison.
   * @return the value 0 if the first byte array is equal to the second byte array; a value less
   *     than 0 if the second byte array is lexicographically less than the first byte array; and a
   *     value greater than 0 if the first byte array is lexicographically greater than the second
   *     byte array.
   */
  public static int compare(
      byte[] first, int firstOff, int firstCount, byte[] second, int secondOff, int secondCount) {
    int n = Math.min(firstCount, secondCount);
    byte c1, c2;
    while (n-- != 0) {
      c1 = first[firstOff++];
      c2 = second[secondOff++];
      if (c1 != c2) {
        return c1 - c2;
      }
    }
    return firstCount - secondCount;
  }

  // CAFFEINE 2.0 DEVELOPMENT - Add App-Info header support in the stack.

  /**
   * Test if a DsByteString is null or empty.
   *
   * @param s a string that is being tested.
   * @return true if s is null or empty string; otherwise return false.
   */
  public static final boolean nullOrEmpty(DsByteString s) {
    return s == null || s.length() == 0;
  }

  /**
   * Compares two strings lexicographically.If one string is null and the other is empty, they are
   * considered equal.
   *
   * @param first a string to compare.
   * @param second a string to compare.
   * @return the value 0 if the first string is equal to the second string; a value less than 0 if
   *     the first string is lexicographically less than the second string; and a value greater than
   *     0 if the first string is lexicographically greater than the second string.
   */
  public static int compareIgnoreNull(DsByteString a, DsByteString b) {
    if (nullOrEmpty(a)) {
      return nullOrEmpty(b) ? 0 : -1;
    }
    return nullOrEmpty(b) ? 1 : compare(a, b);
  }

  /**
   * Concatenates the specified two byte strings into a third byte string and return it. The data is
   * copied from both strings.
   *
   * @param first the first string in the concatenation.
   * @param second the second string in the concatenation.
   * @return a new DsByteString with <code>second</code> appended to <code>first</code>.
   */
  public static DsByteString concat(DsByteString first, DsByteString second) {
    int size = first.count + second.count;
    byte[] bytes = new byte[size];
    System.arraycopy(first.data, first.offset, bytes, 0, first.count);
    System.arraycopy(second.data, second.offset, bytes, first.count, second.count);
    return new DsByteString(bytes, 0, size);
  }

  /**
   * Remove all white space from the start and end of this string. No copying is done and no
   * characters in the underlying byte array are changed. The starting index and the count of
   * characters represents may change.
   *
   * <p>All characters less than or equals to the space character (0x20) are trimmed.
   */
  public void trim() {
    // trim from the left
    while (count > 0 && data[offset] <= ' ') {
      --count;
      ++offset;
    }

    // now trim from the right
    while (count > 0 && data[offset + count - 1] <= ' ') {
      --count;
    }
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

  // public static void main(String[] args)
  // {
  // DsByteString s1 = new DsByteString("HeLlo");
  // DsByteString s2 = new DsByteString("hE?Lo");
  // System.out.println("Equals = " + s1.equals(s2));
  // System.out.println("EqualsIgnoreCase = " + s1.equalsIgnoreCase(s2));

  // byte[] orig = "The Original Array".getBytes();
  // DsByteString s3 = DsByteString.createCopy(orig, 4, 8);
  // System.out.println("Original = [" + s3 + "]");
  // orig = "The _Copied_ Array".getBytes();
  // System.out.println("Copy Org = [" + s3 + "]");

  // System.out.println("valueOf (int)1   = [" + valueOf((int)1) + "]");
  // System.out.println("valueOf (long)2  = [" + valueOf((long)2) + "]");
  // System.out.println("valueOf (float)3 = [" + valueOf((float)3) + "]");
  // System.out.println("valueOf (doulb)4 = [" + valueOf((double)4) + "]");

  // System.out.println("valueOf (char)c  = [" + valueOf((char)'c') + "]");
  // System.out.println("valueOf true     = [" + valueOf(true) + "]");
  // System.out.println("valueOf false    = [" + valueOf(false) + "]");

  // char[] chars = "abcde".toCharArray();
  // System.out.println("valueOf 'abcde'  = [" + valueOf(chars) + "]");
  // System.out.println("valueOf 'abcde'  = [" + valueOf(chars, 2, 3) + "]");

  // DsByteString s1 = new DsByteString("Hello");
  // String s2 = "Hell";

  // System.out.println("Hello == Hello -> " + s1.equalsIgnoreCase(s2));

  // DsByteString s1 = new DsByteString("Hello, are you there?");
  // DsByteString s2 = new DsByteString("ello");
  // System.out.println("Hello.indexOf(ello) -> " + s1.indexOf(s2));

  // DsByteString s1 = new DsByteString("hello one");
  // DsByteString s2 = new DsByteString("hello two");
  // DsByteString s3 = new DsByteString("one");
  // DsByteString s4 = new DsByteString("two");
  // DsByteString s5 = new DsByteString("one hello");
  // DsByteString s6 = new DsByteString("two hello");

  // System.out.println("[hello one] ew [one] = " + (s1.endsWith(s3)?"true":"false"));
  // System.out.println("[hello two] ew [one] = " + (s2.endsWith(s3)?"true":"false"));
  // System.out.println("[one hello] sw [one] = " + (s5.startsWith(s3)?"true":"false"));
  // System.out.println("[two hello] sw [one] = " + (s6.startsWith(s3)?"true":"false"));

  // System.out.println("[hello one] ew [two] = " + (s1.endsWith(s4)?"true":"false"));
  // System.out.println("[hello two] ew [two] = " + (s2.endsWith(s4)?"true":"false"));
  // System.out.println("[one hello] sw [two] = " + (s5.startsWith(s4)?"true":"false"));
  // System.out.println("[two hello] sw [two] = " + (s6.startsWith(s4)?"true":"false"));
  // }
}
