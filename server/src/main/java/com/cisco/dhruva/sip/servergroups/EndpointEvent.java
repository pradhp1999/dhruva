/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
package com.cisco.dhruva.sip.servergroups;

import com.cisco.dhruva.sip.DsUtil.EndPoint;
import java.util.EventObject;

public class EndpointEvent extends EventObject {

  public static final int UNREACHABLE = 0;
  public static final int OVERLOADED = 1;
  public static final int CLEAR_UNREACHABLE = 2;
  public static final int CLEAR_OVERLOADED = 3;
  public static final String DEFAULT_FAILURE_REASON = "unknown";

  private int type = UNREACHABLE;
  private EndPoint ep = null;
  private String serverGroupName;
  private String failureReason;

  public EndpointEvent(Object source, int type) {
    super(source);
    this.type = type;
    this.ep = (EndPoint) source;
  }

  public EndpointEvent(Object source, int type, String serverGroupName) {
    this(source, type);
    this.failureReason = DEFAULT_FAILURE_REASON;
    this.serverGroupName = serverGroupName;
  }

  public int getType() {
    return type;
  }

  public EndPoint getEndPoint() {
    return ep;
  }

  public String setFailureReason(String Reason) {
    return failureReason = Reason;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public String getServerGroupName() {
    return serverGroupName;
  }
}
