package com.cisco.dhruva.util.cac;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.EndPoint;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.UsageLimitInterface;

/** Created by IntelliJ IDEA. User: rrachuma Date: 1/26/11 Time: 1:50 PM */
public class CACLimit implements UsageLimitInterface {
  public boolean checkActiveUsageLimit(EndPoint endPoint, int limit) {
    return (!SIPSessions.getTrackSIPSessions()
        || limit > SIPSessions.getActiveSessionCountByEndPoint(endPoint));
  }
}
