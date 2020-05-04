/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

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
