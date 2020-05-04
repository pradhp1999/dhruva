/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 * @author graivitt
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;

public class DsInternalProxyErrorException extends DsException {

  public DsInternalProxyErrorException(String msg) {
    super(msg);
  }
}
