/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import java.util.EventObject;

public class ServerGroupEvent extends EventObject {

  public static final int UNREACHABLE = 0;
  public static final int CLEAR_UNREACHABLE = 2;

  private int type = UNREACHABLE;

  public ServerGroupEvent(AbstractServerGroup source, int type) {
    super(source);
    this.type = type;
  }

  public int getType() {
    return type;
  }

  public DsByteString getName() {
    return ((AbstractServerGroup) source).getName();
  }
}
