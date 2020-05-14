/*
 * Copyright (c) 2001-2002, 2003-2005 by cisco Systems, Inc.
 * All rights reserved.
 */
// FILENAME:	$RCSFile$
//
// MODULE:	servergroups
//
// $Id: NonExistantElementException.java,v 1.1.1.1.16.1 2005/11/21 01:45:26 lthamman Exp $
//
// COPYRIGHT:
// ============== copyright 2000 dynamicsoft Inc. =================
// ==================== all rights reserved =======================
// /////////////////////////////////////////////////////////////////
package com.cisco.dhruva.sip.servergroups;

/** Thrown when attempting to modify a server group element that does not exist. */
public class NonExistantElementException extends Exception {

  /**
   * Creates a new NonExistantElementException with the given error message.
   *
   * @param error the error message.
   */
  public NonExistantElementException(String error) {
    super(error);
  }
}
