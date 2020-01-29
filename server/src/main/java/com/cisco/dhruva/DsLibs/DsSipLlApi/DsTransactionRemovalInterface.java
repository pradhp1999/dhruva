// Copyright (c) 2006 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

/** Callback interface to allow application to be notified about transaction removal. */
public interface DsTransactionRemovalInterface {
  /**
   * Called when a transaction is remove from the transaction manager. Application code can use
   * isInvite() and isServerTransaction() on <code>transaction</code> to determine the exact type of
   * transaction that was removed.
   *
   * @param transaction the transaction that was just removed.
   */
  public void transactionRemoved(DsSipTransaction transaction);
}
