// Copyright (c) 2005-2011 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

import gnu.trove.TLinkable;

/**
 * A class to replace TimerTask. It uses less memory and wraps a DsEvent, as the children of
 * TimerTask use to do. For use with DsDiscreteTimer and DsDiscreteTimerMgr. Users should not need
 * to create subclasses of this class directly, this is done automatically when the DsDiscreteTimer
 * class and hence, indirectly by the DsDiscreteTimerMgr class.
 */
public abstract class DsDiscreteTimerTask implements TLinkable {
  /** Next task. */
  private TLinkable m_next;
  /** Previous task. */
  private TLinkable m_previous;

  /** Absolute time to fire. */
  private long m_fireTime;

  /** The event associated with this task. */
  protected DsEvent m_event;

  /**
   * Constructs a new Timer Task that has an associated event to run in deltaTime ms.
   *
   * @param event the event to run
   * @param deltaTime the time in ms to wait before executing the event
   */
  public DsDiscreteTimerTask(DsEvent event, long deltaTime) {
    m_fireTime = System.currentTimeMillis() + deltaTime;
    m_event = event;
  }

  /** Method that is called when the timer fires. */
  public abstract void run();

  /**
   * Method to stop this task from executing.
   *
   * @return true if it prevents a task from running.
   */
  public abstract boolean cancel();

  /**
   * Returns the time that this task will execute.
   *
   * @return the time that this task will execute
   */
  public final long getFireTime() {
    return m_fireTime;
  }

  /**
   * Returns true if this task was cancelled.
   *
   * @return true if this task was cancelled.
   */
  public final boolean isCancelled() {
    return (m_event == null);
  }

  /**
   * Adjust the firing time based on a system clock change.
   *
   * @param delta the time, in millisecond, to add to the fire time, use a negative number to
   *     decrease the fire time
   */
  final void adjustFireTime(long delta) {
    m_fireTime += delta;
  }

  //////////////////////////////
  // TLinkable interface impl //
  //////////////////////////////

  public final TLinkable getNext() {
    return m_next;
  }

  public final TLinkable getPrevious() {
    return m_previous;
  }

  public final void setNext(TLinkable linkable) {
    m_next = linkable;
  }

  public final void setPrevious(TLinkable linkable) {
    m_previous = linkable;
  }
}
