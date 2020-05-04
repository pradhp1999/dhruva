/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.rep.DsLibs.DsSipProxy;
/*
 * This class implements a Via loop detector for servers that use max-forwards
 * rather than Via headers to detect loops.  Therefore, this class does not
 * actually do any via loop detection at all.
 */

import com.cisco.dhruva.sip.re.configs.DsControllerConfig;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipDefaultBranchIdImpl;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.util.log.Trace;
import java.net.InetAddress;

public class DsMaxForwardsLoopDetector implements DsLoopCheckInterface {

  protected static Trace Log = Trace.getTrace(DsMaxForwardsLoopDetector.class.getName());

  static {
    try {
      String hostName = InetAddress.getLocalHost().getHostAddress();

      if (Log.on && Log.isDebugEnabled())
        Log.debug("Initializing branch-id creator with hostname: " + hostName);

      DsSipDefaultBranchIdImpl.init(hostName);
    } catch (Exception e) {
      Log.warn("Error obtaining hostname for branch-id creator initialization");
    }
  }

  private static DsSipDefaultBranchIdImpl branchIdGenerator = new DsSipDefaultBranchIdImpl();

  protected DsMaxForwardsLoopDetector(DsViaHandler viaHandler) {}

  public final boolean isLooped(DsSipRequest request) {
    return false;
  }

  public final DsByteString getBranchID(int n_branch, DsSipRequest request) {
    // Must pass the request if we are processing statelessly so we can
    // ensure that the same branch-ID is generated each time.
    if (DsControllerConfig.getCurrent().getStateMode() == DsControllerConfig.STATEFUL)
      return branchIdGenerator.nextBranchId(null);
    else return branchIdGenerator.nextBranchId(request);
  }
}
