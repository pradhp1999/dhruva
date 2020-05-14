/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// MODULE: servergroups
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
//
// MODIFICATIONS:
//
//
//////////////////////////////////////////////////////////////////////////////
package com.cisco.dhruva.sip.servergroups;

/**
 * Thrown when a malformed server group is detected when adding a server group to the repository.
 */
public class MalformedServerGroupException extends Exception {

  /**
   * Creates a new MalformedServerGroupException with the given error message.
   *
   * @param error the error message.
   */
  public MalformedServerGroupException(String error) {
    super(error);
  }
}
