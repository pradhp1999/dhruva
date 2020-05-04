// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.event.Level;

/** This class manages the assignment of threads from a thread pool to the work in the queue. */
public class DsQProcessor extends DsThread {
  private DsWorkQueue workQ; // the queue I get my work from
  private boolean markedForDeath; // should I exit after I wake up?
  private static int threadSequenceNumber = 0; // unique identifier for a thread
  public Object threadLock = new Object();
  /**
   * Associate a queue of work with a thread to process it.
   *
   * @param work the queue to look for work on
   */
  DsQProcessor(DsWorkQueue work) {
    super(work.getName() + "." + threadSequenceNumber++);
    workQ = work;
    markedForDeath = false;
  }

  /** Schedule an orderly exit for the thread. */
  public void markForDeath() {
    markedForDeath = true;
  }

  /** What the thread should do. */
  public void run() {
    while (true) {
      try {
        // look for work
        DsUnitOfWork m = workQ.dqueue();
        if (m != null) {
          ThreadContext.clearMap();
          m.process();
        } else {
          // System.out.println(">>> I have nothing left to do");

          synchronized (threadLock) {
            // put myself back on the list of threads available to process messages
            workQ.available(this);

            // System.out.println(">>> Waiting for more work: " + this);
            try {
              threadLock.wait();
            } catch (InterruptedException i) {
              // nothing  to do except resume the enclosing while loop
            }

            if (markedForDeath) {
              return;
            }
          }

          // System.out.println (">>> I'm waking up... lets see if there is anything to do");
        }
      } catch (Throwable e) // don't let this thread die, we need to maintain count
      {
        // log uncaught exception
        if (DsLog4j.threadCat.isEnabled(Level.WARN)) {
          DsLog4j.threadCat.warn("Caught missed exception at thread level:", e);
        }
      }
    }
  }
}
