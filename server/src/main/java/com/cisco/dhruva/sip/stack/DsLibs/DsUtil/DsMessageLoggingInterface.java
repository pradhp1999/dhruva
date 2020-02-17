// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsUtil;

import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipRequest;
import com.cisco.dhruva.sip.stack.DsLibs.DsSipObject.DsSipResponse;

/** Interface for message logging. */
public interface DsMessageLoggingInterface {
  /** For incoming requests and responses. */
  public static final byte DIRECTION_IN = 0;

  /** For incoming requests and responses. */
  public static final byte DIRECTION_OUT = 1;

  /**
   * The regular incoming request/response which is not a retransmission and is not a stray ACK or
   * response.
   */
  public static final int REASON_REGULAR = 0;

  /** The message was generated internally at the user's request. */
  public static final int REASON_GENERATED = 1;

  /** The message was sent automatically by the UA stack. */
  public static final int REASON_AUTO = 2;

  /** The message is malformed. */
  public static final int REASON_MALFORMED = 3;

  /** The message is a merged request. */
  public static final int REASON_MERGED = 4;

  /** The message is a retransmission, either incoming or outgoing. */
  public static final int REASON_RETRANSMISSION = 5;

  /** The message is a request for which there is no handler. */
  public static final int REASON_NO_HANDLER = 6;

  /** The message is a request received in an overloaded state. */
  public static final int REASON_OVERLOADED = 7;

  /** The message is a request received in shutdown state. */
  public static final int REASON_SHUTDOWN = 8;

  /** Reached max hops. */
  public static final int REASON_MAXHOPS = 9;

  /** Represents a stray ACK or Response. */
  public static final int REASON_STRAY = 10;

  /** Represents a TCP or TLS socket being closed. These messages were dropped. */
  public static final int REASON_STREAM_CLOSED = 11;

  /** sip message normalization state */
  public static enum SipMsgNormalizationState {
    UNMODIFIED,
    PRE_NORMALIZED,
    POST_NORMALIZED
  }

  /**
   * Notification for the specified <code>request</code> and whether its incoming or outgoing is
   * specified by <code>direction</code> flag and what was the reason of this request is specified
   * by <code>reason</code> whose possible values are defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the request being received or sent. The various possible values are
   *     defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the <code>request
   *     </code> was received, otherwise the <code>request</code> was sent.
   * @param request the SIP request that is either received or sent.
   */
  public void logRequest(int reason, byte direction, DsSipRequest request);

  /**
   * Notification for the specified <code>response</code> and whether its incoming or outgoing is
   * specified by <code>direction</code> flag and what was the reason of this response is specified
   * by <code>reason</code> whose possible values are defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the response being received or sent. The various possible values
   *     are defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the <code>response
   *     </code> was received, otherwise the <code>response</code> was sent.
   * @param response the SIP response that is either received or sent.
   */
  public void logResponse(int reason, byte direction, DsSipResponse response);

  /**
   * Notification for the specified <code>response</code> and whether its incoming or outgoing is
   * specified by <code>direction</code> flag and what was the reason of this response is specified
   * by <code>reason</code> whose possible values are defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the response being received or sent. The various possible values
   *     are defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the <code>response
   *     </code> was received, otherwise the <code>response</code> was sent.
   * @param response the SIP response that is either received or sent.
   * @param request the SIP request that is either received or sent.
   */
  public void logResponse(int reason, byte direction, DsSipResponse response, DsSipRequest request);

  /**
   * Notification for the specified <code>request</code> and whether its incoming or outgoing is
   * specified by <code>direction</code> flag and what was the reason of this request is specified
   * by <code>reason</code> whose possible values are defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the request being received or sent. The various possible values are
   *     defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the <code>request
   *     </code> was received, otherwise the <code>request</code> was sent.
   * @param request the SIP request byte array that is either received or sent.
   * @param method the request method.
   * @param bindingInfo the network binding information.
   */
  public void logRequest(
      int reason, byte direction, byte[] request, int method, DsBindingInfo bindingInfo);

  /**
   * Notification for the specified <code>response</code> and whether its incoming or outgoing is
   * specified by <code>direction</code> flag and what was the reason of this response is specified
   * by <code>reason</code> whose possible values are defined in {@link DsMessageLoggingInterface}.
   *
   * @param reason The reason of the response being received or sent. The various possible values
   *     are defined in {@link DsMessageLoggingInterface}.
   * @param direction if <code>DsMessageLoggingInterface.DIRECTION_IN</code> then the <code>response
   *     </code> was received, otherwise the <code>response</code> was sent.
   * @param response the SIP response bytes that is either received or sent.
   * @param statusCode the response status code.
   * @param method the response method name as defined by the CSeq header method name value.
   * @param bindingInfo the network binding information.
   */
  public void logResponse(
      int reason,
      byte direction,
      byte[] response,
      int statusCode,
      int method,
      DsBindingInfo bindingInfo,
      DsSipRequest request);

  public void logResponse(
      int reason,
      byte direction,
      byte[] response,
      int statusCode,
      int method,
      DsBindingInfo bindingInfo);
} // Ends DsMessageLoggingInterface
