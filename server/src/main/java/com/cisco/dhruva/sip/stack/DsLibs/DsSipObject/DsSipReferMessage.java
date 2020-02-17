// Copyright (c) 2005 by Cisco Systems, Inc.
// All rights reserved.

package com.cisco.dhruva.sip.stack.DsLibs.DsSipObject;

/** This class represents the REFER message as specified by draft-ietf-sip-refer-06.txt. */
public final class DsSipReferMessage extends DsSipRequest {
  /** Default constructor. */
  protected DsSipReferMessage() {
    super(REFER);
  }

  /**
   * Constructs this REFER request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>bodyType
   * </code> and the specified <code>body</code>. The specified headers, <code>from</code>, <code>to
   * </code> and <code>contact</code> will be cloned.
   *
   * @param from the from header object.
   * @param to the to header object.
   * @param contact the contact header object.
   * @param callId the call id.
   * @param cSeqNo the sequence number.
   * @param bodyType the body type.
   * @param body the body data.
   */
  public DsSipReferMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo,
      DsByteString bodyType,
      byte[] body) {
    this(from, to, contact, callId, cSeqNo, bodyType, body, true);
  }

  /**
   * Constructs this REFER request with the specified <code>from</code> header, <code>to</code>
   * header, <code>contact</code> header, <code>callId</code>, <code>cSeqNo</code>, <code>bodyType
   * </code> and the specified <code>body</code>. The specified headers, <code>fromHeader</code>,
   * <code>toHeader</code> and <code>contactHeader</code> will be cloned if the specified option
   * <code>clone</code> is <code>true</code>.
   *
   * @param from the from header object.
   * @param to the to header object.
   * @param contact the contact header object.
   * @param callId the call id.
   * @param cSeqNo the sequence number.
   * @param bodyType the body type.
   * @param body the body data.
   * @param clone if true, it will clone the headers, else just use the references.
   */
  public DsSipReferMessage(
      DsSipFromHeader from,
      DsSipToHeader to,
      DsSipContactHeader contact,
      DsByteString callId,
      long cSeqNo,
      DsByteString bodyType,
      byte[] body,
      boolean clone) {
    super(REFER, from, to, contact, callId, cSeqNo, bodyType, body, clone);
  }
}
