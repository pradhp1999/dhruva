// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

import com.cisco.dhruva.sip.stack.DsLibs.DsUtil.*;

/** This class represents the Cancel message as specified in RFC 3261. */
public final class DsSipCancelMessage extends DsSipRequest {
  /** Default constructor. */
  protected DsSipCancelMessage() {
    super(CANCEL);
  }

  protected DsSipCancelMessage(boolean encoded) {
    super(CANCEL, encoded);
  }

  /**
   * Constructs this CANCEL message from the specified SIP Request <code>request</code>. It clones
   * the To, From and Contact headers in the specified request before adding them in this CANCEL
   * message.
   *
   * @param request the SIP request.
   * @throws IllegalArgumentException if the passed in request is null.
   */
  public DsSipCancelMessage(DsSipRequest request) {
    super(CANCEL);
    if (request == null) {
      throw new IllegalArgumentException("Can not create a CANCEL message from a null Request");
    }

    // Use the entire binding info, rather than just the remote host and port
    setBindingInfo((DsBindingInfo) request.getBindingInfo().clone());

    updateHeader((DsSipHeaderInterface) request.getFromHeader());
    updateHeader((DsSipHeaderInterface) request.getToHeader());
    setCallId(request.getCallId());
    // Not required any more
    // --        updateHeader(new DsSipContentLengthHeader(0), false);
    m_lCSeq = request.m_lCSeq;
    m_strCSeq = BS_CANCEL;
    setURI(request.getURI());
  }
}
