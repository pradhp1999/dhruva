// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.
package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * This interface defines a contract that an implementing Queue needs to follow to let SNMP, CLI or
 * user code query its properties. The implementing Queue needs to register itself by invoking
 * DsConfigManager.registerQueue() static method and passing its reference as the method parameter.
 * The queue name should be unique with in the stack as its registered to the DsConfigManager class
 * with its unique name.
 *
 * @since SIP User Agent Java v5.0
 */
public interface DsQueueInterface {

  /**
   * If the queue is full, discard elements from the front of the queue to make room for new
   * incoming elements.
   */
  public static final short DISCARD_OLDEST = 0;

  /** If the queue is full, discard the new incoming element. */
  public static final short DISCARD_NEWEST = 1;

  /**
   * If the queue is full, it grows the size of the queue to make room for new incoming elements and
   * discards no element.
   */
  public static final short GROW_WITHOUT_BOUND = 2;

  /**
   * Returns the name of the implementing Queue.
   *
   * @return the name of the implementing Queue
   */
  public String getName();

  /**
   * Returns the discard policy of the implementing Queue. The discard policy can be either
   * DISCARD_NEWEST or DISCARD_OLDEST.
   *
   * @return the discard policy of the implementing Queue.
   */
  public short getDiscardPolicy();

  /**
   * Sets the discard policy of the implementing Queue. The discard policy can be either
   * DISCARD_NEWEST or DISCARD_OLDEST.
   *
   * @param policy the discard policy the implementing Queue will follow
   */
  public void setDiscardPolicy(short policy);

  /**
   * Returns the maximum size of the implementing Queue. Its the size beyond which the implementing
   * Queue will start dropping the queue elements.
   *
   * @return the maximum size of the implementing Queue.
   */
  public int getMaxSize();

  /**
   * Sets the maximum size of the implementing Queue. Its the size beyond which the implementing
   * Queue will start dropping the queue elements.
   *
   * @param size the max size
   */
  public void setMaxSize(int size);

  /**
   * Returns the current size of the implementing Queue.
   *
   * @return the current size of the implementing Queue.
   */
  public int getSize();

  public boolean getGenerateThreadDump();

  public void setGenerateThreadDump(boolean generateThreadDump);

  /**
   * Returns the threshold size of the implementing Queue. Its the size when reached, the user
   * should be notified so that he/she can either increase the queue maximum size limit or should be
   * ready to expect queue element drop offs once the queue maximum size is reached.
   *
   * @return the threshold size of the implementing queue.
   */
  public int getThresholdSize();

  /**
   * Sets the threshold value(in percents, relative to the queue maximum size) of the specified
   * Queue. Its the size when reached, the user should be notified so that he/she can either
   * increase the queue maximum size limit or should be ready to expect queue element drop offs once
   * the queue maximum size is reached.
   *
   * @param size the threshold size of the implementing queue.
   */
  public void setThresholdSize(int size);

  /**
   * Returns the maximum number of worker threads that can operate on the implementing Queue. The
   * active number of workers can be less than or equal to this number but will not exceed this
   * number.
   *
   * @return the maximum number of worker threads that can operate on the implementing Queue.
   */
  public int getMaxNoOfWorkers();

  /**
   * Sets the maximum number of worker threads that can operate on the implementing Queue. The
   * active number of workers can be less than or equal to this number but will not exceed this
   * number.
   *
   * @param size the maximum number of worker threads that can operate on the implementing Queue.
   */
  public void setMaxNoOfWorkers(int size);

  /**
   * Returns the active number of workers in the implementing Queue.
   *
   * @return the active number of workers in the implementing Queue.
   */
  public int getActiveNoOfWorkers();

  /**
   * Its the time interval(in seconds) used to determine the average number of elements in the queue
   * with in this time interval.
   *
   * @return the time interval used to determine the average number of elements in the queue.
   */
  public int getAverageWindowTime();

  /**
   * Its the time interval(in seconds) used to determine the average number of elements in the queue
   * with in this time interval.
   *
   * @param secs the time interval used to determine the average number of elements in the queue.
   */
  public void setAverageWindowTime(int secs);

  /**
   * Its the average number of elements that are present in the implementing Queue with in the time
   * interval specified by the average window time.
   *
   * @return the average number of elements that are present in the implementing Queue with in the
   *     time interval specified by the average window time.
   * @see DsQueueInterface#getAverageWindowTime()
   */
  public int getAverageSize();
}
