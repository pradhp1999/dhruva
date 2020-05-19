/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.servergroups;

import java.util.EventListener;

public interface EndpointListener extends EventListener {

  void endpointUnreachable(EndpointEvent e);

  void endpointOverloaded(EndpointEvent e);

  void endpointUnreachableClear(EndpointEvent e);

  void endpointOverloadedClear(EndpointEvent e);
}
