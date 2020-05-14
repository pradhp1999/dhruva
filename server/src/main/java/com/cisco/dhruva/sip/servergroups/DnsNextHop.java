/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
/*
 * User: bjenkins
 * Date: Mar 25, 2002
 * Time: 1:40:37 PM
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.sip.DsUtil.EndPoint;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRetryAfterHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.transport.Transport;
import com.cisco.dhruva.util.log.Trace;
import java.util.HashMap;

/**
 * This class implements a Next Hop with the capability to notify listeners when the state of the
 * Next Hop changes. It is assumed that instances of this class are elements of DefaultServerGroup
 * objects
 *
 * @see DefaultServerGroup
 */
public class DnsNextHop extends AbstractNextHop {

  private static final Trace Log = Trace.getTrace(DnsNextHop.class.getName());

  public DnsNextHop(
      DsByteString network,
      DsByteString hostname,
      int port,
      Transport transport,
      float qValue,
      DsByteString serverGroup) {
    super(network, hostname, port, transport, qValue, serverGroup, true);
  }

  public DnsNextHop(
      DsByteString network, EndPoint endPoint, float qValue, DsByteString serverGroup) {
    this(
        network,
        endPoint.getHost(),
        endPoint.getPort(),
        endPoint.getProtocol(),
        qValue,
        serverGroup);
  }

  // below methods are overridden to avoid notification for DnsServerGroup
  @Override
  public void onFailover(String failureReason) {
    if (Trace.on && Log.isInfoEnabled())
      Log.info("inside  onFailover for DnsServerGroup : " + failureReason);
  }

  @Override
  public void onFailoverResponse(DsSipRetryAfterHeader header) {
    if (Trace.on && Log.isInfoEnabled()) Log.info("inside  onFailoverResponse for DnsServerGroup");
  }

  @Override
  public void onICMPError(String failureReason) {
    if (Trace.on && Log.isInfoEnabled())
      Log.info("inside  onICMPError for DnsServerGroup : " + failureReason);
  }

  @Override
  public void onSuccess() {
    if (Trace.on && Log.isInfoEnabled()) Log.info("inside  onSuccess for DnsServerGroup");
  }

  @Override
  public boolean isPingOn() {
    return false;
  }

  @Override
  public boolean isAvailable() {
    return true;
  }
  /**
   * Overrides Object
   *
   * @return the DefaultNextHop in CLI command format
   */
  public String toString() {
    String value = null;
    HashMap elementMap = new HashMap();
    elementMap.put(SG.sgSgElementSgName, parent);
    elementMap.put(SG.sgSgElementHost, getDomainName());
    elementMap.put(SG.sgSgElementPort, new Integer(getPort()));
    elementMap.put(
        SG.sgSgElementTransport,
        DsSipTransportType.getTypeAsString(getProtocol().getValue()).toUpperCase());
    elementMap.put(SG.sgSgElementQValue, String.valueOf(getQValue()));
    elementMap.put(SG.sgSgElementWeight, String.valueOf(getWeight()));

    // MIGRATION
    value = elementMap.toString();

    return value;
  }

  public Object clone() {
    return new DnsNextHop(
        this.getGlobalWrapper().getNetwork(),
        this.getDomainName(),
        this.getPort(),
        this.getProtocol(),
        this.getQValue(),
        this.getParent());
  }
}
