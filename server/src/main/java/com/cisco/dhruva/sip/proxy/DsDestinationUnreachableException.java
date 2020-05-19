/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	DsDestinationUnreachableException.java
//
// MODULE:	DsSipProxy
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
///////////////////////////////////////////////////////////////////

package com.cisco.dhruva.sip.proxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

public class DsDestinationUnreachableException extends DsException {

  public DsDestinationUnreachableException(String msg) {
    super(msg);
  }

  public DsDestinationUnreachableException(String message, Exception exception) {
    super(message, exception);
  }
}
