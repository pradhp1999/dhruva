/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	$RCSFile$
//
// MODULE:	servergroups
//
// $Id: ServerGlobalStateWrapper.java,v 1.14.2.1 2005/11/21 01:45:27 lthamman Exp $
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
// /////////////////////////////////////////////////////////////////
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.sip.DsPings.PingObject;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRetryAfterHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.UsageLimitInterface;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class is a wrapper around state information that needs to be common to all end points with
 * the same host, port, and transport-type tuple. The state information includes a retry after date
 * and the number of tries the end point referenced by this object has been unsucessfully used.
 */
public class ServerGlobalStateWrapper extends PingObject {
  private static final Trace Log = Trace.getTrace(ServerGlobalStateWrapper.class.getName());

  private static UsageLimitInterface usageLimitInterface = null;

  private HashSet listeners = null;
  private EndpointEvent clearUnreachable;
  private EndpointEvent clearOverloaded;
  private EndpointEvent unreachable;
  private EndpointEvent overloaded;
  private int numReferences = 1;
  private List parentServerGroupList = new LinkedList();

  private int activeUsageLimit = -1;
  private AtomicLong usageCount = new AtomicLong();
  private AtomicLong failureCount = new AtomicLong();

  /**
   * Sets the usage limit interface through which active usage checks can be done on the endpoint
   *
   * @param usageLimit Usage limit interface
   */
  public static void setUsageLimitInterface(UsageLimitInterface usageLimit) {
    usageLimitInterface = usageLimit;
  }

  /**
   * Creates a new ServerGlobalStateWrapper object with the given <code>EndPoint</code> and number
   * of tries.
   *
   * @param host host address of the PingObject
   * @param port port number of the PingObject
   * @param protocol the transport type of the PingObject is considered to have failed.
   */
  public ServerGlobalStateWrapper(
      DsByteString network,
      DsByteString host,
      String parentServerGroup,
      int port,
      Transport protocol,
      Boolean dnsServerGroup) {
    super(network, host, port, protocol, dnsServerGroup);
    resetTries();
    clearUnreachable = new EndpointEvent(this, EndpointEvent.CLEAR_UNREACHABLE, parentServerGroup);
    clearOverloaded = new EndpointEvent(this, EndpointEvent.CLEAR_OVERLOADED, parentServerGroup);
    unreachable = new EndpointEvent(this, EndpointEvent.UNREACHABLE, parentServerGroup);
    overloaded = new EndpointEvent(this, EndpointEvent.OVERLOADED, parentServerGroup);
    parentServerGroupList.add(parentServerGroup);
    if (Log.on && Log.isInfoEnabled()) Log.info("ServerGlobalStateWrapper created for " + this);
  }

  /** Decrements the number of tries. */
  public synchronized boolean decrementTries(String failureReason) {
    boolean b = super.decrementTries(failureReason);
    if (b) {
      if (!checkServerState()) {
        if (Log.on && Log.isInfoEnabled()) Log.info(this + " is UNAVAILABLE");
        notifyListeners(EndpointEvent.UNREACHABLE, failureReason);
      }
    }
    return b;
  }

  public synchronized void addParentServerGroupName(String parentServerGroup) {
    parentServerGroupList.add(parentServerGroup);
  }

  public boolean isCodeInFailoverCodeSet(int code) {
    return (FailoverResponseCode.getInstance()
        .isCodeInFailoverCodeSet(parentServerGroupList, code));
  }

  /** Sets the number of tries to 0 */
  public synchronized boolean zeroTries() {
    boolean b = super.zeroTries();
    // Send notification.
    if (b) notifyListeners(EndpointEvent.UNREACHABLE);
    return b;
  }

  /** Sets the number of tries to 0 */
  public synchronized boolean zeroTries(String failureReason) {
    boolean b = super.zeroTries();
    // Send notification.
    if (b) notifyListeners(EndpointEvent.UNREACHABLE, failureReason);
    return b;
  }

  /** Resets the number of tries to the maximum. */
  public synchronized void resetTries() {
    boolean wasUnreachable = !checkServerState();
    super.resetTries();
    if (wasUnreachable) {
      if (Log.on && Log.isInfoEnabled()) Log.info(this + " is AVAILABLE");
      notifyListeners(EndpointEvent.CLEAR_UNREACHABLE);
    }
  }

  protected void removeRetryAfter() {
    super.removeRetryAfter();
    notifyListeners(EndpointEvent.CLEAR_OVERLOADED);
  }

  public synchronized boolean setRetryAfter(DsSipRetryAfterHeader header) {
    boolean b = super.setRetryAfter(header);
    if (b) notifyListeners(EndpointEvent.OVERLOADED);
    return b;
  }

  /**
   * Checks to see if this server is available to route to.
   *
   * @return <code>true</code> if this server is available, <code>false</code> otherwise.
   */
  public boolean isServerAvailable() {
    boolean success = (checkServerState() && checkRetryAfter() && checkActiveUsageLimit());
    if (Log.on && Log.isInfoEnabled()) Log.info(this + "--->isServerAvailable(): " + success);
    return success;
  }

  private boolean checkActiveUsageLimit() {
    return (usageLimitInterface == null
        || activeUsageLimit == -1
        || usageLimitInterface.checkActiveUsageLimit(this, activeUsageLimit));
  }

  /**
   * ****************************************** Listener Notification Interface methods *
   * ******************************************
   */
  /**
   * returns "true" if this server is up else "false". does not include active usage limit check
   *
   * @return <code>true</code> if this server is up, <code>false</code> otherwise.
   */
  public synchronized boolean getStatus() {
    boolean success = (checkServerState() && checkRetryAfter());
    if (Log.on && Log.isInfoEnabled()) Log.info(this + "--->getStatus(): " + success);
    return success;
  }

  /**
   * ****************************************** Endpoint Event Listener methods *
   * ******************************************
   */

  /**
   * Adds an EndpointListener to this object
   *
   * @param e An EndpointListener
   * @see EndpointListener
   */
  public void addEndPointListener(EndpointListener e) {
    if (listeners == null) listeners = new HashSet();

    if (Log.on && Log.isInfoEnabled()) Log.info("Adding endpoint listener to " + getHashKey());
    listeners.add(e);

    boolean isUnreachable = !checkServerState();
    boolean isOverloaded = !checkRetryAfter();

    // If this endpoint is down, notify the new listener
    if (isUnreachable) e.endpointUnreachable(unreachable);

    // If this endpoint is overloaded notify the new listener
    if (isOverloaded) e.endpointOverloaded(overloaded);
  }

  /**
   * Removes an EndpointListener from this object
   *
   * @param e An EndpointListener
   * @see EndpointListener
   */
  public void removeEndPointListener(EndpointListener e) {
    if (listeners != null) {
      if (Log.on && Log.isDebugEnabled())
        Log.debug("Removing endpoint listener from " + getHashKey());
      listeners.remove(e);
    }
  }

  /**
   * Removes all EndpointListeners from this object
   *
   * @see EndpointListener
   */
  public void removeAllListeners() {
    if (Log.on && Log.isDebugEnabled())
      Log.debug("Removing all endpoint listeners from " + getHashKey());
    listeners = null;
  }

  /**
   * Sends notifications to each EndpointListener when the state of this object changes.
   *
   * @param type the type of EndpointEvent
   * @see EndpointEvent
   */
  private void notifyListeners(int type) {
    if ((listeners != null) && (listeners.size() > 0)) {

      for (Iterator i = listeners.iterator(); i.hasNext(); ) {
        EndpointListener l = (EndpointListener) i.next();
        switch (type) {
          case EndpointEvent.UNREACHABLE:
            l.endpointUnreachable(unreachable);
            break;
          case EndpointEvent.OVERLOADED:
            l.endpointOverloaded(overloaded);
            break;
          case EndpointEvent.CLEAR_OVERLOADED:
            l.endpointOverloadedClear(clearOverloaded);
            break;
          case EndpointEvent.CLEAR_UNREACHABLE:
            l.endpointUnreachableClear(clearUnreachable);
            break;
        }
      }
    }
  }

  private void notifyListeners(int type, String failureReason) {
    if ((listeners != null) && (listeners.size() > 0)) {

      for (Iterator i = listeners.iterator(); i.hasNext(); ) {
        EndpointListener l = (EndpointListener) i.next();
        switch (type) {
          case EndpointEvent.UNREACHABLE:
            unreachable.setFailureReason(failureReason);
            l.endpointUnreachable(unreachable);
            break;
          case EndpointEvent.OVERLOADED:
            l.endpointOverloaded(overloaded);
            break;
          case EndpointEvent.CLEAR_OVERLOADED:
            l.endpointOverloadedClear(clearOverloaded);
            break;
          case EndpointEvent.CLEAR_UNREACHABLE:
            l.endpointUnreachableClear(clearUnreachable);
            break;
        }
      }
    }
  }

  public void incrementReferences() {
    numReferences++;
  }

  public int getNumReferences() {
    return numReferences;
  }

  public void decrementReferences() {
    if (numReferences > 0) numReferences--;
  }

  public void resetReferences() {
    numReferences = 1;
  }

  public int getActiveUsageLimit() {
    return activeUsageLimit;
  }

  public void setActiveUsageLimit(int activeUsageLimit) {
    this.activeUsageLimit = activeUsageLimit;
  }

  public void incrementTotalUsageCount() {
    usageCount.incrementAndGet();
  }

  public long getTotalUsageCount() {
    return usageCount.get();
  }

  public void incrementTotalFailureCount() {
    failureCount.incrementAndGet();
  }

  public long getTotalFailureCount() {
    return failureCount.get();
  }

  public void resetTotalCounts(long activeUsageCount) {
    usageCount.set(activeUsageCount);
    failureCount.set(0);
  }
}
