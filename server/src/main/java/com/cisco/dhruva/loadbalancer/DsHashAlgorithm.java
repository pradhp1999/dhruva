/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.util.log.Trace;

/**
 * Hash based algorithm used to select the next server/hop. It takes a key to ensure that those
 * requests with the same key will go to the same server/hop.
 */
public class DsHashAlgorithm {

  /** our log object * */
  protected static Trace Log = Trace.getTrace(DsHashAlgorithm.class.getName());

  /**
   * Select the index based on the key and the size of the list
   *
   * @param key Key used to select the server/hop.
   * @param listSize The size of the list of those servers available.
   * @return The index of the next hop in the server list.
   */
  public static int selectIndex(DsByteString key, int listSize) {
    if (Log.on && Log.isTraceEnabled()) Log.trace("Entering selectIndex()");

    if (listSize <= 0) return -1;

    int hashValue = Math.abs(key.hashCode());

    int index = hashValue % listSize;

    if (Log.on && Log.isTraceEnabled()) Log.trace("Leaving selectIndex()");

    return index;
  }
}
