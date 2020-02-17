// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsTcpConnection;
import java.util.*;
import org.apache.logging.log4j.Level;

/**
 * A pool of threads for I/O based work. There is a minimum number of threads that are maintained in
 * the pool at all times. The total number may grow but, threads will die when they are not being
 * used, back down to the minimum.
 */
public class DsIoThreadPool {
  /** Normal Reaper thread interval, in seconds. */
  private static int REAP_TIME = 90;

  /** Fast Reaper thread interval, in seconds. */
  private static int FAST_REAP_TIME = 20;

  /** The pool of threads. */
  private ArrayList m_threads;

  /** The minimum number of threads to keep in the pool. */
  private int m_min;

  /** The Active number of threads. */
  private int m_active;

  /** Default constructor. */
  public DsIoThreadPool() {
    this(16);
  }

  /**
   * Constructor that takes a minimum number of threads.
   *
   * @param num the minimum number of threads.
   */
  public DsIoThreadPool(int num) {
    m_min = num;
    m_threads = new ArrayList(num * 2);

    DsIoThread thread;
    for (int i = 0; i < num; i++) {
      thread = new DsIoThread(this);
      // GOGONG 07.31.06 CSCsd90062 - create DsIoThread as daemon thread
      thread.setDaemon(true);
      // Increment the active count
      m_active++;
      thread.start();
      // We don't need to add these threads in the thread pool
      // as these threads will get added to the pool once they are idle.
      // --            m_threads.add(thread);
    }
    // GOGONG 07.31.06 CSCsd90062 - create ReaperThread as daemon thread.
    ReaperThread reaperThread = new ReaperThread(REAP_TIME, FAST_REAP_TIME);
    reaperThread.setDaemon(true);
    reaperThread.start();
  }

  public DsIoThread assign(DsTcpConnection conn) {
    DsIoThread thread;
    synchronized (m_threads) {
      int size = m_threads.size();

      // Increment the active count
      m_active++;

      if (size == 0) {
        thread = new DsIoThread(this);
        // GOGONG 07.31.06 CSCsd90062 - create DsIoThread as daemon thread.
        thread.setDaemon(true);
        thread.setConenction(conn);
        thread.start();
        return thread;
      }
      thread = (DsIoThread) m_threads.remove(size - 1);
    }

    synchronized (thread.threadLock) {
      thread.setConenction(conn);
      thread.threadLock.notifyAll();
    }

    return thread;
  }

  public void put(DsIoThread thread) {
    synchronized (m_threads) {
      // Decrement the active count
      m_active--;

      m_threads.add(thread);
    }
  }

  /**
   * Returns the number of active threads.
   *
   * @return the number of active threads.
   */
  public int getActiveThreads() {
    return m_active;
  }

  /**
   * Returns the number of minimum threads.
   *
   * @return the number of minimum threads.
   */
  public int getMinimumThreads() {
    return m_min;
  }

  /**
   * Returns the total number of threads, either active or available in the pool.
   *
   * @return the total number of threads, either active or available in the pool.
   */
  public int getTotalThreads() {
    int total = 0;
    synchronized (m_threads) {
      total = m_active + m_threads.size();
    }
    return total;
  }

  class ReaperThread extends DsThread {
    private long m_normalTime;
    private long m_fastTime;
    private long m_time;

    public ReaperThread(int normalTime, int fastTime) {
      m_normalTime = normalTime * 1000;
      m_fastTime = fastTime * 1000;

      m_time = normalTime;
    }

    public void run() {
      DsIoThread thread;

      while (true) {
        try {
          sleep(m_time);

          thread = null;
          int size = 0;
          synchronized (m_threads) {
            size = m_threads.size();

            if (size > m_min) {
              thread = (DsIoThread) m_threads.remove(size - 1);
            }
          }

          if (thread != null) {
            if (DsLog4j.threadCat.isEnabled(Level.INFO)) {
              DsLog4j.threadCat.info(
                  "Thread--["
                      + thread.getName()
                      + "] marked for death at the Pool size ["
                      + size
                      + "].");
            }
            synchronized (thread.threadLock) {
              thread.markForDeath();
              thread.threadLock.notifyAll();
            }

            // sleep less when there are extra threads, so we kill them faster
            m_time = m_fastTime;
          } else {
            // sleep longer while there are no extra threads
            m_time = m_normalTime;
          }
        } catch (Exception e) {
          if (DsLog4j.threadCat.isEnabled(Level.INFO)) {
            DsLog4j.threadCat.info("Exception in the I/O-Thread Reaper ...", e);
          }
        }
      }
    }
  }
}
