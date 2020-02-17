// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import java.security.cert.X509Certificate;

/**
 * This interface defines whether a SSL client or SSL server be authenticated or not. If yes, then
 * whether it should be authenticated now or always. This interface provides an opportunity to the
 * user to decide the trust relationship between peers interactively.
 */
public interface DsTrustInterface {
  /** Constant specifying that the peer should not be trusted. */
  public static final short TRUST_NO = 0;

  /** Constant specifying that the peer should not be trusted this time. */
  public static final short TRUST_NOW = 1;

  /** Constant specifying that the peer should always be trusted, now onwards. */
  public static final short TRUST_ALWAYS = 2;

  /**
   * Tells whether an SSL client is trusted, not trusted or should be trusted always. Given the
   * partial or complete certificate chain provided by the SSL client, returns any of the following
   * three options:<br>
   * TRUST_NO - Don't trust this certificate chain<br>
   * TRUST_NOW - Trust this certificate chain, this time<br>
   * TRUST_ALWAYS - Always trust this certificate chain and don't ask again.<br>
   *
   * @param chain the peer certificate chain
   * @return <code>TRUST_NO</code> if not to trust this certificate chain<br>
   *     <code>TRUST_NOW</code> if trust this certificate chain, this time<br>
   *     <code>TRUST_ALWAYS</code> if Always trust this certificate chain and don't ask again.<br>
   */
  public short isClientTrusted(X509Certificate[] chain);

  /**
   * Tells whether an SSL server is trusted, not trusted or should be trusted always. Given the
   * partial or complete certificate chain provided by the SSL server, returns any of the following
   * three options:<br>
   * TRUST_NO - Don't trust this certificate chain<br>
   * TRUST_NOW - Trust this certificate chain, this time<br>
   * TRUST_ALWAYS - Always trust this certificate chain and don't ask again.<br>
   *
   * @param chain the peer certificate chain
   * @return <code>TRUST_NO</code> if not to trust this certificate chain<br>
   *     <code>TRUST_NOW</code> if trust this certificate chain, this time<br>
   *     <code>TRUST_ALWAYS</code> if Always trust this certificate chain and don't ask again.<br>
   */
  public short isServerTrusted(X509Certificate[] chain);
}
