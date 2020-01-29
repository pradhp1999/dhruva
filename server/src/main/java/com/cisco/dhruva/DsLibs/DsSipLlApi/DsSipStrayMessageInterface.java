// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.*;

/**
 * The stray message interface defines callbacks from the transaction manager for messages that do
 * not have a transaction associated with them.
 */
public interface DsSipStrayMessageInterface {

  /**
   * Ack message was received without an associated transaction.
   *
   * @param ack ack message that was received
   */
  void strayAck(DsSipAckMessage ack);

  /**
   * Cancel message was received without an associated transaction.
   *
   * @param cancel cancel message that was received
   */
  void strayCancel(DsSipCancelMessage cancel);

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  /**
   * Prack message was received without an associated transaction.
   *
   * @param prack prack message that was received
   */
  void strayPrack(DsSipPRACKMessage prack);

  /**
   * Response that received without an associated transaction.
   *
   * @param response response that was received
   */
  void strayResponse(DsSipResponse response);
}
