// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.util.*;

/** A Timing class adapted from Murali Sharma's C++ version. */
public class Timing {
  /** The default number of iterations. */
  public static final int DEFAULT_ITERATIONS = 100;
  /** The default factor. */
  public static final int DEFAULT_FACTOR = 10;
  /** The default percentage. */
  public static final int DEFAULT_PCT = 10;

  public static class DataPoint {
    /**
     * Stream output of DataPoint, with three digits of precision. The output is of the form (one
     * data point per line): trials min 5th median 95th max percent where the values are - trials:
     * the number of trials - min: time of the fastest run - 5th: time 95% of data was faster than -
     * median: time half the data is faster than - 95th: time 95% of data was slower than - max: the
     * slowest runtime - percent: the mid 90% of the data is within this percentage of the median.
     */
    public String toString() {
      // precision(3)
      return "Trials:\t"
          + trials
          + "\t"
          + "\nMin:\t"
          + min
          + "\t"
          + "\nFifth:\t"
          + fifth_percentile
          + "\t"
          + "\nMedian:\t"
          + median
          + "\t"
          + "\n95%:\t"
          + ninety_fifth_percentile
          + "\t"
          + "\nMax:\t"
          + max
          + "\t"
          + "\nPercent:\t"
          + percent;
    }

    public int trials;
    public double median;
    public double min;
    public double max;
    public double fifth_percentile;
    public double ninety_fifth_percentile;
    public double percent;
  }

  /**
   * Measures the time it takes to query the clock. Waits until a tick happens, and then repeatedly
   * queries the clock until the next tick happens. The clock overhead is then the interval divided
   * by the number of queries.
   *
   * @return overhead of querying the clock, in microseconds.
   */
  static double clock_overhead() {
    long count = 0;
    long start;
    long k = System.currentTimeMillis();
    do start = System.currentTimeMillis();
    while (start == k);
    // a tick just happened
    while ((k = System.currentTimeMillis()) == start) ++count; // count to next tick
    return ((double) (k - start)) / (double) count;
  }

  static DataPoint measure_clock() {
    return measure_clock(DEFAULT_ITERATIONS, DEFAULT_PCT);
  }

  static DataPoint clock_ovhd = null;

  static {
    clock_ovhd = measure_clock();
  }

  /**
   * Statistically measures the time to query the clock. A trial consists of twenty measurements of
   * the clock overhead. The data from successive trials is analyzed cumulatively - no data is
   * disregarded to prevent bias. The trials continue until either the analysis reveals that the
   * measurements are sufficiently close to the median or (to prevent a fruitless infinite attempt)
   * a maximum number of trials have been conducted. The measurements are considered adequate when
   * the 5th and 95th percentiles are within the required percentage of the median. Then, the median
   * can, to this degree of accuracy, be chosen as the clock overhead.
   *
   * @param maxtrials Attempt up to this many trials (Default is 100).
   * @param percent Desired accuracy of measurement (Default is 10).
   * @return the data point
   */
  static DataPoint measure_clock(int maxtrials, int percent) {
    try {
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    } catch (Exception exc) {
    }
    int factor = DEFAULT_FACTOR; // start with 20 elements
    double[] mv = new double[2 * maxtrials * factor];
    DataPoint d = new DataPoint();
    int n = 0;

    do {
      ++n;
      int len = 2 * n * factor;
      for (int i = 2 * factor; i > 0; --i) mv[len - i] = clock_overhead();

      Arrays.sort(mv, 0, len);

      d.median = (mv[n * factor] + mv[n * factor - 1]) / 2.;
      // at the nth iteration need to look at elements mv[n] and
      // mv[mv.size()-n-1] to check if they are within desired
      // limits.
      d.trials = len;
      d.min = mv[0];
      d.max = mv[len - 1];
      d.fifth_percentile = mv[n];
      d.ninety_fifth_percentile = mv[len - 1 - n];
      d.percent =
          100.
              / d.median
              * Math.max((d.median - d.fifth_percentile), (d.ninety_fifth_percentile - d.median));
      if (d.percent < percent) {
        return d; // done!
      }
    } while (n < maxtrials);
    return d; // couldn't reach the desired accuracy.
  }

  /**
   * Performs one trial of 20 measurements of the execution time for f(). Each measurement invokes
   * f() a sufficient number of times, so that at least 10 milliseconds elapse, and calculates the
   * execution time as follows. The measurement starts at a clock tick. The clock is checked after
   * one call of f(), then 2, then 4, and so on, to see if this is true. When 10 milliseconds have
   * elapsed, the clock is queried repeatedly until the next tick. Thus, the interval between some
   * two ticks of the clock has been consumed by a certain number of invocations of f(), and another
   * of clock(). Knowing the interval, the overhead of querying the clock, and these two counts
   * gives us the execution time of f().
   *
   * @param f The function whose execution time is being measured.
   * @param mv The vector to store the measurements in.
   */
  static void measure_aux(Runnable f, double[] mv, int len) {

    int factor = DEFAULT_FACTOR;

    // wait until tick
    long start = 0, finish = 0;
    long now = System.currentTimeMillis();
    do start = System.currentTimeMillis();
    while (start == now);

    // make 2*factor new measurements, each of enough iterations
    // of the function call to fill up clocklimit.
    for (int i = 2 * factor; i > 0; --i) {
      long totalcalls = 0, numcalls = 1, clockcalls = 0;
      long clocklimit = start + 10;
      do {
        while (totalcalls < numcalls) {
          f.run();
          ++totalcalls;
        }
        numcalls *= 2;
        ++clockcalls;
      } while ((now = System.currentTimeMillis()) < clocklimit);

      // fill up up to next tick with clock calls.
      do ++clockcalls;
      while ((finish = System.currentTimeMillis()) == now);

      // record measurement and time for next measurement
      mv[len - i] =
          ((double) ((finish - start) - (clockcalls * clock_ovhd.median))) / (double) totalcalls;
      start = finish;
    }
  }

  public static DataPoint measure(Runnable r) {
    return measure(r, DEFAULT_ITERATIONS, DEFAULT_PCT);
  }

  /**
   * Statistically measures the execution time of f(). Performs a sufficient number of trials (using
   * measure_aux()) so that the median of the accumulated measurements can reliably be taken to be a
   * measure of the execution time of the function. That is, 90% of the measurements are required to
   * be within the input percentage of the median value. To avoid a potentially infinite attempt, a
   * maximum number of trials is performed.
   *
   * @param f The function whose execution time is measured.
   * @param maxtrials Maximum number of trials.
   * @param percent Desired accuracy of the measurements.
   * @return DataPoint The execution time.
   */
  public static DataPoint measure(Runnable f, int maxtrials, int percent) {
    try {
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    } catch (Exception exc) {
    }
    int factor = DEFAULT_FACTOR; // start with 20 elements
    double[] mv = new double[2 * maxtrials * factor];
    DataPoint d = new DataPoint();
    int n = 0;

    do {
      ++n;
      int len = 2 * n * factor;
      measure_aux(f, mv, len);
      Arrays.sort(mv, 0, len);

      d.median = (mv[n * factor] + mv[n * factor - 1]) / 2.;
      // at the nth iteration need to look at elements mv[n] and
      // mv[mv.size()-n-1] to check if they are within desired
      // limits.
      d.trials = len;
      d.min = mv[0];
      d.max = mv[len - 1];
      d.fifth_percentile = mv[n];
      d.ninety_fifth_percentile = mv[len - 1 - n];
      d.percent =
          100.
              / d.median
              * Math.max((d.median - d.fifth_percentile), (d.ninety_fifth_percentile - d.median));

      // at the nth iteration need to look at elements mv[n] and
      // mv[mv.size()-n-1] to check if they are within desired
      // limits.
      if (d.percent < percent) return d; // done!
    } while (n < maxtrials);
    return d;
  }

  //  ///////////////////////////////////////////////////////
  //  ///////////////////////////////////////////////////////
  //  /     use Timing to test cloning times
  //  ///////////////////////////////////////////////////////
  //  ///////////////////////////////////////////////////////

  static class c1 {
    c1(int c11) {
      this.c11 = c11;
    }

    int c11;

    public Object clone() {
      c1 ret = null;
      try {
        ret = (c1) super.clone();
      } catch (CloneNotSupportedException cne) {
      }
      return ret;
    }

    public String toString() {
      return c11 + "";
    }
  }

  static class c2 extends c1 {
    int c21;

    c2(int c11, int c21) {
      super(c11);
      this.c21 = c21;
    }

    public Object clone() {
      c2 ret = (c2) super.clone();
      return ret;
    }

    public String toString() {
      return c21 + " " + super.toString();
    }
  }

  static class c3 extends c2 {
    int c31;

    c3(int c11, int c21, int c31) {
      super(c11, c21);
      this.c31 = c31;
    }

    public Object clone() {
      c3 ret = (c3) super.clone();
      return ret;
    }

    public String toString() {
      return c31 + " " + super.toString();
    }
  }

  static class a {
    int a1;
    int a2;
    int a3;
    int a5;

    a(int a1, int a2) {
      this.a2 = a2;
      this.a1 = a1;
    }

    public Object clone() {
      a ret = null;
      try {
        ret = (a) super.clone();
      } catch (CloneNotSupportedException cne) {
      }
      return ret;
    }

    public String toString() {
      return a1 + " " + a2;
    }
  }

  static class f1 implements Runnable {
    a a1 = new a(1, 3);

    public void run() {
      a1.clone();
    }
  }

  static class f2 implements Runnable {
    c2 c = new c2(1, 3);

    public void run() {
      c.clone();
    }
  }

  static class f3 implements Runnable {
    c3 c = new c3(1, 3, 4);

    public void run() {
      c.clone();
    }
  }

  public static void main(String args[]) {
    f1 F1 = new f1();
    f2 F2 = new f2();
    f3 F3 = new f3();

    for (int i = 0; i < 20000; ++i) {
      F2.run();
      F3.run();
      F1.run();
    }

    int n = 0;
    try {
      n = Integer.parseInt(args[0]);

      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    } catch (Exception exc) {
    }

    switch (n) {
      case 1:
        while (true) {
          Timing.DataPoint d = Timing.measure(F1);
          System.out.println("%" + d.percent + " trials = " + d.trials + " median = " + d.median);
        }
      case 2:
        while (true) {
          Timing.DataPoint d = Timing.measure(F2);
          System.out.println("%" + d.percent + " trials = " + d.trials + " median = " + d.median);
        }
      case 3:
        while (true) {
          Timing.DataPoint d = Timing.measure(F3);
          System.out.println("%" + d.percent + " trials = " + d.trials + " median = " + d.median);
        }
    }
  }
}
