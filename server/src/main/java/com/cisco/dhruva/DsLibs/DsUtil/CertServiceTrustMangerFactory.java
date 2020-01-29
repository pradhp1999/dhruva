package com.cisco.dhruva.DsLibs.DsUtil;

import javax.net.ssl.TrustManager;

@FunctionalInterface
public interface CertServiceTrustMangerFactory {
  public TrustManager[] getTrustManagers() throws Exception;
}
