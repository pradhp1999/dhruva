// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * A subclass of Thread created to hold ThreadLocals the way that JDK 1.3 does as opposed to JDK
 * 1.2.2, so we can have this performance optimization in 1.2.2. All code that executes in a
 * non-DsThread will still perform properly, but may not run as quickly where ThreadLocals are used.
 * Note also that DsThreadLocal should be used instead of ThreadLocal, especially when running with
 * JDK 1.2.2.
 *
 * <p>When creating threads, it is important to pool them and make sure that the exist for a while
 * and are not constantly created. The are many optimizations in the code that make use of
 * ThreadLocals. These optimizations will actually hamper performance if they have to be created
 * over and over again because threads are not living long enough. Again, functionally it will work,
 * but performance will suffer.
 *
 * <p>DsThreadLocal has been removed. This is now just a shell class that wraps Thread for the sole
 * reason of being backward compatible.
 */
public class DsThread extends Thread {
  /** Default constructor. */
  public DsThread() {
    super();
  }

  /**
   * Constructor that takes a Runnable.
   *
   * @param r the runnable for this thread.
   */
  public DsThread(Runnable r) {
    super(r);
  }

  /**
   * Constructor that takes a Runnable, and a name.
   *
   * @param r the runnable for this thread.
   * @param name the name of this thread.
   */
  public DsThread(Runnable r, String name) {
    super(r, name);
  }

  /**
   * Constructor that takes a name.
   *
   * @param name the name of this thread.
   */
  public DsThread(String name) {
    super(name);
  }
}
