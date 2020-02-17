// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

/**
 * Defines the contract between the used queues in the User Agent stack and the SNMP or user code
 * interface that provides for monitoring the queue behavior at run time. The SNMP or user code
 * interested in monitoring and get notified of queue events, need to implement this interface and
 * register itself with the <code>DsConfigManager</code>. This way it provides an opportunity for
 * the user for either to increase the queue max size or to be ready to expect loss of queue
 * elements, either the newest or the oldest element, depending on the queue discarding policy.
 * <code>DsConfigManager</code> allows only one registration at a time. Thus the queues can be
 * monitored either through the SNMP or through the user code.
 *
 * @since SIP User Agent Java v5.0
 */
public interface DsQueueAlarmInterface {
  /**
   * Raises an alarm notifying that the threshold size has exceeded for the queue specified by the
   * name <code>qName</code>.
   *
   * @param qName the name of the queue whose threshold size is exceeded.
   */
  public void raiseThresholdExceeded(String qName);

  /**
   * Raises an alarm notifying that the maximum size has exceeded for the queue specified by the
   * name <code>qName</code>.
   *
   * @param qName the name of the queue whose maximum size is exceeded.
   */
  public void raiseMaxSizeExceeded(String qName);

  /**
   * Raises a clear alarm notifying that the queue, specified by the name <code>qName</code>, is
   * ready again to accept new elements. This condition occurs when the queue size hits queue
   * maximum size limit and start dropping elements on any new addition either from the front or
   * tail, depending on the queue discard policy, and keep dropping elements on any new addition
   * untill the queue size drops down to the threshold size. And the moment the queue size drops
   * down to the threshold size, this QueueOkAgain signal is raised to notify the user that its safe
   * now to add new elements in the queue.
   *
   * @param qName the name of the queue whose threshold size is exceeded.
   */
  public void raiseQueueOkAgain(String qName);

  /**
   * Registers the queue, to be monitored through SNMP or user code, and adds it into the queue
   * table with the queue name as its key. Once the queue is registered, then its attributes can be
   * monitored through SNMP or user code.
   *
   * @param qName the name of the queue that needs to be registered and hence can be monitored
   *     through SNMP or user code.
   */
  public void registerQueue(String qName);

  /**
   * Removes the registered queue, that no longer needs to be monitored through SNMP or user code,
   * and removes it from the queue table . Once the queue is unregistered, then its attributes can
   * no longer be monitored through the SNMP or user code.
   *
   * @param qName the name of the queue that needs to be unregistered and hence no longer can be
   *     monitored through SNMP or user code.
   */
  public void unregisterQueue(String qName);

  /**
   * Raises a clear alarm notifying all the registered queue listeners that the queue, specified by
   * the name <code>qName</code> 's threshold has come down below lowerThreshold set. Lower
   * threshold is 10% lesser than the threshold set for the queue accept new elements. This
   * condition occurs when the queue size hits threshold limit And the moment the queue size drops
   * down to the lower threshold size
   *
   * @param qName the name of the queue
   */
  public default void raiseQueueThresholdOk(String qName) {}
}
