/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME: $RCSfile: ServerInterface.java,v $
//
// MODULE:  lb
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
//
// MODIFICATIONS:
//
//
//////////////////////////////////////////////////////////////////////////////
package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.DsUtil.EndPoint;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRetryAfterHeader;
import com.cisco.dhruva.transport.Transport;

/**
 * This interface defines all the methods needed to get the actual next hop destination and update
 * the status of the attempt. This interface was created so that applications wouldn't have access
 * to the <code>ServerGroupElement</code> methods that directly modify the status, statistics, and
 * information of that object.
 */
public interface ServerInterface {
  /**
   * Gets the network name
   *
   * @return the network name
   */
  public DsByteString getNetwork();

  /**
   * Gets the host name.
   *
   * @return the host name.
   */
  public DsByteString getDomainName();

  /**
   * Gets the port.
   *
   * @return the port.
   */
  public int getPort();

  /**
   * Gets the transport type.
   *
   * @return the transport protocol.
   */
  public Transport getProtocol();

  /**
   * Gets the end point associated with this server
   *
   * @return EndPoint end point
   */
  public EndPoint getEndPoint();

  /**
   * Chaecks if the response code is one of the failover response code
   *
   * @param code response code
   * @return
   */
  public boolean isCodeInFailoverCodeSet(int code);

  /**
   * Updates the state of this object when a failover occurs that is caused by something other than
   * receiving a Failover Response or an ICMP Error.
   */
  public void onFailover(String failureReason);

  /**
   * Updates the state of this object when a failover occurs that is caused by receiving a Failover
   * Response.
   *
   * @param header the Retry-After header from the failover response. If the header is <code>null
   *     </code>, the failure response default retry after value is used.
   */
  public void onFailoverResponse(DsSipRetryAfterHeader header);

  /** Updates the state of this object when it is successfully used. */
  public void onSuccess();

  /** Updates the state of this object when an ICMP error is detected. */
  public void onICMPError(String failureReason);

  /**
   * Indicates whether or not this server is available.
   *
   * @return <code>true</code> if this server can be routed to.
   */
  public boolean isAvailable();

  /** increments the usage count of this server by 1 */
  public void incrementUsageCount();
}
