// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipRequest;

/**
 * This interface allows the user code to specify a server transaction for new server transactions
 * created by the transaction manager. The user code may alternately call DsSipServerTransaction's
 * setInterface method.
 *
 * @see DsSipServerTransaction#setInterface
 */
public interface DsSipTransactionInterfaceFactory {
  /**
   * This method is called by the transaction manager to obtain a server transaction interface. The
   * server transaction interface is used to notify the user code of events on the newly created
   * server transaction.
   *
   * @param request the request for the server transaction being created
   * @return the server transaction interface which should be passed to the new server transaction
   */
  DsSipServerTransactionInterface createServerTransactionInterface(DsSipRequest request);
}
