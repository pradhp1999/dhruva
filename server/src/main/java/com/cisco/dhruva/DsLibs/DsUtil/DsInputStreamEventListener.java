// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsUtil;

/** Defines an interface to listen for the Input Stream Events. */
public interface DsInputStreamEventListener {
  /**
   * Handler for DsInputStreamClosedEvent.
   *
   * @param evt the event to be handled
   */
  public void onDsInputStreamClosedEvent(DsInputStreamClosedEvent evt);

  /**
   * Handler for DsInputStreamErrorEvent.
   *
   * @param evt the event to be handled
   */
  public void onDsInputStreamErrorEvent(DsInputStreamErrorEvent evt);

  /**
   * Handle events for old-style notification on I/O for DsInputStream. DsInputStream has been
   * superceded by DsStreamListener's derived classes.
   *
   * @param event the event to be handled
   */
  public void onDsInputStreamEvent(DsInputStreamEvent event);
}
