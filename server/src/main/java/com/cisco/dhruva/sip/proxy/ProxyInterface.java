package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.adaptor.AppAdaptorInterface;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

// MEETPASS
// ProxyResponseInterface not required since proxy adaptor manages the response
public interface ProxyInterface {

  void proxyTo(Location location, DsSipRequest request, AppAdaptorInterface callbackIf);

  void proxyTo(
      Location location, DsSipRequest request, AppAdaptorInterface callbackIf, long timeout);

  void cancel(Location location, boolean timedOut);

  void respond(DsSipResponse response) throws DsException;
}
