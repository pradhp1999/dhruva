// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipMessage;

/**
 * Provides a means for the transaction manager to notify the user code of transaction life-cycle
 * events and errors.
 */
public interface DsSipTransactionEventInterface {
  /** Notification that the transaction manager is shutting down. */
  void transactionManagerShutdown();

  /**
   * Notification that the transaction has been removed from the transaction manager transaction
   * map.
   *
   * @param transaction the transaction that has been removed
   */
  void transactionTerminated(DsSipTransaction transaction);

  /**
   * Notification that the transaction submitted to the transaction manager has been started.
   *
   * @param transaction the transaction that has been started
   */
  void transactionStarted(DsSipTransaction transaction);

  /**
   * Handle an individual error.
   *
   * @param transaction the transaction in which the error occurred
   * @param message the message being sent
   * @param error the error encountered
   */
  void transactionError(DsSipClientTransaction transaction, DsSipMessage message, Throwable error);

  /**
   * Handle an individual error.
   *
   * @param transaction the transaction in which the error occurred
   * @param message the message being sent
   * @param error the error encountered
   */
  void transactionError(DsSipServerTransaction transaction, DsSipMessage message, Throwable error);
}
