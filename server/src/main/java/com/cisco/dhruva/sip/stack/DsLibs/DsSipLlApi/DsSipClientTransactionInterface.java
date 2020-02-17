// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;

/**
 * Implement this interface and pass it to one of the transaction manager's factory methods in order
 * to be notified of events related to the client transaction.
 */
public interface DsSipClientTransactionInterface {
  /**
   * This method is invoked upon receipt of a provisional response from the server.
   *
   * @param clientTransaction handle of transaction
   * @param response handle to message from server
   */
  void provisionalResponse(DsSipClientTransaction clientTransaction, DsSipResponse response);

  /**
   * This method is invoked upon receipt of a final response from the server.
   *
   * @param clientTransaction handle of transaction
   * @param response handle to message from server
   */
  void finalResponse(DsSipClientTransaction clientTransaction, DsSipResponse response);

  /**
   * This method is invoked upon timeout of the client transaction.
   *
   * @param clientTransaction handle of transaction
   */
  void timeOut(DsSipClientTransaction clientTransaction);

  /**
   * This method is invoked when an ICMP error occurs while transmitting message through UDP.
   *
   * <p>UPDATE: this method is used to report ANY IOException sending the request or the end of the
   * server list
   *
   * @param clientTransaction handle of transaction
   */
  void icmpError(DsSipClientTransaction clientTransaction);
}
