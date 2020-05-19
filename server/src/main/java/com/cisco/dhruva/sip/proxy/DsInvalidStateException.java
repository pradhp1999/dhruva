/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsInvalidStateException.java
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
 * DsInvalidStateException is thrown when an operation is attempted in a state where it is
 * prohibited, e.g., when proxyTo() is called after a final response has been sent
 */
public class DsInvalidStateException extends DsException {

  public DsInvalidStateException(String msg) {
    super(msg);
  }
}
