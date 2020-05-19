/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.servergroups;

import java.util.EventListener;

public interface ServerGroupListener extends EventListener {

  public void servergroupUnreachable(ServerGroupEvent e);

  public void servergroupUnreachableClear(ServerGroupEvent e);
}
