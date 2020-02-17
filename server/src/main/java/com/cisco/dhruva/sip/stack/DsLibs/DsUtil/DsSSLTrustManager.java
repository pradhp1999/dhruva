// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.security.cert.X509Certificate;

/**
 * Trust manager interface which provides for checking the authenticity of the server and client.
 */
public interface DsSSLTrustManager {
  /**
   * Given the partial or complete certificate chain provided by the peer, return <code>true</code>
   * if the client should be trusted.
   *
   * @param chain the peer certificate chain
   * @return <code>true</code> if the client should be trusted, <code>false</code> otherwise
   */
  public boolean isClientTrusted(X509Certificate[] chain);
  /**
   * Given the partial or complete certificate chain provided by the peer, return <code>true</code>
   * if the server should be trusted.
   *
   * @param chain the peer certificate chain
   * @return <code>true</code> if the server should be trusted, <code>false</code> otherwise
   */
  public boolean isServerTrusted(X509Certificate[] chain);
}
