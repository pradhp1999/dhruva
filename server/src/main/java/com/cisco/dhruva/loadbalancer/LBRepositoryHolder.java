/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.loadbalancer;

import com.cisco.dhruva.sip.servergroups.AbstractServerGroup;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import java.util.HashMap;

/** An interface that can be used by any class which wants to store a server group repository. */
@FunctionalInterface
public interface LBRepositoryHolder {

  /** Retrieve a hashmap containing the current server group repository. */
  HashMap<DsByteString, AbstractServerGroup> getServerGroups();
}
