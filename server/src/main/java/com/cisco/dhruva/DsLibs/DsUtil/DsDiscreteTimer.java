// Copyright (c) 2005-2011 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import gnu.trove.TLinkedList;
import org.apache.logging.log4j.Level;

/**
 * A timer for a specific time period. All tasks that are put on this timer are executed after the
 * same delay. This allows for optimal insertion time and optimal retrieval time for tasks. By using
 * the DsDiscreteTimerMgr class, it looks like a generic timer class, however, users must use
 * caution. The should not be many different values of discrete timers, since these will each create
 * their own thread.
 */
public final class DsDiscreteTimer {
  /** The internal timer thread. */
  private DsDiscreteTimerThread m_timerThread;

  /** The discrete delay for each task in the timer queue. */
  private long m_delay;

  /**
   * Constructs a new timer for delay ms. This queue will only die if it is explicitly cancelled by
   * calling <code>cancel()</code>.
   *
   * @param delay the single time, in ms, to wait before executing tasks for this timer
   */
  public DsDiscreteTimer(long delay) {
    this(delay, 0);
  }

  /**
   * Constructs a new timer for delay ms. This queue will die on its own if waitToDie ms elapse an
   * no tasks are scheduled. Set waitToDie to 0 if you do not want this timer to die automatically.
   *
   * @param delay the single time, in ms, to wait before executing tasks for this timer
   * @param waitToDie if greater than 0, the time in ms that this timer will exit after being idle
   *     for this long
   */
  public DsDiscreteTimer(long delay, long waitToDie) {
    m_delay = delay;

    m_timerThread = new DsDiscreteTimerThread(delay, waitToDie);
    m_timerThread.start();
  }

  /**
   * Schedule an event to execute in the configured ms. This event will be put on the timer queue,
   * and execute as a unit of work in a thread separate from the timer thread. This is useful if you
   * suspect that your task will take more than a trivial amount of time to execute. It is therefore
   * safer than <code>scheduleNoQ</code>, but has additional overhead associated with it.
   *
   * @param event the event to run
   * @param arg an argument to pass the event when it is run, <code>null</code> is OK
   * @return the created DsDiscreteTimerTask that will run this scheduled event
   */
  public DsDiscreteTimerTask schedule(DsEvent event, Object arg) {
    DsDiscreteTimerTask task;

    if (arg == null) {
      task = new DsDiscreteNoArgTimerTask(event, m_delay);
    } else {
      task = new DsDiscreteArgTimerTask(event, arg, m_delay);
    }

    m_timerThread.schedule(task);

    return task;
  }

  /**
   * Schedule an event to execute in the configured ms. This event will be run in the timer thread
   * itself. Task execution times must be short or it will postpone the timers behind it, since the
   * next timer cannot fire until this task completes. This is faster than <code>schedule</code>.
   *
   * @param event the event to run
   * @param arg an argument to pass the event when it is run, <code>null</code> is OK
   * @return the created DsDiscreteTimerTask that will run this scheduled event
   */
  public DsDiscreteTimerTask scheduleNoQ(DsEvent event, Object arg) {
    DsDiscreteTimerTask task;

    if (arg == null) {
      task = new DsDiscreteNoArgTimerTaskNoQ(event, m_delay);
    } else {
      task = new DsDiscreteTimerTaskNoQ(event, arg, m_delay);
    }

    m_timerThread.schedule(task);

    return task;
  }

  /**
   * Returns the number of task queued for this timer.
   *
   * @return the number of task queued for this timer
   */
  public int size() {
    return m_timerThread.size();
  }

  /**
   * Kills this timer thread. It will not die until all currently queued timers are fired. Also,
   * additional tasks may still be scheduled and they will fire. However, it is recommended that you
   * do not use a timer after calling cancel.
   */
  public void cancel() {
    m_timerThread.cancel();
  }

  /**
   * Returns a debug string for this timer with the delay and the number of waiting tasks.
   *
   * @return a debug string for this timer with the delay and the number of waiting tasks.
   */
  public String toString() {
    return ("DiscreteTimer " + m_delay + " ms\tSize = " + size());
  }
}

/**
 * A thread that checks for events on a list, and fire them. It ignores them if they were cancelled.
 */
final class DsDiscreteTimerThread extends DsThread {
  /** The queue of tasks to execute. */
  TLinkedList m_list = new TLinkedList();

  /** When true, this thread will die after the queue drains. */
  private boolean m_markForDeath = false;

  /** Duration of all timers for this timer queue. */
  private long m_ms;

  /** Duration of idle time before the thread exits due to inactivity. */
  private long m_waitToDie;

  /** The time that the queue of tasks first became empty. */
  private long m_beginIdleTime = System.currentTimeMillis();

  public DsDiscreteTimerThread(long ms, long waitToDie) {
    super("DsDiscreteTimer." + String.valueOf(ms));
    m_ms = ms;
    m_waitToDie = waitToDie;
  }

  public void run() {
    long fireTime;
    long curTime;
    DsDiscreteTimerTask task;

    while (!m_markForDeath || m_list.size() > 0) {
      try {
        // this should be safe w/o sync
        if (m_list.size() == 0) {
          if (m_waitToDie > 0) {
            if (System.currentTimeMillis() > (m_beginIdleTime + m_waitToDie)) {
              // we have waited too long for any tasks to be scheduled, time to stop waiting
              m_markForDeath = true;
              continue;
            }
          }

          // there is nothing to process and will not be until at least m_ms
          sleep(m_ms);

          // only fall through to the code below if there is something on the list
          continue;
        }

        synchronized (m_list) {
          // moved into the sync block since it may be part of a problem we are seeing
          task = (DsDiscreteTimerTask) m_list.getFirst();

          // get the first task that has not been cancelled
          if (task.isCancelled()) {
            while (task.isCancelled()) {
              // find the next task that has not been cancelled
              m_list.removeFirst();

              if (m_list.size() == 0) {
                // no more tasks, go to sleep
                task = null;
                break;
              }

              task = (DsDiscreteTimerTask) m_list.getFirst();
            }
            if (task == null) {
              continue;
            }
          }
        }

        fireTime = task.getFireTime();
        curTime = System.currentTimeMillis(); // possible optimization - fire multiple timers per
        // currentTimeMillis() call

        // just use the last time an event fired as the beginning of the idle time
        // when the size = 0 then it will be correct, and that is the only time we check it
        m_beginIdleTime = curTime;

        if (curTime >= fireTime) // time for this event
        {
          synchronized (m_list) {
            m_list.removeFirst();
          }
          task.run();
        } else // no more events until this one is ready
        {
          // Only fixed for when the clock is rolled back, not forward.
          // CSCsi95540: Caffeine timer problems when clock set back
          long sleepTime = fireTime - curTime;
          if (sleepTime > m_ms) {
            // The system clock was set back, we need to reset the fire times
            fixTimes();
            sleepTime = 1;
          }
          sleep(sleepTime);
        }
      } catch (Exception e) {
        DsLog4j.threadCat.warn("Exception firing timer", e);
      }
    }

    DsDiscreteTimerMgr.removeTimer(m_ms);
  }

  public void cancel() {
    m_markForDeath = true;
  }

  public int size() {
    return m_list.size();
  }

  public void schedule(DsDiscreteTimerTask task) {
    synchronized (m_list) {
      m_list.addLast(task);
    }
  }

  /**
   * Adjust the fire times by the amount that the clock was set back. This assumes that it is time
   * for the first element to fire, so that fire time should equal now. We use that delta to change
   * the remaining timers in the queue. We ensure that we do not change the firing times for timers
   * that were set after the system clock was changed.
   *
   * <p>Note that this is deisgned for when the clock is set BACK, not FORWARD. When the clock is
   * set forward, all timers will fire immediately in succession.
   */
  private void fixTimes() {
    synchronized (m_list) {
      if (m_list.size() == 0) {
        return;
      }

      // Assume that we woke up because it was time to fire the first element
      // and use now and the first timer fire time as an estimate for how much
      // the clock was changed.
      DsDiscreteTimerTask task = (DsDiscreteTimerTask) m_list.getFirst();
      long delta = System.currentTimeMillis() - task.getFireTime();

      if (DsLog4j.threadCat.isEnabled(Level.WARN)) {
        DsLog4j.threadCat.warn(
            "The system clock set back by "
                + delta
                + " ms.  Adjusting timers for the "
                + m_ms
                + " ms queue.");
      }

      // Unadjusted times
      long prevFireTime = task.getFireTime();
      long curFireTime;

      task.adjustFireTime(delta);
      while ((task = (DsDiscreteTimerTask) task.getNext()) != null) {
        curFireTime = task.getFireTime();

        if (prevFireTime > curFireTime) {
          // Stop adjusting, this timer was set using the updated clock
          break;
        }

        prevFireTime = curFireTime;

        task.adjustFireTime(delta);
      }
    }
  }
}

/** A lightweight timer task with no arguments, that goes on a queue to run. */
final class DsDiscreteNoArgTimerTask extends DsDiscreteTimerTask implements DsUnitOfWork {
  public DsDiscreteNoArgTimerTask(DsEvent event, long deltaTime) {
    super(event, deltaTime);
  }

  /** What to do when the timer goes off. */
  public void run() {
    // when the timer goes off, enqueue this event to be processed so that
    // we can go off and service the next timer event
    try {
      DsTimer.timerEventQueue.nqueue(this);
    } catch (Exception e) {
      DsLog4j.threadCat.warn("Exception trying to enqueue a timer unit of work:", e);
    }
  }

  /** How to cancel a timer event. */
  public boolean cancel() {
    // these are no real TimerTasks, so we do not need to tell anyone else
    m_event = null;

    // hack - just return true for now
    return true;
  }

  /**
   * A timer event is a kind of DsUnitOfWork. This is the mandatory process() method required by the
   * DsUnitOfWork interface.
   */
  public void process() {
    // here is where the timer event actually gets to do its work
    if (m_event != null) {
      m_event.run(null);
    }
  }

  /** How to abort this kind of timer event. */
  public void abort() {
    cancel();
  }
}

/** A lightweight timer task with an argument, but that does not get enqueued. */
final class DsDiscreteTimerTaskNoQ extends DsDiscreteTimerTask {
  private Object m_arg;

  public DsDiscreteTimerTaskNoQ(DsEvent event, Object arg, long deltaTime) {
    super(event, deltaTime);
    m_arg = arg;
  }

  /** What to do when the timer goes off. */
  public void run() {
    if (m_event != null) {
      try {
        m_event.run(m_arg);
      } catch (Exception e) // make sure that this thread does not exit
      {
        DsLog4j.threadCat.warn("Exception trying to run a timer event:", e);
      }
    }
  }

  /** How to cancel a timer event. */
  public boolean cancel() {
    m_event = null;
    m_arg = null;
    return true;
  }
}

/** A lightweight timer task without an argument, but that does not get enqueued. */
final class DsDiscreteNoArgTimerTaskNoQ extends DsDiscreteTimerTask {
  public DsDiscreteNoArgTimerTaskNoQ(DsEvent event, long deltaTime) {
    super(event, deltaTime);
  }

  /** What to do when the timer goes off. */
  public void run() {
    if (m_event != null) {
      try {
        m_event.run(null);
      } catch (Exception e) // make sure that this thread does not exit
      {
        DsLog4j.threadCat.warn("Exception trying to run a timer event:", e);
      }
    }
  }

  /** How to cancel a timer event. */
  public boolean cancel() {
    m_event = null;
    return true;
  }
}

/** A lightweight timer task with an argument, but that gets enqueued. */
final class DsDiscreteArgTimerTask extends DsDiscreteTimerTask implements DsUnitOfWork {
  private Object m_arg;

  public DsDiscreteArgTimerTask(DsEvent event, Object arg, long deltaTime) {
    super(event, deltaTime);
    m_arg = arg;
  }

  /** What to do when the timer goes off. */
  public void run() {
    // when the timer goes off, enqueue this event to be processed so that
    // we can go off and service the next timer event
    try {
      DsTimer.timerEventQueue.nqueue(this);
    } catch (Exception e) {
      DsLog4j.threadCat.warn("Exception trying to enqueue a timer unit of work:", e);
    }
  }

  /** How to cancel a timer event. */
  public boolean cancel() {
    // these are no real TimerTasks, so we do not need to tell anyone else
    m_event = null;
    return true;
  }

  /**
   * A timer event is a kind of DsUnitOfWork. This is the mandatory process() method required by the
   * DsUnitOfWork interface.
   */
  public void process() {
    // here is where the timer event actually gets to do its work
    if (m_event != null) {
      m_event.run(m_arg);
    }
  }

  /** How to abort this kind of timer event. */
  public void abort() {
    cancel();
  }
}
