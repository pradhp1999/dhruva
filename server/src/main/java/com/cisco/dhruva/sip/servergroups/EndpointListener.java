/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.servergroups;

import java.util.EventListener;

public interface EndpointListener extends EventListener {

  public void endpointUnreachable(EndpointEvent e);

  public void endpointOverloaded(EndpointEvent e);

  public void endpointUnreachableClear(EndpointEvent e);

  public void endpointOverloadedClear(EndpointEvent e);
}
