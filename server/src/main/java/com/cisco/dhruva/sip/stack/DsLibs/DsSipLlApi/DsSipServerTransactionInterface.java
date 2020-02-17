// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipCancelMessage;

/**
 * Implement this interface and supply it to the transaction manager by using a
 * DsTransactionFactoryInterface in order to get notified about events related to a server
 * transaction.
 */
public interface DsSipServerTransactionInterface {
  /**
   * Gets invoked upon receipt of ACK message from client.
   *
   * @param serverTransaction handle of transaction
   * @param ackMessage handle to ACK message from client
   */
  void ack(DsSipServerTransaction serverTransaction, DsSipAckMessage ackMessage);

  /**
   * Gets upon receipt of CANCEL message from client.
   *
   * @param serverTransaction handle of transaction
   * @param cancelMessage handle to CANCEL message from client
   */
  void cancel(DsSipServerTransaction serverTransaction, DsSipCancelMessage cancelMessage);

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support

  /**
   * Gets invoked upon receipt of PRACK message from client.
   *
   * @param inviteServerTransaction handle of the INVITE transaction
   * @param prackServerTransaction handle of the PRACK transaction
   */
  void prack(
      DsSipServerTransaction inviteServerTransaction,
      DsSipServerTransaction prackServerTransaction);

  /**
   * Gets when there is no ACK from client.
   *
   * @param serverTransaction handle of transaction
   */
  void timeOut(DsSipServerTransaction serverTransaction);

  /**
   * Gets invoked when an ICMP error occurs while transmitting message through UDP.
   *
   * <p>UPDATE: this method is used to report ANY IOException sending the response or the end of the
   * client list
   *
   * @param serverTransaction handle of transaction
   */
  public void icmpError(DsSipServerTransaction serverTransaction);
}
