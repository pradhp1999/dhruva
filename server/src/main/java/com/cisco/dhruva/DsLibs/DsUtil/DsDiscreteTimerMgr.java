// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import gnu.trove.TLongObjectHashMap;

/**
 * Manages collection of DsDiscreteTimers. These timers are created on demand, and die after a
 * configured amount of time. This class allows you to treat the discrete timers as a generic heap
 * of timers. However, users are cautioned not to use large amounts of different timers, as each
 * discrete timer value will run in its own thread. If you need to do this, it is recommended that
 * you use DsTimer for this purpose.
 */
public final class DsDiscreteTimerMgr {
  /** Time before timer threads die */
  private static long WAIT_TO_DIE;

  /** A lock for synchronous access to the map of timers */
  private static Object m_mapLock = new Object();
  /** The map of timers */
  private static TLongObjectHashMap m_timerMap = new TLongObjectHashMap(31);

  static {
    WAIT_TO_DIE =
        DsConfigManager.getProperty(
            DsConfigManager.PROP_TIMER_TIMEOUT, DsConfigManager.PROP_TIMER_TIMEOUT_DEFAULT);
  }

  /** Static class - do not allow construction */
  private DsDiscreteTimerMgr() {}

  /**
   * Schedule an event to execute in <code>millis</code> ms. This event will be put on the timer
   * queue, and execute as a unit of work in a thread separate from the timer thread. This is useful
   * if you suspect that your task will take more than a trivial amount of time to execute. It is
   * therefore safer than <code>scheduleNoQ</code>, but has additional overhead associated with it.
   *
   * @param millis number of milliseconds before the event is allowed to run
   * @param event the event to run
   * @param arg an argument to pass the event when it is run, <code>null</code> is OK
   * @return the created DsDiscreteTimerTask that will run this scheduled event
   */
  public static DsDiscreteTimerTask schedule(long millis, DsEvent event, Object arg) {
    DsDiscreteTimer timer = (DsDiscreteTimer) m_timerMap.get(millis);

    if (timer == null) {
      timer = addTimer(millis);
    }

    return timer.schedule(event, arg);
  }

  /**
   * Schedule an event to execute in <code>millis</code> ms. This event will be run in the timer
   * thread itself. Task execution times must be short or it will postpone the timers behind it,
   * since the next timer cannot fire until this task completes. This is faster than <code>schedule
   * </code>.
   *
   * @param millis number of milliseconds before the event is allowed to run
   * @param event the event to run
   * @param arg an argument to pass the event when it is run, <code>null</code> is OK
   * @return the created DsDiscreteTimerTask that will run this scheduled event
   */
  public static DsDiscreteTimerTask scheduleNoQ(long millis, DsEvent event, Object arg) {
    DsDiscreteTimer timer = (DsDiscreteTimer) m_timerMap.get(millis);

    if (timer == null) {
      timer = addTimer(millis);
    }

    return timer.scheduleNoQ(event, arg);
  }

  /**
   * Removes a timer from the manager. This does not cancel the timer, it only removes the reference
   * from the internal structure.
   *
   * @param millis number of milliseconds of the time that you wish to remove
   */
  public static void removeTimer(long millis) {
    synchronized (m_mapLock) {
      m_timerMap.remove(millis);
    }
  }

  /**
   * Adds a timer to the manager. The timer is created and started automatically.
   *
   * @param millis number of milliseconds of the time that you wish to add
   * @return the timer that was created and added
   */
  private static DsDiscreteTimer addTimer(long millis) {
    synchronized (m_mapLock) {
      DsDiscreteTimer timer = (DsDiscreteTimer) m_timerMap.get(millis);

      if (timer == null) {
        timer = new DsDiscreteTimer(millis, WAIT_TO_DIE);
        m_timerMap.put(millis, timer);
      }

      return timer;
    }
  }

  /**
   * Returns the total number of tasks waiting to fire from all managed timers. This method is a
   * little expensive to call, since it must get all of the timers from the internal map, so it
   * should only be called for debug or occasionally.
   *
   * @return the total number of tasks waiting to fire from all managed timers.
   */
  public static int size() {
    int size = 0;
    Object[] timers = m_timerMap.getValues();

    for (int i = 0; i < timers.length; i++) {
      size += ((DsDiscreteTimer) timers[i]).size();
    }

    return size;
  }

  /**
   * Returns a String for debugging, all timers and their sizes are returned. This method is a
   * little expensive to call, since it must get all of the timers from the internal map, so it
   * should only be called for debug or occasionally.
   *
   * @return a debug string with timer values and sizes
   */
  public static String debugString() {
    StringBuffer sb = new StringBuffer(128);

    Object[] timers = m_timerMap.getValues();

    for (int i = 0; i < timers.length; i++) {
      sb.append(timers[i].toString());
      sb.append('\n');
    }

    if (timers.length == 0) {
      sb.append("No Discrete Timers\n");
    }

    return sb.toString();
  }
}
