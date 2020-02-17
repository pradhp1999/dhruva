package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import javax.net.ssl.TrustManager;

@FunctionalInterface
public interface CertServiceTrustMangerFactory {
  public TrustManager[] getTrustManagers() throws Exception;
}
