/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsProxyParams.java
//
// MODULE:	DsProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.controller;

import com.cisco.dhruva.config.sip.controller.DsControllerConfig;
import com.cisco.dhruva.sip.proxy.DsListenInterface;
import com.cisco.dhruva.sip.proxy.DsProxyParamsInterface;
import com.cisco.dhruva.sip.proxy.DsViaListenInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPathHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;
import com.cisco.dhruva.transport.Transport;

/**
 * Encapsulates parameters that can be passed to ProxyTransaction's constructor to control its
 * behavior
 */
public class DsProxyParams extends DsProxyBranchParams implements DsProxyParamsInterface {

  protected int defaultPort;
  protected Transport defaultProtocol;
  protected DsListenInterface reInterface = null;
  protected DsViaListenInterface reViaInterface = null;
  protected DsSipRecordRouteHeader recordRouteInterface = null;
  protected DsSipPathHeader pathInterface = null;
  protected DsProxyParamsInterface storedIface;

  // Took out direction
  // private String m_RequestDirection;
  // See getInterface method for the only other significant change  MR

  /**
   * Constructs a DsProxyParams object based on the config passed as a parameter
   *
   * @param config the configuration is copied into the params object being created
   */
  public DsProxyParams(DsProxyParamsInterface config, String requestDirection) {
    super(config, requestDirection);

    defaultPort = config.getDefaultPort();
    defaultProtocol = config.getDefaultProtocol();

    storedIface = config;
  }

  /*
    public DsProxyParams(DsProxyParamsInterface config) {
      this(config, DsControllerConfig.INBOUND);
    }
  */
  /** @return default SIP port number to be used for this ProxyTransaction */
  public int getDefaultPort() {
    return defaultPort;
  }

  /** @return the default protocol to be used for outgoing requests or to put in Record-Route */
  public Transport getDefaultProtocol() {
    return defaultProtocol;
  }

  /**
   * Allows to overwrite SIP default port 5060
   *
   * @param port port number to use instead of 5060
   */
  public void setDefaultPort(int port) {
    if (port > 0) {
      defaultPort = port;
    }
  }

  /**
   * Sets the default protocol to use for outgoing requests
   *
   * @param protocol one of DsProxyConfig.UDP or DsProxyConfig.TCP; any other value will be
   *     converted to UDP
   */
  public void setDefaultProtocol(int protocol) {
    defaultProtocol = Transport.valueOf(DsControllerConfig.normalizedProtocol(protocol)).get();
  }

  public DsViaListenInterface getViaInterface(int protocol, String direction) {
    return ((DsControllerConfig) storedIface)
        .getViaInterface(Transport.valueOf(protocol).get(), direction);
  }
  /*
    public DsSipRecordRouteHeader getRecordRouteInterface() {
      if (recordRouteInterface == null)
          return null;
      return (DsSipRecordRouteHeader)recordRouteInterface.clone();
    }
  */

  @Override
  public DsViaListenInterface getViaInterface(Transport protocol, String direction) {
    return ((DsControllerConfig) storedIface).getViaInterface(protocol, direction);
  }
  /*
    public DsSipRecordRouteHeader getRecordRouteInterface( int direction ) {
      //This method is logically incoherant...
      if (recordRouteInterface == null)
          return null;
      return (DsSipRecordRouteHeader)recordRouteInterface.clone();
    }
  */

  public DsSipRecordRouteHeader getRecordRouteInterface(String direction) {
    // This method is logically incoherant...
    if (recordRouteInterface == null) {
      recordRouteInterface = storedIface.getRecordRouteInterface(direction);
    }
    return recordRouteInterface;
  }

  public String getRequestDirection() {
    return m_RequestDirection;
  }
}
