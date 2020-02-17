// Copyright (c) 2005-2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.*;

/** A class used to convert Hex values to bytes and bytes back to hex values. */
public class DsHexEncoding {

  /**
   * Converts used to convert the specified byte array to hex string.
   *
   * @param message the data to encode
   * @return the hex encoded string
   */
  public static String toHex(byte[] message) {
    StringBuffer buffer = new StringBuffer(message.length * 2);

    int length = message.length;

    for (int j = 0; j < length; j++) {
      String buf = Integer.toString(message[j] & 0x000000ff, 16);

      if (buf.length() == 1) {
        buffer.append('0');
      }

      buffer.append(buf);
    }

    return buffer.substring(0, buffer.length());
  }

  /**
   * Converts used to convert the specified character array to hex string.
   *
   * @param message character array data to encode
   * @return the hex encoded string
   */
  public static String toHex(char[] message) {
    StringBuffer buffer = new StringBuffer(message.length * 2);

    int length = message.length;

    for (int j = 0; j < length; j++) {
      String buf = Integer.toString(message[j] & 0x000000ff, 16);

      if (buf.length() == 1) {
        buffer.append('0');
      }

      buffer.append(buf);
    }

    return buffer.substring(0, buffer.length());
  }

  /**
   * Converts used to convert a character to hex string.
   *
   * @param aChar the char to encode
   * @return the hex encoded string
   */
  public static String toHex(char aChar) {
    StringBuffer buffer = new StringBuffer(2);

    String buf = Integer.toString(aChar & 0x000000ff, 16);

    if (buf.length() == 1) {
      buffer.append('0');
    }

    buffer.append(buf);

    return buffer.toString();
  }

  /**
   * Converts used to convert the specified hex string to byte array.
   *
   * @param message the hex string
   * @return the decoded string
   * @throws DsException if the string length is not even
   */
  public static byte[] fromHex(String message) throws DsException {
    final char string[] = message.toCharArray();

    final int stringLen = string.length;

    if (stringLen % 2 != 0) {
      throw new DsException("input hex string should be of even length");
    }

    byte buffer[] = new byte[stringLen / 2];
    int bufPos = 0;

    for (int i = 0; i < stringLen; i += 2) {
      char c = string[i];
      byte low;
      byte high;

      if (Character.isDigit(c)) {
        low = (byte) (c - '0');
      } else if (c >= 'a' && c <= 'f') {
        low = (byte) (c - ('a' - 10));
      } else if (c >= 'A' && c <= 'F') {
        low = (byte) (c - ('A' - 10));
      } else {
        throw new DsException("input string is not hex string");
      }
      c = string[i + 1];

      if (Character.isDigit(c)) {
        high = (byte) (c - '0');
      } else if (c >= 'a' && c <= 'f') {
        high = (byte) (c - ('a' - 10));
      } else if (c >= 'A' && c <= 'F') {
        high = (byte) (c - ('A' - 10));
      } else {
        throw new DsException("input string is not hex string");
      }

      buffer[bufPos++] = (byte) ((low) * 16 + high);
    }

    return buffer;
  }

  /**
   * Converts used to convert the hex encoded char array to char.
   *
   * @param hexValue the hex encoded char array
   * @return the decoded character
   * @throws DsException if the size of input char array is not equal to 2
   */
  public static char fromHex(char[] hexValue) throws DsException {
    char c = hexValue[0];
    byte low;
    byte high;

    if (hexValue.length != 2) {
      throw new DsException("This method only takes array of size 2");
    }
    if (Character.isDigit(c)) {
      low = (byte) (c - '0');
    } else if (c >= 'a' && c <= 'f') {
      low = (byte) (c - ('a' - 10));
    } else if (c >= 'A' && c <= 'F') {
      low = (byte) (c - ('A' - 10));
    } else {
      throw new DsException("input array is not hex array");
    }

    c = hexValue[1];

    if (Character.isDigit(c)) {
      high = (byte) (c - '0');
    } else if (c >= 'a' && c <= 'f') {
      high = (byte) (c - ('a' - 10));
    } else if (c >= 'A' && c <= 'F') {
      high = (byte) (c - ('A' - 10));
    } else {
      throw new DsException("input array is not hex array");
    }

    return (char) (byte) ((low) * 16 + high);
  }

  /**
   * Converts used to convert the hex encoded byte array to byte.
   *
   * @param hexValue the hex encoded byte array
   * @param off the offset in the byte array where from the hex-encoded byte starts.
   * @return the decoded byte
   * @throws DsException if the number of available bytes in the input byte array is less than 2
   */
  public static byte fromHex(byte[] hexValue, int off) throws DsException {
    byte c = hexValue[off];
    byte low;
    byte high;

    if ((hexValue.length - off) < 2) {
      throw new DsException("The number of available bytes is < 2");
    }
    if (Character.isDigit((char) c)) {
      low = (byte) (c - '0');
    } else if (c >= 'a' && c <= 'f') {
      low = (byte) (c - ('a' - 10));
    } else if (c >= 'A' && c <= 'F') {
      low = (byte) (c - ('A' - 10));
    } else {
      throw new DsException("input array is not hex array");
    }

    c = hexValue[off + 1];

    if (Character.isDigit((char) c)) {
      high = (byte) (c - '0');
    } else if (c >= 'a' && c <= 'f') {
      high = (byte) (c - ('a' - 10));
    } else if (c >= 'A' && c <= 'F') {
      high = (byte) (c - ('A' - 10));
    } else {
      throw new DsException("input array is not hex array");
    }

    return (byte) ((low) * 16 + high);
  }

  public static DsByteString toHexByteString(byte[] bytes) {
    return new DsByteString(toHexBytes(bytes));
  }

  public static byte[] toHexBytes(byte[] bytes) {
    int length = bytes.length;
    byte[] result = new byte[length * 2];

    int index;
    for (int j = 0; j < length; j++) {
      index = bytes[j] & 0x000000ff;

      result[j * 2] = m_lookup[index][0];
      result[(j * 2) + 1] = m_lookup[index][1];
    }

    // make sure that we copy the bytes from the thread local
    return result;
  }

  private static final byte[][] m_lookup = {
    {(byte) '0', (byte) '0'},
    {(byte) '0', (byte) '1'},
    {(byte) '0', (byte) '2'},
    {(byte) '0', (byte) '3'},
    {(byte) '0', (byte) '4'},
    {(byte) '0', (byte) '5'},
    {(byte) '0', (byte) '6'},
    {(byte) '0', (byte) '7'},
    {(byte) '0', (byte) '8'},
    {(byte) '0', (byte) '9'},
    {(byte) '0', (byte) 'a'},
    {(byte) '0', (byte) 'b'},
    {(byte) '0', (byte) 'c'},
    {(byte) '0', (byte) 'd'},
    {(byte) '0', (byte) 'e'},
    {(byte) '0', (byte) 'f'},
    {(byte) '1', (byte) '0'},
    {(byte) '1', (byte) '1'},
    {(byte) '1', (byte) '2'},
    {(byte) '1', (byte) '3'},
    {(byte) '1', (byte) '4'},
    {(byte) '1', (byte) '5'},
    {(byte) '1', (byte) '6'},
    {(byte) '1', (byte) '7'},
    {(byte) '1', (byte) '8'},
    {(byte) '1', (byte) '9'},
    {(byte) '1', (byte) 'a'},
    {(byte) '1', (byte) 'b'},
    {(byte) '1', (byte) 'c'},
    {(byte) '1', (byte) 'd'},
    {(byte) '1', (byte) 'e'},
    {(byte) '1', (byte) 'f'},
    {(byte) '2', (byte) '0'},
    {(byte) '2', (byte) '1'},
    {(byte) '2', (byte) '2'},
    {(byte) '2', (byte) '3'},
    {(byte) '2', (byte) '4'},
    {(byte) '2', (byte) '5'},
    {(byte) '2', (byte) '6'},
    {(byte) '2', (byte) '7'},
    {(byte) '2', (byte) '8'},
    {(byte) '2', (byte) '9'},
    {(byte) '2', (byte) 'a'},
    {(byte) '2', (byte) 'b'},
    {(byte) '2', (byte) 'c'},
    {(byte) '2', (byte) 'd'},
    {(byte) '2', (byte) 'e'},
    {(byte) '2', (byte) 'f'},
    {(byte) '3', (byte) '0'},
    {(byte) '3', (byte) '1'},
    {(byte) '3', (byte) '2'},
    {(byte) '3', (byte) '3'},
    {(byte) '3', (byte) '4'},
    {(byte) '3', (byte) '5'},
    {(byte) '3', (byte) '6'},
    {(byte) '3', (byte) '7'},
    {(byte) '3', (byte) '8'},
    {(byte) '3', (byte) '9'},
    {(byte) '3', (byte) 'a'},
    {(byte) '3', (byte) 'b'},
    {(byte) '3', (byte) 'c'},
    {(byte) '3', (byte) 'd'},
    {(byte) '3', (byte) 'e'},
    {(byte) '3', (byte) 'f'},
    {(byte) '4', (byte) '0'},
    {(byte) '4', (byte) '1'},
    {(byte) '4', (byte) '2'},
    {(byte) '4', (byte) '3'},
    {(byte) '4', (byte) '4'},
    {(byte) '4', (byte) '5'},
    {(byte) '4', (byte) '6'},
    {(byte) '4', (byte) '7'},
    {(byte) '4', (byte) '8'},
    {(byte) '4', (byte) '9'},
    {(byte) '4', (byte) 'a'},
    {(byte) '4', (byte) 'b'},
    {(byte) '4', (byte) 'c'},
    {(byte) '4', (byte) 'd'},
    {(byte) '4', (byte) 'e'},
    {(byte) '4', (byte) 'f'},
    {(byte) '5', (byte) '0'},
    {(byte) '5', (byte) '1'},
    {(byte) '5', (byte) '2'},
    {(byte) '5', (byte) '3'},
    {(byte) '5', (byte) '4'},
    {(byte) '5', (byte) '5'},
    {(byte) '5', (byte) '6'},
    {(byte) '5', (byte) '7'},
    {(byte) '5', (byte) '8'},
    {(byte) '5', (byte) '9'},
    {(byte) '5', (byte) 'a'},
    {(byte) '5', (byte) 'b'},
    {(byte) '5', (byte) 'c'},
    {(byte) '5', (byte) 'd'},
    {(byte) '5', (byte) 'e'},
    {(byte) '5', (byte) 'f'},
    {(byte) '6', (byte) '0'},
    {(byte) '6', (byte) '1'},
    {(byte) '6', (byte) '2'},
    {(byte) '6', (byte) '3'},
    {(byte) '6', (byte) '4'},
    {(byte) '6', (byte) '5'},
    {(byte) '6', (byte) '6'},
    {(byte) '6', (byte) '7'},
    {(byte) '6', (byte) '8'},
    {(byte) '6', (byte) '9'},
    {(byte) '6', (byte) 'a'},
    {(byte) '6', (byte) 'b'},
    {(byte) '6', (byte) 'c'},
    {(byte) '6', (byte) 'd'},
    {(byte) '6', (byte) 'e'},
    {(byte) '6', (byte) 'f'},
    {(byte) '7', (byte) '0'},
    {(byte) '7', (byte) '1'},
    {(byte) '7', (byte) '2'},
    {(byte) '7', (byte) '3'},
    {(byte) '7', (byte) '4'},
    {(byte) '7', (byte) '5'},
    {(byte) '7', (byte) '6'},
    {(byte) '7', (byte) '7'},
    {(byte) '7', (byte) '8'},
    {(byte) '7', (byte) '9'},
    {(byte) '7', (byte) 'a'},
    {(byte) '7', (byte) 'b'},
    {(byte) '7', (byte) 'c'},
    {(byte) '7', (byte) 'd'},
    {(byte) '7', (byte) 'e'},
    {(byte) '7', (byte) 'f'},
    {(byte) '8', (byte) '0'},
    {(byte) '8', (byte) '1'},
    {(byte) '8', (byte) '2'},
    {(byte) '8', (byte) '3'},
    {(byte) '8', (byte) '4'},
    {(byte) '8', (byte) '5'},
    {(byte) '8', (byte) '6'},
    {(byte) '8', (byte) '7'},
    {(byte) '8', (byte) '8'},
    {(byte) '8', (byte) '9'},
    {(byte) '8', (byte) 'a'},
    {(byte) '8', (byte) 'b'},
    {(byte) '8', (byte) 'c'},
    {(byte) '8', (byte) 'd'},
    {(byte) '8', (byte) 'e'},
    {(byte) '8', (byte) 'f'},
    {(byte) '9', (byte) '0'},
    {(byte) '9', (byte) '1'},
    {(byte) '9', (byte) '2'},
    {(byte) '9', (byte) '3'},
    {(byte) '9', (byte) '4'},
    {(byte) '9', (byte) '5'},
    {(byte) '9', (byte) '6'},
    {(byte) '9', (byte) '7'},
    {(byte) '9', (byte) '8'},
    {(byte) '9', (byte) '9'},
    {(byte) '9', (byte) 'a'},
    {(byte) '9', (byte) 'b'},
    {(byte) '9', (byte) 'c'},
    {(byte) '9', (byte) 'd'},
    {(byte) '9', (byte) 'e'},
    {(byte) '9', (byte) 'f'},
    {(byte) 'a', (byte) '0'},
    {(byte) 'a', (byte) '1'},
    {(byte) 'a', (byte) '2'},
    {(byte) 'a', (byte) '3'},
    {(byte) 'a', (byte) '4'},
    {(byte) 'a', (byte) '5'},
    {(byte) 'a', (byte) '6'},
    {(byte) 'a', (byte) '7'},
    {(byte) 'a', (byte) '8'},
    {(byte) 'a', (byte) '9'},
    {(byte) 'a', (byte) 'a'},
    {(byte) 'a', (byte) 'b'},
    {(byte) 'a', (byte) 'c'},
    {(byte) 'a', (byte) 'd'},
    {(byte) 'a', (byte) 'e'},
    {(byte) 'a', (byte) 'f'},
    {(byte) 'b', (byte) '0'},
    {(byte) 'b', (byte) '1'},
    {(byte) 'b', (byte) '2'},
    {(byte) 'b', (byte) '3'},
    {(byte) 'b', (byte) '4'},
    {(byte) 'b', (byte) '5'},
    {(byte) 'b', (byte) '6'},
    {(byte) 'b', (byte) '7'},
    {(byte) 'b', (byte) '8'},
    {(byte) 'b', (byte) '9'},
    {(byte) 'b', (byte) 'a'},
    {(byte) 'b', (byte) 'b'},
    {(byte) 'b', (byte) 'c'},
    {(byte) 'b', (byte) 'd'},
    {(byte) 'b', (byte) 'e'},
    {(byte) 'b', (byte) 'f'},
    {(byte) 'c', (byte) '0'},
    {(byte) 'c', (byte) '1'},
    {(byte) 'c', (byte) '2'},
    {(byte) 'c', (byte) '3'},
    {(byte) 'c', (byte) '4'},
    {(byte) 'c', (byte) '5'},
    {(byte) 'c', (byte) '6'},
    {(byte) 'c', (byte) '7'},
    {(byte) 'c', (byte) '8'},
    {(byte) 'c', (byte) '9'},
    {(byte) 'c', (byte) 'a'},
    {(byte) 'c', (byte) 'b'},
    {(byte) 'c', (byte) 'c'},
    {(byte) 'c', (byte) 'd'},
    {(byte) 'c', (byte) 'e'},
    {(byte) 'c', (byte) 'f'},
    {(byte) 'd', (byte) '0'},
    {(byte) 'd', (byte) '1'},
    {(byte) 'd', (byte) '2'},
    {(byte) 'd', (byte) '3'},
    {(byte) 'd', (byte) '4'},
    {(byte) 'd', (byte) '5'},
    {(byte) 'd', (byte) '6'},
    {(byte) 'd', (byte) '7'},
    {(byte) 'd', (byte) '8'},
    {(byte) 'd', (byte) '9'},
    {(byte) 'd', (byte) 'a'},
    {(byte) 'd', (byte) 'b'},
    {(byte) 'd', (byte) 'c'},
    {(byte) 'd', (byte) 'd'},
    {(byte) 'd', (byte) 'e'},
    {(byte) 'd', (byte) 'f'},
    {(byte) 'e', (byte) '0'},
    {(byte) 'e', (byte) '1'},
    {(byte) 'e', (byte) '2'},
    {(byte) 'e', (byte) '3'},
    {(byte) 'e', (byte) '4'},
    {(byte) 'e', (byte) '5'},
    {(byte) 'e', (byte) '6'},
    {(byte) 'e', (byte) '7'},
    {(byte) 'e', (byte) '8'},
    {(byte) 'e', (byte) '9'},
    {(byte) 'e', (byte) 'a'},
    {(byte) 'e', (byte) 'b'},
    {(byte) 'e', (byte) 'c'},
    {(byte) 'e', (byte) 'd'},
    {(byte) 'e', (byte) 'e'},
    {(byte) 'e', (byte) 'f'},
    {(byte) 'f', (byte) '0'},
    {(byte) 'f', (byte) '1'},
    {(byte) 'f', (byte) '2'},
    {(byte) 'f', (byte) '3'},
    {(byte) 'f', (byte) '4'},
    {(byte) 'f', (byte) '5'},
    {(byte) 'f', (byte) '6'},
    {(byte) 'f', (byte) '7'},
    {(byte) 'f', (byte) '8'},
    {(byte) 'f', (byte) '9'},
    {(byte) 'f', (byte) 'a'},
    {(byte) 'f', (byte) 'b'},
    {(byte) 'f', (byte) 'c'},
    {(byte) 'f', (byte) 'd'},
    {(byte) 'f', (byte) 'e'},
    {(byte) 'f', (byte) 'f'}
  };
}
