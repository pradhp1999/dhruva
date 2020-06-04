// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.common.executor.ExecutorService;
import com.cisco.dhruva.common.executor.ExecutorType;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Schedules events for execution at a later time. See also, DsDiscreteTimerMgr. */
public class DsTimer {

  private static final int MAX_TIMER_THREADS = 8;

  private static ScheduledExecutorService timer;
  private static String name = "Timer";

  static {
    timer = Executors.newSingleThreadScheduledExecutor();
  }

  public static void startTimers(ExecutorService executorService) {
    if (timer != null) {
      timer.shutdownNow();
    }
    executorService.startScheduledExecutorService(ExecutorType.SIP_TIMER, MAX_TIMER_THREADS);
    timer = executorService.getScheduledExecutorThreadPool(ExecutorType.SIP_TIMER);
  }

  /** Support class for DsTimer encapsulating an event with a parameter. */
  static class DsTimerTask extends ScheduledTaskDelay implements Runnable {

    private DsEvent event;
    private Object arg;

    /**
     * Create a timer event.
     *
     * @param e the event to run
     * @param a the argument to pass to the event
     */
    DsTimerTask(DsEvent e, Object a, long time, TimeUnit timeUnit) {
      super(time, timeUnit);
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
  }

  /** Support class for DsTimer encapsulating an event with a parameter. */
  static class DsNoArgTimerTask extends ScheduledTaskDelay implements Runnable {

    private DsEvent event;

    /**
     * Create a timer event.
     *
     * @param e the event to run
     */
    DsNoArgTimerTask(DsEvent e, long time, TimeUnit timeUnit) {
      super(time, timeUnit);
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
  }

  public static ScheduledFuture schedule(long millis, DsEvent event, Object arg) {
    Runnable task;
    if (arg == null) {
      task = new DsNoArgTimerTask(event, millis, TimeUnit.MILLISECONDS);
    } else {
      task = new DsTimerTask(event, arg, millis, TimeUnit.MILLISECONDS);
    }

    ScheduledFuture<?> scheduledTaskFuture = timer.schedule(task, millis, TimeUnit.MILLISECONDS);

    return scheduledTaskFuture;
  }
}
