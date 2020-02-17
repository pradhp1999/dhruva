// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;

/**
 * A class for quickly converting an int to a String without creating any extra Strings or char
 * arrays.
 *
 * <p>With the addition of DsByteString, this class now also converts an int to a byte array.
 */
public final class DsIntStrCache {
  /** Default maximum number of ints to store, default size it 16KB. */
  private static int MAX_SIZE = 16 * 1024;
  /** Array for storing ints as Strings. */
  private static String[] strArray = new String[MAX_SIZE];
  /** Array for storing ints as byte arrays. */
  private static byte[][] bytesArray = new byte[MAX_SIZE][];
  /** Determines if this cache is in use or not, default is true. */
  private static boolean isOn = true;

  /** The constructor. */
  public DsIntStrCache() {}

  /**
   * Convert an int into a string.
   *
   * @param i the integer to convert
   * @return the string representation of the specified integer
   */
  public static final String intToStr(int i) {
    if (!isOn || i < 0 || i >= MAX_SIZE) // never in cache
    {
      // let java do it
      return Integer.toString(i, 10);
    }

    if (strArray[i] == null) // not cached yet
    {
      strArray[i] = Integer.toString(i, 10);
    }

    return strArray[i];
  }

  /**
   * Convert a long into a string.
   *
   * @param i the long integer to convert
   * @return the string representation of the specified long integer
   */
  public static final String intToStr(long i) {
    if (!isOn || i < 0 || i >= MAX_SIZE) // never in cache
    {
      // let java do it
      return Long.toString(i, 10);
    }

    if (strArray[(int) i] == null) // not cached yet
    {
      strArray[(int) i] = Integer.toString((int) i, 10);
    }

    return strArray[(int) i];
  }

  /**
   * Convert an int into a byte[].
   *
   * @param i the integer to convert
   * @return the byte[] representation of the specified integer
   */
  public static final byte[] intToBytes(int i) {
    if (!isOn || i < 0 || i >= MAX_SIZE) // never in cache
    {
      // let java do it
      return DsByteString.getBytes(Integer.toString(i, 10));
    }

    if (bytesArray[i] == null) // not cached yet
    {
      bytesArray[i] = DsByteString.getBytes(Integer.toString(i, 10));
    }

    return bytesArray[i];
  }

  /**
   * Convert an int into a byte[].
   *
   * @param i the long to convert
   * @return the byte[] representation of the specified long
   */
  public static final byte[] intToBytes(long i) {
    if (!isOn || i < 0 || i >= MAX_SIZE) // never in cache
    {
      // let java do it
      return DsByteString.getBytes(Long.toString(i, 10));
    }

    if (bytesArray[(int) i] == null) // not cached yet
    {
      bytesArray[(int) i] = DsByteString.getBytes(Integer.toString((int) i, 10));
    }

    return bytesArray[(int) i];
  }

  /** Turn caching off. Frees the memory that was associated with the cache. */
  public static void off() {
    isOn = false;
    if (strArray == null) {
      return;
    }

    for (int i = 0; i < strArray.length; i++) {
      strArray[i] = null;
    }
    strArray = null;

    for (int i = 0; i < bytesArray.length; i++) {
      bytesArray[i] = null;
    }
    bytesArray = null;

    return;
  }

  /**
   * Turn caching on.
   *
   * @param size the upper bound of integers to convert, the range starts at zero.
   */
  public static void on(int size) {
    MAX_SIZE = size;
    on();
  }

  /**
   * Turn caching on, defaulting the range of cached integers from 0 to MAX_SIZE, or what was set in
   * on(int) last.
   */
  public static void on() {
    strArray = new String[MAX_SIZE];
    bytesArray = new byte[MAX_SIZE][];
    isOn = true;
  }
}
