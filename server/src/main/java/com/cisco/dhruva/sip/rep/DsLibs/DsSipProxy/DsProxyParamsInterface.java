/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPathHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransportType;
import com.cisco.dhruva.transport.Transport;

/** Describes configuration settings of a ProxyTransaction */
public interface DsProxyParamsInterface extends DsProxyBranchParamsInterface {

  public static final int NO_MAX_FORWARDS = 0;

  public int getDefaultPort();

  /**
   * @return the interface to be inserted into Record-Route if the transport parameter of that
   *     interface is NONE, no transport parameter will be used in R-R; otherwise, the transport of
   *     this interface will be used.
   */
  public DsSipRecordRouteHeader getRecordRouteInterface(String direction);

  /**
   * Get the interface to be inserted into Path headers. If the port of that interface is -1, no
   * port will be used in the Path. If the transport is {@link DsSipTransportType#NONE}, no
   * transport parameter will be used the the Path.
   *
   * @return the interface to be inserted into a Path header, or <code>null</code> if no Path header
   *     should be used
   */
  public DsSipPathHeader getPathInterface(Transport protocol, String direction);

  /**
   * @param protocol UDP or TCP
   * @return the address and port number that needs to be inserted into the Via header for a
   *     specific protocol used
   */
  public DsViaListenInterface getViaInterface(Transport protocol, String direction);

  /**
   * @return default protocol we are listening on (one of the constants defined in
   *     DsSipTransportType.java) //This is used in Record-Route, for example This is not really
   *     used by the proxy core anymore
   */
  public Transport getDefaultProtocol();
}
