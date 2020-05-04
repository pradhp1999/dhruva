/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.re.search.interfaces;

import com.cisco.dhruva.sip.re.search.Location;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;

public interface ProxyResponseInterface {
  void onProvisionalResponse(Location location, DsSipResponse response);

  void onSuccessResponse(Location location, DsSipResponse response, int responseCode);

  void onRedirectResponse(Location location, DsSipResponse response, int responseCode);

  void onFailureResponse(Location location, DsSipResponse response, int responseCode);

  void onGlobalFailureResponse(Location location, DsSipResponse response, int responseCode);

  void onRequestTimeout(Location location, DsSipResponse response);

  void onProxySuccess(Location location);

  void onProxyFailure(Location location, int responseCode);
}
