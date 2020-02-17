// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipLlApi;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;
import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.DsMessageLoggingInterface;

/**
 * This interface allows the user code to hear about SIP messages which otherwise would not be
 * visible. These include but are not limited to:
 *
 * <p>
 *
 * <ul>
 *   <li>Messages which the stack generates internally at the user's request
 *       <p>Examples: Low-Level user calls sendResponse() (defaults to a 100 response); Outgoing
 *       request or response retransmissions
 *   <li>Requests which we respond to on behalf of the user
 *       <p>Examples: requests when we are in an overloaded state; requests that are malformed;
 *       requests that are merged; requests for which that there is no registered handler; requests
 *       when we are overloaded
 *   <li>Requests which we bury completely
 *       <p>Examples: incoming Request retransmissions
 *   <li>The responses we send automatically
 *       <p>Examples:
 *       <p>200/ 487 for CANCEL/ INVITE; 400 for malformed message; METHOD_NOT_ALLOWED for a request
 *       for which there is no handler; MERGED_REQUEST_RESPONSE for merged request; 503 or 300 for
 *       overloaded condition
 * </ul>
 *
 * @deprecated Use {@link DsMessageLoggingInterface DsMessageLoggingInterface}.
 */
public interface DsSipMessageLoggingInterface {
  /** For incoming requests and responses. */
  public static final byte DIRECTION_IN = 0;

  /** For incoming requests and responses. */
  public static final byte DIRECTION_OUT = 1;

  /** The message was generated internally at the user's request. */
  public static final int REASON_GENERATED = 0;

  /** The message was sent automatically by the UA stack. */
  public static final int REASON_AUTO = 1;

  /** The message is malformed. */
  public static final int REASON_MALFORMED = 2;

  /** The message is a merged request. */
  public static final int REASON_MERGED = 3;

  /** The message is an incoming retransmission. */
  public static final int REASON_INCOMING_RTX = 4;

  /** The message is an outgoing retransmission. */
  public static final int REASON_OUTGOING_RTX = 5;

  /** The message is a request for which there is no handler. */
  public static final int REASON_NO_HANDLER = 6;

  /** The message is a request received in an overloaded state. */
  public static final int REASON_OVERLOADED = 7;

  /** The message is a request received in shutdown state. */
  public static final int REASON_SHUTDOWN = 8;

  /** Reached max hops. */
  public static final int REASON_MAXHOPS = 9;

  /**
   * Log a request. This method is called by the stack to notify the user code of a request that
   * would otherwise be hidden to it.
   *
   * @param reason the reason
   * @param direction the direction
   * @param request the SIP request
   */
  public void logRequest(int reason, byte direction, DsSipRequest request);

  /**
   * Log a response. This method is called by the stack to notify the user code of a response that
   * would otherwise be hidden to it.
   *
   * @param reason the reason
   * @param direction the direction
   * @param response the SIP response
   */
  public void logResponse(int reason, byte direction, DsSipResponse response);

  /**
   * Log a request. This method is called by the stack to notify the user code of a request that
   * would otherwise be hidden to it.
   *
   * @param reason the reason
   * @param direction the direction
   * @param request the SIP request
   */
  public void logRequest(int reason, byte direction, byte[] request);

  /**
   * Log a response. This method is called by the stack to notify the user code of a response that
   * would otherwise be hidden to it.
   *
   * @param reason the reason
   * @param direction the direction
   * @param response the SIP response
   */
  public void logResponse(int reason, byte direction, byte[] response);
}
