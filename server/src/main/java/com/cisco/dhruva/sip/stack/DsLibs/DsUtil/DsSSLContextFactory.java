/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.security.KeyStore;

public class DsSSLContextFactory {

  private DsSSLContextFactory() {}

  private static DsSslContextInterface sslcontextprovider;

  public static void setSslcontextprovider(DsSslContextInterface sslcontextprovider) {
    DsSSLContextFactory.sslcontextprovider = sslcontextprovider;
  }

  public static DsTrustManagerImpl getDsTrustManagerImpl(
      KeyStore trustStore,
      String keyAlgorithm,
      String tlsProvider,
      DsSSLTrustManager sslManager,
      DsTrustInterface trustInterface,
      boolean certServiceTrustManagerEnabled)
      throws Exception {

    if (sslcontextprovider != null) {

      DsTrustManagerImpl dsTrustManagerImpl =
          sslcontextprovider.getDsTrustManagerImpl(
              trustStore,
              keyAlgorithm,
              tlsProvider,
              sslManager,
              trustInterface,
              certServiceTrustManagerEnabled);
      return dsTrustManagerImpl;
    }
    return new DsTrustManagerImpl(
        trustStore,
        keyAlgorithm,
        tlsProvider,
        sslManager,
        trustInterface,
        certServiceTrustManagerEnabled);
  }
}
