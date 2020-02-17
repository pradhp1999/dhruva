// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

/**
 * Application needs to implement this interface and get notified when the inactivity timer for a
 * persistent connection expires.
 */
public interface DsSipConnectionEventInterface {
  /**
   * This method is invoked when the inactivity timer expires for a persistent connection
   *
   * @param connection handle
   * @param context set by the application
   */
  void inactivityTimerExpired(DsConnection connection, Object context);
}
