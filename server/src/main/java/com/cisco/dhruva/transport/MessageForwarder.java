/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.transport;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsBindingInfo;

public interface MessageForwarder {

  public void processMessage(byte[] messageBytes, DsBindingInfo bindingInfo);
}
