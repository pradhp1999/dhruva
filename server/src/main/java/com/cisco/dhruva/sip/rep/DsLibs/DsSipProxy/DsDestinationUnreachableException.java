/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

public class DsDestinationUnreachableException extends DsException {

  public DsDestinationUnreachableException(String msg) {
    super(msg);
  }

  public DsDestinationUnreachableException(String message, Exception exception) {
    super(message, exception);
  }
}
