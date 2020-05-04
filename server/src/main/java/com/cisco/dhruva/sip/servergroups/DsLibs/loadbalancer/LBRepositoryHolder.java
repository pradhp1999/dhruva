/*
 * Copyright (c) 2020  by Cisco Systems, Inc.All Rights Reserved.
 */

package com.cisco.dhruva.sip.servergroups.DsLibs.loadbalancer;

import java.util.HashMap;

public interface LBRepositoryHolder {

  /** Retrieve a hashmap containing the current server group repository. */
  public HashMap getServerGroups();
}
