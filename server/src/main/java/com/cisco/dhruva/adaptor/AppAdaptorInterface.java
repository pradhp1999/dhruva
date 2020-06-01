package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.sip.proxy.Location;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;

public interface AppAdaptorInterface {
  void handleRequest(DsSipRequest request) throws DhruvaException;

  void handleResponse(Location loc, DsSipResponse response, int responseCode)
      throws DhruvaException;
}
