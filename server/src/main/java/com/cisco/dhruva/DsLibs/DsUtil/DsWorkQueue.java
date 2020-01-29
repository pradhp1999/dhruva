// Copyright (c) 2005-2009 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import java.util.*;
import java.util.concurrent.*;
import org.apache.logging.log4j.Level;

/**
 * A work queue is the abstraction for a queue of things to be done.
 *
 * <p>The queue has a set of threads that are associated with it that are responsible for pulling
 * work off the queue and executing it.
 *
 * <p>The unit of work enqueued must be an object that implements the DsUnitOfWork interface.
 *
 * <p>This pattern permits a queue to contain a wide variety of different kinds of work to be done.
 * It therefore becomes the implementer's choice to decide if a given queue only processes a
 * specific type of work, or if the queue will contain a heterogeneous collection of tasks.
 */
public class DsWorkQueue implements DsQueueInterface, Executor {
  /** Name of the Data queue. */
  public static final String DATA_IN_QNAME = "DATAI";

  /** Name of the Selectable Channel processing queue. */
  public static final String IO_WORK_QNAME = "IO_WORK";

  /** Name of the Incoming Request queue. */
  public static final String REQUEST_IN_QNAME = "REQUESTI";

  /** Name of the Outgoing Request queue. */
  public static final String REQUEST_OUT_QNAME = "REQUESTO";

  /** Name of the logger queue. */
  public static final String LOGGER_QNAME = "LOGGER";

  /** Name of the dialog callback queue. */
  public static final String DIALOG_CALLBACK_QNAME = "DIALOG_CALLBACK";

  /** Name of the offer/answer callback queue. */
  // CAFFEINE 2.0 - add OA Manager support
  public static final String OFFER_ANSWER_CALLBACK_QNAME = "OFFER_ANSWER_CALLBACK";

  /** Name of the subscribe dialog callback queue. */
  public static final String SUB_DIALOG_CALLBACK_QNAME = "SUB_DIALOG_CALLBACK";

  /** Name of the client callback queue. */
  public static final String CLIENT_CALLBACK_QNAME = "CT_CALLBACK";

  /** Name of the server callback queue. */
  public static final String SERVER_CALLBACK_QNAME = "ST_CALLBACK";

  /** Name of the Timer queue. */
  public static final String TIMER_QNAME = "TIMER";

  /** discard from the front of the queue to make room for new work. */
  public static final short DISCARD_OLDEST = 0;

  /** if the queue is full, return the submitted work. */
  public static final short DISCARD_NEWEST = 1;

  /** if the queue is full, it grows, drop nothing. */
  public static final short GROW_WITHOUT_BOUND = 2;

  /** a central repository for all queues. */
  private static HashMap queueTable = new HashMap();

  /** the default maximum length of a queue. */
  private static final int DEFAULT_MAX_QUEUE_SIZE = 2000;

  /** the default number of threads to process a queue. */
  private static final int DEFAULT_THREAD_COUNT = 4;

  /** if queue grows this is factor to grow by - must be > 1.0; 1.2 = 20%. */
  private static final float GROWTH_FACTOR = 1.2f;

  /** at what % fullness do we start issuing warnings?. */
  public static final int THRESHOLD = 80;

  private static HashMap<String, Double> queueAverageSize;

  public static final String REQUESTIQ = "REQUESTI";
  public static final String DATAIQ = "DATAI";
  public static final String ST_CALLBACKQ = "ST_CALLBACK";
  public static final String CT_CALLBACKQ = "CT_CALLBACK";
  public static final String XCLQ = "Xclqueue";
  public static final String RADIUSWORKQ = "RADIUSWorkQueue";
  public static final String TIMERQ = "TIMER";
  public static final String IO_WORKQ = "IO_WORK";

  static {
    queueAverageSize = new HashMap(8);

    queueAverageSize.put(DATAIQ, 0.0);
    queueAverageSize.put(REQUESTIQ, 0.0);
    queueAverageSize.put(ST_CALLBACKQ, 0.0);
    queueAverageSize.put(CT_CALLBACKQ, 0.0);
    queueAverageSize.put(RADIUSWORKQ, 0.0);
    queueAverageSize.put(TIMERQ, 0.0);
    queueAverageSize.put(XCLQ, 0.0);
    queueAverageSize.put(IO_WORKQ, 0.0);
  }

  /** the implementation of the queue. */
  private DsUnitOfWork[] Q;

  /** a list of threads that are available to process this object. */
  private ArrayList threads;

  /** the maximum number of threads available to process this object. */
  private int maxThreads;

  private int maxThreadsTmp; // hold state after queue is destroyed

  /** number of threads actually doing work. */
  private int countOfThreadsDoingWork;

  /** head of the circular queue. */
  private int firstEntry;

  /** tail of the circular queue. */
  private int lastEntry;

  /** the discard policy. */
  private short discardPolicy = DISCARD_NEWEST;

  /** this monitors the thread pool size. */
  private Qmonitor mon;

  /** the name to report on. */
  private String queueName;

  private Boolean isGenThreadDumpEnabled = true;

  /** factory to create processor threads. */
  private DsQProcessorFactory factory;

  /** at what % fullness do we start issuing warnings?. */
  private int warningThreshold = THRESHOLD;

  /**
   * % Threshold delta value from warningThreshold which indicates Queue is OK. A
   * raiseQueueThresholdOk notification is sent when Queue length falls beyond this value i.e
   * (warningThreshold-lowerThresholdDelta)%
   */
  private final int LOWER_THRESHOLD_DELTA = 10;

  /** what # does that correspond to?. */
  private int thresholdCount;

  /**
   * A value 10% lower than the threshold which indicates Queue is fine and QueueThresholdOk
   * notification is sent when queue length falls below this value
   */
  private int lowerThresholdCount;

  /** by default the sampling interval is 1 second. */
  private int samplingInterval = 1;

  /** for computation of avg elements on queue. */
  private long totalElementsOnQueue = 0;

  /** for computation of avg elements on queue. */
  private int numberOfSamplingPeriods = 1;

  /** have we attempted to exceed the queue size?. */
  private boolean queueSizeExceeded = false;

  /** have CP exceeded the queueThreshold * */
  private boolean queueThresholdExceeded = false;

  /** for shrinking the Q. */
  private int newMaxQSize = -1;

  // PRE CAFFEINE 2.0 ds remove hitThreadhold

  /**
   * -1 if we have not touched the max bounds <br>
   * 0 if we first time hit the max bounds <br>
   * &GT 0 if we touched the max bounds more than once consecutively.
   */
  private short hitBounds = -1;

  /** The max queue size. */
  private int queueMaxSize = 0;

  private int dropped = 0;

  /** Count of messages dropped between lastOverflow and overflow clear * */
  private int droppedSinceLastOverflow = 0;

  private final int QUEUE_SIZE_ARRAY_LENGTH =
      DsConfigManager.getProperty(
          DsConfigManager.PROP_QUEUE_STAT_INTERVAL,
          DsConfigManager.PROP_QUEUE_STAT_INTERVAL_DEFAULT);

  public int getDroppedSinceLastOverflow() {
    return droppedSinceLastOverflow;
  }

  public int getDropped() {
    return dropped;
  }

  /**
   * Create a DsWorkQueue.
   *
   * @param name The external name of the work queue, used when turning trace on
   * @param maxQ The maximum number of entries on the queue. A valid value is greater than zero; if
   *     an invalid value is specified, the value of 2000 is used.
   * @param maxThreadCount The maximum number of threads
   */
  public DsWorkQueue(String name, int maxQ, int maxThreadCount) {
    queueMaxSize = maxQ = validateMaxQ(maxQ);

    Q = new DsUnitOfWork[maxQ]; // allocate/initialize the container that will hold the work
    threads = new ArrayList(); // allocate the thread pool
    setMaxThreadCount(maxThreads = maxThreadCount); // establish the maximum number of threads
    factory = new DsQProcessorFactory(); // default to creating DsQProcessor threads
    firstEntry = 0;
    lastEntry = 0;
    countOfThreadsDoingWork = 0;
    queueName = name;
    queueTable.put(queueName, this); // register this queue
    mon = new Qmonitor();
    // GOGONG 04.12.06 CSCsd90062 - create a monitor daemon thread
    //     so that the parent thread can be shut down without waiting for this non-daemon thread.
    mon.setDaemon(true);
    mon.start();
    setThresholdSize(warningThreshold);
  }

  public static void setQueueAverageSize(String queueName, double avg) {
    queueAverageSize.put(queueName, avg);
  }

  public static double getQueueAverageSize(String queueName) {
    return queueAverageSize.get(queueName);
  }

  /**
   * Set the factory that determines what kind of DsQProcessor gets created.
   *
   * @param f the factory that creates DsQProcessors (or its descendents)
   */
  public void setFactory(DsQProcessorFactory f) {
    factory = f;
  }

  /**
   * Destroy all resources associated with this work queue. <b>THE QUEUE SHOULD NOT BE USED AFTER
   * THIS CALL IS MADE.</b>
   */
  public synchronized void destroy() {
    maxThreadsTmp = maxThreads;
    unvalidatedSetMaxThreadCount(0);
    queueTable.remove(queueName);
  }

  /** Reinitialize the queue. */
  public synchronized void init() {
    Q = new DsUnitOfWork[queueMaxSize]; // allocate/initialize the container that will hold the work
    threads = new ArrayList(); // allocate the thread pool
    firstEntry = 0;
    lastEntry = 0;
    countOfThreadsDoingWork = 0;
    queueTable.put(queueName, this); // register this queue
    mon = new Qmonitor();
    // GOGONG 04.12.06 CSCsd90062 - create a monitor daemon thread
    //     so that the parent thread can be shut down without waiting for this non-daemon thread.
    mon.setDaemon(true);
    mon.start();
    setThresholdSize(warningThreshold);
    setMaxThreadCount(maxThreadsTmp);
  }

  /**
   * Set the maximum thread count (possibly making the max threads smaller).
   *
   * @param count the new maximum
   */
  public synchronized void setMaxThreadCount(int count) {
    count = validateThreadCount(count);
    unvalidatedSetMaxThreadCount(count);
  }

  /**
   * Set the maximum thread count (possibly making the max threads smaller). Unlike
   * setMaxThreadCount(), the count is not validated, it is forced.
   *
   * @param count the new maximum
   */
  private void unvalidatedSetMaxThreadCount(int count) {
    maxThreads = count; // set the maximum number of threads for the pool

    synchronized (threads) {
      // are there too many threads now?
      if (threads.size() + countOfThreadsDoingWork > maxThreads) {
        // yes... are any of them sleeping?
        if (threads.size() > 0) {
          // yes... kill them in their sleep!

          // NOTE: this may only kill some of the necessary threads, while the others are out
          // working
          // they need to be killed when they become available

          int numberOfThreadsToKill =
              Math.min(threads.size(), threads.size() + countOfThreadsDoingWork - maxThreads);
          for (int i = 0; i < numberOfThreadsToKill; i++) {
            DsQProcessor target = (DsQProcessor) threads.remove(threads.size() - 1);
            synchronized (target.threadLock) {
              target.markForDeath();
              target.threadLock.notifyAll(); // wake up and die
            }
          }
        }
      } else {
        // this is how many threads we could possibly create now
        int canMakeThisManyNewThreads = maxThreads - (threads.size() + countOfThreadsDoingWork);
        int shouldMakeThisManyThreads = Math.min(canMakeThisManyNewThreads, size());
        //              System.out.println(">>> # active threads: " + countOfThreadsDoingWork);
        //              System.out.println(">>> # available threads: " + threads.size());
        //              System.out.println(">>> # items on the queue: " + size());
        //              System.out.println(">>> Could make up to this many new threads: " +
        // canMakeThisManyNewThreads);
        //              System.out.println(">>> going to make this many new threads: " +
        // shouldMakeThisManyThreads);

        for (int i = 0; i < shouldMakeThisManyThreads; i++) {
          factory.makeProcessor(this).start(); // create a new processor thread and put it to work
          countOfThreadsDoingWork++;
        }
      }
    }
  }

  /**
   * Display/don't display the queue size at runtime.
   *
   * @param shouldMonitor true means display the queue, false means don't display
   */
  public void runMonitor(boolean shouldMonitor) {
    mon.monitor(shouldMonitor);
  }

  /**
   * Method to put a thread back on the list of available threads.
   *
   * @param thread the thread to be made available
   */
  public void available(DsQProcessor thread) {
    // make a thread appear on the free list

    boolean markForDeath = false;
    synchronized (threads) {
      countOfThreadsDoingWork--;
      if (threads.size() < maxThreads) // do we need this many threads?
      {
        threads.add(thread); // yes... store it
      } else // someone shrunk maxThreads, must kill this thread
      {
        markForDeath = true;
      }
    }

    if (markForDeath) {
      synchronized (thread.threadLock) {
        thread.markForDeath();
        thread.threadLock.notifyAll(); // wake up and die
      }
    }
  }

  /**
   * Get the current maximum number of threads we are willing to run to process this queue.
   *
   * @return the maximum number of threads we are willing to run to process this queue
   */
  public int getMaxThreads() {
    return maxThreads;
  }

  /**
   * Get the number of threads that are actively doing processing.
   *
   * @return the active thread count
   */
  public int getActiveThreadCount() {
    return countOfThreadsDoingWork;
  }

  /**
   * Is the thread quiescent?.
   *
   * @return true means there are no busy threads
   */
  public boolean isQuiescent() {
    return countOfThreadsDoingWork == 0;
  }

  /**
   * Turn queue tracing on/off.
   *
   * @param traceOn true means turn tracing on
   */
  public void monitor(boolean traceOn) {
    mon.monitor(traceOn);
  }

  /** Wake a thread up: there's work to be done!. */
  private synchronized void activate() {
    // let the thread pool know that there is work to be done
    //         System.out.println(">>> # threads sleeping: " + threads.size() + ", # working: " +
    // countOfThreadsDoingWork + ", and max threads: " + maxThreads);

    DsQProcessor last = null;
    synchronized (threads) {
      if (threads.size() > 0) // are any threads sleeping?
      {
        last = (DsQProcessor) threads.remove(threads.size() - 1);

        countOfThreadsDoingWork++;
      } else if (countOfThreadsDoingWork < maxThreads) // have we reached our maximum thread count?
      {
        // no... create another one
        factory.makeProcessor(this).start(); // create a new processor thread and put it to work

        countOfThreadsDoingWork++;
      }
    }

    if (last != null) // an existing thread from the list was used
    {
      synchronized (last.threadLock) {
        last.threadLock.notifyAll(); // yes... wake one thread up
      }
    }

    // otherwise the first thread to finish its work will go look for more work to do
  }

  /**
   * Class that monitors a the queue, accomplishing two things.
   *
   * <p>1. Once a second attempts to delete a single idle thread from the thread pool <br>
   * 2. Prints a (user-level) debugging message if the queue length is not zero <br>
   */
  class Qmonitor extends Thread {
    private boolean shouldWriteOutput;
    /** Array which holds the Queue size for each sampling period * */
    private int[] queueSizeArray = new int[QUEUE_SIZE_ARRAY_LENGTH];

    private int queueSizeArrayindex = 0;

    Qmonitor() {
      shouldWriteOutput = false;
    }

    public void monitor(boolean trace) {
      shouldWriteOutput = trace;
    }

    public void run() {
      int sum;
      double avg;
      while (true) {
        try {
          sleep(samplingInterval * 1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        // update the statistics
        int size = size();
        queueSizeArray[queueSizeArrayindex++] = size;

        if (queueSizeArrayindex >= QUEUE_SIZE_ARRAY_LENGTH) {
          sum = 0;
          for (int temp : queueSizeArray) {
            sum += temp;
          }
          avg = (double) sum / QUEUE_SIZE_ARRAY_LENGTH;
          queueSizeArrayindex = 0;
          DsWorkQueue.setQueueAverageSize(queueName, avg);
        }
        // If we need to roll over.
        if (size > (Long.MAX_VALUE - totalElementsOnQueue)
            || Integer.MAX_VALUE == numberOfSamplingPeriods) {
          totalElementsOnQueue = getAverageSize();
          numberOfSamplingPeriods = 1;
        } else {
          totalElementsOnQueue += size;
          numberOfSamplingPeriods++;
        }
      }
    }
  }

  private void tryResize() {
    // Is there room for all current elements in new Q
    if (newMaxQSize > size()) {
      setMaxSize(newMaxQSize);
      newMaxQSize = -1;
    }
  }

  /**
   * Check to see if we have come back into "normal" territory (below the threshold level) after
   * having blown the max size.
   */
  void testThreshold() {
    // PRE CAFFEINE 2.0 ds changes the logic of measureing threshold
    if (size() >= thresholdCount) {
      //              System.out.println(">>> Queue threshold exceeded");
      DsConfigManager.raiseQueueThresholdExceeded(queueName);
      queueThresholdExceeded = true;
    } else {
      if (queueSizeExceeded || hitBounds >= 0) // remove this 'if' if we
      // always want to clear
      {
        DsConfigManager.raiseQueueOkAgain(queueName);
        queueSizeExceeded = false;
        hitBounds = -1;
        droppedSinceLastOverflow = 0;
      }

      if (queueThresholdExceeded && size() <= lowerThresholdCount) {
        queueThresholdExceeded = false;
        DsConfigManager.raiseQueueThresholdOk(queueName);
      }
    }
  }

  /**
   * Set the policy of what to discard if the queue should become full.
   *
   * @param policy one of DISCARD_OLDEST, DISCARD_NEWEST, GROW_WITHOUT_BOUND
   */
  public void setDiscardPolicy(short policy) {
    discardPolicy = policy;
  }

  /**
   * Get the discard policy.
   *
   * @return an integer corresponding to the discard policy (for example DISCARD_OLDEST or
   *     DISCARD_NEWEST)
   */
  public short getDiscardPolicy() {
    return discardPolicy;
  }

  /**
   * Returns the maximum size the queue can grow to.
   *
   * @return the maximum size the queue can grow to
   */
  public synchronized int getMaxSize() {
    // --        return Q.length;
    return queueMaxSize;
  }

  /**
   * Reset the maximum length of the queue.
   *
   * @param maxSize the new max size
   */
  public synchronized void setMaxSize(int maxSize) {
    queueMaxSize = maxSize = validateMaxQ(maxSize);

    newMaxQSize = -1; // set if we need to shrink the Q

    if (maxSize > Q.length) // Q needs to grow
    {
      DsUnitOfWork newQ[] = new DsUnitOfWork[maxSize];

      // OK, since we have a circular queue, we cannot just do an array copy when
      // we resize.
      moveQ(newQ);
    } else if (maxSize < Q.length) // Q needs to shrink
    {
      // never drop items when shrinking

      if (!(maxSize < size())) // there is room for all elements in the new Q
      {
        // allocate a new queue
        DsUnitOfWork[] newQ = new DsUnitOfWork[maxSize];
        moveQ(newQ);
      } else // - no room - set flag - resize later when they will all fit
      {
        newMaxQSize = maxSize;
        queueSizeExceeded = true; // do not accept new items

        DsConfigManager.raiseQueueMaxSizeExceeded(queueName);
      }
    }
    // else size did not change - do nothing
  }

  /**
   * Grows the queue without affecting the max size and threshold parameters.
   *
   * @param size the new size after growing
   */
  private void grow(int size) {
    DsUnitOfWork newQ[] = new DsUnitOfWork[size];
    moveQ(newQ);
    newMaxQSize = queueMaxSize;
  }

  /**
   * Moves the old Q to the newQ provided. It is the callers responsibility to ensure that it fits.
   * Resets the firstEntry and lastEntry pointers.
   *
   * @param newQ the new work queue - big enough to hold all elements from the old queue
   */
  private void moveQ(DsUnitOfWork[] newQ) {
    if (size() == 0) // no elements to remove
    {
      Q = newQ;
      firstEntry = 0;
      lastEntry = 0;

      setThresholdSize(warningThreshold);

      return;
    }

    if (firstEntry < lastEntry) // does not wrap
    {
      // trivial case
      System.arraycopy(Q, firstEntry, newQ, 0, lastEntry - firstEntry);
      lastEntry = lastEntry - firstEntry;
    } else // wraps
    {
      // Copy from the firstEntry to the end of the array and then append from 0
      // to the lastEntry.
      System.arraycopy(Q, firstEntry, newQ, 0, Q.length - firstEntry);
      System.arraycopy(Q, 0, newQ, Q.length - firstEntry, lastEntry + 1);
      lastEntry = (Q.length - firstEntry) + lastEntry; // + 1;
    }
    Q = newQ;
    firstEntry = 0;

    // did we move in or out of threshold area by resizing?
    setThresholdSize(warningThreshold);
    testThreshold();
  }

  /**
   * Returns the name of the queue.
   *
   * @return the name of the queue
   */
  public String getName() {
    return queueName;
  }

  // DG-TCP
  public void execute(Runnable command) {
    // it better be a unit of work
    nqueue((DsUnitOfWork) command);
  }

  /**
   * Enqueue a unit of work.
   *
   * @param w the work to enqueue
   * @return null if successful. Otherwise, if the discard policy is set to DISCARD_NEWEST, returns
   *     the the unit of work passed in, otherwise if the discard policy is set to DISCARD_OLDEST,
   *     returns the unit of work at the font of the queue.
   */
  public synchronized DsUnitOfWork nqueue(DsUnitOfWork w) {
    // don't put null work on queue
    if (w == null) return null;

    DsUnitOfWork overflow = null;
    int newLastEntry = (lastEntry + 1) % Q.length;

    boolean overflowCountedFirsttime = false;

    // did this request exceed the queue size (just became full)
    if (newLastEntry == firstEntry && !queueSizeExceeded) {
      if (discardPolicy == GROW_WITHOUT_BOUND) {
        if (hitBounds == 0) {
          DsConfigManager.raiseQueueMaxSizeExceeded(queueName);
        }
        // --                setMaxSize((int)((Q.length * GROWTH_FACTOR) + 1));
        grow((int) ((Q.length * GROWTH_FACTOR) + 1));
        queueSizeExceeded = false;
        hitBounds++;
        newLastEntry = (lastEntry + 1) % Q.length;
      } else {
        queueSizeExceeded = true;
        // increasing the dropped count to 1 for the first time or else
        // Alarms will have dropped count as zero
        // as we are updating droppedSinceLastOverflow after the
        // raiseQueueMaxSizeExceeded
        if (droppedSinceLastOverflow == 0) {
          droppedSinceLastOverflow = 1;
          overflowCountedFirsttime = true;
        }
        DsConfigManager.raiseQueueMaxSizeExceeded(queueName);
      }
    }

    testThreshold();
    if (queueSizeExceeded) {
      overflow = dropWork(w);
      if (discardPolicy == DISCARD_NEWEST) {
        if (overflow != null) {
          dropped++;
          if (!overflowCountedFirsttime) {
            droppedSinceLastOverflow++;
          }

          if (DsLog4j.threadCat.isEnabled(Level.WARN)) {
            DsLog4j.threadCat.log(Level.WARN, queueName + ": Queue Overflow - UOW dropped.");
          }
        }
        return overflow;
      }
    }

    Q[lastEntry] = w; // add the object to the queue
    lastEntry = newLastEntry;

    activate(); // tell the thread pool about it

    if (overflow != null) {
      dropped++;
      if (!overflowCountedFirsttime) {
        droppedSinceLastOverflow++;
      }

      if (DsLog4j.threadCat.isEnabled(Level.WARN)) {
        DsLog4j.threadCat.log(Level.WARN, queueName + ": Queue Overflow - UOW dropped.");
      }
    }

    return overflow;
  }

  private final DsUnitOfWork dropWork(DsUnitOfWork w) {
    if (discardPolicy == DISCARD_OLDEST) {
      w = Q[firstEntry];
      Q[firstEntry] = null;
      firstEntry = (firstEntry + 1) % Q.length;
    }
    return w;
  }

  //  private DsUnitOfWork redTest(DsUnitOfWork w, double m_averageLen,
  //                                         double queueMaxSize, double thresholdCount )
  //     {
  //         if ((m_averageLen > thresholdCount) && (m_averageLen < queueMaxSize))
  //         {
  //             ++m_REDcount;
  //             double REDp = m_REDMaxP * (m_averageLen - thresholdCount) / (queueMaxSize -
  // thresholdCount);
  //             REDp = REDp / (1 - m_REDpassCount * REDp);
  //             if (m_REDcount > m_rand.nextDouble() / REDp)
  //             {
  //                 return  dropWork(w);
  //             }
  //             else
  //             {
  //                 ++m_REDpassCount;
  //             }
  //         }
  //         else if (m_averageLen >= queueMaxSize)
  //         {
  //             return dropWork(w);
  //         }
  //         else
  //         {
  //             m_REDcount = 0;
  //             m_REDpassCount = 0;
  //         }

  //         return null;

  //     }

  //     void printRed(int j)
  //     {
  //         //System.out.println("REDcount = " + m_REDcount + " REDpassCount " + m_REDpassCount + "
  // pct = " + (double) m_REDpassCount / (double) m_REDcount);
  //         System.out.println( j + "," + (double) (m_REDcount - m_REDpassCount) / (double)
  // m_REDcount);
  //     }

  //     void clearRed()
  //     {
  //         m_REDpassCount = 0;
  //         m_REDcount = 0;
  //     }

  //     public final static void main(String args[])
  //     {
  //         DsWorkQueue foo = new DsWorkQueue("foo", 100, 3);
  //         DsUnitOfWork w = new DsUnitOfWork()
  //             {
  //                 public void process()
  //                 {
  //                 }
  //                 public void abort(){}
  //             };
  //         for (int j = 10; j < 500; j += 10)
  //         {
  //             for (int i = 1000; i > 0; --i)
  //             {
  //                 foo.redTest(w,500 + j, 1000, 500);
  //             }
  //             foo.printRed(j);
  //             foo.clearRed();
  //         }
  //     }

  /**
   * Returns the next element on the queue to be processed, or null if there is no more work to be
   * done.
   *
   * @return the next element on the queue to be processed, or null if there is no more work to be
   *     done
   */
  public synchronized DsUnitOfWork dqueue() {
    DsUnitOfWork work;

    if (firstEntry == lastEntry) {
      work = null;
      testThreshold();
    } else {
      work = Q[firstEntry];
      Q[firstEntry] = null;
      firstEntry = (firstEntry + 1) % Q.length;
      testThreshold();
      tryResize();
    }

    return work;
  }

  /**
   * Return The number of elements currently on the queue.
   *
   * @return The number of elements currently on the queue
   */
  public synchronized int size() {
    // just in case anyone wants to know
    return lastEntry >= firstEntry ? lastEntry - firstEntry : Q.length - firstEntry + lastEntry;
  }

  /**
   * Accessor to the global queue hash map. This method should not be published to end-users. Its
   * purpose is to enable performance measurement programs to compile runtime statistics on the UA
   * stack's internal queues.
   *
   * @return the hash map containing all the queues
   */
  public static HashMap getQueueTable() {
    return queueTable;
  }

  /**
   * Returns the current size of the implementing Queue.
   *
   * @return The number of elements currently on the queue
   */
  public int getSize() {
    return size();
  }

  /**
   * Returns the threshold size of the implementing Queue. Its the size when reached, the user
   * should be notified so that he/she can either increase the queue maximum size limit or should be
   * ready to expect queue element drop offs once the queue maximum size is reached.
   *
   * @return the threshold size of the implementing queue.
   */
  public synchronized int getThresholdSize() {
    return warningThreshold;
  }

  /**
   * Sets the threshold size of the implementing Queue. Its the size when reached, the user should
   * be notified so that he/she can either increase the queue maximum size limit or should be ready
   * to expect queue element drop offs once the queue maximum size is reached.
   *
   * @param size the threshold size of the implementing queue, expressed as a percentage. The legal
   *     values are 1 - 100 inclusive; any other value will be ignored, and the default value of 80
   *     will be used.
   */
  public synchronized void setThresholdSize(int size) {
    if (size < 20 || size > 100) {
      if (DsLog4j.threadCat.isEnabled(Level.WARN))
        DsLog4j.threadCat.log(
            Level.WARN, queueName + ": invalid threshold " + size + " being reset to " + THRESHOLD);
      size = THRESHOLD;
    }

    // the Q cannot fill entirely, since this is the same as being empty
    // so there must always be one empty slot - hence Q.length - 1
    // --        thresholdCount = (warningThreshold = size) * (Q.length - 1) / 100;
    int maxSize = queueMaxSize - 1;
    thresholdCount = (warningThreshold = size) * (maxSize) / 100;

    lowerThresholdCount = (size - LOWER_THRESHOLD_DELTA) * (maxSize) / 100;

    // make sure that the threshold can be passed in both directions - jsm
    if (thresholdCount < 1) {
      thresholdCount = 1;
    }
    // --        else if (thresholdCount >= Q.length - 1)
    else if (thresholdCount >= maxSize) {
      // --            thresholdCount = Q.length - 2;
      thresholdCount = maxSize - 1;
    }

    // has the queue has exceeded its threshold?
    testThreshold();
  }

  /**
   * Returns the maximum number of worker threads that can operate on the implementing Queue. The
   * active number of workers can be less than or equal to this number but will not exceed this
   * number.
   *
   * @return the maximum number of worker threads that can operate on the implementing Queue.
   */
  public int getMaxNoOfWorkers() {
    return getMaxThreads();
  }

  /**
   * Sets the maximum number of worker threads that can operate on the implementing Queue. The
   * active number of workers can be less than or equal to this number but will not exceed this
   * number.
   *
   * @param size the maximum number of worker threads that can operate on the implementing Queue.
   */
  public void setMaxNoOfWorkers(int size) {
    setMaxThreadCount(size);
  }

  /**
   * Returns the active number of workers in the implementing Queue.
   *
   * @return the active number of workers in the implementing Queue.
   */
  public int getActiveNoOfWorkers() {
    return getActiveThreadCount();
  }

  public boolean getGenerateThreadDump() {
    return isGenThreadDumpEnabled;
  }

  public void setGenerateThreadDump(boolean generateThreadDump) {
    isGenThreadDumpEnabled = generateThreadDump;
  }

  /**
   * Its the time interval(in seconds) used to determine the average number of elements in the queue
   * within this time interval.
   *
   * @return the time interval used to determine the average number of elements in the queue.
   */
  public int getAverageWindowTime() {
    return samplingInterval;
  }

  /**
   * Its the time interval(in seconds) used to determine the average number of elements in the queue
   * with in this time interval.
   *
   * @param secs the time interval used to determine the average number of elements in the queue.
   */
  public void setAverageWindowTime(int secs) {
    samplingInterval = secs;
    totalElementsOnQueue = size();
    numberOfSamplingPeriods = 1;
  }

  /**
   * Its the average number of elements that are present in the implementing Queue with in the time
   * interval specified by the average window time.
   *
   * @return the average number of elements that are present in the implementing Queue with in the
   *     time interval specified by the average window time.
   * @see DsQueueInterface#getAverageWindowTime()
   */
  public int getAverageSize() {
    return (int) (totalElementsOnQueue / numberOfSamplingPeriods);
  }

  private int validateThreadCount(int maxThreadCount) {
    if (maxThreadCount < 1) {
      if (DsLog4j.threadCat.isEnabled(Level.WARN))
        DsLog4j.threadCat.log(
            Level.WARN,
            queueName
                + ": invalid maximum thread count "
                + maxThreadCount
                + " being reset to "
                + DEFAULT_THREAD_COUNT);
      maxThreadCount = DEFAULT_THREAD_COUNT;
    }

    return maxThreadCount;
  }

  private int validateMaxQ(int maxQ) {
    if (maxQ < 3) {
      if (DsLog4j.threadCat.isEnabled(Level.WARN))
        DsLog4j.threadCat.log(
            Level.WARN,
            queueName
                + ": invalid maximum queue size "
                + maxQ
                + " being reset to "
                + DEFAULT_MAX_QUEUE_SIZE);
      maxQ = DEFAULT_MAX_QUEUE_SIZE;
    }

    return maxQ;
  }

  //      public static void main(String args []) throws InterruptedException
  //      {
  //          DsWorkQueue foo = new DsWorkQueue("foo", 100, 3);
  //          for (int i = 0; i < 2000; i++)
  //          {
  //              foo.nqueue(new DsUnitOfWork()
  //              {
  //                  public void process()
  //                  {
  //                  }
  //                  public void abort(){}
  //              });
  //          }
  //          for (int i = 0; i < 200; i++)
  //          {
  //              System.out.println(">>> Queue length: " + foo.size());
  //          }
  //          foo.destroy();
  //          foo = null;
  //      }

  // public static void main(String args []) throws InterruptedException
  //        {
  //            DsWorkQueue foo = new DsWorkQueue("foo", 100, 1);
  //            for (int i = 0; i < 100; i++)
  //            {
  //                foo.nqueue(new DsUnitOfWork()
  //                 {
  //                     public void process()
  //                     {
  //                         try{
  //                             Thread.sleep(1000000);
  //                         }catch (Exception e){}
  //                     }
  //                     public void abort(){}
  //                    });

  //                System.out.println(">>> Queue length: " + foo.size());
  //                Thread.sleep(300);
  //            }
  //            for (int i = 0; i < 11; i++)
  //            {
  //                foo.nqueue(new DsUnitOfWork()
  //                 {
  //                     public void process()
  //                     {
  //                         try{
  //                             Thread.sleep(1000000);
  //                         }catch (Exception e){}
  //                     }
  //                     public void abort(){}
  //                    });
  //            }
  //        }
}
