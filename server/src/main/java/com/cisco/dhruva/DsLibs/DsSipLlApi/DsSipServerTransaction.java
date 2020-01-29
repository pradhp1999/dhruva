// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipLlApi;

import com.cisco.dhruva.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipCancelMessage;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipPRACKMessage;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.DsLibs.DsSipObject.DsSipTransactionKey;
import com.cisco.dhruva.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.DsLibs.DsUtil.DsLog4j;
import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

/**
 * This is an abstract class which supports the server side of a SIP transaction. It supports both
 * upcalls from the transaction manager and user code control of protocol timers and messages.
 */
public abstract class DsSipServerTransaction implements DsSipTransaction, java.io.Serializable {
  /**
   * Called by user code to send a server response of type
   * DsSipResponseCode.DS_RESPONSE_NOT_ACCEPTABLE to the client request.
   *
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public abstract void sendNotAcceptable() throws IOException, DsException;

  /**
   * Called by user code to send a server response, of the type specified, to a client request.
   *
   * @param responseCode the response code to be sent
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public abstract void sendResponse(int responseCode) throws IOException, DsException;

  /**
   * Called by user code to send a server response. Used in conjunction with
   * DsSipResponse.createResponseBytes.
   *
   * @param response response bytes
   * @param statusCode the response code of the response bytes to be sent
   * @throws DsException if there is an exception in the state machine
   * @throws IOException if there is an IOException encountered upon sending the response.
   */
  public abstract void sendResponse(byte[] response, int statusCode)
      throws IOException, DsException;

  /**
   * Called by user code to send a server response, of the type specified, to a client request. If
   * no response is provided, a default 100 response will be sent.
   *
   * @param response optional response message to send to client
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public abstract void sendResponse(DsSipResponse response) throws IOException, DsException;

  /**
   * Called by user code to set a server transaction interface. If the user has never registered an
   * interface, the supplied interface is called to update it of events that have happened during
   * the lifetime of this transaction.
   *
   * @param serverInterface pointer to server interface. A value of null causes the default
   *     interface to be used
   */
  public abstract void setInterface(DsSipServerTransactionInterface serverInterface);

  /**
   * Called by user code to abort the transaction. This method should be called when the transaction
   * is in its initial state in order to cleanly remove the transaction from the transaction
   * manager. If a CANCEL has been received for this request a 481 response is sent.
   *
   * <p>This method should only be called in proxy server mode.
   *
   * @throws DsException if not in proxy server mode
   */
  public abstract void abort() throws DsException;

  /**
   * Return this transaction's key constructed without the Via header.
   *
   * @return this transaction's key constructed without the Via header
   */
  public abstract DsSipTransactionKey getKeyNoVia();

  /// **
  // * Returns the maximum allowable time for transaction to complete.
  // * A value of zero means no maximum time to live.
  // *
  // * @return int timeout value in milliseconds
  // *
  // */
  // public abstract int getTn();

  /// **
  // * Sets the maximum allowable time for transaction to complete.
  // * A value of zero means no maximum time to live.
  // *
  // * @param timeout timeout value in milliseconds
  // *
  // */
  // public abstract void setTn(int timeout);

  /// **
  // * Returns the maximum time to wait (in milliseconds) for the client
  // * to send an ACK. If this time elapses, the reply is retransmitted.
  // *
  // * @return int timeout value in milliseconds
  // *
  // */
  // public abstract int   getT1();

  /// **
  // * Sets the maximum time to wait (in milliseconds) to go from the
  // * completed state to the final state
  // *
  // * @param timeout timeout value in milliseconds
  // */
  // public abstract void  setTC2F(int timeout);

  /// **
  // * Gets the maximum time to wait (in milliseconds) to go from the
  // * succeeded state to the final state
  // *
  // * @return int timeout value in milliseconds
  // */
  // public abstract int   getTC2F();

  /**
   * Handle a request retransmission delivered by transaction manager.
   *
   * @param request the request
   */
  protected abstract void onRequestRetransmission(DsSipRequest request);

  /**
   * Handle an ACK from the network (delivered via the transaction manager).
   *
   * @param request the ACK message
   */
  protected abstract void onAck(DsSipAckMessage request);

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support
  /**
   * Handle a PRACK message from the network (delivered via the transaction manager).
   *
   * @param prackTransaction the transaction for the PRACK message
   * @param request the PRACK message
   * @throws DsException if there is an exception in the state machine
   */
  protected abstract void onPrack(
      DsSipServerTransaction prackTransaction, DsSipPRACKMessage request) throws DsException;

  /**
   * Handle a CANCEL message from the network (delivered via the transaction manager).
   *
   * @param request the CANCEL message
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  protected abstract void onCancel(DsSipCancelMessage request) throws IOException, DsException;

  /**
   * Called by the transaction manager to mark the transaction as cancelled before it has been acted
   * upon by user code. This is an atomic operation to check to see if the transaction is started
   * and if not, to mark it cancelled. If it is already started it shouldn't be marked cancelled and
   * this method returns false.
   *
   * @param cancel_txn the server transaction for the CANCEL message
   * @return false if this transaction is already started, otherwise returns true
   */
  protected abstract boolean tryMarkCancelled(DsSipServerTransaction cancel_txn);

  /**
   * Returns <code>true</code> if this a server transaction for a merged request.
   *
   * @return <code>true</code> if this a server transaction for a merged request
   */
  protected abstract boolean isMerged();

  /**
   * Returns <code>true</code> the first time it is called, otherwise returns <code>false</code>.
   * Used for atomic table lookup / create.
   *
   * @return <code>true</code> the first time it is called, otherwise returns <code>false</code>;
   *     Used for atomic table lookup / create.
   */
  protected abstract boolean isNew();

  // CAFFEINE 2.0 DEVELOPMENT - (EDCS-295391) PRACK Support

  /** General logging Category. */
  protected static Logger genCat = null;

  static {
    genCat = DsLog4j.LlSMServerCat;
  }

  /** Used to indicate if 100rel is Required/Supported/Unsupported. */
  protected byte m_100relSupport;
  /** Flag to indicate if a non-100 Provisional Response is sent or not */
  protected boolean m_non100ResponseSent = false;

  /**
   * The RSeq number. This is to make it increase by one, instead of random'y generated every time.
   */
  protected long m_rseqNumber = 0;

  public void set100relSupport(byte attribute) {
    if (m_non100ResponseSent == true) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn(
            "set100relSupport(): Try to set the 100rel attribute after non-100 response is sent");
      }
    } else {
      if (attribute == DsSipConstants.REQUIRE
          || attribute == DsSipConstants.SUPPORTED
          || attribute == DsSipConstants.UNSUPPORTED) {
        m_100relSupport = attribute;
      } else {
        if (genCat.isEnabled(Level.WARN)) {
          genCat.log(
              Level.WARN,
              "set100relSupport(): Try to set the 100rel attribute to an invalid value: "
                  + attribute);
        }
      }
    }
  }

  public byte get100relSupport() {
    return m_100relSupport;
  }

  /**
   * Sets the starting RSeq number for reliable provisional response.
   *
   * @param rseq Starting RSeq number
   * @throws IllegalArgumentException if starting RSeq number is out of valid range [1, 2**32-1]
   * @throws IllegalStateException if first reliable provisional response has already been sent out
   */
  public synchronized void setStartingRSeq(long rseq) {
    if (m_rseqNumber > 0)
      throw new IllegalStateException("First reliable provisional response has been sent.");
    if (rseq < MIN_RSEQ || rseq > MAX_RSEQ) {
      throw new IllegalArgumentException(
          "Invalid RSeq number: "
              + rseq
              + ". Should be in range ["
              + MIN_RSEQ
              + ", "
              + MAX_RSEQ
              + "]");
    }
    m_rseqNumber = rseq;
  }

  /**
   * Returns the last reponse sent. This may or may not be a copy. User code should not modify the
   * returned response.
   *
   * @return the last response sent, which may or may not be a copy
   */
  public abstract DsSipResponse getResponse();
}
