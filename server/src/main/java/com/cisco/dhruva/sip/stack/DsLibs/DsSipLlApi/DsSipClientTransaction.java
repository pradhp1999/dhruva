// Copyright (c) 2005-2008 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsByteString;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipAckMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipCancelMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipConstants;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipPRACKMessage;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsException;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsLog4j;
import com.cisco.dhruva.util.log.Logger;
import java.io.IOException;
import java.net.UnknownHostException;
import org.slf4j.event.Level;

/**
 * This is an abstract class which supports the client side of a SIP transaction. It supports both
 * upcalls from the transaction manager and user code control of protocol timers and messages.
 */
public abstract class DsSipClientTransaction
    implements DsSipTransaction, DsSipStateMachineDefinitions, java.io.Serializable {
  /**
   * Called by user code to confirm a final response to an INVITE request by sending an ACK request.
   * If no ACK message is provided, a default ACK message will be sent. The default
   * DsSipClientTransactionInterface.finalResponse() implementation calls this method automatically.
   *
   * @param request optional ACK message to be sent to server
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @throws UnknownHostException not thrown any more, but its there for backward compatibility and
   *     may be removed in the next release
   */
  public abstract void ack(DsSipAckMessage request)
      throws DsException, IOException, UnknownHostException;

  /**
   * Called by user code to cancel a transaction. This method can only be invoked if no final
   * response has been received. It will cause a CANCEL request to be sent. If no CANCEL is provided
   * a default is built. Note that a final response to the request may still arrive in which case
   * the final response method of the client transaction interface is invoked. If no final response
   * is received, the timeout method of the client transaction interface is called.
   *
   * @param request The CANCEL message to be sent to server
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public abstract void cancel(DsSipCancelMessage request) throws DsException, IOException;

  /**
   * Call this version of cancel() when you want to handle the responses for CANCEL request you are
   * sending out. Otherwise, it is the same as the other version.
   *
   * @param cancelInterface An interface to handle CANCEl responses.
   * @param request The CANCEL message to be sent to server
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   */
  public abstract void cancel(
      DsSipClientTransactionInterface cancelInterface, DsSipCancelMessage request)
      throws DsException, IOException;

  // CAFFEINE 2.0 DEVELOPMENT - Add (EDCS-295391) PRACK Support

  /**
   * Called by user code to prack a reliable provisional response. Users can create their own PRACK
   * and call this method. If null is passed in, the stack will create one based on the reliable 1xx
   * response that's being PRACK'd.
   *
   * @param request The PRACK message to be sent to server
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @return PRACK client transaction.
   */
  public abstract DsSipClientTransaction prack(DsSipPRACKMessage request)
      throws DsException, IOException;

  /**
   * Call this version of prack() when you want to handle the responses for PRACK request you are
   * sending out. Otherwise, it is the same as the other version. The progress on prack transaction
   * will be reported to client transaction interface.
   *
   * @param clientInterface An interface to handle PRACK (the same one also handles ACK and CANCEL).
   * @param request The PRACK request. This request is created by the user code. If it's null, the
   *     stack will create one based on the 1xx response it wants to PRACK.
   * @throws DsException if there is an exception in the state machine
   * @throws IOException not thrown any more, but its there for backward compatibility and may be
   *     removed in the next release
   * @return PRACK client transaction.
   */
  public abstract DsSipClientTransaction prack(
      DsSipClientTransactionInterface clientInterface, DsSipPRACKMessage request)
      throws DsException, IOException;

  /**
   * Handles an incoming response delivered by the transaction manager.
   *
   * @param response the incoming response
   */
  protected abstract void onResponse(DsSipResponse response);

  /*
   * CAFFEINE 2.0 DEVELOPMENT
   * The following method was moved to DsSipTransaction.java:
   * protected abstract void onIOException(IOException ioe);
   */

  /**
   * Gets the To tag associated with this client transaction.
   *
   * @return the To tag associated with this client transaction (null if there is none)
   */
  protected abstract DsByteString getToTag();

  /**
   * Set the To tag associated with this client transaction.
   *
   * @param toTag the To tag associated with this client transaction
   */
  protected abstract void setToTag(DsByteString toTag);

  /**
   * Creates a copy of this client transaction based on a received multiple final response. This
   * method creates a new client transaction that already has its request and response set, and is
   * ready for the user to ACK.
   *
   * @param response the received multiple final response
   * @return copy of this transaction
   * @throws DsException if there is an exception while creating a copy
   */
  protected abstract DsSipClientTransaction createCopy(DsSipResponse response) throws DsException;

  /**
   * Handles an incoming multiple final response delivered by the transaction manager. Note - this
   * only applies to INVITE client transactions.
   *
   * @param originalTransaction the original (INVITE) client transaction
   * @param response the incoming multiple final response
   */
  protected abstract void onMultipleFinalResponse(
      DsSipClientTransaction originalTransaction, DsSipResponse response);

  /**
   * Checks if multiple final response handling is enabled for this client transaction.
   *
   * @return boolean indicating if multiple final responses are enabled.
   */
  protected abstract boolean multipleFinalResponsesEnabled();

  /**
   * Cancels the Tn timer for this transaction. Additionally this method facilitate the
   * implementation of Timer C functionality for the proxy application.<br>
   * On start of any Client Transaction, Timer Tn is scheduled, that decides the life time of the
   * transaction. For an application ( for example, proxy server) to be able to extend the life time
   * of such a transaction, this method and {@link #terminate()} methods are provided. The
   * application can invoke {@link #cancelTn()} first and then can set its own timer (Timer C), and
   * when this timer fires, the application can invoke {@link #terminate()} to terminate this
   * transaction. Note that this method should only be invoked by proxy server when it is handling
   * Timer C firing for an invite client transaction.
   */
  public abstract void cancelTn();

  /**
   * Terminates this transaction and free up the resources. Additionally this method facilitate the
   * implementation of Timer C functionality for the proxy application.<br>
   * Note that this method should only be invoked by proxy server when it is handling Timer C firing
   * for an invite client transaction.
   *
   * @see #cancelTn()
   */
  public abstract void terminate();

  // CAFFEINE 2.0 DEVELOPMENT - Add new methods

  /** General logging Category. */
  protected static Logger genCat; // = null;

  static {
    genCat = DsLog4j.LlSMClientCat;
  }

  /** Used to indicate if 100rel is Required/Supported/Unsupported. */
  protected byte m_100relSupport;

  public void set100relSupport(byte attribute) {
    if (isStarted()) {
      if (genCat.isEnabled(Level.WARN)) {
        genCat.warn(
            "set100relSupport(): Try to set the 100rel attribute after the state machine is started");
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
}
