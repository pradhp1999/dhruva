// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.*;
import com.cisco.dhruva.DsLibs.DsUtil.*;
import java.io.*;
import java.net.*;
import java.util.*;

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
