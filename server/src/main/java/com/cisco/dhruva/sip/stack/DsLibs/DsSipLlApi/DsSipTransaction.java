// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipTransactionKey;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import java.io.IOException;

/** This interface defines methods common to client and server transactions. */
public interface DsSipTransaction {
  // CAFFEINE 2.0 DEVELOPMENT
  // Bugid used: CSCef24857 Internal RSeq counter starts at initial fixed value
  /** Long that represents the minimum RSeq number. */
  public static final long MIN_RSEQ = 1L;
  /** Long that represents the maximum RSeq number. */
  public static final long MAX_RSEQ = (1L << 31) - 1;

  /**
   * Starts the transaction. Performs the transition between the initial and next state of the state
   * machine. Perform initialization actions not performed by the constructor.
   *
   * @throws IOException if the execution of the state machine results in an IOException
   * @throws DsException if transaction is no longer in a state consistent with the semantics of
   *     this method
   */
  void start() throws IOException, DsException;;

  /**
   * Returns false if the transaction is in the DsSipStateMachineDefinitions.DS_INITIAL state.
   * Otherwise returns true.
   *
   * @return false if the transaction is in the DsSipStateMachineDefinitions.DS_INITIAL state,
   *     otherwise returns true.
   */
  boolean isStarted();

  /**
   * Returns this transaction's initial request.
   *
   * @return this transaction's initial request.
   */
  DsSipRequest getRequest();

  /**
   * Test to see whether the transaction is a server transaction.
   *
   * @return true if the server is a server transaction, otherwise returns false
   */
  boolean isServerTransaction();

  /**
   * Returns true if this transaction uses a proxy server state machine.
   *
   * @return true if this transaction uses a proxy server state machine
   */
  boolean isProxyServerMode();

  /**
   * if <code>true</code> this transaction will use a proxy server state machine, otherwise it will
   * not.
   *
   * @param mode if <code>true</code> this transaction will use a proxy server state machine,
   *     otherwise it will not.
   */
  void setProxyServerMode(boolean mode);

  /**
   * Returns <code>true</code> if this is an INVITE transaction, otherwise returns <code>false
   * </code>.
   *
   * @return <code>true</code> if this is an INVITE transaction, otherwise returns <code>false
   *     </code>
   */
  boolean isInvite();

  /**
   * Returns the method of this transaction's request.
   *
   * @return the method of this transaction's request
   */
  int getMethodID();

  /**
   * Returns The transaction key for this transaction.
   *
   * @return The transaction key for this transaction
   */
  DsSipTransactionKey getKey();

  /**
   * Returns the current state of this transaction.
   *
   * @return the current state of this transaction
   * @see DsSipStateMachineDefinitions
   */
  int getState();

  /**
   * Used for debugging.
   *
   * @return a string representation of this transaction
   */
  String getAsString();

  /**
   * Handle an asynchronous IO Exception.
   *
   * @param ioe the exception
   */
  void onIOException(IOException ioe);

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  /**
   * Use get100relSupport() to get the value of 100rel support level (REQUIRE, SUPPORTED, or
   * UNSUPPORTED).
   *
   * @return current 100rel support level.
   */
  public byte get100relSupport();

  /**
   * Set the 100rel support level to one of "Require" (REQIURE), "Supported" (SUPPORTED), or
   * "Unsupported" (UNSUPPORTED)
   *
   * @param attribute 100rel support level. Pass in one of the above attributes. If null is passed,
   *     no action will be taken.
   */
  public void set100relSupport(byte attribute);
}
