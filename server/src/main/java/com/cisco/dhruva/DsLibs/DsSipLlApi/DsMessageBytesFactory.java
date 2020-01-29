// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsUtil.*;

public interface DsMessageBytesFactory {
  public DsMessageBytes createMessageBytes(byte[] bytes, DsBindingInfo binfo);
}
