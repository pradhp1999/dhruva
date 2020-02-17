package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

public interface UsageLimitInterface {
  public boolean checkActiveUsageLimit(EndPoint endPoint, int limit);
}
