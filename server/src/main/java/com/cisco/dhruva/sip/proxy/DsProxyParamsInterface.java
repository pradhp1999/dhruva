/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsProxyBranchParamsInterface.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPathHeader;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRecordRouteHeader;

/**
 * Describes configuration settings of a
 * ProxyTransaction
 */
public interface DsProxyParamsInterface extends DsProxyBranchParamsInterface {

  public static final int NO_MAX_FORWARDS = 0;

  // Message Direction
  //public static final byte LISTEN_INTERNAL = RE.index_reListenDirection_internal;
  //public static final byte LISTEN_EXTERNAL = RE.index_reListenDirection_external;
  //public static final byte UNKNOWN_DIRECTION = -1;

  //public static final byte LISTEN_NONE = UNKNOWN_DIRECTION;
  //public static final byte INBOUND = LISTEN_INTERNAL;
  //public static final byte OUTBOUND = LISTEN_EXTERNAL;

  /**
   * @return default SIP port number
   */
  public int getDefaultPort();


//   /**
//    * @return the local IP address that the server is
//    * running on. It's needed for Via processing
//    */
//   public InetAddress getLocalAddress();

//   /** @return local IP address/hostname as a string
//    * It's needed for Via/Record-Route/etc processing
//    */
//   public String getLocalHost();

//   /**
//    * @return if true, the proxy will operate in stateful mode
//    * be default, otherwise the proxy will default to stateless
//    */
//   public boolean isStateful();


  /**
   * @return the interface to be inserted into Record-Route
   * if the transport parameter of that interface is NONE, no
   * transport parameter will be used in R-R; otherwise, the transport
   * of this interface will be used.
   */
  public DsSipRecordRouteHeader getRecordRouteInterface(String direction);

  /**
   * Get the interface to be inserted into Path headers.  If the
   * port of that interface is -1, no port will be used in the Path.
   * If the transport is {@link com.dynamicsoft.DsLibs.DsSipObject.DsSipTransportType#NONE},
   * no transport parameter will be used the the Path.
   * @return the interface to be inserted into a Path header, or
   * <code>null</code> if no Path header should be used
   */
  public DsSipPathHeader getPathInterface(int protocol, String direction);


  /**
   * @param protocol UDP or TCP
   * @return the address and port number that needs to be inserted
   * into the Via header for a specific protocol used
   */
  public DsViaListenInterface getViaInterface(int protocol, String direction);

  /**
   * @return default protocol we are listening on (one of the constants
   * defined in DsSipTransportType.java)
   * //This is used in Record-Route, for example
   * This is not really used by the proxy core anymore
   */
  public int getDefaultProtocol();

}
