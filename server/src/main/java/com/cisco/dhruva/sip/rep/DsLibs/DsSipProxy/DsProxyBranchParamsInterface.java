/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.transport.Transport;

/**
 * Encapsulates parameters that can be passed to ProxyTransaction API calls (i.e., only proxyTo()
 * roght now) to modify the default behavior for a single branch NOTE: The API for this class might
 * change as OAM&P matures
 */
public interface DsProxyBranchParamsInterface {

  // should use constants from DsSipTransportType instead

  //   public static final int UDP = DsSipTransportType.UDP;
  //   public static final int TCP = DsSipTransportType.TCP;
  //   public static final int TLS = DsSipTransportType.TLS;
  //   public static final int NONE = DsSipTransportType.NONE;

  public static final int HIDE_NONE = 0;
  public static final int HIDE_HOP = 1;
  public static final int HIDE_ROUTE = 2;

  /**
   * Specifies whether the proxy needs to insert itself into the Record-Route
   *
   * @return Record-Route setting
   */
  public boolean doRecordRoute();

  // This was both unnecessary and confusing so I just removed
  // it. The same functionality is achieved by setting
  // proxyToAddress/Port/Protocol, which had to be set anyway.

  //   /** Returns true if the user set outbound IP address/port
  //    * @return true if set, false otherwise
  //    * When used in DsProxyParamsInterface, it effectively
  //    * specifies the local outbound proxy
  //    */
  //   public boolean isLocalProxySet();

  /**
   * Returns the address to proxy to
   *
   * @return the address to proxy to, null if the default forwarding logic is to be used
   */
  public DsByteString getProxyToAddress();

  /**
   * Returns port to proxy to
   *
   * @return the port to proxy to; if -1 is returned, default port will be used
   */
  public int getProxyToPort();

  /** @return protocol to use for outgoing request */
  public Transport getProxyToProtocol();

  /**
   * @return the timeout value in milliseconds for outgoing requests. -1 means default timeout This
   *     allows to set timeout values that are _lower_ than SIP defaults. Values higher than SIP
   *     deafults will have no effect.
   */
  public long getRequestTimeout();

  public String getRequestDirection();

  public DsByteString getRecordRouteUserParams();
}
