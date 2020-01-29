// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipMessage;

/**
 * Implement this interface to generate a byte array whose bytes will be used as part of the
 * transaction key. It is the user's responsibility to make sure this byte array only contains those
 * bytes intended to be used to reduce memory usage. After implementing this interface, call
 * DsSipTransactionManager.setTransKeyGenerator() to register this interface implementation and get
 * called back when stack is constructing transaction key.
 */
public interface DsSipTransKeyGenerator {
  /**
   * Implement this method to generate a byte array whose bytes will be used as part of the
   * transaction key. It is the user's responsibility to make sure this byte array only contains
   * those bytes intended to be used to reduce memory usage.
   *
   * @param message the SIP message
   * @param lookup when 'message' is an ACK or CANCEL, this parameter describes whether or not a
   *     lookup is being performed.
   * @param useVia if <code>true</code> create a full key (with Via) otherwise construct a key
   *     without the Via
   * @param isServerTrans <code>true</code> if we're constructing a server transaction key otherwise
   *     construct a client transaction key
   * @param comma ignored
   * @param request_uri if <code>true</code> use the request URI as part of the key
   * @param toTag if <code>true</code> use the To tag as part of the key
   * @return a byte array containing bytes intended to be used as part of transaction key.
   */
  public byte[] generate(
      DsSipMessage message,
      boolean lookup,
      boolean useVia,
      boolean isServerTrans,
      boolean comma,
      boolean request_uri,
      boolean toTag);
}
