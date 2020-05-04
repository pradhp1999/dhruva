package com.cisco.dhruva.adaptor;

import com.cisco.dhruva.Exception.DhruvaException;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

public interface AppAdaptorInterface {
  void handleRequest(DsSipRequest request) throws DhruvaException;
}
