package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;

// Controller to App response Path
// Controller gets this interface from cookieThing.getResponseInterface() and invokes
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
