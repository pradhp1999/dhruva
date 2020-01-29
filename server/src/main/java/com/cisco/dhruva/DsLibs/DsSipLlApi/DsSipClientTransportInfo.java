// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsUtil.DsBindingInfo;
import com.cisco.dhruva.DsLibs.DsUtil.DsNetwork;
import java.util.Set;

/**
 * This class is used in the DsSipClientTransaction constructor to configure the Via header when the
 * connection transport is determined. This interface is needed since the transport is determined
 * dynamically by the DsSipClientTransaction implementation. If a instance of
 * DsSipClientTransportInfo is not provided to the client transaction constructor, a listening
 * transport is selected from the transport layer.
 */
public interface DsSipClientTransportInfo {
  /**
   * Get the binding info that the client wishes to associate with this transport.
   *
   * @param transport the transport being queried
   * @param network the network from the current request or the default network if no network exists
   *     in the request
   * @return the binding info that the client wishes to use for via header parameters
   */
  DsBindingInfo getViaInfoForTransport(int transport, DsNetwork network);

  /**
   * Return Set of Integer(s) representing supported protocols.
   *
   * @param network the network from the current request or the default network if no network exists
   *     in the request
   * @return Set of Integer(s) representing supported protocols
   */
  Set getSupportedTransports(DsNetwork network);
}
