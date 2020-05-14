/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.controller;

/*
 * A wrapper class which holds a location and response interface.  Used by the
 * <code>DsProxyController</code> as the cookie object to the proxy core.
 */

import com.cisco.dhruva.sip.proxy.DsProxyCookieInterface;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.proxy.ProxyResponseInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

public class DsProxyCookieThing implements DsProxyCookieInterface {

  protected Location location;
  protected ProxyResponseInterface responseIf;
  protected DsSipRequest outboundRequest = null;

  public DsProxyCookieThing(Location location, ProxyResponseInterface responseIf) {
    this.location = location;
    this.responseIf = responseIf;
  }

  public DsProxyCookieThing(
      Location location, ProxyResponseInterface responseIf, DsSipRequest request) {
    this.location = location;
    this.responseIf = responseIf;
    outboundRequest = request;
  }

  public Location getLocation() {
    return location;
  }

  public ProxyResponseInterface getResponseInterface() {
    return responseIf;
  }

  public DsSipRequest getOutboundRequest() {
    return outboundRequest;
  }
}
