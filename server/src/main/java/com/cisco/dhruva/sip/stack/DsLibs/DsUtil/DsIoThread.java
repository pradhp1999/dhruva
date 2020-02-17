// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi.DsTcpConnection;
import org.apache.logging.log4j.Level;

/** A thread that processes I/O based work when it is told what to do. */
public class DsIoThread extends DsThread {
  /** Unique sequence number for a thread. */
  private static int s_threadSequenceNumber = 0;

  /** Determines if the thread should exit gracefully after waking up. */
  private boolean m_markedForDeath;

  /** The thread pool that this thread belongs to. */
  private DsIoThreadPool m_pool;

  /** The connection to get work from. */
  private DsTcpConnection m_conn;

  public Object threadLock = new Object();
  /** Create a new I/O worker thread. */
  DsIoThread(DsIoThreadPool pool) {
    super("I/O-Thread." + s_threadSequenceNumber++);

    m_pool = pool;
  }

  /** Schedule an orderly exit for the thread. */
  public void markForDeath() {
    m_markedForDeath = true;
  }

  /**
   * Set the connection to get work from. Only set this when you know that the thread is sleeping
   * and waiting for work. You may add work to this list while the thread is working, but it must be
   * synchronized within the connection.
   */
  public void setConenction(DsTcpConnection conn) {
    m_conn = conn;
  }

  /** What the thread should do. */
  public void run() {
    Runnable work;
    while (true) {
      try {
        // look for work
        work = null;
        if (m_conn != null) {
          work = (Runnable) m_conn.getQueuedMsg();
        }

        if (work != null) {
          work.run();
        } else {
          synchronized (threadLock) {
            // do not hold the connection after there is no more work to do
            m_conn = null;

            // put myself back on the list of threads available to process messages
            m_pool.put(this);

            try {
              threadLock.wait();
            } catch (InterruptedException i) {
              // nothing to do except resume the enclosing while loop
            }

            if (m_markedForDeath) {
              return;
            }
          }
        }
      } catch (Throwable e) // don't let this thread die
      {
        // log uncaught exception
        if (DsLog4j.threadCat.isEnabled(Level.WARN)) {
          DsLog4j.threadCat.warn("Caught missed exception at I/O thread level:", e);
        }
      }
    }
  }
}
