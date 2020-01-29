// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import java.util.Timer;
import java.util.TimerTask;

/** Schedules events for execution at a later time. See also, DsDiscreteTimerMgr. */
public class DsTimer {
  private static final int MAX_TIMER_EVENTS = 2500; // this queue will grow if needed
  private static final int MAX_TIMER_THREADS = 8;

  // use to be private, shared with DsDiscreteTimer now - jsm
  static DsWorkQueue timerEventQueue;

  private static boolean inited;

  private static Timer timer;
  private static Timer timerNoQ;

  static {
    timerEventQueue = new DsWorkQueue(DsWorkQueue.TIMER_QNAME, MAX_TIMER_EVENTS, MAX_TIMER_THREADS);

    // Make sure that timer events never fall on the floor, since many of them
    // are used to clean up memory.  i.e. remove transactions.
    timerEventQueue.setDiscardPolicy(DsWorkQueue.GROW_WITHOUT_BOUND);

    DsConfigManager.registerQueue((DsQueueInterface) timerEventQueue);

    startTimers();

    inited = true;
  }

  private static void startTimers() {
    // GOGONG 04.12.06 CSCsd90062 - create a timer daemon thread
    //     so that the parent thread can be shut down without waiting for this non-daemon thread.
    timer = new Timer(true);
    timerNoQ = new Timer(true);
  }

  /** Support class for DsTimer encapsulating an event with a parameter. */
  static class DsNoArgTimerTask extends TimerTask implements DsUnitOfWork {
    private DsEvent event;
    boolean fired = false;

    /**
     * Create a timer event.
     *
     * @param e the event to run
     * @param a the argument to pass to the event
     */
    DsNoArgTimerTask(DsEvent e) {
      event = e;
    }

    /** What to do when the timer goes off. */
    public void run() {
      if (fired) process();
      fired = true;

      // when the timer goes off, enqueue this event to be processed so that
      // we can go off and service the next timer event
      try {
        timerEventQueue.nqueue(this);
      } catch (Throwable t) {
        DsLog4j.threadCat.warn("error trying to enqueue a timer unit of work", t);
      }
    }

    /** How to cancel a timer event. */
    public boolean cancel() {
      if (event != null) {
        event = null;
      }

      return super.cancel();
    }

    /**
     * A timer event is a kind of DsUnitOfWork. This is the mandatory process() method required by
     * the DsUnitOfWork interface.
     */
    public void process() {
      // here is where the timer event actually gets to do its work
      if (event != null) {
        event.run(null);
      }
    }

    /** How to abort this kind of timer event. */
    public void abort() {
      cancel();
    }
  }

  /** Support class for DsTimer encapsulating an event with a parameter. */
  static class DsTimerTaskNoQ extends TimerTask {
    private DsEvent event;
    private Object arg;

    /**
     * Create a timer event.
     *
     * @param e the event to run
     * @param a the argument to pass to the event
     */
    DsTimerTaskNoQ(DsEvent e, Object a) {
      event = e;
      arg = a;
    }

    /** What to do when the timer goes off. */
    public void run() {
      if (event != null) {
        try {
          event.run(arg);
        } catch (Throwable t) // make sure that this thread does not exit
        {
          DsLog4j.threadCat.warn("Error trying to run a timer event:", t);
        }
      }
    }

    /** How to cancel a timer event. */
    public boolean cancel() {
      if (event != null) {
        event = null;
      }

      arg = null;

      return super.cancel();
    }
  }

  /** Support class for DsTimer encapsulating an event with a parameter. */
  static class DsNoArgTimerTaskNoQ extends TimerTask {
    private DsEvent event;

    /**
     * Create a timer event.
     *
     * @param e the event to run
     * @param a the argument to pass to the event
     */
    DsNoArgTimerTaskNoQ(DsEvent e) {
      event = e;
    }

    /** What to do when the timer goes off. */
    public void run() {
      if (event != null) {
        try {
          event.run(null);
        } catch (Throwable t) // make sure that this thread does not exit
        {
          DsLog4j.threadCat.warn("Error trying to run a timer event:", t);
        }
      }
    }

    /** How to cancel a timer event. */
    public boolean cancel() {
      if (event != null) {
        event = null;
      }

      return super.cancel();
    }
  }

  /** Support class for DsTimer encapsulating an event with a parameter. */
  static class DsTimerTask extends TimerTask implements DsUnitOfWork {
    private DsEvent event;
    private Object arg;
    boolean fired = false;

    /**
     * Create a timer event.
     *
     * @param e the event to run
     * @param a the argument to pass to the event
     */
    DsTimerTask(DsEvent e, Object a) {
      event = e;
      arg = a;
    }

    /** What to do when the timer goes off. */
    public void run() {
      if (fired) process();
      fired = true;

      // when the timer goes off, enqueue this event to be processed so that
      // we can go off and service the next timer event
      try {
        timerEventQueue.nqueue(this);
      } catch (Throwable t) {
        DsLog4j.threadCat.warn("error trying to enqueue a timer unit of work", t);
      }
    }

    /** How to cancel a timer event. */
    public boolean cancel() {
      if (event != null) {
        event = null;
      }

      arg = null;
      return super.cancel();
    }

    /**
     * A timer event is a kind of DsUnitOfWork. This is the mandatory process() method required by
     * the DsUnitOfWork interface.
     */
    public void process() {
      // here is where the timer event actually gets to do its work
      if (event != null) {
        event.run(arg);
      }
    }

    /** How to abort this kind of timer event. */
    public void abort() {
      cancel();
    }
  }

  /**
   * Schedule an event.
   *
   * @param millis number of milliseconds before the event is allowed to run
   * @param event the event to run
   * @param arg an argument to pass the event when it is run
   * @return the created timer task that will run this scheduled event.
   */
  public static TimerTask schedule(long millis, DsEvent event, Object arg) {
    TimerTask timerTask;

    // if (timer == null)
    // {
    // timer = new Timer();
    // }

    if (arg == null) {
      timerTask = new DsNoArgTimerTask(event);
    } else {
      timerTask = new DsTimerTask(event, arg);
    }

    timer.schedule(timerTask, millis);

    return timerTask;
  }

  /**
   * Schedule an event, without putting it on a queue to run it. This event will run in the actual
   * timer thread. It MUST be a very short task, or the rest of the tasks will back up behind it.
   *
   * @param millis number of milliseconds before the event is allowed to run
   * @param event the event to run
   * @param arg an argument to pass the event when it is run
   * @return the created timer task that will run this scheduled event.
   */
  public static TimerTask scheduleNoQ(long millis, DsEvent event, Object arg) {
    TimerTask timerTask;

    // if (timerNoQ == null)
    // {
    // timerNoQ = new Timer();
    // }

    if (arg == null) {
      timerTask = new DsNoArgTimerTaskNoQ(event);
    } else {
      timerTask = new DsTimerTaskNoQ(event, arg);
    }

    timerNoQ.schedule(timerTask, millis);

    return timerTask;
  }

  /**
   * Returns the number of timer events whose timers have not yet gone off.
   *
   * @return the number of timer events whose timers have not yet gone off
   */
  // qfang - 07.05.06 - CSCse63190 switch to JDK's Timer implementation
  /*
  public static int size()
  {
      return
      (
          timer.numTimers() +
          timerNoQ.numTimers()
      );
  }
  */

  /** Stop internal timer and destroy the work queue. */
  public static void stop() {
    if (timer != null) {
      timer.cancel();
      timer = null;
    }

    if (timerNoQ != null) {
      timerNoQ.cancel();
      timerNoQ = null;
    }

    timerEventQueue.destroy();
    inited = false;
  }

  /** Initialize the timer class. */
  public static void init() {
    if (!inited) {
      inited = true;
      timerEventQueue.init();
      startTimers();
    }
  }

  //     static class hiccup implements DsEvent
  //     {
  //         public void run(Object string)
  //         {
  //             System.out.println((String)string);
  //         }
  //     }
  //
  //     public static void main(String[] args)
  //     {
  //         try
  //         {
  //             DsTimer.schedule(500L, new hiccup(), (Object)"Hic!");
  //             Thread.currentThread().sleep(250);
  //             DsTimer.schedule(500L, new hiccup(), (Object)"Hic!");
  //             Thread.currentThread().sleep(2250);
  //
  //             System.exit(0);
  //         }
  //         catch (Exception e)
  //         {
  //         }
  //     }
}
