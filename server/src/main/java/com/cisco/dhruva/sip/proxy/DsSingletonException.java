/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsSingletonException.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

/**
 * DsSingletonException is thrown when a second instance of a singleton class is attempted to be
 * created
 */
public class DsSingletonException extends DsException {

  public DsSingletonException(String msg) {
    super(msg);
  }
}
