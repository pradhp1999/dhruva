package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;

public interface DsProxyInterface {
  void proxyTo(
      DsSipRequest request, DsProxyCookieInterface cookie, DsProxyBranchParamsInterface params);

  void addProxyRecordRoute(DsSipRequest request, DsProxyBranchParamsInterface params);
}
