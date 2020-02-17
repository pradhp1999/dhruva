// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.util.Collections;
import java.util.Vector;

/**
 * This class follows the leaky bucket model. It creates buckets according to the setting parameters
 * supplied to its constructor or reConfigure(). The setting should be int values in the pair of
 * two. This method will create one bucket for each pair of values in setting array. The first value
 * in the pair is the time interval you want to limit number of tokens for and the second value is
 * the number of tokens you want to allow for that time interval. The isOK() method of this classes
 * returns <code>true</code> for a certain token only when each bucket allows it in its current time
 * interval, that is, the token number limit has not been exceeded.
 */
public final class DsBuckets {
  /** The 2 dimensional array representing the configuration. */
  int[][] config;
  /** The number of buckets. */
  int bucketNum;
  /** The buckets. */
  Vector buckets;
  /** The name of these buckets. */
  String name;
  /** <code>true</code> if enabled. */
  boolean running; // = false;

  /**
   * This constructor creates buckets according to its parameter. The length of the two-dimensional
   * array is the number of buckets to be created. Each pair of the array value is used to configure
   * a bucket. The first value in the pair is the time interval you want to limit number of tokens
   * for and the second value is the number of tokens you want to allow for that time interval. Note
   * that you have to call start() to make buckets setting take effect.
   *
   * @param setting a two-dimensional array containing the configure setting for each of buckets
   *     intended to create. The first value in the pair is the time interval you want to limit
   *     number of tokens for and the second value is the number of tokens you want to allow for
   *     that time interval.
   * @throws DsException if there are negative values in setting.
   */
  public DsBuckets(int[][] setting) throws DsException {
    buckets = new Vector(3);
    reConfigure(setting);
  }

  /**
   * Reconfigure buckets with new configure settings. You have to call start() again after
   * reconfiguration to make new setting take effect.
   *
   * @param setting a two-dimensional array containing the configure setting for each of buckets
   *     intended to create.
   * @throws DsException if there are negative values in setting.
   */
  public void reConfigure(int[][] setting) throws DsException {
    if (isRunning()) stop();
    for (int i = 0; i < setting.length; i++) {
      if (setting[i].length != 2) throw new DsException("setting values must be in pairs of 2");
    }
    if (!checkSetting(setting)) {
      throw new DsException("setting values must be positive");
    }
    config = setting;
    bucketNum = config.length;
    buckets.clear();
    for (int index = 0; index < config.length; index++) {
      DsBucket aBucket = new DsBucket(config[index][0], config[index][1]);
      buckets.add(aBucket);
    }
    // sort buckets in ascending order since the one with small intervals should
    // be queried first
    Collections.sort(buckets);
  }

  /** This method actually starts buckets and make settings take effect from this point of time. */
  public void start() {
    for (int index = 0; index < bucketNum; index++) {
      ((DsBucket) buckets.get(index)).start();
    }
    running = true;
  }

  /** This method stops the buckets so that no settings(limits) will be in effect for tokens. */
  public void stop() {
    for (int index = 0; index < bucketNum; index++) {
      ((DsBucket) buckets.get(index)).stop();
    }
    running = false;
  }

  /**
   * This method tells whether it is OK to process a token. If the limit for any one buckets is
   * reached, this method returns false.
   *
   * @return whether it is OK to process a token. If the limit for any one buckets is reached, this
   *     method returns <code>false</code>, <code>true</code> otherwise.
   */
  public final boolean isOK() {
    if (running == false) return true;
    boolean result = true;
    synchronized (this) {
      for (int index = 0; index < bucketNum; index++) {
        result = ((DsBucket) buckets.get(index)).isOK();
        if (!result) {
          // it is overflown, roll back other previous buckets
          for (int i = 0; i < index; i++) {
            ((DsBucket) buckets.get(i)).rollback();
          }
          return result;
        }
      }
    }
    return result;
  }

  /** No negative values are allowed in the setting array. */
  private boolean checkSetting(int[][] setting) {
    for (int index = 0; index < setting.length; index++) {
      if (setting[index][0] <= 0 || setting[index][1] <= 0) {
        return false;
      }
    }

    return true;
  }

  /**
   * Queries the whether buckets are running (settings are in effect).
   *
   * @return <code>true</code> when buckets are running. <code>false</code> otherwise.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Sets name for this DsBuckets.
   *
   * @param aName the name to be assigned to this DsBuckets
   */
  public void setName(String aName) {
    name = aName;
  }

  /**
   * Returns name of these buckets.
   *
   * @return the name of these buckets
   */
  public String getName() {
    return name;
  }

  /**
   * Implementation of a bucket. It allows only specified number of tokens for a specified time
   * interval. It's isOK() returns <code>true</code> when that condition is met and <code>false
   * </code> otherwise.
   */
  private static class DsBucket implements Comparable {
    int interval;
    int numAllowed;
    int available;
    long timestamp;
    // since tmpTime is going to be used a lot, declare it here
    long tmpTime;
    long endTime;
    boolean running; // = false;

    private DsBucket(int anInterval, int aNum) {
      interval = anInterval;
      numAllowed = aNum;
    }

    /** Starts to count. */
    void start() {
      timestamp = System.currentTimeMillis();
      endTime = timestamp + 1000 * interval;
      available = numAllowed;
      running = true;
    }

    /**
     * Checks whether the limit has been exceeded. It decrements the counter if a token is allowed.
     */
    boolean isOK() {
      // no limit right now
      if (running == false) return true;
      // limit is not exceeded
      if (available > 0) {
        available--;
        return true;
      }
      // refill the bucket since we are now in a new time interval
      else if ((tmpTime = System.currentTimeMillis()) >= endTime) {
        available = numAllowed;
        available--;
        timestamp = tmpTime;
        endTime = timestamp + 1000 * interval;
        return true;
      }
      // no refill for now since current time interval is not over yet
      else {
        return false;
      }
    }

    // when the test of all buckets returns false, some may have returned true
    // and available counter have been decremented. restore its value here.
    void rollback() {
      available++;
    }

    void stop() {
      running = false;
    }

    public int compareTo(Object o) {
      DsBucket aBucket = (DsBucket) o;
      return interval - aBucket.interval;
    }
  } // end of DsBucket

  /*
  public static void main(String[] args)
  {
      int[][] setting = new int[][]{
      {20, 40},
      {6, 20},
      {1, 6
      }
      };
      DsBuckets buckets;
      try
      {
          buckets = new DsBuckets(setting);
      }
      catch(DsException e)
      {
          e.printStackTrace();
          return;
      }
      buckets.start();
      for (int i=0; i<50; i++)
      {
          System.out.println("    buckets.isOK()-->"+buckets.isOK());
          try
          {
              Thread.currentThread().sleep(100);
          }
          catch(InterruptedException e)
          {
              e.printStackTrace();
          }
      }
  }
  */
} // end of DsBuckets
