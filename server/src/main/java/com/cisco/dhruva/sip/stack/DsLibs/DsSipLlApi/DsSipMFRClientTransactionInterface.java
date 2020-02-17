// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsConfigManager;

/**
 * This interface allows you to be notified of events related to client transactions, including the
 * receipt of multiple final responses.
 *
 * <p>A multiple final response is a final response received for a client transaction that has a
 * different To tag to the initial final response received i.e. the sent request was forked and
 * final responses were returned to the client by different servers. Forking should only ever occur
 * for INVITE client transactions so you do not have to worry about the non-INVITE case.
 *
 * <p>In order to process these responses you must first enable the handling of multiple final
 * responses globally via DsConfigManager. This is done using its static
 * handleMultipleFinalResponses(boolean) method. Then you can enable multiple final response
 * handling on a per transaction basis by passing an object that implements this interface to the
 * transaction manager's factory methods. If you do not want to handle multiple final responses for
 * a particular transaction you use an object that only implements DsSipClientTransactionInterface
 * (this interface's parent).
 *
 * <p>Note that the global setting for handling multiple final responses overrides individual
 * transaction settings, and that multiple final response handling is disabled by default.
 *
 * <p>When a multiple final response is received and handling is enabled (both globally and locally)
 * the transaction is cloned and the response is passed to the new transaction. The new transaction
 * has the same MFR client transaction interface as the original transaction. The
 * multipleFinalResponse() method of the interface is invoked with the original transaction, the new
 * transaction and the multiple final response that was received as parameters.
 *
 * <p>Note that the creation of new transactions occurs for both 2xx and non-2xx multiple final
 * responses. You need to ACK each newly created transaction individually.
 *
 * <p>When a multiple final response is received and handling is disabled (either globally or
 * locally) the response is treated as a regular final response and is passed to the original
 * transaction. No new transaction is created.
 *
 * @see DsSipClientTransactionInterface
 * @see DsConfigManager
 */
public interface DsSipMFRClientTransactionInterface extends DsSipClientTransactionInterface {
  /**
   * This method is invoked upon receipt of multiple final responses from the server.
   *
   * @param originalTransaction the handle to original transaction
   * @param newTransaction the handle to the new cloned transaction
   * @param response the handle to message from server
   */
  public void multipleFinalResponse(
      DsSipClientTransaction originalTransaction,
      DsSipClientTransaction newTransaction,
      DsSipResponse response);
}
