/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME: $RCSfile: AbstractNextHop.java,v $
//
// MODULE:  servergroups
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
//
// MODIFICATIONS:
//
//
//////////////////////////////////////////////////////////////////////////////
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.loadbalancer.ServerGroupElementInterface;
import com.cisco.dhruva.loadbalancer.ServerInterface;
import com.cisco.dhruva.sip.DsUtil.EndPoint;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRetryAfterHeader;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.util.StringTokenizer;

/**
 * This class servers as a superclass for <code>Next Hop</code> server group elements. Because it is
 * a general-purpose class, it defines a number of methods for creating new instances, accessing
 * data members, comparing two instances, and creating a copy of an instance while preserving the
 * internal reference to some data members.
 *
 * @see ServerGroupElement
 * @see ServerInterface
 * @see com.dynamicsoft.DsLibs.DsUtil.EndPoint
 * @see ServerGlobalStateWrapper
 */
public abstract class AbstractNextHop extends ServerGroupElement
    implements Comparable, ServerInterface {

  private ServerGlobalStateWrapper globalWrapper = null;

  private static final Trace Log = Trace.getTrace(AbstractNextHop.class.getName());

  /**
   * Constructor
   *
   * @param host The Host name of this end point/address.
   * @param port The port number.
   * @param protocol The int representing the protocol.
   * @param qValue the priority of this endpoint
   */
  protected AbstractNextHop(
      DsByteString network,
      DsByteString host,
      int port,
      Transport protocol,
      float qValue,
      DsByteString serverGroup,
      Boolean dnsServerGroup) {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering AbstractNextHop()");
    this.isNextHop = true;

    globalWrapper =
        new ServerGlobalStateWrapper(
            network, host, serverGroup.toString(), port, protocol, dnsServerGroup);
    this.setQValue(qValue);
    setParent(serverGroup);
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving AbstractNextHop()");
  }

  protected AbstractNextHop(
      DsByteString network,
      DsByteString host,
      int port,
      Transport protocol,
      DsByteString serverGroup,
      Boolean dnsServerGroup) {
    this(network, host, port, protocol, ServerGroupElement.DEFAULT_Q, serverGroup, dnsServerGroup);
  }

  /**
   * Gets the destination this <code>NextHop</code> is keeping track of.
   *
   * @return the <code>EndPoint</code>
   */
  public final EndPoint getEndPoint() {
    return globalWrapper;
  }

  /**
   * Gets the object containing the globally available state for this endpoint.
   *
   * @return the <code>GlobalStateWrapper</code> containing the globally available state for this
   *     endpoint.
   * @see ServerGlobalStateWrapper
   */
  public final ServerGlobalStateWrapper getGlobalWrapper() {
    return globalWrapper;
  }

  /**
   * Sets the object containing the globally available state for this endpoint.
   *
   * @param wrapper the <code>GlobalStateWrapper</code> containing the globally available state for
   *     this endpoint.
   * @see ServerGlobalStateWrapper
   */
  public final void setGlobalWrapper(ServerGlobalStateWrapper wrapper) {
    this.globalWrapper = wrapper;
  }

  /**
   * Determines if the given object has the same values for <code>EndPoint</code> and <code>qValue
   * </code> as this object.
   *
   * @param obj the object to compare.
   * @return <code>true</code> if this object is equal to the given object.
   */
  public boolean equals(Object obj) {
    if (obj == null) return false;
    try {
      AbstractNextHop nh = (AbstractNextHop) obj;
      if (Float.compare(getQValue(), nh.getQValue()) == 0)
        return globalWrapper.equals(nh.getGlobalWrapper());
    } catch (ClassCastException cce) {
    }
    return false;
  }

  /**
   * Overrides Object
   *
   * @return A String representation of this object
   */
  public abstract String toString();

  /**
   * Compares two hosts, whether hostname, IP address, or mixed.
   *
   * @param domain1 this objects domain.
   * @param domain2 another object's domain.
   * @return a positive integer if this endpoint should come after the given object the supplied
   *     object in a sorted list, a negative integer if this endpoint should come before the given
   *     object in a sorted list, or zero if the two objects are equivalent.
   */
  private int compareDomainNames(DsByteString domain1, DsByteString domain2) {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering compareDomainNames()");
    int compare = 0;
    if (!domain1.equals(domain2)) {

      StringTokenizer st1 = new StringTokenizer(domain1.toString(), ".");
      StringTokenizer st2 = new StringTokenizer(domain2.toString(), ".");

      String[] list1 = new String[st1.countTokens()];
      String[] list2 = new String[st2.countTokens()];
      int i = 0;
      while (st1.hasMoreTokens()) {
        list1[i] = st1.nextToken();
        i++;
      }
      i = 0;
      while (st2.hasMoreTokens()) {
        list2[i] = st2.nextToken();
        i++;
      }
      if (list1.length == list2.length) {
        try {
          for (i = 0; i < list1.length; i++) {
            int a = Integer.parseInt(list1[i]);
            int b = Integer.parseInt(list2[i]);
            if (a > b) {
              compare = 1;
              break;
            } else if (b > a) {
              compare = -1;
              break;
            }
          }
        } catch (NumberFormatException nfe) {
          compare = doStringDomainCompare(list1, list2);
        }
      } else {
        compare = doStringDomainCompare(list1, list2);
      }
    }
    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving compareDomainNames()");
    return compare;
  }

  private int doStringDomainCompare(String[] list1, String[] list2) {

    int compare = 0;
    int i = Math.min(list1.length, list2.length) - 1;
    for (; i >= 0; i--) {
      compare = list1[i].compareTo(list2[i]);
      if (compare != 0) return compare;
    }
    if (list1.length < list2.length) return -1;
    return 1;
  }

  /**
   * ******************************************************** ServerGroupElement methods *
   * ********************************************************
   */

  /**
   * Compares this next hop to the given object.
   *
   * @return a positive integer if this endpoint should come after the given object the supplied
   *     object in a sorted list, a negative integer if this endpoint should come before the given
   *     object in a sorted list, or zero if the two objects are equivalent.
   * @param obj the object to compare.
   * @throws ClassCastException
   */
  public final int compareTo(Object obj) throws ClassCastException {
    int compare = super.compareTo(obj);
    if (compare != 0) return compare;
    ServerGroupElement e = (ServerGroupElement) obj;
    if (!e.isNextHop()) return 1;
    AbstractNextHop nh = (AbstractNextHop) e;
    compare = compareDomainNames(getDomainName(), nh.getDomainName());
    if (compare != 0) return compare;
    if (getPort() < nh.getPort()) return 1;
    else if (getPort() > nh.getPort()) return -1;
    else if (getProtocol().getValue() < nh.getProtocol().getValue()) return 1;
    else if (getProtocol().getValue() > nh.getProtocol().getValue()) return -1;
    else return 0;
  }

  /**
   * Determines if the given <code>ServerGroupElementInterface</code> has the same host, port, and
   * transport type as this next hop.
   *
   * @param element the <code>ServerGroupElementInterface</code> to compare.
   * @return <code>true</code> if the given element has the same <code>EndPoint</code> values,
   *     <code>false</code> otherwise.
   */
  public final boolean isSameReferenceTo(ServerGroupElementInterface element) {
    if (element.isNextHop()) {
      AbstractNextHop nh = (AbstractNextHop) element;
      return nh.getEndPoint().equals(globalWrapper);
    } else return false;
  }

  /**
   * ******************************************************** ServerInterface methods *
   * ********************************************************
   */

  /**
   * Gets the network for this endpoint.
   *
   * @return the network for this endpoint.
   */
  public final DsByteString getNetwork() {
    return globalWrapper.getNetwork();
  }

  /**
   * Gets the host for this endpoint.
   *
   * @return the host for this endpoint.
   */
  public final DsByteString getDomainName() {
    return globalWrapper.getHost();
  }

  /**
   * Gets the port for this endpoint.
   *
   * @return the port for this endpoint.
   */
  public final int getPort() {
    return globalWrapper.getPort();
  }

  /**
   * Gets the transport type for this endpoint.
   *
   * @return the transport protocol for this endpoint.
   */
  public final Transport getProtocol() {
    return globalWrapper.getProtocol();
  }

  /**
   * Chaecks if the response code is one of the failover response code
   *
   * @param code response code
   * @return
   */
  public boolean isCodeInFailoverCodeSet(int code) {
    return globalWrapper.isCodeInFailoverCodeSet(code);
  }

  /**
   * Updates the state of this object when a failover occurs that is caused by something other than
   * receiving a Failover Response or an ICMP Error.
   */
  public void onFailover(String failureReason) {
    globalWrapper.decrementTries(failureReason);
    globalWrapper.incrementTotalFailureCount();
  }

  /**
   * Updates the state of this object when a failover occurs that is caused by than receiving a
   * Failover Response or an ICMP Error.
   *
   * @param header the Retry-After header from the failover response. If the header is <code>null
   *     </code>, the failure response default retry after value is used.
   */
  public void onFailoverResponse(DsSipRetryAfterHeader header) {
    globalWrapper.setRetryAfter(header);
    globalWrapper.incrementTotalFailureCount();
  }

  /** Updates the state of this object when an ICMP error is detected. */
  public void onICMPError(String failureReason) {
    globalWrapper.zeroTries(failureReason);
    globalWrapper.incrementTotalFailureCount();
  }

  /** Resets the number of tries for this endpoint. */
  public void onSuccess() {
    globalWrapper.resetTries();
  }

  /**
   * Indicates whether or not this server is available for service
   *
   * @return <code>true</code> if this next hop can be routed to.
   */
  public boolean isAvailable() {
    return globalWrapper.isServerAvailable();
  }

  /**
   * indicates the state of the next hop.
   *
   * @return "true" if next hop state is up else "false"
   */
  public boolean getStatus() {
    return globalWrapper.getStatus();
  }

  public boolean isPingOn() {
    return globalWrapper.isPingOn();
  }

  public void setPingOn(boolean pingOn) {
    globalWrapper.setPingOn(pingOn);
  }

  public void incrementUsageCount() {
    globalWrapper.incrementTotalUsageCount();
  }

  public long getUsageCount() {
    return globalWrapper.getTotalUsageCount();
  }

  public int getActiveUsageLimit() {
    return globalWrapper.getActiveUsageLimit();
  }

  public long getFailureCount() {
    return globalWrapper.getTotalFailureCount();
  }
}
