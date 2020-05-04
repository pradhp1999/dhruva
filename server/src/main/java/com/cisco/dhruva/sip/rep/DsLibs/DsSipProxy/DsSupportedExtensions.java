/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import java.util.HashMap;

class DsSupportedExtensions {

  private static HashMap extensions = new HashMap();

  /** Adds an extension to the extension list */
  protected static synchronized void addExtension(DsByteString extension) {

    if (extensions.containsKey(extension)) return; // the extension is already listed as supported

    HashMap newExtensions = (HashMap) extensions.clone();
    newExtensions.put(extension, extension);

    // the following is an atomic operation according to the JVM definition
    extensions = newExtensions;
  }

  /**
   * Checks whether an extension is supported
   *
   * @param extension extensio name as it appears in Proxy-Require
   * @return true is the extension is registered as supported, false otherwise
   */
  protected static boolean isSupported(DsByteString extension) {
    return extensions.containsKey(extension);
  }

  /** removes an extension from the extensions list */
  protected static synchronized boolean removeExtension(DsByteString extension) {

    if (!extensions.containsKey(extension)) return false;

    HashMap newExtensions = (HashMap) extensions.clone();
    newExtensions.remove(extension);

    // the following is an atomic operation according to the JVM definition
    extensions = newExtensions;

    return true;
  }
}
