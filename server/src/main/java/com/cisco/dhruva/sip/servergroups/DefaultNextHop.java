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
public class DefaultNextHop extends AbstractNextHop {

  private static final Trace Log = Trace.getTrace(DefaultNextHop.class.getName());

  public DefaultNextHop(
      DsByteString network,
      DsByteString hostname,
      int port,
      Transport transport,
      float qValue,
      DsByteString serverGroup) {
    super(network, hostname, port, transport, qValue, serverGroup, false);
  }

  public DefaultNextHop(
      DsByteString network, EndPoint endPoint, float qValue, DsByteString serverGroup) {
    this(
        network,
        endPoint.getHost(),
        endPoint.getPort(),
        endPoint.getProtocol(),
        qValue,
        serverGroup);
  }

  public void addEndPointListener(EndpointListener l) {
    this.getGlobalWrapper().addEndPointListener(l);
  }

  public void removeEndPointListener(EndpointListener l) {
    this.getGlobalWrapper().removeEndPointListener(l);
  }

  public void removeAllListeners() {
    this.getGlobalWrapper().removeAllListeners();
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
    // MEETPASS
    elementMap.put(SG.sgSgElementTransport, DsSipTransportType.getTypeAsString(0));
    elementMap.put(SG.sgSgElementQValue, String.valueOf(getQValue()));
    elementMap.put(SG.sgSgElementWeight, String.valueOf(getWeight()));

    // MIGRATION
    value = elementMap.toString();
    /*
    try {
      value = UmsCliUtil.buildSyntax(UmsReaderFactory.getInstance().getReader(),
                                     SG.dsSgSgElement, elementMap, true);
    }
    catch (Throwable t) {
      value = getGlobalWrapper().toString();
      if (Log.on && Log.isEnabledFor(Level.WARN))
        Log.warn("Error converting server group " + parent +
                 " element " + getDomainName() + " " + getPort() + " " +
                 DsSipTransportType.getTypeAsString(getProtocol()) + " " +
                 getQValue() + " to CLI command; probable reason: not using the SG ums properly");
    }
    */
    return value;
  }

  public Object clone() {
    return new DefaultNextHop(
        this.getGlobalWrapper().getNetwork(),
        this.getDomainName(),
        this.getPort(),
        this.getProtocol(),
        this.getQValue(),
        this.getParent());
  }
}
