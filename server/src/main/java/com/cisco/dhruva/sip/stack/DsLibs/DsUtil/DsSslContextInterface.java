/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.security.KeyStore;

public interface DsSslContextInterface {

  DsTrustManagerImpl getDsTrustManagerImpl(
      KeyStore trustStore,
      String keyAlgorithm,
      String tlsProvider,
      DsSSLTrustManager sslManager,
      DsTrustInterface trustInterface,
      boolean certServiceTrustManagerEnabled)
      throws Exception;
}
