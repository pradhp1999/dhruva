// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipMessage;

/**
 * Defines a contract to deal with any malformed SIP message that comes in from the network. This
 * malformed SIP message can be a recognized SIP message with some invalid data or it can be any
 * un-recognized SIP message. The user code should not attempt to respond to malformed requests as
 * the stack does so automatically.
 */
public interface DsSipMalformedMessageInterface {
  /**
   * Handles the malformed SIP message that came from the network.
   *
   * @param message the malformed SIP message
   */
  public void malformedMessage(DsSipMessage message);

  /**
   * Handles the malformed SIP message that came from the network.
   *
   * @param bytes the malformed SIP message bytes
   */
  public void malformedMessage(byte[] bytes);
}
