// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/**
 * Provides access to high resolution timer and sleep methods. Still works if the native library
 * does not load, but use the lower resolution Java calls.
 */
public class HighResTimer {
  /** <code>true</code> if the native high res timer lib loaded. */
  private static boolean HI_RES;

  /** Multiply results by this to get to milliseconds. */
  public static double FACTOR_TO_MILLIS;
  /** Multiply results by this to get to microseconds. */
  public static double FACTOR_TO_MICROS;

  static {
    try {
      System.loadLibrary("hrt");
      HI_RES = true;
      FACTOR_TO_MILLIS = 0.000001d;
      FACTOR_TO_MICROS = 0.001d;
    } catch (Throwable e) {
      HI_RES = false;
      FACTOR_TO_MILLIS = 1.0d;
      FACTOR_TO_MICROS = 1000.0d;
    }
  }

  /** Disallow construction. */
  private HighResTimer() {}

  /**
   * Gets the high resolution time.
   *
   * @return the high resolution time.
   */
  private static native long gethrtime();

  /**
   * Sleeps for microsecs microseconds.
   *
   * @param microsecs the number of microseconds to sleep for.
   * @return microsecs.
   */
  public static native int usleep(long microsecs);

  /**
   * Gets the current time. If the native lib loaded, it gets a high resolution time, else it
   * returns System.currentTimeMillis(). Use FACTOR_TO_MILLIS or FACTOR_TO_MICROS to get the results
   * in a consistent form.
   *
   * @return the current time.
   */
  public static long time() {
    if (HI_RES) {
      return gethrtime();
    } else {
      return System.currentTimeMillis();
    }
  }

  /*
      public static void main(String args[])
      {
          long start, end;

          for (int i = 0; i < 100000; i++)
          {
              time();
          }

          start = time();
          do
          {
          } while ((end = time()) == start);

          System.out.println("Time per Tick = " + (end - start)*FACTOR_TO_MILLIS + " ms");
      }
  */
}
