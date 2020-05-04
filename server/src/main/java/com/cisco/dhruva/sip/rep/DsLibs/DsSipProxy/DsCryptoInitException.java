/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

/**
 * DsSingletonException is thrown when a second instance of a singleton class is attempted to be
 * created
 */
public class DsCryptoInitException extends DsException {

  public DsCryptoInitException(String msg) {
    super(msg);
  }
}
