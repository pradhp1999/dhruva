/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.search.interfaces;

import com.cisco.dhruva.sip.re.search.Location;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

public interface ProxyInterface {

  public void proxyTo(Location location, DsSipRequest request, ProxyResponseInterface callbackIf);

  public void proxyTo(
      Location location, DsSipRequest request, ProxyResponseInterface callbackIf, long timeout);

  /**
   * Callback causing the transaction branch(es) associated with the specified location to be
   * cancelled.
   *
   * @param location the {@link com.cisco.re.search.Location} whose transaction is to be cancelled
   * @param timedOut indicates whether or not the transaction branch(es) are being cancelled due to
   *     a timeout. This is currently only <code>true</code> when an iterate times out.
   * @see {@link com.cisco.xcl.ProxyScriptAgent#run(Object)}
   */
  public void cancel(Location location, boolean timedOut);
}
