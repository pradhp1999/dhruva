package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.DsUtil.EndPoint;

public interface UsageLimitInterface {
  public boolean checkActiveUsageLimit(EndPoint endPoint, int limit);
}
