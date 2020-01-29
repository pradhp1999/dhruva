// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.DsLibs.DsSipObject;

import com.cisco.dhruva.DsLibs.DsUtil.*;

/** This class represents the ACK message as specified in RFC 3261. */
public final class DsSipAckMessage extends DsSipRequest {
  /** The default constructor. */
  protected DsSipAckMessage() {
    super(ACK);
  }

  /**
   * Constructs this ACK message.
   *
   * @param encoded encodes the ACK, if this is true
   */
  protected DsSipAckMessage(boolean encoded) {
    super(ACK, encoded);
  }

  /**
   * Constructs this ACK message from the specified <code>response</code> and sets the body and body
   * type in this constructed ACK message, if the specified <code>body</code> and the body <code>
   * type</code> are not <code>null</code>.
   *
   * @param response the sipResponse.
   * @param body the ACK message body, <code>null</code> ok.
   * @param type the body content type, <code>null</code> ok.
   * @throws IllegalArgumentException if the response status code is 1xx or the method name in CSeq
   *     header of response is not INVITE.
   */
  public DsSipAckMessage(DsSipResponse response, byte[] body, DsByteString type) {
    super(ACK);
    int status = (response.getStatusCode()) / 100;
    if (status == 1) {
      throw new IllegalArgumentException("Can't create ACK for a provisional response");
    }

    // CSeq header
    DsByteString method = response.getCSeqMethod();
    if (null == method || !method.equalsIgnoreCase(BS_INVITE)) {
      throw new IllegalArgumentException(
          "Can't ACK method: " + method + ". The SIP ACK Request is only for INVITE Response");
    }
    // CSeq Header
    setCSeqNumber(response.getCSeqNumber());
    setCSeqMethod(BS_ACK);

    // From header
    updateHeader(response.getHeader(FROM));
    // To header
    DsSipHeaderInterface toHeader = (DsSipHeaderInterface) response.getHeader(TO).clone();
    updateHeader(toHeader, false);

    // Contact header
    // contact header is useful for ack only for 200 response. When other final
    // response is received, ack's request-URI should be the same as that of the
    // original request message. This is handled in Low-Level API. This solution
    // is from Robert Sparks.
    DsSipHeaderInterface cHeader = response.getHeader(CONTACT);

    try {
      DsURI uri =
          (status == 2 && cHeader != null)
              ? createURI(cHeader, true)
              // temporarily set to To header, will be set to original request-URI in Low-Level
              : createURI(toHeader, false);
      setURI(uri);
    } catch (Exception exc) {
      // exc.printStackTrace();
      // What do we do?
    }
    // Call-Id header
    setCallId(response.getCallId());
    // Body
    setBody(body, type);
    // addSessionIdHeader
    addSessionIDHeader();
  }
}
